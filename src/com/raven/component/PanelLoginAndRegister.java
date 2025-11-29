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

    // Remove static variables to prevent race conditions
    private volatile boolean oauthInProgress = false; // Instance-level flag
    private volatile boolean dashboardOpened = false; // Instance-level flag
    private GoogleAuthService currentAuthService = null; // Track current auth service
    
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
        cmd.setFont(new Font("sansserif", Font.BOLD, 14)); // Explicit bold font for register button
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

        // Add Google Sign-In button for registration
        GoogleSignInButton googleBtn = new GoogleSignInButton();
        googleBtn.setText("Sign up with Google");
        googleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGoogleSignIn();
            }
        });
        register.add(googleBtn, "w 70%, h 50, gaptop 20");
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
        JLabel labelDescription = new JLabel("Acceso con tu Cuenta de Google");
        labelDescription.setFont(new Font("sansserif", 1, 18));
        labelDescription.setForeground(new Color(29, 99, 81));
        login.add(labelDescription);
        
        // Comment out email/password fields to focus on Google authentication
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
        cmd.setFont(new Font("sansserif", Font.BOLD, 14)); // Explicit bold font for login button
        cmd.setText("SIGN IN");
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin(txtEmail.getText().trim(), String.valueOf(txtPass.getPassword()));
            }
        });
        login.add(cmd, "w 40%, h 40");
        
        // Centered Google Sign-In Button with enhanced styling
        GoogleSignInButton googleBtn = new GoogleSignInButton();
        googleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGoogleSignIn();
            }
        });
        login.add(googleBtn, "w 70%, h 50, gaptop 20");
        
        // Add informational label
        JLabel infoLabel = new JLabel("Utiliza tu cuenta institucional para acceder");
        infoLabel.setFont(new Font("sansserif", Font.ITALIC, 14));
        infoLabel.setForeground(new Color(100, 100, 100));
        login.add(infoLabel, "gaptop 10");
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
        
        // Cleanup any previous auth service
        if (currentAuthService != null) {
            System.out.println("🧹 Cleaning up previous OAuth service...");
            currentAuthService.cleanup();
            currentAuthService = null;
        }
        
        oauthInProgress = true;
        System.out.println("🚀 Starting fresh Google OAuth flow...");
        
        SwingWorker<GoogleAuthService.GoogleUserInfo, Void> worker = new SwingWorker<GoogleAuthService.GoogleUserInfo, Void>() {
            @Override
            protected GoogleAuthService.GoogleUserInfo doInBackground() throws Exception {
                try {
                    System.out.println("🚀 Starting Google OAuth flow...");
                    
                    // Check if OAuth is properly configured first
                    if (!OAuthConfig.isConfigured()) {
                        System.out.println("⚠️ OAuth not properly configured - missing valid Client ID");
                        OAuthConfig.printDiagnostics();
                        System.out.println("💡 Please configure oauth.properties with your Google OAuth credentials");
                        System.out.println("📖 See GOOGLE_OAUTH_SETUP_GUIDE.md for detailed setup instructions");
                        
                        // Inform user about the missing configuration and stop the process
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(PanelLoginAndRegister.this, 
                                "Google OAuth is not configured properly.\n\n" +
                                "To use Google Sign-In:\n" +
                                "1. Run setup-oauth.bat for guided setup\n" +
                                "2. Get OAuth credentials from Google Cloud Console\n" +
                                "3. Update oauth.properties with your Client ID\n" +
                                "4. Restart the application\n\n" +
                                "See GOOGLE_OAUTH_SETUP_GUIDE.md for details.", 
                                "OAuth Configuration Required", 
                                JOptionPane.ERROR_MESSAGE);
                        });
                        
                        // Throw exception instead of returning demo user
                        throw new RuntimeException("OAuth not configured properly. Please set up oauth.properties with valid credentials.");
                    }
                    
                    // Initialize the Google Auth Service with real OAuth
                    String clientSecret = OAuthConfig.getClientSecret();
                    System.out.println("🔧 Using client secret: " + (clientSecret.isEmpty() ? "EMPTY (Desktop App Mode)" : "PROVIDED (Web App Mode)"));
                    
                    currentAuthService = new GoogleAuthService(
                        OAuthConfig.getClientId(), 
                        clientSecret
                    );
                    
                    // Start OAuth authorization flow
                    System.out.println("🚀 Starting OAuth authorization...");
                    GoogleAuthService.GoogleUserInfo userInfo = currentAuthService.authorize();
                    
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
                    
                    // Cleanup auth service
                    if (currentAuthService != null) {
                        currentAuthService.cleanup();
                        currentAuthService = null;
                    }
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
                // Process OAuth user with database operations
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
        // Map to internal User model and open MainSelectionFrame
        com.raven.model.User user = new com.raven.model.User(userInfo.getEmail(), userInfo.getName());
        com.raven.ui.MainSelectionFrame mainSelection = new com.raven.ui.MainSelectionFrame(user);
        mainSelection.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        mainSelection.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        mainSelection.setVisible(true);
        System.out.println("✅ MainSelectionFrame opened for: " + userInfo.getEmail());
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
     * Reset OAuth state for a fresh login session.
     */
    public void resetOAuthState() {
        oauthInProgress = false;
        dashboardOpened = false;
        
        // Cleanup auth service
        if (currentAuthService != null) {
            currentAuthService.cleanup();
            currentAuthService = null;
        }
    }

}
