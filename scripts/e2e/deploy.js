/**
 * T11 部署验收：按官方「验收五下」逐条验证 Nginx（80 端口）部署结果
 *
 *   1. 用服务器 IP/本机 80 打开登录页，地址里没有 9092
 *   2. admin_1 能登录，菜单完整（9 项）
 *   3. 药品列表能查出种子数据
 *   4. 新上传一张图能显示（走界面真实上传）
 *   5. 医师账号菜单变少（5 项）
 *
 * 运行：NODE_PATH=".../.dev/tools/node_modules" node scripts/e2e/deploy.js
 * 产物：docs/report/screenshots/deploy/*.png
 */
const puppeteer = require('puppeteer-core')
const fs = require('fs')
const path = require('path')
const zlib = require('zlib')
const struct = require('node:buffer')

const SHOT_DIR = path.resolve(__dirname, '../../docs/report/screenshots/deploy')
const BASE = 'http://127.0.0.1'
const sleep = ms => new Promise(r => setTimeout(r, ms))

/** 现场生成一张 2x2 的 PNG，用于真实上传测试 */
function makePng (file) {
  function chunk (type, data) {
    const len = Buffer.alloc(4)
    len.writeUInt32BE(data.length)
    const body = Buffer.concat([Buffer.from(type), data])
    const crc = Buffer.alloc(4)
    crc.writeUInt32BE(crc32(body) >>> 0)
    return Buffer.concat([len, body, crc])
  }
  function crc32 (buf) {
    let c, crc = 0xffffffff
    for (let n = 0; n < buf.length; n++) {
      c = (crc ^ buf[n]) & 0xff
      for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1
      crc = (crc >>> 8) ^ c
    }
    return crc ^ 0xffffffff
  }
  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(2, 0)
  ihdr.writeUInt32BE(2, 4)
  ihdr[8] = 8
  ihdr[9] = 2
  const raw = Buffer.from([0, 19, 183, 165, 74, 125, 255, 0, 19, 183, 165, 0, 0, 0])
  const png = Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw)),
    chunk('IEND', Buffer.alloc(0))
  ])
  fs.writeFileSync(file, png)
  return file
}

async function fill (page, selector, value) {
  await page.evaluate((sel, val) => {
    const el = document.querySelector(sel)
    if (!el) throw new Error('找不到元素 ' + sel)
    el.value = val
    el.dispatchEvent(new Event('input', { bubbles: true }))
    el.dispatchEvent(new Event('change', { bubbles: true }))
  }, selector, value)
}

async function login (page, uname, pwd) {
  await page.goto(BASE + '/#/user/login', { waitUntil: 'networkidle2', timeout: 60000 })
  await page.waitForSelector('input[placeholder="请输入用户名"]', { timeout: 20000 })
  await sleep(500)
  await fill(page, 'input[placeholder="请输入用户名"]', uname)
  await fill(page, 'input[placeholder="请输入密码"]', pwd)
  await page.evaluate(() => document.querySelector('.login-button').click())
  await page.waitForFunction('location.hash.indexOf("/home") >= 0', { timeout: 25000 })
  await sleep(1500)
}

async function main () {
  fs.mkdirSync(SHOT_DIR, { recursive: true })
  const pngPath = makePng(path.join(SHOT_DIR, 'upload-test.png'))

  const browser = await puppeteer.connect({
    browserURL: 'http://127.0.0.1:9222',
    defaultViewport: { width: 1440, height: 900 }
  })
  const page = await browser.newPage()
  const errors = []
  page.on('pageerror', e => errors.push(e.message))
  const results = []
  const check = (name, ok, extra) => {
    results.push({ name, ok, extra })
    console.log(`${ok ? 'PASS' : 'FAIL'}  ${name}${extra ? '  ' + extra : ''}`)
  }

  // 1. 80 端口登录页
  await page.goto(BASE, { waitUntil: 'domcontentloaded' })
  await page.evaluate(() => localStorage.clear())
  await page.reload({ waitUntil: 'networkidle2' })
  await sleep(1200)
  const loc = await page.evaluate(() => ({ href: location.href, port: location.port, hash: location.hash }))
  check('① 登录页来自 80 端口、地址无 9092', loc.port === '' || loc.port === '80', loc.href)
  await page.screenshot({ path: path.join(SHOT_DIR, '01-80端口登录页.png') })

  // 2. 管理员登录 + 菜单
  await login(page, 'admin_1', '123456')
  const adminMenus = await page.$$eval('.layout-menu .el-menu-item span', els => els.map(e => e.innerText.trim()))
  check('② admin_1 登录成功、菜单 9 项', adminMenus.length === 9, adminMenus.join('/'))
  await page.screenshot({ path: path.join(SHOT_DIR, '02-管理员首页.png') })

  // 3. 药品列表种子数据
  await page.evaluate(() => { location.hash = '#/manage/drug' })
  await sleep(2000)
  const rows = await page.$$eval('.el-table__body tbody tr', trs => trs.length).catch(() => 0)
  check('③ 药品列表有种子数据', rows > 0, rows + ' 行')
  await page.screenshot({ path: path.join(SHOT_DIR, '03-药品列表.png') })

  // 4. 真实上传一张图 → 保存 → 列表里能显示
  await page.evaluate(() => {
    const btn = [...document.querySelectorAll('button')].find(b => b.innerText.replace(/\s/g, '').includes('新增药品'))
    if (btn) btn.click()
  })
  await sleep(1200)
  await fill(page, '.el-dialog__wrapper:not([style*="display: none"]) input[maxlength="100"]', '部署验收-测试药品')
  await page.evaluate(() => {
    const dlg = document.querySelector('.el-dialog__wrapper:not([style*="display: none"])')
    const tas = dlg.querySelectorAll('textarea')
    tas[0].value = '成分：验收测试'
    tas[0].dispatchEvent(new Event('input', { bubbles: true }))
    tas[1].value = '功能：验收测试'
    tas[1].dispatchEvent(new Event('input', { bubbles: true }))
  })
  const fileInput = await page.$('.el-dialog__wrapper:not([style*="display: none"]) input[type=file]')
  await fileInput.uploadFile(pngPath)
  await sleep(3000)
  const uploaded = await page.evaluate(() => {
    const img = document.querySelector('.el-dialog__wrapper:not([style*="display: none"]) .upload-preview')
    return img ? img.getAttribute('src') : ''
  })
  check('④a 图片上传接口返回并可预览', !!uploaded && uploaded.indexOf('/image/') >= 0, uploaded)

  await page.evaluate(() => {
    const dlg = document.querySelector('.el-dialog__wrapper:not([style*="display: none"])')
    const btn = [...dlg.querySelectorAll('button')].find(b => b.innerText.replace(/\s/g, '').includes('保存'))
    if (btn) btn.click()
  })
  await sleep(2500)

  const imgOk = await page.evaluate(() => {
    const tr = [...document.querySelectorAll('.el-table__body tbody tr')]
      .find(t => t.innerText.indexOf('部署验收-测试药品') >= 0)
    if (!tr) return { found: false }
    const img = tr.querySelector('.el-image img')
    return { found: true, src: img ? img.getAttribute('src') : '', loaded: img ? img.naturalWidth > 0 : false }
  })
  check('④b 新上传的图在列表里能显示', imgOk.found && imgOk.loaded, JSON.stringify(imgOk))
  await page.screenshot({ path: path.join(SHOT_DIR, '04-新上传图片显示.png') })

  // 清理：删掉测试药品
  await page.evaluate(() => {
    const tr = [...document.querySelectorAll('.el-table__body tbody tr')]
      .find(t => t.innerText.indexOf('部署验收-测试药品') >= 0)
    if (!tr) return
    const btn = [...tr.querySelectorAll('button')].find(b => b.innerText.indexOf('删除') >= 0)
    if (btn) btn.click()
  })
  await sleep(800)
  await page.evaluate(() => {
    const ok = [...document.querySelectorAll('.el-message-box__btns button')].find(b => b.innerText.indexOf('确定') >= 0)
    if (ok) ok.click()
  })
  await sleep(2000)

  // 5. 医师账号菜单变少
  await page.evaluate(() => localStorage.clear())
  await page.reload({ waitUntil: 'networkidle2' })
  await login(page, '王小明4125', '123456')
  const doctorMenus = await page.$$eval('.layout-menu .el-menu-item span', els => els.map(e => e.innerText.trim()))
  check('⑤ 医师账号菜单变少（5 项）', doctorMenus.length === 5, doctorMenus.join('/'))
  await page.screenshot({ path: path.join(SHOT_DIR, '05-医师首页.png') })

  console.log('\n页面错误:', errors.length ? errors.join('\n') : '无')
  const allOk = results.every(r => r.ok) && errors.length === 0
  console.log('结论:', allOk ? 'PASS（验收五下全过）' : 'FAIL')

  await page.close()
  browser.disconnect()
  if (!allOk) process.exit(1)
}

main().catch(e => { console.error('E2E 失败:', e.message); process.exit(1) })
