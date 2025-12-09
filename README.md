# 🎓 Academic Project Management & Data Structures Learning Platform

![Java](https://img.shields.io/badge/Java-11+-orange.svg)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-blue.svg)
![Apache Ant](https://img.shields.io/badge/Build-Apache%20Ant-red.svg)
![License](https://img.shields.io/badge/License-Educational-green.svg)

A comprehensive Java Swing application combining **user authentication**, **data structure visualization**, **accountability management**, and **AI-powered assistance** for academic and business purposes.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Modules](#modules)
- [Build & Run](#build--run)
- [Documentation](#documentation)
- [Contributing](#contributing)

---

## 🎯 Overview

This application serves as a multi-purpose platform designed for:

1. **Educational purposes** - Interactive visualization of data structures and algorithms
2. **Business accountability** - Comprehensive financial management, invoicing, and compliance tools
3. **User management** - Secure authentication with Google OAuth integration
4. **AI assistance** - Integration with OpenAI and GitHub Models for intelligent help

The application features a modern, animated UI built with Java Swing and follows best practices for educational software development.

---

## ✨ Features

### 🔐 Authentication & User Management
- ✅ **Traditional login/register** with email and password
- ✅ **Google OAuth 2.0 integration** for seamless sign-in
- ✅ **Email verification** system with verification codes
- ✅ **Password recovery** functionality
- ✅ **Session management** with secure user tracking
- ✅ **Multi-database support** (SQLite, in-memory, Firebase)

### 📊 Data Structure Visualizations
Interactive visualizations with step-by-step animations for:

- **Binary Search Tree (BST)** - Insert, delete, search, traversals (In-order, Pre-order, Post-order)
- **AVL Tree** - Self-balancing operations with rotation visualizations
- **Binary Heap** - Min/Max heap operations with heapify animations
- **Hash Table** - Collision handling (chaining, open addressing)
- **Graph Algorithms** - Dijkstra's shortest path, Bellman-Ford algorithm
- **Dynamic Arrays** - Resizing and capacity management
- **Heapsort** - Step-by-step sorting visualization

#### Visualization Features:
- 🎨 **Enhanced contrast** with professional color schemes
- 📝 **Step-by-step explanations** for each operation
- 📊 **Operation history tracking**
- 📄 **HTML/PDF export** for analysis reports
- 🎬 **Smooth animations** with timing framework
- 📈 **Complexity analysis** display

### 💼 Business Accountability Suite

A complete business management system including:

#### Billing & Invoicing
- ✅ Create, edit, and manage invoices
- ✅ Customer management system
- ✅ Invoice status tracking (draft, pending, paid, overdue)
- ✅ Automated calculations (subtotal, tax, total)
- ✅ **Firebase cloud backup** integration
- ✅ Invoice viewer with print capabilities
- ✅ Statistics dashboard

#### Financial Accounting
- General ledger management
- Account tracking
- Financial reports generation

#### Compliance & Audit
- Audit trail logging
- Compliance monitoring
- Tax management panel

#### Reporting
- Customizable financial reports
- Export capabilities (CSV, PDF)
- Visual analytics

### 🤖 AI Assistant

- **OpenAI GPT integration** for intelligent assistance
- **GitHub Models API** support as fallback
- **Multi-modal support**: Text, audio, and video processing
- Context-aware responses based on user profile
- Configurable system prompts

---

## 🏗 Architecture

The application follows a modular architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                     Main Window                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │          Main Selection Frame                     │  │
│  │  ┌─────────────┬──────────────┬───────────────┐   │  │
│  │  │ Data        │ Accountability│ AI           │   │  │
│  │  │ Structures  │ Management   │ Assistant    │   │  │
│  │  └─────────────┴──────────────┴───────────────┘   │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
           │                    │                │
           ▼                    ▼                ▼
    ┌─────────────┐    ┌─────────────┐   ┌─────────────┐
    │ DS Modules  │    │ Accounting  │   │ AI Service  │
    │             │    │ Panels      │   │             │
    │ - BST       │    │ - Billing   │   │ - OpenAI    │
    │ - AVL       │    │ - Financial │   │ - GitHub    │
    │ - Heap      │    │ - Reporting │   │   Models    │
    │ - Graph     │    │ - Audit     │   │             │
    └─────────────┘    └─────────────┘   └─────────────┘
           │                    │                │
           └────────────────────┴────────────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │ Backend Services    │
                    │                     │
                    │ - Authentication    │
                    │ - Database (SQLite) │
                    │ - Firebase          │
                    │ - Email Service     │
                    └─────────────────────┘
```

### Design Patterns Used
- **Singleton Pattern** - Service management (AIService, SessionManager)
- **Factory Pattern** - Database service creation
- **Observer Pattern** - Animation state management
- **Strategy Pattern** - Multiple database implementations
- **MVC Pattern** - Separation of UI and business logic

---

## 🛠 Technology Stack

### Core Technologies
- **Java 11+** - Primary programming language
- **Java Swing** - GUI framework
- **Apache Ant** - Build automation

### Libraries & Dependencies

#### UI & Animation
- `miglayout-core-5.3.jar` - Advanced layout manager
- `miglayout-swing-5.3.jar` - Swing integration for MigLayout
- `timingframework-1.0.jar` - Animation framework

#### Database
- `sqlite-jdbc-3.43.2.0.jar` - SQLite JDBC driver
- Local file-based database storage

#### Authentication & OAuth
- `google-oauth-client-1.34.1.jar` - Google OAuth client
- `google-oauth-client-jetty-1.34.1.jar` - Jetty integration
- `google-oauth-client-java6-1.34.1.jar` - Java 6 compatibility
- `google-http-client-1.42.3.jar` - HTTP client
- `google-http-client-gson-1.42.3.jar` - JSON parsing
- `google-api-client-1.32.1.jar` - Google API client
- `jetty-server-9.4.44.v20210927.jar` - OAuth callback server

#### Firebase Integration
- `firebase-admin-9.2.0.jar` - Firebase Admin SDK
- `google-cloud-firestore-3.7.9.jar` - Firestore database
- `google-auth-library-oauth2-http-1.16.0.jar` - OAuth2 auth
- `google-auth-library-credentials-1.16.0.jar` - Credentials management
- `google-cloud-core-2.22.0.jar` - Google Cloud core
- `gax-2.32.0.jar` - Google API extensions
- `grpc-context-1.50.2.jar` - gRPC context
- `protobuf-java-3.24.0.jar` - Protocol buffers
- `opencensus-api-0.28.3.jar` - Observability framework

#### Utilities
- `gson-2.8.9.jar` - JSON serialization
- `guava-31.1-jre.jar` - Google core libraries
- `slf4j-api-2.0.9.jar` - Logging facade
- `slf4j-simple-2.0.9.jar` - Simple logging implementation
- `threetenbp-1.6.8.jar` - Date/time library

---

## 📁 Project Structure

```
Proyectos/
├── src/com/raven/
│   ├── main/
│   │   └── Main.java                    # Main entry point
│   ├── component/                       # Reusable UI components
│   │   ├── Message.java
│   │   ├── PanelCover.java
│   │   ├── PanelLoginAndRegister.java
│   │   ├── PanelVerifyCode.java
│   │   └── PanelLoading.java
│   ├── ui/                              # User interface frames
│   │   ├── MainSelectionFrame.java      # Main dashboard selector
│   │   ├── DashboardFrame.java          # Data structures dashboard
│   │   ├── AIAssistantFrame.java        # AI chat interface
│   │   └── AdminPanel.java
│   ├── ds/                              # Data structures module
│   │   ├── core/
│   │   │   ├── AnimationEngine.java
│   │   │   ├── PDFExporter.java
│   │   │   └── PDFDocumentGenerator.java
│   │   └── modules/
│   │       ├── bst/                     # Binary Search Tree
│   │       ├── avl/                     # AVL Tree
│   │       ├── heap/                    # Binary Heap
│   │       ├── hashtable/               # Hash Table
│   │       ├── graph/                   # Graph algorithms
│   │       ├── dynamicarray/            # Dynamic Array
│   │       └── heapsort/                # Heapsort algorithm
│   ├── accountability/                  # Business management module
│   │   ├── ui/
│   │   │   ├── AccountabilityManagementFrame.java
│   │   │   └── components/
│   │   │       ├── BillingAndInvoicingPanel.java
│   │   │       ├── FinancialAccountingPanel.java
│   │   │       ├── CompliancePanel.java
│   │   │       ├── ReportingPanel.java
│   │   │       ├── TaxManagementPanel.java
│   │   │       └── AuditTrailPanel.java
│   │   ├── model/                       # Business entities
│   │   ├── service/                     # Business logic
│   │   ├── database/                    # Data access layer
│   │   └── util/                        # Utilities
│   ├── service/                         # Backend services
│   │   ├── GoogleAuthService.java       # OAuth implementation
│   │   ├── FirebaseService.java         # Firebase integration
│   │   ├── AIService.java               # AI API integration
│   │   ├── AuthenticationManager.java
│   │   ├── SessionManager.java
│   │   ├── ServiceMail.java             # Email service
│   │   └── OAuth2CallbackHandler.java
│   ├── config/                          # Configuration management
│   │   ├── OAuthConfig.java
│   │   ├── FirebaseConfig.java
│   │   ├── AIConfig.java
│   │   └── OAuthVerifier.java
│   ├── database/                        # Database layer
│   │   ├── DBManager.java
│   │   ├── LocalDatabaseService.java
│   │   ├── MemoryDatabaseService.java
│   │   └── SimpleUserDatabase.java
│   ├── model/                           # Data models
│   │   ├── User.java
│   │   ├── UserDAO.java
│   │   ├── ModelUser.java
│   │   └── ModelMessage.java
│   └── swing/                           # Custom Swing components
│       ├── Button.java
│       ├── ButtonOutLine.java
│       ├── MyTextField.java
│       └── MyPasswordField.java
├── lib/                                 # External libraries
├── bin/                                 # Compiled classes
├── docs/                                # Documentation
│   ├── BST_Documentation.html
│   └── Graph_Documentation.html
├── exported/                            # Exported analysis reports
├── build.xml                            # Ant build script
├── manifest.mf                          # JAR manifest
└── oauth.properties.template            # OAuth configuration template
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK) 11 or higher**
- **Apache Ant** (for building)
- **IDE** (NetBeans recommended, but any Java IDE works)
- **Google Cloud Console** account (for OAuth)
- **OpenAI API key** (optional, for AI features)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/titanioco/Proyectos-Backup.git
   cd Proyectos-Backup
   ```

2. **Configure OAuth credentials**
   ```bash
   # Copy the template
   cp oauth.properties.template oauth.properties
   
   # Edit oauth.properties and add your credentials
   # See OAUTH_SETUP_GUIDE.md for detailed instructions
   ```

3. **Configure AI services** (optional)
   ```bash
   # Create ai.properties in src/com/raven/
   # Add your OpenAI API key
   openai.api.key=YOUR_API_KEY_HERE
   ```

4. **Configure Firebase** (optional)
   ```bash
   # Download service account JSON from Firebase Console
   # Place it in the project root directory
   # See FIREBASE_SETUP.md for details
   ```

5. **Build the project**
   ```bash
   ant clean compile
   ```

6. **Run the application**
   ```bash
   ant run
   ```

---

## ⚙ Configuration

### OAuth Configuration

The application uses Google OAuth 2.0 for authentication. Setup requires:

1. Create a project in [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google OAuth 2.0 API
3. Create OAuth 2.0 credentials (Desktop application)
4. Configure redirect URIs:
   - `http://127.0.0.1:8080/oauth2callback`
   - `http://localhost:8080/oauth2callback`

**File:** `oauth.properties`
```properties
google.oauth.client.id=YOUR_CLIENT_ID
google.oauth.client.secret=YOUR_CLIENT_SECRET
google.oauth.redirect.uri=http://127.0.0.1:8080/oauth2callback
google.oauth.scope=openid email profile
```

📖 **See:** `OAUTH_SETUP_GUIDE.md` for detailed setup instructions

### Firebase Configuration

For cloud backup and synchronization:

**File:** `backup-contable-firebase-adminsdk-*.json`
```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "...",
  "private_key": "...",
  "client_email": "...",
  ...
}
```

📖 **See:** `FIREBASE_INTEGRATION_COMPLETE.md` for setup details

### AI Configuration

**File:** `src/com/raven/config/ai.properties`
```properties
# OpenAI Configuration (Primary)
openai.api.key=sk-...
openai.model=gpt-4
openai.max.tokens=2000

# GitHub Models (Fallback)
github.api.token=ghp_...
github.model=gpt-4o

# System Configuration
ai.system.prompt=You are a helpful AI assistant...
```

### Database Configuration

The application supports multiple database backends:

- **SQLite** (default) - Local file-based storage
- **In-Memory** - Temporary storage for testing
- **Firebase Firestore** - Cloud storage with sync

Database selection is automatic based on availability.

---

## 📚 Modules

### 1. Data Structures Module

Interactive learning tool for computer science students.

**Supported Data Structures:**

#### Binary Search Tree (BST)
- Insert nodes with visual feedback
- Delete nodes (leaf, one child, two children cases)
- Search with path highlighting
- Traversals (In-order, Pre-order, Post-order)
- Tree balancing analysis
- Export to HTML/PDF

#### AVL Tree
- Self-balancing insertions
- Rotation visualizations (LL, RR, LR, RL)
- Balance factor display
- Height tracking

#### Binary Heap
- Min/Max heap operations
- Heapify animations
- Priority queue demonstrations
- Parent-child relationship visualization

#### Hash Table
- Open addressing visualization
- Chaining collision resolution
- Load factor monitoring
- Rehashing animations

#### Graph Algorithms
- Dijkstra's shortest path
- Bellman-Ford algorithm
- Weighted/unweighted graphs
- Interactive node placement
- Path highlighting

**Features:**
- 🎬 Step-by-step animations
- 📝 Detailed explanations
- 📊 Complexity analysis
- 📄 Export reports (HTML/PDF)
- 🎨 Professional visualizations

### 2. Accountability Management Module

Complete business management system for small to medium enterprises.

**Components:**

#### Dashboard
- Financial overview
- Quick statistics
- Recent activity feed

#### Billing & Invoicing
- Create and manage invoices
- Customer database
- Payment tracking
- Status management (Draft, Pending, Paid, Overdue, Cancelled)
- Automatic calculations
- Cloud backup to Firebase

#### Financial Accounting
- General ledger
- Account management
- Transaction recording

#### Reporting
- Financial reports
- Tax calculations
- Custom date ranges
- Export capabilities

#### Compliance & Audit
- Audit trail logging
- Compliance monitoring
- Regulatory reporting

**Database Schema:**
- Customers
- Invoices
- Invoice Items
- Accounts
- Transactions
- Audit Logs

### 3. AI Assistant Module

Intelligent help system powered by modern AI models.

**Capabilities:**
- Natural language conversations
- Context-aware responses
- Multi-modal input (text, audio, video)
- User-specific assistance
- Integration with OpenAI GPT-4
- Fallback to GitHub Models API

**Use Cases:**
- Code explanations
- Data structure help
- Business process guidance
- Document analysis
- Financial advice

---

## 🔨 Build & Run

### Using Apache Ant

```bash
# Clean previous builds
ant clean

# Compile source code
ant compile

# Run the application
ant run

# Clean, compile, and run in one command
ant clean compile run

# Create distributable JAR
ant jar
```

### Using VS Code Task

The project includes a pre-configured task:

```bash
# Press Ctrl+Shift+P (Cmd+Shift+P on Mac)
# Type "Run Task"
# Select "Build and Run Java Application"
```

### Manual Compilation

```bash
# Compile
javac -d bin -cp "lib/*" src/com/raven/**/*.java

# Run
java -cp "bin;lib/*" com.raven.main.Main
```

---

## 📖 Documentation

### Available Documentation

- `README.md` - This file
- `OAUTH_SETUP_GUIDE.md` - OAuth 2.0 configuration guide
- `OAUTH_RESTORATION_COMPLETE.md` - OAuth implementation details
- `FIREBASE_INTEGRATION_COMPLETE.md` - Firebase setup guide
- `FIREBASE_SETUP.md` - Additional Firebase documentation
- `GOOGLE_OAUTH_SETUP_GUIDE.md` - Google Cloud Console setup
- `TEST_BST_IMPROVEMENTS.md` - BST enhancement documentation
- `docs/BST_Documentation.html` - Interactive BST documentation
- `docs/Graph_Documentation.html` - Graph algorithms documentation

### Code Documentation

Most classes include JavaDoc comments:

```bash
# Generate JavaDoc (if configured)
ant javadoc
```

### Exported Analysis

The Data Structures module can export detailed analysis reports:
- HTML format with interactive styling
- PDF format for printing
- Includes complexity analysis, operation history, and theoretical explanations

Example exports are in the `exported/` directory.

---

## 🧪 Testing

### Manual Testing

1. **Authentication Flow**
   ```bash
   ant run
   # Test traditional login
   # Test Google OAuth
   # Test email verification
   ```

2. **Data Structures**
   - Insert values into BST
   - Perform operations
   - Export analysis
   - Verify animations

3. **Accountability System**
   - Create invoices
   - Manage customers
   - Test Firebase backup
   - Generate reports

### Test Files

- `src/com/raven/test/DatabaseTest.java` - Database connectivity
- `src/com/raven/test/SimpleTest.java` - Basic functionality
- `src/com/raven/test/PropertiesTest.java` - Configuration testing

---

## 🔒 Security

### Best Practices

1. **Never commit credentials**
   - `oauth.properties` is in `.gitignore`
   - `ai.properties` is in `.gitignore`
   - Firebase service account JSON is excluded

2. **Use templates**
   - Commit `oauth.properties.template` with placeholders
   - Real credentials only in local files

3. **Secure OAuth flow**
   - Uses HTTPS for OAuth
   - State parameter for CSRF protection
   - Automatic port selection (8080, 8081, 3000)

4. **Database security**
   - Prepared statements (SQL injection prevention)
   - Password hashing (if implemented)
   - Session token management

### Sensitive Files Backup

A list of sensitive files is maintained in `SENSITIVE_FILES_BACKUP_LIST.txt` for team members who need to set up the application.

---

## 🐛 Troubleshooting

### Common Issues

#### OAuth "redirect_uri_mismatch"
**Solution:** Verify redirect URIs in Google Cloud Console match exactly:
- `http://127.0.0.1:8080/oauth2callback`
- `http://localhost:8080/oauth2callback`

#### Port Already in Use
**Solution:** The app tries ports 8080, 8081, 3000 automatically. Ensure at least one is available.

#### Firebase Connection Failed
**Solution:** 
- Verify service account JSON is valid
- Check project ID matches
- Ensure Firestore is enabled in Firebase Console

#### AI Service Not Configured
**Solution:**
- Create `ai.properties` in `src/com/raven/config/`
- Add valid API keys
- Check `AIConfig.java` for property names

#### Database Lock Error
**Solution:**
- Close other connections to the SQLite database
- Restart the application
- Check file permissions on database file

### Verification Scripts

```bash
# Windows
verify-oauth.bat        # Verify OAuth configuration
security-check.bat      # Check for exposed credentials
cleanup-oauth.bat       # Clean OAuth cache

# Linux/Mac
./setup-oauth-simple.sh # Set up OAuth
```

---

## 🎨 UI Screenshots

*(Add screenshots of your application here)*

### Login Screen
![Login Screen](docs/screenshots/login.png)

### Data Structures Dashboard
![Dashboard](docs/screenshots/dashboard.png)

### BST Visualization
![BST](docs/screenshots/bst.png)

### Accountability Management
![Accounting](docs/screenshots/accounting.png)

---

## 🗺 Roadmap

### Version 0.5 (Current)
- ✅ Core authentication system
- ✅ Google OAuth integration
- ✅ Data structure visualizations (7 types)
- ✅ Basic accountability system
- ✅ AI assistant integration
- ✅ Firebase backup

### Version 0.6 (Planned)
- [ ] User database persistence
- [ ] Complete email service integration
- [ ] Advanced financial reporting
- [ ] Multi-user support
- [ ] Role-based access control

### Version 1.0 (Future)
- [ ] Mobile companion app
- [ ] Real-time collaboration
- [ ] Advanced AI features (voice, video)
- [ ] Cloud synchronization
- [ ] Plugin architecture

---

## 👥 Contributing

Contributions are welcome! This is an educational project.

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Add JavaDoc for public methods
- Keep methods focused and small
- Write self-documenting code

---

## 📄 License

This project is licensed for educational purposes. See individual library licenses in the `lib/` directory.

---

## 🙏 Acknowledgments

- **MigLayout** - Flexible layout manager
- **Timing Framework** - Animation library
- **Google OAuth Client** - Authentication
- **Firebase** - Cloud backend
- **OpenAI** - AI capabilities
- **SQLite** - Lightweight database

---

## 📧 Contact

**Repository:** [titanioco/Proyectos-Backup](https://github.com/titanioco/Proyectos-Backup)
**Branch:** version_0.5

---

## 📊 Project Statistics

- **Languages:** Java
- **Total Lines of Code:** ~25,000+
- **Modules:** 3 (Data Structures, Accountability, AI)
- **Data Structures:** 7 types
- **External Libraries:** 30+
- **UI Components:** 50+

---

## 🔧 Development Setup

### Recommended IDE: NetBeans

This project was developed in NetBeans and includes NetBeans project files:
- `nbproject/project.properties`
- `nbproject/build-impl.xml`
- `build.xml`

### Alternative IDEs

**IntelliJ IDEA:**
1. Import as Ant project
2. Configure JDK 11+
3. Add libraries from `lib/` to classpath

**Eclipse:**
1. Import existing Ant project
2. Set build path to include `lib/*.jar`
3. Configure build.xml as Ant buildfile

**VS Code:**
1. Install Java Extension Pack
2. Configure `java.project.referencedLibraries` in settings
3. Use integrated terminal for `ant` commands

---

## 🌟 Key Highlights

- **Educational Focus:** Designed for learning data structures and algorithms
- **Modern UI:** Clean, animated interface with professional styling
- **Extensible:** Modular architecture for easy feature addition
- **Cloud-Ready:** Firebase integration for cloud backup
- **AI-Powered:** Intelligent assistance for users
- **Business-Ready:** Complete accountability management system
- **Well-Documented:** Extensive documentation and code comments
- **Secure:** OAuth 2.0 authentication with best practices

---

**Built with ❤️ for education and learning**

*Last Updated: December 2025*
*Version: 0.5*
