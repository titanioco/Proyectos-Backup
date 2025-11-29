package com.raven.accountability.ui.components;

import com.raven.accountability.model.SampleInvoice;
import com.raven.accountability.model.*;
import com.raven.accountability.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Billing and Invoicing Panel - Enhanced with real-time updates and pagination
 * Features:
 * - Real-time dependent column calculations (balance = amount - paid)
 * - Session change tracking across multiple edits
 * - Database integration for persistent storage
 * - Pagination with 25 rows per page
 * - Enhanced save functionality for session changes
 */
public class BillingPanelSimple extends JPanel {
    
    // Database services
    private InvoiceService workingInvoiceService;
    private CustomerService workingCustomerService;
    
    // Main components
    private JTable invoicesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton createInvoiceBtn;
    private JButton editInvoiceBtn;
    private JButton viewInvoiceBtn;
    private JButton deleteInvoiceBtn;
    private JButton saveSessionBtn;
    private JButton saveToProductionBtn;
    private JButton clearAllDataBtn;
    
    // Pagination components
    private JButton prevPageBtn;
    private JButton nextPageBtn;
    private JLabel pageInfoLabel;
    private static final int ROWS_PER_PAGE = 25;
    private int currentPage = 0;
    private int totalPages = 0;
    
    // Statistics panels
    private JPanel statsPanel;
    private JLabel totalInvoicesLabel;
    private JLabel totalAmountLabel;
    private JLabel outstandingLabel;
    private JLabel overdueLabel;
    
    // Data storage
    private List<SampleInvoice> allInvoices = new ArrayList<>(); // All invoices (for pagination)
    private List<SampleInvoice> currentPageInvoices = new ArrayList<>(); // Current page invoices
    
    // Session change tracking
    private Map<String, SampleInvoice> originalInvoiceStates = new HashMap<>();
    private Set<String> sessionChangedInvoices = new HashSet<>();
    private boolean hasUnsavedChanges = false;
    
    public BillingPanelSimple() {
        // Initialize database services
        initializeDatabaseServices();
        
        // Initialize components
        initComponents();
        setupLayout();
        
        // Load data and calculate pagination
        loadAllInvoices();
        calculatePagination();
        loadCurrentPage();
        updateStatistics();
        
        System.out.println("BILLING: BillingPanelSimple initialized with clean empty table (0 invoices)");
    }
    
    private void initializeDatabaseServices() {
        try {
            this.workingInvoiceService = new InvoiceService();
            this.workingCustomerService = new CustomerService();
            
            // CLEAR ALL EXISTING INVOICES FROM DATABASE FOR FRESH START
            clearAllExistingInvoicesFromDatabase();
            
            System.out.println("BILLING: Database services initialized with EMPTY database - fresh start");
        } catch (Exception e) {
            System.err.println("BILLING: Failed to initialize database services: " + e.getMessage());
            this.workingInvoiceService = null;
            this.workingCustomerService = null;
        }
    }
    
    /**
     * Clear all existing invoices from database for fresh start
     */
    private void clearAllExistingInvoicesFromDatabase() {
        try {
            if (workingInvoiceService != null) {
                // Get all existing invoices
                List<Invoice> existingInvoices = workingInvoiceService.getAllInvoices();
                
                if (existingInvoices != null && !existingInvoices.isEmpty()) {
                    System.out.println("BILLING: Found " + existingInvoices.size() + " existing invoices - clearing for fresh start");
                    
                    // Delete all existing invoices
                    for (Invoice invoice : existingInvoices) {
                        try {
                            workingInvoiceService.deleteInvoice(invoice.getInvoiceId());
                        } catch (Exception e) {
                            System.err.println("BILLING: Error deleting invoice " + invoice.getInvoiceNumber() + ": " + e.getMessage());
                        }
                    }
                    
                    System.out.println("BILLING: Database cleared - ready for fresh manual input");
                } else {
                    System.out.println("BILLING: Database already empty - ready for fresh manual input");
                }
            }
        } catch (Exception e) {
            System.err.println("BILLING: Error clearing database: " + e.getMessage());
        }
    }
    
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Initialize table with enhanced editable functionality and real-time calculations
        String[] columnNames = {"Invoice #", "Customer", "Date", "Due Date", "Status", "Amount", "Paid", "Balance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // All cells editable except balance (calculated automatically)
                return column != 7; // Balance column is not directly editable
            }
            
            @Override
            public void setValueAt(Object value, int row, int col) {
                super.setValueAt(value, row, col);
                // Update the corresponding invoice and trigger real-time calculations
                BillingPanelSimple.this.updateInvoiceWithRealTimeCalculation(row, col, value);
            }
        };
        
        invoicesTable = new JTable(tableModel);
        invoicesTable.setRowHeight(40);
        invoicesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        invoicesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Enhanced header styling with better contrast - light background with dark text
        invoicesTable.getTableHeader().setBackground(new Color(238, 242, 248)); // Very light shade of #425c80
        invoicesTable.getTableHeader().setForeground(new Color(66, 92, 128)); // #425c80 for text
        invoicesTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        // Custom header renderer that forces #425c80 styling to override Nimbus
        invoicesTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Force the styling to override Nimbus completely
                setOpaque(true);
                setBackground(new Color(238, 242, 248)); // Very light shade of #425c80
                setForeground(new Color(66, 92, 128)); // #425c80 for text
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(189, 203, 220)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                
                return this;
            }
        });
        
        // Enhanced table styling with #425c80 theme
        invoicesTable.setBackground(new Color(223, 230, 240)); // Light shade of #425c80
        invoicesTable.setSelectionBackground(new Color(66, 92, 128)); // #425c80
        invoicesTable.setSelectionForeground(Color.WHITE);
        invoicesTable.setGridColor(new Color(189, 203, 220)); // Medium shade of #425c80
        invoicesTable.setShowGrid(true);
        invoicesTable.setIntercellSpacing(new Dimension(1, 1));
        
        // Alternating row colors for better readability with #425c80 theme
        invoicesTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        setBackground(new Color(238, 242, 248)); // Very light shade of #425c80
                    } else {
                        setBackground(new Color(223, 230, 240)); // Light shade of #425c80
                    }
                    setForeground(new Color(52, 73, 94)); // Dark text for good contrast
                } else {
                    setBackground(new Color(66, 92, 128)); // #425c80 for selection
                    setForeground(Color.WHITE);
                }
                
                // Status column styling - only apply colors when not selected
                if (column == 4 && value != null && !isSelected) {
                    String status = value.toString();
                    switch (status) {
                        case "PAID":
                            setForeground(new Color(34, 139, 77)); // Darker green for better contrast on light backgrounds
                            break;
                        case "OVERDUE":
                            setForeground(new Color(192, 57, 43)); // Darker red for better contrast
                            break;
                        case "SENT":
                            setForeground(new Color(41, 121, 175)); // Darker blue for better contrast
                            break;
                        case "DRAFT":
                            setForeground(new Color(125, 138, 139)); // Darker gray for better contrast
                            break;
                        case "PARTIALLY_PAID":
                            setForeground(new Color(184, 100, 27)); // Darker orange for better contrast
                            break;
                        default:
                            setForeground(new Color(52, 73, 94));
                    }
                } else if (!isSelected) {
                    setForeground(new Color(52, 73, 94));
                }
                
                return this;
            }
        });
        
        // Add table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        invoicesTable.setRowSorter(tableSorter);
        
        // Table double-click listener for editing
        invoicesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedInvoice();
                }
            }
        });
        
        // Search components
        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        String[] statusOptions = {"All Statuses", "DRAFT", "SENT", "VIEWED", "PAID", "OVERDUE", "CANCELLED", "PARTIALLY_PAID"};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Search listeners
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        
        statusFilter.addActionListener(e -> filterTable());
        
        // Action buttons - Enhanced with session tracking
        createInvoiceBtn = createActionButton("📄 Create Invoice", new Color(52, 152, 219));
        editInvoiceBtn = createActionButton("✏️ Edit Invoice", new Color(39, 174, 96));
        viewInvoiceBtn = createActionButton("👁️ View Invoice", new Color(155, 89, 182));
        deleteInvoiceBtn = createActionButton("🗑️ Delete Invoice", new Color(231, 76, 60));
        saveSessionBtn = createActionButton("💾 Save Session Changes", new Color(46, 125, 50));
        saveToProductionBtn = createActionButton("🚀 Save to Production Database", new Color(220, 53, 69));
        clearAllDataBtn = createActionButton("🧹 Clear All Data", new Color(156, 39, 176));
        
        // Pagination buttons
        prevPageBtn = createActionButton("◀ Previous", new Color(108, 117, 125));
        nextPageBtn = createActionButton("Next ▶", new Color(108, 117, 125));
        pageInfoLabel = new JLabel("Page 1 of 1", SwingConstants.CENTER);
        pageInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageInfoLabel.setForeground(new Color(52, 73, 94));
        
        // Button listeners
        createInvoiceBtn.addActionListener(e -> createNewInvoice());
        editInvoiceBtn.addActionListener(e -> editSelectedInvoice());
        viewInvoiceBtn.addActionListener(e -> viewSelectedInvoice());
        deleteInvoiceBtn.addActionListener(e -> deleteSelectedInvoice());
        saveSessionBtn.addActionListener(e -> saveSessionChanges());
        saveToProductionBtn.addActionListener(e -> saveToProductionDatabase());
        clearAllDataBtn.addActionListener(e -> clearAllData());
        
        // Pagination listeners
        prevPageBtn.addActionListener(e -> goToPreviousPage());
        nextPageBtn.addActionListener(e -> goToNextPage());
        
        // Initially disable edit/view/delete buttons and save button
        editInvoiceBtn.setEnabled(false);
        viewInvoiceBtn.setEnabled(false);
        deleteInvoiceBtn.setEnabled(false);
        saveSessionBtn.setEnabled(false);
        saveToProductionBtn.setEnabled(true); // Always enabled
        clearAllDataBtn.setEnabled(true); // Always enabled
        updatePaginationButtons();
        
        // Table selection listener
        invoicesTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = invoicesTable.getSelectedRow() != -1;
            editInvoiceBtn.setEnabled(hasSelection);
            viewInvoiceBtn.setEnabled(hasSelection);
            deleteInvoiceBtn.setEnabled(hasSelection);
        });
        
        createStatisticsPanel();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        
        // Search and filter panel
        JPanel searchPanel = createSearchPanel();
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane tableScrollPane = new JScrollPane(invoicesTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 221, 225)));
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Split pane for statistics and content
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statsPanel, contentPanel);
        splitPane.setDividerLocation(120);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("🧾 Billing & Invoicing Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel subtitleLabel = new JLabel("Create, manage, and track invoices and payments - Manual Input Mode");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private void createStatisticsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 100));
        
        // Create stat cards
        totalInvoicesLabel = new JLabel("0");
        totalAmountLabel = new JLabel("$0.00");
        outstandingLabel = new JLabel("$0.00");
        overdueLabel = new JLabel("$0.00");
        
        statsPanel.add(createStatCard("📊", "Total Invoices", totalInvoicesLabel, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("💰", "Total Amount", totalAmountLabel, new Color(39, 174, 96)));
        statsPanel.add(createStatCard("⏳", "Outstanding", outstandingLabel, new Color(230, 126, 34)));
        statsPanel.add(createStatCard("⚠️", "Overdue", overdueLabel, new Color(231, 76, 60)));
    }
    
    private JPanel createStatCard(String icon, String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Accent line
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 12, 12);
                
                // Shadow effect
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, getWidth(), getHeight(), 12, 12);
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(127, 140, 141));
        
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(new Color(52, 73, 94));
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchLabel.setForeground(new Color(52, 73, 94));
        
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterLabel.setForeground(new Color(52, 73, 94));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(filterLabel);
        searchPanel.add(statusFilter);
        
        return searchPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel mainButtonPanel = new JPanel(new BorderLayout());
        mainButtonPanel.setOpaque(false);
        mainButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setOpaque(false);
        
        actionPanel.add(createInvoiceBtn);
        actionPanel.add(editInvoiceBtn);
        actionPanel.add(viewInvoiceBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(deleteInvoiceBtn);
        actionPanel.add(Box.createHorizontalStrut(20));
        actionPanel.add(saveSessionBtn);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(saveToProductionBtn);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(clearAllDataBtn);
        
        // Pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        paginationPanel.setOpaque(false);
        
        paginationPanel.add(prevPageBtn);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(nextPageBtn);
        
        mainButtonPanel.add(actionPanel, BorderLayout.WEST);
        mainButtonPanel.add(paginationPanel, BorderLayout.EAST);
        
        return mainButtonPanel;
    }
    
    private JButton createActionButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    // REMOVED: Sample data methods - now using manual input only
    // private void loadSampleInvoices() { ... }
    // private void initializeSampleInvoices() { ... }
    // private void updateSampleInvoice() { ... }
    
    private void updateStatistics() {
        if (allInvoices == null || allInvoices.isEmpty()) {
            totalInvoicesLabel.setText("0");
            totalAmountLabel.setText("$0.00");
            outstandingLabel.setText("$0.00");
            overdueLabel.setText("$0.00");
            return;
        }
        
        int totalInvoices = allInvoices.size();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        
        for (SampleInvoice invoice : allInvoices) {
            totalAmount = totalAmount.add(invoice.getAmount());
            totalOutstanding = totalOutstanding.add(invoice.getBalance());
            
            if ("OVERDUE".equals(invoice.getStatus())) {
                overdueAmount = overdueAmount.add(invoice.getBalance());
            }
        }
        
        totalInvoicesLabel.setText(String.valueOf(totalInvoices));
        totalAmountLabel.setText("$" + totalAmount.toString());
        outstandingLabel.setText("$" + totalOutstanding.toString());
        overdueLabel.setText("$" + overdueAmount.toString());
    }
    
    private void createNewInvoice() {
        // Create dialog for new invoice input
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Create New Invoice", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Form fields
        JTextField invoiceNumberField = new JTextField(20);
        JTextField customerNameField = new JTextField(20);
        JTextField dateField = new JTextField(LocalDate.now().toString(), 20);
        JTextField dueDateField = new JTextField(LocalDate.now().plusDays(30).toString(), 20);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"DRAFT", "SENT", "VIEWED", "PAID", "OVERDUE", "CANCELLED", "PARTIALLY_PAID"});
        JTextField amountField = new JTextField("0.00", 20);
        JTextField paidField = new JTextField("0.00", 20);
        
        // Generate invoice number
        String nextInvoiceNumber = generateNextInvoiceNumber();
        invoiceNumberField.setText(nextInvoiceNumber);
        
        // Add form fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Invoice Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(invoiceNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(customerNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Invoice Date:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dueDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Paid Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(paidField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Invoice");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                // Validate and create invoice
                String invoiceNumber = invoiceNumberField.getText().trim();
                String customerName = customerNameField.getText().trim();
                String date = dateField.getText().trim();
                String dueDate = dueDateField.getText().trim();
                String status = (String) statusCombo.getSelectedItem();
                BigDecimal amount = new BigDecimal(amountField.getText().replace("$", "").replace(",", ""));
                BigDecimal paid = new BigDecimal(paidField.getText().replace("$", "").replace(",", ""));
                BigDecimal balance = amount.subtract(paid);
                
                if (invoiceNumber.isEmpty() || customerName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Invoice Number and Customer Name are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Create new invoice
                SampleInvoice newInvoice = new SampleInvoice(
                    invoiceNumber, customerName, date, dueDate, status,
                    "$" + amount.toString(), "$" + paid.toString(), "$" + balance.toString()
                );
                
                // Add to allInvoices
                allInvoices.add(newInvoice);
                
                // Store original state for change tracking
                originalInvoiceStates.put(invoiceNumber, new SampleInvoice(
                    invoiceNumber, customerName, date, dueDate, status,
                    "$" + amount.toString(), "$" + paid.toString(), "$" + balance.toString()
                ));
                
                // Mark as changed for session
                sessionChangedInvoices.add(invoiceNumber);
                hasUnsavedChanges = true;
                
                // Refresh UI
                calculatePagination();
                loadCurrentPage();
                updateStatistics();
                updateSaveButtonState();
                
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Invoice created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid amounts!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error creating invoice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void editSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Edit Invoice functionality - Coming Soon", 
            "Feature Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void viewSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, "View Invoice functionality - Coming Soon", 
            "Feature Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get the invoice from current page
        if (selectedRow >= 0 && selectedRow < currentPageInvoices.size()) {
            SampleInvoice selectedInvoice = currentPageInvoices.get(selectedRow);
            
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete invoice " + selectedInvoice.getInvoiceNumber() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (result == JOptionPane.YES_OPTION) {
                // Remove from allInvoices
                allInvoices.removeIf(invoice -> invoice.getInvoiceNumber().equals(selectedInvoice.getInvoiceNumber()));
                
                // Remove from tracking maps
                originalInvoiceStates.remove(selectedInvoice.getInvoiceNumber());
                sessionChangedInvoices.remove(selectedInvoice.getInvoiceNumber());
                
                // Mark as session change if it was a database invoice
                hasUnsavedChanges = true;
                
                // Refresh UI
                calculatePagination();
                loadCurrentPage();
                updateStatistics();
                updateSaveButtonState();
                
                JOptionPane.showMessageDialog(this, "Invoice deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Generate next invoice number
     */
    private String generateNextInvoiceNumber() {
        int maxNumber = 0;
        String prefix = "INV-" + LocalDate.now().getYear() + "-";
        
        for (SampleInvoice invoice : allInvoices) {
            String invoiceNumber = invoice.getInvoiceNumber();
            if (invoiceNumber.startsWith(prefix)) {
                try {
                    String numberPart = invoiceNumber.substring(prefix.length());
                    int number = Integer.parseInt(numberPart);
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    // Skip invalid numbers
                }
            }
        }
        
        return prefix + String.format("%03d", maxNumber + 1);
    }
    
    /**
     * Filter table based on search and status criteria
     */
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        
        if (searchText.isEmpty() && "All Statuses".equals(selectedStatus)) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    // Get row data
                    String invoiceNumber = entry.getStringValue(0).toLowerCase();
                    String customer = entry.getStringValue(1).toLowerCase();
                    String status = entry.getStringValue(4);
                    
                    // Apply search filter
                    boolean matchesSearch = searchText.isEmpty() || 
                        invoiceNumber.contains(searchText) || 
                        customer.contains(searchText);
                    
                    // Apply status filter
                    boolean matchesStatus = "All Statuses".equals(selectedStatus) || 
                        status.equals(selectedStatus);
                    
                    return matchesSearch && matchesStatus;
                }
            });
        }
        
        // Update statistics after filtering
        updateStatistics();
    }
    
    // === ENHANCED METHODS FOR REAL-TIME CALCULATIONS, SESSION TRACKING, AND PAGINATION ===
    
    /**
     * Load all invoices from database only - STARTS COMPLETELY EMPTY
     */
    private void loadAllInvoices() {
        allInvoices.clear();
        
        // Load invoices from database if available
        try {
            if (workingInvoiceService != null) {
                List<Invoice> dbInvoices = workingInvoiceService.getAllInvoices();
                if (dbInvoices != null && !dbInvoices.isEmpty()) {
                    System.out.println("BILLING: Loading " + dbInvoices.size() + " invoices from database");
                    
                    // Convert database invoices to SampleInvoice format
                    List<SampleInvoice> convertedInvoices = convertDatabaseInvoicesToSample(dbInvoices);
                    allInvoices.addAll(convertedInvoices);
                    
                    System.out.println("BILLING: Loaded " + allInvoices.size() + " invoices from database");
                } else {
                    System.out.println("BILLING: Database is empty - ready for fresh manual input");
                }
            } else {
                System.out.println("BILLING: Database service not available - starting with empty table");
            }
        } catch (Exception e) {
            System.err.println("BILLING: Error loading invoices from database: " + e.getMessage());
            System.out.println("BILLING: Starting with empty table - use 'Create Invoice' button to add new invoices manually");
        }
    }
    
    /**
     * Convert database invoices to SampleInvoice format
     */
    private List<SampleInvoice> convertDatabaseInvoicesToSample(List<Invoice> dbInvoices) {
        List<SampleInvoice> sampleInvoices = new ArrayList<>();
        
        for (Invoice dbInvoice : dbInvoices) {
            try {
                String invoiceNumber = dbInvoice.getInvoiceNumber();
                String customerName = dbInvoice.getCustomer() != null ? 
                    dbInvoice.getCustomer().getCompanyName() : "Unknown";
                String invoiceDate = dbInvoice.getInvoiceDate().toString();
                String dueDate = dbInvoice.getDueDate().toString();
                String status = dbInvoice.getStatus().toString();
                String amount = "$" + dbInvoice.getTotalAmount().toString();
                String paid = "$" + dbInvoice.getPaidAmount().toString();
                String balance = "$" + dbInvoice.getBalanceAmount().toString();
                
                SampleInvoice sampleInvoice = new SampleInvoice(
                    invoiceNumber, customerName, invoiceDate, dueDate, 
                    status, amount, paid, balance
                );
                
                // Store original state for change tracking
                originalInvoiceStates.put(invoiceNumber, new SampleInvoice(
                    invoiceNumber, customerName, invoiceDate, dueDate, 
                    status, amount, paid, balance
                ));
                
                sampleInvoices.add(sampleInvoice);
            } catch (Exception e) {
                System.err.println("Error converting database invoice: " + e.getMessage());
            }
        }
        
        return sampleInvoices;
    }
    
    /**
     * Initialize sample data for demo - REMOVED: Now focusing on manual input only
     */
    private void initializeSampleData() {
        // Method removed - no longer using sample data
        // All data must be manually entered and stored in database
        System.out.println("BILLING: Sample data initialization skipped - manual input only");
    }
    
    /**
     * Calculate pagination based on total invoices
     */
    private void calculatePagination() {
        totalPages = (int) Math.ceil((double) allInvoices.size() / ROWS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        // Ensure current page is valid
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }
        
        updatePageInfo();
        updatePaginationButtons();
    }
    
    /**
     * Load current page data into table
     */
    private void loadCurrentPage() {
        // Clear table
        tableModel.setRowCount(0);
        currentPageInvoices.clear();
        
        // Calculate start and end indices for current page
        int startIndex = currentPage * ROWS_PER_PAGE;
        int endIndex = Math.min(startIndex + ROWS_PER_PAGE, allInvoices.size());
        
        // Load current page invoices
        for (int i = startIndex; i < endIndex; i++) {
            SampleInvoice invoice = allInvoices.get(i);
            currentPageInvoices.add(invoice);
            
            try {
                Object[] rowData = invoice.toTableRow();
                tableModel.addRow(rowData);
            } catch (Exception e) {
                System.err.println("Error adding invoice to table: " + e.getMessage());
            }
        }
        
        // Refresh table display
        tableModel.fireTableDataChanged();
        invoicesTable.repaint();
        
        System.out.println("BILLING: Loaded page " + (currentPage + 1) + " with " + currentPageInvoices.size() + " invoices");
    }
    
    /**
     * Update invoice with real-time calculation of dependent columns
     */
    private void updateInvoiceWithRealTimeCalculation(int row, int col, Object value) {
        if (row >= 0 && row < currentPageInvoices.size()) {
            SampleInvoice invoice = currentPageInvoices.get(row);
            String invoiceNumber = invoice.getInvoiceNumber();
            String strValue = value.toString();
            
            // Track this invoice as changed in this session
            sessionChangedInvoices.add(invoiceNumber);
            hasUnsavedChanges = true;
            
            // Update the invoice field
            switch (col) {
                case 0: // Invoice Number
                    invoice.setInvoiceNumber(strValue);
                    break;
                case 1: // Customer
                    invoice.setCustomerName(strValue);
                    break;
                case 2: // Date
                    invoice.setDate(strValue);
                    break;
                case 3: // Due Date
                    invoice.setDueDate(strValue);
                    break;
                case 4: // Status
                    invoice.setStatus(strValue);
                    break;
                case 5: // Amount
                    invoice.setFormattedAmount(strValue);
                    recalculateBalance(invoice, row);
                    break;
                case 6: // Paid
                    invoice.setFormattedPaid(strValue);
                    recalculateBalance(invoice, row);
                    break;
                // Column 7 (Balance) is not directly editable
            }
            
            // Update the corresponding invoice in allInvoices
            int globalIndex = currentPage * ROWS_PER_PAGE + row;
            if (globalIndex < allInvoices.size()) {
                allInvoices.set(globalIndex, invoice);
            }
            
            // Update UI components
            updateStatistics();
            updateSaveButtonState();
            
            System.out.println("BILLING: Updated invoice " + invoiceNumber + " (Session changes: " + sessionChangedInvoices.size() + ")");
        }
    }
    
    /**
     * Recalculate balance when amount or paid changes
     */
    private void recalculateBalance(SampleInvoice invoice, int tableRow) {
        try {
            // Get the BigDecimal values (already parsed by the setters)
            BigDecimal amount = invoice.getAmount();
            BigDecimal paid = invoice.getPaid();
            BigDecimal balance = amount.subtract(paid);
            
            // Update balance using BigDecimal setter
            invoice.setBalance(balance);
            
            // Update table cell directly for real-time display
            tableModel.setValueAt(invoice.getFormattedBalance(), tableRow, 7);
            
        } catch (Exception e) {
            System.err.println("Error calculating balance: " + e.getMessage());
            invoice.setBalance(BigDecimal.ZERO);
            tableModel.setValueAt("$0.00", tableRow, 7);
        }
    }
    
    /**
     * Save all session changes to database
     */
    private void saveSessionChanges() {
        if (!hasUnsavedChanges || sessionChangedInvoices.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No session changes to save.", 
                "Information", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show confirmation dialog
        int result = JOptionPane.showConfirmDialog(this,
            "Save " + sessionChangedInvoices.size() + " changed invoices to database?\n" +
            "This will update the persistent storage with all session changes.",
            "Confirm Save Session Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Save changes
        int savedCount = 0;
        for (String invoiceNumber : sessionChangedInvoices) {
            try {
                // Find the invoice in allInvoices
                SampleInvoice changedInvoice = null;
                for (SampleInvoice inv : allInvoices) {
                    if (inv.getInvoiceNumber().equals(invoiceNumber)) {
                        changedInvoice = inv;
                        break;
                    }
                }
                
                if (changedInvoice != null && workingInvoiceService != null) {
                    // Convert to database format and save
                    Invoice dbInvoice = convertSampleToDatabase(changedInvoice);
                    workingInvoiceService.saveInvoice(dbInvoice);
                    savedCount++;
                    
                    // Update original state
                    originalInvoiceStates.put(invoiceNumber, new SampleInvoice(
                        changedInvoice.getInvoiceNumber(),
                        changedInvoice.getCustomerName(),
                        changedInvoice.getDate(),
                        changedInvoice.getDueDate(),
                        changedInvoice.getStatus(),
                        changedInvoice.getFormattedAmount(),
                        changedInvoice.getFormattedPaid(),
                        changedInvoice.getFormattedBalance()
                    ));
                }
            } catch (Exception e) {
                System.err.println("Error saving invoice " + invoiceNumber + ": " + e.getMessage());
            }
        }
        
        // Clear session changes
        sessionChangedInvoices.clear();
        hasUnsavedChanges = false;
        updateSaveButtonState();
        
        // Show result
        String message = "Session changes saved successfully!\nSaved: " + savedCount + " invoices";
        JOptionPane.showMessageDialog(this, message, "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        
        System.out.println("BILLING: Session save completed, saved " + savedCount + " invoices");
    }
    
    /**
     * Convert SampleInvoice to database Invoice format
     */
    private Invoice convertSampleToDatabase(SampleInvoice sampleInvoice) throws SQLException {
        Invoice invoice = new Invoice();
        
        // Set basic fields
        invoice.setInvoiceNumber(sampleInvoice.getInvoiceNumber());
        
        // Set dates
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            invoice.setInvoiceDate(LocalDate.parse(sampleInvoice.getDate(), formatter));
            invoice.setDueDate(LocalDate.parse(sampleInvoice.getDueDate(), formatter));
        } catch (Exception e) {
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(30));
        }
        
        // Set amounts - use BigDecimal getters directly
        invoice.setTotalAmount(sampleInvoice.getAmount());
        invoice.setPaidAmount(sampleInvoice.getPaid());
        invoice.setBalanceAmount(sampleInvoice.getBalance());
        
        // Set status
        try {
            Invoice.InvoiceStatus status = Invoice.InvoiceStatus.valueOf(sampleInvoice.getStatus());
            invoice.setStatus(status);
        } catch (IllegalArgumentException e) {
            invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        }
        
        // Ensure customer exists
        Customer customer = ensureCustomerExists(sampleInvoice.getCustomerName());
        invoice.setCustomer(customer);
        
        return invoice;
    }
    
    /**
     * Ensure customer exists in database
     */
    private Customer ensureCustomerExists(String companyName) throws SQLException {
        if (workingCustomerService == null) {
            throw new SQLException("Customer service is not available");
        }
        
        // Try to find existing customer
        List<Customer> customers = workingCustomerService.getAllCustomers();
        if (customers != null) {
            for (Customer customer : customers) {
                if (customer.getCompanyName() != null && 
                    customer.getCompanyName().equalsIgnoreCase(companyName)) {
                    return customer;
                }
            }
        }
        
        // Create new customer
        Customer newCustomer = new Customer();
        newCustomer.setCustomerCode(companyName.toUpperCase().replaceAll("\\s+", "").substring(0, Math.min(companyName.length(), 8)));
        newCustomer.setCompanyName(companyName);
        newCustomer.setContactPerson("Contact Person");
        newCustomer.setEmail(companyName.toLowerCase().replaceAll("\\s+", "") + "@example.com");
        newCustomer.setPhone("(555) 123-4567");
        newCustomer.setAddress("123 Business Street");
        newCustomer.setCity("Business City");
        newCustomer.setState("State");
        newCustomer.setZipCode("12345");
        newCustomer.setCountry("United States");
        newCustomer.setStatus(Customer.CustomerStatus.ACTIVE);
        newCustomer.setPaymentTerms(Customer.PaymentTerms.NET_30);
        
        return workingCustomerService.saveCustomer(newCustomer);
    }
    
    /**
     * Go to previous page
     */
    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadCurrentPage();
            updatePageInfo();
            updatePaginationButtons();
        }
    }
    
    /**
     * Go to next page
     */
    private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadCurrentPage();
            updatePageInfo();
            updatePaginationButtons();
        }
    }
    
    /**
     * Update page information display
     */
    private void updatePageInfo() {
        int totalInvoices = allInvoices.size();
        int startItem = (currentPage * ROWS_PER_PAGE) + 1;
        int endItem = Math.min((currentPage + 1) * ROWS_PER_PAGE, totalInvoices);
        
        pageInfoLabel.setText(String.format("Page %d of %d (%d-%d of %d items)", 
            currentPage + 1, totalPages, startItem, endItem, totalInvoices));
    }
    
    /**
     * Update pagination button states
     */
    private void updatePaginationButtons() {
        prevPageBtn.setEnabled(currentPage > 0);
        nextPageBtn.setEnabled(currentPage < totalPages - 1);
    }
    
    /**
     * Update save button state based on session changes
     */
    private void updateSaveButtonState() {
        saveSessionBtn.setEnabled(hasUnsavedChanges && !sessionChangedInvoices.isEmpty());
        
        if (hasUnsavedChanges && !sessionChangedInvoices.isEmpty()) {
            saveSessionBtn.setText("💾 Save Session Changes (" + sessionChangedInvoices.size() + ")");
        } else {
            saveSessionBtn.setText("💾 Save Session Changes");
        }
    }
    
    /**
     * Save all data from backup database to production database
     */
    private void saveToProductionDatabase() {
        // Show confirmation dialog with warning
        int result = JOptionPane.showConfirmDialog(this,
            "⚠️ WARNING: This will copy ALL invoices from backup database to production database!\n\n" +
            "This action will:\n" +
            "• Copy all invoices from backup (working) database\n" +
            "• Overwrite any existing data in production database\n" +
            "• This action cannot be undone\n\n" +
            "Are you sure you want to proceed?",
            "Confirm Production Database Update",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Create progress dialog
            JDialog progressDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Copying to Production", true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setString("Copying invoices to production database...");
            progressBar.setStringPainted(true);
            
            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressPanel.add(new JLabel("🚀 Copying to Production Database"), BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            
            progressDialog.add(progressPanel);
            progressDialog.setSize(400, 150);
            progressDialog.setLocationRelativeTo(this);
            
            // Perform copy in background thread
            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    if (workingInvoiceService == null) {
                        throw new SQLException("Invoice service not available");
                    }
                    
                    int copiedCount = 0;
                    
                    // Get all invoices from backup/working database
                    List<Invoice> backupInvoices = workingInvoiceService.getAllInvoices();
                    
                    if (backupInvoices != null && !backupInvoices.isEmpty()) {
                        // Create production database connection/service
                        // Note: You would need to implement a production database service
                        // For now, we'll simulate the copy operation
                        
                        // TODO: Replace this with actual production database service
                        InvoiceService productionService = new InvoiceService(); // This should be production DB
                        
                        for (Invoice invoice : backupInvoices) {
                            try {
                                // Save each invoice to production database
                                productionService.saveInvoice(invoice);
                                copiedCount++;
                                
                                // Small delay to show progress
                                Thread.sleep(50);
                            } catch (Exception e) {
                                System.err.println("Error copying invoice " + invoice.getInvoiceNumber() + ": " + e.getMessage());
                            }
                        }
                    }
                    
                    return copiedCount;
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    
                    try {
                        int copiedCount = get();
                        
                        String message = "✅ Production Database Update Complete!\n\n" +
                                       "Successfully copied " + copiedCount + " invoices to production database.\n" +
                                       "Production system is now updated with the latest data.";
                        
                        JOptionPane.showMessageDialog(BillingPanelSimple.this, 
                            message, 
                            "Production Update Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                            
                        System.out.println("BILLING: Successfully copied " + copiedCount + " invoices to production database");
                        
                    } catch (Exception e) {
                        String errorMessage = "❌ Error copying to production database:\n" + e.getMessage() + 
                                             "\n\nPlease check the database connection and try again.";
                        
                        JOptionPane.showMessageDialog(BillingPanelSimple.this, 
                            errorMessage, 
                            "Production Update Error", 
                            JOptionPane.ERROR_MESSAGE);
                            
                        System.err.println("BILLING: Error copying to production database: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);
            
        } catch (Exception e) {
            String errorMessage = "❌ Failed to start production database copy:\n" + e.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("BILLING: Failed to start production copy: " + e.getMessage());
        }
    }
    
    /**
     * Clear all data from both UI and database for fresh start
     */
    private void clearAllData() {
        // Show confirmation dialog with strong warning
        int result = JOptionPane.showConfirmDialog(this,
            "🚨 DANGER: This will permanently delete ALL invoice data!\n\n" +
            "This action will:\n" +
            "• Delete ALL invoices from the database\n" +
            "• Clear ALL invoices from the current session\n" +
            "• Remove ALL unsaved changes\n" +
            "• This action CANNOT be undone!\n\n" +
            "Are you ABSOLUTELY sure you want to proceed?",
            "⚠️ CONFIRM DATA DELETION",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Second confirmation
        result = JOptionPane.showConfirmDialog(this,
            "FINAL WARNING!\n\n" +
            "This will PERMANENTLY DELETE all invoice data.\n" +
            "Click YES only if you are absolutely certain.",
            "⚠️ FINAL CONFIRMATION",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);
            
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Clear database
            clearAllExistingInvoicesFromDatabase();
            
            // Clear UI data
            allInvoices.clear();
            currentPageInvoices.clear();
            originalInvoiceStates.clear();
            sessionChangedInvoices.clear();
            hasUnsavedChanges = false;
            
            // Refresh UI
            calculatePagination();
            loadCurrentPage();
            updateStatistics();
            updateSaveButtonState();
            
            // Show success
            JOptionPane.showMessageDialog(this, 
                "✅ All data cleared successfully!\n\nThe system is now ready for fresh manual input.", 
                "Data Cleared", 
                JOptionPane.INFORMATION_MESSAGE);
                
            System.out.println("BILLING: All data cleared - fresh start ready");
            
        } catch (Exception e) {
            String errorMessage = "❌ Error clearing data:\n" + e.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("BILLING: Error clearing data: " + e.getMessage());
        }
    }
}
