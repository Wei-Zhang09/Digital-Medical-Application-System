@echo off
setlocal
rem ============================================================
rem  把项目数据库导入到你本机的 MySQL 服务（MySQL09，端口 3306）
rem  前提：MySQL09 服务已经启动
rem  会提示输入 root 密码（输入时不回显），只问一次
rem ============================================================

set "PROJ=%~dp0.."
set "SQLDIR=%PROJ%\sql"
set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

if not exist "%MYSQL%" (
  echo 找不到 mysql.exe：%MYSQL%
  echo 请把 MySQL 安装路径改成你本机的路径后重试
  goto end
)

echo 请确认 MySQL09 服务已启动（services.msc 里看，或在管理员命令行执行 net start MySQL09）
echo.
set /p PWD_IN=请输入 MySQL root 密码后回车：

set "MYSQL_PWD=%PWD_IN%"

echo.
echo [1/5] 创建数据库 bin_text ...
"%MYSQL%" -uroot --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS bin_text DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 goto fail

echo [2/5] 导入建表与种子数据（约 1 MB，稍等）...
"%MYSQL%" -uroot --default-character-set=utf8mb4 bin_text < "%SQLDIR%\bin_text.sql"
if errorlevel 1 goto fail

echo [3/5] 统一演示账号口令为 123456 ...
"%MYSQL%" -uroot --default-character-set=utf8mb4 bin_text < "%SQLDIR%\reset_pwd.sql"
if errorlevel 1 goto fail

echo [4/5] 加销售地点经纬度字段（地图扩展）...
"%MYSQL%" -uroot --default-character-set=utf8mb4 bin_text < "%SQLDIR%\sale_ext.sql"
if errorlevel 1 goto fail

echo [5/5] 药品图片地址改相对路径 ...
"%MYSQL%" -uroot --default-character-set=utf8mb4 bin_text < "%SQLDIR%\image_url_relative.sql"
if errorlevel 1 goto fail

echo.
echo 完成：库 bin_text 已导入到 127.0.0.1:3306
echo 账号：管理员 admin_1 / 123456    医师 王小明4125 / 123456
set "MYSQL_PWD="
goto end

:fail
echo.
echo 导入失败：请检查 MySQL09 是否已启动、密码是否正确
set "MYSQL_PWD="
goto end

:end
endlocal
