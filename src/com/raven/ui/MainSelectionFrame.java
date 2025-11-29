package com.raven.ui;

import com.raven.model.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainSelectionFrame extends JFrame {
    
    private JPanel leftPanel;
    private JPanel centerPanel;
    private JPanel rightPanel;
    private JLabel titleLabel;
    private User currentUser;
    
    public MainSelectionFrame() {
        this(null);
    }
    
    public MainSelectionFrame(User user) {
        this.currentUser = user;
        initComponents();
        setupEvents();
        
        // Register with SessionManager for navigation
        com.raven.service.SessionManager.getInstance().setMainSelectionFrame(this);
        
        // Ensure window is properly sized and brought to front
        SwingUtilities.invokeLater(() -> {
            setExtendedState(JFrame.NORMAL);
            toFront();
            requestFocus();
        });
    }
    
    private void initComponents() {
        setTitle("University Project Manager - Main Menu");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed from EXIT_ON_CLOSE
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(245, 245, 245));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 73, 94));
        titlePanel.setPreferredSize(new Dimension(1200, 100));
        
        titleLabel = new JLabel("Universidad Nacional - Proyectos", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        titleLabel.setToolTipText(currentUser != null && currentUser.getUserType() == User.UserType.ADMIN ? 
                                "Click for Admin Panel" : "Welcome to the University Project Manager");
        titlePanel.add(titleLabel);
        
        if (currentUser != null) {
            JLabel userLabel = new JLabel("Welcome: " + currentUser.getFullName(), SwingConstants.RIGHT);
            userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            userLabel.setForeground(Color.WHITE);
            titlePanel.add(userLabel, BorderLayout.EAST);
        }
        
        JPanel selectionContainer = new JPanel();
        selectionContainer.setLayout(new java.awt.GridLayout(1, 3, 30, 0));
        selectionContainer.setBackground(new Color(245, 245, 245));
        selectionContainer.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        leftPanel = createSelectionPanel("BUSINESS ACCOUNTABILITY", 
                                       "Comprehensive Accountability Software Suite for Business Management", 
                                       new Color(52, 152, 219));
        
        centerPanel = createSelectionPanel("CODE EXAMPLES", 
                                         "Data Structures & Algorithms Visualization and Learning Platform", 
                                         new Color(46, 204, 113));
        
        rightPanel = createSelectionPanel("AI ASSISTANT", 
                                        "Get help with transcription, document analysis, and AI-powered assistance", 
                                        new Color(231, 76, 60));
        
        selectionContainer.add(leftPanel);
        selectionContainer.add(centerPanel);
        selectionContainer.add(rightPanel);
        
        mainContainer.add(titlePanel, BorderLayout.NORTH);
        mainContainer.add(selectionContainer, BorderLayout.CENTER);
        
        add(mainContainer);
    }
    
    private void setupEvents() {
        // Admin panel access through title click (admin users only)
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentUser != null && currentUser.getUserType() == User.UserType.ADMIN) {
                    handleAdminPanelAccess();
                }
            }
        });
        
        leftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleLeftPanelClick();
            }
        });
        
        centerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCenterPanelClick();
            }
        });
        
        rightPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleRightPanelClick();
            }
        });
    }
    
    private JPanel createSelectionPanel(String title, String description, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 8, 20, 20);
            }
        };
        
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(350, 600));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(new Color(52, 73, 94));
        
        JLabel iconLabel = new JLabel("[ICON]", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        
        JLabel featuresLabel = new JLabel();
        if (title.contains("BUSINESS ACCOUNTABILITY")) {
            featuresLabel.setText("<html><div style='text-align: left;'>" +
                "• Financial Accounting & Reporting<br>" +
                "• Tax Management & Compliance<br>" +
                "• E-Invoicing & Billing System<br>" +
                "• Audit Trail & Documentation<br>" +
                "• Business Intelligence Dashboard<br>" +
                "• Regulatory Compliance Tools" +
                "</div></html>");
        } else if (title.contains("CODE EXAMPLES")) {
            featuresLabel.setText("<html><div style='text-align: left;'>" +
                "• Binary Search Trees (BST)<br>" +
                "• Graph Algorithms & Visualization<br>" +
                "• Hash Tables & Collision Handling<br>" +
                "• Sorting Algorithms (Heapsort, etc.)<br>" +
                "• AVL Trees & Balancing<br>" +
                "• Dynamic Arrays & Data Structures" +
                "</div></html>");
        } else {
            featuresLabel.setText("<html><div style='text-align: left;'>" +
                "• Voice Transcription<br>" +
                "• Document Analysis<br>" +
                "• Video Summarization<br>" +
                "• File Processing<br>" +
                "• AI Chat Assistant" +
                "</div></html>");
        }
        featuresLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        featuresLabel.setForeground(new Color(52, 73, 94));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(descLabel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(featuresLabel, BorderLayout.SOUTH);
        
        // Add hover effects
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(250, 250, 250));
                panel.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
                panel.repaint();
            }
        });
        
        return panel;
    }
    
    private void handleAdminPanelAccess() {
        System.out.println("ADMIN: Opening admin panel for " + currentUser.getEmail());
        try {
            SwingUtilities.invokeLater(() -> {
                AdminPanel adminPanel = new AdminPanel(currentUser);
                adminPanel.setVisible(true);
                // Don't close main frame - admin panel is a separate window
            });
        } catch (Exception e) {
            System.err.println("ERROR: Failed to open Admin Panel: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Admin Panel is temporarily unavailable.\nPlease try again later.\n\nError: " + e.getMessage(), 
                "Service Unavailable", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void handleLeftPanelClick() {
        System.out.println("CLICK: Left panel clicked - Business Accountability Software");
        
        // Then open the AccountabilityManagementFrame
        try {
            System.out.println("DEBUG: Creating AccountabilityManagementFrame...");
            com.raven.accountability.ui.AccountabilityManagementFrame accountabilityFrame = 
                new com.raven.accountability.ui.AccountabilityManagementFrame(currentUser);
            
            System.out.println("DEBUG: Setting AccountabilityManagementFrame visible...");
            accountabilityFrame.setVisible(true);
            
            System.out.println("DEBUG: Hiding MainSelectionFrame...");
            this.setVisible(false); // Hide instead of dispose to avoid triggering logout
            
            System.out.println("SUCCESS: AccountabilityManagementFrame opened successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to open Accountability Management Frame: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Business Accountability Suite is temporarily unavailable.\n" +
                "Error: " + e.getMessage() + "\n\nPlease try again later.", 
                "Service Unavailable", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void handleCenterPanelClick() {
        System.out.println("CLICK: Center panel clicked - Code Examples");
        try {
            // Open DashboardFrame for code examples
            SwingUtilities.invokeLater(() -> {
                DashboardFrame dashboardFrame = new DashboardFrame(currentUser);
                dashboardFrame.setVisible(true);
                this.setVisible(false); // Hide instead of dispose to avoid triggering logout
            });
        } catch (Exception e) {
            System.err.println("ERROR: Failed to open Code Examples Dashboard: " + e.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Code Examples Dashboard is temporarily unavailable.\nPlease try again later.", 
                "Service Unavailable", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void handleRightPanelClick() {
        System.out.println("CLICK: Right panel clicked - AI Assistant");
        try {
            // Open AI Assistant Frame
            SwingUtilities.invokeLater(() -> {
                com.raven.ui.AIAssistantFrame aiFrame = new com.raven.ui.AIAssistantFrame(currentUser);
                aiFrame.setVisible(true);
                this.setVisible(false); // Hide instead of dispose to avoid triggering logout
            });
        } catch (Exception e) {
            System.err.println("ERROR: Failed to open AI Assistant: " + e.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, 
                "AI Assistant is temporarily unavailable.\nPlease try again later.", 
                "Service Unavailable", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }
    /**
     * Brings the frame to front and restores to normal window size
     * Used when receiving confirmations or alerts
     */
    public void bringToFrontAndMaximize() {
        SwingUtilities.invokeLater(() -> {
            // Close any file explorer windows that might be open
            closeExplorerWindows();
            
            // Restore to normal window size (not maximized to full screen)
            setExtendedState(JFrame.NORMAL);
            // Ensure the window is the correct size
            setSize(1200, 800);
            // Center it on screen
            setLocationRelativeTo(null);
            // Bring to front
            toFront();
            requestFocus();
            // Make sure it's visible
            setVisible(true);
        });
    }
    
    /**
     * Closes only the Google OAuth popup window safely
     */
    private void closeExplorerWindows() {
        try {
            // Only close the specific Google OAuth popup window
            closeGoogleOAuthPopup();
            
            System.out.println("INFO: Google OAuth popup close attempted");
        } catch (Exception e) {
            System.err.println("WARNING: Could not close Google OAuth popup: " + e.getMessage());
        }
    }
    
    /**
     * Safely close only the Google OAuth popup window
     */
    private void closeGoogleOAuthPopup() {
        try {
            // Use very specific window title matching for Google OAuth
            String[] safeCommands = {
                // Close only windows with Google OAuth specific titles
                "powershell -command \"Get-Process | Where-Object {$_.MainWindowTitle -like '*accounts.google.com*' -or $_.MainWindowTitle -like '*Sign in - Google*' -or $_.MainWindowTitle -like '*Google OAuth*'} | ForEach-Object { try { $_.CloseMainWindow() } catch { } }\"",
                // Alternative approach using window class and title
                "powershell -command \"Add-Type -AssemblyName Microsoft.VisualBasic; [Microsoft.VisualBasic.Interaction]::AppActivate('Sign in - Google'); Start-Sleep -Milliseconds 100; Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('%{F4}')\""
            };
            
            for (String command : safeCommands) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", command);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    // Wait maximum 3 seconds for the command to complete
                    if (!process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                        System.out.println("DEBUG: OAuth popup close command timed out (safe)");
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG: Safe OAuth close attempt failed: " + e.getMessage());
                    // Continue to next method - this is non-critical
                }
            }
            
            System.out.println("INFO: Google OAuth popup close attempted safely");
        } catch (Exception e) {
            System.err.println("WARNING: Safe Google OAuth popup close failed: " + e.getMessage());
            // This is non-critical, don't throw
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (titleLabel != null) {
            repaint(); // Refresh the display
        }
    }
}
