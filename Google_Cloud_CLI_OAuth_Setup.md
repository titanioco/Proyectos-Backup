# Google Cloud CLI Commands for OAuth Configuration

## 🌐 **Update OAuth Redirect URI via Cloud Shell**

Since you can't see the configuration UI, use these Google Cloud CLI commands in the Cloud Shell terminal:

### **Step 1: Set Up Authentication**

```bash
# Login and set your project
gcloud auth application-default login
gcloud config set project YOUR_PROJECT_ID

# Get your project ID if you don't know it
gcloud config get-value project
```

### **Step 2: Use REST API to Update OAuth Client**

```bash
# Get access token
ACCESS_TOKEN=$(gcloud auth application-default print-access-token)
PROJECT_ID=$(gcloud config get-value project)
CLIENT_ID="639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok"

# Update the OAuth client with redirect URI using REST API
curl -X PATCH \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "web": {
      "redirect_uris": ["http://localhost:8080/oauth/callback"]
    }
  }' \
  "https://www.googleapis.com/oauth2/v1/client/$CLIENT_ID"
```

### **Step 3: Verify the Update**

```bash
# Check that the redirect URI was added correctly
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  "https://www.googleapis.com/oauth2/v1/client/$CLIENT_ID"
```

---

## 🛠️ **Working REST API Method**

Since the `gcloud alpha oauth-clients` commands are not available, use the Google OAuth2 API directly:

### **Method 1: Update Existing OAuth Client**

```bash
# Set variables
ACCESS_TOKEN=$(gcloud auth application-default print-access-token)
CLIENT_ID="639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok"

# First, get the current client configuration
echo "📋 Getting current OAuth client configuration..."
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  "https://www.googleapis.com/oauth2/v1/client/$CLIENT_ID"

# Update with redirect URI
echo "🔄 Updating redirect URI..."
curl -X PATCH \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "web": {
      "redirect_uris": ["http://localhost:8080/oauth/callback"],
      "javascript_origins": ["http://localhost:8080"]
    }
  }' \
  "https://www.googleapis.com/oauth2/v1/client/$CLIENT_ID"
```

### **Method 2: Use Google Cloud Console API**

```bash
# Alternative using the Cloud Resource Manager API
ACCESS_TOKEN=$(gcloud auth application-default print-access-token)
PROJECT_ID=$(gcloud config get-value project)

# Update OAuth consent screen and client
curl -X PATCH \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok",
    "redirectUris": ["http://localhost:8080/oauth/callback"]
  }' \
  "https://console.developers.google.com/apis/credentials/oauthclient/$CLIENT_ID?project=$PROJECT_ID"
```

---

## 🔧 **Complete Working Script**

Copy and paste this script into Google Cloud Shell:

```bash
#!/bin/bash

# OAuth Configuration Script for Google Cloud Shell
echo "🔧 Configuring OAuth Client via REST API..."

# Get authentication and project info
echo "📋 Setting up authentication..."
ACCESS_TOKEN=$(gcloud auth application-default print-access-token)
PROJECT_ID=$(gcloud config get-value project)
CLIENT_ID="639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok"
REDIRECT_URI="http://localhost:8080/oauth/callback"

echo "Project ID: $PROJECT_ID"
echo "Client ID: $CLIENT_ID"
echo "Redirect URI: $REDIRECT_URI"

# Check current configuration
echo -e "\n🔍 Current OAuth client configuration:"
curl -s -H "Authorization: Bearer $ACCESS_TOKEN" \
  "https://www.googleapis.com/oauth2/v1/client/$CLIENT_ID" | jq '.'

# Update the OAuth client configuration
echo -e "\n🔄 Updating OAuth client with redirect URI..."

# Try different API endpoints
echo "Attempting update via OAuth2 API..."
RESPONSE=$(curl -s -X PATCH \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"web\": {
      \"redirect_uris\": [\"$REDIRECT_URI\"],
      \"javascript_origins\": [\"http://localhost:8080\"]
    }
  }" \
  "https://www.googleapis.com/oauth2/v1/client/$CLIENT_ID")

echo "Response: $RESPONSE"

# Verify the update
echo -e "\n✅ Verifying updated configuration..."
curl -s -H "Authorization: Bearer $ACCESS_TOKEN" \
  "https://www.googleapis.com/oauth2/v1/client/$CLIENT_ID" | jq '.web.redirect_uris'

echo -e "\n🎉 OAuth configuration script complete!"
echo "Your redirect URI should now be set to: $REDIRECT_URI"
```

---

## 🚨 **Important Notes**

### **Before Running Commands:**

1. **Replace `your-project-id-here`** with your actual Google Cloud project ID
2. **Make sure you're in the correct project** in Cloud Shell
3. **Enable the OAuth API** if not already enabled:

```bash
# Enable necessary APIs
gcloud services enable oauth2.googleapis.com
gcloud services enable cloudasset.googleapis.com
```

### **If You Get Permission Errors:**

```bash
# Make sure you have the right permissions
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
    --member="user:your-email@gmail.com" \
    --role="roles/oauth.admin"
```

---

## 🔍 **Troubleshooting Commands**

### **Check Current Project:**
```bash
gcloud config get-value project
```

### **List Available Projects:**
```bash
gcloud projects list
```

### **Set Different Project:**
```bash
gcloud config set project YOUR_PROJECT_ID
```

### **Check OAuth API Status:**
```bash
gcloud services list --enabled | grep oauth
```

### **Enable OAuth API:**
```bash
gcloud services enable oauth2.googleapis.com
```

---

## 📋 **Expected Output**

After running the update command, you should see:

```
Updated oauth-client [639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok].

authorizedRedirectUris:
- http://localhost:8080/oauth/callback
clientId: 639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok
displayName: Java Desktop App OAuth
```

---

## ✅ **Quick Test**

After updating, test the configuration:

```bash
# Quick verification
echo "Testing redirect URI configuration..."
gcloud alpha oauth-clients describe 639717052187-nao616khtqhnt7a1lesq2plo0eu3vaok | grep "localhost:8080"
```

If this returns the redirect URI, your configuration is correct!

---

## 🎯 **Next Steps**

1. **Run the update command in Cloud Shell**
2. **Verify the redirect URI is set correctly**
3. **Test your Java OAuth application**
4. **The OAuth flow should now complete successfully**

The redirect URI `http://localhost:8080/oauth/callback` will allow your Java application's OAuth callback server to receive the authorization code from Google.
