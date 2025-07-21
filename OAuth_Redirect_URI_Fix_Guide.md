# Fix Google Cloud Console Redirect URI - Multiple Solutions

## 🌐 **Problem: Google Cloud Console UI Glitch**

You have OAuth client `639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok.apps.googleusercontent.com` but can't set the redirect URI due to UI issues.

---

## 🔧 **Solution 1: Direct Console URL Access**

Try accessing the OAuth client directly with this specific URL:

```
https://console.cloud.google.com/apis/credentials/oauthclient/639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok
```

**Steps:**
1. Copy the URL above
2. Paste into browser address bar
3. This should take you directly to the OAuth client edit page
4. Add redirect URI: `http://localhost:8080/oauth/callback`

---

## 🔧 **Solution 2: Browser Debugging**

### **Chrome/Edge Developer Tools:**
1. **Open Developer Tools** (F12)
2. **Go to Console tab**
3. **Navigate to your OAuth client page**
4. **If you see JavaScript errors**, try:
   ```javascript
   // Clear local storage
   localStorage.clear();
   sessionStorage.clear();
   
   // Reload page
   location.reload();
   ```

### **Try Different Browsers:**
- **Chrome** → Try **Firefox** or **Edge**
- **Use Incognito/Private mode**
- **Disable browser extensions**

---

## 🔧 **Solution 3: Mobile Google Cloud Console**

1. **Open on mobile device**: https://console.cloud.google.com
2. **Navigate to**: APIs & Services → Credentials
3. **Find your OAuth client**
4. **Mobile UI might show different interface**
5. **Add redirect URI**: `http://localhost:8080/oauth/callback`

---

## 🔧 **Solution 4: Create New OAuth Client**

Since the existing one has UI issues:

1. **Go to Google Cloud Console**
2. **APIs & Services → Credentials**
3. **Create Credentials → OAuth 2.0 Client ID**
4. **Application type**: Desktop application
5. **Name**: "Java Desktop App - Fixed"
6. **Authorized redirect URIs**: `http://localhost:8080/oauth/callback`
7. **Create and copy new Client ID**

---

## 🔧 **Solution 5: Alternative Redirect URIs**

If you can't set `localhost:8080`, try these alternatives:

### **Option A: Use 127.0.0.1 instead of localhost**
```
http://127.0.0.1:8080/oauth/callback
```

### **Option B: Use different port**
```
http://localhost:8000/oauth/callback
```

Then update your `oauth.properties`:
```properties
google.oauth.redirect.uri=http://127.0.0.1:8080/oauth/callback
```

### **Option C: Use urn:ietf:wg:oauth:2.0:oob**
```
urn:ietf:wg:oauth:2.0:oob
```
This special URI shows the authorization code in the browser instead of redirecting.

---

## 🔧 **Solution 6: JSON Configuration**

If you can download the OAuth client configuration:

1. **Go to your OAuth client in console**
2. **Download JSON** (if available)
3. **Edit the JSON** to add redirect URI
4. **Re-upload** (if console supports it)

---

## 🚀 **Quick Test While We Fix OAuth**

Let's test your dashboard features with demo mode first:

### **Current Test Steps:**
1. **Application is running** ✅
2. **Click "Sign in with Google"** 
3. **Should show OAuth setup dialog** (click OK)
4. **Dashboard should open** with Interactive Data Structures Learning Suite

### **Dashboard Features to Test:**
- ✅ **7 tabs visible** (BST + 6 placeholders)
- ✅ **BST tab functional** (insert, delete, find operations)  
- ✅ **Animation controls** (play, pause, step, speed)
- ✅ **Tree traversals** (in-order, pre-order, post-order, level-order)
- ✅ **PDF export** functionality

---

## 🎯 **Immediate Action Plan**

### **Step 1: Test Dashboard Features**
- Use current demo mode to verify all BST functionality works
- Test animations, controls, and PDF export
- Confirm Interactive Data Structures Learning Suite is complete

### **Step 2: Fix OAuth Redirect URI**
Try solutions in this order:
1. **Direct URL access** (Solution 1)
2. **Different browser/incognito** (Solution 2)  
3. **Create new OAuth client** (Solution 4)
4. **Use 127.0.0.1 instead of localhost** (Solution 5A)

### **Step 3: Test Real OAuth**
Once redirect URI is set:
- Enable OAuth client ID in `oauth.properties`
- Test complete OAuth flow
- Verify dashboard opens with real Google user data

---

## 📞 **Current Status Check**

**Right now, please:**
1. **Check if dashboard window opened** after clicking "Sign in with Google"
2. **Tell me what you see** - tabs, BST interface, any errors
3. **Try BST operations** - insert a few numbers (like 50, 30, 70)
4. **Test animations** - click play button and watch tree build

This will confirm the Interactive Data Structures Learning Suite is working correctly before we finalize OAuth setup!
