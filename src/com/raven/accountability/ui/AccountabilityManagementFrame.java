package com.raven.accountability.ui;

import com.raven.model.User;
import com.raven.accountability.ui.components.*;
import com.raven.accountability.ui.components.BillingAndInvoicingPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main Accountability Management System Frame
 * This serves as the central hub for all business accountability modules
 * including financial management, compliance, reporting, and audit systems.
 */
public class AccountabilityManagementFrame extends JFrame {
    private User currentUser;
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;
    private CardLayout cardLayout;
    
    // Module panels - using JPanel for fallback compatibility
    private JPanel dashboardPanel;
    private JPanel financialPanel;
    private JPanel compliancePanel;
    private JPanel reportingPanel;
    private JPanel auditPanel;
    private BillingAndInvoicingPanel billingPanel;
    private JPanel taxPanel;
    
    public AccountabilityManagementFrame() {
        this(null);
    }
    
    public AccountabilityManagementFrame(User user) {
        this.currentUser = user;
        try {
            System.out.println("INIT: Starting AccountabilityManagementFrame initialization...");
            initComponents();
            setupLayout();
            setupEventHandlers();
            
            // Ensure we show the dashboard even if other panels failed
            SwingUtilities.invokeLater(() -> {
                try {
                    showDashboard(); // Default view
                    System.out.println("SUCCESS: Dashboard displayed as default view");
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to show dashboard: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            System.out.println("SUCCESS: AccountabilityManagementFrame initialized successfully");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Failed to initialize AccountabilityManagementFrame: " + e.getMessage());
            e.printStackTrace();
            // Initialize a basic fallback interface
            initializeFallbackInterface();
        }
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Business Accountability Suite - " + (currentUser != null ? currentUser.getFullName() : "Guest"));
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(1200, 700));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content area with sidebar
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create sidebar
        sidebarPanel = createSidebarPanel();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Create main content panel with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(new Color(248, 249, 250));
        
        // Initialize module panels
        initializeModulePanels();
        
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        
        // Create footer panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        // Title and logo area
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("BUSINESS ACCOUNTABILITY SUITE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Comprehensive Business Management & Compliance");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        
        titlePanel.add(titleContainer);
        
        // User info and navigation
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        if (currentUser != null) {
            JLabel userLabel = new JLabel("Welcome: " + currentUser.getFullName());
            userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            userLabel.setForeground(Color.WHITE);
            userPanel.add(userLabel);
            
            // Add some spacing
            userPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        }
        
        JButton backButton = new JButton("← Main Menu");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backButton.setBackground(new Color(41, 128, 185));
        backButton.setForeground(Color.WHITE);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        backButton.addActionListener(e -> returnToMainMenu());
        userPanel.add(backButton);
        
        if (currentUser != null) {
            // Add some spacing
            userPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            
            // Logout button
            JButton logoutButton = new JButton(" Logout");
            logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            logoutButton.setBackground(new Color(231, 76, 60));
            logoutButton.setForeground(Color.WHITE);
            logoutButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            logoutButton.setFocusPainted(false);
            logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            logoutButton.setToolTipText("Logout and return to login screen");
            
            logoutButton.addActionListener(e -> handleLogout());
            userPanel.add(logoutButton);
        }
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Sidebar title
        JLabel sidebarTitle = new JLabel("MODULES", SwingConstants.CENTER);
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sidebarTitle.setForeground(new Color(189, 195, 199));
        sidebarTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 10));
        sidebar.add(sidebarTitle);
        
        // Navigation buttons
        sidebar.add(createNavButton(" Dashboard", "dashboard", true));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createNavButton(" Financial Accounting", "financial", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createNavButton(" Compliance Management", "compliance", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createNavButton(" Reporting & Analytics", "reporting", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createNavButton(" Audit Trail", "audit", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createNavButton(" Billing & Invoicing", "billing", false));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(createNavButton(" Tax Management", "tax", false));
        
        sidebar.add(Box.createVerticalGlue());
        
        // Siigo Login button
        JButton siigoButton = new JButton("🔗 Siigo Login");
        siigoButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        siigoButton.setHorizontalAlignment(SwingConstants.CENTER);
        siigoButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        siigoButton.setMaximumSize(new Dimension(220, 40));
        siigoButton.setFocusPainted(false);
        siigoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        siigoButton.setBackground(new Color(46, 204, 113));
        siigoButton.setForeground(Color.WHITE);
        siigoButton.setToolTipText("Open Siigo login page in browser");
        
        siigoButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://account.siigo.com/siigob2cco.onmicrosoft.com/b2c_1a_col_pd_ssosiigo/oauth2/v2.0/authorize?client_id=c0f95d00-a5b7-4cfc-a84c-7fc1be2a6720&redirect_uri=https%3A%2F%2Fsiigonube.siigo.com%2Fopenid-callback&response_type=code&scope=openid+profile+https%3A%2F%2Fsiigob2cco.onmicrosoft.com%2Fshell-pd-col%2Fbasic+offline_access&state=5b95a3da80174ef388d362dc0e46310d&code_challenge=BAd1vV8wSQSZGMfTb6bRcwjZr8PW35fxMFoGORuf9JI&code_challenge_method=S256&response_mode=fragment"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Could not open browser. Please visit Siigo login page manually.",
                    "Browser Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(siigoButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // LEVVY Login button at the bottom
        JButton levvyButton = new JButton("🔗 LEVVY Login");
        levvyButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        levvyButton.setHorizontalAlignment(SwingConstants.CENTER);
        levvyButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        levvyButton.setMaximumSize(new Dimension(220, 40));
        levvyButton.setFocusPainted(false);
        levvyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        levvyButton.setBackground(new Color(52, 152, 219));
        levvyButton.setForeground(Color.WHITE);
        levvyButton.setToolTipText("Open LEVVY login page in browser");
        
        levvyButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://app.levvy.com/login"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Could not open browser. Please visit: https://app.levvy.com/login",
                    "Browser Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        sidebar.add(levvyButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        return sidebar;
    }
    
    private JButton createNavButton(String text, String cardName, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        button.setMaximumSize(new Dimension(220, 40));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (selected) {
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(44, 62, 80));
            button.setForeground(new Color(189, 195, 199));
        }
        
        button.addActionListener(e -> switchToModule(cardName, button));
        
        return button;
    }
    
    private void initializeModulePanels() {
        // Initialize all module panels with comprehensive error handling
        System.out.println("INIT: Starting module panel initialization...");
        
        // Dashboard Panel - Critical component, must work
        try {
            dashboardPanel = new DashboardPanel(currentUser);
            System.out.println("SUCCESS: Dashboard panel initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize dashboard panel: " + e.getMessage());
            e.printStackTrace();
            dashboardPanel = createFallbackDashboard();
        }
        
        // Financial Panel - Initialize with error handling
        try {
            financialPanel = new FinancialAccountingPanel(currentUser);
            System.out.println("SUCCESS: Financial panel initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize financial panel: " + e.getMessage());
            financialPanel = createPlaceholderPanel("Financial Accounting");
        }
        
        // Compliance Panel - Initialize with error handling
        try {
            compliancePanel = new CompliancePanel(currentUser);
            System.out.println("SUCCESS: Compliance panel initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize compliance panel: " + e.getMessage());
            compliancePanel = createPlaceholderPanel("Compliance");
        }
        
        // Reporting Panel - Initialize with error handling
        try {
            reportingPanel = new ReportingPanel(currentUser);
            System.out.println("SUCCESS: Reporting panel initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize reporting panel: " + e.getMessage());
            reportingPanel = createPlaceholderPanel("Reporting");
        }
        
        // Audit Panel - Initialize with error handling
        try {
            auditPanel = new AuditTrailPanel(currentUser);
            System.out.println("SUCCESS: Audit panel initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize audit panel: " + e.getMessage());
            auditPanel = createPlaceholderPanel("Audit Trail");
        }
        
        // Billing Panel - Initialize with error handling to prevent crashing
        try {
            System.out.println("INFO: Attempting to initialize billing panel...");
            billingPanel = new BillingAndInvoicingPanel(currentUser);
            System.out.println("SUCCESS: Billing and Invoicing panel initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize billing panel (Database issue): " + e.getMessage());
            System.err.println("FALLBACK: Creating placeholder billing panel");
            e.printStackTrace();
            // Create a placeholder panel instead of crashing
            billingPanel = null; // Will be handled in the card layout section
        }
        
        // Tax Panel - Initialize with error handling
        try {
            taxPanel = new TaxManagementPanel(currentUser);
            System.out.println("SUCCESS: Tax panel initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize tax panel (Database issue): " + e.getMessage());
            System.err.println("FALLBACK: Creating placeholder tax panel");
            taxPanel = createPlaceholderPanel("Tax Management");
        }
        
        // Add panels to card layout with null checks
        try {
            if (dashboardPanel != null) {
                mainContentPanel.add(dashboardPanel, "dashboard");
                System.out.println("LAYOUT: Dashboard panel added to layout");
            }
            if (financialPanel != null) {
                mainContentPanel.add(financialPanel, "financial");
                System.out.println("LAYOUT: Financial panel added to layout");
            }
            if (compliancePanel != null) {
                mainContentPanel.add(compliancePanel, "compliance");
                System.out.println("LAYOUT: Compliance panel added to layout");
            }
            if (reportingPanel != null) {
                mainContentPanel.add(reportingPanel, "reporting");
                System.out.println("LAYOUT: Reporting panel added to layout");
            }
            if (auditPanel != null) {
                mainContentPanel.add(auditPanel, "audit");
                System.out.println("LAYOUT: Audit panel added to layout");
            }
            if (billingPanel != null) {
                mainContentPanel.add(billingPanel, "billing");
                System.out.println("LAYOUT: Billing panel added to layout");
            } else {
                // Add placeholder if billing panel failed to initialize
                JPanel billingPlaceholder = createPlaceholderPanel("Billing & Invoicing");
                mainContentPanel.add(billingPlaceholder, "billing");
                System.out.println("LAYOUT: Billing placeholder panel added to layout");
            }
            if (taxPanel != null) {
                mainContentPanel.add(taxPanel, "tax");
                System.out.println("LAYOUT: Tax panel added to layout");
            }
            System.out.println("SUCCESS: All available panels added to layout");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to add panels to layout: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(236, 240, 241));
        footerPanel.setPreferredSize(new Dimension(0, 40));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        JLabel footerLabel = new JLabel("Business Accountability Suite v1.0 - Comprehensive Management System");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(127, 140, 141));
        
        JLabel statusLabel = new JLabel("System Status: Active");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(39, 174, 96));
        
        footerPanel.add(footerLabel, BorderLayout.WEST);
        footerPanel.add(statusLabel, BorderLayout.EAST);
        
        return footerPanel;
    }
    
    private void setupEventHandlers() {
        // Window closing event
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = JOptionPane.showConfirmDialog(
                    AccountabilityManagementFrame.this,
                    "Are you sure you want to exit Business Accountability Suite?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    private void switchToModule(String moduleName, JButton selectedButton) {
        try {
            // Verify the panel exists before trying to show it
            Component[] cards = mainContentPanel.getComponents();
            boolean moduleExists = false;
            for (Component card : cards) {
                String name = null;
                if (card == dashboardPanel) name = "dashboard";
                else if (card == financialPanel) name = "financial";
                else if (card == compliancePanel) name = "compliance";
                else if (card == reportingPanel) name = "reporting";
                else if (card == auditPanel) name = "audit";
                else if (card == billingPanel) name = "billing";
                else if (card == taxPanel) name = "tax";
                
                if (moduleName.equals(name)) {
                    moduleExists = true;
                    break;
                }
            }
            
            if (!moduleExists) {
                System.err.println("WARNING: Module " + moduleName + " not found, defaulting to dashboard");
                moduleName = "dashboard";
            }
            
            cardLayout.show(mainContentPanel, moduleName);
            
            // Update button states
            Component[] components = sidebarPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if (btn == selectedButton) {
                        btn.setBackground(new Color(52, 152, 219));
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setBackground(new Color(44, 62, 80));
                        btn.setForeground(new Color(189, 195, 199));
                    }
                }
            }
            
            System.out.println("NAVIGATION: Successfully switched to module - " + moduleName);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to switch to module " + moduleName + ": " + e.getMessage());
            e.printStackTrace();
            // Fallback to dashboard
            try {
                cardLayout.show(mainContentPanel, "dashboard");
                System.out.println("FALLBACK: Switched to dashboard due to navigation error");
            } catch (Exception fallbackError) {
                System.err.println("CRITICAL: Even dashboard fallback failed: " + fallbackError.getMessage());
            }
        }
    }
    
    private void showDashboard() {
        try {
            cardLayout.show(mainContentPanel, "dashboard");
            
            // Ensure the dashboard button is selected in the sidebar
            Component[] components = sidebarPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    String text = btn.getText();
                    if (text != null && text.contains("Dashboard")) {
                        btn.setBackground(new Color(52, 152, 219));
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setBackground(new Color(44, 62, 80));
                        btn.setForeground(new Color(189, 195, 199));
                    }
                }
            }
            
            System.out.println("SUCCESS: Dashboard displayed successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to show dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void returnToMainMenu() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Return to main menu? Any unsaved changes will be lost.",
            "Return to Main Menu",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(() -> {
                this.dispose();
                // Use SessionManager to return to existing MainSelectionFrame
                com.raven.service.SessionManager.getInstance().showMainSelectionFrame();
            });
        }
    }
    
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?\nAny unsaved changes will be lost.",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Clear user session
            this.currentUser = null;
            
            // Show logout message
            JOptionPane.showMessageDialog(
                this,
                "You have been successfully logged out.\nReturning to login screen.",
                "Logout Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Close current window
            dispose();
            
            // Use SessionManager to return to original login screen
            com.raven.service.SessionManager.getInstance().logout();
            System.out.println("LOGOUT: User logged out successfully");
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    private void initializeFallbackInterface() {
        // Clear any existing components
        getContentPane().removeAll();
        
        setLayout(new BorderLayout());
        setTitle("Business Accountability Suite - Safe Mode");
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🏢 Business Accountability Suite - Safe Mode");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JButton backButton = new JButton("← Back to Main Menu");
        backButton.setBackground(new Color(231, 76, 60));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            dispose();
            // Use SessionManager to return to existing MainSelectionFrame
            com.raven.service.SessionManager.getInstance().showMainSelectionFrame();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        JLabel errorTitleLabel = new JLabel("⚠️ Database Initialization Error");
        errorTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        errorTitleLabel.setForeground(new Color(231, 76, 60));
        
        JLabel errorMessageLabel = new JLabel("<html><center>" +
            "The Business Accountability Suite encountered a database initialization error.<br><br>" +
            "<b>Issue:</b> SQLite library version incompatibility<br>" +
            "<b>Solution:</b> Please check the following:<br><br>" +
            "• Ensure the SQLite JDBC driver version is compatible<br>" +
            "• Verify SLF4J library versions are consistent<br>" +
            "• Check that the database file permissions are correct<br><br>" +
            "The system is running in Safe Mode with limited functionality." +
            "</center></html>");
        errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorMessageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorMessageLabel.setForeground(new Color(52, 73, 94));
        
        // Add dashboard panel that works without database
        JPanel safeDashboardPanel = createSafeDashboardPanel();
        
        messagePanel.add(errorTitleLabel);
        messagePanel.add(Box.createVerticalStrut(20));
        messagePanel.add(errorMessageLabel);
        messagePanel.add(Box.createVerticalStrut(30));
        messagePanel.add(safeDashboardPanel);
        
        contentPanel.add(messagePanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
    
    private JPanel createSafeDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219)),
            "Safe Mode Dashboard",
            0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 152, 219)
        ));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Safe mode features
        JLabel featuresLabel = new JLabel("<html>" +
            "<b>Available in Safe Mode:</b><br><br>" +
            "• Dashboard overview (static data)<br>" +
            "• User interface preview<br>" +
            "• System status information<br>" +
            "• Error reporting and diagnostics<br><br>" +
            "<b>Disabled Features:</b><br><br>" +
            "• Database operations<br>" +
            "• Invoice management<br>" +
            "• Tax calculations<br>" +
            "• Data persistence<br>" +
            "</html>");
        featuresLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        featuresLabel.setForeground(new Color(52, 73, 94));
        featuresLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(featuresLabel);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFallbackDashboard() {
        // Create a simple dashboard that doesn't require database connectivity
        return new DashboardPanel(currentUser);
    }
    
    private JPanel createPlaceholderPanel(String moduleName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 126, 34), 2),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        JLabel titleLabel = new JLabel("⚠️ " + moduleName + " - Initialization Error");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(230, 126, 34));
        
        JLabel messageLabel = new JLabel("<html><center>" +
            "This module could not be initialized due to database connectivity issues.<br><br>" +
            "The " + moduleName + " module requires database access to function properly.<br>" +
            "Please resolve the database configuration and restart the application." +
            "</center></html>");
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(52, 73, 94));
        
        messagePanel.add(titleLabel);
        messagePanel.add(Box.createVerticalStrut(20));
        messagePanel.add(messageLabel);
        
        panel.add(messagePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (dashboardPanel != null && dashboardPanel instanceof DashboardPanel) {
            ((DashboardPanel) dashboardPanel).updateUserInfo(user);
        }
    }
}
