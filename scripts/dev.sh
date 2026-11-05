#!/usr/bin/env bash
# 开发态一键启动（数据库 + 后端 8080 + 前端 9092）
#
# 用法：
#   bash scripts/dev.sh          启动全部（前端在前台，Ctrl+C 结束前端；后端留在后台）
#   bash scripts/dev.sh backend  只启动数据库 + 后端
#   bash scripts/dev.sh frontend 只启动前端（后端已在跑时用）
#   bash scripts/dev.sh stop     停止后端与前端
#   bash scripts/dev.sh status   查看端口与进程状态
#
# 说明：本机系统 Node 是 26.x，前端必须用项目内的 Node 22.13（.dev/node22），脚本已处理。
set -u
PROJ_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NODE_DIR="$PROJ_ROOT/.dev/node22/PFiles64/nodejs"
JAR="$PROJ_ROOT/backend/target/medicai-1.0-SNAPSHOT.jar"
LOG="$PROJ_ROOT/backend/target/app.log"

port_pid () { netstat -ano 2>/dev/null | grep ":$1 .*LISTENING" | awk '{print $5}' | head -1; }

status () {
  echo "MySQL  : $(bash "$PROJ_ROOT/scripts/db.sh" status)"
  p=$(port_pid 8080); [ -n "$p" ] && echo "后端   : 运行中 pid=$p  http://127.0.0.1:8080" || echo "后端   : 未启动"
  p=$(port_pid 9092); [ -n "$p" ] && echo "前端   : 运行中 pid=$p  http://localhost:9092" || echo "前端   : 未启动"
  p=$(port_pid 80);   [ -n "$p" ] && echo "Nginx  : 运行中 pid=$p  http://127.0.0.1" || echo "Nginx  : 未启动（部署态才需要）"
}

stop_all () {
  echo "== 停止前端 =="
  p=$(port_pid 9092); [ -n "$p" ] && powershell -NoProfile -Command "Stop-Process -Id $p -Force" 2>/dev/null
  echo "== 停止后端 =="
  p=$(port_pid 8080); [ -n "$p" ] && powershell -NoProfile -Command "Stop-Process -Id $p -Force" 2>/dev/null
  sleep 1
  echo "已停止（数据库实例保留运行，需要停用 bash scripts/db.sh stop）"
}

start_db_and_backend () {
  bash "$PROJ_ROOT/scripts/db.sh" start

  if [ ! -f "$JAR" ]; then
    echo "== 没找到 jar，先打包 =="
    ( cd "$PROJ_ROOT/backend" && powershell -NoProfile -Command "& 'D:\Work_app\apache-maven-3.9.11\bin\mvn.cmd' -B -q clean package -DskipTests" )
  fi

  if [ -n "$(port_pid 8080)" ]; then
    echo "后端已在 8080 运行，跳过"
  else
    echo "== 启动后端（日志：backend/target/app.log）=="
    mkdir -p "$PROJ_ROOT/backend/target/uploads"
    bash "$PROJ_ROOT/scripts/seed-images.sh" "$PROJ_ROOT/backend/target/uploads" >/dev/null
    # java 是原生 Windows 程序，不认 /d/... 路径；用 PowerShell 的 Start-Process
    # 以 target 为工作目录启动，进程完全脱离当前 shell（否则脚本退出后终端会一直挂着）
    WIN_TARGET="$(cygpath -w "$PROJ_ROOT/backend/target")"
    WIN_LOG="$(cygpath -w "$LOG")"
    powershell -NoProfile -Command "Start-Process -FilePath 'java' -ArgumentList '-jar','medicai-1.0-SNAPSHOT.jar' -WorkingDirectory '$WIN_TARGET' -WindowStyle Hidden -RedirectStandardOutput '$WIN_LOG' -RedirectStandardError '$WIN_LOG.err'" >/dev/null

    for i in $(seq 1 40); do
      sleep 2
      if curl -s -f --max-time 2 "http://127.0.0.1:8080/swagger-ui.html/" > /dev/null; then
        echo "后端就绪：http://127.0.0.1:8080/swagger-ui.html/"
        return 0
      fi
    done
    echo "后端 80 秒没起来，看日志：$LOG"
    tail -20 "$LOG"
    return 1
  fi
}

start_frontend () {
  if [ ! -d "$PROJ_ROOT/frontend/node_modules" ]; then
    echo "== 首次运行，先 npm install（约 5 分钟）=="
    ( cd "$PROJ_ROOT/frontend" && PATH="$NODE_DIR:$PATH" npm install --no-audit --no-fund )
  fi
  echo "== 启动前端 dev server（Ctrl+C 结束）=="
  cd "$PROJ_ROOT/frontend"
  PATH="$NODE_DIR:$PATH" npm run serve
}

case "${1:-all}" in
  stop)     stop_all ;;
  status)   status ;;
  backend)  start_db_and_backend ;;
  frontend) start_frontend ;;
  all)
    start_db_and_backend || exit 1
    echo
    echo "后端已就绪，正在起前端；起好后浏览器打开 http://localhost:9092/#/user/login"
    echo "账号：admin_1 / 123456　医师：王小明4125 / 123456"
    echo
    start_frontend
    ;;
  *) echo "用法：bash scripts/dev.sh [all|backend|frontend|stop|status]"; exit 2 ;;
esac
