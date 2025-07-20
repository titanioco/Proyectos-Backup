package com.raven.ui;

import com.raven.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardFrame extends JFrame {
    private User currentUser;
    
    public DashboardFrame(User user) {
        this.currentUser = user;
        initComponents();
        setupUI();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Dashboard - " + currentUser.getFullName());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        
        JLabel titleLabel = new JLabel("Welcome to your Dashboard");
        titleLabel.setFont(new Font("sansserif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("Hello, " + currentUser.getFullName());
        userLabel.setFont(new Font("sansserif", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(logoutButton);
        userPanel.add(Box.createHorizontalStrut(20));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(236, 240, 241));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // User Info Card
        JPanel userInfoCard = createInfoCard("User Information", 
            "Email: " + currentUser.getEmail() + "\n" +
            "Full Name: " + currentUser.getFullName() + "\n" +
            "User ID: " + currentUser.getId());
        
        // Stats Card
        JPanel statsCard = createInfoCard("Account Stats", 
            "Account Type: " + (currentUser.getGoogleSub() != null ? "Google Account" : "Email Account") + "\n" +
            "Registration: Email Verified\n" +
            "Status: Active");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(userInfoCard, gbc);
        
        gbc.gridx = 1;
        mainPanel.add(statsCard, gbc);
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(44, 62, 80));
        footerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel footerLabel = new JLabel("University Management System - Dashboard");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        footerPanel.add(footerLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createInfoCard(String title, String content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("sansserif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("sansserif", Font.PLAIN, 14));
        contentArea.setForeground(new Color(85, 85, 85));
        contentArea.setOpaque(false);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        card.add(contentArea, BorderLayout.SOUTH);
        
        return card;
    }
    
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            // Restart the main application
            SwingUtilities.invokeLater(() -> {
                try {
                    new com.raven.main.Main().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    public static void showFor(User user) {
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame(user).setVisible(true);
        });
    }
}
