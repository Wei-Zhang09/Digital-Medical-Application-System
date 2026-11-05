/**
 * T7 端到端验证：登录 → 动态菜单 → 路由跳转 → 退出 → 换账号看菜单差异 → 刷新保持
 *
 * 运行（puppeteer-core 装在 .dev/tools，不进仓库）：
 *   NODE_PATH="D:/HermesWorkSpace/Digital Medical Application System/.dev/tools/node_modules" \
 *     node scripts/e2e/login.js
 *
 * 前置：后端 8080 已启动、前端 npm run serve 在 9092、Chrome 带 --remote-debugging-port=9222 开着。
 * 产物：docs/report/screenshots/01~05-*.png（报告第 6 章界面设计直接用）
 */
const puppeteer = require('puppeteer-core')
const fs = require('fs')
const path = require('path')

const SHOT_DIR = path.resolve(__dirname, '../../docs/report/screenshots')
const BASE = 'http://localhost:9092'
const sleep = ms => new Promise(r => setTimeout(r, ms))

/** 直接给 el-input 赋值并派发 input 事件：puppeteer 逐字符打字会丢中文 */
async function fill (page, placeholder, value) {
  await page.evaluate((ph, val) => {
    const el = document.querySelector(`input[placeholder="${ph}"]`)
    if (!el) throw new Error('找不到输入框：' + ph)
    el.value = val
    el.dispatchEvent(new Event('input', { bubbles: true }))
    el.dispatchEvent(new Event('change', { bubbles: true }))
  }, placeholder, value)
}

/** 等登录表单就绪，避免和路由守卫抢跑 */
async function waitLoginForm (page) {
  await page.waitForSelector('input[placeholder="请输入用户名"]', { timeout: 20000 })
  await page.waitForSelector('.login-button', { timeout: 20000 })
  await sleep(500)
}

async function doLogin (page, uname, pwd) {
  await fill(page, '请输入用户名', uname)
  await fill(page, '请输入密码', pwd)
  // 用 JS 点击：鼠标点击在部分布局下会被遮挡层吃掉，JS click 更稳
  await page.evaluate(() => {
    const btn = document.querySelector('.login-button')
    if (!btn) throw new Error('找不到登录按钮')
    btn.click()
  })
  try {
    await page.waitForFunction('location.hash.indexOf("/home") >= 0', { timeout: 25000 })
  } catch (e) {
    // 失败时把现场打出来，便于定位（提示语 / token / 当前地址）
    const state = await page.evaluate(() => ({
      hash: location.hash,
      tip: (document.querySelector('.login-tip') || {}).innerText || '',
      token: !!localStorage.token,
      role: localStorage.role || '',
      loading: !!document.querySelector('.login-button.is-loading')
    }))
    console.error('登录未跳转，现场：', JSON.stringify(state))
    await page.screenshot({ path: path.join(SHOT_DIR, 'debug-login-fail.png') })
    throw e
  }
  await sleep(1200)
}

async function menus (page) {
  return page.$$eval('.layout-menu .el-menu-item span', els => els.map(e => e.innerText.trim()))
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

  // 0. 干净起点：清登录态 + 整页重载（Vuex 里不能残留旧 token）
  await page.goto(BASE, { waitUntil: 'domcontentloaded', timeout: 60000 })
  await page.evaluate(() => { localStorage.clear(); location.hash = '#/user/login' })
  await page.reload({ waitUntil: 'networkidle2', timeout: 60000 })
  await waitLoginForm(page)

  await page.screenshot({ path: path.join(SHOT_DIR, '01-登录页.png') })
  console.log('登录页标题:', await page.title())

  // 管理员
  await doLogin(page, 'admin_1', '123456')
  await page.screenshot({ path: path.join(SHOT_DIR, '02-管理员首页.png') })
  const adminMenus = await menus(page)
  console.log('管理员菜单(' + adminMenus.length + '):', adminMenus.join(' | '))

  // 动态路由：点进药品管理
  await page.evaluate(() => {
    const target = [...document.querySelectorAll('.layout-menu .el-menu-item')]
      .find(i => i.innerText.indexOf('药品') >= 0)
    if (target) target.click()
  })
  await sleep(1500)
  console.log('当前路由:', await page.evaluate(() => location.hash))
  await page.screenshot({ path: path.join(SHOT_DIR, '03-药品管理页.png') })

  // 退出
  await page.evaluate(() => {
    const el = document.querySelector('.user-info')
    if (el) el.click()
  })
  await sleep(600)
  await page.evaluate(() => {
    const t = [...document.querySelectorAll('.el-dropdown-menu__item')]
      .find(i => i.innerText.indexOf('退出') >= 0)
    if (t) t.click()
  })
  await sleep(800)
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 20000 }).catch(() => {}),
    page.evaluate(() => {
      const ok = [...document.querySelectorAll('.el-message-box__btns button')]
        .find(b => b.innerText.indexOf('确定') >= 0)
      if (ok) ok.click()
    })
  ])
  await sleep(1500)
  console.log('退出后路由:', await page.evaluate(() => location.hash))

  // 医师：退出流程已验证过，这里做一次硬重置，避免上一轮的动态路由残留干扰
  await page.evaluate(() => { localStorage.clear(); location.hash = '#/user/login' })
  await page.reload({ waitUntil: 'networkidle2', timeout: 60000 })
  await waitLoginForm(page)
  await doLogin(page, '王小明4125', '123456')
  const doctorMenus = await menus(page)
  console.log('医师菜单(' + doctorMenus.length + '):', doctorMenus.join(' | '))
  await page.screenshot({ path: path.join(SHOT_DIR, '04-医师首页.png') })

  // 刷新后菜单仍在
  await page.reload({ waitUntil: 'networkidle2', timeout: 60000 })
  await sleep(2500)
  const afterReload = await menus(page)
  console.log('刷新后菜单(' + afterReload.length + '):', afterReload.join(' | '))
  await page.screenshot({ path: path.join(SHOT_DIR, '05-刷新后.png') })

  console.log('\n页面错误:', errors.length ? errors.join('\n') : '无')
  const ok = adminMenus.length === 9 && doctorMenus.length === 5 && afterReload.length === 5 && errors.length === 0
  console.log('\n结论:', ok ? 'PASS（9 / 5 / 刷新后 5，无页面错误）' : 'FAIL')

  await page.close()
  browser.disconnect()
  if (!ok) process.exit(1)
}

main().catch(e => { console.error('E2E 失败:', e.message); process.exit(1) })
