package com.raven.accountability.ui.components;

import com.raven.accountability.model.SampleQuotation;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Dialog for creating new quotations
 */
public class QuotationDialog extends JDialog {
    private SampleQuotation quotation;
    private boolean quotationSaved = false;
    
    // Form components
    private JTextField quotationNumberField;
    private JTextField customerNameField;
    private JSpinner quotationDateSpinner;
    private JSpinner validUntilDateSpinner;
    private JComboBox<String> statusCombo;
    private JTextField amountField;
    private JTextArea descriptionArea;
    private JTextArea notesArea;
    
    public QuotationDialog(Window parent) {
        super(parent, "Create New Quotation", ModalityType.APPLICATION_MODAL);
        
        initComponents();
        setupLayout();
        
        setSize(550, 550);
        setMinimumSize(new Dimension(400, 450));
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        // Generate quotation number
        String quotationNumber = "QUO-2025-" + String.format("%03d", (int)(Math.random() * 900) + 100);
        quotationNumberField = new JTextField(quotationNumber);
        quotationNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quotationNumberField.setBackground(new Color(245, 245, 245));
        quotationNumberField.setEditable(false);
        
        customerNameField = new JTextField();
        customerNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Set default dates using JSpinner with date models
        Calendar today = Calendar.getInstance();
        Calendar validUntil = Calendar.getInstance();
        validUntil.add(Calendar.MONTH, 1);
        
        quotationDateSpinner = new JSpinner(new SpinnerDateModel(today.getTime(), null, null, Calendar.DAY_OF_MONTH));
        quotationDateSpinner.setEditor(new JSpinner.DateEditor(quotationDateSpinner, "yyyy-MM-dd"));
        quotationDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        validUntilDateSpinner = new JSpinner(new SpinnerDateModel(validUntil.getTime(), null, null, Calendar.DAY_OF_MONTH));
        validUntilDateSpinner.setEditor(new JSpinner.DateEditor(validUntilDateSpinner, "yyyy-MM-dd"));
        validUntilDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        String[] statusOptions = {"DRAFT", "SENT", "VIEWED", "ACCEPTED", "REJECTED", "EXPIRED"};
        statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCombo.setSelectedItem("DRAFT");
        
        amountField = new JTextField("0.00");
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        notesArea = new JTextArea(3, 30);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("📋 Create New Quotation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main panel with scroll capability
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create scroll pane for main content
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Quotation Number
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Quotation #:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(quotationNumberField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        // Customer Name
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(customerNameField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        // Quotation Date
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Quotation Date:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(quotationDateSpinner, gbc);
        
        // Valid Until Date
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Valid Until:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(validUntilDateSpinner, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(statusCombo, gbc);
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Amount ($):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(amountField, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        
        // Description
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 0.5;
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(300, 100));
        descScrollPane.setMinimumSize(new Dimension(200, 60));
        descScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(descScrollPane, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.0; gbc.weighty = 0.0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 0.5;
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        notesScrollPane.setPreferredSize(new Dimension(300, 80));
        notesScrollPane.setMinimumSize(new Dimension(200, 40));
        notesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        notesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(notesScrollPane, gbc);
        
        add(mainScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton createBtn = new JButton("✅ Create Quotation");
        createBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        createBtn.setBackground(new Color(39, 174, 96));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> createQuotation());
        
        JButton cancelBtn = new JButton("❌ Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setBackground(new Color(127, 140, 141));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());
        
        // Hover effects
        createBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                createBtn.setBackground(new Color(39, 174, 96).darker());
            }
            public void mouseExited(MouseEvent evt) {
                createBtn.setBackground(new Color(39, 174, 96));
            }
        });
        
        cancelBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                cancelBtn.setBackground(new Color(127, 140, 141).darker());
            }
            public void mouseExited(MouseEvent evt) {
                cancelBtn.setBackground(new Color(127, 140, 141));
            }
        });
        
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createQuotation() {
        try {
            // Validate required fields
            if (customerNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a customer name.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                customerNameField.requestFocus();
                return;
            }
            
            // Date validation - dates are always set by the spinners
            Date quotationDate = (Date) quotationDateSpinner.getValue();
            Date validUntilDate = (Date) validUntilDateSpinner.getValue();
            
            if (validUntilDate.before(quotationDate)) {
                JOptionPane.showMessageDialog(this, 
                    "Valid until date must be after quotation date.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                validUntilDateSpinner.requestFocus();
                return;
            }
            
            // Validate amount
            try {
                new BigDecimal(amountField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid amount.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                amountField.requestFocus();
                return;
            }
            
            // Format dates for display
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate quotationLocalDate = quotationDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate validUntilLocalDate = validUntilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            // Create quotation
            quotation = new SampleQuotation(
                quotationNumberField.getText().trim(),
                customerNameField.getText().trim(),
                quotationLocalDate.format(formatter),
                validUntilLocalDate.format(formatter),
                (String) statusCombo.getSelectedItem(),
                "$" + amountField.getText().trim(),
                descriptionArea.getText().trim(),
                notesArea.getText().trim()
            );
            
            quotationSaved = true;
            
            // Show success message
            JOptionPane.showMessageDialog(this,
                "<html><div style='width: 300px;'>" +
                "<h3>✅ Quotation Created Successfully</h3><br>" +
                "<b>Quotation #:</b> " + quotation.getQuotationNumber() + "<br>" +
                "<b>Customer:</b> " + quotation.getCustomerName() + "<br>" +
                "<b>Amount:</b> " + quotation.getFormattedAmount() + "<br>" +
                "<b>Status:</b> " + quotation.getStatus() + "<br><br>" +
                "The quotation has been added to your session." +
                "</div></html>",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error creating quotation: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public SampleQuotation getQuotation() {
        return quotationSaved ? quotation : null;
    }
}