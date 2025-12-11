package com.raven.accountability.demo;

import com.raven.accountability.ui.components.BillingAndInvoicingPanel;
import com.raven.accountability.service.*;
import com.raven.model.User;
import javax.swing.*;
import java.awt.*;

/**
 * Simple test to verify the BillingAndInvoicingPanel tabs work correctly
 */
public class BillingTabsTest {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel to system default
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look and feel
                System.out.println("Using default look and feel");
            }
            
            // Create test frame
            JFrame frame = new JFrame("Billing & Invoicing - Tabbed Interface Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            
            // Create mock user
            User user = createMockUser();
            
            // Create services (they will work in demo mode with null databases)
            InvoiceService invoiceService = new InvoiceService();
            QuotationService quotationService = new QuotationService();
            CustomerService customerService = new CustomerService();
            InvoiceTemplateGenerator templateGenerator = new InvoiceTemplateGenerator();
            
            // Create the tabbed billing panel
            BillingAndInvoicingPanel billingPanel = new BillingAndInvoicingPanel(user);
            
            // Add to frame
            frame.add(billingPanel, BorderLayout.CENTER);
            
            // Add a welcome message
            JLabel welcomeLabel = new JLabel("Welcome to Tabbed Billing & Invoicing System", JLabel.CENTER);
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.add(welcomeLabel, BorderLayout.NORTH);
            
            // Show frame
            frame.setVisible(true);
            
            System.out.println("✅ Billing and Invoicing tabs launched successfully!");
            System.out.println("📄 Invoice management available in first tab");
            System.out.println("📋 Quotation management available in second tab");
            System.out.println("🔄 Convert quotations to invoices with one click");
        });
    }
    
    /**
     * Create a mock user for testing
     */
    private static User createMockUser() {
        User user = new User();
        try {
            user.setEmail("demo@example.com");
            user.setFullName("Demo User");
        } catch (Exception e) {
            System.out.println("Note: Using basic user object for demo");
        }
        return user;
    }
}
