package com.raven.accountability.ui.components;

import com.raven.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard Panel for Business Accountability Suite
 * Provides overview of key metrics, recent activities, and quick access to common functions
 */
public class DashboardPanel extends JPanel {
    private User currentUser;
    private JLabel welcomeLabel;
    private JPanel metricsPanel;
    private JPanel recentActivitiesPanel;
    private JPanel quickActionsPanel;
    
    public DashboardPanel(User user) {
        this.currentUser = user;
        initComponents();
        setupLayout();
        loadDashboardData();
    }
    
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(20, 20));
        
        // Header section
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content area
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        
        // Top metrics row
        metricsPanel = createMetricsPanel();
        contentPanel.add(metricsPanel, BorderLayout.NORTH);
        
        // Middle section with recent activities and quick actions
        JPanel middlePanel = new JPanel(new BorderLayout(20, 0));
        middlePanel.setOpaque(false);
        
        recentActivitiesPanel = createRecentActivitiesPanel();
        quickActionsPanel = createQuickActionsPanel();
        
        middlePanel.add(recentActivitiesPanel, BorderLayout.CENTER);
        middlePanel.add(quickActionsPanel, BorderLayout.EAST);
        
        contentPanel.add(middlePanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Welcome message
        welcomeLabel = new JLabel();
        updateWelcomeMessage();
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(52, 73, 94));
        
        // Date and time
        JLabel dateTimeLabel = new JLabel();
        dateTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - HH:mm")));
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateTimeLabel.setForeground(new Color(127, 140, 141));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(welcomeLabel, BorderLayout.NORTH);
        leftPanel.add(dateTimeLabel, BorderLayout.SOUTH);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 120));
        
        // Financial Overview Card
        panel.add(createMetricCard("💰", "Total Revenue", "$24,567.89", "+12.5%", new Color(39, 174, 96)));
        
        // Compliance Status Card
        panel.add(createMetricCard("✅", "Compliance Score", "94%", "+2.1%", new Color(52, 152, 219)));
        
        // Pending Tasks Card
        panel.add(createMetricCard("📋", "Pending Tasks", "7", "-3", new Color(230, 126, 34)));
        
        // System Health Card
        panel.add(createMetricCard("⚡", "System Health", "Excellent", "100%", new Color(155, 89, 182)));
        
        return panel;
    }
    
    private JPanel createMetricCard(String icon, String title, String value, String change, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Accent line
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 12, 12);
                
                // Shadow effect
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, getWidth(), getHeight(), 12, 12);
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Icon and title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(127, 140, 141));
        
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Value and change
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(52, 73, 94));
        
        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        changeLabel.setForeground(change.startsWith("+") ? new Color(39, 174, 96) : 
                                 change.startsWith("-") ? new Color(231, 76, 60) : new Color(127, 140, 141));
        
        bottomPanel.add(valueLabel, BorderLayout.WEST);
        bottomPanel.add(changeLabel, BorderLayout.EAST);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(bottomPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createRecentActivitiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
            "Recent Activities",
            0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 73, 94)
        ));
        panel.setBackground(Color.WHITE);
        
        // Activities list
        JPanel activitiesContainer = new JPanel();
        activitiesContainer.setLayout(new BoxLayout(activitiesContainer, BoxLayout.Y_AXIS));
        activitiesContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        activitiesContainer.setBackground(Color.WHITE);
        
        // Sample activities
        activitiesContainer.add(createActivityItem("📄", "New invoice generated", "Invoice #INV-2025-001", "2 minutes ago"));
        activitiesContainer.add(createActivityItem("✅", "Compliance check completed", "Tax filing verification", "15 minutes ago"));
        activitiesContainer.add(createActivityItem("💳", "Payment received", "$1,250.00 from Client ABC", "1 hour ago"));
        activitiesContainer.add(createActivityItem("📊", "Monthly report generated", "Financial Summary - January", "3 hours ago"));
        activitiesContainer.add(createActivityItem("🔍", "Audit trail updated", "System access logs", "5 hours ago"));
        
        JScrollPane scrollPane = new JScrollPane(activitiesContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActivityItem(String icon, String title, String details, String time) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailsLabel.setForeground(new Color(127, 140, 141));
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(149, 165, 166));
        
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(detailsLabel, BorderLayout.CENTER);
        
        item.add(iconLabel, BorderLayout.WEST);
        item.add(contentPanel, BorderLayout.CENTER);
        item.add(timeLabel, BorderLayout.EAST);
        
        return item;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225), 1),
            "Quick Actions",
            0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 73, 94)
        ));
        panel.setBackground(Color.WHITE);
        
        JPanel actionsContainer = new JPanel();
        actionsContainer.setLayout(new BoxLayout(actionsContainer, BoxLayout.Y_AXIS));
        actionsContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        actionsContainer.setBackground(Color.WHITE);
        
        // Quick action buttons
        actionsContainer.add(createQuickActionButton("📄 Generate Invoice", new Color(52, 152, 219)));
        actionsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        actionsContainer.add(createQuickActionButton("📊 View Reports", new Color(39, 174, 96)));
        actionsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        actionsContainer.add(createQuickActionButton("🔍 Run Compliance Check", new Color(230, 126, 34)));
        actionsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        actionsContainer.add(createQuickActionButton("💳 Record Payment", new Color(155, 89, 182)));
        actionsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        actionsContainer.add(createQuickActionButton("🏛️ Tax Filing", new Color(231, 76, 60)));
        
        actionsContainer.add(Box.createVerticalGlue());
        
        panel.add(actionsContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createQuickActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        button.addActionListener(e -> handleQuickAction(text));
        
        return button;
    }
    
    private void handleQuickAction(String actionText) {
        // First show the informational popup
        JOptionPane.showMessageDialog(this, 
            "Quick Action: " + actionText + "\n\nThis feature will be implemented in the next development phase.",
            "Feature Coming Soon",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Then attempt to navigate to the appropriate module
        try {
            // Get the parent AccountabilityManagementFrame
            java.awt.Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof com.raven.accountability.ui.AccountabilityManagementFrame) {
                com.raven.accountability.ui.AccountabilityManagementFrame parentFrame = 
                    (com.raven.accountability.ui.AccountabilityManagementFrame) window;
                
                // Navigate to the appropriate module based on the action
                navigateToModule(actionText, parentFrame);
            }
        } catch (Exception ex) {
            System.err.println("ERROR: Failed to navigate after quick action: " + ex.getMessage());
            // If navigation fails, show a more detailed dashboard view
            showEnhancedDashboardView(actionText);
        }
    }
    
    private void navigateToModule(String actionText, com.raven.accountability.ui.AccountabilityManagementFrame parentFrame) {
        // Use the switchToModule method via reflection or by making the necessary methods public
        // For now, we'll provide a helpful message about the intended navigation
        
        String targetModule = "dashboard"; // default fallback
        String moduleDescription = "";
        
        if (actionText.contains("Invoice")) {
            targetModule = "billing";
            moduleDescription = "Billing & Invoicing Module";
        } else if (actionText.contains("Reports")) {
            targetModule = "reporting";
            moduleDescription = "Reporting & Analytics Module";
        } else if (actionText.contains("Compliance")) {
            targetModule = "compliance";
            moduleDescription = "Compliance Management Module";
        } else if (actionText.contains("Payment")) {
            targetModule = "financial";
            moduleDescription = "Financial Accounting Module";
        } else if (actionText.contains("Tax")) {
            targetModule = "tax";
            moduleDescription = "Tax Management Module";
        }
        
        // Show navigation information
        if (!targetModule.equals("dashboard")) {
            JOptionPane.showMessageDialog(this,
                "Navigating to " + moduleDescription + "...\n\n" +
                "You can access this module from the sidebar navigation menu.\n" +
                "Click on the corresponding module button to explore its features.",
                "Module Navigation",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showEnhancedDashboardView(String actionText) {
        // Create a detailed view for the specific action
        JDialog detailDialog = new JDialog();
        detailDialog.setTitle("Dashboard Detail: " + actionText);
        detailDialog.setSize(600, 400);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setModal(true);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Enhanced Dashboard View", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        String detailContent = getDetailContent(actionText);
        JLabel contentLabel = new JLabel("<html><div style='text-align: left;'>" + detailContent + "</div></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentLabel.setForeground(new Color(52, 73, 94));
        
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(52, 152, 219));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> detailDialog.dispose());
        
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(contentLabel, BorderLayout.CENTER);
        contentPanel.add(closeButton, BorderLayout.SOUTH);
        
        detailDialog.add(contentPanel);
        detailDialog.setVisible(true);
    }
    
    private String getDetailContent(String actionText) {
        if (actionText.contains("Invoice")) {
            return "Invoice Generation Features:<br><br>" +
                   "• Create professional invoices with customizable templates<br>" +
                   "• Automatic calculation of taxes and totals<br>" +
                   "• Client management and billing history<br>" +
                   "• Payment tracking and reminders<br>" +
                   "• Integration with accounting systems<br><br>" +
                   "Navigate to the Billing module from the sidebar to access these features.";
        } else if (actionText.contains("Reports")) {
            return "Reporting & Analytics Features:<br><br>" +
                   "• Financial performance dashboards<br>" +
                   "• Custom report generation<br>" +
                   "• Data visualization and charts<br>" +
                   "• Export capabilities (PDF, Excel)<br>" +
                   "• Scheduled report delivery<br><br>" +
                   "Navigate to the Reporting module from the sidebar to access these features.";
        } else if (actionText.contains("Compliance")) {
            return "Compliance Management Features:<br><br>" +
                   "• Regulatory compliance tracking<br>" +
                   "• Audit trail maintenance<br>" +
                   "• Document management<br>" +
                   "• Compliance status monitoring<br>" +
                   "• Automated compliance checks<br><br>" +
                   "Navigate to the Compliance module from the sidebar to access these features.";
        } else if (actionText.contains("Payment")) {
            return "Payment Recording Features:<br><br>" +
                   "• Multiple payment method support<br>" +
                   "• Payment reconciliation<br>" +
                   "• Transaction history tracking<br>" +
                   "• Integration with banking systems<br>" +
                   "• Automated payment processing<br><br>" +
                   "Navigate to the Financial module from the sidebar to access these features.";
        } else if (actionText.contains("Tax")) {
            return "Tax Filing Features:<br><br>" +
                   "• Tax calculation and preparation<br>" +
                   "• Multiple tax jurisdiction support<br>" +
                   "• Electronic filing capabilities<br>" +
                   "• Tax document management<br>" +
                   "• Deadline tracking and reminders<br><br>" +
                   "Navigate to the Tax Management module from the sidebar to access these features.";
        }
        return "Dashboard Overview:<br><br>" +
               "This comprehensive business dashboard provides access to all major<br>" +
               "business accountability features through an intuitive interface.<br><br>" +
               "Use the sidebar navigation to explore different modules and their capabilities.";
    }
    
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getFullName() + "!");
        } else {
            welcomeLabel.setText("Welcome to Business Accountability Suite");
        }
    }
    
    private void loadDashboardData() {
        // TODO: Load real dashboard data from database/service
        // This is a placeholder for future implementation
        System.out.println("DASHBOARD: Loading dashboard data for user: " + 
                          (currentUser != null ? currentUser.getEmail() : "Guest"));
    }
    
    public void updateUserInfo(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
        repaint();
    }
    
    public void refreshData() {
        loadDashboardData();
        repaint();
    }
}
