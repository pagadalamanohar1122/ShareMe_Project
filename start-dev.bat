@echo off
echo Starting TaskSphere Development Environment...

REM Start backend
echo Starting Backend Server...
start cmd /k "cd backend && mvn spring-boot:run"

REM Wait for backend to start
echo Waiting for backend to initialize...
timeout /t 30

REM Start frontend
echo Starting Frontend Server...
start cmd /k "cd frontend && npm run dev"

echo Development environment is starting up...
echo Backend will be available at http://localhost:8080
echo Frontend will be available at http://localhost:5173
echo Swagger UI will be available at http://localhost:8080/swagger-ui.html