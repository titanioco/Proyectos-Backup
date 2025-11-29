package com.raven.accountability.ui.components;

import com.raven.model.User;
import javax.swing.*;
import java.awt.*;

/**
 * Compliance Management Panel
 * Handles regulatory compliance, policy management, and compliance monitoring
 */
public class CompliancePanel extends JPanel {
    private User currentUser;
    
    public CompliancePanel(User user) {
        this.currentUser = user;
        initComponents();
        setupLayout();
    }
    
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("📋 Compliance Management Module", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel descLabel = new JLabel("<html><center>Complete compliance management system:<br/>" +
                                    "• Regulatory Compliance Tracking<br/>" +
                                    "• Policy Management<br/>" +
                                    "• Compliance Audits<br/>" +
                                    "• Risk Assessment<br/>" +
                                    "• Documentation Management</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(new Color(127, 140, 141));
        
        JLabel statusLabel = new JLabel("🚀 Module under development - Coming in Phase 2", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(230, 126, 34));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel, BorderLayout.NORTH);
        centerPanel.add(descLabel, BorderLayout.CENTER);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
    }
}
