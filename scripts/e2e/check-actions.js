/**
 * 全量检查 8 个页面的「操作」列是否完整显示（fixed="right" 修复后的回归）
 * 用法：NODE_PATH=... node scripts/e2e/check-actions.js
 * 产物：docs/report/screenshots/fix/*.png
 */
const puppeteer = require('puppeteer-core')
const fs = require('fs')
const path = require('path')

const BASE = 'http://localhost:9092'
const SHOT = path.resolve(__dirname, '../../docs/report/screenshots/fix')
const sleep = ms => new Promise(r => setTimeout(r, ms))

const PAGES = [
  ['/base/company', '医药公司管理'],
  ['/base/sale', '销售地点管理'],
  ['/base/city', '城市信息管理'],
  ['/manage/drug', '药品信息管理'],
  ['/manage/medical/policy', '医保政策管理'],
  ['/manage/company/policy', '医药公司政策管理'],
  ['/manage/doctor', '医生信息管理'],
  ['/manage/material', '必备材料管理']
]

async function fill (page, ph, val) {
  await page.evaluate((p, v) => {
    const el = document.querySelector(`input[placeholder="${p}"]`)
    el.value = v
    el.dispatchEvent(new Event('input', { bubbles: true }))
    el.dispatchEvent(new Event('change', { bubbles: true }))
  }, ph, val)
}

;(async () => {
  fs.mkdirSync(SHOT, { recursive: true })
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

  let allOk = true
  for (const [route, name] of PAGES) {
    await page.evaluate(r => { location.hash = '#' + r }, route)
    await sleep(1700)

    const m = await page.evaluate(() => {
      const card = document.querySelector('.el-card__body').getBoundingClientRect()
      const fixed = document.querySelector('.el-table__fixed-right .el-table__body tbody tr:first-child')
      if (!fixed) return { ok: false, reason: '没有固定列' }
      const wrap = document.querySelector('.el-table__body-wrapper')
      const btns = [...fixed.querySelectorAll('button')]
      const last = btns[btns.length - 1]
      const r = last.getBoundingClientRect()
      return {
        text: last.innerText.replace(/\s/g, ''),
        right: Math.round(r.right),
        cardRight: Math.round(card.right),
        inside: r.right <= card.right - 4,
        btnCount: btns.length,
        overflowX: wrap.scrollWidth - wrap.clientWidth
      }
    })

    // 注：Element 内部会留一个 40px 的隐藏 gutter，overflowX 恒为 40，与视觉无关，不作为判定条件
    const ok = m.inside && m.text === '删除'
    if (!ok) allOk = false
    console.log(`${ok ? 'PASS' : 'FAIL'}  ${name}  末按钮=${m.text}  右边界=${m.right}/${m.cardRight}  按钮数=${m.btnCount}  横向溢出=${m.overflowX}px`)

    const file = path.join(SHOT, `${name}.png`)
    await page.screenshot({ path: file, clip: { x: 940, y: 140, width: 500, height: 620 } })
  }

  console.log('\n结论:', allOk ? 'PASS（8 个页面操作列均完整显示）' : 'FAIL')
  await page.close()
  browser.disconnect()
  if (!allOk) process.exit(1)
})().catch(e => { console.error('失败:', e.message); process.exit(1) })
