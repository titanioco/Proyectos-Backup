package com.raven.component;

import com.raven.model.ModelUser;
import com.raven.model.User;
import com.raven.model.UserDAO;
import com.raven.service.GoogleAuthService;
import com.raven.service.OAuth2CallbackHandler;
import com.raven.swing.Button;
import com.raven.swing.MyPasswordField;
import com.raven.swing.MyTextField;
import com.raven.ui.DashboardFrame;
import com.raven.ui.GoogleSignInButton;
import com.raven.ui.PasswordStrengthLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Optional;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;

public class PanelLoginAndRegister extends javax.swing.JLayeredPane {

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
        System.out.println("🚀 Starting Google OAuth flow...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    GoogleAuthService authService = new GoogleAuthService();
                    
                    // Start OAuth flow
                    System.out.println("📧 Initiating OAuth with Google...");
                    authService.initiateOAuth();
                    
                    // Check if OAuth is properly configured after initiation
                    if (authService.getCodeVerifier() == null) {
                        System.out.println("⚠️ OAuth not configured or user dismissed setup, using demo mode");
                        // Demo mode fallback
                        Thread.sleep(1000); // Simulate processing time
                        GoogleAuthService.GoogleUserInfo userInfo = authService.getMockUserInfo();
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("🎮 Using demo mode with mock user: " + userInfo.getEmail());
                            processGoogleUser(userInfo);
                        });
                        return null;
                    }
                    
                    // Real OAuth flow - start callback server
                    System.out.println("🌐 Starting OAuth callback server...");
                    OAuth2CallbackHandler callbackHandler = new OAuth2CallbackHandler(
                        authService, 
                        authService.getState()
                    );
                    
                    callbackHandler.startServer();
                    System.out.println("✓ OAuth callback server running on port 8080");
                    
                    // Wait for OAuth callback (with timeout)
                    System.out.println("⏳ Waiting for Google OAuth callback...");
                    GoogleAuthService.GoogleUserInfo userInfo = callbackHandler.getUserInfoFuture()
                        .get(5, java.util.concurrent.TimeUnit.MINUTES); // 5 minute timeout
                    
                    System.out.println("✓ OAuth callback received, processing user...");
                    SwingUtilities.invokeLater(() -> processGoogleUser(userInfo));
                    
                } catch (java.util.concurrent.TimeoutException e) {
                    System.err.println("⏰ Google OAuth timed out");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelLoginAndRegister.this, 
                            "Google sign-in timed out after 5 minutes.\n\nPlease try again or check your internet connection.", 
                            "OAuth Timeout", JOptionPane.WARNING_MESSAGE);
                    });
                } catch (Exception ex) {
                    System.err.println("❌ Google OAuth error: " + ex.getMessage());
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelLoginAndRegister.this, 
                            "Google sign-in error:\n" + ex.getMessage() + 
                            "\n\nPlease try again or contact support if the problem persists.", 
                            "OAuth Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
            
            @Override
            protected void done() {
                System.out.println("🏁 Google OAuth worker completed");
            }
        };
        worker.execute();
    }
    
    private void processGoogleUser(GoogleAuthService.GoogleUserInfo userInfo) {
        try {
            System.out.println("🔄 Processing Google user: " + userInfo.getEmail());
            
            User user;
            
            try {
                // For demo mode, skip database and create temporary user
                System.out.println("⚠️ Demo mode: using temporary user without database");
                user = new User(userInfo.getEmail(), userInfo.getName());
                user.setGoogleSub(userInfo.getSub());
                System.out.println("✓ Temporary user created for demo: " + user.getEmail());
                
                // TODO: Enable database operations when SLF4J is available
                /*
                // Try database operations first
                Optional<User> existingUser = UserDAO.findByEmail(userInfo.getEmail());
                
                if (existingUser.isPresent()) {
                    user = existingUser.get();
                    // Update Google sub if not set
                    if (user.getGoogleSub() == null) {
                        user.setGoogleSub(userInfo.getSub());
                        UserDAO.updateUser(user);
                    }
                    System.out.println("✓ Existing Google user updated: " + user.getEmail());
                } else {
                    // Create new user
                    user = new User(userInfo.getEmail(), userInfo.getName());
                    user.setGoogleSub(userInfo.getSub());
                    UserDAO.createUser(user);
                    System.out.println("✓ New Google user created: " + user.getEmail());
                }
                */
            } catch (Exception dbEx) {
                System.err.println("⚠️ Database error, using temporary user: " + dbEx.getMessage());
                // Fallback: create temporary user without database
                user = new User(userInfo.getEmail(), userInfo.getName());
                user.setGoogleSub(userInfo.getSub());
                System.out.println("✓ Temporary user created for demo: " + user.getEmail());
            }
            
            // Ensure UI updates happen in EDT
            final User finalUser = user;
            SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("🚀 Opening Interactive Data Structures Learning Suite...");
                    
                    // Get the main window to close
                    java.awt.Window window = SwingUtilities.getWindowAncestor(PanelLoginAndRegister.this);
                    
                    // Open the dashboard first
                    DashboardFrame.showFor(finalUser);
                    
                    // Then close the login window
                    if (window != null) {
                        window.dispose();
                        System.out.println("✓ Login window closed");
                    }
                    
                    System.out.println("✅ Dashboard opened successfully for: " + finalUser.getFullName());
                    
                } catch (Exception uiEx) {
                    System.err.println("❌ Error opening dashboard: " + uiEx.getMessage());
                    uiEx.printStackTrace();
                    
                    JOptionPane.showMessageDialog(PanelLoginAndRegister.this, 
                        "Error opening Dashboard:\n" + uiEx.getMessage() + 
                        "\n\nPlease check the console for details.", 
                        "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
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

    @SuppressWarnings("unchecked")

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

}
