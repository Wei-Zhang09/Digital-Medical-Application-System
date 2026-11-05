"""
把 docs/report/项目总结.md 导出成 Word（复用 export_report.py 的 Markdown 渲染器）。

用法：python scripts/export_summary.py
产物：docs/report/项目总结报告.docx
"""
import os
import sys

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import export_report as er  # noqa: E402

ROOT = er.ROOT
SRC = os.path.join(ROOT, "docs", "report", "项目总结.md")
OUT = os.path.join(ROOT, "docs", "report", "项目总结报告.docx")


def main():
    if not os.path.exists(SRC):
        print("缺少源文件：", SRC)
        return 1

    doc = Document()
    er.init_styles(doc)
    er.add_page_number(doc)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run("项目总结报告")
    er.set_run_font(run, font=er.HEAD_FONT, size=24, bold=True)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run("慧医数字医疗应用系统")
    er.set_run_font(run, font=er.HEAD_FONT, size=14, color=er.RGBColor(0x13, 0xB7, 0xA5))
    doc.add_paragraph()

    er.render_markdown(doc, SRC)
    doc.save(OUT)
    print("已生成：%s（%.0f KB）" % (OUT, os.path.getsize(OUT) / 1024))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
