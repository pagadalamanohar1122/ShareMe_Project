#!/bin/bash

echo "Starting ShareMe TaskSphere Application..."
echo

echo "Setting up database (if needed)..."
echo "Please ensure MySQL is running and execute database/setup.sql manually if this is your first run."
echo

echo "Starting Backend Server..."
cd backend
gnome-terminal --title="Backend Server" -- bash -c "mvn spring-boot:run; exec bash" &
echo "Backend starting on http://localhost:8080"
echo

echo "Waiting for backend to initialize..."
sleep 10

echo "Starting Frontend Development Server..."
cd ../frontend
gnome-terminal --title="Frontend Server" -- bash -c "npm run dev; exec bash" &
echo "Frontend starting on http://localhost:3000"
echo

echo "Both servers are starting..."
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:3000"
echo "API Documentation: http://localhost:8080/swagger-ui/index.html"
echo

echo "Press Enter to continue..."
read