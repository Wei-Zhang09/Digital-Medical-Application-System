"""
生成答辩 PPT：docs/report/慧医数字医疗应用系统-答辩PPT.pptx

用法：python scripts/export_ppt.py
风格：16:9、青绿主色（与系统界面一致）、左侧文字右侧截图的左右分栏版式。
"""
import os

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.util import Emu, Inches, Pt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SHOT = os.path.join(ROOT, "docs", "report", "screenshots")
OUT = os.path.join(ROOT, "docs", "report", "慧医数字医疗应用系统-答辩PPT.pptx")

PRIMARY = RGBColor(0x13, 0xB7, 0xA5)
PRIMARY_DARK = RGBColor(0x0E, 0x8C, 0x7F)
DARK = RGBColor(0x1F, 0x2D, 0x3D)
GRAY = RGBColor(0x60, 0x62, 0x66)
LIGHT = RGBColor(0xF4, 0xF6, 0xF9)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
FONT = "微软雅黑"

W, H = Inches(13.333), Inches(7.5)


def new_deck():
    prs = Presentation()
    prs.slide_width = W
    prs.slide_height = H
    return prs


def blank(prs):
    return prs.slides.add_slide(prs.slide_layouts[6])


def textbox(slide, left, top, width, height, text, size=18, bold=False,
            color=DARK, align=PP_ALIGN.LEFT, spacing=1.2, anchor=MSO_ANCHOR.TOP):
    box = slide.shapes.add_textbox(left, top, width, height)
    tf = box.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = anchor
    lines = text.split("\n")
    for i, line in enumerate(lines):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.alignment = align
        p.line_spacing = spacing
        run = p.add_run()
        run.text = line
        run.font.name = FONT
        run.font.size = Pt(size)
        run.font.bold = bold
        run.font.color.rgb = color
    return box


def bullets(slide, left, top, width, height, items, size=16, color=GRAY, gap=10):
    box = slide.shapes.add_textbox(left, top, width, height)
    tf = box.text_frame
    tf.word_wrap = True
    for i, item in enumerate(items):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(gap)
        p.line_spacing = 1.35
        if isinstance(item, tuple):
            head, detail = item
            r1 = p.add_run()
            r1.text = "● " + head
            r1.font.name = FONT
            r1.font.size = Pt(size)
            r1.font.bold = True
            r1.font.color.rgb = DARK
            if detail:
                r2 = p.add_run()
                r2.text = "　" + detail
                r2.font.name = FONT
                r2.font.size = Pt(size - 2)
                r2.font.color.rgb = color
        else:
            r = p.add_run()
            r.text = "● " + item
            r.font.name = FONT
            r.font.size = Pt(size)
            r.font.color.rgb = color
    return box


def header(slide, title, subtitle=None):
    """统一页眉：左侧竖色条 + 标题 + 细横线"""
    bar = slide.shapes.add_shape(1, Inches(0.6), Inches(0.55), Inches(0.09), Inches(0.62))
    bar.fill.solid()
    bar.fill.fore_color.rgb = PRIMARY
    bar.line.fill.background()

    textbox(slide, Inches(0.85), Inches(0.5), Inches(10.5), Inches(0.7), title, size=26, bold=True)
    if subtitle:
        textbox(slide, Inches(0.87), Inches(1.08), Inches(11), Inches(0.4), subtitle, size=13, color=GRAY)

    line = slide.shapes.add_shape(1, Inches(0.6), Inches(1.5), Inches(12.1), Pt(1))
    line.fill.solid()
    line.fill.fore_color.rgb = RGBColor(0xE4, 0xE7, 0xED)
    line.line.fill.background()


def footer(slide, page):
    textbox(slide, Inches(11.6), Inches(6.95), Inches(1.2), Inches(0.35),
            str(page), size=11, color=RGBColor(0xC0, 0xC4, 0xCC), align=PP_ALIGN.RIGHT)
    textbox(slide, Inches(0.6), Inches(6.95), Inches(8), Inches(0.35),
            "慧医数字医疗应用系统 · 软件工程232 张伟", size=10, color=RGBColor(0xC0, 0xC4, 0xCC))


def add_table(slide, left, top, width, headers, rows, col_widths=None, size=12, row_h=0.42):
    n_rows, n_cols = len(rows) + 1, len(headers)
    shape = slide.shapes.add_table(n_rows, n_cols, left, top, width, Inches(row_h * n_rows))
    table = shape.table
    if col_widths:
        total = sum(col_widths)
        for i, cw in enumerate(col_widths):
            table.columns[i].width = Emu(int(width * cw / total))

    for j, h in enumerate(headers):
        cell = table.cell(0, j)
        cell.fill.solid()
        cell.fill.fore_color.rgb = PRIMARY
        cell.margin_left = Inches(0.08)
        p = cell.text_frame.paragraphs[0]
        p.alignment = PP_ALIGN.CENTER
        r = p.add_run()
        r.text = h
        r.font.name = FONT
        r.font.size = Pt(size)
        r.font.bold = True
        r.font.color.rgb = WHITE

    for i, row in enumerate(rows, start=1):
        for j, val in enumerate(row):
            cell = table.cell(i, j)
            cell.fill.solid()
            cell.fill.fore_color.rgb = WHITE if i % 2 else LIGHT
            cell.margin_left = Inches(0.08)
            p = cell.text_frame.paragraphs[0]
            p.alignment = PP_ALIGN.LEFT if j == 0 else PP_ALIGN.CENTER
            r = p.add_run()
            r.text = str(val)
            r.font.name = FONT
            r.font.size = Pt(size)
            r.font.color.rgb = DARK
    return table


def add_image(slide, path, left, top, width=None, height=None):
    full = os.path.join(SHOT, path)
    if not os.path.exists(full):
        textbox(slide, left, top, Inches(4), Inches(0.4), "[截图缺失] " + path, size=10, color=RGBColor(0xC0, 0, 0))
        return
    if width:
        slide.shapes.add_picture(full, left, top, width=width)
    elif height:
        slide.shapes.add_picture(full, left, top, height=height)
    else:
        slide.shapes.add_picture(full, left, top)


# ------------------------------------------------------------------ 幻灯片
def slide_cover(prs):
    s = blank(prs)
    bg = s.shapes.add_shape(1, 0, 0, W, H)
    bg.fill.solid()
    bg.fill.fore_color.rgb = DARK
    bg.line.fill.background()

    accent = s.shapes.add_shape(1, Inches(1.0), Inches(2.55), Inches(0.12), Inches(1.5))
    accent.fill.solid()
    accent.fill.fore_color.rgb = PRIMARY
    accent.line.fill.background()

    textbox(s, Inches(1.4), Inches(2.45), Inches(10.5), Inches(1.0),
            "慧医数字医疗应用系统", size=44, bold=True, color=WHITE)
    textbox(s, Inches(1.42), Inches(3.35), Inches(10.5), Inches(0.5),
            "Digital Medical Application System", size=16, color=RGBColor(0x8F, 0xA3, 0xB3))
    textbox(s, Inches(1.42), Inches(4.0), Inches(10.5), Inches(0.6),
            "答辩演示 · Spring Boot + Vue + MySQL 前后端分离", size=18, color=PRIMARY)

    textbox(s, Inches(1.42), Inches(5.4), Inches(10), Inches(0.9),
            "软件工程 232　|　2330142026　|　张伟", size=16, color=RGBColor(0xC0, 0xC9, 0xD4))
    return s


def slide_agenda(prs, page):
    s = blank(prs)
    header(s, "今天讲四件事", "不铺功能清单，只讲清一条链路、两处扩展、一次部署和一轮测试")
    items = [
        ("01", "系统能做什么", "9 个模块、两种角色、菜单与接口双重权限"),
        ("02", "核心链路怎么实现", "以药品为例：页面 → 接口 → 控制器 → 服务 → 映射 → 表"),
        ("03", "两处扩展", "首页数据面板（ECharts）、销售地点地图（高德 JS API）"),
        ("04", "怎么部署、怎么验证", "Nginx 反代 + 验收五下 + 35 条接口用例与 17 条单测"),
    ]
    top = Inches(1.95)
    for num, head, desc in items:
        textbox(s, Inches(0.85), top, Inches(1.0), Inches(0.6), num, size=30, bold=True, color=PRIMARY)
        textbox(s, Inches(1.95), top - Inches(0.05), Inches(4.2), Inches(0.5), head, size=20, bold=True)
        textbox(s, Inches(1.97), top + Inches(0.45), Inches(9.5), Inches(0.5), desc, size=13, color=GRAY)
        top += Inches(1.18)
    footer(s, page)
    return s


def slide_background(prs, page):
    s = blank(prs)
    header(s, "项目背景：三个具体问题", "医疗主数据分散在人事、药房、Excel 与文件柜里")
    bullets(s, Inches(0.85), Inches(1.9), Inches(6.4), Inches(4.4), [
        ("同一份数据多处维护", "药品在药房台账和采购单各存一份，名称与图片对不上"),
        ("权限没有边界", "台账放在共享盘，谁都能改，无法按角色限制"),
        ("看不见全局", "想知道有多少医师、药品、药店只能人工数"),
    ], size=16)

    textbox(s, Inches(7.5), Inches(1.9), Inches(5.2), Inches(0.4), "本项目要做的", size=15, bold=True, color=PRIMARY)
    textbox(s, Inches(7.5), Inches(2.35), Inches(5.2), Inches(1.2),
            "把主数据收进一个管理后台：\n一个数据库、一套接口、一个按角色区分的界面。", size=15, color=GRAY, spacing=1.4)
    textbox(s, Inches(7.5), Inches(3.7), Inches(5.2), Inches(0.4), "范围", size=15, bold=True, color=PRIMARY)
    textbox(s, Inches(7.5), Inches(4.15), Inches(5.2), Inches(1.6),
            "必做：8 个主数据模块 + 登录权限\n扩展：数据面板、销售地点地图\n不做：患者端、库存进销存、移动端",
            size=14, color=GRAY, spacing=1.4)
    footer(s, page)
    return s


def slide_modules(prs, page):
    s = blank(prs)
    header(s, "系统能做什么：9 个模块，两种角色", "菜单由后端按角色下发，不是前端写死")
    rows = [
        ["登录与权限", "JWT 登录、按角色下发菜单、接口级角色控制"],
        ["医生信息", "增删改查 + 同步建账号 + 重置口令"],
        ["医药公司", "增删改查（公司政策的挂靠对象）"],
        ["公司政策", "增删改查，必须挂已存在公司"],
        ["城市信息", "增、查、删（官方需求不含修改）"],
        ["医保政策", "增删改查，按城市生效"],
        ["销售地点", "增删改查 + 经纬度 + 地图展示"],
        ["药品信息", "增删改查 + 图片上传 + 关联药店"],
        ["必备材料", "增删改查（字段最少，适合当模板）"],
    ]
    add_table(s, Inches(0.85), Inches(1.85), Inches(7.4), ["模块", "能力"], rows,
              col_widths=[2, 5], size=12, row_h=0.44)

    textbox(s, Inches(8.7), Inches(1.85), Inches(4.0), Inches(0.4), "权限矩阵", size=15, bold=True, color=PRIMARY)
    add_table(s, Inches(8.7), Inches(2.3), Inches(3.9), ["操作", "管理员", "医师"],
              [["全部查询", "✓", "✓"],
               ["药品新增", "✓", "✓"],
               ["药品改删", "✓", "✗"],
               ["其余改删", "✓", "✗"]],
              col_widths=[2, 1, 1], size=12, row_h=0.42)
    textbox(s, Inches(8.7), Inches(4.6), Inches(3.9), Inches(1.4),
            "菜单：管理员 9 项 / 医师 5 项\n接口：@RolesAllowed 强制拦截\n前端隐藏按钮只是体验优化",
            size=12, color=GRAY, spacing=1.4)
    footer(s, page)
    return s


def slide_arch(prs, page):
    s = blank(prs)
    header(s, "技术栈与架构：前后端分离三层", "前端只管渲染，业务规则与权限都在后端")

    layers = [
        ("表现层", "Vue 2.6 + Element UI + Vue Router + Vuex", "页面渲染、表单校验、路由守卫、按角色渲染菜单"),
        ("接入层", "Nginx 1.20.1（生产）/ vue-cli dev server（开发）", "托管静态资源；反代 /api 与 /image"),
        ("应用层", "Spring Boot 2.5.3 + Spring Security + MyBatis", "认证鉴权、业务逻辑、参数校验、统一返回体"),
        ("数据层", "MySQL 8.0 + Druid 连接池", "17 张表：主数据 + 权限"),
    ]
    top = Inches(1.85)
    for name, stack, duty in layers:
        box = s.shapes.add_shape(1, Inches(0.85), top, Inches(11.6), Inches(1.05))
        box.fill.solid()
        box.fill.fore_color.rgb = LIGHT
        box.line.color.rgb = RGBColor(0xE4, 0xE7, 0xED)
        textbox(s, Inches(1.05), top + Inches(0.12), Inches(1.6), Inches(0.4), name, size=15, bold=True, color=PRIMARY)
        textbox(s, Inches(2.6), top + Inches(0.1), Inches(9.6), Inches(0.4), stack, size=13, bold=True)
        textbox(s, Inches(2.6), top + Inches(0.52), Inches(9.6), Inches(0.4), duty, size=12, color=GRAY)
        top += Inches(1.2)
    footer(s, page)
    return s


def slide_db(prs, page):
    s = blank(prs)
    header(s, "数据库：17 张表，三类关系", "权限靠 role_permission，药品与药店是多对多")
    add_table(s, Inches(0.85), Inches(1.85), Inches(6.6),
              ["关系", "类型", "说明"],
              [["account ↔ doctor", "1 : 1", "医师与账号，删医师连带删账号"],
               ["doctor → doctor_level", "N : 1", "职称（主任/普通/实习）"],
               ["doctor → treat_type", "N : 1", "科室（肩/膝/踝/腰）"],
               ["drug ↔ sale", "M : N", "靠 drug_sale 关联"],
               ["company_policy → drugcompany", "N : 1", "公司政策挂靠公司"],
               ["medical_policy → city", "N : 1", "医保政策按城市生效"],
               ["permission ↔ role", "M : N", "菜单通过 role_permission 授权"]],
              col_widths=[3, 1, 4], size=11.5, row_h=0.44)

    textbox(s, Inches(7.8), Inches(1.85), Inches(4.8), Inches(0.4), "关键设计", size=15, bold=True, color=PRIMARY)
    bullets(s, Inches(7.8), Inches(2.3), Inches(4.8), Inches(3.6), [
        ("口令只存 BCrypt 密文", "库里不出现明文"),
        ("SQL 全部 #{} 参数化", "不用 ${} 拼接"),
        ("多表写操作加事务", "新增医师、药品关联替换"),
        ("图片只存相对路径", "/image/xxx，换机器不裂图"),
    ], size=12.5, gap=8)
    footer(s, page)
    return s


def slide_auth(prs, page):
    s = blank(prs)
    header(s, "登录与权限：菜单是权限的可视化结果", "页面藏不住，真正拦截在后端")
    add_image(s, "01-登录页.png", Inches(7.3), Inches(1.75), width=Inches(5.4))

    bullets(s, Inches(0.85), Inches(1.9), Inches(6.1), Inches(4.2), [
        ("JWT 无状态认证", "登录签发 HS256 token，24 小时有效"),
        ("JwtFilter 先解析", "放在用户名密码过滤器之前，解析后写入安全上下文"),
        ("四类失败分开处理", "未登录 10006 / 认证失败 10000 / 无权限 10000 / 成功"),
        ("菜单由后端算", "按 role_permission 查、按 pid 递归组装树"),
        ("医师标题降级", "「药品信息管理」→「药品信息查询」"),
    ], size=13.5, gap=10)
    footer(s, page)
    return s


def slide_drug(prs, page):
    s = blank(prs)
    header(s, "核心链路：一次点击穿过这些层", "以药品为例，其余模块照着做")
    chain = "页面 DrugManage/index.vue  →  api/admin/drugInfoManage.js  →  DrugController  →  DrugService  →  DrugMapper  →  drug / drug_sale"
    box = s.shapes.add_shape(1, Inches(0.85), Inches(1.75), Inches(11.6), Inches(0.62))
    box.fill.solid()
    box.fill.fore_color.rgb = RGBColor(0xE8, 0xF8, 0xF5)
    box.line.color.rgb = PRIMARY
    textbox(s, Inches(1.0), Inches(1.87), Inches(11.3), Inches(0.4), chain, size=11.5, color=PRIMARY_DARK)

    add_image(s, "pages/05-药品信息管理.png", Inches(0.85), Inches(2.55), width=Inches(6.6))

    bullets(s, Inches(7.8), Inches(2.55), Inches(4.7), Inches(3.6), [
        ("关联药店整体替换", "先删 drug_sale 再插新的，不留脏关系"),
        ("列表一次带出药店名", "GROUP_CONCAT，避免前端二次请求"),
        ("图片落 uploads/", "UUID 命名，/image/** 映射"),
        ("改删仅管理员", "新增开放给角色 1、2"),
    ], size=12, gap=8)
    footer(s, page)
    return s


def slide_dashboard(prs, page):
    s = blank(prs)
    header(s, "扩展一：首页数据面板", "四个数字 + 两张图 + 最新资讯，不新增菜单")
    add_image(s, "dashboard/01-管理员数据面板.png", Inches(0.85), Inches(1.8), width=Inches(7.6))
    bullets(s, Inches(8.7), Inches(1.95), Inches(3.9), Inches(3.8), [
        ("四个计数", "医师 12 / 公司 4 / 药品 6 / 药店 18"),
        ("职称柱状图", "doctor 左连 doctor_level 分组"),
        ("科室环形饼图", "doctor 左连 treat_type 分组"),
        ("最新资讯", "三张内容表 UNION 后按时间取 5 条"),
    ], size=12, gap=8)
    footer(s, page)
    return s


def slide_map(prs, page):
    s = blank(prs)
    header(s, "扩展二：销售地点地图", "高德 JS API 2.0 + 安全密钥")
    add_image(s, "map/02-地图模式.png", Inches(0.85), Inches(1.8), width=Inches(7.8))
    bullets(s, Inches(8.9), Inches(1.95), Inches(3.7), Inches(3.8), [
        ("开关切换", "列表 / 地图，切走时销毁实例"),
        ("18 个标记", "数据来自全量 /api/sales/all"),
        ("点标记改药店", "直接回填编辑弹窗"),
        ("选点新增", "地图点击落点 + 逆地理编码出地址"),
    ], size=12, gap=8)
    footer(s, page)
    return s


def slide_deploy(prs, page):
    s = blank(prs)
    header(s, "部署：Nginx 只做两件事", "托管页面、转发接口和图片")
    add_table(s, Inches(0.85), Inches(1.85), Inches(6.4),
              ["#", "验收项", "结果"],
              [["1", "80 端口打开登录页，地址无 9092", "✓"],
               ["2", "admin_1 登录，菜单 9 项", "✓"],
               ["3", "药品列表有种子数据", "✓ 6 行"],
               ["4", "新上传的图能显示", "✓ naturalWidth>0"],
               ["5", "医师账号菜单变少", "✓ 5 项"]],
              col_widths=[0.6, 5, 1.6], size=12, row_h=0.46)

    textbox(s, Inches(7.6), Inches(1.85), Inches(4.9), Inches(0.4), "一个只有真机才能发现的坑", size=14, bold=True, color=PRIMARY)
    textbox(s, Inches(7.6), Inches(2.3), Inches(4.9), Inches(2.2),
            "location /image/ 被后面的静态资源正则\n~* \\.(png|jpg|…)$ 抢走，\n图片全部 404。\n\n前缀加 ^~ 后解决。",
            size=12.5, color=GRAY, spacing=1.35)
    textbox(s, Inches(7.6), Inches(4.5), Inches(4.9), Inches(1.2),
            "接口测试全绿也发现不了——\n页面能开、图全裂。", size=12.5, color=RGBColor(0xE6, 0xA2, 0x3C), spacing=1.35)
    footer(s, page)
    return s


def slide_test(prs, page):
    s = blank(prs)
    header(s, "测试：四层，全绿", "接口层保证数据正确，界面层保证人看到的是对的")
    add_table(s, Inches(0.85), Inches(1.85), Inches(7.2),
              ["层次", "方式", "数量", "结果"],
              [["单元测试", "JUnit 5 + Mockito", "17 条", "全通过"],
               ["接口测试", "Python 直连 HTTP", "35 条", "35/35"],
               ["界面测试", "puppeteer 真实浏览器", "5 组 32 断言", "全通过"],
               ["部署验收", "验收五下", "5 条", "全通过"]],
              col_widths=[1.6, 3.2, 1.6, 1.4], size=12.5, row_h=0.5)

    textbox(s, Inches(8.4), Inches(1.85), Inches(4.2), Inches(0.4), "7 个缺陷，4 个是「接口全绿、界面坏」", size=13, bold=True, color=PRIMARY)
    add_table(s, Inches(8.4), Inches(2.35), Inches(4.2),
              ["缺陷", "接口", "界面"],
              [["红屏遮罩", "20000", "无法操作"],
               ["药品图空", "url 正常", "占位图标"],
               ["时间折行", "合法 ISO", "列被撑开"],
               ["部署图 404", "/api 正常", "图全裂"]],
              col_widths=[2, 1.4, 1.6], size=11, row_h=0.42)
    footer(s, page)
    return s


def slide_engineering(prs, page):
    s = blank(prs)
    header(s, "工程实践：环境与工具链的坑", "这些不在任务书里，但真实开发都会遇到")
    add_table(s, Inches(0.85), Inches(1.85), Inches(11.6),
              ["问题", "现象", "处理"],
              [["Maven 不可用", "bash 包装报 classworlds 缺失", "改用 mvn.cmd（安装本身完好）"],
               ["MySQL 服务无权限", "系统服务由管理员安装，普通用户起不来", "项目内独立实例 3307"],
               ["Node 版本过高", "系统 Node 26，vue-cli 4 的 webpack 4 跑不动", "MSI 解到项目内，用 22.13"],
               ["baseURL 写死", "部署后前端去访问访问者自己的 8080", ".env.development / .env.production 分离"],
               ["时间格式带 T", "2021-11-18T17:34:02 折行", "Jackson2ObjectMapperBuilderCustomizer"],
               ["大文件推送", "docx/PDF 走代理推 GitHub 408", "生成物不进版本库，脚本随时重建"]],
              col_widths=[2.2, 4.6, 5.0], size=11.5, row_h=0.48)
    footer(s, page)
    return s


def slide_next(prs, page):
    s = blank(prs)
    header(s, "不足与改进方向", "说清楚没做什么，比硬吹做完了更可信")
    bullets(s, Inches(0.85), Inches(1.95), Inches(5.6), Inches(4.2), [
        ("未做并发压测", "单机演示场景，Druid 上限 20"),
        ("只测 Chromium 内核", "未验证 Firefox / Safari"),
        ("未做移动端适配", "后台管理定位，最小宽度 1366"),
        ("界面测试未接 CI", "依赖本地 Chrome 调试端口"),
    ], size=13.5, gap=12)

    textbox(s, Inches(6.9), Inches(1.95), Inches(5.7), Inches(0.4), "上生产前必须补的", size=14, bold=True, color=PRIMARY)
    bullets(s, Inches(6.9), Inches(2.4), Inches(5.7), Inches(3.8), [
        ("换掉 root 数据库账号", "只授予业务库 CRUD 权限"),
        ("高德 Key 限制 referer", "避免被他人盗用配额"),
        ("登录失败次数限制", "配合操作日志与审计"),
        ("Playwright + Actions", "把界面回归固化成流水线"),
    ], size=13.5, gap=12)
    footer(s, page)
    return s


def slide_demo(prs, page):
    s = blank(prs)
    header(s, "现场演示：按这条线走 8 分钟", "每一步只做一次成功操作")
    steps = [
        ("1  登录", "admin_1 / 123456，看完整菜单 9 项"),
        ("2  药品", "查询 → 新增（选药店 + 传图）→ 修改 → 删除"),
        ("3  扩展", "首页数据面板；销售地点切地图、点标记"),
        ("4  换人", "退出 → 王小明4125 登录，菜单只剩 5 项"),
        ("5  部署", "http://127.0.0.1/ 打开，地址里没有 9092"),
    ]
    top = Inches(1.9)
    for head, desc in steps:
        box = s.shapes.add_shape(1, Inches(0.85), top, Inches(11.6), Inches(0.78))
        box.fill.solid()
        box.fill.fore_color.rgb = LIGHT
        box.line.color.rgb = RGBColor(0xE4, 0xE7, 0xED)
        textbox(s, Inches(1.05), top + Inches(0.18), Inches(1.8), Inches(0.4), head, size=14, bold=True, color=PRIMARY)
        textbox(s, Inches(3.0), top + Inches(0.18), Inches(9.2), Inches(0.4), desc, size=13, color=GRAY)
        top += Inches(0.92)

    textbox(s, Inches(0.85), Inches(6.35), Inches(11.6), Inches(0.4),
            "演示账号：管理员 admin_1 / 123456　医师 王小明4125 / 123456", size=12, color=GRAY)
    footer(s, page)
    return s


def slide_end(prs):
    s = blank(prs)
    bg = s.shapes.add_shape(1, 0, 0, W, H)
    bg.fill.solid()
    bg.fill.fore_color.rgb = DARK
    bg.line.fill.background()

    textbox(s, Inches(1.2), Inches(3.0), Inches(11), Inches(0.9), "谢谢，请提问", size=40, bold=True, color=WHITE, align=PP_ALIGN.CENTER)
    textbox(s, Inches(1.2), Inches(4.0), Inches(11), Inches(0.5),
            "慧医数字医疗应用系统　|　软件工程 232　2330142026　张伟", size=14,
            color=RGBColor(0x8F, 0xA3, 0xB3), align=PP_ALIGN.CENTER)
    return s


def main():
    prs = new_deck()
    slide_cover(prs)
    slide_agenda(prs, 2)
    slide_background(prs, 3)
    slide_modules(prs, 4)
    slide_arch(prs, 5)
    slide_db(prs, 6)
    slide_auth(prs, 7)
    slide_drug(prs, 8)
    slide_dashboard(prs, 9)
    slide_map(prs, 10)
    slide_deploy(prs, 11)
    slide_test(prs, 12)
    slide_engineering(prs, 13)
    slide_next(prs, 14)
    slide_demo(prs, 15)
    slide_end(prs)

    prs.save(OUT)
    print("已生成：%s（%d 页）" % (OUT, len(prs.slides.__iter__.__self__._sldIdLst)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
