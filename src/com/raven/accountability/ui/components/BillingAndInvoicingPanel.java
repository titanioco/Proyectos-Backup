package com.raven.accountability.ui.components;

import com.raven.model.User;
import javax.swing.*;
import java.awt.*;

/**
 * Billing and Invoicing Panel - Simplified version without database dependency
 * Shows placeholder message until database issues are resolved
 */
public class BillingAndInvoicingPanel extends JPanel {
    private User currentUser;
    
    public BillingAndInvoicingPanel(User user) {
        this.currentUser = user;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Create main message panel
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("📋 Billing & Invoicing Module");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 152, 219));
        
        // Message
        JLabel messageLabel = new JLabel("<html><center>" +
            "This module is currently running in demo mode.<br><br>" +
            "<b>Features available in full version:</b><br><br>" +
            "• Invoice Management<br>" +
            "• Quotation System<br>" +
            "• Customer Database<br>" +
            "• Payment Tracking<br>" +
            "• PDF Generation<br><br>" +
            "Other accountability modules are fully functional." +
            "</center></html>");
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(52, 73, 94));
        
        messagePanel.add(titleLabel);
        messagePanel.add(Box.createVerticalStrut(20));
        messagePanel.add(messageLabel);
        
        add(messagePanel, BorderLayout.CENTER);
        
        System.out.println("BILLING PANEL: Initialized in demo mode (database-free)");
    }
    
    // Stub methods for compatibility
    public void refreshData() {
        // No-op in demo mode
    }
    
    public boolean canLogout() {
        return true;
    }
}