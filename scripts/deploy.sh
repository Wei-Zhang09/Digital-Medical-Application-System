#!/usr/bin/env bash
# 慧医数字医疗应用系统 · 本机部署脚本（Windows / git-bash）
#
# 做四件事：打后端 jar → 打前端 dist（走 .env.production）→ 拷进 Nginx 站点目录 → 起后端与 Nginx
#
# 用法：
#   bash scripts/deploy.sh          # 完整部署（构建 + 启动）
#   bash scripts/deploy.sh build    # 只构建，不启动
#   bash scripts/deploy.sh stop     # 停止后端与 Nginx
#
# 前置：.dev/nginx/nginx-1.20.1 已解压（见 docs/report/部署说明.md 第 2 步）
set -eu
PROJ_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NGINX_DIR="$PROJ_ROOT/.dev/nginx/nginx-1.20.1"
SITE_DIR="$NGINX_DIR/html/medical"
MYSQL_BIN="C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"

stop_all () {
  echo "== 停止 Nginx =="
  ( cd "$NGINX_DIR" && ./nginx.exe -s quit 2>/dev/null ) || true
  echo "== 停止后端 =="
  PID=$(netstat -ano | grep ":8080 .*LISTENING" | awk '{print $5}' | head -1 || true)
  if [ -n "${PID:-}" ]; then powershell -NoProfile -Command "Stop-Process -Id $PID -Force" 2>/dev/null || true; fi
  sleep 1
  echo "已停止"
}

if [ "${1:-all}" = "stop" ]; then stop_all; exit 0; fi

echo "== 1/5 打包后端 =="
( cd "$PROJ_ROOT/backend" && powershell -NoProfile -Command "& 'D:\Work_app\apache-maven-3.9.11\bin\mvn.cmd' -B -q clean package -DskipTests" )
ls -la "$PROJ_ROOT/backend/target/medicai-1.0-SNAPSHOT.jar"

echo "== 2/5 打包前端（生产模式，baseURL=/api）=="
( cd "$PROJ_ROOT/frontend" && PATH="$PROJ_ROOT/.dev/node22/PFiles64/nodejs:$PATH" npm run build )

echo "== 3/5 部署到 Nginx 站点目录 =="
rm -rf "$SITE_DIR"
mkdir -p "$SITE_DIR"
cp -r "$PROJ_ROOT/frontend/dist/"* "$SITE_DIR/"
cp "$PROJ_ROOT/deploy/nginx.conf" "$NGINX_DIR/conf/nginx.conf"

echo "== 4/5 准备上传目录与种子图片 =="
mkdir -p "$PROJ_ROOT/backend/target/uploads"
bash "$PROJ_ROOT/scripts/seed-images.sh" "$PROJ_ROOT/backend/target/uploads"

echo "== 5/5 启动 =="
if [ "${1:-all}" = "build" ]; then echo "只构建，未启动"; exit 0; fi

( cd "$NGINX_DIR" && ./nginx.exe -t && ./nginx.exe )
nohup java -jar "$PROJ_ROOT/backend/target/medicai-1.0-SNAPSHOT.jar" > "$PROJ_ROOT/backend/target/app.log" 2>&1 &
echo "等待后端启动..."
for i in $(seq 1 40); do
  sleep 2
  if curl -s -o /dev/null --max-time 3 -X POST "http://127.0.0.1/api/login" -d "username=admin_1&password=123456"; then
    echo "部署完成：http://127.0.0.1/#/user/login"
    echo "  管理员 admin_1 / 123456 ；医师 王小明4125 / 123456"
    exit 0
  fi
done
echo "后端 80 秒内没起来，看日志：$PROJ_ROOT/backend/target/app.log"
exit 1
