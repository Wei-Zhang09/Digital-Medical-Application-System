#!/usr/bin/env bash
# 把种子药品图片放到后端运行目录的 uploads/，让库里那些 http://localhost:8080/image/xxx 能显示。
#
# 为什么需要：bin_text.sql 里的 drug.drug_img 是相对路径 /image/xxx，
# 而 /image/** 映射的是「jar 工作目录下的 uploads/」。图片文件在 backend/seed-images/ 里。
#
# 两个常见工作目录都要放：
#   backend/target/uploads  —— 命令行 java -jar（dev.sh / deploy.sh 用这个）
#   backend/uploads         —— IDEA 直接 Run（默认工作目录是模块根 backend/）
#
# 用法：bash scripts/seed-images.sh [目标目录...]
#   不带参数时两个目录都拷。
set -eu
PROJ_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$PROJ_ROOT/backend/seed-images"

if [ "$#" -gt 0 ]; then
  TARGETS=("$@")
else
  TARGETS=("$PROJ_ROOT/backend/target/uploads" "$PROJ_ROOT/backend/uploads")
fi

for DST in "${TARGETS[@]}"; do
  mkdir -p "$DST"
  count=0
  for f in "$SRC"/*.jpg; do
    [ -e "$f" ] || continue
    cp -n "$f" "$DST/" && count=$((count+1))
  done
  echo "已拷贝 $count 张种子图片到 $DST"
done
