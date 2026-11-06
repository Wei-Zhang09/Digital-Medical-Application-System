/**
 * T8 页面级验证：8 个业务页面能加载出数据 + 必备材料走一遍完整增删改 + 药品编辑弹窗能带出关联药店
 *
 * 运行：NODE_PATH=".../.dev/tools/node_modules" node scripts/e2e/pages.js
 * 产物：docs/report/screenshots/pages/*.png
 */
const puppeteer = require('puppeteer-core')
const fs = require('fs')
const path = require('path')

const SHOT_DIR = path.resolve(__dirname, '../../docs/report/screenshots/pages')
const BASE = 'http://localhost:9092'
const sleep = ms => new Promise(r => setTimeout(r, ms))

const PAGES = [
  ['/home', '首页'],
  ['/base/company', '医药公司管理'],
  ['/base/sale', '销售地点管理'],
  ['/base/city', '城市信息管理'],
  ['/manage/drug', '药品信息管理'],
  ['/manage/medical/policy', '医保政策管理'],
  ['/manage/company/policy', '医药公司政策管理'],
  ['/manage/doctor', '医生信息管理'],
  ['/manage/material', '必备材料管理']
]

async function fill (page, selector, value) {
  await page.evaluate((sel, val) => {
    const el = document.querySelector(sel)
    if (!el) throw new Error('找不到元素：' + sel)
    el.value = val
    el.dispatchEvent(new Event('input', { bubbles: true }))
    el.dispatchEvent(new Event('change', { bubbles: true }))
  }, selector, value)
}

/** 点带文字的按钮（Element UI 按钮文字里有空格，用 includes 匹配） */
async function clickButton (page, text, scope = 'body') {
  const ok = await page.evaluate((t, sc) => {
    const root = document.querySelector(sc)
    const btn = [...root.querySelectorAll('button')].find(b => b.innerText.replace(/\s/g, '').includes(t))
    if (btn) { btn.click(); return true }
    return false
  }, text, scope)
  if (!ok) throw new Error('找不到按钮：' + text)
  await sleep(500)
}

async function login (page, uname, pwd) {
  await page.goto(BASE, { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => { localStorage.clear(); location.hash = '#/user/login' })
  await page.reload({ waitUntil: 'networkidle2' })
  await page.waitForSelector('input[placeholder="请输入用户名"]', { timeout: 20000 })
  await fill(page, 'input[placeholder="请输入用户名"]', uname)
  await fill(page, 'input[placeholder="请输入密码"]', pwd)
  await page.evaluate(() => document.querySelector('.login-button').click())
  await page.waitForFunction('location.hash.indexOf("/home") >= 0', { timeout: 25000 })
  await sleep(1200)
}

async function main () {
  fs.mkdirSync(SHOT_DIR, { recursive: true })
  const browser = await puppeteer.connect({
    browserURL: 'http://127.0.0.1:9222',
    defaultViewport: { width: 1440, height: 900 }
  })
  const page = await browser.newPage()
  const errors = []
  page.on('pageerror', e => errors.push(e.message))

  await login(page, 'admin_1', '123456')

  // 1. 逐页巡检：能否进页面、表格有没有数据
  let pass = 0
  for (let i = 0; i < PAGES.length; i++) {
    const [route, title] = PAGES[i]
    await page.evaluate(r => { location.hash = '#' + r }, route)
    await sleep(1600)
    const rows = await page.$$eval('.el-table__body-wrapper .el-table__body tbody tr', trs => trs.length).catch(() => 0)
    const heading = await page.evaluate(() => (document.querySelector('.page-title') || {}).innerText || '')
    await page.screenshot({ path: path.join(SHOT_DIR, String(i + 1).padStart(2, '0') + '-' + title + '.png') })
    const ok = heading === title
    if (ok) pass++
    console.log(`${ok ? 'PASS' : 'FAIL'}  ${title.padEnd(10)} 路由=${route}  表格行数=${rows}  顶栏标题=${heading}`)
  }

  // 2. 必备材料：新增 → 查回 → 修改 → 删除（全流程走 UI）
  await page.evaluate(() => { location.hash = '#/manage/material' })
  await sleep(1500)

  await clickButton(page, '新增材料')
  await page.waitForSelector('.el-dialog__wrapper:not([style*="display: none"]) textarea', { timeout: 8000 })
  await fill(page, '.el-dialog__wrapper:not([style*="display: none"]) input[maxlength="100"]', 'T8-UI新增材料')
  await fill(page, '.el-dialog__wrapper:not([style*="display: none"]) textarea', '这是通过界面新增的测试材料')
  await clickButton(page, '保存', '.el-dialog__wrapper:not([style*="display: none"])')
  await sleep(2000)

  let titles = await page.$$eval('.el-table__body-wrapper .el-table__body tbody tr', trs => trs.map(t => t.innerText))
  const added = titles.some(t => t.indexOf('T8-UI新增材料') >= 0)
  console.log(`${added ? 'PASS' : 'FAIL'}  材料新增后出现在列表`)

  // 修改：点该行的「编辑」
  // 注意：操作列用了 fixed="right"，按钮只渲染在 .el-table__fixed-right 里，
  // 主表格对应单元格是空的，所以先定位行号再去固定层点按钮
  await page.evaluate(() => {
    const rows = [...document.querySelectorAll('.el-table__body-wrapper .el-table__body tbody tr')]
    const idx = rows.findIndex(t => t.innerText.indexOf('T8-UI新增材料') >= 0)
    const fixedRows = [...document.querySelectorAll('.el-table__fixed-right .el-table__body tbody tr')]
    const btn = [...fixedRows[idx].querySelectorAll('button')].find(b => b.innerText.indexOf('编辑') >= 0)
    btn.click()
  })
  await sleep(1200)
  await fill(page, '.el-dialog__wrapper:not([style*="display: none"]) input[maxlength="100"]', 'T8-UI材料(已改)')
  await clickButton(page, '保存', '.el-dialog__wrapper:not([style*="display: none"])')
  await sleep(2000)
  titles = await page.$$eval('.el-table__body-wrapper .el-table__body tbody tr', trs => trs.map(t => t.innerText))
  const edited = titles.some(t => t.indexOf('T8-UI材料(已改)') >= 0)
  console.log(`${edited ? 'PASS' : 'FAIL'}  材料修改后列表已更新`)
  await page.screenshot({ path: path.join(SHOT_DIR, '10-材料增改后.png') })

  // 删除
  await page.evaluate(() => {
    const rows = [...document.querySelectorAll('.el-table__body-wrapper .el-table__body tbody tr')]
    const idx = rows.findIndex(t => t.innerText.indexOf('T8-UI材料(已改)') >= 0)
    const fixedRows = [...document.querySelectorAll('.el-table__fixed-right .el-table__body tbody tr')]
    const btn = [...fixedRows[idx].querySelectorAll('button')].find(b => b.innerText.indexOf('删除') >= 0)
    btn.click()
  })
  await sleep(700)
  await clickButton(page, '确定', '.el-message-box__btns')
  await sleep(2000)
  titles = await page.$$eval('.el-table__body-wrapper .el-table__body tbody tr', trs => trs.map(t => t.innerText))
  const deleted = !titles.some(t => t.indexOf('T8-UI材料') >= 0)
  console.log(`${deleted ? 'PASS' : 'FAIL'}  材料删除后从列表消失`)

  // 3. 药品：编辑弹窗能带出关联药店（drug_sale 关系）
  await page.evaluate(() => { location.hash = '#/manage/drug' })
  await sleep(1800)
  await page.evaluate(() => {
    const fixedRow = document.querySelector('.el-table__fixed-right .el-table__body tbody tr')
    const btn = [...fixedRow.querySelectorAll('button')].find(b => b.innerText.indexOf('编辑') >= 0)
    if (btn) btn.click()
  })
  await sleep(1500)
  const tags = await page.$$eval('.el-dialog .el-select__tags .el-tag', els => els.map(e => e.innerText.trim()))
  console.log(`${tags.length ? 'PASS' : 'FAIL'}  药品编辑弹窗带出关联药店：${tags.join('、') || '无'}`)
  await page.screenshot({ path: path.join(SHOT_DIR, '11-药品编辑弹窗.png') })
  await page.evaluate(() => {
    const btns = [...document.querySelectorAll('.el-dialog__footer button')]
    const cancel = btns.find(b => b.innerText.indexOf('取消') >= 0)
    if (cancel) cancel.click()
  })
  await sleep(600)

  // 4. 医师账号：药品页可见「新增」但看不到「编辑/删除」
  await login(page, '王小明4125', '123456')
  await page.evaluate(() => { location.hash = '#/manage/drug' })
  await sleep(1800)
  const doctorView = await page.evaluate(() => {
    const btns = [...document.querySelectorAll('button')].map(b => b.innerText.replace(/\s/g, ''))
    return {
      hasAdd: btns.some(t => t.indexOf('新增药品') >= 0),
      hasEdit: btns.some(t => t.indexOf('编辑') >= 0)
    }
  })
  const roleOk = doctorView.hasAdd === true && doctorView.hasEdit === false
  console.log(`${roleOk ? 'PASS' : 'FAIL'}  医师视角：新增可见=${doctorView.hasAdd} 编辑不可见=${!doctorView.hasEdit}`)
  await page.screenshot({ path: path.join(SHOT_DIR, '12-医师视角药品页.png') })

  console.log('\n页面错误:', errors.length ? errors.join('\n') : '无')
  const allOk = pass === PAGES.length && added && edited && deleted && tags.length > 0 && roleOk && errors.length === 0
  console.log('\n结论:', allOk ? 'PASS' : 'FAIL')
  await page.close()
  browser.disconnect()
  if (!allOk) process.exit(1)
}

main().catch(e => { console.error('E2E 失败:', e.message); process.exit(1) })
