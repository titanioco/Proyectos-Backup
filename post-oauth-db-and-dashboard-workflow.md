# Post-OAuth: Database Persistence & Dashboard Transition Fix

## Context & Issues

1. **Missing Class Error**
   - `java.lang.NoClassDefFoundError: io/opencensus/trace/propagation/TextFormat$Setter`
   - Google HTTP client depends on OpenCensus; need opencensus API and HTTP util JARs on classpath.

2. **User Persistence**
   - Current demo mode skips database operations.
   - Goal: Store or update authenticated user in the database (`UserDAO`) after OAuth.

3. **UI Transition**
   - Login panel stays open; dashboard menu does not appear automatically.
   - After successful OAuth and DB check, minimize/close login window and show dashboard.

---

## 1. Add OpenCensus Dependencies

1. Download JARs (e.g., v0.28.3):
   - `opencensus-api-0.28.3.jar`
   - `opencensus-contrib-http-util-0.28.3.jar`
2. Place them into your `lib/` folder.

3. Update both `nbproject/project.properties` and top-level `project.properties`:
   ```properties
   # OpenCensus (for Google HTTP client)
   file.reference.opencensus-api-0.28.3.jar=lib/opencensus-api-0.28.3.jar
   file.reference.opencensus-http-util-0.28.3.jar=lib/opencensus-contrib-http-util-0.28.3.jar
   
   javac.classpath=...:\
       ${file.reference.opencensus-api-0.28.3.jar}:\
       ${file.reference.opencensus-http-util-0.28.3.jar}
   
   run.classpath=...:${file.reference.opencensus-api-0.28.3.jar}:${file.reference.opencensus-http-util-0.28.3.jar}
   ```

4. Re-run `ant clean compile` to verify no `NoClassDefFoundError`.

---

## 2. Enable True User Persistence

Edit `PanelLoginAndRegister.java`:

1. In `processGoogleUser(GoogleUserInfo userInfo)`:
   - Uncomment the database block:
     ```java
     Optional<User> existing = UserDAO.findByEmail(userInfo.getEmail());
     if (existing.isPresent()) {
         user = existing.get();
         if (user.getGoogleSub() == null) {
             user.setGoogleSub(userInfo.getId());
             UserDAO.updateUser(user);
         }
     } else {
         user = new User(userInfo.getEmail(), userInfo.getName());
         user.setGoogleSub(userInfo.getId());
         UserDAO.createUser(user);
     }
     ```
2. Remove or bypass demo fallback when `OAuthConfig.isConfigured()` is true.
3. Handle SQL exceptions with meaningful error dialogs.

---

## 3. Ensure Seamless UI Transition

In the same class, replace your post-auth block with:

```java
SwingUtilities.invokeLater(() -> {
    Window loginWindow = SwingUtilities.getWindowAncestor(this);
    // Initialize and show dashboard
    DashboardFrame dashboard = new DashboardFrame(user);
    dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
    dashboard.setVisible(true);

    // Hide/Dispose login after a short delay
    new Timer(300, e -> {
        if (loginWindow != null) {
            loginWindow.dispose();
        }
    }).start();
});
```

- Import:
  ```java
  import javax.swing.JFrame;
  import javax.swing.Timer;
  import java.awt.Window;
  ```

---

## 4. Validate & Test

1. Place a breakpoint or log in `doInBackground()` to confirm database operations occur.
2. Run `ant clean compile run`.
3. Click **Sign in with Google**.
4. Complete OAuth in browser.
5. Observe console:
   - OpenCensus classes loaded
   - DB lookup/create messages
   - UI transition logs
6. Confirm:
   - No errors in console
   - Login window closes
   - Dashboard with examples menu is visible and interactive.


---

After these steps, your application will:
- Include all required dependencies (OpenCensus)
- Persist users in the database
- Seamlessly transition from login to dashboard

🎉 **Success!** Enjoy your fully functional authentication-to-dashboard flow. 🚀
