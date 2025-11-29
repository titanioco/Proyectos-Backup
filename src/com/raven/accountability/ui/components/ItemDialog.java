package com.raven.accountability.ui.components;

import com.raven.accountability.model.InvoiceItem;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Diamount (#)log for adding/editing invoice items
 */
public class ItemDialog extends JDialog {
    private InvoiceItem invoiceItem;
    private boolean itemSaved = false;
    
    // Form components
    private JTextField descriptionField;
    private JTextField productCodeField;
    private JTextField quantityField;
    private JComboBox<String> unitCombo;
    private JTextField unitPriceField;
    private JTextField discountField;
    private JLabel totalLabel;
    
    public ItemDialog(Window parent) {
        super(parent, "Add Invoice Item", ModalityType.APPLICATION_MODAL);
        
        initComponents();
        setupLayout();
        
        setSize(450, 350);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        descriptionField = new JTextField(20);
        descriptionField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        productCodeField = new JTextField(15);
        productCodeField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        quantityField = new JTextField("1", 10);
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        String[] units = {"unit", "hour", "piece", "kg", "lb", "meter", "foot", "service", "license", "month"};
        unitCombo = new JComboBox<>(units);
        unitCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        unitPriceField = new JTextField("0.00", 10);
        unitPriceField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        discountField = new JTextField("0.00", 10);
        discountField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(52, 73, 94));
        
        // Add listeners for automatic calculation
        quantityField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
        });
        
        unitPriceField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
        });
        
        discountField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateTotal(); }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("📦 Add Invoice Item");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Description
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(descriptionField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        // Product Code
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Product Code:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(productCodeField, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(quantityField, gbc);
        
        // Unit
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(unitCombo, gbc);
        
        // Unit Price
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(unitPriceField, gbc);
        
        // Discount
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Discount:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(discountField, gbc);
        
        // Total
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Total:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(totalLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton addBtn = new JButton("✅ Add Item");
        addBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addBtn.setBackground(new Color(39, 174, 96));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> addItem());
        
        JButton cancelBtn = new JButton("❌ Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setBackground(new Color(127, 140, 141));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(cancelBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void calculateTotal() {
        try {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText());
            BigDecimal discount = new BigDecimal(discountField.getText());
            
            BigDecimal subtotal = quantity.multiply(unitPrice);
            BigDecimal total = subtotal.subtract(discount);
            
            totalLabel.setText("$" + total.toString());
            
        } catch (NumberFormatException e) {
            totalLabel.setText("$0.00");
        }
    }
    
    private void addItem() {
        try {
            // Validate required fields
            if (descriptionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a description.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create invoice item
            invoiceItem = new InvoiceItem();
            invoiceItem.setDescription(descriptionField.getText().trim());
            invoiceItem.setProductCode(productCodeField.getText().trim());
            invoiceItem.setQuantity(new BigDecimal(quantityField.getText()));
            invoiceItem.setUnit((String) unitCombo.getSelectedItem());
            invoiceItem.setUnitPrice(new BigDecimal(unitPriceField.getText()));
            invoiceItem.setDiscount(new BigDecimal(discountField.getText()));
            invoiceItem.calculateTotal();
            
            itemSaved = true;
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numeric values for quantity, unit price, and discount.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error creating item: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public InvoiceItem getInvoiceItem() {
        return itemSaved ? invoiceItem : null;
    }
}
