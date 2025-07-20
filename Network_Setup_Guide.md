# Network Access Setup for OAuth Application

## 🌐 **Network Requirements**

Your Java application requires network access for the OAuth flow to function properly:

### **Required Network Connections:**

1. **Outbound HTTPS to Google APIs:**
   - `accounts.google.com` - OAuth authorization
   - `oauth2.googleapis.com` - Token exchange
   - `www.googleapis.com` - User info retrieval

2. **Local HTTP Server:**
   - `localhost:8080` - OAuth callback server
   - Must accept incoming connections from browser

### **Ports Required:**
- **Port 8080** (HTTP) - OAuth callback server
- **Port 443** (HTTPS) - Google API calls

---

## 🔒 **Windows Firewall Setup**

### **Method 1: Allow Java Application**

1. Open **Windows Defender Firewall**
2. Click **"Allow an app or feature through Windows Defender Firewall"**
3. Click **"Change Settings"** (requires admin)
4. Click **"Allow another app..."**
5. Browse to your Java installation:
   - Usually: `C:\Program Files\Java\jdk-XX\bin\java.exe`
   - Or: `C:\Program Files\Java\jre-XX\bin\java.exe`
6. Check both **"Private"** and **"Public"** networks
7. Click **"OK"**

### **Method 2: Allow Port 8080**

1. Open **Windows Defender Firewall with Advanced Security**
2. Click **"Inbound Rules"** in left panel
3. Click **"New Rule..."** in right panel
4. Select **"Port"** → **"Next"**
5. Select **"TCP"** and enter **"8080"** → **"Next"**
6. Select **"Allow the connection"** → **"Next"**
7. Check all network types → **"Next"**
8. Name: **"Java OAuth Callback Server"** → **"Finish"**

### **Method 3: Temporarily Disable Firewall (Testing Only)**

⚠️ **Warning: Only for testing! Re-enable after testing.**

1. Open **Windows Defender Firewall**
2. Click **"Turn Windows Defender Firewall on or off"**
3. Temporarily turn off for **Private networks**
4. Test your application
5. **Re-enable firewall immediately after testing**

---

## 🖥️ **Command Line Testing**

### **Test Network Connectivity:**

```powershell
# Test Google OAuth endpoint
curl -I https://accounts.google.com/o/oauth2/v2/auth

# Test Google API endpoint
curl -I https://www.googleapis.com/oauth2/v2/userinfo

# Test if port 8080 is available
netstat -an | findstr :8080
```

### **Test Local Server:**

```powershell
# Start a simple HTTP server on port 8080 (testing)
python -m http.server 8080
# Then visit: http://localhost:8080
```

---

## 🛡️ **Antivirus Considerations**

### **Common Issues:**
- **Antivirus blocking Java network access**
- **Real-time protection blocking HTTP server**
- **URL filtering blocking Google OAuth**

### **Solutions:**
1. **Add Java to Antivirus Exclusions:**
   - Add Java installation folder
   - Add your project folder
   - Add `java.exe` and `javaw.exe` to allowed programs

2. **Temporarily Disable Real-time Protection:**
   - Only for testing
   - Re-enable after confirming application works

---

## 🔧 **Application-Specific Network Setup**

### **OAuth Configuration Check:**

Your `oauth.properties` file should have:
```properties
google.oauth.client.id=639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok.apps.googleusercontent.com
google.oauth.client.secret=
google.oauth.redirect.uri=http://localhost:8080/oauth/callback
google.oauth.scope=openid email profile
google.oauth.auth.url=https://accounts.google.com/o/oauth2/v2/auth
google.oauth.token.url=https://oauth2.googleapis.com/token
```

### **Network Debugging in Code:**

Add these debug prints to see network issues:

```java
// In GoogleAuthService.java
System.out.println("🌐 Attempting to connect to: " + OAuthConfig.getAuthUrl());

// In OAuth2CallbackHandler.java  
System.out.println("🔌 Starting HTTP server on port 8080...");
System.out.println("🔍 Server address: http://localhost:8080/oauth/callback");

// Test connectivity
try {
    URL testUrl = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
    HttpURLConnection conn = (HttpURLConnection) testUrl.openConnection();
    conn.setRequestMethod("HEAD");
    int responseCode = conn.getResponseCode();
    System.out.println("✓ Google API reachable: " + responseCode);
} catch (Exception e) {
    System.err.println("❌ Network connectivity issue: " + e.getMessage());
}
```

---

## 🚨 **Common Network Error Messages**

### **"Connection refused" or "ConnectException":**
- **Cause:** Firewall blocking outbound connections
- **Solution:** Allow Java through firewall

### **"Address already in use" (Port 8080):**
- **Cause:** Another application using port 8080
- **Solution:** 
  ```powershell
  # Find what's using port 8080
  netstat -ano | findstr :8080
  # Kill the process or change OAuth callback port
  ```

### **"UnknownHostException":**
- **Cause:** DNS resolution issues
- **Solution:** Check internet connectivity and DNS

### **"SocketTimeoutException":**
- **Cause:** Network timeout or slow connection
- **Solution:** Increase timeout or check connection speed

---

## ✅ **Quick Network Test**

### **Step 1: Test Basic Connectivity**
```powershell
ping google.com
nslookup accounts.google.com
```

### **Step 2: Test Port 8080**
```powershell
# Check if port is free
netstat -an | findstr :8080
# Should show no results if port is available
```

### **Step 3: Test Java Network Access**
Create a simple test file:

```java
// NetworkTest.java
import java.net.*;
import java.io.*;

public class NetworkTest {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            System.out.println("✓ Network access working: " + conn.getResponseCode());
        } catch (Exception e) {
            System.err.println("❌ Network issue: " + e.getMessage());
        }
    }
}
```

Compile and run:
```powershell
javac NetworkTest.java
java NetworkTest
```

---

## 🎯 **Expected Results**

### **Successful Network Setup:**
- Java can connect to Google APIs
- Port 8080 is available for OAuth callback
- Browser can reach `http://localhost:8080/oauth/callback`
- No firewall/antivirus blocking connections

### **OAuth Flow Network Activity:**
```
1. Browser → https://accounts.google.com (OAuth consent)
2. Google → http://localhost:8080/oauth/callback (redirect)
3. Java App → https://oauth2.googleapis.com/token (token exchange)
4. Java App → https://www.googleapis.com/oauth2/v2/userinfo (user info)
```

If any of these connections fail, the OAuth flow will get stuck at that step.

---

## 🔄 **Next Steps**

1. **Allow Java through Windows Firewall**
2. **Test network connectivity with the commands above**
3. **Run the OAuth application again**
4. **Check console output for network-related errors**

Most OAuth hanging issues are caused by network connectivity problems, especially the callback server not being reachable by the browser.
