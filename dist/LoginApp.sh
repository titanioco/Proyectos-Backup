#!/bin/bash

echo "Starting Login Application..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed!"
    echo "Please install Java 11 or higher."
    exit 1
fi

# Get the directory where this script is located
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Run the application
java -Xmx512m -jar "$DIR/LoginApp-portable.jar"
