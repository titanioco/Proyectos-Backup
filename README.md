# 🎓 University Project Management System

A modern Java-based desktop application for managing university projects with Google OAuth integration, AI assistant functionality, and interactive data structures visualization.

## 📋 Table of Contents
- [What is this?](#what-is-this)
- [Features](#features)
- [Demo Instructions](#demo-instructions) 
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)
- [Security Notes](#security-notes)
- [Contributing](#contributing)

## 🎯 What is this?

This is a **University Project Management System** built in Java using Swing for the user interface. It's designed to help students and faculty manage academic projects with modern features including:

- 🔐 **Secure Authentication** with Google OAuth integration
- 🎓 **Learning Dashboard** with interactive data structures visualization (BST, Hash Tables, Graphs, Heaps)
- 🤖 **AI Assistant** with text processing and chat capabilities  
- 📧 **Email Verification** for new user registration
- 🎨 **Modern UI** with custom Swing components and smooth animations
- 💾 **Database Storage** using SQLite
- � **Export Functionality** for visualizations and reports

## ✨ Features

### 🔐 Authentication & Security
- **Traditional Login**: Username/password with email verification
- **Google OAuth 2.0**: Secure sign-in with Google accounts (requires setup)
- **Password Strength**: Real-time password strength validation
- **Email Verification**: SMTP-based email verification system

### 🎓 Learning Dashboard
- **Binary Search Tree (BST)**: Interactive tree visualization with insertion, deletion, and traversal
- **Graph Algorithms**: Dijkstra's shortest path with step-by-step animation
- **Hash Tables**: Visual hash table operations with collision resolution
- **Binary Heap**: Min/Max heap operations with priority queue visualization
- **Export Features**: HTML export for all visualizations

### 🤖 AI Assistant
- **Chat Interface**: Natural language conversation interface
- **Document Processing**: Framework ready for text analysis
- **Extensible Architecture**: Ready for integration with OpenAI, GitHub Copilot, or similar services

### 🎨 User Interface
- **Modern Design**: Custom-styled Swing components with professional appearance
- **Responsive Layout**: MigLayout-based adaptive interface
- **Smooth Animations**: TimingFramework-powered transitions
- **Main Selection Menu**: Centralized navigation hub
- **Multi-window Architecture**: Seamless navigation between modules

## 🚀 Demo Instructions

### Quick Demo Setup (No Configuration Required)
1. Clone this repository
2. Run the application using the quick start commands below
3. Use the **demo mode** to explore features without OAuth setup
4. Click "Learning Dashboard" to explore interactive data structures
5. Try the AI Assistant interface (chat functionality available)

### For Full Functionality (OAuth & AI)
Follow the [Configuration](#configuration) section to set up Google OAuth and AI services.

## 🛠️ Prerequisites

### Required Software
- **Java 8 or higher** (JDK recommended) - *Tested with Java 21*
- **Apache Ant** (for building) - *Usually included with JDK*
- **Git** (for version control)

### Verification Commands
```bash
# Check Java installation
java -version
javac -version

# Check Ant installation (optional, included in most JDK installations)
ant -version
```

**Installation Help:**
- **Java**: Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- **Ant**: Usually included with JDK, or download from [Apache Ant](https://ant.apache.org/)

## 📦 Installation & Setup

### Step 1: Clone the Repository
```bash
git clone https://github.com/titanioco/Proyectos-Backup.git
cd Proyectos-Backup
```

### Step 2: Quick Start (Demo Mode)

#### Windows Users
```powershell
# Navigate to project directory
cd "path\to\Proyectos-Backup"

# Build and run
ant clean compile run
```

#### Mac/Linux Users
```bash
# Navigate to project directory
cd /path/to/Proyectos-Backup

# Build and run
ant clean compile run

# Alternative: Use the provided script
chmod +x run.sh
./run.sh
```

### Step 3: Verify Installation
If successful, you should see:
1. The main login/registration window
2. Option to register a new account or use demo mode
3. Access to the main selection menu after authentication

## ⚙️ Configuration

### 🔐 Google OAuth Setup (Optional)

> **Note**: OAuth setup is optional for demo purposes. The application works without it, but OAuth features will be disabled.

#### Step 1: Create Configuration Files
```bash
# Copy template files
cp oauth.properties.template oauth.properties
cp ai.properties.template ai.properties
```

#### Step 2: Google Cloud Console Setup
1. Visit [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the **Google Identity API**
4. Create OAuth 2.0 credentials (Desktop Application)
5. Download the client configuration

#### Step 3: Update oauth.properties
```properties
# Replace with your actual credentials
google.oauth.client.id=YOUR_CLIENT_ID_HERE
google.oauth.client.secret=YOUR_CLIENT_SECRET_HERE
google.oauth.redirect.uri=http://127.0.0.1:8080/oauth2callback
```

### 🤖 AI Services Setup (Optional)

#### Step 1: Choose Your AI Provider
- **OpenAI**: Get API key from [OpenAI Platform](https://platform.openai.com/api-keys)
- **GitHub Models**: Get token from [GitHub Settings](https://github.com/settings/tokens)

#### Step 2: Update ai.properties
```properties
# For OpenAI
openai.api.key=sk-your-actual-openai-api-key-here
openai.enabled=true

# For GitHub Models  
github.api.token=github_pat_your-actual-token-here
github.enabled=true
```
2. Click "Create Credentials" → "OAuth 2.0 Client IDs"
3. Select "Desktop application"
4. Name it "University Project Manager"
5. Copy the **Client ID** and **Client Secret**

#### Step 3: Configure Application
Edit `oauth.properties` and replace the placeholder values:
```properties
google.oauth.client.id=YOUR_ACTUAL_CLIENT_ID_HERE
google.oauth.client.secret=YOUR_ACTUAL_CLIENT_SECRET_HERE
google.oauth.redirect.uri=http://127.0.0.1:8080/oauth2callback
google.oauth.scope=openid email profile
```

### 📧 Email Configuration (Optional)

For email verification functionality, set these system properties when running:
```bash
java -Dmail.username=your-email@gmail.com \
     -Dmail.password=your-app-password \
     -Dmail.from=your-email@gmail.com \
     -cp "bin;lib/*" com.raven.main.Main
```

**Note**: Use Gmail App Passwords, not your regular password.

## 🚀 Running the Application

## 🏃 Running the Application

### Method 1: Using Ant (Recommended)
```bash
# Clean build and run
ant clean compile run

# Just compile
ant compile

# Just run (after compiling)
ant run
```

### Method 2: Using Java Directly
```bash
# Compile
javac -cp "lib/*:src" -d build/classes src/com/raven/main/Main.java

# Run
java -cp "build/classes:lib/*" com.raven.main.Main
```

### Method 3: Using Provided Scripts
```bash
# Windows
run.bat

# Mac/Linux
./run.sh
```

## 📁 Project Structure

```
Proyectos-Backup/
├── src/                          # Source code
│   └── com/raven/               
│       ├── main/                 # Main application entry
│       ├── ui/                   # User interface frames
│       ├── component/            # Custom UI components
│       ├── model/                # Data models
│       ├── database/             # Database utilities
│       ├── ds/                   # Data structures modules
│       │   ├── core/            # Animation engine
│       │   └── modules/         # BST, Graph, Hash, Heap
│       └── swing/                # Custom Swing components
├── lib/                          # Dependencies (JAR files)
├── nbproject/                    # NetBeans project files
├── build.xml                     # Ant build configuration
├── oauth.properties.template     # OAuth configuration template
├── ai.properties.template        # AI services configuration template
├── .gitignore                    # Git ignore rules
├── run.bat                       # Windows run script
├── run.sh                        # Mac/Linux run script
└── README.md                     # This file
```

## 🔧 Troubleshooting

### Common Issues

#### "Java command not found"
```bash
# Check if Java is installed
java -version

# If not installed, download from:
# https://adoptium.net/
```

#### "Cannot find main class"
```bash
# Ensure you're in the correct directory
ls -la  # Should show src/, lib/, build.xml

# Clean and rebuild
ant clean compile run
```

#### "OAuth Configuration Missing"
- The application runs in demo mode without OAuth
- For full OAuth functionality, follow the [Google OAuth Setup](#google-oauth-setup-optional) section

#### "Build Failed" 
```bash
# Check Java and Ant versions
java -version
ant -version

# Ensure all dependencies are present
ls lib/  # Should show multiple JAR files

# Try manual compilation
javac -version
```

#### Database Issues
```bash
# Remove existing database (will be recreated)
rm university.db

# Restart application
ant clean compile run
```

### Getting Help
1. Check the console output for error messages
2. Ensure all prerequisites are installed
3. Verify you're in the correct directory
4. Try a clean rebuild: `ant clean compile run`

## 🔒 Security Notes

### Files That Are Safe to Share
✅ **Template files** (`.template` extensions)
✅ **Source code** (in `src/` directory)
✅ **Build scripts** (`build.xml`, `run.bat`, `run.sh`)
✅ **Documentation** (`README.md`, guides)

### Files That Should NEVER Be Shared
❌ **`oauth.properties`** - Contains actual OAuth credentials
❌ **`ai.properties`** - Contains actual API keys
❌ **`university.db`** - May contain user data
❌ **`build/`** directory - Contains compiled classes
❌ **`nbproject/private/`** - Contains local configurations

### Best Practices
1. Never commit actual credentials to version control
2. Use template files for sharing configurations
3. Keep API keys secure and rotate them regularly
4. Use environment variables for production deployments

## 🤝 Contributing

### Development Setup
1. Fork this repository
2. Clone your fork locally
3. Create a feature branch
4. Make your changes
5. Test thoroughly
6. Submit a pull request

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Maintain consistent indentation (4 spaces)

### Testing
- Test all UI interactions
- Verify OAuth flow (if configured)
- Test data structure visualizations
- Ensure cross-platform compatibility

## 📄 License

This project is developed for educational purposes. Please respect the included libraries' licenses and terms of use for any external services (Google OAuth, AI APIs).

---

**📞 Need Help?** 
- Check the [Troubleshooting](#troubleshooting) section
- Review the console output for error messages  
- Ensure all [Prerequisites](#prerequisites) are installed

**🎯 Ready to Start?** 
Run `ant clean compile run` and explore the interactive data structures!
├── database/              # Database utilities
├── ds/                    # Data structure implementations
├── swing/                 # Custom Swing components
└── util/                  # Utility classes
```

## 📚 Libraries Used

### Core Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| SQLite JDBC | 3.43.2.0 | Database connectivity |
| Google OAuth Client | 1.34.1 | Google authentication |
| MigLayout | 5.3 | Advanced UI layouts |
| TimingFramework | 1.0 | Smooth animations |
| JavaMail | Latest | Email functionality |
| Gson | 2.8.9 | JSON processing |

### UI Enhancement
- **Custom Swing Components** - Modern UI elements
- **Professional Styling** - Enhanced visual appearance
- **Responsive Design** - Adaptive layouts

### Server Components
- **Jetty Server** (9.4.44) - OAuth callback handling
- **SLF4J** (1.7.36) - Logging framework

## 🔧 Troubleshooting

### Common Issues & Solutions

#### ❌ "Java not found"
**Symptoms**: `'java' is not recognized as an internal or external command`
**Solution**:
1. Install Java JDK from [Adoptium](https://adoptium.net/)
2. Add Java to system PATH
3. Restart command prompt/terminal
4. Verify: `java -version`

#### ❌ Compilation Errors
**Symptoms**: `java.lang.ClassNotFoundException` or compilation failures
**Solution**:
1. Ensure all JAR files are in `lib/` directory
2. Verify classpath includes all dependencies
3. Check for missing files: `ls lib/` or `dir lib\`
4. Clean and rebuild: `ant clean compile`

#### ❌ OAuth Not Working
**Symptoms**: "OAuth configuration error" or authentication failures
**Solution**:
1. Verify `oauth.properties` exists with correct credentials
2. Check Google Cloud Console project settings
3. Ensure redirect URI matches: `http://127.0.0.1:8080/oauth2callback`
4. Verify Google Identity API is enabled

#### ❌ Database Issues
**Symptoms**: SQLite errors or data persistence problems
**Solution**:
1. Check write permissions in project directory
2. Verify SQLite JDBC driver in classpath
3. Database file (`university.db`) is auto-created
4. Check for file locking issues

#### ❌ Email Verification Failed
**Symptoms**: "Email verification failed" or SMTP errors
**Solution**:
1. Use Gmail App Password (not regular password)
2. Enable 2-factor authentication on Gmail
3. Verify system properties are set correctly
4. Check firewall/network restrictions

### Debug Mode
Run with additional logging:
```bash
java -Djava.util.logging.config.file=logging.properties \
     -cp "bin;lib/*" com.raven.main.Main
```

### Performance Issues
If the application runs slowly:
1. Check available system memory
2. Close unnecessary applications
3. Update to latest Java version
4. Consider increasing heap size: `-Xmx512m`

## 🔒 Security Notes

### Protected Information
- ✅ OAuth credentials excluded from version control
- ✅ Email passwords use environment variables/system properties
- ✅ Database files not committed to repository
- ✅ Sensitive configuration templates provided

### Security Best Practices
1. **Never commit** real OAuth credentials to version control
2. **Use separate** Google Cloud projects for development/production
3. **Regularly update** dependencies for security patches
4. **Backup** database files regularly
5. **Use strong passwords** for email accounts

### File Protection
- `oauth.properties` - Contains sensitive OAuth credentials
- `university.db` - User data and project information
- Email passwords - Use app passwords, not account passwords

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Make changes and test thoroughly
4. Update documentation if needed
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Create Pull Request

### Coding Standards
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Include unit tests for new features
- Maintain consistent code formatting
- Update README.md for new features

## 📄 License

This project is developed for educational purposes. Please respect the original authors and contributors.

---

## 🎉 Success! You're Ready to Go!

### What You've Accomplished:
- ✅ **Built** the project successfully
- ✅ **Configured** the development environment
- ✅ **Set up** authentication system (OAuth ready)
- ✅ **Integrated** AI assistant framework
- ✅ **Prepared** for production deployment

### Next Steps:
1. **🚀 Run the application**: `ant run`
2. **👤 Create your account** using the registration system
3. **🔐 Test Google OAuth** (if configured)
4. **🤖 Explore AI assistant** features
5. **📊 Try data structure visualizations**
6. **🎨 Customize the UI** to your preferences

### Need Help?
- 📖 **Check troubleshooting** section above
- 🔍 **Look at console output** for error messages
- ⚙️ **Verify configuration** files are correct
- 💬 **Create an issue** on GitHub for bugs

**Happy coding!** 🚀 Your University Project Management System is ready to help you organize and manage your academic projects efficiently!

## ✨ Features

### 🔐 Authentication & Security
- **Traditional Login**: Username/password with email verification
- **Google OAuth**: Sign in with your Google account
- **Password Strength**: Real-time password strength checking
- **Email Verification**: Get a code via email to verify your account

### 🎨 User Interface
- **Modern Design**: Custom-styled Swing components
- **Responsive Layout**: Adapts to different window sizes
- **Smooth Animations**: Professional transitions and effects
- **Dark/Light Themes**: Customizable appearance

### 💾 Data Management
- **SQLite Database**: Local data storage
- **User Profiles**: Store user information securely
- **Project Organization**: Manage academic projects
- **Backup System**: Automatic data backup

## 🛠️ Prerequisites

Before you start, make sure you have:

### Required Software
- **Java 8 or higher** (JDK, not just JRE)
- **Git** (to download the project)
- **Text Editor** (like VS Code, IntelliJ IDEA, or Notepad++)

### How to Check if Java is Installed
Open your command prompt/terminal and type:
```bash
java -version
```
If you see something like "java version 1.8.0_xxx" or higher, you're good!

If not, download Java from: https://adoptium.net/

## 📦 Installation

### Step 1: Download the Project
```bash
# Clone the repository
git clone [your-repository-url]
cd Proyectos
```

### Step 2: Verify Project Structure
You should see these folders and files:
```
Proyectos/
├── src/                    # Source code
├── lib/                    # Libraries (JAR files)
├── build.xml              # Build configuration
├── .gitignore             # Git ignore rules
└── README.md              # This file!
```

### Step 3: Check Dependencies
The project comes with all required libraries in the `lib/` folder:
- SQLite JDBC driver
- Google OAuth libraries
- UI layout libraries
- Email libraries

## ⚙️ Configuration

### 🔐 Setting Up Google OAuth (Optional but Recommended)

**Why Google OAuth?**
It lets users sign in with their Google account instead of creating a new password. It's more secure and convenient!

#### Step 1: Create Google Cloud Project
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the "Google+ API" or "Google Identity API"

#### Step 2: Create OAuth Credentials
1. Go to "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "OAuth 2.0 Client IDs"
3. Choose "Desktop application"
4. Give it a name like "University Project Manager"
5. Copy the **Client ID** and **Client Secret**

#### Step 3: Configure the Application
1. Copy `oauth.properties.template` to `oauth.properties`
2. Edit `oauth.properties` and add your credentials:
```properties
google.oauth.client.id=YOUR_CLIENT_ID_HERE
google.oauth.client.secret=YOUR_CLIENT_SECRET_HERE
```

### 📧 Setting Up Email (Optional)

**Why Email Setup?**
The app sends verification codes to new users' email addresses to make sure they're real people!

#### Step 1: Get Gmail App Password
1. Go to your Google Account settings
2. Enable 2-Factor Authentication
3. Generate an "App Password" for this application

#### Step 2: Configure Email Settings
Run the application with these system properties:
```bash
java -Dmail.username=your-email@gmail.com -Dmail.password=your-app-password -Dmail.from=your-email@gmail.com -jar your-app.jar
```

## 🚀 Running the Application

### Method 1: Using the Build Script (Recommended)
```bash
# On Windows
ant run

# On Mac/Linux
./run-gui.sh
```

### Method 2: Manual Compilation and Run
```bash
# Compile the project
javac -cp "lib/*" -d bin src/com/raven/main/*.java src/com/raven/**/*.java

# Run the application
java -cp "bin;lib/*" com.raven.main.Main
```

### Method 3: Using an IDE
1. Open the project in IntelliJ IDEA, Eclipse, or VS Code
2. Set up the classpath to include all JAR files in the `lib/` folder
3. Run the `Main.java` file

## 📁 Project Structure

```
src/com/raven/
├── main/           # Main application entry point
├── ui/             # User interface components
├── model/          # Data models (User, Project, etc.)
├── service/        # Business logic services
├── database/       # Database connection and management
├── config/         # Configuration and OAuth setup
├── util/           # Utility classes
├── component/      # Custom UI components
├── swing/          # Custom Swing components
├── connection/     # Database connections
├── ds/             # Data structures
├── icon/           # Application icons
└── test/           # Test classes
```

## 📚 Libraries Used

### Core Libraries
- **SQLite JDBC** (`sqlite-jdbc-3.43.2.0.jar`) - Database connectivity
- **Google OAuth Client** (`google-oauth-client-*.jar`) - Google authentication
- **JavaMail** (`javax.mail.jar`) - Email functionality

### UI Libraries
- **MigLayout** (`miglayout-*.jar`) - Advanced layout management
- **Timing Framework** (`TimingFramework-*.jar`) - Smooth animations
- **Custom Swing Components** - Modern UI elements

### Utility Libraries
- **Gson** (`gson-*.jar`) - JSON processing
- **Guava** (`guava-*.jar`) - Google's utility library
- **SLF4J** (`slf4j-*.jar`) - Logging framework

### Server Libraries (for OAuth)
- **Jetty Server** (`jetty-*.jar`) - Local OAuth callback server
- **OpenCensus** (`opencensus-*.jar`) - Metrics and tracing

## 🔧 Troubleshooting

### Common Issues

#### "Java not found" Error
**Problem**: `'java' is not recognized as an internal or external command`
**Solution**: 
1. Install Java JDK (not just JRE)
2. Add Java to your system PATH
3. Restart your command prompt

#### "Class not found" Errors
**Problem**: `java.lang.ClassNotFoundException`
**Solution**:
1. Make sure all JAR files are in the `lib/` folder
2. Check that the classpath includes all JAR files
3. Verify the compilation was successful

#### Google OAuth Not Working
**Problem**: "OAuth configuration error"
**Solution**:
1. Check that `oauth.properties` exists and has correct credentials
2. Verify your Google Cloud project has OAuth enabled
3. Make sure the redirect URI is set to `http://127.0.0.1:8080/oauth2callback`

#### Email Not Sending
**Problem**: "Email verification failed"
**Solution**:
1. Check your Gmail app password is correct
2. Make sure 2-factor authentication is enabled
3. Verify the email properties are set correctly

#### Database Errors
**Problem**: "SQLite database error"
**Solution**:
1. Check that `university.db` file is writable
2. Verify SQLite JDBC driver is in the classpath
3. The app will automatically create the database if it doesn't exist

### Getting Help
If you're still having trouble:
1. Check the console output for error messages
2. Look at the log files in the project directory
3. Make sure all prerequisites are installed
4. Try running with `-Djava.util.logging.config.file=logging.properties` for more detailed logs

## 🔒 Security Notes

### What's Protected
- ✅ **OAuth credentials** are in `.gitignore`
- ✅ **Email passwords** are not hardcoded
- ✅ **Database files** are excluded from version control
- ✅ **Sensitive configuration** files are protected

### What You Need to Do
1. **Never commit** `oauth.properties` with real credentials
2. **Use environment variables** or system properties for email passwords
3. **Keep your Google OAuth credentials** secure
4. **Regularly update** dependencies for security patches

### Safe Development Practices
- Use `oauth.properties.template` as a starting point
- Test with dummy credentials first
- Use separate Google Cloud projects for development and production
- Regularly backup your database files

## 🤝 Contributing

Want to help improve this project?

1. **Fork** the repository
2. **Create** a feature branch
3. **Make** your changes
4. **Test** thoroughly
5. **Submit** a pull request

### Development Guidelines
- Follow Java naming conventions
- Add comments for complex logic
- Test your changes before submitting
- Update documentation if needed

## 📄 License

This project is for educational purposes. Please respect the original authors and contributors.

---

## 🎉 You're Ready!

Congratulations! You now have a fully functional University Project Management System. 

**Next Steps:**
1. Run the application
2. Create your first account
3. Explore the features
4. Customize it for your needs

**Need Help?**
- Check the troubleshooting section above
- Look at the console output for error messages
- Make sure all configuration files are set up correctly

Happy coding! 🚀 