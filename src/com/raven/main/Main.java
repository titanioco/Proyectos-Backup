package com.raven.main;

import com.raven.component.Message;
import com.raven.component.PanelCover;
import com.raven.component.PanelLoading;
import com.raven.component.PanelLoginAndRegister;
import com.raven.component.PanelVerifyCode;
import com.raven.model.ModelMessage;
import com.raven.model.ModelUser;
import com.raven.model.User;
import com.raven.model.UserDAO;
import com.raven.service.ServiceMail;
import com.raven.ui.DashboardFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Optional;
import java.util.Locale;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;

public class Main extends javax.swing.JFrame {

    private static Main instance; // Static reference to the main window
    private final DecimalFormat df = new DecimalFormat("##0.###", DecimalFormatSymbols.getInstance(Locale.US));
    private MigLayout layout;
    private PanelCover cover;
    private PanelLoading loading; 
    private PanelVerifyCode verifyCode;
    private PanelLoginAndRegister loginAndRegister;
    private boolean isLogin = true;
    private final double addSize = 30;
    private final double coverSize = 40;
    private final double loginSize = 60;

    public Main() {
        initComponents();
        instance = this; // Set the static reference
        init();
    }

    /**
     * Get the main window instance
     */
    public static Main getInstance() {
        return instance;
    }
    
    /**
     * Close the main window from anywhere in the application
     */
    public static void closeMainWindow() {
        if (instance != null) {
            System.out.println("🔒 Closing Main window via static method...");
            SwingUtilities.invokeLater(() -> {
                try {
                    instance.setVisible(false);
                    instance.dispose();
                    System.out.println("✅ Main window closed successfully");
                } catch (Exception e) {
                    System.err.println("❌ Error closing main window: " + e.getMessage());
                    e.printStackTrace();
                    // Force exit as fallback
                    System.exit(0);
                }
            });
        } else {
            System.out.println("⚠️ Main window instance is null, cannot close");
        }
    }

    private void init() {
        layout = new MigLayout("fill, insets 0");
        cover = new PanelCover();
        loading=new PanelLoading();
        verifyCode=new PanelVerifyCode();
        ActionListener eventRegister = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                register();
            }
        };
        
        // Reset OAuth and dashboard state for a fresh login/register session
        PanelLoginAndRegister.resetOAuthState();
        loginAndRegister = new PanelLoginAndRegister(eventRegister);
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                double fractionCover;
                double fractionLogin;
                double size = coverSize;
                if (fraction <= 0.5f) {
                    size += fraction * addSize;
                } else {
                    size += addSize - fraction * addSize;
                }
                if (isLogin) {
                    fractionCover = 1f - fraction;
                    fractionLogin = fraction;
                    if (fraction >= 0.5f) {
                        cover.registerRight(fractionCover * 100);
                    } else {
                        cover.loginRight(fractionLogin * 100);
                    }
                } else {
                    fractionCover = fraction;
                    fractionLogin = 1f - fraction;
                    if (fraction <= 0.5f) {
                        cover.registerLeft(fraction * 100);
                    } else {
                        cover.loginLeft((1f - fraction) * 100);
                    }
                }
                if (fraction >= 0.5f) {
                    loginAndRegister.showRegister(isLogin);
                }
                fractionCover = Double.valueOf(df.format(fractionCover));
                fractionLogin = Double.valueOf(df.format(fractionLogin));
                layout.setComponentConstraints(cover, "width " + size + "%, pos " + fractionCover + "al 0 n 100%");
                layout.setComponentConstraints(loginAndRegister, "width " + loginSize + "%, pos " + fractionLogin + "al 0 n 100%");
                bg.revalidate();
            }

            @Override
            public void end() {
                isLogin = !isLogin;
            }
        };
        Animator animator = new Animator(800, target);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
        animator.setResolution(0);  //  for smooth animation
        bg.setLayout(layout);
        bg.setLayer(loading, JLayeredPane.POPUP_LAYER);
        bg.setLayer(verifyCode, JLayeredPane.POPUP_LAYER);
        bg.add(loading, "pos 0 0 100% 100%");
        bg.add(verifyCode, "pos 0 0 100% 100%");
        bg.add(cover, "width " + coverSize + "%, pos " + (isLogin ? "1al" : "0al") + " 0 n 100%");
        bg.add(loginAndRegister, "width " + loginSize + "%, pos " + (isLogin ? "0al" : "1al") + " 0 n 100%"); //  1al as 100%
        loginAndRegister.showRegister(!isLogin);
        cover.login(isLogin);
        cover.addEvent(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!animator.isRunning()) {
                    animator.start();
                }
            }
        });
        
        // Add verification code event handler
        verifyCode.addEventButtonOK(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleVerificationCode();
            }
        });
        // Add Exit button to the top-right corner and set its layer to always be on top
        com.raven.swing.Button exitButton = new com.raven.swing.Button() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                // Draw a square with rounded corners (12px radius)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        exitButton.setText("X");
        exitButton.setBackground(new java.awt.Color(200, 50, 50));
        exitButton.setForeground(new java.awt.Color(255, 255, 255));
        exitButton.setFont(new java.awt.Font("sansserif", java.awt.Font.BOLD, 18));
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        exitButton.setEffectColor(new java.awt.Color(255,255,255,60)); // subtle white ripple
        exitButton.addActionListener(e -> System.exit(0));
        // Highlight on hover
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                exitButton.setBackground(new java.awt.Color(220, 80, 80));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                exitButton.setBackground(new java.awt.Color(200, 50, 50));
            }
        });
        bg.add(exitButton, "pos 1al 0.01al, w 40!, h 40!");
        bg.setLayer(exitButton, javax.swing.JLayeredPane.DRAG_LAYER);
    }
    private void register(){
        ModelUser modelUser = loginAndRegister.getUser();
        
        new Thread(() -> {
            try {
                loading.setVisible(true);
                
                // Check if user already exists
                Optional<User> existingUser = UserDAO.findByEmail(modelUser.getEmail());
                if (existingUser.isPresent()) {
                    loading.setVisible(false);
                    showMessage(Message.MessageType.ERROR, "Email already exists");
                    return;
                }
                
                // Hash the password and create new user
                String hashedPassword = UserDAO.hashPassword(modelUser.getPassword());
                User newUser = new User(modelUser.getEmail(), hashedPassword, modelUser.getUserName());
                
                // Save user to database
                UserDAO.createUser(newUser);
                
                // Send verification email (mock)
                ModelMessage ms = new ServiceMail().sendMain(modelUser.getEmail(), "123456");
                loading.setVisible(false);
                
                if (ms.isSuccess()) {
                    verifyCode.setVisible(true);
                } else {
                    showMessage(Message.MessageType.ERROR, ms.getMessage());
                }
                
            } catch (SQLException e) {
                loading.setVisible(false);
                showMessage(Message.MessageType.ERROR, "Error creating account: " + e.getMessage());
            }
        }).start();
    }
    
    private void handleVerificationCode() {
        String inputCode = verifyCode.getInputCode();
        
        // For demo purposes, accept any 6-digit code or "123456"
        if (inputCode.equals("123456") || (inputCode.length() == 6 && inputCode.matches("\\d+"))) {
            new Thread(() -> {
                try {
                    loading.setVisible(true);
                    verifyCode.setVisible(false);
                    
                    // Simulate verification delay
                    Thread.sleep(1000);
                    
                    // Get the registered user from the database
                    ModelUser modelUser = loginAndRegister.getUser();
                    Optional<User> userOpt = UserDAO.findByEmail(modelUser.getEmail());
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        loading.setVisible(false);
                        
                        // Open dashboard and close main window
                        SwingUtilities.invokeLater(() -> {
                            DashboardFrame.showFor(user);
                            Main.this.dispose();
                        });
                    } else {
                        loading.setVisible(false);
                        showMessage(Message.MessageType.ERROR, "User not found after verification");
                    }
                    
                } catch (Exception ex) {
                    loading.setVisible(false);
                    showMessage(Message.MessageType.ERROR, "Verification error: " + ex.getMessage());
                }
            }).start();
        } else {
            showMessage(Message.MessageType.ERROR, "Invalid verification code");
        }
    }
    
    private void sendMain(ModelUser user) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loading.setVisible(true);
                ModelMessage ms = new ServiceMail().sendMain(user.getEmail(), user.getVerifyCode());
                if (ms.isSuccess()) {
                    loading.setVisible(false);
                    verifyCode.setVisible(true);
                } else {
                    loading.setVisible(false);
                    showMessage(Message.MessageType.ERROR, ms.getMessage());
                }
            }
        }).start();
    } 
    private void showMessage(Message.MessageType messageType, String message) {
        Message ms = new Message();
        ms.showMessage(messageType, message);
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void begin() {
                if (!ms.isShow()) {
                    bg.add(ms, "pos 0.5al -30", 0); //  Insert to bg fist index 0
                    ms.setVisible(true);
                    bg.repaint();
                }
            }

            @Override
            public void timingEvent(float fraction) {
                float f;
                if (ms.isShow()) {
                    f = 40 * (1f - fraction);
                } else {
                    f = 40 * fraction;
                }
                layout.setComponentConstraints(ms, "pos 0.5al " + (int) (f - 30));
                bg.repaint();
                bg.revalidate();
            }

            @Override
            public void end() {
                if (ms.isShow()) {
                    bg.remove(ms);
                    bg.repaint();
                    bg.revalidate();
                } else {
                    ms.setShow(true);
                }
            }
        };
        Animator animator = new Animator(300, target);
        animator.setResolution(0);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
        animator.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    animator.start();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
        }).start();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 933, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 537, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane bg;
    // End of variables declaration//GEN-END:variables
}
