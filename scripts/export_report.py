"""
把 docs/report/chapters/*.md 导出成 Word 报告。

用法：python scripts/export_report.py
产物：docs/report/慧医数字医疗应用系统-项目分析与设计报告.docx

支持的 Markdown 子集（够写技术报告）：
  # / ## / ### / #### 标题
  | a | b |            表格（第二行是 |---| 分隔行，会被跳过）
  - 项目                无序列表
  1. 项目               有序列表
  ![说明](图片路径)      图片，按 md 文件位置解析相对路径
  ```代码块```           等宽字体段落
  > 引用                 灰色斜体
  正文里 **粗体**、`行内代码`
"""
import os
import re
import sys

from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CH_DIR = os.path.join(ROOT, "docs", "report", "chapters")
OUT = os.path.join(ROOT, "docs", "report", "慧医数字医疗应用系统-项目分析与设计报告.docx")

CHAPTERS = [
    "01-项目立项.md",
    "02-项目启动.md",
    "03-需求分析.md",
    "04-概要设计.md",
    "05-详细设计.md",
    "06-界面设计.md",
    "07-数据库设计.md",
    "08-编码与单元测试.md",
    "09-软件测试.md",
]

BODY_FONT = "宋体"
HEAD_FONT = "黑体"
CODE_FONT = "Consolas"


# ---------------------------------------------------------------- 基础样式
def set_run_font(run, font=BODY_FONT, size=12, bold=False, color=None):
    run.font.name = font
    run.font.size = Pt(size)
    run.font.bold = bold
    run._element.rPr.rFonts.set(qn("w:eastAsia"), font)
    if color:
        run.font.color.rgb = color


def init_styles(doc):
    normal = doc.styles["Normal"]
    normal.font.name = BODY_FONT
    normal.font.size = Pt(12)
    normal._element.rPr.rFonts.set(qn("w:eastAsia"), BODY_FONT)

    for level, size in ((1, 18), (2, 15), (3, 13), (4, 12)):
        style = doc.styles["Heading %d" % level]
        style.font.name = HEAD_FONT
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = RGBColor(0x1F, 0x2D, 0x3D)
        style._element.rPr.rFonts.set(qn("w:eastAsia"), HEAD_FONT)

    for section in doc.sections:
        section.top_margin = Cm(2.5)
        section.bottom_margin = Cm(2.5)
        section.left_margin = Cm(3.0)
        section.right_margin = Cm(2.5)


def add_page_number(doc):
    """页脚居中页码。

    用 w:fldSimple 并给它一个缓存结果 run（<w:r><w:t>1</w:t></w:r>）。
    没有缓存结果的空 fldSimple 会让 Word 打开后反复重排、导出 PDF 卡死；
    三个 fldChar 塞在同一个 run 里，Word 直接判定文档损坏。
    """
    for section in doc.sections:
        p = section.footer.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER

        fld = OxmlElement("w:fldSimple")
        fld.set(qn("w:instr"), " PAGE ")
        run = OxmlElement("w:r")
        rpr = OxmlElement("w:rPr")
        fonts = OxmlElement("w:rFonts")
        fonts.set(qn("w:ascii"), BODY_FONT)
        fonts.set(qn("w:hAnsi"), BODY_FONT)
        fonts.set(qn("w:eastAsia"), BODY_FONT)
        size = OxmlElement("w:sz")
        size.set(qn("w:val"), "18")
        rpr.append(fonts)
        rpr.append(size)
        t = OxmlElement("w:t")
        t.text = "1"
        run.append(rpr)
        run.append(t)
        fld.append(run)
        p._p.append(fld)


# ---------------------------------------------------------------- 行内格式
INLINE = re.compile(r"(\*\*.+?\*\*|`.+?`)")


def add_inline(paragraph, text, size=12, bold=False, color=None, italic=False):
    for part in INLINE.split(text):
        if not part:
            continue
        if part.startswith("**") and part.endswith("**") and len(part) > 4:
            run = paragraph.add_run(part[2:-2])
            set_run_font(run, size=size, bold=True, color=color)
            run.italic = italic
        elif part.startswith("`") and part.endswith("`") and len(part) > 2:
            run = paragraph.add_run(part[1:-1])
            set_run_font(run, font=CODE_FONT, size=size - 1, bold=bold, color=color)
        else:
            run = paragraph.add_run(part)
            set_run_font(run, size=size, bold=bold, color=color)
            run.italic = italic


# ---------------------------------------------------------------- 表格
def shade_cell(cell, hex_color="E8F4F2"):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), hex_color)
    tc_pr.append(shd)


def add_table(doc, rows):
    header, body = rows[0], rows[1:]
    table = doc.add_table(rows=1, cols=len(header))
    table.style = "Table Grid"
    table.alignment = WD_TABLE_ALIGNMENT.CENTER

    for i, text in enumerate(header):
        cell = table.rows[0].cells[i]
        cell.text = ""
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        add_inline(p, text, size=10.5, bold=True)
        shade_cell(cell)

    for row in body:
        cells = table.add_row().cells
        for i in range(len(header)):
            text = row[i] if i < len(row) else ""
            p = cells[i].paragraphs[0]
            add_inline(p, text, size=10.5)
    return table


# ---------------------------------------------------------------- 代码块
def add_code_block(doc, lines):
    for line in lines:
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = 1.0
        p.paragraph_format.left_indent = Cm(0.5)
        run = p.add_run(line if line else " ")
        set_run_font(run, font=CODE_FONT, size=9, color=RGBColor(0x33, 0x33, 0x33))


# ---------------------------------------------------------------- 图片
def add_image(doc, md_path, alt, base_dir):
    path = os.path.normpath(os.path.join(base_dir, md_path))
    if not os.path.exists(path):
        p = doc.add_paragraph()
        add_inline(p, "[图片缺失：%s]" % md_path, size=10, color=RGBColor(0xC0, 0x00, 0x00))
        return
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run().add_picture(path, width=Cm(15.5))
    if alt:
        cap = doc.add_paragraph()
        cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        add_inline(cap, alt, size=10, color=RGBColor(0x60, 0x60, 0x60))


# ---------------------------------------------------------------- 解析 md
def parse_table_row(line):
    line = line.strip()
    if line.startswith("|"):
        line = line[1:]
    if line.endswith("|"):
        line = line[:-1]
    return [c.strip() for c in line.split("|")]


def is_separator_row(cells):
    return all(re.fullmatch(r":?-{2,}:?", c.replace(" ", "")) for c in cells if c)


def render_markdown(doc, md_file):
    base_dir = os.path.dirname(md_file)
    with open(md_file, encoding="utf-8") as f:
        lines = f.read().split("\n")

    i = 0
    in_code = False
    code_buf = []
    table_buf = []

    def flush_table():
        nonlocal table_buf
        if len(table_buf) >= 2:
            rows = [parse_table_row(r) for r in table_buf]
            rows = [r for r in rows if not is_separator_row(r)]
            if rows:
                add_table(doc, rows)
                doc.add_paragraph()
        table_buf = []

    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # 代码块
        if stripped.startswith("```"):
            if in_code:
                add_code_block(doc, code_buf)
                doc.add_paragraph()
                code_buf = []
                in_code = False
            else:
                flush_table()
                in_code = True
            i += 1
            continue
        if in_code:
            code_buf.append(line)
            i += 1
            continue

        # 表格
        if stripped.startswith("|"):
            table_buf.append(stripped)
            i += 1
            continue
        flush_table()

        if not stripped:
            i += 1
            continue

        # 标题
        m = re.match(r"^(#{1,4})\s+(.*)$", stripped)
        if m:
            level = len(m.group(1))
            doc.add_heading(m.group(2).strip(), level=level)
            i += 1
            continue

        # 图片
        m = re.match(r"^!\[(.*?)\]\((.*?)\)$", stripped)
        if m:
            add_image(doc, m.group(2), m.group(1), base_dir)
            i += 1
            continue

        # 引用
        if stripped.startswith(">"):
            p = doc.add_paragraph()
            p.paragraph_format.left_indent = Cm(0.5)
            add_inline(p, stripped.lstrip("> ").strip(), size=10.5,
                       color=RGBColor(0x70, 0x70, 0x70), italic=True)
            i += 1
            continue

        # 列表
        m = re.match(r"^[-*]\s+(.*)$", stripped)
        if m:
            p = doc.add_paragraph(style="List Bullet")
            add_inline(p, m.group(1))
            i += 1
            continue
        m = re.match(r"^\d+\.\s+(.*)$", stripped)
        if m:
            p = doc.add_paragraph(style="List Number")
            add_inline(p, m.group(1))
            i += 1
            continue

        # 普通段落
        p = doc.add_paragraph()
        p.paragraph_format.first_line_indent = Cm(0.74)
        p.paragraph_format.line_spacing = 1.5
        p.paragraph_format.space_after = Pt(4)
        add_inline(p, stripped)
        i += 1

    flush_table()
    if code_buf:
        add_code_block(doc, code_buf)


# ---------------------------------------------------------------- 封面
def build_cover(doc):
    for _ in range(3):
        doc.add_paragraph()

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run("项目分析与设计报告")
    set_run_font(run, font=HEAD_FONT, size=28, bold=True)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run("慧医数字医疗应用系统")
    set_run_font(run, font=HEAD_FONT, size=20, bold=False, color=RGBColor(0x13, 0xB7, 0xA5))

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run("Digital Medical Application System")
    set_run_font(run, size=11, color=RGBColor(0x90, 0x93, 0x99))

    for _ in range(6):
        doc.add_paragraph()

    info = [
        ("专业班级", "软件工程 232"),
        ("学　　号", "2330142026"),
        ("姓　　名", "张伟"),
        ("指导教师", "（由学院填写）"),
        ("完成日期", "2026 年 11 月"),
    ]
    for label, value in info:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(10)
        run = p.add_run("%s：%s" % (label, value))
        set_run_font(run, size=14)

    doc.add_page_break()


def add_toc_field(doc):
    """插入 Word 的目录域（TOC）。

    页码由 Word 自己算，配合 settings.xml 里的 updateFields=true，
    打开文档时会自动生成/刷新目录，不用人工去点「更新域」。
    """
    p = doc.add_paragraph()
    run = p.add_run()
    begin = OxmlElement("w:fldChar")
    begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = 'TOC \\o "1-3" \\h \\z \\u'
    separate = OxmlElement("w:fldChar")
    separate.set(qn("w:fldCharType"), "separate")
    placeholder = OxmlElement("w:t")
    placeholder.text = "（打开文档时会自动生成目录；若未生成，右键此处选择「更新域」）"
    end = OxmlElement("w:fldChar")
    end.set(qn("w:fldCharType"), "end")

    for el in (begin, instr, separate, placeholder, end):
        run._r.append(el)
    set_run_font(run, size=10, color=RGBColor(0x90, 0x93, 0x99))


def enable_update_fields(doc):
    """让 Word 打开文档时自动更新域（目录页码）。

    w:updateFields 在 CT_Settings 里有固定顺序，必须插在 w:compat 之类元素之前；
    直接 append 到末尾会违反 schema，Word 会提示「文档已损坏」。
    这里插到 w:defaultTabStop 之后（属于靠前的位置）。
    """
    settings = doc.settings.element
    el = OxmlElement("w:updateFields")
    el.set(qn("w:val"), "true")
    anchor = None
    for tag in ("w:defaultTabStop", "w:characterSpacingControl", "w:compat"):
        anchor = settings.find(qn(tag))
        if anchor is not None:
            break
    if anchor is not None:
        anchor.addprevious(el)
    else:
        settings.insert(0, el)


def build_toc_hint(doc):
    """目录页"""
    p = doc.add_paragraph()
    run = p.add_run("目　录")
    set_run_font(run, font=HEAD_FONT, size=18, bold=True)
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER

    add_toc_field(doc)
    doc.add_page_break()


def main():
    missing = [c for c in CHAPTERS if not os.path.exists(os.path.join(CH_DIR, c))]
    if missing:
        print("缺少章节文件：", missing)
        return 1

    doc = Document()
    init_styles(doc)
    add_page_number(doc)
    enable_update_fields(doc)
    build_cover(doc)
    build_toc_hint(doc)

    for name in CHAPTERS:
        path = os.path.join(CH_DIR, name)
        print("  写入", name)
        render_markdown(doc, path)
        doc.add_page_break()

    doc.save(OUT)

    size = os.path.getsize(OUT) / 1024
    print("已生成：%s（%.0f KB）" % (OUT, size))
    return 0


if __name__ == "__main__":
    sys.exit(main())
