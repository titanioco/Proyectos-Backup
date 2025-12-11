# Firebase Admin SDK Setup Guide

## Required Dependencies

The following JAR files are required for Firebase Admin SDK integration:

### Core Firebase Dependencies
1. **firebase-admin-9.2.0.jar** - Main Firebase Admin SDK
2. **google-cloud-firestore-3.7.9.jar** - Firestore database client
3. **google-auth-library-oauth2-http-1.16.0.jar** - OAuth2 authentication
4. **google-auth-library-credentials-1.16.0.jar** - Credentials management

### Already Available in lib/
5. **google-api-client-1.32.1.jar** ✓
6. **google-http-client-1.42.3.jar** ✓
7. **google-http-client-gson-1.42.3.jar** ✓
8. **gson-2.8.9.jar** ✓
9. **guava-31.1-jre.jar** ✓
10. **grpc-context-1.50.2.jar** ✓

## Installation Steps

### Option 1: Automatic Download (Recommended)
Run the provided batch script:
```batch
download-firebase-deps.bat
```

### Option 2: Manual Download
Download the following JARs from Maven Central and place them in the `lib/` directory:

1. Firebase Admin SDK:
   https://repo1.maven.org/maven2/com/google/firebase/firebase-admin/9.2.0/firebase-admin-9.2.0.jar

2. Google Cloud Firestore:
   https://repo1.maven.org/maven2/com/google/cloud/google-cloud-firestore/3.7.9/google-cloud-firestore-3.7.9.jar

3. Google Auth Library OAuth2:
   https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/1.16.0/google-auth-library-oauth2-http-1.16.0.jar

4. Google Auth Library Credentials:
   https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.16.0/google-auth-library-credentials-1.16.0.jar

## Configuration

### Service Account Key
Your Firebase service account key is located at:
```
backup-contable-firebase-adminsdk-fbsvc-55f4d4ba75.json
```

**Project ID:** backup-contable

### Build Configuration
The `build.xml` file needs to be updated to include the new Firebase JARs in the classpath.

## Verification

After setup, verify the integration by:
1. Compiling the project: `ant clean compile`
2. Running the application: `ant run`
3. Testing the Firebase backup feature in the Billing & Invoicing panel

## Security Notes

⚠️ **IMPORTANT:** Never commit the service account JSON file to version control!
- Add `*firebase*.json` to `.gitignore`
- Keep credentials secure and private
