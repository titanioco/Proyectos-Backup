package com.raven.component;

import com.raven.config.OAuthConfig;
import com.raven.model.ModelUser;
import com.raven.model.User;
import com.raven.model.UserDAO;
import com.raven.service.GoogleAuthService;
import com.raven.swing.Button;
import com.raven.swing.MyPasswordField;
import com.raven.swing.MyTextField;
import com.raven.ui.DashboardFrame;
import com.raven.ui.GoogleSignInButton;
import com.raven.ui.PasswordStrengthLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Optional;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
// ...existing imports at top...
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;

public class PanelLoginAndRegister extends javax.swing.JLayeredPane {

    private static boolean oauthInProgress = false; // Prevent multiple OAuth attempts
    // Prevent opening multiple dashboard instances
    private static boolean dashboardOpened = false;
    public ModelUser getUser() {
        return user;
    }
    private ModelUser user;
    
    public PanelLoginAndRegister(ActionListener eventRegister) {
        initComponents();
        initRegister(eventRegister);
        initLogin();
        login.setVisible(false);
        register.setVisible(true);
    }

    private void initRegister(ActionListener eventRegister) {
        register.setLayout(new MigLayout("wrap", "push[center]push", "push[]25[]10[]10[]5[]10[]25[]push"));
        JLabel label = new JLabel("Crear Cuenta");
        label.setFont(new Font("sansserif", 1, 30));
        label.setForeground(new Color(7, 164, 121));
        register.add(label);
        MyTextField txtUser = new MyTextField();
        txtUser.setPrefixIcon(new ImageIcon(getClass().getResource("/com/raven/icon/user.png")));
        txtUser.setHint("Name");
        register.add(txtUser, "w 60%");
        MyTextField txtEmail = new MyTextField();
        txtEmail.setPrefixIcon(new ImageIcon(getClass().getResource("/com/raven/icon/mail.png")));
        txtEmail.setHint("Email");
        register.add(txtEmail, "w 60%");
        MyPasswordField txtPass = new MyPasswordField();
        txtPass.setPrefixIcon(new ImageIcon(getClass().getResource("/com/raven/icon/pass.png")));
        txtPass.setHint("Password");
        register.add(txtPass, "w 60%");
        
        // Add password strength indicator
        PasswordStrengthLabel strengthLabel = new PasswordStrengthLabel();
        register.add(strengthLabel, "w 60%");
        
        // Add password strength checking
        txtPass.addCaretListener(e -> {
            strengthLabel.updateStrength(String.valueOf(txtPass.getPassword()));
        });
        
        Button cmd = new Button();
        cmd.setBackground(new Color(7, 164, 121));
        cmd.setForeground(new Color(250, 250, 250));
        cmd.addActionListener(eventRegister);
        cmd.setText("SIGN UP");
        register.add(cmd, "w 40%, h 40");
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String userName = txtUser.getText().trim();
                String email = txtEmail.getText().trim();
                String password = String.valueOf(txtPass.getPassword());
                user = new ModelUser(0, userName, email, password);
            }
        });        

    }

    private void initLogin() {
        login.setLayout(new MigLayout("wrap", "push[center]push", "push[]25[]10[]10[]10[]10[]10[]push"));
        JLabel label = new JLabel("Universidad Nacional");
        label.setFont(new Font("sansserif", 1, 40));
        label.setForeground(new Color(7, 163, 15));
        login.add(label);
        JLabel labelLogo = new JLabel("Proyectos Universitarios UN");
        labelLogo.setFont(new Font("sansserif", 1, 30));
        labelLogo.setForeground(new Color(7, 163, 121));
        login.add(labelLogo);
        JLabel labelDescription = new JLabel("Registrate con tu Correo Institucional");
        labelDescription.setFont(new Font("sansserif", 1, 15));
        labelDescription.setForeground(new Color(29, 99, 81));
        login.add(labelDescription);
        MyTextField txtEmail = new MyTextField();
        txtEmail.setPrefixIcon(new ImageIcon(getClass().getResource("/com/raven/icon/mail.png")));
        txtEmail.setHint("Email");
        login.add(txtEmail, "w 60%");
        MyPasswordField txtPass = new MyPasswordField();
        txtPass.setPrefixIcon(new ImageIcon(getClass().getResource("/com/raven/icon/pass.png")));
        txtPass.setHint("Password");
        login.add(txtPass, "w 60%");
        JButton cmdForget = new JButton("Olvidaste tu Contraseña ?");
        cmdForget.setForeground(new Color(100, 100, 100));
        cmdForget.setFont(new Font("sansserif", 1, 12));
        cmdForget.setContentAreaFilled(false);
        cmdForget.setCursor(new Cursor(Cursor.HAND_CURSOR));

        login.add(cmdForget);
        Button cmd = new Button();
        cmd.setBackground(new Color(29, 99, 81));
        cmd.setForeground(new Color(250, 250, 250));
        cmd.setText("SIGN IN");
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin(txtEmail.getText().trim(), String.valueOf(txtPass.getPassword()));
            }
        });
        login.add(cmd, "w 40%, h 40");
        
        // Add Google Sign-In Button
        GoogleSignInButton googleBtn = new GoogleSignInButton();
        googleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGoogleSignIn();
            }
        });
        login.add(googleBtn, "w 60%, h 45");
    }
    
    private void handleLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Optional<User> userOpt = UserDAO.findByEmail(email);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        if (UserDAO.verifyPassword(password, user.getPasswordHash())) {
                            // Login successful
                            SwingUtilities.invokeLater(() -> {
                                DashboardFrame.showFor(user);
                                javax.swing.SwingUtilities.getWindowAncestor(PanelLoginAndRegister.this).dispose();
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(PanelLoginAndRegister.this, "Invalid password", "Error", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(PanelLoginAndRegister.this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelLoginAndRegister.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void handleGoogleSignIn() {
        // Prevent multiple OAuth attempts
        if (oauthInProgress) {
            System.out.println("⚠️ OAuth already in progress, ignoring additional attempts");
            JOptionPane.showMessageDialog(this, 
                "OAuth sign-in is already in progress.\nPlease wait for the current attempt to complete.", 
                "OAuth In Progress", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Stop any existing OAuth servers/handlers first
        System.out.println("🧹 Cleaning up any previous OAuth attempts...");
        // No need to stop any servers - the Google OAuth client library handles this automatically
        
        oauthInProgress = true;
        System.out.println("🚀 Starting fresh Google OAuth flow...");
        
        SwingWorker<GoogleAuthService.GoogleUserInfo, Void> worker = new SwingWorker<GoogleAuthService.GoogleUserInfo, Void>() {
            @Override
            protected GoogleAuthService.GoogleUserInfo doInBackground() throws Exception {
                try {
                    System.out.println("� Starting Google OAuth flow...");
                    
                    // Check if OAuth is properly configured first
                    if (!OAuthConfig.isConfigured()) {
                        System.out.println("⚠️ OAuth not configured, using demo mode");
                        // Demo mode fallback - return mock user
                        return new GoogleAuthService.GoogleUserInfo("demo@example.com", "Demo User", "demo123");
                    }
                    
                    // Initialize the Google Auth Service with real OAuth
                    String clientSecret = OAuthConfig.getClientSecret();
                    System.out.println("🔧 Using client secret: " + (clientSecret.isEmpty() ? "EMPTY (Desktop App Mode)" : "PROVIDED (Web App Mode)"));
                    
                    GoogleAuthService authService = new GoogleAuthService(
                        OAuthConfig.getClientId(), 
                        clientSecret
                    );
                    
                    // Simplified OAuth flow - the library handles everything
                    System.out.println("🚀 Starting OAuth authorization...");
                    GoogleAuthService.GoogleUserInfo userInfo = authService.authorize();
                    
                    System.out.println("✅ OAuth completed successfully for: " + userInfo.getEmail());
                    return userInfo;
                    
                } catch (Exception ex) {
                    System.err.println("❌ Google OAuth error: " + ex.getMessage());
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelLoginAndRegister.this, 
                            "Google sign-in error:\n" + ex.getMessage() + 
                            "\n\nPlease try again or contact support if the problem persists.", 
                            "OAuth Error", JOptionPane.ERROR_MESSAGE);
                    });
                    throw ex;
                }
            }
            
            @Override
            protected void done() {
                try {
                    GoogleAuthService.GoogleUserInfo userInfo = get();
                    if (userInfo != null) {
                        System.out.println("🔄 Processing Google user: " + userInfo.getEmail());
                        SwingUtilities.invokeLater(() -> {
                            processGoogleUser(userInfo);
                            closeLoginAndShowDashboard(userInfo);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error getting OAuth result: " + e.getMessage());
                } finally {
                    System.out.println("🏁 Google OAuth worker completed");
                    oauthInProgress = false;
                }
            }
        };
        worker.execute();
    }
    
    private void processGoogleUser(GoogleAuthService.GoogleUserInfo userInfo) {
        try {
            System.out.println("🔄 Processing Google user: " + userInfo.getEmail());
            
            User user;
            
            try {
                // Try database operations first if OAuth is properly configured
                if (OAuthConfig.isConfigured()) {
                    System.out.println("🔍 Checking for existing user in database...");
                    Optional<User> existingUser = UserDAO.findByEmail(userInfo.getEmail());
                    
                    if (existingUser.isPresent()) {
                        user = existingUser.get();
                        // Update Google sub if not set
                        if (user.getGoogleSub() == null) {
                            user.setGoogleSub(userInfo.getId());
                            UserDAO.updateUser(user);
                        }
                        System.out.println("✅ Existing Google user found and updated: " + user.getEmail());
                    } else {
                        // Create new user
                        user = new User(userInfo.getEmail(), userInfo.getName());
                        user.setGoogleSub(userInfo.getId());
                        UserDAO.createUser(user);
                        System.out.println("✅ New Google user created: " + user.getEmail());
                    }
                } else {
                    // Fallback for demo mode
                    System.out.println("⚠️ Demo mode: using temporary user without database");
                    user = new User(userInfo.getEmail(), userInfo.getName());
                    user.setGoogleSub(userInfo.getId());
                    System.out.println("✓ Temporary user created for demo: " + user.getEmail());
                }
                
            } catch (Throwable dbEx) {
                System.err.println("⚠️ Database or DAO init error, falling back to temporary user: " + dbEx.getMessage());
                // Fallback: create temporary user without database
                user = new User(userInfo.getEmail(), userInfo.getName());
                user.setGoogleSub(userInfo.getId());
                System.out.println("✓ Temporary user created as fallback: " + user.getEmail());
            }
            
            // UI transition is handled by closeLoginAndShowDashboard
            // No additional UI work here
            
        } catch (Exception ex) {
            System.err.println("✗ Error processing Google user: " + ex.getMessage());
            ex.printStackTrace();
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(PanelLoginAndRegister.this, 
                    "Error processing Google user: " + ex.getMessage(), 
                    "Authentication Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    public void showRegister(boolean show) {
        if (show) {
            register.setVisible(true);
            login.setVisible(false);
        } else {
            register.setVisible(false);
            login.setVisible(true);
        }
    }

    /**
     * Closes the login window and opens the dashboard after OAuth.
     */
    private void closeLoginAndShowDashboard(GoogleAuthService.GoogleUserInfo userInfo) {
        if (dashboardOpened) {
            return; // Already opened
        }
        // Locate and hide the login window
        java.awt.Window loginWindow = SwingUtilities.getWindowAncestor(this);
        if (loginWindow != null) {
            loginWindow.dispose();
            System.out.println("✅ Login window closed by helper");
        }
        // Map to internal User model and open Dashboard
        com.raven.model.User user = new com.raven.model.User(userInfo.getEmail(), userInfo.getName());
        com.raven.ui.DashboardFrame dashboard = new com.raven.ui.DashboardFrame(user);
        dashboard.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        dashboard.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        dashboard.setVisible(true);
        System.out.println("✅ Dashboard opened by helper for: " + userInfo.getEmail());
        dashboardOpened = true;
    }


    private void initComponents() {

        login = new javax.swing.JPanel();
        register = new javax.swing.JPanel();

        setLayout(new java.awt.CardLayout());

        login.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout loginLayout = new javax.swing.GroupLayout(login);
        login.setLayout(loginLayout);
        loginLayout.setHorizontalGroup(
            loginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 327, Short.MAX_VALUE)
        );
        loginLayout.setVerticalGroup(
            loginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        add(login, "card3");

        register.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout registerLayout = new javax.swing.GroupLayout(register);
        register.setLayout(registerLayout);
        registerLayout.setHorizontalGroup(
            registerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 327, Short.MAX_VALUE)
        );
        registerLayout.setVerticalGroup(
            registerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        add(register, "card2");
    }

    private javax.swing.JPanel login;
    private javax.swing.JPanel register;

    /**
     * Reset static state for a fresh login session.
     */
    public static void resetOAuthState() {
        oauthInProgress = false;
        dashboardOpened = false;
    }

}
