/**
 * 诊断表格右侧被裁切的问题：量一下表格、容器、最后一列的实际尺寸
 * 用法：NODE_PATH=... node scripts/e2e/diag-table.js
 */
const puppeteer = require('puppeteer-core')

const BASE = 'http://localhost:9092'
const sleep = ms => new Promise(r => setTimeout(r, ms))

async function fill (page, ph, val) {
  await page.evaluate((p, v) => {
    const el = document.querySelector(`input[placeholder="${p}"]`)
    el.value = v
    el.dispatchEvent(new Event('input', { bubbles: true }))
    el.dispatchEvent(new Event('change', { bubbles: true }))
  }, ph, val)
}

;(async () => {
  const browser = await puppeteer.connect({ browserURL: 'http://127.0.0.1:9222', defaultViewport: { width: 1440, height: 900 } })
  const page = await browser.newPage()
  await page.goto(BASE, { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => { localStorage.clear(); location.hash = '#/user/login' })
  await page.reload({ waitUntil: 'networkidle2' })
  await page.waitForSelector('input[placeholder="请输入用户名"]', { timeout: 20000 })
  await fill(page, '请输入用户名', 'admin_1')
  await fill(page, '请输入密码', '123456')
  await page.evaluate(() => document.querySelector('.login-button').click())
  await page.waitForFunction('location.hash.indexOf("/home") >= 0', { timeout: 25000 })
  await sleep(1200)

  for (const [route, name] of [['/base/company', '医药公司'], ['/base/sale', '销售地点'], ['/base/city', '城市']]) {
    await page.evaluate(r => { location.hash = '#' + r }, route)
    await sleep(1800)
    const m = await page.evaluate(() => {
      const card = document.querySelector('.el-card__body')
      const table = document.querySelector('.el-table')
      const wrapper = document.querySelector('.el-table__body-wrapper')
      const lastCell = document.querySelector('.el-table__body tbody tr:last-child td:last-child')
      // fixed="right" 后，固定列渲染在 .el-table__fixed-right 里，要从那里取按钮
      const fixedBody = document.querySelector('.el-table__fixed-right .el-table__body tbody tr:first-child')
      const lastBtn = fixedBody
        ? [...fixedBody.querySelectorAll('button')].pop()
        : (lastCell ? lastCell.querySelector('button:last-of-type') : null)
      const r = el => el ? el.getBoundingClientRect() : null
      return {
        cardRight: r(card) && Math.round(r(card).right),
        tableWidth: table && Math.round(table.getBoundingClientRect().width),
        wrapperClient: wrapper && wrapper.clientWidth,
        wrapperScroll: wrapper && wrapper.scrollWidth,
        lastCellRight: lastCell && Math.round(lastCell.getBoundingClientRect().right),
        lastBtnRight: lastBtn && Math.round(lastBtn.getBoundingClientRect().right),
        lastBtnText: lastBtn && lastBtn.innerText.trim(),
        docScroll: document.documentElement.scrollWidth,
        winInner: window.innerWidth
      }
    })
    console.log(name, JSON.stringify(m))
    await page.screenshot({ path: `D:/HermesWorkSpace/Digital Medical Application System/.dev/table-${route.split('/').pop()}.png`, clip: { x: 1000, y: 150, width: 440, height: 500 } })
  }

  await page.close()
  browser.disconnect()
})().catch(e => { console.error('失败:', e.message); process.exit(1) })
