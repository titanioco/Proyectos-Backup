package com.raven.accountability.ui.components;

import com.raven.model.User;
import com.raven.accountability.model.*;
import com.raven.accountability.model.SampleInvoice; // Explicit import to use model version
import com.raven.accountability.service.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main Billing Panel with Tabbed Interface
 * Contains tabs for Invoices and Quotations with conversion functionality
 */
public class BillingPanel extends JPanel {
    private User currentUser;
    private InvoiceService invoiceService;
    private QuotationService quotationService;
    private CustomerService customerService;
    private InvoiceTemplateGenerator templateGenerator;
    
    // Main components
    private JTabbedPane tabbedPane;
    private InvoicePanel invoicePanel;
    private QuotationPanel quotationPanel;
    
    public BillingPanel(User user) {
        this.currentUser = user;
        
        // Initialize services (will fallback to sample data if database unavailable)
        initializeServices();
        
        // Initialize UI
        initComponents();
        setupLayout();
        
        System.out.println("BILLING PANEL: Initialized with tabbed interface for user: " + 
                          (user != null ? user.getEmail() : "demo"));
    }
    
    private void initializeServices() {
        try {
            // Initialize services with error handling
            this.invoiceService = new InvoiceService();
            this.quotationService = new QuotationService();
            this.customerService = new CustomerService();
            this.templateGenerator = new InvoiceTemplateGenerator();
            
            System.out.println("BILLING PANEL: Services initialized successfully");
        } catch (Exception e) {
            System.err.println("BILLING PANEL WARNING: Service initialization failed - " + e.getMessage());
            System.out.println("BILLING PANEL: Will use sample data for demo mode");
            
            // Services will be null, panels will handle this gracefully
            this.invoiceService = null;
            this.quotationService = null;
            this.customerService = null;
            this.templateGenerator = null;
        }
    }
    
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setOpaque(false);
        
        // Create tabbed pane with custom styling
        tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Custom tab area background
                g2.setColor(new Color(248, 249, 250));
                g2.fillRect(0, 0, getWidth(), 35);
            }
        };
        
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(new Color(248, 249, 250));
        tabbedPane.setForeground(new Color(52, 73, 94));
        
        // Create individual panels
        invoicePanel = new InvoicePanel(currentUser, invoiceService, customerService, templateGenerator);
        quotationPanel = new QuotationPanel(currentUser, quotationService, customerService, templateGenerator, this);
        
        // Add tabs with icons
        tabbedPane.addTab("📄 Invoices", invoicePanel);
        tabbedPane.addTab("📋 Quotations", quotationPanel);
        
        // Custom tab styling
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, Color.WHITE);
            tabbedPane.setForegroundAt(i, new Color(52, 73, 94));
        }
        
        // Tab change listener for refreshing data
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0 && invoicePanel != null) {
                invoicePanel.refreshData();
            } else if (selectedIndex == 1 && quotationPanel != null) {
                quotationPanel.refreshData();
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Main tabbed content
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(52, 152, 219),
                    0, getHeight(), new Color(41, 128, 185)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(0, 80));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("💼 Billing & Invoicing Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel subtitleLabel = new JLabel("Manage invoices and quotations with conversion capabilities");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        return titlePanel;
    }
    
    /**
     * Convert a quotation to an invoice
     * This method is called by QuotationPanel when converting quotations
     */
    public void convertQuotationToInvoice(SampleQuotation quotation) {
        try {
            // Generate new invoice number
            String newInvoiceNumber = "INV-2025-" + String.format("%03d", (int)(Math.random() * 900) + 100);
            
            // Create new sample invoice from quotation
            String dueDate = java.time.LocalDate.now().plusDays(30).toString();
            SampleInvoice newInvoice = new SampleInvoice(
                newInvoiceNumber,
                quotation.getCustomerName(),
                java.time.LocalDate.now().toString(),
                dueDate,
                "PENDING",
                quotation.getFormattedAmount(),
                "$0.00",
                quotation.getFormattedAmount()
            );
            
            // Mark quotation as converted
            quotation.setConvertedToInvoice(true);
            quotation.setConvertedInvoiceNumber(newInvoiceNumber);
            quotation.setStatus("CONVERTED");
            
            // Add invoice to invoice panel
            if (invoicePanel != null) {
                invoicePanel.addNewInvoice(newInvoice);
            }
            
            // Refresh quotation panel
            if (quotationPanel != null) {
                quotationPanel.refreshData();
            }
            
            // Switch to invoice tab to show the new invoice
            tabbedPane.setSelectedIndex(0);
            
            // Show success message
            JOptionPane.showMessageDialog(this,
                "<html><div style='width: 350px;'>" +
                "<h3>✅ Conversion Successful!</h3><br>" +
                "<b>Quotation:</b> " + quotation.getQuotationNumber() + "<br>" +
                "<b>New Invoice:</b> " + newInvoiceNumber + "<br>" +
                "<b>Customer:</b> " + quotation.getCustomerName() + "<br>" +
                "<b>Amount:</b> " + quotation.getFormattedAmount() + "<br><br>" +
                "The quotation has been converted to an invoice and added to the Invoices tab.<br>" +
                "You can now process payment and manage the invoice lifecycle." +
                "</div></html>",
                "Quotation Converted Successfully",
                JOptionPane.INFORMATION_MESSAGE);
                
            System.out.println("BILLING PANEL: Successfully converted quotation " + 
                             quotation.getQuotationNumber() + " to invoice " + newInvoiceNumber);
                             
        } catch (Exception e) {
            System.err.println("BILLING PANEL ERROR: Failed to convert quotation - " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Failed to convert quotation to invoice.\nError: " + e.getMessage(),
                "Conversion Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Check if user can logout (handles unsaved changes)
     */
    public boolean canLogout() {
        boolean canLogout = true;
        
        if (invoicePanel != null && !invoicePanel.canLogout()) {
            canLogout = false;
        }
        
        if (quotationPanel != null && !quotationPanel.canLogout()) {
            canLogout = false;
        }
        
        return canLogout;
    }
    
    /**
     * Refresh all data in both panels
     */
    public void refreshAllData() {
        if (invoicePanel != null) {
            invoicePanel.refreshData();
        }
        if (quotationPanel != null) {
            quotationPanel.refreshData();
        }
    }
    
    /**
     * Get current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Switch to invoices tab
     */
    public void showInvoicesTab() {
        tabbedPane.setSelectedIndex(0);
    }
    
    /**
     * Switch to quotations tab
     */
    public void showQuotationsTab() {
        tabbedPane.setSelectedIndex(1);
    }
}
