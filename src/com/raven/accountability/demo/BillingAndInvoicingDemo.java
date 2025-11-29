package com.raven.accountability.demo;

import com.raven.accountability.ui.components.BillingAndInvoicingPanel;
import com.raven.accountability.service.*;
import com.raven.model.User;
import javax.swing.*;
import java.awt.*;

/**
 * Demo class showing how to use the new BillingAndInvoicingPanel with tabs
 */
public class BillingAndInvoicingDemo {
    
    public static void createAndShowDemo() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel to system default
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look and feel
            }
            
            // Create demo frame
            JFrame frame = new JFrame("Billing and Invoicing Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            
            // Create sample user (adjust based on your User class structure)
            User user = new User();
            // user.setUsername("demo_user"); // Uncomment if your User class has this method
            // user.setFullName("Demo User"); // Uncomment if your User class has this method
            
            
            // Optional: Show a welcome message
            JOptionPane.showMessageDialog(frame,
                "<html><div style='width: 400px;'>" +
                "<h2>🎉 Welcome to Billing and Invoicing Management!</h2><br>" +
                "This system now includes both:<br><br>" +
                "<b>📄 Invoices Tab:</b> Manage all your invoices and billing<br>" +
                "<b>📋 Quotations Tab:</b> Create and manage quotations/estimates<br><br>" +
                "You can easily convert quotations to invoices by selecting a quotation and clicking 'Convert to Invoice'.<br><br>" +
                "<i>Note: This is running in demo mode with sample data.</i>" +
                "</div></html>",
                "System Ready",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    public static void main(String[] args) {
        createAndShowDemo();
    }
}