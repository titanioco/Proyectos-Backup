package com.raven.accountability.ui.components;

import com.raven.accountability.model.Invoice;
import com.raven.accountability.service.InvoiceTemplateGenerator;
import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Invoice Viewer - displays invoice in HTML format
 */
public class InvoiceViewer extends JFrame {
    private Invoice invoice;
    private InvoiceTemplateGenerator templateGenerator;
    private JEditorPane htmlViewer;
    
    public InvoiceViewer(Invoice invoice, InvoiceTemplateGenerator templateGenerator) {
        this.invoice = invoice;
        this.templateGenerator = templateGenerator;
        
        initComponents();
        setupLayout();
        loadInvoiceHTML();
        
        setTitle("Invoice Viewer - " + invoice.getInvoiceNumber());
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        htmlViewer = new JEditorPane();
        htmlViewer.setContentType("text/html");
        htmlViewer.setEditable(false);
        htmlViewer.setBackground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("📄 Invoice: " + invoice.getInvoiceNumber());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel customerLabel = new JLabel("Customer: " + invoice.getCustomer().getCompanyName());
        customerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        customerLabel.setForeground(new Color(220, 221, 225));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(customerLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Viewer panel
        JScrollPane scrollPane = new JScrollPane(htmlViewer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        JButton printBtn = new JButton("🖨️ Print");
        printBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        printBtn.setBackground(new Color(52, 152, 219));
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.addActionListener(e -> printInvoice());
        
        JButton exportBtn = new JButton("📤 Export HTML");
        exportBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exportBtn.setBackground(new Color(39, 174, 96));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportInvoice());
        
        JButton closeBtn = new JButton("❌ Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setBackground(new Color(127, 140, 141));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(printBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(closeBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadInvoiceHTML() {
        try {
            String htmlContent = templateGenerator.generateHTMLInvoice(invoice);
            htmlViewer.setText(htmlContent);
            htmlViewer.setCaretPosition(0); // Scroll to top
        } catch (Exception e) {
            htmlViewer.setText("<html><body><h2>Error loading invoice</h2><p>" + e.getMessage() + "</p></body></html>");
        }
    }
    
    private void printInvoice() {
        try {
            boolean printed = htmlViewer.print();
            if (printed) {
                JOptionPane.showMessageDialog(this, 
                    "Invoice sent to printer successfully!",
                    "Print Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to print invoice: " + e.getMessage(),
                "Print Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportInvoice() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Invoice");
            fileChooser.setSelectedFile(new File(templateGenerator.generateInvoiceFilename(invoice)));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                templateGenerator.saveInvoiceToFile(invoice, file.getAbsolutePath());
                
                JOptionPane.showMessageDialog(this, 
                    "Invoice exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to export invoice: " + e.getMessage(),
                "Export Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
