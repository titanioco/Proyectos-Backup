package com.raven.accountability.ui.components;

import com.raven.accountability.model.*;
import com.raven.accountability.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Invoice Dialog for creating and editing invoices
 */
public class InvoiceDialog extends JDialog {
    private Invoice invoice;
    private CustomerService customerService;
    private InvoiceService invoiceService;
    private boolean invoiceSaved = false;
    
    // Form components
    private JTextField invoiceNumberField;
    private JComboBox<Customer> customerCombo;
    private JTextField invoiceDateField;
    private JTextField dueDateField;
    private JComboBox<Invoice.InvoiceStatus> statusCombo;
    private JTextField descriptionField;
    private JTextArea notesArea;
    private JTextField discountField;
    
    // Items table
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private JButton addItemBtn;
    private JButton removeItemBtn;
    
    // Totals
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    
    public InvoiceDialog(Window parent, Invoice invoice, CustomerService customerService) {
        super(parent, invoice == null ? "Create New Invoice" : "Edit Invoice", ModalityType.APPLICATION_MODAL);
        this.invoice = invoice;
        this.customerService = customerService;
        this.invoiceService = new InvoiceService();
        
        initComponents();
        setupLayout();
        loadData();
        
        if (invoice != null) {
            populateFields();
        }
        
        setSize(900, 700);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        // Basic fields
        invoiceNumberField = new JTextField(15);
        invoiceNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        customerCombo = new JComboBox<>();
        customerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        invoiceDateField = new JTextField(LocalDate.now().toString(), 10);
        invoiceDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        dueDateField = new JTextField(LocalDate.now().plusDays(30).toString(), 10);
        dueDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        statusCombo = new JComboBox<>(Invoice.InvoiceStatus.values());
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        descriptionField = new JTextField(20);
        descriptionField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        notesArea = new JTextArea(3, 20);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        discountField = new JTextField("0.00", 10);
        discountField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Items table
        String[] itemColumns = {"Description", "Product Code", "Qty", "Unit", "Unit Price", "Discount", "Total"};
        itemsTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: case 4: case 5: case 6: return BigDecimal.class;
                    default: return String.class;
                }
            }
        };
        
        itemsTable = new JTable(itemsTableModel);
        itemsTable.setRowHeight(25);
        itemsTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        itemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        addItemBtn = new JButton("➕ Add Item");
        addItemBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addItemBtn.setBackground(new Color(39, 174, 96));
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.setFocusPainted(false);
        
        removeItemBtn = new JButton("➖ Remove Item");
        removeItemBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        removeItemBtn.setBackground(new Color(231, 76, 60));
        removeItemBtn.setForeground(Color.WHITE);
        removeItemBtn.setFocusPainted(false);
        
        // Totals
        subtotalLabel = new JLabel("$0.00");
        subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        taxLabel = new JLabel("$0.00");
        taxLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(52, 73, 94));
        
        // Event listeners
        addItemBtn.addActionListener(e -> addNewItem());
        removeItemBtn.addActionListener(e -> removeSelectedItem());
        discountField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { recalculateTotals(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { recalculateTotals(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { recalculateTotals(); }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Basic info tab
        JPanel basicInfoPanel = createBasicInfoPanel();
        tabbedPane.addTab("📋 Basic Information", basicInfoPanel);
        
        // Items tab
        JPanel itemsPanel = createItemsPanel();
        tabbedPane.addTab("📦 Items & Pricing", itemsPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(invoice == null ? "📄 Create New Invoice" : "✏️ Edit Invoice");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Invoice Number
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Invoice Number:"), gbc);
        gbc.gridx = 1;
        panel.add(invoiceNumberField, gbc);
        
        // Customer
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(customerCombo, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        // Invoice Date
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Invoice Date:"), gbc);
        gbc.gridx = 1;
        panel.add(invoiceDateField, gbc);
        
        // Due Date
        gbc.gridx = 2;
        panel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 3;
        panel.add(dueDateField, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        panel.add(statusCombo, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(descriptionField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(notesArea), gbc);
        
        return panel;
    }
    
    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Items table
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Item buttons
        JPanel itemButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        itemButtonPanel.add(addItemBtn);
        itemButtonPanel.add(removeItemBtn);
        panel.add(itemButtonPanel, BorderLayout.NORTH);
        
        // Totals panel
        JPanel totalsPanel = createTotalsPanel();
        panel.add(totalsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Invoice Totals"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // Discount
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Discount:"), gbc);
        gbc.gridx = 1;
        panel.add(discountField, gbc);
        
        // Subtotal
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1;
        panel.add(subtotalLabel, gbc);
        
        // Tax
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Tax:"), gbc);
        gbc.gridx = 1;
        panel.add(taxLabel, gbc);
        
        // Total
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Total:"), gbc);
        gbc.gridx = 1;
        panel.add(totalLabel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton saveBtn = new JButton("💾 Save Invoice");
        saveBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveInvoice());
        
        JButton cancelBtn = new JButton("❌ Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setBackground(new Color(127, 140, 141));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());
        
        panel.add(saveBtn);
        panel.add(cancelBtn);
        
        return panel;
    }
    
    private void loadData() {
        try {
            // Load customers
            List<Customer> customers = customerService.getAllActiveCustomers();
            DefaultComboBoxModel<Customer> customerModel = new DefaultComboBoxModel<>();
            for (Customer customer : customers) {
                customerModel.addElement(customer);
            }
            customerCombo.setModel(customerModel);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to load customers: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateFields() {
        if (invoice == null) return;
        
        invoiceNumberField.setText(invoice.getInvoiceNumber());
        customerCombo.setSelectedItem(invoice.getCustomer());
        invoiceDateField.setText(invoice.getInvoiceDate().toString());
        dueDateField.setText(invoice.getDueDate().toString());
        statusCombo.setSelectedItem(invoice.getStatus());
        descriptionField.setText(invoice.getDescription());
        notesArea.setText(invoice.getNotes());
        
        if (invoice.getDiscountAmount() != null) {
            discountField.setText(invoice.getDiscountAmount().toString());
        }
        
        // Load items
        itemsTableModel.setRowCount(0);
        for (InvoiceItem item : invoice.getItems()) {
            Object[] rowData = {
                item.getDescription(),
                item.getProductCode(),
                item.getQuantity(),
                item.getUnit(),
                item.getUnitPrice(),
                item.getDiscountAmount(),
                item.getTotal()
            };
            itemsTableModel.addRow(rowData);
        }
        
        recalculateTotals();
    }
    
    private void addNewItem() {
        ItemDialog itemDialog = new ItemDialog(this);
        itemDialog.setVisible(true);
        
        if (itemDialog.getInvoiceItem() != null) {
            InvoiceItem item = itemDialog.getInvoiceItem();
            Object[] rowData = {
                item.getDescription(),
                item.getProductCode(),
                item.getQuantity(),
                item.getUnit(),
                item.getUnitPrice(),
                item.getDiscountAmount(),
                item.getTotal()
            };
            itemsTableModel.addRow(rowData);
            recalculateTotals();
        }
    }
    
    private void removeSelectedItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow != -1) {
            itemsTableModel.removeRow(selectedRow);
            recalculateTotals();
        }
    }
    
    private void recalculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
            Object totalObj = itemsTableModel.getValueAt(i, 6);
            if (totalObj instanceof BigDecimal) {
                subtotal = subtotal.add((BigDecimal) totalObj);
            }
        }
        
        // Apply discount
        BigDecimal discount = BigDecimal.ZERO;
        try {
            discount = new BigDecimal(discountField.getText());
        } catch (NumberFormatException e) {
            // Ignore invalid discount
        }
        
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        
        // Calculate tax (10% for now)
        BigDecimal tax = discountedSubtotal.multiply(new BigDecimal("0.10"));
        
        // Calculate total
        BigDecimal total = discountedSubtotal.add(tax);
        
        subtotalLabel.setText("$" + subtotal.toString());
        taxLabel.setText("$" + tax.toString());
        totalLabel.setText("$" + total.toString());
    }
    
    private void saveInvoice() {
        try {
            // Validate fields
            if (customerCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a customer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (itemsTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Please add at least one item.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create or update invoice
            if (invoice == null) {
                invoice = new Invoice((Customer) customerCombo.getSelectedItem());
            }
            
            // Set invoice properties
            if (!invoiceNumberField.getText().trim().isEmpty()) {
                invoice.setInvoiceNumber(invoiceNumberField.getText().trim());
            }
            invoice.setCustomer((Customer) customerCombo.getSelectedItem());
            invoice.setInvoiceDate(LocalDate.parse(invoiceDateField.getText()));
            invoice.setDueDate(LocalDate.parse(dueDateField.getText()));
            invoice.setStatus((Invoice.InvoiceStatus) statusCombo.getSelectedItem());
            invoice.setDescription(descriptionField.getText());
            invoice.setNotes(notesArea.getText());
            
            // Set discount
            try {
                BigDecimal discount = new BigDecimal(discountField.getText());
                invoice.setDiscountAmount(discount);
            } catch (NumberFormatException e) {
                invoice.setDiscountAmount(BigDecimal.ZERO);
            }
            
            // Clear existing items and add new ones
            invoice.getItems().clear();
            for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
                InvoiceItem item = new InvoiceItem();
                item.setDescription((String) itemsTableModel.getValueAt(i, 0));
                item.setProductCode((String) itemsTableModel.getValueAt(i, 1));
                item.setQuantity((BigDecimal) itemsTableModel.getValueAt(i, 2));
                item.setUnit((String) itemsTableModel.getValueAt(i, 3));
                item.setUnitPrice((BigDecimal) itemsTableModel.getValueAt(i, 4));
                item.setDiscount((BigDecimal) itemsTableModel.getValueAt(i, 5));
                item.calculateTotal();
                invoice.addItem(item);
            }
            
            // Recalculate invoice totals
            invoice.recalculateAmounts();
            
            // Save to database
            if (invoice.getInvoiceId() == null) {
                invoiceService.saveInvoice(invoice);
            } else {
                invoiceService.updateInvoice(invoice);
            }
            
            invoiceSaved = true;
            
            JOptionPane.showMessageDialog(this, 
                "Invoice saved successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to save invoice: " + e.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean wasInvoiceSaved() {
        return invoiceSaved;
    }
}
