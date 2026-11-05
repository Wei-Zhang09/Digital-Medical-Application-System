#!/usr/bin/env bash
# 打包交付物：软件工程232+2330142026+张伟.zip
#
# 目录结构：
#   01-报告/     项目分析与设计报告 docx + pdf
#   02-答辩/     答辩 PPT + 答辩提纲
#   03-源码/     git archive 导出的受管文件（自动排除 target/node_modules/.dev）
#   04-测试/     测试用例、执行记录、接口清单、Postman 集合
#   05-部署/     部署说明 + nginx.conf
#   项目总结报告.docx / 项目总结.md
#
# 用法：bash scripts/package.sh
set -eu
PROJ_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NAME="软件工程232+2330142026+张伟"
STAGE="$PROJ_ROOT/.dev/package/$NAME"
OUT="$PROJ_ROOT/docs/report/$NAME.zip"
REPORT="$PROJ_ROOT/docs/report"

echo "== 清理旧包 =="
rm -rf "$STAGE"
mkdir -p "$STAGE"/{01-报告,02-答辩,03-源码,04-测试,05-部署}

echo "== 01 报告 =="
cp "$REPORT/慧医数字医疗应用系统-项目分析与设计报告.docx" "$STAGE/01-报告/" 2>/dev/null || echo "  （缺 docx，先跑 python scripts/export_report.py）"
cp "$REPORT/慧医数字医疗应用系统-项目分析与设计报告.pdf" "$STAGE/01-报告/" 2>/dev/null || true

echo "== 02 答辩 =="
cp "$REPORT/慧医数字医疗应用系统-答辩PPT.pptx" "$STAGE/02-答辩/" 2>/dev/null || echo "  （缺 PPT，先跑 python scripts/export_ppt.py）"
cp "$REPORT/答辩提纲.md" "$STAGE/02-答辩/" 2>/dev/null || true

echo "== 03 源码（git archive，只含受管文件）=="
# git 是原生 Windows 程序，不认 /d/... 这种 MSYS 路径，必须先进目录再执行
( cd "$PROJ_ROOT" && git archive HEAD ) | tar -x -C "$STAGE/03-源码"
rm -rf "$STAGE/03-源码/docs" "$STAGE/03-源码/.hermes"

echo "== 04 测试 =="
for f in 测试用例.md 测试执行记录.md 接口清单.md postman-collection.json; do
  cp "$REPORT/$f" "$STAGE/04-测试/" 2>/dev/null || true
done

echo "== 05 部署 =="
cp "$REPORT/部署说明.md" "$STAGE/05-部署/" 2>/dev/null || true
mkdir -p "$STAGE/05-部署/deploy"
cp "$PROJ_ROOT/deploy/nginx.conf" "$STAGE/05-部署/deploy/" 2>/dev/null || true

echo "== 总结 =="
cp "$REPORT/项目总结报告.docx" "$STAGE/" 2>/dev/null || true
cp "$REPORT/项目总结.md" "$STAGE/" 2>/dev/null || true

echo "== 压缩（用 Python zipfile，保证中文文件名正确）=="
rm -f "$OUT"
python - "$(cygpath -w "$PROJ_ROOT/.dev/package")" "$(cygpath -w "$OUT")" "$NAME" <<'PY'
import os, sys, zipfile

base, out, name = sys.argv[1], sys.argv[2], sys.argv[3]
root = os.path.join(base, name)
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as z:
    for dirpath, _, files in os.walk(root):
        for f in files:
            full = os.path.join(dirpath, f)
            rel = os.path.relpath(full, base)
            z.write(full, rel)
print("entries:", len(zipfile.ZipFile(out).namelist()))
PY
echo "已生成：$OUT"
du -sh "$OUT"
echo
echo "包内结构："
( cd "$STAGE" && find . -maxdepth 2 -type d | sort | sed 's/^/  /' )
echo
echo "文件数：$(find "$STAGE" -type f | wc -l)"
