#!/usr/bin/env bash
# 启动/停止项目专用 MySQL 实例（端口 3307，数据目录 .dev/mysql/data）
# 为什么不用系统服务 MySQL09：该服务由管理员账户安装，普通用户既不能启动也不能读它的数据目录。
# 这个实例完全在项目目录内，初始化用 --initialize-insecure，root 口令已改为 123456。

set -u
PROJ_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MYSQLD="C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqld.exe"
DATADIR="$PROJ_ROOT/.dev/mysql/data"
LOGFILE="$PROJ_ROOT/.dev/logs/mysqld.err"
PIDFILE="$PROJ_ROOT/.dev/mysql/mysqld.pid"
PORT=3307

is_up() { netstat -ano | grep -q "127.0.0.1:$PORT .*LISTENING"; }

case "${1:-start}" in
  start)
    if is_up; then echo "MySQL 已在 $PORT 运行"; exit 0; fi
    mkdir -p "$PROJ_ROOT/.dev/logs"
    # 用 PowerShell 的 Start-Process 启动：让 mysqld 完全脱离当前 shell 的进程树。
    # 只加 nohup 不够——调用方（终端/脚本）结束时会把整个进程组带走，
    # 现象是日志里明明写着 ready for connections，端口却马上又没了。
    WIN_DATADIR="$(cygpath -w "$DATADIR")"
    WIN_LOG="$(cygpath -w "$LOGFILE")"
    WIN_PID="$(cygpath -w "$PIDFILE")"
    powershell -NoProfile -Command "Start-Process -FilePath 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe' -ArgumentList @('--no-defaults','--basedir=C:\Program Files\MySQL\MySQL Server 8.0','--datadir=$WIN_DATADIR','--port=$PORT','--bind-address=127.0.0.1','--character-set-server=utf8mb4','--collation-server=utf8mb4_general_ci','--lower-case-table-names=1','--log-error=$WIN_LOG','--mysqlx=OFF','--pid-file=$WIN_PID') -WindowStyle Hidden" >/dev/null 2>&1

    for i in $(seq 1 30); do sleep 1; if is_up; then echo "MySQL 已启动：127.0.0.1:$PORT"; exit 0; fi; done
    echo "启动失败，看日志：$LOGFILE"; tail -20 "$LOGFILE"; exit 1
    ;;
  stop)
    if [ -f "$PIDFILE" ]; then kill "$(cat "$PIDFILE")" 2>/dev/null && echo "已停止"; else echo "没有 pid 文件，按端口杀"; fi
    ;;
  status)
    if is_up; then echo "running on $PORT"; else echo "stopped"; fi
    ;;
  *) echo "用法: $0 {start|stop|status}"; exit 2 ;;
esac
