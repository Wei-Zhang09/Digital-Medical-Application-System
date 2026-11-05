/**
 * T9 数据面板验证：登录后首页四个数字、两张图、资讯列表都渲染出来
 *
 * 运行：NODE_PATH=".../.dev/tools/node_modules" node scripts/e2e/dashboard.js
 * 产物：docs/report/screenshots/dashboard/*.png
 */
const puppeteer = require('puppeteer-core')
const fs = require('fs')
const path = require('path')

const SHOT_DIR = path.resolve(__dirname, '../../docs/report/screenshots/dashboard')
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
  await sleep(2500)
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

  // 四个数字
  const stats = await page.$$eval('.stat-value', els => els.map(e => e.innerText.trim()))
  const labels = await page.$$eval('.stat-label', els => els.map(e => e.innerText.trim()))
  console.log('统计卡片:', labels.map((l, i) => `${l}=${stats[i]}`).join('  '))

  // 两张图有没有真画出来（canvas 尺寸 & 非空）
  const charts = await page.evaluate(() => {
    const list = [...document.querySelectorAll('.chart canvas')]
    return list.map(c => ({ w: c.width, h: c.height }))
  })
  console.log('图表 canvas:', JSON.stringify(charts))

  // 资讯条数
  const news = await page.$$eval('.news-card .el-table__body tbody tr', trs => trs.map(t => t.innerText.replace(/\s+/g, ' ').trim()))
  console.log('最新资讯(' + news.length + '):')
  news.forEach(n => console.log('   -', n))

  await page.screenshot({ path: path.join(SHOT_DIR, '01-管理员数据面板.png') })
  await page.screenshot({ path: path.join(SHOT_DIR, '02-数据面板整页.png'), fullPage: true })

  // 医师也能看
  await login(page, '王小明4125', '123456')
  const stats2 = await page.$$eval('.stat-value', els => els.map(e => e.innerText.trim()))
  console.log('医师视角统计:', stats2.join(' / '))
  await page.screenshot({ path: path.join(SHOT_DIR, '03-医师数据面板.png') })

  const ok = stats.length === 4 && charts.length === 2 && charts.every(c => c.w > 100 && c.h > 100) &&
    news.length > 0 && stats2.length === 4 && errors.length === 0
  console.log('\n页面错误:', errors.length ? errors.join('\n') : '无')
  console.log('结论:', ok ? 'PASS' : 'FAIL')

  await page.close()
  browser.disconnect()
  if (!ok) process.exit(1)
}

main().catch(e => { console.error('E2E 失败:', e.message); process.exit(1) })
