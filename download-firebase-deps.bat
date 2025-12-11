@echo off
REM Firebase Admin SDK Dependency Downloader - COMPLETE VERSION
REM This script downloads ALL required Firebase Admin SDK JARs

echo ========================================
echo Firebase Admin SDK Dependency Downloader
echo ========================================
echo.

set LIB_DIR=lib
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"

echo Downloading Firebase Admin SDK and ALL dependencies...
echo.

REM Firebase Admin SDK (v9.2.0)
echo [1/10] Downloading firebase-admin-9.2.0.jar...
curl -L -o "%LIB_DIR%\firebase-admin-9.2.0.jar" "https://repo1.maven.org/maven2/com/google/firebase/firebase-admin/9.2.0/firebase-admin-9.2.0.jar"

REM Google Cloud Firestore (v3.7.9)
echo [2/10] Downloading google-cloud-firestore-3.7.9.jar...
curl -L -o "%LIB_DIR%\google-cloud-firestore-3.7.9.jar" "https://repo1.maven.org/maven2/com/google/cloud/google-cloud-firestore/3.7.9/google-cloud-firestore-3.7.9.jar"

REM Google Auth Library (v1.16.0)
echo [3/10] Downloading google-auth-library-oauth2-http-1.16.0.jar...
curl -L -o "%LIB_DIR%\google-auth-library-oauth2-http-1.16.0.jar" "https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/1.16.0/google-auth-library-oauth2-http-1.16.0.jar"

REM Google Auth Library Credentials (v1.16.0)
echo [4/10] Downloading google-auth-library-credentials-1.16.0.jar...
curl -L -o "%LIB_DIR%\google-auth-library-credentials-1.16.0.jar" "https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.16.0/google-auth-library-credentials-1.16.0.jar"

REM Google API Common (v2.15.0) - MISSING DEPENDENCY
echo [5/10] Downloading google-api-common-2.15.0.jar...
curl -L -o "%LIB_DIR%\google-api-common-2.15.0.jar" "https://repo1.maven.org/maven2/com/google/api/api-common/2.15.0/api-common-2.15.0.jar"

REM Google Cloud Core (v2.22.0) - MISSING DEPENDENCY
echo [6/10] Downloading google-cloud-core-2.22.0.jar...
curl -L -o "%LIB_DIR%\google-cloud-core-2.22.0.jar" "https://repo1.maven.org/maven2/com/google/cloud/google-cloud-core/2.22.0/google-cloud-core-2.22.0.jar"

REM Google API Core (v2.22.0) - MISSING DEPENDENCY FOR ApiFuture
echo [7/10] Downloading gax-2.32.0.jar...
curl -L -o "%LIB_DIR%\gax-2.32.0.jar" "https://repo1.maven.org/maven2/com/google/api/gax/2.32.0/gax-2.32.0.jar"

REM Protobuf Java (v3.24.0) - Required by Firebase
echo [8/10] Downloading protobuf-java-3.24.0.jar...
curl -L -o "%LIB_DIR%\protobuf-java-3.24.0.jar" "https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.24.0/protobuf-java-3.24.0.jar"

REM Protobuf Java Util (v3.24.0) - Required by Firebase
echo [9/10] Downloading protobuf-java-util-3.24.0.jar...
curl -L -o "%LIB_DIR%\protobuf-java-util-3.24.0.jar" "https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java-util/3.24.0/protobuf-java-util-3.24.0.jar"

REM Threetenbp (v1.6.8) - Date/Time library for Firebase
echo [10/10] Downloading threetenbp-1.6.8.jar...
curl -L -o "%LIB_DIR%\threetenbp-1.6.8.jar" "https://repo1.maven.org/maven2/org/threeten/threetenbp/1.6.8/threetenbp-1.6.8.jar"

echo.
echo ========================================
echo Download Complete!
echo ========================================
echo.
echo All Firebase Admin SDK dependencies have been downloaded to the lib directory.
echo You can now compile and run the application with Firebase support.
echo.
pause
