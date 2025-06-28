#!/bin/bash

# Simple installer for Login Application
# This script will copy the application to a system directory and create desktop shortcuts

APP_NAME="LoginApp"
INSTALL_DIR="/opt/$APP_NAME"
DESKTOP_FILE="$HOME/.local/share/applications/${APP_NAME}.desktop"
BIN_LINK="/usr/local/bin/$APP_NAME"

echo "🚀 $APP_NAME Installer"
echo "====================="

# Check if running as root for system-wide installation
if [ "$EUID" -eq 0 ]; then
    echo "Installing system-wide..."
    INSTALL_DIR="/opt/$APP_NAME"
    DESKTOP_FILE="/usr/share/applications/${APP_NAME}.desktop"
else
    echo "Installing for current user..."
    INSTALL_DIR="$HOME/.local/opt/$APP_NAME"
    BIN_LINK="$HOME/.local/bin/$APP_NAME"
fi

# Create installation directory
echo "📁 Creating installation directory: $INSTALL_DIR"
sudo mkdir -p "$INSTALL_DIR"

# Copy application files
echo "📋 Copying application files..."
sudo cp -r LoginApp/* "$INSTALL_DIR/"
sudo cp LoginApp-portable.jar "$INSTALL_DIR/"
sudo cp README.md "$INSTALL_DIR/"

# Make executable
sudo chmod +x "$INSTALL_DIR/bin/LoginApp"

# Create symlink for command line access
echo "🔗 Creating command line shortcut..."
sudo ln -sf "$INSTALL_DIR/bin/LoginApp" "$BIN_LINK"

# Create desktop entry
echo "🖥️  Creating desktop shortcut..."
mkdir -p "$(dirname "$DESKTOP_FILE")"

cat > "$DESKTOP_FILE" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=$APP_NAME
Comment=Login and Registration Application
Exec=$INSTALL_DIR/bin/LoginApp
Icon=$INSTALL_DIR/icon.png
Terminal=false
Categories=Utility;
StartupNotify=true
EOF

chmod +x "$DESKTOP_FILE"

echo ""
echo "✅ Installation complete!"
echo ""
echo "🎯 You can now run the application by:"
echo "   • Searching for '$APP_NAME' in your applications menu"
echo "   • Running '$APP_NAME' in terminal"
echo "   • Executing: $INSTALL_DIR/bin/LoginApp"
echo ""
echo "📂 Installation location: $INSTALL_DIR"
echo ""

# Test the installation
echo "🧪 Testing installation..."
if [ -x "$INSTALL_DIR/bin/LoginApp" ]; then
    echo "✅ Executable is properly installed"
else
    echo "❌ Installation may have failed"
    exit 1
fi

echo ""
echo "🗑️  To uninstall, run:"
echo "   sudo rm -rf '$INSTALL_DIR'"
echo "   sudo rm -f '$BIN_LINK'"
echo "   rm -f '$DESKTOP_FILE'"
