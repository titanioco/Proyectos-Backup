| File                             | Purpose                                                           |
| -------------------------------- | ----------------------------------------------------------------- |
| `database/DBManager.java`        | SQLite connection + schema creation                               |
| `model/User.java`                | POJO mapping `users` table                                        |
| `model/UserDAO.java`             | CRUD + findByEmail / findByGoogleSub                              |
| `service/GoogleAuthService.java` | OAuth 2.0 PKCE flow helper                                        |
| `ui/GoogleSignInButton.java`     | Styled button to trigger Google flow                              |
| `ui/DashboardFrame.java`         | Empty dashboard shown after login                                 |
| `ui/PasswordStrengthLabel.java`  | Optional UX helper                                                |
| **Patches**                      | `Main.java`, `PanelLoginAndRegister.java`, `PanelVerifyCode.java` |
6. Agent Tasks
Generate the files above with correct package names (com.raven.*).
Replace ServiceUser stub with UserDAO.
Insert Google button under classic form in PanelLoginAndRegister.
Ensure all DB & network work uses SwingWorker + PanelLoading.
After successful login/reg, dispose Main and open DashboardFrame.showFor(user).
7. Acceptance Criteria Checklist
[ ] New classic user → receives verification code → row stored with bcrypt hash.
[ ] Existing classic user → password verified via bcrypt → dashboard opens.
[ ] Google flow → browser consent → token → user created/updated → dashboard opens.
[ ] No plaintext passwords stored.
[ ] App still compiles & runs with a single mvn package && java -jar target/app.jar.
8. Quick Test Script
Sign up with email → check university.db → password_hash starts with $2a$.
Restart app → sign in with same email/password → dashboard appears.
Click Google button → authenticate → dashboard appears (new row with google_sub).