# Post-OAuth Final Step: Minimize Login and Open Dashboard

After the user grants consent in the browser, the application should:

1. Minimize or close the login window.
2. Launch the main Data Structures Learning Suite dashboard.

This document walks through the final implementation details and highlights the critical code changes.

---

## 1. Ensure JSON parsing for userinfo

The NullPointerException during `response.parseAs(Map.class)` can happen when no parser is set. In `GoogleAuthService.java`, update the request factory:

```java
// ... existing imports ...
import com.google.api.client.json.JsonObjectParser;

// Inside authorize():
HttpRequestFactory requestFactory = HTTP.createRequestFactory(request -> {
    request.getHeaders().setAuthorization("Bearer " + credential.getAccessToken());
    // Set JSON parser for parsing the userinfo response
    request.setParser(new JsonObjectParser(JSON));
});
```

---

## 2. Add a helper to close or minimize the login UI and open dashboard

In `PanelLoginAndRegister.java`, after successful OAuth in `done()` or `processGoogleUser()`, invoke a helper that handles the window transition:

```java
// In PanelLoginAndRegister class
private void closeLoginAndShowDashboard(GoogleAuthService.GoogleUserInfo userInfo) {
    // Locate and hide the login window
    Window loginWindow = SwingUtilities.getWindowAncestor(this);
    if (loginWindow != null) {
        loginWindow.setVisible(false);
        loginWindow.dispose();
    }

    // Create and display the dashboard
    DashboardFrame dashboard = new DashboardFrame(userInfo);
    dashboard.setTitle("Data Structures Learning Suite - " + userInfo.getName());
    dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
    dashboard.setVisible(true);
    dashboard.toFront();
}
```

Then call this helper right after processing the `userInfo`:

```java
// After processGoogleUser(userInfo);
closeLoginAndShowDashboard(userInfo);
```

---

## 3. Verify `DashboardFrame` constructor

Ensure `DashboardFrame` has a constructor that accepts `GoogleUserInfo` or your `User` model:

```java
public class DashboardFrame extends JFrame {

    public DashboardFrame(GoogleAuthService.GoogleUserInfo userInfo) {
        initComponents();
        // Use userInfo for personalization, e.g. welcome label
        welcomeLabel.setText("Welcome, " + userInfo.getName() + "!");
    }
    // ... rest of implementation ...
}
```

---

## 4. Testing the final flow

1. Clean and recompile the application:

   ```powershell
   ant clean; ant compile; ant run
   ```

2. Click the Google Sign-In button.
3. Complete the browser consent.
4. The login window should close, and the dashboard opens maximized.

---

With these changes in place, the NullPointerException will be resolved and the post-authentication UI transition will work as expected.
