# Google OAuth Setup Guide

## 🔧 Quick Setup (5 minutes)

### Step 1: Create Google OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API or Google People API
4. Go to **Credentials** → **Create Credentials** → **OAuth 2.0 Client ID**
5. Choose **Desktop Application** (recommended) or **Web Application**
6. Copy the **Client ID** (looks like: `123456789012-abcdefghijklmnop.apps.googleusercontent.com`)

### Step 2: Configure the Application

1. Open `oauth.properties` in the project root
2. Replace `YOUR_CLIENT_ID_HERE` with your actual Client ID:
   ```properties
   google.oauth.client.id=123456789012-abcdefghijklmnop.apps.googleusercontent.com
   ```
3. For **Desktop Application**: Leave client secret empty:
   ```properties
   google.oauth.client.secret=
   ```
4. For **Web Application**: Add your client secret:
   ```properties
   google.oauth.client.secret=GOCSPX-your-actual-client-secret-here
   ```

### Step 3: Configure Redirect URI (Web Applications Only)

If you chose **Web Application**, add this redirect URI in Google Cloud Console:
```
http://127.0.0.1:8080/oauth2callback
```

### Step 4: Test the Configuration

1. Run: `ant clean compile`
2. Run: `ant run`
3. Click **"Sign in with Google"**
4. Complete the OAuth flow in your browser

## 🔍 Troubleshooting

### "OAuth not properly configured" message
- Check that your Client ID ends with `.apps.googleusercontent.com`
- Ensure you copied the full Client ID without extra spaces
- Verify the oauth.properties file exists and is not empty

### "redirect_uri_mismatch" error
- In Google Cloud Console, add: `http://127.0.0.1:8080/oauth2callback`
- For desktop apps, also add: `http://localhost` (without port)

### Browser doesn't open / OAuth timeout
- Check Windows Firewall settings
- Try disabling antivirus temporarily
- Ensure port 8080 is not blocked

### "Access blocked" error
- Your OAuth consent screen needs to be configured
- Add test users in Google Cloud Console
- Set application to "Testing" mode initially

## 📝 OAuth Application Types

### Desktop Application (Recommended)
✅ No client secret required
✅ Works with any port
✅ Simpler setup
❌ Requires browser interaction

### Web Application
✅ More secure with client secret
✅ Professional OAuth flow
❌ Requires exact redirect URI match
❌ More complex setup

## 🔒 Security Notes

- Never commit `oauth.properties` to git (it's already in .gitignore)
- Keep your client secret private
- Use environment variables in production
- Regularly rotate credentials

## 📚 Useful Links

- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Google Cloud Console](https://console.cloud.google.com/)
- [OAuth 2.0 Scopes](https://developers.google.com/identity/protocols/oauth2/scopes)

## ⚠️ Important Notes

1. **First Time Setup**: Google may show a warning about "unverified app" - click "Advanced" → "Go to [App Name] (unsafe)" for testing
2. **Consent Screen**: Configure your OAuth consent screen in Google Cloud Console
3. **API Limits**: Google APIs have usage quotas - monitor your usage
4. **Test Users**: Add test email addresses in Google Cloud Console for restricted apps

---

**Need Help?** Check the console output when testing OAuth - it provides detailed debugging information.
