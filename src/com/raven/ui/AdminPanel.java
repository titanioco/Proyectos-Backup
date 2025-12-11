package com.raven.ui;

import com.raven.model.User;
import com.raven.service.FirebaseService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.Set;

/**
 * Admin Panel for user management and system control
 * Only accessible by admin users
 */
public class AdminPanel extends JFrame {
    private User adminUser;
    private FirebaseService firebaseService;
    
    private JTable whitelistTable;
    private DefaultTableModel whitelistModel;
    private JTextField emailField;
    private JLabel userCountLabel;
    private JLabel maxUsersLabel;
    private JLabel systemStatusLabel;
    
    public AdminPanel(User adminUser) {
        this.adminUser = adminUser;
        this.firebaseService = FirebaseService.getInstance();
        
        // Verify admin privileges
        if (adminUser.getUserType() != User.UserType.ADMIN) {
            JOptionPane.showMessageDialog(null, 
                "Access Denied\n\nYou must be an administrator to access this panel.",
                "Unauthorized Access", 
                JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        initComponents();
        setupUI();
        loadData();
    }
    
    private void initComponents() {
        setTitle("System Administration - " + adminUser.getFullName());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("System Administration Panel");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel adminLabel = new JLabel("Administrator: " + adminUser.getFullName());
        adminLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        adminLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(adminLabel, BorderLayout.EAST);
        
        // Main content
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // System Overview Tab
        tabbedPane.addTab("System Overview", createSystemOverviewPanel());
        
        // User Management Tab
        tabbedPane.addTab("User Management", createUserManagementPanel());
        
        // Whitelist Management Tab
        tabbedPane.addTab("Whitelist Management", createWhitelistPanel());
        
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createSystemOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // System stats
        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        statsPanel.add(new JLabel("Current Users:"));
        userCountLabel = new JLabel("Loading...");
        userCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsPanel.add(userCountLabel);
        
        statsPanel.add(new JLabel("Maximum Users:"));
        maxUsersLabel = new JLabel("Loading...");
        maxUsersLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsPanel.add(maxUsersLabel);
        
        statsPanel.add(new JLabel("System Status:"));
        systemStatusLabel = new JLabel("Loading...");
        systemStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        systemStatusLabel.setForeground(new Color(46, 204, 113));
        statsPanel.add(systemStatusLabel);
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // System actions
        JPanel actionsPanel = new JPanel(new FlowLayout());
        
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setBackground(new Color(52, 152, 219));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadData());
        
        actionsPanel.add(refreshButton);
        
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel infoLabel = new JLabel("<html>" +
            "<h3>User Management</h3>" +
            "<p>This section allows you to manage user accounts and monitor system usage.</p>" +
            "<p><b>Current Implementation:</b> Basic user statistics and whitelist management.</p>" +
            "<p><b>Future Features:</b> Individual user management, account suspension, role changes.</p>" +
            "</html>");
        
        panel.add(infoLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createWhitelistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Email Whitelist Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Whitelist table
        String[] columns = {"Email Address", "Status"};
        whitelistModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        whitelistTable = new JTable(whitelistModel);
        whitelistTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        whitelistTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        whitelistTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(whitelistTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Whitelisted Emails"));
        
        // Add/Remove panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Email:"));
        
        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inputPanel.add(emailField);
        
        JButton addButton = new JButton("Add to Whitelist");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addToWhitelist());
        inputPanel.add(addButton);
        
        JButton removeButton = new JButton("Remove Selected");
        removeButton.setBackground(new Color(231, 76, 60));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(e -> removeFromWhitelist());
        inputPanel.add(removeButton);
        
        controlPanel.add(inputPanel, BorderLayout.NORTH);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            // Load system statistics
            Map<String, Object> stats = firebaseService.getSystemStats(adminUser);
            
            userCountLabel.setText(stats.get("totalUsers").toString());
            maxUsersLabel.setText(stats.get("maxUsers").toString());
            
            boolean whitelistEnabled = (Boolean) stats.get("whitelistEnabled");
            systemStatusLabel.setText(whitelistEnabled ? "Whitelist Active" : "Open Registration");
            systemStatusLabel.setForeground(whitelistEnabled ? 
                new Color(46, 204, 113) : new Color(243, 156, 18));
            
            // Load whitelist
            loadWhitelist();
        });
    }
    
    private void loadWhitelist() {
        whitelistModel.setRowCount(0);
        Set<String> emails = firebaseService.getWhitelistedEmails(adminUser);
        
        for (String email : emails) {
            whitelistModel.addRow(new Object[]{email, "Active"});
        }
    }
    
    private void addToWhitelist() {
        String email = emailField.getText().trim().toLowerCase();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter an email address.", 
                "Input Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid email address.", 
                "Invalid Email", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean success = firebaseService.addToWhitelist(email, adminUser);
        
        if (success) {
            emailField.setText("");
            loadWhitelist();
            JOptionPane.showMessageDialog(this, 
                "Email added to whitelist successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to add email to whitelist.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeFromWhitelist() {
        int selectedRow = whitelistTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an email to remove.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String email = (String) whitelistModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove '" + email + "' from the whitelist?", 
            "Confirm Removal", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = firebaseService.removeFromWhitelist(email, adminUser);
            
            if (success) {
                loadWhitelist();
                JOptionPane.showMessageDialog(this, 
                    "Email removed from whitelist successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to remove email from whitelist.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
    
    /**
     * Show admin panel for authorized users
     */
    public static void showFor(User user) {
        if (user.getUserType() == User.UserType.ADMIN) {
            SwingUtilities.invokeLater(() -> {
                new AdminPanel(user).setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(null, 
                "Access Denied\n\nAdministrator privileges required.",
                "Unauthorized", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
