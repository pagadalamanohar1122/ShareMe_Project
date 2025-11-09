@echo off
setlocal enabledelayedexpansion
echo === ShareMe TaskSphere Startup ===
echo.

REM Check MySQL service (common Windows service names: MySQL, MySQL80, mysql)
set MYSQL_SERVICE=
for %%S in (MySQL MySQL80 mysql) do (
	if not defined MYSQL_SERVICE (
		sc query %%S >nul 2>&1 && set MYSQL_SERVICE=%%S
	)
)
if not defined MYSQL_SERVICE (
	echo [WARN] MySQL service not detected by common names (MySQL/MySQL80/mysql). Ensure database is running.
) else (
	echo [OK] MySQL service detected: %MYSQL_SERVICE%
)
echo.

REM Ensure required databases exist (lightweight check)
echo Checking database connectivity (skip if mysql client not installed)...
where mysql >nul 2>&1
if %errorlevel% equ 0 (
	for %%D in (shareme) do (
		mysql -e "CREATE DATABASE IF NOT EXISTS %%D" 2>nul
		if !errorlevel! equ 0 (echo   [OK] Database %%D ready) else (echo   [WARN] Could not verify/create database %%D)
	)
) else (
	echo [INFO] mysql client not found in PATH; skipping DB existence check.
)
echo.

REM Free port 8080 if occupied
for /f "tokens=5" %%P in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
	echo [INFO] Port 8080 in use by PID %%P. Attempting to terminate...
	taskkill /PID %%P /F >nul 2>&1
	if !errorlevel! equ 0 (echo   [OK] Freed port 8080) else (echo   [WARN] Could not terminate PID %%P; startup may fail.)
)
echo.

REM Start Backend (dev profile for relaxed settings)
echo Starting Backend Server (dev profile, MySQL: shareme)...
pushd backend
start "Backend Server" cmd /k "mvn -q -Dspring-boot.run.profiles=dev spring-boot:run"
popd
echo   Backend launching on http://localhost:8080
echo.

REM Wait and health check loop (max 30s)
set /a WAIT=0
echo Waiting for backend readiness (health)...
:health_loop
curl -s -o nul http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
	echo   [OK] Backend health endpoint reachable.
) else (
	set /a WAIT+=5
	if !WAIT! GEQ 30 (
		echo   [ERROR] Backend not reachable after !WAIT!s. Check logs in Backend Server window.
		goto skip_frontend
	)
	timeout /t 5 /nobreak >nul
	goto health_loop
)
echo.

REM Start Frontend
echo Starting Frontend Dev Server...
pushd frontend
start "Frontend Server" cmd /k "npm run dev"
popd
echo   Frontend launching on http://localhost:3000
echo.

:skip_frontend
echo === Startup Summary ===
echo Backend:  http://localhost:8080
echo Health:   http://localhost:8080/actuator/health
echo Swagger:  http://localhost:8080/swagger-ui/index.html
echo Frontend: http://localhost:3000
echo.
echo To avoid future connection refused errors:
echo  1. Ensure port 8080 free (this script auto-frees it).
echo  2. Keep Backend Server window open; closing it stops the API.
echo  3. Use dev profile for local (auto DB update, disables Flyway).
echo  4. Check health endpoint before hitting other APIs.
echo.
pause
endlocal
