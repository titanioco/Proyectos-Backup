# 🛡️ SECURE OAuth Setup Guide - For Team Collaboration

## 🚨 **SECURITY NOTICE**
This guide shows you how to set up OAuth credentials **WITHOUT** exposing them in version control.

## 📋 **What You Need**
1. **Google Cloud Console Access**: https://console.cloud.google.com/
2. **Project Admin Permissions**: To create OAuth credentials

---

## 🔐 **Step 1: Create Google OAuth Credentials**

### **A. Access Google Cloud Console**
1. Go to: https://console.cloud.google.com/
2. Select your project (or create a new one)
3. Enable the Google Identity API

### **B. Create OAuth Client ID**
1. Navigate to: **APIs & Services** → **Credentials**
2. Click: **Create Credentials** → **OAuth 2.0 Client ID**
3. Choose: **Desktop Application**
4. Name: `Interactive Data Structures Learning Suite`
5. **IMPORTANT**: Copy both values:
   - Client ID (format: `xxxxx-xxxxx.apps.googleusercontent.com`)
   - Client Secret (format: `GOCSPX-xxxxxxxxxxxxx`)

---

## 🔧 **Step 2: Local Configuration Setup**

### **A. Copy Template File**
```powershell
# In your project directory, copy the template
copy oauth.properties.template oauth.properties
```

### **B. Edit Your Local Configuration**
1. Open `oauth.properties` (this file is **git-ignored** for security)
2. Replace these placeholder values:
   ```properties
   # Replace with your actual Client ID
   google.oauth.client.id=YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com
   
   # Replace with your actual Client Secret  
   google.oauth.client.secret=YOUR_GOOGLE_CLIENT_SECRET
   ```

### **C. Verify Security**
Run this command to ensure your credentials won't be committed:
```powershell
git status
```
You should **NOT** see `oauth.properties` in the list of files to be committed.

---

## ✅ **Step 3: Test Your Setup**

### **Compile and Run**
```powershell
ant clean compile
ant run
```

### **Expected Behavior**
- ✅ Console shows: `✓ Loaded OAuth configuration from oauth.properties`
- ✅ "Sign in with Google" opens browser to Google OAuth
- ✅ After consent, dashboard opens automatically

### **If You See Demo Mode**
- ❌ Console shows: `⚠️ OAuth not configured, using demo mode`
- **Solution**: Check that `oauth.properties` has real values (not placeholders)

---

## 🔄 **For Team Members**

### **Each Developer Must:**
1. **Get their own Google OAuth credentials** (don't share!)
2. **Copy `oauth.properties.template` to `oauth.properties`**
3. **Fill in their own credentials in the local file**
4. **Never commit `oauth.properties` to git** (it's auto-ignored)

### **Project Repository Contains:**
- ✅ `oauth.properties.template` (safe placeholder file)
- ✅ `OAuthConfig.java` (code to read credentials)
- ✅ `.gitignore` (protects credential files)
- ❌ **No actual credentials anywhere**

---

## 🛡️ **Security Features**

### **Files Protected by .gitignore:**
```
oauth.properties
oauth.properties.fixed
oauth.properties.local
*.credentials
*.secret
client_secret*.json
```

### **What Gets Committed:**
- ✅ Template files with placeholders
- ✅ Source code
- ✅ Build configuration
- ❌ **NO real credentials**

---

## 🚨 **Security Best Practices**

### **DO:**
- ✅ Use separate OAuth projects for dev/staging/production
- ✅ Rotate credentials regularly
- ✅ Use environment variables in production
- ✅ Keep credentials in local files only

### **DON'T:**
- ❌ Commit credentials to any repository (public or private)
- ❌ Share credentials via email/chat
- ❌ Use production credentials for development
- ❌ Store credentials in source code

---

## 🔍 **Troubleshooting**

### **"OAuth not configured" Error**
- Check: `oauth.properties` exists and has real values
- Verify: File is not empty or contains placeholders

### **"Invalid client" Error**
- Check: Client ID/Secret copied correctly (no extra spaces)
- Verify: OAuth client type is "Desktop Application"

### **"Invalid redirect URI" Error**
- Add to Google Console: `http://127.0.0.1:8080/oauth2callback`
- Alternative: `http://localhost:8080/oauth2callback`

---

## 🎯 **Quick Setup Checklist**

- [ ] Created Google OAuth credentials
- [ ] Copied `oauth.properties.template` to `oauth.properties`  
- [ ] Filled in real credentials in local `oauth.properties`
- [ ] Verified `oauth.properties` is **NOT** in `git status`
- [ ] Tested OAuth flow works
- [ ] ✅ **Ready for secure collaboration!**

---

**Remember**: Every team member needs their own OAuth credentials. Never share credentials between developers!
