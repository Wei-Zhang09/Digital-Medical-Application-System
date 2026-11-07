@echo off
setlocal
rem 启动项目专用 MySQL 实例（127.0.0.1:3307）
rem 系统里的 MySQL09 是管理员装的服务，普通用户起不来，所以项目自带一个实例。

set "PROJ=%~dp0.."
set "MYSQLD=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe"
set "DATADIR=%PROJ%\.dev\mysql\data"
set "LOGFILE=%PROJ%\.dev\logs\mysqld.err"
set "PIDFILE=%PROJ%\.dev\mysql\mysqld.pid"

netstat -ano | findstr ":3307" | findstr "LISTENING" >nul
if not errorlevel 1 goto already

if not exist "%PROJ%\.dev\logs" mkdir "%PROJ%\.dev\logs"

rem 路径里有空格，参数值必须加内层双引号，否则 mysqld 会把 datadir 截断成 "D:\HermesWorkSpace\Digital"
powershell -NoProfile -Command "Start-Process -FilePath 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe' -ArgumentList @('--no-defaults','--basedir=\"C:\Program Files\MySQL\MySQL Server 8.0\"','--datadir=\"%DATADIR%\"','--port=3307','--bind-address=127.0.0.1','--character-set-server=utf8mb4','--collation-server=utf8mb4_general_ci','--lower-case-table-names=1','--log-error=\"%LOGFILE%\"','--mysqlx=OFF','--pid-file=\"%PIDFILE%\"') -WindowStyle Hidden"

echo 正在启动 MySQL 3307 ...
set /a tries=0

:wait
netstat -ano | findstr ":3307" | findstr "LISTENING" >nul
if not errorlevel 1 goto ok
set /a tries+=1
if %tries% GEQ 15 goto failed
ping -n 2 127.0.0.1 >nul
goto wait

:ok
echo 启动成功：127.0.0.1:3307   库 digital_medical_application_system   账号 root/123456
goto end

:already
echo MySQL 已在 3307 运行
goto end

:failed
echo 启动失败，请看日志：%LOGFILE%
goto end

:end
endlocal
