# Google OAuth 2.0 Integration for Java Swing Desktop App

## Project Overview

You’re working on a Java Swing desktop app located under:
c:\Users\Office1\Downloads\college\Software\estructuras de datos\Proyectos\Proyectos

The goal is to implement a seamless Google Sign-In using the official Google OAuth client library, replacing an older hand-rolled PKCE flow.

## Current Issues

- Clicking the “Sign in with Google” button prints:Cleaning up any previous OAuth attempts... Starting fresh Google OAuth flow... Starting Google OAuth flow... Error getting OAuth result: java.lang.NoClassDefFoundError: com/google/common/base/Preconditions Google OAuth worker completed
- Missing `com.google.common.base.Preconditions` (Guava) on the classpath.
- Legacy classes `OAuth2CallbackHandler` and manual PKCE code remain in the codebase.
- The app compiles but the OAuth dialog never launches.

## Required Changes

1. **Add Dependencies**  
 Download and place the following JARs into `lib/`:
 - google-oauth-client-1.34.1.jar  
 - google-oauth-client-jetty-1.34.1.jar  
 - google-oauth-client-java6-1.34.1.jar  
 - google-http-client-1.42.3.jar  
 - google-http-client-gson-1.42.3.jar  
 - google-api-client-1.32.1.jar  
 - jetty-server-9.4.44.v20210927.jar  
 - jetty-util-9.4.44.v20210927.jar  
 - gson-2.8.9.jar  
 - **guava-31.1-jre.jar** (to resolve `Preconditions`)

2. **Update Ant Build**  
 In `nbproject/project.properties`, add file references and append all new JARs to:
 ```properties
 file.reference.guava-31.1-jre.jar=lib/guava-31.1-jre.jar
 ...
 javac.classpath=\
   ...:\
   ${file.reference.guava-31.1-jre.jar}
 run.classpath=\
   ...:\
   ${file.reference.guava-31.1-jre.jar}
 ```

3. **Remove Legacy OAuth Classes**  
 Delete or archive:
 - `src/com/raven/service/OAuth2CallbackHandler.java`
 - Any manual PKCE code in `GoogleAuthService.java` (we’ll replace it entirely).

4. **Implement Official Google OAuth Flow**  
 Replace `GoogleAuthService.java` with:
 
```java
package com.raven.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Collections;
import java.util.Map;

public class GoogleAuthService {
    private static final NetHttpTransport HTTP = new NetHttpTransport();
    private static final GsonFactory JSON = new GsonFactory();

    private final GoogleAuthorizationCodeFlow flow;
    private final LocalServerReceiver receiver;

    public GoogleAuthService(String clientId, String clientSecret) throws Exception {
        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP, JSON,
                clientId, clientSecret,
                Collections.singletonList("openid email profile"))
            .setAccessType("offline")
            .build();
        receiver = new LocalServerReceiver.Builder()
            .setHost("127.0.0.1")
            .setPort(8080)
            .build();
    }

    public GoogleUserInfo authorize() throws Exception {
        Credential cred = new AuthorizationCodeInstalledApp(flow, receiver)
            .authorize("user");
        HttpRequestFactory rf = HTTP.createRequestFactory(
            req -> req.getHeaders().setAuthorization("Bearer " + cred.getAccessToken()));
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
        @SuppressWarnings("unchecked")
        Map<String,Object> data = (Map<String,Object>) rf.buildGetRequest(url)
            .execute()
            .parseAs(Map.class);
        return new GoogleUserInfo(
            (String)data.get("email"),
            (String)data.get("name"),
            (String)data.get("id")
        );
    }

    public static class GoogleUserInfo {
        private final String email, name, id;
        public GoogleUserInfo(String email, String name, String id) {
            this.email = email; this.name = name; this.id = id;
        }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getId() { return id; }
    }
}
```


Update Login Panel
In PanelLoginAndRegister.java, remove OAuth2CallbackHandler code and replace the SwingWorker with:
```java
import com.raven.config.OAuthConfig;
import com.raven.service.GoogleAuthService;

// ...
SwingWorker<GoogleAuthService.GoogleUserInfo, Void> worker =
    new SwingWorker<>() {
        @Override
        protected GoogleAuthService.GoogleUserInfo doInBackground() throws Exception {
            if (!OAuthConfig.isConfigured()) {
                return new GoogleAuthService.GoogleUserInfo(
                    "demo@example.com", "Demo User", "demo");
            }
            GoogleAuthService svc = new GoogleAuthService(
                OAuthConfig.getClientId(), "" /* installed-app secret */);
            return svc.authorize();
        }
        @Override
        protected void done() {
            try {
                var userInfo = get();
                if (userInfo != null) {
                    SwingUtilities.invokeLater(() ->
                        processGoogleUser(userInfo));
                }
            } catch (Exception e) { /* show error dialog */ }
        }
    };
worker.execute();
```

Ensure Configuration

oauth.properties must contain valid:
````
google.oauth.client.id=YOUR_CLIENT_ID.apps.googleusercontent.com
google.oauth.redirect.uri=http://127.0.0.1:8080
````

Client secret can be blank for “Installed <vscode_annotation details='%5B%7B%22title%22%3A%22hardcoded-credentials%22%2C%22description%22%3A%22Embedding%20credentials%20in%20source%20code%20risks%20unauthorized%20access%22%7D%5D'> apps</vscode_annotation>GUI”.
Build & Test
ant clean compile run

The default browser should open the Google login page.
Upon success, you should be returned to your Swing app and see the user’s email/name.

Deliverables
Updated lib folder with all JARs (including Guava).
New GoogleAuthService.java.
Updated PanelLoginAndRegister.java.
Updated Ant project properties.
Cleaned up legacy callback classes.
Once applied, clicking Sign in with Google should open the browser, handle the callback automatically, and log the user into your desktop app.