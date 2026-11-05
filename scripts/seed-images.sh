#!/usr/bin/env bash
# 把种子药品图片放到后端运行目录的 uploads/，让库里那些 http://localhost:8080/image/xxx 能显示。
#
# 为什么需要：bin_text.sql 里的 drug.drug_img 全部写死了 localhost:8080 的绝对地址，
# 而 /image/** 映射的是 jar 工作目录下的 uploads/。图片文件在 backend/seed-images/ 里。
#
# 用法：bash scripts/seed-images.sh [目标目录]
#   默认目标：backend/target/uploads（开发/演示跑 jar 的位置）
set -eu
PROJ_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$PROJ_ROOT/backend/seed-images"
DST="${1:-$PROJ_ROOT/backend/target/uploads}"

mkdir -p "$DST"
count=0
for f in "$SRC"/*.jpg; do
  [ -e "$f" ] || continue
  cp -n "$f" "$DST/" && count=$((count+1))
done
echo "已拷贝 $count 张种子图片到 $DST"
