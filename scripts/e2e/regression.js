/**
 * 全量回归：两个角色 × 全部页面 + 本轮新增功能冒烟。
 * 输出 JSON 报告，供整理验证清单使用。
 */
const puppeteer = require('puppeteer-core')
const BASE = 'http://127.0.0.1:9093'
const OUT = 'D:/HermesWorkSpace/Digital Medical Application System/docs/report/screenshots'
const sleep = ms => new Promise(r => setTimeout(r, ms))

async function setInput (page, el, v) {
  await page.evaluate((node, val) => {
    node.value = val
    node.dispatchEvent(new Event('input', { bubbles: true }))
    node.dispatchEvent(new Event('change', { bubbles: true }))
  }, el, v)
}

async function fresh (page) {
  await page.goto(BASE + '/', { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => localStorage.clear())
  await page.goto('about:blank')
}

async function login (page, user, pass) {
  await fresh(page)
  await page.goto(BASE + '/#/user/login', { waitUntil: 'domcontentloaded' })
  await page.waitForSelector('.login-card .el-input__inner', { timeout: 30000 })
  await sleep(600)
  const ins = await page.$$('.login-card .el-input__inner')
  await setInput(page, ins[0], user)
  await setInput(page, ins[1], pass)
  await page.evaluate(() => document.querySelector('.login-button').click())
  await sleep(3500)
  return page.evaluate(() => location.hash)
}

const ADMIN_ROUTES = [
  ['/home', '首页'], ['/base/company', '医药公司'], ['/base/sale', '销售地点'], ['/base/city', '城市信息'],
  ['/manage/drug', '药品信息'], ['/manage/medical/policy', '医保政策'], ['/manage/company/policy', '公司政策'],
  ['/manage/doctor', '医生信息'], ['/manage/material', '必备材料'], ['/manage/account', '账号管理'], ['/manage/log', '操作日志']
]

;(async () => {
  const b = await puppeteer.connect({ browserURL: 'http://127.0.0.1:9222', defaultViewport: null })
  const page = await b.newPage()
  page.setDefaultTimeout(30000)
  await page.setViewport({ width: 1440, height: 900 })

  const httpErr = new Set(), pageErr = new Set()
  page.on('response', r => {
    const u = r.url()
    if (r.status() >= 400 && u.includes('/api/')) httpErr.add(r.status() + ' ' + u.replace('http://127.0.0.1:8081', ''))
  })
  page.on('pageerror', e => pageErr.add(String(e).slice(0, 120)))

  const R = { admin: {}, doctor: {}, features: {}, httpErr: [], pageErr: [] }

  try {
    // ============ 管理员 ============
    R.admin.login = await login(page, 'admin_1', '123456')
    R.admin.menus = await page.evaluate(() => [...document.querySelectorAll('.el-menu-item')].map(m => m.innerText.trim()))

    for (const [path, name] of ADMIN_ROUTES) {
      await page.goto(BASE + '/#' + path, { waitUntil: 'domcontentloaded' })
      await sleep(1300)
      R.admin[name] = await page.evaluate(() => ({
        rows: document.querySelectorAll('.el-table__body-wrapper tbody tr').length,
        btns: [...document.querySelectorAll('.toolbar button, .toolbar-right button')].map(x => x.innerText.trim()),
        opCol: [...document.querySelectorAll('.el-table th')].some(t => t.innerText.trim() === '操作')
      }))
    }

    // ---- 新功能 1：个人中心弹窗 ----
    await page.evaluate(() => document.querySelector('.user-info').click())
    await sleep(500)
    await page.evaluate(() => [...document.querySelectorAll('.el-dropdown-menu__item')].find(i => i.innerText.trim() === '个人中心').click())
    await sleep(900)
    R.features.profile = await page.evaluate(() => ({
      title: (document.querySelector('.el-dialog__title') || {}).innerText || '',
      info: (document.querySelector('.profile-info') || {}).innerText.replace(/\n/g, ' | ') || '',
      pwdInputs: document.querySelectorAll('.el-dialog .el-input__inner').length
    }))
    await page.evaluate(() => {
      const d = document.querySelector('.el-dialog__wrapper')
      const btn = [...d.querySelectorAll('button')].find(b => b.innerText.includes('取消'))
      if (btn) btn.click()
    })
    await sleep(600)

    // ---- 新功能 2：首页资讯可点 ----
    await page.goto(BASE + '/#/home', { waitUntil: 'domcontentloaded' })
    await sleep(1800)
    await page.evaluate(() => document.querySelector('.news-table .el-table__row').click())
    await sleep(1500)
    R.features.news = await page.evaluate(() => ({
      dialogTitle: (document.querySelector('.el-dialog__title') || {}).innerText || '',
      newsTitle: (document.querySelector('.news-title') || {}).innerText || '',
      contentLen: ((document.querySelector('.news-content') || {}).innerText || '').length,
      hasGoBtn: [...document.querySelectorAll('.el-dialog button')].some(x => x.innerText.includes('前往该模块'))
    }))
    await page.evaluate(() => {
      const btn = [...document.querySelectorAll('.el-dialog button')].find(x => x.innerText.includes('关'))
      if (btn) btn.click()
    })
    await sleep(500)

    // ---- 新功能 3：排序（药品按药名）----
    await page.goto(BASE + '/#/manage/drug', { waitUntil: 'domcontentloaded' })
    await sleep(1600)
    const before = await page.evaluate(() => [...document.querySelectorAll('.el-table__body-wrapper tbody tr')].map(r => r.children[1].innerText.trim()))
    await page.evaluate(() => {
      const th = [...document.querySelectorAll('.el-table th')].find(t => t.innerText.includes('药名'))
      th.click()
    })
    await sleep(1500)
    const after = await page.evaluate(() => [...document.querySelectorAll('.el-table__body-wrapper tbody tr')].map(r => r.children[1].innerText.trim()))
    R.features.sort = { before: before.slice(0, 3), after: after.slice(0, 3), changed: JSON.stringify(before) !== JSON.stringify(after) }

    // ---- 新功能 4：多条件筛选（药品成分）----
    const din = await page.$$('.toolbar .search-input input')
    await setInput(page, din[1], '连翘')
    await page.evaluate(() => [...document.querySelectorAll('.toolbar button')].find(b => b.innerText.includes('查询')).click())
    await sleep(1600)
    R.features.filter = await page.evaluate(() => ({
      rows: document.querySelectorAll('.el-table__body-wrapper tbody tr').length,
      names: [...document.querySelectorAll('.el-table__body-wrapper tbody tr')].map(r => r.children[1].innerText.trim())
    }))

    // ---- 新功能 5：导出按钮存在且可点 ----
    const exportResp = []
    page.on('response', r => { if (r.url().includes('/api/export/')) exportResp.push(r.status()) })
    await page.evaluate(() => [...document.querySelectorAll('.toolbar button')].find(b => b.innerText.includes('导出')).click())
    await sleep(2500)
    R.features.export = { responses: exportResp }

    // ---- 新功能 6：注册页可达 ----
    await fresh(page)
    await page.goto(BASE + '/#/user/register', { waitUntil: 'domcontentloaded' })
    await page.waitForSelector('.register-card .el-input__inner', { timeout: 20000 })
    R.features.register = await page.evaluate(() => ({
      hash: location.hash,
      inputs: document.querySelectorAll('.register-card .el-input__inner').length,
      hasBackLink: document.body.innerText.includes('返回登录')
    }))

    // ============ 医师 ============
    R.doctor.login = await login(page, '王小明4125', '123456')
    R.doctor.menus = await page.evaluate(() => [...document.querySelectorAll('.el-menu-item')].map(m => m.innerText.trim()))
    for (const [path, name] of ADMIN_ROUTES) {
      await page.goto(BASE + '/#' + path, { waitUntil: 'domcontentloaded' })
      await sleep(1000)
      R.doctor[name] = await page.evaluate(() => ({
        hash: location.hash,
        rows: document.querySelectorAll('.el-table__body-wrapper tbody tr').length,
        opCol: [...document.querySelectorAll('.el-table th')].some(t => t.innerText.trim() === '操作'),
        hasAdd: [...document.querySelectorAll('.toolbar-right button')].some(b => b.innerText.includes('新增'))
      }))
    }
  } catch (e) {
    R.error = String(e).slice(0, 300)
  }

  R.httpErr = [...httpErr]
  R.pageErr = [...pageErr]
  console.log(JSON.stringify(R, null, 1))
  await page.close().catch(() => {})
  await b.disconnect()
  process.exit(0)
})()
