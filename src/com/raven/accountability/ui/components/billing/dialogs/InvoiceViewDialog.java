package com.raven.accountability.ui.components.billing.dialogs;

import com.raven.accountability.model.Invoice;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Invoice View Dialog - Read-only view of invoice details
 */
public class InvoiceViewDialog extends JDialog {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public InvoiceViewDialog(Window parent, Invoice invoice) {
        super(parent, "Invoice Details - " + invoice.getInvoiceNumber(), ModalityType.MODELESS);
        
        initComponents(invoice);
        
        setSize(500, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents(Invoice invoice) {
        setLayout(new BorderLayout());
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("INVOICE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(20));

        // Details Grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 10));
        grid.setOpaque(false);
        
        addDetail(grid, "Invoice Number:", invoice.getInvoiceNumber());
        addDetail(grid, "Status:", invoice.getStatus().getDisplayName());
        addDetail(grid, "Date:", invoice.getInvoiceDate().format(dateFormatter));
        addDetail(grid, "Due Date:", invoice.getDueDate().format(dateFormatter));
        addDetail(grid, "Customer:", invoice.getCustomer() != null ? invoice.getCustomer().getCompanyName() : "N/A");
        addDetail(grid, "Email:", invoice.getCustomer() != null ? invoice.getCustomer().getEmail() : "N/A");
        
        content.add(grid);
        content.add(Box.createVerticalStrut(20));
        
        // Financials
        JPanel financials = new JPanel(new GridLayout(0, 2, 10, 10));
        financials.setOpaque(false);
        financials.setBorder(BorderFactory.createTitledBorder("Financials"));
        
        addDetail(financials, "Total Amount:", "$" + invoice.getTotalAmount());
        addDetail(financials, "Paid Amount:", "$" + invoice.getPaidAmount());
        addDetail(financials, "Balance Due:", "$" + invoice.getBalanceAmount());
        
        content.add(financials);
        content.add(Box.createVerticalStrut(20));
        
        // Notes
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            JLabel notesLabel = new JLabel("Notes:");
            notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            content.add(notesLabel);
            
            JTextArea notes = new JTextArea(invoice.getNotes());
            notes.setEditable(false);
            notes.setLineWrap(true);
            notes.setWrapStyleWord(true);
            notes.setBackground(new Color(250, 250, 250));
            content.add(new JScrollPane(notes));
        }

        add(new JScrollPane(content), BorderLayout.CENTER);
        
        // Close Button
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addDetail(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.GRAY);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panel.add(l);
        panel.add(v);
    }
}
