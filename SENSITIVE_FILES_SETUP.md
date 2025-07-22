# 🔐 SENSITIVE FILES MANUAL SETUP GUIDE

## ⚠️ CRITICAL: Files NOT in Repository (For Security)

The following files contain sensitive information and are **intentionally excluded** from the repository via `.gitignore`. You **MUST** copy these manually when setting up a new local environment.

### 📄 **Required Sensitive Files**

#### 1. `oauth.properties` 
**❌ NOT in repository** - Contains real Google OAuth credentials
- **Location**: Root directory (`/oauth.properties`)
- **Source**: Copy from your working local environment
- **Template**: Use `oauth.properties.template` or `oauth.properties.example` as reference

#### 2. `university.db` (if contains data)
**❌ NOT in repository** - May contain user data
- **Location**: Root directory (`/university.db`)
- **Source**: Copy from your working local environment if it contains important data
- **Note**: Empty database files are safe, but populated ones may contain sensitive user information

---

## 🚀 **Setup Instructions for New Local Environment**

### Step 1: Clone Repository
```bash
git clone [your-repository-url]
cd [repository-name]
```

### Step 2: Copy Sensitive Files Manually
From your working local environment, copy these files:

```bash
# Copy OAuth configuration (REQUIRED for OAuth functionality)
cp /path/to/working/environment/oauth.properties ./oauth.properties

# Copy database if it contains important data (OPTIONAL)
cp /path/to/working/environment/university.db ./university.db
```

### Step 3: Verify Setup
- Ensure `oauth.properties` exists and contains your real credentials
- Ensure `university.db` exists (can be empty for new setups)
- Run the application to verify OAuth functionality

---

## 📋 **File Security Status**

| File | Status | Reason | Action Required |
|------|--------|--------|-----------------|
| `oauth.properties` | ❌ **EXCLUDED** | Contains real Google OAuth credentials | ✅ **Copy manually** |
| `oauth.properties.template` | ✅ **SAFE** | Template with placeholders only | ❌ **No action** |
| `oauth.properties.example` | ✅ **SAFE** | Example with placeholders only | ❌ **No action** |
| `university.db` | ❌ **EXCLUDED** | May contain user data | ✅ **Copy if needed** |
| `OAUTH_INVESTIGATION_FIX_GUIDE.md` | ✅ **SAFE** | Documentation only | ❌ **No action** |
| All `.java` files | ✅ **SAFE** | Source code without credentials | ❌ **No action** |
| All `.md` guides | ✅ **SAFE** | Documentation only | ❌ **No action** |
| `build.xml`, `manifest.mf` | ✅ **SAFE** | Build configuration without secrets | ❌ **No action** |

---

## 🔒 **Security Best Practices**

### ✅ **DO:**
- Keep real credentials in local files only
- Use template files with placeholders for repository
- Copy sensitive files manually between environments
- Regularly rotate OAuth credentials
- Keep `.gitignore` updated for new sensitive files

### ❌ **DON'T:**
- Commit real OAuth credentials to repository
- Share credentials via email or messaging
- Include sensitive data in commit messages
- Remove files from `.gitignore` without careful review

---

## 🚨 **Emergency: If Credentials Were Committed**

If you accidentally committed sensitive files:

1. **Immediately rotate credentials** in Google Cloud Console
2. **Remove from git history**:
   ```bash
   git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch oauth.properties' --prune-empty --tag-name-filter cat -- --all
   git push --force --all
   ```
3. **Update `.gitignore`** to prevent future commits
4. **Generate new credentials** in Google Cloud Console

---

## ✅ **Verification Checklist**

Before pushing changes:

- [ ] `oauth.properties` is in `.gitignore`
- [ ] No real credentials in committed files
- [ ] Template files contain only placeholders
- [ ] Application works with manual credential files
- [ ] New team members can set up using this guide

---

## 📞 **Support**

If you need help setting up sensitive files:

1. Contact the project maintainer for credential access
2. Follow this guide step-by-step
3. Verify functionality before development
4. Report any security concerns immediately

---

**🔐 Remember: Security is everyone's responsibility!**
