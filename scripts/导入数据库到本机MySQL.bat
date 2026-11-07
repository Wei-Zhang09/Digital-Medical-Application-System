@echo off
setlocal enabledelayedexpansion
rem ============================================================
rem  把项目数据库导入到本机 MySQL 服务（MySQL09，3306）
rem  前提：MySQL09 已启动（管理员命令行 net start MySQL09）
rem  会提示输入 root 密码；密码只写进临时 cnf，用完即删
rem ============================================================

set "PROJ=%~dp0.."
set "SQLDIR=%PROJ%\sql"
set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set "CNF=%TEMP%\huiyi_my_%RANDOM%.cnf"

if not exist "%MYSQL%" (
  echo [错误] 找不到 mysql.exe：%MYSQL%
  goto end
)

echo 请确认 MySQL09 服务已启动（services.msc 或 net start MySQL09）
echo.
set /p PWD_IN=请输入 MySQL root 密码后回车：

rem 用临时 cnf 传口令：不受命令行特殊字符、环境变量转义影响
> "%CNF%" echo [client]
>> "%CNF%" echo user=root
>> "%CNF%" echo password=%PWD_IN%
>> "%CNF%" echo host=127.0.0.1
>> "%CNF%" echo port=3306
>> "%CNF%" echo default-character-set=utf8mb4

echo.
echo [0/5] 测试连接 ...
"%MYSQL%" --defaults-extra-file="%CNF%" -e "SELECT VERSION() AS mysql_version;" 2>nul
if errorlevel 1 (
  echo.
  echo [失败] 连不上数据库：口令不对，或 MySQL09 没启动。
  goto end
)
echo       连接成功

echo [1/5] 创建数据库 bin_text ...
"%MYSQL%" --defaults-extra-file="%CNF%" -e "CREATE DATABASE IF NOT EXISTS bin_text DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 goto fail

echo [2/5] 导入建表与种子数据（约 1 MB，稍等 10 秒）...
"%MYSQL%" --defaults-extra-file="%CNF%" bin_text < "%SQLDIR%\bin_text.sql"
if errorlevel 1 goto fail

echo [3/5] 统一演示账号口令为 123456 ...
"%MYSQL%" --defaults-extra-file="%CNF%" bin_text < "%SQLDIR%\reset_pwd.sql" >nul
if errorlevel 1 goto fail

echo [4/5] 加销售地点经纬度字段（地图扩展）...
"%MYSQL%" --defaults-extra-file="%CNF%" bin_text < "%SQLDIR%\sale_ext.sql" >nul
if errorlevel 1 goto fail

echo [5/5] 药品图片地址改相对路径 ...
"%MYSQL%" --defaults-extra-file="%CNF%" bin_text < "%SQLDIR%\image_url_relative.sql" >nul
if errorlevel 1 goto fail

echo.
echo 校验结果：
"%MYSQL%" --defaults-extra-file="%CNF%" -N -e "SELECT CONCAT('  表数量: ', COUNT(*)) FROM information_schema.tables WHERE table_schema='bin_text'; SELECT CONCAT('  账号: ', uname, ' / ', utype) FROM bin_text.account WHERE uname IN ('admin_1','王小明4125'); SELECT CONCAT('  药品: ', COUNT(*), ' 条') FROM bin_text.drug;"

echo.
echo 完成：库 bin_text 已导入 127.0.0.1:3306
echo 管理员 admin_1 / 123456    医师 王小明4125 / 123456
goto cleanup

:fail
echo.
echo [失败] 上一步执行出错，请把上面的报错发我

:cleanup
if exist "%CNF%" del /q "%CNF%" >nul 2>&1

:end
echo.
echo 按任意键关闭窗口...
pause >nul
endlocal
