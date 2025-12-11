package com.raven.accountability.ui.components;

import com.raven.accountability.model.*;
import com.raven.accountability.service.*;
import com.raven.accountability.ui.components.billing.*;
import com.raven.accountability.ui.components.billing.dialogs.*;
import com.raven.model.User;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

/**
 * Billing and Invoicing Panel (Modular Version)
 * Integrates sub-components for a clean, maintainable architecture
 */
public class BillingAndInvoicingPanel extends JPanel {
    
    private User currentUser;

    // Services
    private InvoiceService invoiceService;
    private CustomerService customerService;
    private FirebaseBackupService firebaseBackupService;
    
    // Components
    private InvoiceStatsPanel statsPanel;
    private InvoiceTablePanel tablePanel;
    private InvoiceToolbarPanel toolbarPanel;
    
    // Data
    private List<Invoice> invoices;
    
    public BillingAndInvoicingPanel(User user) {
        this.currentUser = user;
        this.invoices = new ArrayList<>();
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        // Initialize services asynchronously to prevent UI blocking/crashing
        initializeServicesAsync();
    }
    
    private void initializeServicesAsync() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    System.out.println("BILLING: Starting service initialization...");
                    invoiceService = new InvoiceService();
                    customerService = new CustomerService();
                    firebaseBackupService = new FirebaseBackupService();
                    System.out.println("BILLING: Services initialized successfully");
                } catch (Exception e) {
                    System.err.println("BILLING: Service initialization failed: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                loadInvoices();
            }
        };
        worker.execute();
    }
    
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        statsPanel = new InvoiceStatsPanel();
        toolbarPanel = new InvoiceToolbarPanel();
        tablePanel = new InvoiceTablePanel();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("🧾 Billing & Invoicing Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel subtitleLabel = new JLabel("Manage invoices, customers, and payments");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(toolbarPanel, BorderLayout.SOUTH);
        
        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statsPanel, contentPanel);
        splitPane.setDividerLocation(120);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Toolbar Actions
        toolbarPanel.setCreateAction(e -> createInvoice());
        toolbarPanel.setEditAction(e -> editSelectedInvoice());
        toolbarPanel.setViewAction(e -> viewSelectedInvoice());
        toolbarPanel.setDeleteAction(e -> deleteSelectedInvoice());
        toolbarPanel.setSaveAction(e -> saveChanges());
        toolbarPanel.setBackupAction(e -> backupToCloud());
        
        // Table Selection
        tablePanel.setOnSelectionChanged(() -> {
            boolean hasSelection = tablePanel.hasSelection();
            toolbarPanel.setSelectionButtonsEnabled(hasSelection);
        });
        
        // Double Click
        tablePanel.setOnDoubleClickListener(this::viewSelectedInvoice);
    }
    
    private void loadInvoices() {
        if (invoiceService == null) return;
        
        try {
            invoices = invoiceService.getAllInvoices();
            tablePanel.setInvoices(invoices);
            updateStatistics();
        } catch (Exception e) {
            System.err.println("Error loading invoices: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatistics() {
        if (invoices == null || invoices.isEmpty()) {
            statsPanel.reset();
            return;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        BigDecimal overdue = BigDecimal.ZERO;
        
        for (Invoice inv : invoices) {
            total = total.add(inv.getTotalAmount());
            outstanding = outstanding.add(inv.getBalanceAmount());
            if (inv.getStatus() == Invoice.InvoiceStatus.OVERDUE) {
                overdue = overdue.add(inv.getBalanceAmount());
            }
        }
        
        statsPanel.updateStats(invoices.size(), total, outstanding, overdue);
    }
    
    // Actions
    private void createInvoice() {
        try {
            List<Customer> customers = customerService != null ? customerService.getAllCustomers() : new ArrayList<>();
            if (customers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please create a customer first.", "No Customers", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            InvoiceDialog dialog = new InvoiceDialog(SwingUtilities.getWindowAncestor(this), null, customers);
            dialog.setVisible(true);
            
            if (dialog.isSaved()) {
                Invoice newInvoice = dialog.getInvoice();
                if (invoiceService != null) {
                    invoiceService.saveInvoice(newInvoice);
                    loadInvoices(); // Refresh
                    JOptionPane.showMessageDialog(this, "Invoice created successfully!");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editSelectedInvoice() {
        Invoice invoice = tablePanel.getSelectedInvoice();
        if (invoice != null) {
            try {
                List<Customer> customers = customerService != null ? customerService.getAllCustomers() : new ArrayList<>();
                InvoiceDialog dialog = new InvoiceDialog(SwingUtilities.getWindowAncestor(this), invoice, customers);
                dialog.setVisible(true);
                
                if (dialog.isSaved()) {
                    if (invoiceService != null) {
                        invoiceService.updateInvoice(invoice);
                        loadInvoices(); // Refresh
                        JOptionPane.showMessageDialog(this, "Invoice updated successfully!");
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewSelectedInvoice() {
        Invoice invoice = tablePanel.getSelectedInvoice();
        if (invoice != null) {
            new InvoiceViewDialog(SwingUtilities.getWindowAncestor(this), invoice).setVisible(true);
        }
    }
    
    private void deleteSelectedInvoice() {
        Invoice invoice = tablePanel.getSelectedInvoice();
        if (invoice == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete invoice " + invoice.getInvoiceNumber() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION && invoiceService != null) {
            try {
                if (invoiceService.deleteInvoice(invoice.getInvoiceId())) {
                    tablePanel.removeSelectedInvoice();
                    invoices.remove(invoice); // Update local list too
                    updateStatistics();
                    JOptionPane.showMessageDialog(this, "Deleted successfully");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveChanges() {
        // Not needed with direct save/update in dialogs, but kept for interface compatibility
        JOptionPane.showMessageDialog(this, "Changes are saved automatically when you click Save in the dialogs.");
    }
    
    private void backupToCloud() {
        if (firebaseBackupService == null || !firebaseBackupService.isServiceAvailable()) {
            JOptionPane.showMessageDialog(this, "Firebase service unavailable", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        toolbarPanel.setBackupButtonState(false, "Backing up...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                return firebaseBackupService.backupInvoices(invoices).get();
            }
            
            protected void done() {
                try {
                    boolean success = get();
                    String msg = success ? "Backup successful!" : "Backup completed with errors.";
                    JOptionPane.showMessageDialog(BillingAndInvoicingPanel.this, msg);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BillingAndInvoicingPanel.this, "Backup failed: " + e.getMessage());
                } finally {
                    toolbarPanel.setBackupButtonState(true, "☁️ Cloud Backup");
                }
            }
        };
        worker.execute();
    }
    
    public void refreshData() {
        loadInvoices();
    }
    
    public boolean canLogout() {
        return true;
    }
}