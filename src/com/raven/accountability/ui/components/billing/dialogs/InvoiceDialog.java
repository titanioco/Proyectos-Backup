package com.raven.accountability.ui.components.billing.dialogs;

import com.raven.accountability.model.Customer;
import com.raven.accountability.model.Invoice;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Invoice Dialog - Handles creating and editing invoices
 */
public class InvoiceDialog extends JDialog {

    private JTextField dateField;
    private JTextField dueDateField;
    private JComboBox<Customer> customerCombo;
    private JComboBox<Invoice.InvoiceStatus> statusCombo;
    private JTextField amountField;
    private JTextField paidField;
    private JTextArea notesArea;
    
    private boolean saved = false;
    private Invoice invoice;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public InvoiceDialog(Window parent, Invoice invoice, List<Customer> customers) {
        super(parent, invoice == null ? "Create Invoice" : "Edit Invoice", ModalityType.APPLICATION_MODAL);
        this.invoice = invoice == null ? new Invoice() : invoice;
        
        initComponents(customers);
        loadData();
        
        setSize(450, 550);
        setLocationRelativeTo(parent);
    }

    private void initComponents(List<Customer> customers) {
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        // Customer
        addLabel(formPanel, "Customer:", gbc, 0);
        customerCombo = new JComboBox<>(customers.toArray(new Customer[0]));
        customerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Customer) {
                    setText(((Customer) value).getCompanyName());
                }
                return this;
            }
        });
        addComponent(formPanel, customerCombo, gbc, 1);
        
        // Date
        addLabel(formPanel, "Date (yyyy-MM-dd):", gbc, 2);
        dateField = new JTextField(LocalDate.now().format(dateFormatter));
        addComponent(formPanel, dateField, gbc, 3);
        
        // Due Date
        addLabel(formPanel, "Due Date (yyyy-MM-dd):", gbc, 4);
        dueDateField = new JTextField(LocalDate.now().plusDays(30).format(dateFormatter));
        addComponent(formPanel, dueDateField, gbc, 5);
        
        // Status
        addLabel(formPanel, "Status:", gbc, 6);
        statusCombo = new JComboBox<>(Invoice.InvoiceStatus.values());
        addComponent(formPanel, statusCombo, gbc, 7);
        
        // Amount
        addLabel(formPanel, "Total Amount ($):", gbc, 8);
        amountField = new JTextField("0.00");
        addComponent(formPanel, amountField, gbc, 9);
        
        // Paid
        addLabel(formPanel, "Paid Amount ($):", gbc, 10);
        paidField = new JTextField("0.00");
        addComponent(formPanel, paidField, gbc, 11);
        
        // Notes
        addLabel(formPanel, "Notes:", gbc, 12);
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        addComponent(formPanel, new JScrollPane(notesArea), gbc, 13);

        add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        
        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label, gbc);
    }
    
    private void addComponent(JPanel panel, JComponent comp, GridBagConstraints gbc, int row) {
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void loadData() {
        if (invoice.getInvoiceId() != null) { // Editing
            if (invoice.getCustomer() != null) customerCombo.setSelectedItem(invoice.getCustomer());
            if (invoice.getInvoiceDate() != null) dateField.setText(invoice.getInvoiceDate().format(dateFormatter));
            if (invoice.getDueDate() != null) dueDateField.setText(invoice.getDueDate().format(dateFormatter));
            statusCombo.setSelectedItem(invoice.getStatus());
            amountField.setText(invoice.getTotalAmount().toString());
            paidField.setText(invoice.getPaidAmount().toString());
            notesArea.setText(invoice.getNotes());
        }
    }

    private void onSave() {
        try {
            // Validation
            if (customerCombo.getSelectedItem() == null) {
                throw new Exception("Please select a customer");
            }
            
            // Update model
            invoice.setCustomer((Customer) customerCombo.getSelectedItem());
            invoice.setInvoiceDate(LocalDate.parse(dateField.getText(), dateFormatter));
            invoice.setDueDate(LocalDate.parse(dueDateField.getText(), dateFormatter));
            invoice.setStatus((Invoice.InvoiceStatus) statusCombo.getSelectedItem());
            invoice.setTotalAmount(new BigDecimal(amountField.getText()));
            invoice.setPaidAmount(new BigDecimal(paidField.getText()));
            invoice.setNotes(notesArea.getText());
            
            // Recalculate balance
            invoice.setBalanceAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
            
            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    public Invoice getInvoice() { return invoice; }
}
