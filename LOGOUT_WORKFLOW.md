# Logout Workflow

This guide describes how to implement and document the logout process in the application so that after a user logs out, the OAuth flags and session data are reset. This allows a new user to log in without restarting the entire application.

## 1. Add a Logout Action in the Dashboard

1. In `DashboardFrame`, add a `Logout` button or menu item (e.g., under a `JMenu` named "Account").
2. Attach an `ActionListener` to trigger the logout flow:

```java
// Inside DashboardFrame
JMenuItem logoutMenuItem = new JMenuItem("Logout");
logoutMenuItem.addActionListener(e -> handleLogout());
accountMenu.add(logoutMenuItem);
```

## 2. Implement the `logout()` Method

In `DashboardFrame`, create a `handleLogout()` method that:

   1. Dispose the dashboard window
   2. Reset static OAuth flags
   3. Relaunch the main application (login/register UI)

Example implementation:

```java
private void logout() {
    int result = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Logout Confirmation",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );
    if (result == JOptionPane.YES_OPTION) {
        // Reset OAuth static state
        com.raven.component.PanelLoginAndRegister.resetOAuthState();

        // Close dashboard window
        this.dispose();

        // Restart the application to show login/register UI
        SwingUtilities.invokeLater(() -> {
            try {
                new com.raven.main.Main().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
``` 

## 3. Add a Static Reset Method in `PanelLoginAndRegister`

To reset the OAuth flags and allow new logins:

```java
public class PanelLoginAndRegister extends JLayeredPane {
    private static boolean oauthInProgress = false;
    private static boolean dashboardOpened = false;
    // ... existing code ...

    /**
     * Reset static state for a fresh login session.
     */
    public static void resetOAuthState() {
        oauthInProgress = false;
        dashboardOpened = false;
    }
    
    // ... existing code ...
}
```

## 4. Reset State when Initializing the Login UI

Ensure the static flags are cleared each time the login/register panel is created. In `Main.init()`, call:

```java
// Before creating the login/register panel
PanelLoginAndRegister.resetOAuthState();
loginAndRegister = new PanelLoginAndRegister(eventRegister);
```

This guarantees a fresh state after logout.

## 4. (Optional) Revoke OAuth Tokens

If you want to fully revoke Google OAuth tokens on logout, extend `GoogleAuthService` with a `revoke()` method and call it:

```java
// After resetting local state
if (OAuthConfig.isConfigured()) {
    new GoogleAuthService(...).revoke();
}
```

## 5. Verify the Flow

1. Launch the app and sign in via Google (or email/password).
2. On `DashboardFrame`, click `Logout`.
3. Ensure the dashboard window closes and the login/register UI reappears.
4. Confirm that logging in again (with the same or a different user) works without restarting the app.

---
Document created on July 21, 2025 by the Development Team as part of the OAuth and session management improvements.
