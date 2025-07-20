# Alternative OAuth Redirect URI Setup Methods

## 🚨 **gcloud CLI is Crashing - Use These Alternatives**

Since the gcloud command is having issues, here are several alternative methods to configure your OAuth redirect URI:

---

## 🌐 **Method 1: Direct Google Cloud Console (Bypass UI Glitch)**

### **Try Different Browsers/Modes:**

1. **Use Incognito/Private Mode**
   - Open Google Cloud Console in incognito mode
   - Sometimes UI glitches are cached

2. **Try Different Browser**
   - Chrome → Try Firefox or Edge
   - Clear browser cache and cookies

3. **Use Mobile Browser**
   - Sometimes mobile view shows different UI elements

4. **Force Desktop Site**
   - In mobile browser, request desktop site

### **Direct URL Access:**

Try accessing the OAuth client directly:
```
https://console.cloud.google.com/apis/credentials/oauthclient/639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok
```

---

## 📱 **Method 2: Google Cloud Console Mobile App**

1. **Download Google Cloud Console app** (iOS/Android)
2. **Navigate to APIs & Services → Credentials**
3. **Find your OAuth client**
4. **Edit redirect URIs**

The mobile app sometimes has different UI that bypasses web glitches.

---

## 💻 **Method 3: Temporary Workaround - Use Different Port**

Since you can't update the redirect URI, temporarily modify your Java application to use a different approach:

### **Option A: Use Different Port**

Update your `oauth.properties`:
```properties
# Try port 8000 instead of 8080
google.oauth.redirect.uri=http://localhost:8000/oauth/callback
```

Then create a new OAuth client with port 8000.

### **Option B: Use Postman/Manual Testing**

1. **Get authorization code manually:**
   ```
   https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=openid email profile
   ```

2. **Use special redirect URI:** `urn:ietf:wg:oauth:2.0:oob`
   - This shows the code in the browser instead of redirecting

3. **Copy the code manually** and test your token exchange

---

## 🔧 **Method 4: Create New OAuth Client**

Since you can't modify the existing one, create a new OAuth client:

### **Steps:**

1. **Go to Google Cloud Console → APIs & Services → Credentials**
2. **Click "Create Credentials" → "OAuth 2.0 Client ID"**
3. **Select "Desktop application"**
4. **Add redirect URI:** `http://localhost:8080/oauth/callback`
5. **Copy the new client ID**

### **Update your oauth.properties:**
```properties
google.oauth.client.id=NEW_CLIENT_ID_HERE
google.oauth.redirect.uri=http://localhost:8080/oauth/callback
```

---

## 🐍 **Method 5: Python Script to Update OAuth**

Create a Python script to update via Google API:

```python
#!/usr/bin/env python3
"""
OAuth Client Updater Script
Run this in Google Cloud Shell or local environment with gcloud auth
"""

import json
import subprocess
import requests

def get_access_token():
    """Get access token using gcloud"""
    try:
        result = subprocess.run(['gcloud', 'auth', 'print-access-token'], 
                              capture_output=True, text=True, check=True)
        return result.stdout.strip()
    except subprocess.CalledProcessError:
        print("Error getting access token")
        return None

def update_oauth_client(client_id, redirect_uri):
    """Update OAuth client redirect URI"""
    access_token = get_access_token()
    if not access_token:
        return False
    
    headers = {
        'Authorization': f'Bearer {access_token}',
        'Content-Type': 'application/json'
    }
    
    data = {
        'web': {
            'redirect_uris': [redirect_uri]
        }
    }
    
    url = f'https://www.googleapis.com/oauth2/v1/client/{client_id}'
    
    try:
        response = requests.patch(url, headers=headers, json=data)
        print(f"Response Status: {response.status_code}")
        print(f"Response: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    CLIENT_ID = "639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok"
    REDIRECT_URI = "http://localhost:8080/oauth/callback"
    
    print(f"Updating OAuth client {CLIENT_ID}")
    print(f"Setting redirect URI to: {REDIRECT_URI}")
    
    success = update_oauth_client(CLIENT_ID, REDIRECT_URI)
    if success:
        print("✅ OAuth client updated successfully!")
    else:
        print("❌ Failed to update OAuth client")
```

Save as `update_oauth.py` and run:
```bash
python3 update_oauth.py
```

---

## 🔄 **Method 6: Use Demo Mode for Now**

Since OAuth configuration is problematic, modify your app to work with demo mode:

### **Update your oauth.properties:**
```properties
# Comment out or remove client ID to force demo mode
# google.oauth.client.id=639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok
google.oauth.client.secret=
google.oauth.redirect.uri=http://localhost:8080/oauth/callback
```

This will make your app use the mock user data and test the dashboard opening without OAuth complications.

---

## 🎯 **Recommended Approach**

### **Immediate Solution:**
1. **Create a new OAuth client** (Method 4) - This is the fastest
2. **Test with demo mode** (Method 6) - This tests your app logic
3. **Try mobile app** (Method 2) - Sometimes works when web doesn't

### **Priority Order:**
1. ✅ **Create new OAuth client** (5 minutes)
2. ✅ **Test app in demo mode** (immediate)
3. ✅ **Try different browser/incognito** (2 minutes)
4. ✅ **Use Python script** (if you have Python)

---

## 🚀 **Test Your Application Now**

While working on OAuth configuration, test your Java application:

### **Check if it's running:**
```powershell
# Check if Java app is still running
netstat -ano | findstr :8080
```

### **Test with demo mode:**
1. Comment out the client ID in `oauth.properties`
2. Run the application
3. Click "Sign in with Google"
4. Should use mock data and open dashboard

This will verify that the **dashboard opening logic** we implemented works correctly, which is the main issue identified in your debug guide.

The OAuth configuration issue is separate from the core application logic problem!
