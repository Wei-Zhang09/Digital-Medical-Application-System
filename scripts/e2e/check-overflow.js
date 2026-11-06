/**
 * 检查表格内容是否被裁切：逐单元格比较 scrollWidth 与 clientWidth，
 * 并检查表格是否横向溢出（溢出时 fixed="right" 的列会盖住右侧内容）。
 *
 * 用法：NODE_PATH=... node scripts/e2e/check-overflow.js [宽度1 宽度2 ...]
 * 默认在 1440 与 1909 两个宽度下各跑一遍。
 */
const puppeteer = require('puppeteer-core')

const BASE = 'http://localhost:9092'
const sleep = ms => new Promise(r => setTimeout(r, ms))

const PAGES = [
  ['/home', '首页数据面板'],
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

async function login (page) {
  await page.goto(BASE, { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => { localStorage.clear(); location.hash = '#/user/login' })
  await page.reload({ waitUntil: 'networkidle2' })
  await page.waitForSelector('input[placeholder="请输入用户名"]', { timeout: 20000 })
  await fill(page, '请输入用户名', 'admin_1')
  await fill(page, '请输入密码', '123456')
  await page.evaluate(() => document.querySelector('.login-button').click())
  await page.waitForFunction('location.hash.indexOf("/home") >= 0', { timeout: 25000 })
  await sleep(1500)
}

/** 统计主表格里被裁切的单元格（文本超出单元格且没有换行） */
function auditInPage () {
  const clipped = []
  const wrap = document.querySelector('.el-table__body-wrapper')
  const overflowX = wrap ? wrap.scrollWidth - wrap.clientWidth : 0
  document.querySelectorAll('.el-table__body-wrapper .el-table__body td .cell').forEach(td => {
    const el = td
    const style = getComputedStyle(el)
    const singleLine = style.whiteSpace === 'nowrap' || style.textOverflow === 'ellipsis'
    if (singleLine && el.scrollWidth > el.clientWidth + 1) {
      clipped.push({ text: el.innerText.trim().slice(0, 24), need: el.scrollWidth, have: el.clientWidth })
    }
  })
  return { overflowX, clipped }
}

;(async () => {
  const widths = process.argv.slice(2).map(Number).filter(Boolean)
  const sizes = widths.length ? widths : [1440, 1909]
  const browser = await puppeteer.connect({ browserURL: 'http://127.0.0.1:9222', defaultViewport: { width: 1440, height: 900 } })
  const page = await browser.newPage()
  let bad = 0

  for (const w of sizes) {
    await page.setViewport({ width: w, height: 900 })
    await login(page)
    console.log(`\n=== 视口宽度 ${w}px ===`)
    for (const [route, name] of PAGES) {
      await page.evaluate(r => { location.hash = '#' + r }, route)
      await sleep(1600)
      const r = await page.evaluate(auditInPage)
      const ok = r.clipped.length === 0 && r.overflowX <= 0
      if (!ok) bad++
      console.log(`${ok ? 'PASS' : 'FAIL'}  ${name.padEnd(10)} 横向溢出=${r.overflowX}px  被裁单元格=${r.clipped.length}`)
      if (r.clipped.length) r.clipped.slice(0, 3).forEach(c => console.log(`        ↳ "${c.text}" 需要${c.need}px 实际${c.have}px`))
    }
  }

  console.log('\n结论:', bad === 0 ? 'PASS（两种宽度下都没有裁切）' : `FAIL（${bad} 处）`)
  await page.close()
  browser.disconnect()
  if (bad) process.exit(1)
})().catch(e => { console.error('失败:', e.message); process.exit(1) })
