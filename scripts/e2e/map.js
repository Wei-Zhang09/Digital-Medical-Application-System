/**
 * T10 地图扩展验证：切换地图 → 标记渲染 → 选点新增 → 弹窗带出经纬度
 *
 * 运行：NODE_PATH=".../.dev/tools/node_modules" node scripts/e2e/map.js
 * 产物：docs/report/screenshots/map/*.png
 */
const puppeteer = require('puppeteer-core')
const fs = require('fs')
const path = require('path')

const SHOT_DIR = path.resolve(__dirname, '../../docs/report/screenshots/map')
const BASE = 'http://localhost:9092'
const sleep = ms => new Promise(r => setTimeout(r, ms))

async function fill (page, placeholder, value) {
  await page.evaluate((ph, val) => {
    const el = document.querySelector(`input[placeholder="${ph}"]`)
    el.value = val
    el.dispatchEvent(new Event('input', { bubbles: true }))
    el.dispatchEvent(new Event('change', { bubbles: true }))
  }, placeholder, value)
}

async function login (page, uname, pwd) {
  await page.goto(BASE, { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => { localStorage.clear(); location.hash = '#/user/login' })
  await page.reload({ waitUntil: 'networkidle2' })
  await page.waitForSelector('input[placeholder="请输入用户名"]', { timeout: 20000 })
  await fill(page, '请输入用户名', uname)
  await fill(page, '请输入密码', pwd)
  await page.evaluate(() => document.querySelector('.login-button').click())
  await page.waitForFunction('location.hash.indexOf("/home") >= 0', { timeout: 25000 })
  await sleep(1500)
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
  const consoleErrors = []
  page.on('console', m => { if (m.type() === 'error') consoleErrors.push(m.text()) })

  await login(page, 'admin_1', '123456')

  // 进入销售地点页
  await page.evaluate(() => { location.hash = '#/base/sale' })
  await sleep(2000)
  const listRows = await page.$$eval('.el-table__body tbody tr', trs => trs.length).catch(() => 0)
  console.log('列表模式行数:', listRows)
  await page.screenshot({ path: path.join(SHOT_DIR, '01-列表模式.png') })

  // 打开地图
  await page.evaluate(() => document.querySelector('.el-switch').click())
  await sleep(6000)

  const mapState = await page.evaluate(() => ({
    hasCanvas: !!document.querySelector('.amap-maps canvas'),
    markers: document.querySelectorAll('.amap-marker').length,
    tip: (document.querySelector('.map-tip') || {}).innerText || ''
  }))
  console.log('地图 canvas:', mapState.hasCanvas, '| 标记数:', mapState.markers)
  console.log('提示:', mapState.tip.replace(/\s+/g, ' ').trim())
  await page.screenshot({ path: path.join(SHOT_DIR, '02-地图模式.png') })

  // 选点新增：点「新增销售地点」→ 在地图上点一下 → 弹窗带经纬度
  await page.evaluate(() => {
    const btn = [...document.querySelectorAll('button')].find(b => b.innerText.replace(/\s/g, '').includes('新增销售地点'))
    if (btn) btn.click()
  })
  await sleep(1200)
  const placing = await page.evaluate(() => (document.querySelector('.map-tip') || {}).innerText || '')
  console.log('选点模式提示:', placing.replace(/\s+/g, ' ').trim())

  const box = await page.evaluate(() => {
    const el = document.querySelector('.map-box')
    const r = el.getBoundingClientRect()
    return { x: r.x + r.width / 2, y: r.y + r.height / 2 }
  })
  await page.mouse.click(box.x, box.y)
  await sleep(2500)

  const dialog = await page.evaluate(() => {
    const dlg = document.querySelector('.el-dialog__wrapper:not([style*="display: none"])')
    if (!dlg) return null
    return {
      title: (dlg.querySelector('.el-dialog__title') || {}).innerText || '',
      coord: (dlg.querySelector('.coord') || {}).innerText || '',
      address: (dlg.querySelector('input[maxlength="200"]') || {}).value || ''
    }
  })
  console.log('落点弹窗:', JSON.stringify(dialog))
  await page.screenshot({ path: path.join(SHOT_DIR, '03-地图选点弹窗.png') })

  // 关掉弹窗，别留下脏数据
  await page.evaluate(() => {
    const btns = [...document.querySelectorAll('.el-dialog__footer button')]
    const cancel = btns.find(b => b.innerText.indexOf('取消') >= 0)
    if (cancel) cancel.click()
  })
  await sleep(800)

  const ok = mapState.hasCanvas && mapState.markers > 0 && dialog && /^\d+\.\d+, \d+\.\d+$/.test(dialog.coord)
  console.log('\n页面错误:', errors.length ? errors.join('\n') : '无')
  console.log('控制台错误:', consoleErrors.length ? consoleErrors.slice(0, 5).join('\n') : '无')
  console.log('结论:', ok ? 'PASS（地图渲染 + 标记 + 选点回填经纬度）' : 'FAIL')

  await page.close()
  browser.disconnect()
  if (!ok) process.exit(1)
}

main().catch(e => { console.error('E2E 失败:', e.message); process.exit(1) })
