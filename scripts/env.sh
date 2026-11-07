#!/usr/bin/env bash
# 慧医项目 · 本机开发环境（bash / git-bash）
# 用法：source scripts/env.sh
# 说明：本机全局 Node 是 26.x，官方要求 22.13；Maven 的 bash 包装脚本在本机会报
#       classworlds 缺失，所以这里直接用 mvn.cmd。MySQL 用项目自带的 3307 实例。

PROJ_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export PROJ_ROOT

# Node 22.13（从 MSI 解出来，不改系统 PATH）
export NODE_HOME="$PROJ_ROOT/.dev/node22/PFiles64/nodejs"
export PATH="$NODE_HOME:$PATH"

# Maven（用 .cmd，bash 版包装在本机跑不起来）
export MAVEN_HOME="D:/Work_app/apache-maven-3.9.11"
export PATH="$MAVEN_HOME/bin:$PATH"
mvn() { powershell -NoProfile -Command "& 'D:\Work_app\apache-maven-3.9.11\bin\mvn.cmd' $*"; }

# MySQL 客户端 + 项目实例（3307）
export MYSQL_HOME="C:/Program Files/MySQL/MySQL Server 8.0"
export PATH="$MYSQL_HOME/bin:$PATH"
export DB_HOST=127.0.0.1
export DB_PORT=3307
export DB_USER=root
export DB_PASS=123456
export DB_NAME=digital_medical_application_system
mysql_bin() { "$MYSQL_HOME/bin/mysql.exe" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 "$@"; }

echo "PROJ_ROOT=$PROJ_ROOT"
echo "node=$(node -v 2>/dev/null) npm=$(npm -v 2>/dev/null)"
echo "db=$DB_HOST:$DB_PORT/$DB_NAME"
