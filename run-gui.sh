#!/bin/bash

# Script to run the Java Swing GUI application
echo "Starting Java Login Application..."

# Check if virtual display is running
if ! pgrep -f "Xvfb :99" > /dev/null; then
    echo "Starting virtual display..."
    Xvfb :99 -screen 0 1024x768x24 &
    sleep 2
fi

# Set display environment
export DISPLAY=:99

# Clean, compile and run the application
echo "Building application..."
cd /workspaces/Proyectos
ant clean compile run &

# Wait a moment for the application to start
sleep 3

# Start VNC server if not running
if ! pgrep -f "x11vnc" > /dev/null; then
    echo "Starting VNC server on port 5900..."
    x11vnc -display :99 -nopw -listen localhost -xkb -forever -shared -bg > /dev/null 2>&1
fi

echo ""
echo "================================================================"
echo "Java Login Application is now running!"
echo "================================================================"
echo "GUI is available via VNC on localhost:5900"
echo "You can connect using a VNC viewer or use VS Code's port forwarding"
echo ""
echo "To view the application:"
echo "1. Forward port 5900 in VS Code"
echo "2. Use a VNC client to connect to localhost:5900"
echo "3. Or use a web-based VNC viewer"
echo ""
echo "To stop the application:"
echo "- Kill this terminal session or press Ctrl+C"
echo "- Or run: pkill -f 'java.*Main'"
echo "================================================================"

# Keep script running
wait
