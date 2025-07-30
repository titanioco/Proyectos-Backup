# 🛠️ Setup and Recovery Procedures

This document provides detailed instructions for setting up the University Project Management System on different machines and recovering from common issues.

## 📋 Table of Contents
- [New Machine Setup](#new-machine-setup)
- [Recovery Procedures](#recovery-procedures)
- [Configuration Management](#configuration-management)
- [Database Recovery](#database-recovery)
- [Troubleshooting Guide](#troubleshooting-guide)
- [Platform-Specific Instructions](#platform-specific-instructions)

## 🖥️ New Machine Setup

### Prerequisites Installation

#### Step 1: Install Java Development Kit (JDK)
**Recommended: Java 8 or Higher (Tested with Java 21)**

##### Windows:
1. Download JDK from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. Run the installer and follow the wizard
3. Verify installation:
   ```powershell
   java -version
   javac -version
   ```

##### macOS:
```bash
# Using Homebrew (recommended)
brew install openjdk@21

# Or download from Adoptium/Oracle and install manually
```

##### Linux (Ubuntu/Debian):
```bash
# OpenJDK installation
sudo apt update
sudo apt install openjdk-21-jdk

# Verify installation
java -version
javac -version
```

#### Step 2: Install Apache Ant (Optional but Recommended)
**Note**: Most JDK installations include Ant, but you can install it separately if needed.

##### Windows:
1. Download from [Apache Ant](https://ant.apache.org/bindownload.cgi)
2. Extract to `C:\apache-ant`
3. Add to PATH: `C:\apache-ant\bin`

##### macOS:
```bash
brew install ant
```

##### Linux:
```bash
sudo apt install ant
```

#### Step 3: Install Git
**For version control and cloning the repository**

##### Windows:
Download from [git-scm.com](https://git-scm.com/download/win)

##### macOS:
```bash
brew install git
```

##### Linux:
```bash
sudo apt install git
```

### Repository Setup

#### Step 1: Clone the Repository
```bash
git clone https://github.com/titanioco/Proyectos-Backup.git
cd Proyectos-Backup
```

#### Step 2: Verify Project Structure
Ensure the following structure exists:
```
Proyectos-Backup/
├── src/                          # Source code ✓
├── lib/                          # JAR dependencies ✓
├── build.xml                     # Ant build file ✓
├── oauth.properties.template     # OAuth template ✓
├── ai.properties.template        # AI template ✓
├── .gitignore                    # Git ignore rules ✓
└── README.md                     # Documentation ✓
```

#### Step 3: Test Basic Setup
```bash
# Test compilation
ant clean compile

# Test run (demo mode)
ant run
```

## 🔧 Recovery Procedures

### Complete Fresh Setup

#### Scenario: Starting from scratch on a new machine

1. **Install Prerequisites** (see above)
2. **Clone Repository**:
   ```bash
   git clone https://github.com/titanioco/Proyectos-Backup.git
   cd Proyectos-Backup
   ```
3. **Verify Dependencies**:
   ```bash
   # Check lib/ directory contains JAR files
   ls lib/
   # Should show: sqlite-jdbc, google-oauth-client, miglayout, etc.
   ```
4. **Test Basic Functionality**:
   ```bash
   ant clean compile run
   ```

### Partial Recovery

#### Scenario: Project exists but has issues

1. **Clean Build**:
   ```bash
   ant clean
   rm -rf build/
   ant compile run
   ```

2. **Reset Configuration**:
   ```bash
   # Remove any existing config files
   rm -f oauth.properties ai.properties university.db
   
   # Copy templates if needed
   cp oauth.properties.template oauth.properties
   cp ai.properties.template ai.properties
   ```

3. **Dependency Check**:
   ```bash
   # Verify all JARs are present
   ls -la lib/
   
   # Re-download if lib/ is missing (not recommended, use git clone instead)
   ```

### Configuration Recovery

#### OAuth Configuration Lost
1. **Restore from Template**:
   ```bash
   cp oauth.properties.template oauth.properties
   ```
2. **Update with Credentials** (if available):
   ```properties
   google.oauth.client.id=YOUR_CLIENT_ID
   google.oauth.client.secret=YOUR_CLIENT_SECRET
   google.oauth.redirect.uri=http://127.0.0.1:8080/oauth2callback
   ```

#### AI Configuration Lost
1. **Restore from Template**:
   ```bash
   cp ai.properties.template ai.properties
   ```
2. **Update with API Keys** (if available):
   ```properties
   openai.api.key=sk-your-openai-key
   github.api.token=github_pat_your-token
   ```

## ⚙️ Configuration Management

### Creating Configuration Files

#### For Demo/Testing (No External Services)
```bash
# Application will work without these files
# Just ensure templates exist for reference
ls oauth.properties.template ai.properties.template
```

#### For Full Functionality
1. **Create OAuth Configuration**:
   ```bash
   cp oauth.properties.template oauth.properties
   # Edit oauth.properties with actual credentials
   ```

2. **Create AI Configuration**:
   ```bash
   cp ai.properties.template ai.properties
   # Edit ai.properties with actual API keys
   ```

### Configuration Validation

#### Check Configuration Files
```bash
# Verify templates exist
ls *.template

# Check for actual config files (optional)
ls oauth.properties ai.properties 2>/dev/null || echo "Config files not found (demo mode)"
```

#### Test Configuration
```bash
# Run application and check console output
ant clean compile run
# Look for messages about OAuth/AI initialization
```

## 🗄️ Database Recovery

### Database Reset
```bash
# Remove existing database
rm -f university.db

# Restart application (will create new database)
ant clean compile run
```

### Database Backup/Restore
```bash
# Backup database (if needed)
cp university.db university.db.backup

# Restore from backup
cp university.db.backup university.db
```

## 🔍 Troubleshooting Guide

### Common Issues and Solutions

#### Issue: "Java command not found"
**Solution**:
```bash
# Check Java installation
which java
java -version

# If not found, install JDK (see Prerequisites section)
```

#### Issue: "Cannot find main class com.raven.main.Main"
**Solution**:
```bash
# Ensure correct directory
pwd  # Should end with Proyectos-Backup

# Check project structure
ls src/com/raven/main/Main.java  # Should exist

# Clean and rebuild
ant clean compile run
```

#### Issue: "Build failed - dependencies not found"
**Solution**:
```bash
# Check lib directory
ls lib/  # Should contain multiple JAR files

# If empty, re-clone repository
cd ..
rm -rf Proyectos-Backup
git clone https://github.com/titanioco/Proyectos-Backup.git
```

#### Issue: "Permission denied" (Linux/macOS)
**Solution**:
```bash
# Make scripts executable
chmod +x run.sh

# Or run directly with Java
java -cp "build/classes:lib/*" com.raven.main.Main
```

#### Issue: OAuth/AI features not working
**Solution**:
```bash
# This is expected in demo mode
# To enable: create and configure oauth.properties and ai.properties
# Or simply use the application without these features
```

### Diagnostic Commands

#### System Diagnostics
```bash
# Java version and location
java -version
which java

# Ant version (if installed)
ant -version

# Project structure check
ls -la  # Should show src/, lib/, build.xml

# Dependencies check
ls lib/*.jar | wc -l  # Should show multiple JAR files
```

#### Application Diagnostics
```bash
# Compile test
ant compile

# Run test (watch console output for errors)
ant run

# Clean build test
ant clean compile run
```

## 🖥️ Platform-Specific Instructions

### Windows Specific

#### PowerShell Setup
```powershell
# Navigate to project
cd "C:\path\to\Proyectos-Backup"

# Build and run
ant clean compile run

# Alternative: Use batch file
.\run.bat
```

#### Common Windows Issues
- **Path Issues**: Use quotes around paths with spaces
- **Permission Issues**: Run PowerShell as Administrator if needed
- **Ant Not Found**: Add Ant bin directory to PATH

### macOS Specific

#### Terminal Setup
```bash
# Navigate to project
cd /path/to/Proyectos-Backup

# Make scripts executable
chmod +x run.sh

# Build and run
ant clean compile run

# Alternative: Use shell script
./run.sh
```

#### Common macOS Issues
- **Java Not Found**: Install JDK using Homebrew or direct download
- **Permission Issues**: Use `sudo` if needed for installations
- **Gatekeeper**: Allow Java applications in Security & Privacy settings

### Linux Specific

#### Ubuntu/Debian Setup
```bash
# Install prerequisites
sudo apt update
sudo apt install openjdk-21-jdk ant git

# Clone and run
git clone https://github.com/titanioco/Proyectos-Backup.git
cd Proyectos-Backup
ant clean compile run
```

#### Common Linux Issues
- **Package Manager**: Use appropriate package manager (apt, yum, pacman)
- **Permissions**: Ensure user has write permissions in project directory
- **Display**: For GUI applications, ensure X11 forwarding if using SSH

## 🚀 Quick Recovery Checklist

### Full System Recovery (New Machine)
- [ ] Install Java JDK (8 or higher)
- [ ] Install Git
- [ ] Clone repository
- [ ] Test basic compilation: `ant compile`
- [ ] Test basic run: `ant run`
- [ ] Verify application launches

### Partial Recovery (Existing Setup Issues)
- [ ] Navigate to project directory
- [ ] Clean build: `ant clean`
- [ ] Remove build artifacts: `rm -rf build/`
- [ ] Recompile: `ant compile`
- [ ] Test run: `ant run`

### Configuration Recovery
- [ ] Check template files exist: `ls *.template`
- [ ] Copy templates if needed: `cp *.template oauth.properties`
- [ ] Update with actual credentials (if available)
- [ ] Test application functionality

## 📞 Support Resources

### Documentation
- [Main README.md](README.md) - Primary documentation
- [Project Structure](#) - Code organization
- [Configuration Templates](#) - oauth.properties.template, ai.properties.template

### Common Commands Reference
```bash
# Essential commands for recovery
ant clean compile run    # Full rebuild and run
ant compile             # Compile only
ant run                # Run only (after compile)
java -version          # Check Java installation
ls lib/               # Check dependencies
```

### Emergency Recovery
If all else fails:
1. Delete the entire project directory
2. Re-clone from GitHub: `git clone https://github.com/titanioco/Proyectos-Backup.git`
3. Follow the [New Machine Setup](#new-machine-setup) procedure

---

**💡 Pro Tip**: Always keep the template files intact - they serve as your configuration backup and reference for what credentials are needed.
