package com.raven.accountability.ui.components.billing;

import com.raven.accountability.model.Invoice;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import javax.swing.RowFilter;

/**
 * Invoice Table Panel - Handles the display, sorting, and filtering of invoices
 * Focused component for data presentation
 */
public class InvoiceTablePanel extends JPanel {
    
    private JTable invoicesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    
    // Data mapping
    private List<Invoice> currentInvoices;
    
    // Listeners
    private Runnable onSelectionChanged;
    private Runnable onDoubleClickListener;
    
    public InvoiceTablePanel() {
        this.currentInvoices = new ArrayList<>();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        
        // 1. Search and Filter Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        
        String[] statuses = {"All Statuses", "DRAFT", "SENT", "VIEWED", "PAID", "OVERDUE", "CANCELLED", "PARTIALLY_PAID"};
        statusFilter = new JComboBox<>(statuses);
        statusFilter.addActionListener(e -> filterTable());
        
        searchPanel.add(new JLabel("🔍 Search:"));
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(statusFilter);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // 2. Table Setup
        String[] columns = {"Invoice #", "Customer", "Date", "Due Date", "Status", "Amount", "Paid", "Balance"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        invoicesTable = new JTable(tableModel);
        invoicesTable.setRowHeight(40);
        invoicesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        invoicesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Styling
        invoicesTable.setBackground(new Color(255, 255, 255));
        invoicesTable.setSelectionBackground(new Color(66, 92, 128));
        invoicesTable.setSelectionForeground(Color.WHITE);
        invoicesTable.setGridColor(new Color(220, 221, 225));
        
        // Sorter
        tableSorter = new TableRowSorter<>(tableModel);
        invoicesTable.setRowSorter(tableSorter);
        
        // Listeners
        invoicesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && onSelectionChanged != null) {
                onSelectionChanged.run();
            }
        });
        
        invoicesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onDoubleClickListener != null) {
                    onDoubleClickListener.run();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(invoicesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 221, 225)));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void setInvoices(List<Invoice> invoices) {
        this.currentInvoices = new ArrayList<>(invoices);
        tableModel.setRowCount(0);
        
        for (Invoice invoice : invoices) {
            Object[] row = {
                invoice.getInvoiceNumber(),
                invoice.getCustomer() != null ? invoice.getCustomer().getCompanyName() : "N/A",
                invoice.getInvoiceDate().toString(),
                invoice.getDueDate().toString(),
                invoice.getStatus().toString(),
                "$" + invoice.getTotalAmount().toString(),
                "$" + invoice.getPaidAmount().toString(),
                "$" + invoice.getBalanceAmount().toString()
            };
            tableModel.addRow(row);
        }
    }
    
    public Invoice getSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) return null;
        
        int modelRow = invoicesTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < currentInvoices.size()) {
            // Note: This assumes the list order matches table model order (before sorting)
            // A safer way is to find by invoice number if list order isn't guaranteed
            // But since we reload the list and table together, it should be fine if we don't modify the list elsewhere
            // Actually, let's be safer and match by ID or Number from the table value
            String invoiceNum = (String) tableModel.getValueAt(modelRow, 0);
            return currentInvoices.stream()
                    .filter(i -> i.getInvoiceNumber().equals(invoiceNum))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    public void removeSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = invoicesTable.convertRowIndexToModel(selectedRow);
            String invoiceNum = (String) tableModel.getValueAt(modelRow, 0);
            currentInvoices.removeIf(i -> i.getInvoiceNumber().equals(invoiceNum));
            tableModel.removeRow(modelRow);
        }
    }
    
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        
        if (searchText.isEmpty() && "All Statuses".equals(selectedStatus)) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String invoiceNum = entry.getStringValue(0).toLowerCase();
                    String customer = entry.getStringValue(1).toLowerCase();
                    String status = entry.getStringValue(4);
                    
                    boolean matchesSearch = searchText.isEmpty() || 
                        invoiceNum.contains(searchText) || customer.contains(searchText);
                    boolean matchesStatus = "All Statuses".equals(selectedStatus) || 
                        status.equals(selectedStatus);
                    
                    return matchesSearch && matchesStatus;
                }
            });
        }
    }
    
    public void setOnSelectionChanged(Runnable listener) {
        this.onSelectionChanged = listener;
    }
    
    public void setOnDoubleClickListener(Runnable listener) {
        this.onDoubleClickListener = listener;
    }
    
    public boolean hasSelection() {
        return invoicesTable.getSelectedRow() != -1;
    }
}
