@echo off
setlocal
rem 停止项目专用 MySQL 实例（3307）

set "PROJ=%~dp0.."
set "PIDFILE=%PROJ%\.dev\mysql\mysqld.pid"

if not exist "%PIDFILE%" goto byport

set /p PID=<"%PIDFILE%"
powershell -NoProfile -Command "Stop-Process -Id %PID% -Force -ErrorAction SilentlyContinue"
echo 已停止 MySQL（pid %PID%）
goto end

:byport
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3307" ^| findstr "LISTENING"') do (
  powershell -NoProfile -Command "Stop-Process -Id %%a -Force -ErrorAction SilentlyContinue"
  echo 已停止 pid %%a
)
goto end

:end
endlocal
