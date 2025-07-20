package com.raven.component;

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
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    GoogleAuthService authService = new GoogleAuthService();
                    authService.initiateOAuth();
                    
                    // For demo purposes, simulate a successful Google auth
                    Thread.sleep(2000);
                    GoogleAuthService.GoogleUserInfo userInfo = authService.getMockUserInfo();
                    
                    Optional<User> existingUser = UserDAO.findByEmail(userInfo.getEmail());
                    User user;
                    
                    if (existingUser.isPresent()) {
                        user = existingUser.get();
                        // Update Google sub if not set
                        if (user.getGoogleSub() == null) {
                            user.setGoogleSub(userInfo.getSub());
                            UserDAO.updateUser(user);
                        }
                    } else {
                        // Create new user
                        user = new User(userInfo.getEmail(), userInfo.getName());
                        user.setGoogleSub(userInfo.getSub());
                        UserDAO.createUser(user);
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        DashboardFrame.showFor(user);
                        javax.swing.SwingUtilities.getWindowAncestor(PanelLoginAndRegister.this).dispose();
                    });
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelLoginAndRegister.this, "Google sign-in error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
        };
        worker.execute();
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
