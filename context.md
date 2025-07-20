# agent-context.md
> Context bundle for coding-agent  
> Goal: add secure SQLite-backed registration/login + Google OAuth + post-login dashboard to the existing Swing project.

## 1. Project Snapshot
- Entry class: `com.raven.main.Main` (extends `JFrame`, uses `JLayeredPane bg`)
- Existing panels: `PanelLoginAndRegister`, `PanelVerifyCode`, `PanelLoading`, `PanelCover`
- Current flow: email-only registration → verification code (mock mail)

## 2. Desired End-to-End Flow
1. User sees login/register form.
2. Two options:
   a. Classic **email + password** (bcrypt) → verification code → DB insert → Dashboard.  
   b. **Sign in with Google** (OAuth 2.0) → token exchange → auto create user → Dashboard.
3. After success, open new `DashboardFrame`.

## 3. Database
Engine: **SQLite** (local file `university.db`).  
Table: `users`

| Column         | Type      | Notes |
|----------------|-----------|-------|
| id             | INTEGER   | PK AUTOINCREMENT |
| email          | TEXT      | UNIQUE |
| password_hash  | TEXT      | NULL if OAuth |
| google_sub     | TEXT      | NULL if classic |
| full_name      | TEXT      | |
| created_at     | DATETIME  | DEFAULT CURRENT_TIMESTAMP |

## 4. Dependencies (Maven)
```xml
<dependencies>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.43.2.0</version>
    </dependency>
    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>
    <dependency>
        <groupId>com.google.auth</groupId>
        <artifactId>google-auth-library-oauth2-http</artifactId>
        <version>1.19.0</version>
    </dependency>
    <dependency>
        <groupId>com.google.http-client</groupId>
        <artifactId>google-http-client-gson</artifactId>
        <version>1.43.3</version>
    </dependency>
</dependencies>