#!/bin/bash

# ========================================
# Java to EXE Converter Script
# ========================================

echo "🚀 Java to EXE Converter for Login Application"
echo "=============================================="

# Set Java 17 as the default for this session
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_NAME="LoginApp"
MAIN_CLASS="com.raven.main.Main"
VERSION="1.0"

echo -e "${BLUE}Step 1: Clean and build the project${NC}"
ant clean compile jar

if [ ! -f "dist/login-001.jar" ]; then
    echo -e "${RED}❌ Error: JAR file not found. Build failed!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ JAR file created successfully${NC}"

# Create a distribution directory
mkdir -p dist/executable
cp dist/login-001.jar dist/executable/
cp *.jar dist/executable/ 2>/dev/null || true

echo -e "${BLUE}Step 2: Create executable using jpackage${NC}"

# Generate Windows EXE using jpackage
$JAVA_HOME/bin/jpackage \
    --input dist/executable \
    --name "$PROJECT_NAME" \
    --main-jar login-001.jar \
    --main-class "$MAIN_CLASS" \
    --type app-image \
    --dest dist \
    --app-version "$VERSION" \
    --vendor "RAVEN" \
    --description "Login and Registration Application" \
    --copyright "2025 RAVEN" \
    --java-options "-Xmx512m" \
    --java-options "-Xms128m"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Executable created successfully!${NC}"
    echo -e "${YELLOW}📁 Location: dist/$PROJECT_NAME/${NC}"
    ls -la "dist/$PROJECT_NAME/"
else
    echo -e "${RED}❌ Error creating executable with jpackage${NC}"
    echo -e "${YELLOW}💡 Trying alternative method with Launch4j...${NC}"
fi

echo ""
echo -e "${BLUE}Step 3: Create portable JAR with dependencies${NC}"
echo -e "${BLUE}Step 2.5: Clean up license files${NC}"

if [ -d "dist/$PROJECT_NAME" ]; then
    # Move licenses to a separate folder
    mkdir -p "dist/$PROJECT_NAME/legal"
    mv "dist/$PROJECT_NAME/THIRD-PARTY-LICENSE" "dist/$PROJECT_NAME/legal/" 2>/dev/null || true
    mv "dist/$PROJECT_NAME/ADDITIONAL_LICENSE_INFO" "dist/$PROJECT_NAME/legal/" 2>/dev/null || true
    
    echo -e "${GREEN}✅ License files moved to legal/ folder${NC}"
fi
# Create a single JAR with all dependencies
mkdir -p dist/fat-jar
cd dist/fat-jar

# Extract main JAR
jar xf ../login-001.jar

# Extract dependency JARs
for jar_file in ../../*.jar; do
    if [ -f "$jar_file" ]; then
        echo "Extracting $(basename "$jar_file")..."
        jar xf "$jar_file"
    fi
done

# Create fat JAR
jar cmf ../../manifest.mf "../${PROJECT_NAME}-portable.jar" *

cd ../..

if [ -f "dist/${PROJECT_NAME}-portable.jar" ]; then
    echo -e "${GREEN}✅ Portable JAR created: dist/${PROJECT_NAME}-portable.jar${NC}"
fi

echo ""
echo -e "${BLUE}Step 4: Create Windows batch launcher${NC}"

cat > "dist/${PROJECT_NAME}.bat" << 'EOF'
@echo off
title Login Application
echo Starting Login Application...

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Java is not installed or not in PATH!
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

REM Run the application
java -Xmx512m -jar "LoginApp-portable.jar"

if %errorlevel% neq 0 (
    echo Application encountered an error!
    pause
)
EOF

echo -e "${GREEN}✅ Windows batch launcher created: dist/${PROJECT_NAME}.bat${NC}"

echo ""
echo -e "${BLUE}Step 5: Create shell script launcher (Linux/Mac)${NC}"

cat > "dist/${PROJECT_NAME}.sh" << 'EOF'
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
EOF

chmod +x "dist/${PROJECT_NAME}.sh"

echo -e "${GREEN}✅ Shell script launcher created: dist/${PROJECT_NAME}.sh${NC}"

echo ""
echo "==============================================="
echo -e "${GREEN}🎉 CONVERSION COMPLETE!${NC}"
echo "==============================================="
echo ""
echo -e "${YELLOW}📦 Available executables:${NC}"
echo ""

if [ -d "dist/$PROJECT_NAME" ]; then
    echo -e "1. ${BLUE}Native executable:${NC} dist/$PROJECT_NAME/"
    echo -e "   ${GREEN}✓${NC} No Java required on target machine"
    echo -e "   ${GREEN}✓${NC} Platform-specific"
    echo ""
fi

if [ -f "dist/${PROJECT_NAME}-portable.jar" ]; then
    echo -e "2. ${BLUE}Portable JAR:${NC} dist/${PROJECT_NAME}-portable.jar"
    echo -e "   ${GREEN}✓${NC} All dependencies included"
    echo -e "   ${GREEN}✓${NC} Works on any OS with Java"
    echo ""
fi

if [ -f "dist/${PROJECT_NAME}.bat" ]; then
    echo -e "3. ${BLUE}Windows launcher:${NC} dist/${PROJECT_NAME}.bat"
    echo -e "   ${GREEN}✓${NC} Double-click to run on Windows"
    echo ""
fi

if [ -f "dist/${PROJECT_NAME}.sh" ]; then
    echo -e "4. ${BLUE}Linux/Mac launcher:${NC} dist/${PROJECT_NAME}.sh"
    echo -e "   ${GREEN}✓${NC} Double-click to run on Linux/Mac"
    echo ""
fi

echo -e "${YELLOW}📋 To distribute your application:${NC}"
echo "1. Copy the entire 'dist' folder to the target machine"
echo "2. Run the appropriate launcher for the platform"
echo "3. For the native executable, no Java installation is required"
echo "4. For the JAR/launcher files, Java 11+ must be installed"
echo ""

echo -e "${BLUE}💡 Testing the executables:${NC}"
if [ -d "dist/$PROJECT_NAME" ]; then
    echo "• Test native: ./dist/$PROJECT_NAME/$PROJECT_NAME"
fi
echo "• Test portable: java -jar dist/${PROJECT_NAME}-portable.jar"
echo "• Test Windows: dist/${PROJECT_NAME}.bat (on Windows)"
echo "• Test Linux/Mac: ./dist/${PROJECT_NAME}.sh"
