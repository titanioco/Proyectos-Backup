package com.raven.accountability.ui.components;

import com.raven.model.User;
import com.raven.accountability.model.*;
import com.raven.accountability.model.SampleInvoice; // Explicit import to avoid conflict with local class
import com.raven.accountability.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Invoice Panel - Child component for invoice management
 * This panel is designed to be used as a child component within BillingAndInvoicingPanel
 * Provides complete invoice management functionality including creation, editing, and tracking
 */
public class InvoicePanel extends JPanel {
    private User currentUser;
    private InvoiceService invoiceService;
    private CustomerService customerService;
    // private InvoiceTemplateGenerator templateGenerator; // Reserved for future template functionality
    
    // Main components
    private JTable invoicesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton createInvoiceBtn;
    private JButton editInvoiceBtn;
    private JButton viewInvoiceBtn;
    private JButton deleteInvoiceBtn;
    private JButton confirmInvoiceBtn;
    private JButton saveSessionBtn;
    private JButton saveChangesBtn;
    private JButton permanentSaveBtn;
    private JButton clearAllBtn;
    private JButton refreshBtn;
    
    // Statistics panels
    private JPanel statsPanel;
    private JLabel totalInvoicesLabel;
    private JLabel totalAmountLabel;
    private JLabel outstandingLabel;
    private JLabel overdueLabel;
    
    // In-memory storage for sample invoices
    private List<SampleInvoice> sampleInvoices;
    private List<SampleInvoice> allInvoices; // Complete dataset for pagination
    
    // Pagination
    private int currentPage = 0;
    private int rowsPerPage = 25;
    private int totalPages = 0;
    private JButton prevPageBtn;
    private JButton nextPageBtn;
    private JLabel pageInfoLabel;
    
    // Track session changes
    private Set<String> modifiedInvoiceNumbers;
    private boolean hasUnsavedChanges = false;
    
    // Prevent infinite recursion in table updates
    private boolean isUpdatingTable = false;
    
    // Last input values for autofill
    private String lastCustomerName = "";
    private String lastDueDays = "30";
    private String lastStatus = "DRAFT";
    private String lastNotes = "";
    
    /**
     * Constructor
     */
    public InvoicePanel(User user, InvoiceService invoiceService, CustomerService customerService, 
                       InvoiceTemplateGenerator templateGenerator) {
        this.currentUser = user;
        
        // Initialize services with fallback creation if needed
        this.invoiceService = invoiceService;
        this.customerService = customerService;
        
        // Test and initialize services if they're null or non-functional
        if (this.invoiceService == null) {
            System.out.println("INVOICE PANEL: InvoiceService is null, attempting to create new instance...");
            try {
                this.invoiceService = new InvoiceService();
                System.out.println("INVOICE PANEL: Successfully created InvoiceService instance");
            } catch (Exception e) {
                System.err.println("INVOICE PANEL WARNING: Failed to create InvoiceService - " + e.getMessage());
                this.invoiceService = null;
            }
        }
        
        if (this.customerService == null) {
            System.out.println("INVOICE PANEL: CustomerService is null, attempting to create new instance...");
            try {
                this.customerService = new CustomerService();
                System.out.println("INVOICE PANEL: Successfully created CustomerService instance");
            } catch (Exception e) {
                System.err.println("INVOICE PANEL WARNING: Failed to create CustomerService - " + e.getMessage());
                this.customerService = null;
            }
        }
        
        // this.templateGenerator = templateGenerator; // Reserved for future template functionality
        
        // Initialize sample invoices for demo mode - now starts empty
        initializeSampleInvoices();
        
        initComponents();
        setupLayout();
        loadInvoices();
        
        // Refresh all invoice statuses to ensure they follow current business rules
        refreshAllInvoiceStatuses();
        
        updateStatistics();
        updateSaveButtonState();
        updatePaginationInfo();
    }
    
    /**
     * Initialize sample invoices - starts empty for real data
     */
    private void initializeSampleInvoices() {
        sampleInvoices = new ArrayList<>();
        allInvoices = new ArrayList<>();
        modifiedInvoiceNumbers = new HashSet<>();
        hasUnsavedChanges = false;
        System.out.println("INVOICE PANEL: Started with empty invoice list for real data entry");
    }
    
    /**
     * Load invoices from database or sample data
     */
    private void loadInvoices() {
        try {
            sampleInvoices.clear();
            allInvoices.clear();
            
            // Try to load from database first
            if (invoiceService != null) {
                try {
                    System.out.println("INVOICE PANEL: Loading invoices from database...");
                    List<Invoice> dbInvoices = invoiceService.getAllInvoices();
                    
                    // Convert database invoices to sample invoices for display
                    for (Invoice dbInvoice : dbInvoices) {
                        SampleInvoice sampleInvoice = convertDbInvoiceToSample(dbInvoice);
                        sampleInvoices.add(sampleInvoice);
                        allInvoices.add(sampleInvoice);
                    }
                    
                    System.out.println("INVOICE PANEL: Successfully loaded " + dbInvoices.size() + " invoices from database");
                } catch (Exception e) {
                    System.err.println("INVOICE PANEL: Error loading from database: " + e.getMessage());
                    // Keep empty list - no fallback to sample data
                }
            } else {
                System.out.println("INVOICE PANEL: InvoiceService not available, starting with empty list");
            }
            
            // Reset change tracking after loading fresh data
            modifiedInvoiceNumbers.clear();
            hasUnsavedChanges = false;
            updateSaveChangesButtonState();
            
            updateTableData();
            
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error in loadInvoices: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Convert database Invoice to SampleInvoice for display
     */
    private SampleInvoice convertDbInvoiceToSample(Invoice dbInvoice) {
        // Create SampleInvoice using the proper constructor with null-safety and formatting
        try {
            java.math.BigDecimal total = dbInvoice.getTotalAmount() != null ? dbInvoice.getTotalAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal paid = dbInvoice.getPaidAmount() != null ? dbInvoice.getPaidAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal balance = dbInvoice.getBalanceAmount() != null ? dbInvoice.getBalanceAmount() : java.math.BigDecimal.ZERO;

            return new SampleInvoice(
                dbInvoice.getInvoiceNumber(),
                dbInvoice.getCustomer() != null ? dbInvoice.getCustomer().getCompanyName() : "Unknown",
                dbInvoice.getInvoiceDate() != null ? dbInvoice.getInvoiceDate().toString() : java.time.LocalDate.now().toString(),
                dbInvoice.getDueDate() != null ? dbInvoice.getDueDate().toString() : java.time.LocalDate.now().toString(),
                dbInvoice.getStatus() != null ? dbInvoice.getStatus().toString() : "DRAFT",
                "$" + total.toString(),
                "$" + paid.toString(),
                "$" + balance.toString()
            );
        } catch (Exception ex) {
            System.err.println("INVOICE PANEL: Safe conversion fallback for invoice due to error: " + ex.getMessage());
            return new SampleInvoice(
                dbInvoice != null ? dbInvoice.getInvoiceNumber() : "",
                (dbInvoice != null && dbInvoice.getCustomer() != null) ? dbInvoice.getCustomer().getCompanyName() : "Unknown",
                dbInvoice != null && dbInvoice.getInvoiceDate() != null ? dbInvoice.getInvoiceDate().toString() : java.time.LocalDate.now().toString(),
                dbInvoice != null && dbInvoice.getDueDate() != null ? dbInvoice.getDueDate().toString() : java.time.LocalDate.now().toString(),
                dbInvoice != null && dbInvoice.getStatus() != null ? dbInvoice.getStatus().toString() : "DRAFT",
                "$0.00",
                "$0.00",
                "$0.00"
            );
        }
    }
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setOpaque(false);
        
        // Initialize table
        String[] columnNames = {"Invoice #", "Customer", "Date", "Due Date", "Status", "Amount", "Paid", "Balance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 7; // Invoice # and Balance are not editable (balance is calculated)
            }
            
            @Override
            public void setValueAt(Object value, int row, int col) {
                super.setValueAt(value, row, col);
                // Update the corresponding invoice and trigger real-time calculations
                InvoicePanel.this.updateInvoiceWithRealTimeCalculation(row, col, value);
            }
        };
        
        invoicesTable = new JTable(tableModel);
        invoicesTable.setRowHeight(40);
        invoicesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        invoicesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Set up custom cell editor that auto-selects text on double-click
        DefaultCellEditor textEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, 
                    boolean isSelected, int row, int column) {
                JTextField textField = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                // Auto-select all text when editing starts
                textField.selectAll();
                return textField;
            }
        };
        
        // Apply the text editor to all editable columns except status
        for (int i = 0; i < invoicesTable.getColumnCount(); i++) {
            if (i != 0 && i != 4 && i != 7) { // Skip Invoice #, Status column, and Balance
                invoicesTable.getColumnModel().getColumn(i).setCellEditor(textEditor);
            }
        }
        
        // Enhanced header styling
        invoicesTable.getTableHeader().setBackground(new Color(238, 242, 248));
        invoicesTable.getTableHeader().setForeground(new Color(66, 92, 128));
        invoicesTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        // Enhanced table styling with #425c80 theme
        invoicesTable.setBackground(new Color(223, 230, 240)); // Light shade of #425c80
        invoicesTable.setSelectionBackground(new Color(66, 92, 128)); // #425c80
        invoicesTable.setSelectionForeground(Color.WHITE);
        invoicesTable.setGridColor(new Color(189, 203, 220)); // Medium shade of #425c80
        invoicesTable.setShowGrid(true);
        invoicesTable.setIntercellSpacing(new Dimension(1, 1));
        
        // Set up status column dropdown editor with dynamic options
        JComboBox<String> statusComboBox = new JComboBox<>();
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Create custom cell editor that updates options based on invoice state
        DefaultCellEditor statusEditor = new DefaultCellEditor(statusComboBox) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, 
                    boolean isSelected, int row, int column) {
                // Update dropdown options based on current invoice state
                InvoicePanel.this.updateStatusDropdownOptions(statusComboBox, row);
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
        };
        
        invoicesTable.getColumnModel().getColumn(4).setCellEditor(statusEditor);
        
        // Enhanced row and status-based coloring with alternating rows
        invoicesTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    // Get status from the status column (column 4) for row background coloring
                    String status = (String) table.getValueAt(row, 4);
                    
                    // Apply status-based row background colors
                    if (status != null) {
                        switch (status) {
                            case "PAID":
                                // Light green background for paid invoices
                                setBackground(row % 2 == 0 ? new Color(232, 245, 233) : new Color(220, 237, 222));
                                break;
                            case "OVERDUE":
                                // Light red background for overdue invoices
                                setBackground(row % 2 == 0 ? new Color(255, 235, 238) : new Color(255, 223, 230));
                                break;
                            case "PARTIALLY_PAID":
                                // Light orange background for partially paid
                                setBackground(row % 2 == 0 ? new Color(255, 248, 225) : new Color(255, 243, 205));
                                break;
                            case "SENT":
                                // Light blue background for sent invoices
                                setBackground(row % 2 == 0 ? new Color(227, 242, 253) : new Color(212, 237, 252));
                                break;
                            case "UNPAID":
                                // Light yellow-red background for unpaid invoices
                                setBackground(row % 2 == 0 ? new Color(255, 245, 235) : new Color(255, 235, 215));
                                break;
                            case "DRAFT":
                            default:
                                // Light gray background for draft invoices (default alternating pattern)
                                if (row % 2 == 0) {
                                    setBackground(new Color(248, 249, 250)); // Very light gray
                                } else {
                                    setBackground(new Color(238, 242, 248)); // Slightly darker light gray
                                }
                                break;
                        }
                    } else {
                        // Default alternating rows if no status
                        if (row % 2 == 0) {
                            setBackground(new Color(248, 249, 250));
                        } else {
                            setBackground(new Color(238, 242, 248));
                        }
                    }
                    
                    setForeground(new Color(52, 73, 94)); // Dark text for good contrast
                } else {
                    setBackground(new Color(66, 92, 128)); // #425c80 for selection
                    setForeground(Color.WHITE);
                }
                
                // Status column text styling - only apply special colors to text when not selected
                if (column == 4 && value != null && !isSelected) {
                    String status = value.toString();
                    switch (status) {
                        case "PAID":
                            setForeground(new Color(27, 94, 32)); // Dark green for text
                            break;
                        case "OVERDUE":
                            setForeground(new Color(183, 28, 28)); // Dark red for text
                            break;
                        case "SENT":
                            setForeground(new Color(13, 71, 161)); // Dark blue for text
                            break;
                        case "DRAFT":
                            setForeground(new Color(97, 97, 97)); // Dark gray for text
                            break;
                        case "PARTIALLY_PAID":
                            setForeground(new Color(191, 97, 8)); // Dark orange for text
                            break;
                        case "UNPAID":
                            setForeground(new Color(200, 81, 0)); // Dark orange-red for text
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
        
        // Create buttons
        createInvoiceBtn = createStyledButton("Create Invoice", new Color(46, 204, 113));
        editInvoiceBtn = createStyledButton("Edit Invoice", new Color(52, 152, 219));
        viewInvoiceBtn = createStyledButton("View Invoice", new Color(155, 89, 182));
        deleteInvoiceBtn = createStyledButton("Delete Invoice", new Color(231, 76, 60));
        confirmInvoiceBtn = createStyledButton("Confirm Invoice", new Color(34, 139, 34));
        clearAllBtn = createStyledButton("Clear All Invoices", new Color(243, 156, 18));
        saveSessionBtn = createStyledButton("Database Info", new Color(34, 139, 34));
        saveChangesBtn = createStyledButton("Save Changes", new Color(46, 125, 50));
        saveChangesBtn.setEnabled(false); // Initially disabled until there are changes
        permanentSaveBtn = createStyledButton("Make Changes Permanent", new Color(220, 53, 69));
        permanentSaveBtn.setEnabled(false); // Initially disabled until there are changes
        refreshBtn = createStyledButton("Refresh from Database", new Color(108, 117, 125));
        
        // Create search components
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        statusFilter = new JComboBox<>(new String[]{"All", "DRAFT", "SENT", "UNPAID", "PARTIALLY_PAID", "PAID", "OVERDUE"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Create pagination components
        prevPageBtn = createStyledButton("← Previous", new Color(149, 165, 166));
        nextPageBtn = createStyledButton("Next →", new Color(149, 165, 166));
        pageInfoLabel = new JLabel("Page 1 of 1");
        pageInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Create statistics labels
        totalInvoicesLabel = new JLabel("0");
        totalAmountLabel = new JLabel("$0.00");  
        outstandingLabel = new JLabel("$0.00");
        overdueLabel = new JLabel("$0.00");
        
        // Create statistics panel
        createStatisticsPanel();
        
        setupEventHandlers();
    }
    
    /**
     * Create styled button
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        createInvoiceBtn.addActionListener(e -> createNewInvoice());
        editInvoiceBtn.addActionListener(e -> editSelectedInvoice());
        viewInvoiceBtn.addActionListener(e -> viewSelectedInvoice());
        deleteInvoiceBtn.addActionListener(e -> deleteSelectedInvoice());
        confirmInvoiceBtn.addActionListener(e -> confirmSelectedInvoice());
        clearAllBtn.addActionListener(e -> clearAllInvoices());
        saveSessionBtn.addActionListener(e -> showDatabaseInfo());
        saveChangesBtn.addActionListener(e -> saveSessionChanges());
        permanentSaveBtn.addActionListener(e -> makePermanentChanges());
        refreshBtn.addActionListener(e -> forceRefreshFromDatabase());
        
        prevPageBtn.addActionListener(e -> previousPage());
        nextPageBtn.addActionListener(e -> nextPage());
        
        searchField.addActionListener(e -> applyFilters());
        statusFilter.addActionListener(e -> applyFilters());
    }
    
    /**
     * Setup layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        
        // Statistics at top
        add(statsPanel, BorderLayout.NORTH);
        
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
        
        // Button and pagination panel
        JPanel bottomPanel = createBottomPanel();
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Create statistics panel
     */
    private void createStatisticsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 100));
        
        // Create stat cards without emojis
        statsPanel.add(createStatCard("Total Invoices", totalInvoicesLabel, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Total Amount", totalAmountLabel, new Color(39, 174, 96)));
        statsPanel.add(createStatCard("Outstanding", outstandingLabel, new Color(243, 156, 18)));
        statsPanel.add(createStatCard("Overdue", overdueLabel, new Color(231, 76, 60)));
    }

    /**
     * Create stat card
     */
    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
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
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(127, 140, 141));
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(new Color(52, 73, 94));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    /**
     * Create search panel
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Search:");
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

    /**
     * Create bottom panel with buttons and pagination
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        
        // Left-aligned buttons
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftButtonPanel.setOpaque(false);
        leftButtonPanel.add(createInvoiceBtn);
        leftButtonPanel.add(editInvoiceBtn);
        leftButtonPanel.add(viewInvoiceBtn);
        leftButtonPanel.add(confirmInvoiceBtn);
        leftButtonPanel.add(Box.createHorizontalStrut(10));
        leftButtonPanel.add(deleteInvoiceBtn);
        leftButtonPanel.add(clearAllBtn);
        leftButtonPanel.add(Box.createHorizontalStrut(10));
        leftButtonPanel.add(saveSessionBtn);
        leftButtonPanel.add(saveChangesBtn);
        leftButtonPanel.add(refreshBtn);
        
        // Right-aligned permanent save button
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.add(permanentSaveBtn);
        
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        
        // Pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.add(prevPageBtn);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(nextPageBtn);
        
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(paginationPanel, BorderLayout.SOUTH);
        
        return bottomPanel;
    }
    
    /**
     * Update table data
     */
    private void updateTableData() {
        tableModel.setRowCount(0);
        
        // Calculate pagination
        int totalInvoices = allInvoices.size();
        totalPages = (int) Math.ceil((double) totalInvoices / rowsPerPage);
        if (totalPages == 0) totalPages = 1;
        
        // Get current page data
        int startIndex = currentPage * rowsPerPage;
        int endIndex = Math.min(startIndex + rowsPerPage, totalInvoices);
        
        for (int i = startIndex; i < endIndex; i++) {
            SampleInvoice invoice = allInvoices.get(i);
            Object[] row = {
                invoice.getInvoiceNumber(),
                invoice.getCustomerName(),
                invoice.getDate(), // Direct string access
                invoice.getDueDate(), // Direct string access
                invoice.getStatus(),
                invoice.getFormattedAmount(),
                invoice.getFormattedPaid(),
                invoice.getFormattedBalance()
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Update invoice with real-time calculation of dependent columns
     */
    private void updateInvoiceWithRealTimeCalculation(int row, int col, Object value) {
        // Prevent infinite recursion from table updates
        if (isUpdatingTable) {
            return;
        }
        
        try {
            isUpdatingTable = true;
            
            // Get the invoice from the current page
            if (row < 0 || row >= getCurrentPageInvoices().size()) {
                return;
            }
            
            String invoiceNumber = (String) tableModel.getValueAt(row, 0);
            SampleInvoice invoice = findInvoiceByNumber(invoiceNumber);
            
            if (invoice != null) {
                String strValue = value != null ? value.toString() : "";
                
                // Update the invoice based on the column that changed
                switch (col) {
                    case 1: // Customer
                        invoice.setCustomerName(strValue);
                        break;
                    case 2: // Date
                        invoice.setDate(strValue);
                        break;
                    case 3: // Due Date
                        invoice.setDueDate(strValue);
                        // Only auto-update to OVERDUE if due date has passed
                        // DRAFT and SENT should not automatically change to payment statuses
                        try {
                            LocalDate dueDate = LocalDate.parse(strValue);
                            LocalDate today = LocalDate.now();
                            String currentStatus = invoice.getStatus();
                            
                            if (dueDate.isBefore(today) && !"OVERDUE".equals(currentStatus)) {
                                invoice.setStatus("OVERDUE");
                                tableModel.setValueAt("OVERDUE", row, 4);
                                System.out.println("INVOICE PANEL: Auto-updated to OVERDUE due to due date change for invoice " + invoice.getInvoiceNumber());
                            } else if (!dueDate.isBefore(today) && "OVERDUE".equals(currentStatus)) {
                                // If no longer overdue, only restore confirmed statuses automatically
                                if ("PAID".equals(invoice.getStatus()) || "UNPAID".equals(invoice.getStatus()) || "PARTIALLY_PAID".equals(invoice.getStatus())) {
                                    String restoredStatus = calculateAutomaticStatus(invoice);
                                    if (!restoredStatus.equals(currentStatus)) {
                                        invoice.setStatus(restoredStatus);
                                        tableModel.setValueAt(restoredStatus, row, 4);
                                        System.out.println("INVOICE PANEL: Restored status from OVERDUE to: " + restoredStatus);
                                    }
                                }
                                // For DRAFT and SENT, they remain OVERDUE until manually changed
                            }
                        } catch (Exception dateEx) {
                            System.err.println("INVOICE PANEL: Error parsing due date: " + dateEx.getMessage());
                        }
                        break;
                    case 4: // Status
                        // Validate that the status change is allowed
                        String requestedStatus = strValue;
                        String validatedStatus = validateStatusChange(invoice, requestedStatus);
                        if (!requestedStatus.equals(validatedStatus)) {
                            // Status change was not allowed, revert to validated status
                            tableModel.setValueAt(validatedStatus, row, 4);
                            JOptionPane.showMessageDialog(this, 
                                "Status '" + requestedStatus + "' is not valid for this invoice.\n" +
                                "Status has been set to '" + validatedStatus + "' instead.", 
                                "Invalid Status Change", 
                                JOptionPane.WARNING_MESSAGE);
                            invoice.setStatus(validatedStatus);
                        } else {
                            invoice.setStatus(strValue);
                        }
                        break;
                    case 5: // Amount
                        try {
                            String cleanAmount = strValue.replace("$", "").replace(",", "").trim();
                            if (!cleanAmount.isEmpty()) {
                                // Validate that it's a valid number
                                BigDecimal amount = new BigDecimal(cleanAmount);
                                invoice.setAmount(amount); // Use BigDecimal setter to ensure proper update
                                System.out.println("INVOICE PANEL: Updated amount to: " + amount);
                                
                                // Immediately update the balance column in the table
                                BigDecimal newBalance = amount.subtract(invoice.getPaid());
                                invoice.setBalance(newBalance);
                                tableModel.setValueAt("$" + newBalance.toString(), row, 7); // Update balance column
                                
                                System.out.println("INVOICE PANEL: Invoice " + invoice.getInvoiceNumber() + " now has amount: " + invoice.getAmount() + ", paid: " + invoice.getPaid() + ", balance: " + invoice.getBalance());
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("INVOICE PANEL: Invalid amount format: " + strValue);
                            // Keep the original formatted amount if parsing fails
                        }
                        recalculateBalance(invoice, row);
                        break;
                    case 6: // Paid
                        try {
                            String cleanPaid = strValue.replace("$", "").replace(",", "").trim();
                            if (!cleanPaid.isEmpty()) {
                                // Validate that it's a valid number
                                BigDecimal paid = new BigDecimal(cleanPaid);
                                invoice.setPaid(paid); // Use BigDecimal setter to ensure proper update
                                System.out.println("INVOICE PANEL: Updated paid amount to: " + paid);
                                
                                // Immediately update the balance column in the table
                                BigDecimal newBalance = invoice.getAmount().subtract(paid);
                                invoice.setBalance(newBalance);
                                tableModel.setValueAt("$" + newBalance.toString(), row, 7); // Update balance column
                                
                                System.out.println("INVOICE PANEL: Invoice " + invoice.getInvoiceNumber() + " now has amount: " + invoice.getAmount() + ", paid: " + invoice.getPaid() + ", balance: " + invoice.getBalance());
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("INVOICE PANEL: Invalid paid amount format: " + strValue);
                            // Keep the original formatted paid amount if parsing fails
                        }
                        recalculateBalance(invoice, row);
                        break;
                    // Column 7 (Balance) is not directly editable
                }
                
                // Track changes for all invoices that came from the database
                // Since we load invoices from database at startup, any invoice in our list
                // that has a proper invoice number should be trackable
                boolean shouldTrackChange = false;
                
                try {
                    if (invoiceService != null && invoiceNumber != null && !invoiceNumber.trim().isEmpty()) {
                        // Try to verify the invoice exists in database
                        Invoice dbInvoice = invoiceService.findInvoiceByNumber(invoiceNumber);
                        shouldTrackChange = (dbInvoice != null);
                        
                        if (shouldTrackChange) {
                            System.out.println("INVOICE PANEL: Confirmed invoice " + invoiceNumber + " exists in database");
                        } else {
                            System.out.println("INVOICE PANEL: Invoice " + invoiceNumber + " not found in database");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("INVOICE PANEL: Error checking invoice existence: " + e.getMessage());
                    // If we can't check, but the invoice has a valid format, assume it might exist
                    // This ensures we don't lose changes due to temporary database issues
                    if (invoiceNumber != null && invoiceNumber.matches("INV-\\d{8}-\\d+")) {
                        shouldTrackChange = true;
                        System.out.println("INVOICE PANEL: Database check failed, but tracking change for valid invoice number " + invoiceNumber);
                    }
                }
                
                if (shouldTrackChange) {
                    // Track the change for database persistence
                    modifiedInvoiceNumbers.add(invoiceNumber);
                    hasUnsavedChanges = true;
                    System.out.println("INVOICE PANEL: Tracked change for invoice " + invoiceNumber + " - " + 
                                     getColumnName(col) + " = " + strValue);
                } else {
                    System.out.println("INVOICE PANEL: Updated local-only invoice " + invoiceNumber + " - " + 
                                     getColumnName(col) + " = " + strValue + " (not tracked for database save)");
                }
                
                updateSaveChangesButtonState();
                
                // Update statistics
                updateStatistics();
            }
        } catch (Exception e) {
            System.err.println("Error updating invoice: " + e.getMessage());
        } finally {
            isUpdatingTable = false;
        }
    }
    
    /**
     * Recalculate balance after amount or paid amount changes
     */
    private void recalculateBalance(SampleInvoice invoice, int row) {
        try {
            BigDecimal amount = invoice.getAmount();
            BigDecimal paid = invoice.getPaid();
            BigDecimal balance = amount.subtract(paid);
            
            // Prevent negative balance
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("INVOICE PANEL: Negative balance detected, adjusting paid amount");
                JOptionPane.showMessageDialog(this, 
                    "Paid amount cannot exceed invoice amount. Adjusting paid amount to match invoice amount.", 
                    "Invalid Payment Amount", 
                    JOptionPane.WARNING_MESSAGE);
                
                // Set paid amount to equal invoice amount (resulting in zero balance)
                invoice.setPaid(amount); // Use BigDecimal setter to ensure proper update
                // Update the paid amount column immediately
                if (!isUpdatingTable) {
                    boolean wasUpdating = isUpdatingTable;
                    isUpdatingTable = true;
                    try {
                        tableModel.setValueAt("$" + amount.toString(), row, 6);
                    } finally {
                        isUpdatingTable = wasUpdating;
                    }
                }
                balance = BigDecimal.ZERO;
            }
            
            // Update the invoice balance (this should already be done by setPaid/setAmount, but ensure consistency)
            invoice.setBalance(balance);
            
            // Update all amount-related columns in the table to reflect the current values
            // Use direct table model updates but prevent triggering setValueAt recursion
            if (!isUpdatingTable) {
                boolean wasUpdating = isUpdatingTable;
                isUpdatingTable = true;
                try {
                    tableModel.setValueAt("$" + invoice.getAmount().toString(), row, 5); // Amount column
                    tableModel.setValueAt("$" + invoice.getPaid().toString(), row, 6);   // Paid column
                    tableModel.setValueAt("$" + invoice.getBalance().toString(), row, 7); // Balance column
                } finally {
                    isUpdatingTable = wasUpdating;
                }
            }
            
            // Only auto-update status for confirmed invoices (PAID, UNPAID, PARTIALLY_PAID)
            // DRAFT and SENT statuses require manual confirmation workflow
            String currentStatus = invoice.getStatus();
            if ("PAID".equals(currentStatus) || "UNPAID".equals(currentStatus) || "PARTIALLY_PAID".equals(currentStatus)) {
                String newStatus = calculateAutomaticStatus(invoice);
                if (!newStatus.equals(currentStatus)) {
                    invoice.setStatus(newStatus);
                    tableModel.setValueAt(newStatus, row, 4);
                    System.out.println("INVOICE PANEL: Auto-updated confirmed invoice status to: " + newStatus + " for invoice " + invoice.getInvoiceNumber());
                }
            }
            // For DRAFT and SENT, only check for overdue status
            else if ("DRAFT".equals(currentStatus) || "SENT".equals(currentStatus)) {
                LocalDate dueDate = LocalDate.parse(invoice.getDueDate());
                LocalDate today = LocalDate.now();
                if (dueDate.isBefore(today) && !"OVERDUE".equals(currentStatus)) {
                    invoice.setStatus("OVERDUE");
                    tableModel.setValueAt("OVERDUE", row, 4);
                    System.out.println("INVOICE PANEL: Auto-updated to OVERDUE for invoice " + invoice.getInvoiceNumber());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error recalculating balance: " + e.getMessage());
        }
    }
    
    /**
     * Calculate the automatic status for an invoice based on business rules
     * PAID, UNPAID, PARTIALLY_PAID can only be reached after SENT -> CONFIRMED workflow
     */
    private String calculateAutomaticStatus(SampleInvoice invoice) {
        try {
            LocalDate dueDate = LocalDate.parse(invoice.getDueDate());
            LocalDate today = LocalDate.now();
            String currentStatus = invoice.getStatus();
            
            // Rule 1: DRAFT and SENT are initial statuses - no automatic changes
            if ("DRAFT".equals(currentStatus) || "SENT".equals(currentStatus)) {
                // Check for overdue only if due date has passed
                if (dueDate.isBefore(today)) {
                    return "OVERDUE";
                }
                return currentStatus;
            }
            
            // Rule 2: For confirmed statuses (PAID, UNPAID, PARTIALLY_PAID), check overdue first
            if (dueDate.isBefore(today)) {
                return "OVERDUE";
            }
            
            // Rule 3: For confirmed statuses, recalculate based on payment amounts
            if ("PAID".equals(currentStatus) || "UNPAID".equals(currentStatus) || "PARTIALLY_PAID".equals(currentStatus)) {
                BigDecimal amount = invoice.getAmount();
                BigDecimal paid = invoice.getPaid();
                BigDecimal balance = invoice.getBalance();
                
                // Only apply payment logic if amount > 0 (valid invoice to pay)
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    if (balance.compareTo(BigDecimal.ZERO) == 0 && paid.equals(amount)) {
                        return "PAID";
                    } else if (paid.compareTo(BigDecimal.ZERO) == 0) {
                        return "UNPAID";
                    } else if (paid.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
                        return "PARTIALLY_PAID";
                    }
                }
            }
            
            // Rule 4: OVERDUE status - check if it can be restored to appropriate payment status
            if ("OVERDUE".equals(currentStatus) && !dueDate.isBefore(today)) {
                // If no longer overdue and was previously a confirmed status, recalculate payment status
                BigDecimal amount = invoice.getAmount();
                BigDecimal paid = invoice.getPaid();
                BigDecimal balance = invoice.getBalance();
                
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    if (balance.compareTo(BigDecimal.ZERO) == 0 && paid.equals(amount)) {
                        return "PAID";
                    } else if (paid.compareTo(BigDecimal.ZERO) == 0) {
                        return "UNPAID";
                    } else if (paid.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
                        return "PARTIALLY_PAID";
                    }
                }
            }
            
            // Fallback to current status if no rules apply
            return currentStatus;
            
        } catch (Exception e) {
            System.err.println("Error calculating automatic status: " + e.getMessage());
            return invoice.getStatus(); // Return current status if calculation fails
        }
    }
    
    /**
     * Update status dropdown options based on invoice state to prevent invalid selections
     * Only DRAFT and SENT are allowed as initial statuses
     * PAID, UNPAID, PARTIALLY_PAID only after SENT -> CONFIRMED workflow
     */
    private void updateStatusDropdownOptions(JComboBox<String> comboBox, int row) {
        try {
            comboBox.removeAllItems();
            
            // Get the invoice data for this row
            if (row < 0 || row >= getCurrentPageInvoices().size()) {
                // Fallback options if row is invalid - only initial statuses
                comboBox.addItem("DRAFT");
                comboBox.addItem("SENT");
                return;
            }
            
            String invoiceNumber = (String) tableModel.getValueAt(row, 0);
            SampleInvoice invoice = findInvoiceByNumber(invoiceNumber);
            
            if (invoice == null) {
                // Fallback options if invoice not found - only initial statuses
                comboBox.addItem("DRAFT");
                comboBox.addItem("SENT");
                return;
            }
            
            LocalDate dueDate = LocalDate.parse(invoice.getDueDate());
            LocalDate today = LocalDate.now();
            String currentStatus = invoice.getStatus();
            
            // Always add the current status first
            comboBox.addItem(currentStatus);
            
            // Determine valid status options based on business rules
            
            // If due date has passed, OVERDUE is the only additional valid status
            if (dueDate.isBefore(today)) {
                if (!"OVERDUE".equals(currentStatus)) {
                    comboBox.addItem("OVERDUE");
                }
                return; // No other options when overdue
            }
            
            // Status transition rules:
            if ("DRAFT".equals(currentStatus)) {
                // DRAFT can only move to SENT
                addIfNotExists(comboBox, "SENT");
                return;
            }
            
            if ("SENT".equals(currentStatus)) {
                // SENT can move back to DRAFT (for editing) but NOT to payment statuses directly
                // Payment statuses (PAID, UNPAID, PARTIALLY_PAID) only via CONFIRM button
                addIfNotExists(comboBox, "DRAFT");
                return;
            }
            
            // For confirmed payment statuses (PAID, UNPAID, PARTIALLY_PAID), no manual transitions allowed
            // These statuses are only changed via the confirm button or automatic calculations
            if ("PAID".equals(currentStatus) || "UNPAID".equals(currentStatus) || "PARTIALLY_PAID".equals(currentStatus)) {
                // These statuses cannot be manually changed - only via confirmation workflow
                return;
            }
            
            // OVERDUE can potentially be restored if due date is updated
            if ("OVERDUE".equals(currentStatus)) {
                // No manual transitions from OVERDUE - only automatic when due date changes
                return;
            }
            
            // Fallback - add only initial status options for unknown states
            addIfNotExists(comboBox, "DRAFT");
            addIfNotExists(comboBox, "SENT");
            
        } catch (Exception e) {
            System.err.println("Error updating status dropdown options: " + e.getMessage());
            // Fallback options - only initial statuses
            comboBox.removeAllItems();
            comboBox.addItem("DRAFT");
            comboBox.addItem("SENT");
        }
    }
    
    /**
     * Helper method to add item to combo box if it doesn't already exist
     */
    private void addIfNotExists(JComboBox<String> comboBox, String item) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (item.equals(comboBox.getItemAt(i))) {
                return; // Item already exists
            }
        }
        comboBox.addItem(item);
    }
    
    /**
     * Validate that a status change is allowed based on business rules
     * PAID, UNPAID, PARTIALLY_PAID can only be reached via SENT -> CONFIRMED workflow
     */
    private String validateStatusChange(SampleInvoice invoice, String requestedStatus) {
        try {
            LocalDate dueDate = LocalDate.parse(invoice.getDueDate());
            LocalDate today = LocalDate.now();
            String currentStatus = invoice.getStatus();
            
            // Rule 1: If due date has passed, only OVERDUE is allowed
            if (dueDate.isBefore(today)) {
                if (!"OVERDUE".equals(requestedStatus)) {
                    System.out.println("INVOICE PANEL: Status change to '" + requestedStatus + "' not allowed - invoice is overdue");
                    return "OVERDUE";
                }
                return requestedStatus; // OVERDUE is valid
            }
            
            // Rule 2: PAID, UNPAID, PARTIALLY_PAID cannot be set manually - only via CONFIRM button
            if ("PAID".equals(requestedStatus) || "UNPAID".equals(requestedStatus) || "PARTIALLY_PAID".equals(requestedStatus)) {
                if (!"PAID".equals(currentStatus) && !"UNPAID".equals(currentStatus) && !"PARTIALLY_PAID".equals(currentStatus)) {
                    System.out.println("INVOICE PANEL: Status change to '" + requestedStatus + "' not allowed - use Confirm Invoice button for payment statuses");
                    return currentStatus;
                }
                // If already in a payment status, don't allow manual changes
                System.out.println("INVOICE PANEL: Payment statuses cannot be changed manually - use Confirm Invoice button");
                return currentStatus;
            }
            
            // Rule 3: Cannot set to OVERDUE if due date hasn't passed
            if ("OVERDUE".equals(requestedStatus) && !dueDate.isBefore(today)) {
                System.out.println("INVOICE PANEL: Status change to 'OVERDUE' not allowed - due date has not passed");
                return currentStatus;
            }
            
            // Rule 4: Validate workflow transitions for initial statuses
            if ("DRAFT".equals(currentStatus)) {
                // DRAFT can only move to SENT
                if ("SENT".equals(requestedStatus) || "DRAFT".equals(requestedStatus)) {
                    return requestedStatus;
                }
                System.out.println("INVOICE PANEL: DRAFT can only move to SENT");
                return "DRAFT";
            }
            
            if ("SENT".equals(currentStatus)) {
                // SENT can move back to DRAFT for editing
                if ("DRAFT".equals(requestedStatus) || "SENT".equals(requestedStatus)) {
                    return requestedStatus;
                }
                System.out.println("INVOICE PANEL: SENT can only move to DRAFT for editing, use Confirm Invoice button for payment statuses");
                return "SENT";
            }
            
            // Rule 5: Payment statuses cannot be changed manually
            if ("PAID".equals(currentStatus) || "UNPAID".equals(currentStatus) || "PARTIALLY_PAID".equals(currentStatus)) {
                System.out.println("INVOICE PANEL: Payment statuses cannot be changed manually - use Confirm Invoice button");
                return currentStatus;
            }
            
            // Rule 6: OVERDUE cannot be changed manually (only through due date updates)
            if ("OVERDUE".equals(currentStatus)) {
                System.out.println("INVOICE PANEL: OVERDUE status cannot be changed manually - update due date if needed");
                return currentStatus;
            }
            
            // Fallback: allow the requested status if no rules prevent it (should not happen with proper UI)
            return requestedStatus;
            
        } catch (Exception e) {
            System.err.println("Error validating status change: " + e.getMessage());
            return invoice.getStatus(); // Return current status if validation fails
        }
    }
    
    /**
     * Refresh all invoice statuses based on current business rules
     * This method can be called manually when needed (e.g., after data load or on demand)
     */
    public void refreshAllInvoiceStatuses() {
        try {
            boolean anyStatusChanged = false;
            System.out.println("INVOICE PANEL: Refreshing all invoice statuses based on current business rules...");
            
            for (int i = 0; i < sampleInvoices.size(); i++) {
                SampleInvoice invoice = sampleInvoices.get(i);
                String currentStatus = invoice.getStatus();
                String calculatedStatus = calculateAutomaticStatus(invoice);
                
                if (!currentStatus.equals(calculatedStatus)) {
                    invoice.setStatus(calculatedStatus);
                    anyStatusChanged = true;
                    System.out.println("INVOICE PANEL: Updated status for invoice " + invoice.getInvoiceNumber() + 
                                     " from '" + currentStatus + "' to '" + calculatedStatus + "'");
                    
                    // Track change if it's a database invoice
                    try {
                        if (invoiceService != null && invoice.getInvoiceNumber() != null) {
                            Invoice dbInvoice = invoiceService.findInvoiceByNumber(invoice.getInvoiceNumber());
                            if (dbInvoice != null) {
                                modifiedInvoiceNumbers.add(invoice.getInvoiceNumber());
                                hasUnsavedChanges = true;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("INVOICE PANEL: Error checking database invoice for tracking: " + e.getMessage());
                    }
                }
            }
            
            if (anyStatusChanged) {
                // Refresh the display
                updateTableData();
                updateStatistics();
                updateSaveChangesButtonState();
                System.out.println("INVOICE PANEL: Status refresh completed with changes");
            } else {
                System.out.println("INVOICE PANEL: Status refresh completed - no changes needed");
            }
            
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error refreshing invoice statuses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find invoice by number in current data
     */
    private SampleInvoice findInvoiceByNumber(String invoiceNumber) {
        for (SampleInvoice invoice : sampleInvoices) {
            if (invoice.getInvoiceNumber().equals(invoiceNumber)) {
                return invoice;
            }
        }
        return null;
    }
    
    /**
     * Get current page invoices for display
     */
    private java.util.List<SampleInvoice> getCurrentPageInvoices() {
        int startIndex = currentPage * rowsPerPage;
        int endIndex = Math.min(startIndex + rowsPerPage, allInvoices.size());
        return allInvoices.subList(startIndex, endIndex);
    }
    
    /**
     * Get column name for logging
     */
    private String getColumnName(int col) {
        String[] columnNames = {"Invoice #", "Customer", "Date", "Due Date", "Status", "Amount", "Paid", "Balance"};
        return col >= 0 && col < columnNames.length ? columnNames[col] : "Unknown";
    }
    
    /**
     * Generate next invoice number
     */
    private String generateNextInvoiceNumber() {
        String prefix = "INV-";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Find the highest existing invoice number to avoid skipping
        int maxNumber = 0;
        for (SampleInvoice invoice : allInvoices) {
            String invoiceNum = invoice.getInvoiceNumber();
            // Extract number from format like "INV-20250802-001"
            if (invoiceNum.contains("-")) {
                String[] parts = invoiceNum.split("-");
                if (parts.length >= 3) {
                    try {
                        int num = Integer.parseInt(parts[parts.length - 1]);
                        maxNumber = Math.max(maxNumber, num);
                    } catch (NumberFormatException e) {
                        // Ignore if format is unexpected
                    }
                }
            }
        }
        
        int nextNumber = maxNumber + 1;
        return prefix + datePart + "-" + String.format("%03d", nextNumber);
    }
    
    /**
     * Find or create customer by name
     */
    private Customer findOrCreateCustomer(String customerName) {
        try {
            if (customerService != null) {
                // Try to find existing customer
                List<Customer> customers = customerService.getAllCustomers();
                for (Customer customer : customers) {
                    if (customer.getCompanyName() != null && 
                        customer.getCompanyName().equalsIgnoreCase(customerName)) {
                        return customer;
                    }
                }
                
                // Create new customer if not found
                Customer newCustomer = new Customer();
                newCustomer.setCompanyName(customerName);
                newCustomer.setContactPerson(customerName);
                newCustomer.setEmail(customerName.toLowerCase().replace(" ", ".") + "@example.com");
                
                Customer savedCustomer = customerService.saveCustomer(newCustomer);
                if (savedCustomer != null) {
                    System.out.println("INVOICE PANEL: Created new customer: " + customerName);
                    return savedCustomer;
                }
            }
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error finding/creating customer: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Create new invoice
     */
    private void createNewInvoice() {
        try {
            // Create input dialog for new invoice
            JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "Create New Invoice", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(500, 600);
            dialog.setLocationRelativeTo(this);
            
            // Create form components
            JTextField invoiceNumberField = new JTextField(generateNextInvoiceNumber());
            invoiceNumberField.setEditable(false);
            
            JTextField customerNameField = new JTextField(lastCustomerName, 20);
            
            JTextField invoiceDateField = new JTextField(LocalDate.now().toString(), 10);
            
            JTextField dueDaysField = new JTextField(lastDueDays, 5);
            
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"DRAFT", "SENT"});
            statusCombo.setSelectedItem(lastStatus);
            
            JTextField amountField = new JTextField("0.00", 10);
            JTextField paidField = new JTextField("0.00", 10);
            
            JTextArea notesArea = new JTextArea(lastNotes, 4, 20);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            
            // Create form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Add form fields
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Invoice Number:"), gbc);
            gbc.gridx = 1;
            formPanel.add(invoiceNumberField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Customer Name:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(customerNameField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
            formPanel.add(new JLabel("Invoice Date:"), gbc);
            gbc.gridx = 1;
            formPanel.add(invoiceDateField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Due Days:"), gbc);
            gbc.gridx = 1;
            formPanel.add(dueDaysField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            formPanel.add(statusCombo, gbc);
            
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Amount ($):"), gbc);
            gbc.gridx = 1;
            formPanel.add(amountField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 6;
            formPanel.add(new JLabel("Paid ($):"), gbc);
            gbc.gridx = 1;
            formPanel.add(paidField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 7;
            formPanel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
            formPanel.add(new JScrollPane(notesArea), gbc);
            
            // Create buttons
            JButton saveButton = createStyledButton("Save Invoice", new Color(46, 204, 113));
            JButton cancelButton = createStyledButton("Cancel", new Color(149, 165, 166));
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            // Layout dialog
            dialog.setLayout(new BorderLayout());
            dialog.add(new JLabel("Create New Invoice", JLabel.CENTER), BorderLayout.NORTH);
            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            final boolean[] invoiceCreated = {false};
            
            // Save button action
            saveButton.addActionListener(e -> {
                try {
                    // Validate input
                    String customerName = customerNameField.getText().trim();
                    if (customerName.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Please enter a customer name");
                        return;
                    }
                    
                    String invoiceNumber = invoiceNumberField.getText().trim();
                    String invoiceDate = invoiceDateField.getText().trim();
                    String dueDaysStr = dueDaysField.getText().trim();
                    String status = (String) statusCombo.getSelectedItem();
                    String amountStr = amountField.getText().trim();
                    String paidStr = paidField.getText().trim();
                    String notes = notesArea.getText().trim();
                    
                    // Parse and validate numeric fields
                    BigDecimal amount;
                    BigDecimal paid;
                    int dueDays;
                    
                    try {
                        amount = new BigDecimal(amountStr);
                        if (amount.compareTo(BigDecimal.ZERO) < 0) {
                            JOptionPane.showMessageDialog(dialog, "Amount cannot be negative");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Please enter a valid amount");
                        return;
                    }
                    
                    try {
                        paid = new BigDecimal(paidStr);
                        if (paid.compareTo(BigDecimal.ZERO) < 0) {
                            JOptionPane.showMessageDialog(dialog, "Paid amount cannot be negative");
                            return;
                        }
                        if (paid.compareTo(amount) > 0) {
                            JOptionPane.showMessageDialog(dialog, "Paid amount cannot be greater than total amount");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Please enter a valid paid amount");
                        return;
                    }
                    
                    try {
                        dueDays = Integer.parseInt(dueDaysStr);
                        if (dueDays < 0) {
                            JOptionPane.showMessageDialog(dialog, "Due days cannot be negative");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Please enter a valid number of due days");
                        return;
                    }
                    
                    // Calculate due date
                    LocalDate dueDate = LocalDate.parse(invoiceDate).plusDays(dueDays);
                    BigDecimal balance = amount.subtract(paid);
                    
                    // Create SampleInvoice
                    SampleInvoice newInvoice = new SampleInvoice(
                        invoiceNumber,
                        customerName,
                        invoiceDate,
                        dueDate.toString(),
                        status,
                        "$" + amount.toString(),
                        "$" + paid.toString(),
                        "$" + balance.toString()
                    );
                    
                    // Try to save to database if service is available
                    boolean savedToDb = false;
                    if (invoiceService != null) {
                        try {
                            // Create database Invoice object
                            Invoice dbInvoice = new Invoice();
                            dbInvoice.setInvoiceNumber(invoiceNumber);
                            dbInvoice.setInvoiceDate(LocalDate.parse(invoiceDate));
                            dbInvoice.setDueDate(dueDate);
                            dbInvoice.setStatus(Invoice.InvoiceStatus.valueOf(status));
                            dbInvoice.setTotalAmount(amount);
                            dbInvoice.setPaidAmount(paid);
                            dbInvoice.setBalanceAmount(balance);
                            dbInvoice.setNotes(notes);
                            dbInvoice.setCreatedBy(currentUser != null ? currentUser.getEmail() : "System");
                            
                            // Create or find customer
                            Customer customer = findOrCreateCustomer(customerName);
                            if (customer != null) {
                                dbInvoice.setCustomer(customer);
                            }
                            
                            // Save to database
                            Invoice savedInvoice = invoiceService.saveInvoice(dbInvoice);
                            if (savedInvoice != null) {
                                savedToDb = true;
                                System.out.println("INVOICE PANEL: Successfully saved invoice to database: " + invoiceNumber);
                            }
                        } catch (Exception ex) {
                            System.err.println("INVOICE PANEL: Error saving to database: " + ex.getMessage());
                            // Continue with local save
                        }
                    }
                    
                    // Add to local lists
                    sampleInvoices.add(newInvoice);
                    allInvoices.add(newInvoice);
                    
                    // Update autofill values
                    lastCustomerName = customerName;
                    lastDueDays = dueDaysStr;
                    lastStatus = status;
                    lastNotes = notes;
                    
                    // Refresh display
                    updateTableData();
                    updateStatistics();
                    updatePaginationInfo();
                    
                    invoiceCreated[0] = true;
                    dialog.dispose();
                    
                    String message = savedToDb ? 
                        "Invoice created successfully and saved to database!" :
                        "Invoice created successfully (local storage only)!";
                    JOptionPane.showMessageDialog(this, message);
                    
                } catch (Exception ex) {
                    System.err.println("Error creating invoice: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Error creating invoice: " + ex.getMessage());
                }
            });
            
            // Cancel button action
            cancelButton.addActionListener(e -> dialog.dispose());
            
            // Show dialog
            dialog.setVisible(true);
            
        } catch (Exception e) {
            System.err.println("Error opening create invoice dialog: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening create invoice dialog: " + e.getMessage());
        }
    }
    
    /**
     * Edit selected invoice
     */
    private void editSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to edit");
            return;
        }
        JOptionPane.showMessageDialog(this, "Edit Invoice functionality would be implemented here");
    }
    
    /**
     * View selected invoice
     */
    private void viewSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to view");
            return;
        }
        JOptionPane.showMessageDialog(this, "View Invoice functionality would be implemented here");
    }
    
    /**
     * Confirm selected invoice - allows SENT invoices to move to payment states
     */
    /**
     * Confirm selected invoice - allows SENT invoices to move to payment states
     * This is the ONLY way to reach PAID, UNPAID, or PARTIALLY_PAID statuses
     */
    private void confirmSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to confirm");
            return;
        }
        
        // Get the invoice number and current status
        String invoiceNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 4);
        
        // Only allow confirmation for SENT invoices
        if (!"SENT".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, 
                "Only invoices with 'SENT' status can be confirmed.\nCurrent status: " + currentStatus + 
                "\n\nTo confirm an invoice:\n1. Set status to 'SENT'\n2. Set amount > 0\n3. Click 'Confirm Invoice'",
                "Invalid Invoice Status", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SampleInvoice invoice = findInvoiceByNumber(invoiceNumber);
        if (invoice == null) {
            JOptionPane.showMessageDialog(this, "Invoice not found");
            return;
        }
        
        // Check that the invoice has a valid amount to pay
        BigDecimal amount = invoice.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                "Cannot confirm invoice: Amount must be greater than zero.\nCurrent amount: $" + amount,
                "Invalid Invoice Amount",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Determine the appropriate status based on current amounts
        BigDecimal paid = invoice.getPaid();
        BigDecimal balance = invoice.getBalance();
        String targetStatus;
        String statusExplanation;
        
        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            targetStatus = "UNPAID";
            statusExplanation = "No payment received yet";
        } else if (balance.compareTo(BigDecimal.ZERO) == 0 && paid.equals(amount)) {
            targetStatus = "PAID";
            statusExplanation = "Fully paid";
        } else if (paid.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
            targetStatus = "PARTIALLY_PAID";
            statusExplanation = "Partial payment received";
        } else {
            // This shouldn't happen with proper validation, but handle it
            targetStatus = "UNPAID";
            statusExplanation = "Status determined by payment amounts";
        }
        
        // Show confirmation dialog
        String message = "Client has confirmed receipt of invoice " + invoiceNumber + ".\n\n" +
                        "Invoice Details:\n" +
                        "• Customer: " + invoice.getCustomerName() + "\n" +
                        "• Amount: $" + amount + "\n" +
                        "• Paid: $" + paid + "\n" +
                        "• Balance: $" + balance + "\n\n" +
                        "Based on the payment amounts, this invoice will be moved to:\n" +
                        "► " + targetStatus + " (" + statusExplanation + ")\n\n" +
                        "Do you want to proceed?";
        
        int choice = JOptionPane.showConfirmDialog(this,
                message,
                "Confirm Invoice - " + invoiceNumber,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Update the invoice status
            invoice.setStatus(targetStatus);
            tableModel.setValueAt(targetStatus, selectedRow, 4);
            
            // Track change for database persistence
            try {
                if (invoiceService != null && invoiceNumber != null) {
                    Invoice dbInvoice = invoiceService.findInvoiceByNumber(invoiceNumber);
                    if (dbInvoice != null) {
                        modifiedInvoiceNumbers.add(invoiceNumber);
                        hasUnsavedChanges = true;
                        updateSaveChangesButtonState();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error tracking invoice change: " + e.getMessage());
            }
            
            // Update statistics
            updateStatistics();
            
            System.out.println("INVOICE PANEL: Confirmed invoice " + invoiceNumber + " -> " + targetStatus);
            JOptionPane.showMessageDialog(this, 
                "Invoice " + invoiceNumber + " has been confirmed and moved to " + targetStatus + " status.",
                "Invoice Confirmed",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Delete selected invoice
     */
    private void deleteSelectedInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to delete");
            return;
        }
        
        // Get the invoice number from the selected row
        String invoiceNumber = (String) tableModel.getValueAt(selectedRow, 0);
        
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete invoice " + invoiceNumber + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Try to delete from database first
                if (invoiceService != null) {
                    // Find invoice by number to get the ID
                    Invoice dbInvoice = invoiceService.findInvoiceByNumber(invoiceNumber);
                    if (dbInvoice != null) {
                        boolean deleted = invoiceService.deleteInvoice(dbInvoice.getInvoiceId());
                        if (deleted) {
                            JOptionPane.showMessageDialog(this, "Invoice deleted successfully from database");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to delete invoice from database");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Invoice not found in database");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error deleting from database: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error deleting from database: " + e.getMessage());
            }
            
            // Also remove from local lists
            sampleInvoices.removeIf(inv -> inv.getInvoiceNumber().equals(invoiceNumber));
            allInvoices.removeIf(inv -> inv.getInvoiceNumber().equals(invoiceNumber));
            
            // Remove from change tracking if it was modified
            modifiedInvoiceNumbers.remove(invoiceNumber);
            if (modifiedInvoiceNumbers.isEmpty()) {
                hasUnsavedChanges = false;
            }
            updateSaveChangesButtonState();
            
            // Refresh display
            updateTableData();
            updateStatistics();
            updatePaginationInfo();
        }
    }
    
    /**
     * Clear all invoices
     */
    private void clearAllInvoices() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete ALL invoices?\nThis will permanently remove all invoice data from the database.\nThis action cannot be undone.",
                "Confirm Clear All Invoices",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Clear from database
                if (invoiceService != null) {
                    List<Invoice> allDbInvoices = invoiceService.getAllInvoices();
                    int deletedCount = 0;
                    for (Invoice invoice : allDbInvoices) {
                        try {
                            if (invoiceService.deleteInvoice(invoice.getInvoiceId())) {
                                deletedCount++;
                            }
                        } catch (Exception e) {
                            System.err.println("Error deleting invoice " + invoice.getInvoiceNumber() + ": " + e.getMessage());
                        }
                    }
                    JOptionPane.showMessageDialog(this, 
                        "Successfully cleared " + deletedCount + " invoices from database");
                } else {
                    JOptionPane.showMessageDialog(this, "InvoiceService not available");
                }
            } catch (Exception e) {
                System.err.println("Error clearing database: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error clearing database: " + e.getMessage());
            }
            
            // Clear local lists
            sampleInvoices.clear();
            allInvoices.clear();
            
            // Reset change tracking
            modifiedInvoiceNumbers.clear();
            hasUnsavedChanges = false;
            updateSaveChangesButtonState();
            
            // Reset pagination
            currentPage = 0;
            
            // Update display
            updateTableData();
            updateStatistics();
            updatePaginationInfo();
        }
    }
    
    /**
     * Show database information
     */
    private void showDatabaseInfo() {
        try {
            if (invoiceService != null) {
                List<Invoice> allDbInvoices = invoiceService.getAllInvoices();
                int totalCount = allDbInvoices.size();
                
                StringBuilder info = new StringBuilder();
                info.append("Database Information:\n\n");
                info.append("Total Invoices in Database: ").append(totalCount).append("\n");
                info.append("Currently Displayed: ").append(sampleInvoices.size()).append("\n\n");
                
                if (totalCount > 0) {
                    info.append("Recent Invoices:\n");
                    int showCount = Math.min(5, totalCount);
                    for (int i = 0; i < showCount; i++) {
                        Invoice inv = allDbInvoices.get(i);
                        String customerName = inv.getCustomer() != null ? inv.getCustomer().getCompanyName() : "Unknown";
                        info.append("- ").append(inv.getInvoiceNumber())
                            .append(" (").append(customerName).append(")\n");
                    }
                    if (totalCount > 5) {
                        info.append("... and ").append(totalCount - 5).append(" more\n");
                    }
                }
                
                JOptionPane.showMessageDialog(this, info.toString(), 
                    "Database Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database service not available", 
                    "Database Information", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error accessing database: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Update statistics
     */
    private void updateStatistics() {
        int totalInvoices = sampleInvoices.size();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        
        LocalDate today = LocalDate.now();
        
        for (SampleInvoice invoice : sampleInvoices) {
            totalAmount = totalAmount.add(invoice.getAmount());
            BigDecimal balance = invoice.getBalance(); // Use getBalance() directly
            
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                totalOutstanding = totalOutstanding.add(balance);
                
                // Parse due date string and compare
                try {
                    LocalDate dueDate = LocalDate.parse(invoice.getDueDate());
                    if (dueDate.isBefore(today)) {
                        totalOverdue = totalOverdue.add(balance);
                    }
                } catch (Exception e) {
                    // If date parsing fails, skip overdue calculation
                    System.err.println("Could not parse due date: " + invoice.getDueDate());
                }
            }
        }
        
        totalInvoicesLabel.setText(String.valueOf(totalInvoices));
        totalAmountLabel.setText("$" + totalAmount);
        outstandingLabel.setText("$" + totalOutstanding);
        overdueLabel.setText("$" + totalOverdue);
    }
    
    /**
     * Update save button state
     */
    private void updateSaveButtonState() {
        saveSessionBtn.setText("Database Info");
        saveSessionBtn.setEnabled(true);
        
        // Also update the permanent save button
        updateSaveChangesButtonState();
    }
    
    /**
     * Update pagination info
     */
    private void updatePaginationInfo() {
        int totalInvoices = allInvoices.size();
        totalPages = totalInvoices == 0 ? 1 : (int) Math.ceil((double) totalInvoices / rowsPerPage);
        
        pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + 
                             " (" + totalInvoices + " total)");
        
        prevPageBtn.setEnabled(currentPage > 0);
        nextPageBtn.setEnabled(currentPage < totalPages - 1);
    }
    
    /**
     * Go to previous page
     */
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTableData();
            updatePaginationInfo();
        }
    }
    
    /**
     * Go to next page
     */
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTableData();
            updatePaginationInfo();
        }
    }
    
    /**
     * Apply filters
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String statusText = (String) statusFilter.getSelectedItem();
        
        allInvoices.clear();
        
        for (SampleInvoice invoice : sampleInvoices) {
            boolean matchesSearch = searchText.isEmpty() || 
                invoice.getInvoiceNumber().toLowerCase().contains(searchText) ||
                invoice.getCustomerName().toLowerCase().contains(searchText);
            
            boolean matchesStatus = "All".equals(statusText) || 
                invoice.getStatus().equals(statusText);
            
            if (matchesSearch && matchesStatus) {
                allInvoices.add(invoice);
            }
        }
        
        currentPage = 0;
        updateTableData();
        updatePaginationInfo();
    }
    
    /**
     * Public methods for external use
     */
    public int getNextInvoiceNumber() {
        // Find the highest existing invoice number to avoid skipping
        int maxNumber = 0;
        for (SampleInvoice invoice : allInvoices) {
            String invoiceNum = invoice.getInvoiceNumber();
            // Extract number from format like "INV-20250802-001"
            if (invoiceNum.contains("-")) {
                String[] parts = invoiceNum.split("-");
                if (parts.length >= 3) {
                    try {
                        int num = Integer.parseInt(parts[parts.length - 1]);
                        maxNumber = Math.max(maxNumber, num);
                    } catch (NumberFormatException e) {
                        // Ignore if format is unexpected
                    }
                }
            }
        }
        return maxNumber + 1;
    }
    
    public void addConvertedInvoice(SampleInvoice invoice) {
        sampleInvoices.add(invoice);
        allInvoices.add(invoice);
        updateTableData();
        updateStatistics();
        updatePaginationInfo();
    }
    
    public void refreshData() {
        loadInvoices();
        updateStatistics();
        updatePaginationInfo();
    }
    
    public boolean canLogout() {
        if (hasUnsavedChanges) {
            int choice = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Do you want to save them before logging out?",
                "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                saveSessionChanges();
                return true;
            } else if (choice == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false; // Cancel logout
            }
        }
        return true;
    }
    
    /**
     * Update the save changes button state based on whether there are unsaved changes
     */
    private void updateSaveChangesButtonState() {
        if (saveChangesBtn != null) {
            saveChangesBtn.setEnabled(hasUnsavedChanges);
            if (hasUnsavedChanges) {
                saveChangesBtn.setText("Save Changes (" + modifiedInvoiceNumbers.size() + ")");
            } else {
                saveChangesBtn.setText("Save Changes");
            }
        }
        
        // Update the permanent save button state - this button should only be enabled
        // when there are actual changes to make permanent (modified invoices or new invoices)
        if (permanentSaveBtn != null) {
            boolean hasChangesToMakePermanent = hasUnsavedChanges || hasNewInvoicesNotInDatabase();
            permanentSaveBtn.setEnabled(hasChangesToMakePermanent);
            
            if (hasChangesToMakePermanent) {
                int changeCount = modifiedInvoiceNumbers.size();
                permanentSaveBtn.setText("Make Changes Permanent (" + changeCount + ")");
            } else {
                permanentSaveBtn.setText("Make Changes Permanent");
            }
        }
    }
    
    /**
     * Save all session changes to the database with confirmation and progress bar
     */
    private void saveSessionChanges() {
        if (!hasUnsavedChanges || modifiedInvoiceNumbers.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No changes to save.", 
                "Save Changes", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show confirmation dialog
        String message = "Are you sure you want to save " + modifiedInvoiceNumbers.size() + 
                        " modified invoice(s) to the permanent database?\n\n" +
                        "Modified invoices:\n";
        
        // List the modified invoices
        int count = 0;
        for (String invoiceNumber : modifiedInvoiceNumbers) {
            if (count < 10) { // Show max 10 invoice numbers
                message += "• " + invoiceNumber + "\n";
                count++;
            } else {
                message += "• ... and " + (modifiedInvoiceNumbers.size() - 10) + " more\n";
                break;
            }
        }
        
        message += "\nThis action will permanently update the database and cannot be undone.";
        
        int choice = JOptionPane.showConfirmDialog(this,
                message,
                "Confirm Save to Database",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Check if invoice service is available
        if (invoiceService == null) {
            JOptionPane.showMessageDialog(this, 
                "Invoice service not available. Cannot save changes to database.", 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create and show progress dialog with proper modal handling
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog progressDialog = new JDialog(parentWindow instanceof Frame ? (Frame) parentWindow : null, "Saving Changes", true);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(450, 180);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setResizable(false);
        
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel statusLabel = new JLabel("Initializing save operation...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JProgressBar progressBar = new JProgressBar(0, modifiedInvoiceNumbers.size());
        progressBar.setStringPainted(true);
        progressBar.setString("0 / " + modifiedInvoiceNumbers.size());
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        // Add a cancel button for safety
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cancelButton.setEnabled(false); // Initially disabled, will be enabled after a delay
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cancelButton);
        
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        progressDialog.add(progressPanel);
        
        // Save changes in background thread with improved error handling
        SwingWorker<Boolean, String> saveWorker = new SwingWorker<Boolean, String>() {
            private int savedCount = 0;
            private int errorCount = 0;
            private StringBuilder errorMessages = new StringBuilder();
            private volatile boolean cancelled = false;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    System.out.println("INVOICE PANEL: Starting save worker for " + modifiedInvoiceNumbers.size() + " invoices");
                    
                    publish("Initializing database connection...");
                    setProgress(0);
                    Thread.sleep(300); // Brief delay to show initialization
                    
                    if (invoiceService == null) {
                        throw new Exception("Invoice service is not available");
                    }
                    
                    int progress = 0;
                    int totalInvoices = modifiedInvoiceNumbers.size();
                    
                    for (String invoiceNumber : modifiedInvoiceNumbers) {
                        if (cancelled) {
                            System.out.println("INVOICE PANEL: Save operation cancelled");
                            break;
                        }
                        
                        publish("Processing invoice: " + invoiceNumber + " (" + (progress + 1) + "/" + totalInvoices + ")");
                        
                        try {
                            System.out.println("INVOICE PANEL: Processing invoice " + invoiceNumber);
                            
                            SampleInvoice sampleInvoice = findInvoiceByNumber(invoiceNumber);
                            if (sampleInvoice == null) {
                                throw new Exception("Invoice " + invoiceNumber + " not found in memory");
                            }
                            
                            // Convert SampleInvoice back to database Invoice
                            Invoice dbInvoice = convertSampleToDbInvoice(sampleInvoice);
                            if (dbInvoice == null) {
                                throw new Exception("Failed to convert invoice " + invoiceNumber + " for database save");
                            }
                            
                            // Save to database using the improved service method
                            // The service will automatically determine whether to insert or update
                            Invoice savedInvoice = invoiceService.saveInvoice(dbInvoice);
                            
                            if (savedInvoice != null) {
                                savedCount++;
                                System.out.println("INVOICE PANEL: Successfully saved invoice " + invoiceNumber);
                                publish("✓ Saved: " + invoiceNumber);
                                
                                // Verify the save
                                try {
                                    Invoice verifyInvoice = invoiceService.findInvoiceByNumber(invoiceNumber);
                                    if (verifyInvoice != null) {
                                        System.out.println("INVOICE PANEL: Verification - Status: " + verifyInvoice.getStatus() + 
                                                         ", Amount: " + verifyInvoice.getTotalAmount() + 
                                                         ", Paid: " + verifyInvoice.getPaidAmount());
                                    }
                                } catch (Exception verifyEx) {
                                    System.err.println("INVOICE PANEL: Could not verify save: " + verifyEx.getMessage());
                                }
                            } else {
                                throw new Exception("Save operation returned null for invoice " + invoiceNumber);
                            }
                            
                        } catch (Exception e) {
                            errorCount++;
                            String error = "Error saving invoice " + invoiceNumber + ": " + e.getMessage();
                            errorMessages.append(error).append("\n");
                            System.err.println("INVOICE PANEL: " + error);
                            publish("✗ Failed: " + invoiceNumber + " - " + e.getMessage());
                        }
                        
                        progress++;
                        setProgress(progress);
                        
                        // Small delay to show progress and prevent UI freezing
                        Thread.sleep(200);
                    }
                    
                    if (!cancelled) {
                        publish("Finalizing save operation...");
                        Thread.sleep(500);
                    }
                    
                    System.out.println("INVOICE PANEL: Save worker completed. Saved: " + savedCount + ", Errors: " + errorCount);
                    return !cancelled;
                    
                } catch (Exception e) {
                    System.err.println("INVOICE PANEL: Fatal error in save worker: " + e.getMessage());
                    e.printStackTrace();
                    publish("Fatal error: " + e.getMessage());
                    throw e;
                }
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                if (!chunks.isEmpty()) {
                    String latestStatus = chunks.get(chunks.size() - 1);
                    SwingUtilities.invokeLater(() -> statusLabel.setText(latestStatus));
                }
            }
            
            @Override
            protected void done() {
                System.out.println("INVOICE PANEL: Save worker done() method called");
                
                try {
                    // Ensure dialog is closed
                    SwingUtilities.invokeLater(() -> {
                        if (progressDialog.isDisplayable()) {
                            progressDialog.dispose();
                        }
                    });
                    
                    Boolean success = get(); // This will throw exceptions if any occurred
                    
                    if (success && !cancelled) {
                        // Clear the change tracking only if we had some success
                        if (savedCount > 0) {
                            SwingUtilities.invokeLater(() -> {
                                // Clear change tracking - but keep the current in-memory data
                                modifiedInvoiceNumbers.clear();
                                hasUnsavedChanges = false;
                                updateSaveChangesButtonState();
                                
                                System.out.println("INVOICE PANEL: Changes cleared, keeping current in-memory data");
                                
                                // Instead of refreshing from database (which overwrites changes),
                                // just update the UI to reflect current state and update statistics
                                updateStatistics();
                                updatePaginationInfo();
                                
                                // Force table to repaint to ensure status colors are updated
                                if (invoicesTable != null) {
                                    invoicesTable.repaint();
                                    System.out.println("INVOICE PANEL: Table repaint triggered to update visual state");
                                }
                                
                                System.out.println("INVOICE PANEL: UI update completed without database reload");
                            });
                        }
                        
                        // Show results to user
                        SwingUtilities.invokeLater(() -> showSaveResults());
                    } else if (cancelled) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(InvoicePanel.this, 
                                "Save operation was cancelled.\nNo changes were saved to the database.", 
                                "Save Cancelled", 
                                JOptionPane.WARNING_MESSAGE);
                        });
                    }
                    
                } catch (Exception e) {
                    System.err.println("INVOICE PANEL: Error in save worker done(): " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(InvoicePanel.this, 
                            "Unexpected error during save operation:\n" + e.getMessage() + "\n\n" +
                            "Some changes may not have been saved to the database.\nPlease check your database connection and try again.", 
                            "Save Error", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            }
            
            private void showSaveResults() {
                String resultMessage;
                if (errorCount == 0 && savedCount > 0) {
                    resultMessage = "✓ Successfully saved " + savedCount + " invoice(s) to the database.\n\n" +
                                  "All changes have been permanently stored and will persist after logout.";
                    JOptionPane.showMessageDialog(InvoicePanel.this, resultMessage, 
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
                } else if (savedCount > 0) {
                    resultMessage = "Partially successful:\n" +
                                  "✓ Saved " + savedCount + " invoice(s) successfully\n" +
                                  "✗ " + errorCount + " invoice(s) failed to save\n\n" +
                                  "Errors:\n" + errorMessages.toString() + "\n" +
                                  "Successfully saved invoices will persist after logout.";
                    JOptionPane.showMessageDialog(InvoicePanel.this, resultMessage, 
                        "Save Completed with Errors", JOptionPane.WARNING_MESSAGE);
                } else {
                    resultMessage = "✗ Failed to save any invoices to the database.\n\n" +
                                  "Errors:\n" + errorMessages.toString() + "\n" +
                                  "No changes were saved. Please check the database connection and try again.";
                    JOptionPane.showMessageDialog(InvoicePanel.this, resultMessage, 
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        // Update progress bar as work progresses and handle cancel button
        final SwingWorker<Boolean, String> finalSaveWorker = saveWorker;
        
        saveWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(progress);
                    progressBar.setString(progress + " / " + modifiedInvoiceNumbers.size());
                });
            }
        });
        
        // Enable cancel button after a short delay
        Timer enableCancelTimer = new Timer(2000, e -> {
            cancelButton.setEnabled(true);
            cancelButton.addActionListener(cancelEvent -> {
                int cancelChoice = JOptionPane.showConfirmDialog(progressDialog,
                    "Are you sure you want to cancel the save operation?\n" +
                    "Any changes saved so far will be kept, but remaining changes will not be saved.",
                    "Cancel Save Operation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (cancelChoice == JOptionPane.YES_OPTION) {
                    finalSaveWorker.cancel(true);
                    progressDialog.dispose();
                }
            });
        });
        enableCancelTimer.setRepeats(false);
        enableCancelTimer.start();
        
        // Show progress dialog and start the work
        SwingUtilities.invokeLater(() -> {
            progressDialog.setVisible(true);
        });
        
        // Safety timeout to prevent stuck dialogs
        Timer safetyTimer = new Timer(60000, e -> { // 60 second timeout
            if (progressDialog.isDisplayable()) {
                System.err.println("INVOICE PANEL: Save operation timed out, forcing dialog close");
                saveWorker.cancel(true);
                progressDialog.dispose();
                JOptionPane.showMessageDialog(InvoicePanel.this,
                    "Save operation timed out after 60 seconds.\n" +
                    "Please check your database connection and try again.",
                    "Save Timeout",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        safetyTimer.setRepeats(false);
        safetyTimer.start();
        
        saveWorker.execute();
    }
    
    /**
     * Make the current session changes permanent in the database
     * This only processes invoices that have been modified or are new
     */
    private void makePermanentChanges() {
        if (!hasUnsavedChanges && !hasNewInvoicesNotInDatabase()) {
            JOptionPane.showMessageDialog(this, 
                "No changes to make permanent.\nAll invoices are already saved in the database.", 
                "Make Changes Permanent", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Count invoices that need to be saved
        List<SampleInvoice> invoicesToSave = new ArrayList<>();
        
        // Add modified invoices
        for (String invoiceNumber : modifiedInvoiceNumbers) {
            SampleInvoice invoice = findInvoiceByNumber(invoiceNumber);
            if (invoice != null) {
                invoicesToSave.add(invoice);
            }
        }
        
        // Add new invoices (ones not in database)
        for (SampleInvoice sampleInvoice : sampleInvoices) {
            if (!modifiedInvoiceNumbers.contains(sampleInvoice.getInvoiceNumber())) {
                // Check if this invoice exists in database
                try {
                    if (invoiceService != null) {
                        Invoice dbInvoice = invoiceService.findInvoiceByNumber(sampleInvoice.getInvoiceNumber());
                        if (dbInvoice == null) {
                            // Invoice doesn't exist in database, add it to save list
                            invoicesToSave.add(sampleInvoice);
                        }
                    }
                } catch (Exception e) {
                    // If findInvoiceByNumber throws exception, invoice doesn't exist
                    invoicesToSave.add(sampleInvoice);
                }
            }
        }
        
        if (invoicesToSave.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No changes to make permanent.\nAll invoices are already saved in the database.", 
                "Make Changes Permanent", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Check what actually needs to be made permanent
        // If all invoices are already from database and no changes were made, there's nothing to do
        if (!hasUnsavedChanges && modifiedInvoiceNumbers.isEmpty()) {
            boolean hasNewInvoices = false;
            for (SampleInvoice invoice : sampleInvoices) {
                try {
                    if (invoiceService != null) {
                        Invoice dbInvoice = invoiceService.findInvoiceByNumber(invoice.getInvoiceNumber());
                        if (dbInvoice == null) {
                            hasNewInvoices = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    // If we can't find it, assume it's new
                    hasNewInvoices = true;
                    break;
                }
            }
            
            if (!hasNewInvoices) {
                JOptionPane.showMessageDialog(this, 
                    "All invoices are already permanent in the database.\n" +
                    "No changes need to be made permanent.", 
                    "Already Permanent", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        // Show strong confirmation dialog with warning
        String message = "⚠️ PERMANENT DATABASE UPDATE ⚠️\n\n" +
                        "You are about to PERMANENTLY save " + invoicesToSave.size() + 
                        " invoice(s) to the database.\n\n" +
                        "Changes to be made permanent:\n";
        
        // List the invoices to be saved
        int count = 0;
        for (SampleInvoice invoice : invoicesToSave) {
            if (count < 8) { // Show max 8 invoice numbers
                String changeType = modifiedInvoiceNumbers.contains(invoice.getInvoiceNumber()) ? "Modified" : "New";
                message += "• " + invoice.getInvoiceNumber() + " (" + changeType + ")\n";
                count++;
            } else {
                message += "• ... and " + (invoicesToSave.size() - count) + " more\n";
                break;
            }
        }
        
        message += "\n⚠️ WARNING: This action will:\n" +
                  "• Permanently save changes to the database\n" +
                  "• Make changes persist after logout/login\n" +
                  "• Cannot be undone once completed\n\n" +
                  "Are you absolutely sure you want to proceed?";
        
        // Use custom option dialog for more emphasis
        String[] options = {"Yes, Make Permanent", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                message,
                "⚠️ CONFIRM PERMANENT DATABASE UPDATE",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]); // Default to Cancel
        
        if (choice != 0) { // If not "Yes, Make Permanent"
            return;
        }
        
        // Double confirmation for extra safety
        int doubleConfirm = JOptionPane.showConfirmDialog(this,
                "Last chance to cancel!\n\n" +
                "This will PERMANENTLY save " + invoicesToSave.size() + " invoice(s) to the database.\n" +
                "This action CANNOT be undone.\n\n" +
                "Proceed with permanent save?",
                "⚠️ FINAL CONFIRMATION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (doubleConfirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Check if invoice service is available
        if (invoiceService == null) {
            JOptionPane.showMessageDialog(this, 
                "Invoice service not available. Cannot make state permanent.", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create and show progress dialog
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog progressDialog = new JDialog(parentWindow instanceof Frame ? (Frame) parentWindow : null, "Making State Permanent", true);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(500, 200);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setResizable(false);
        
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel statusLabel = new JLabel("⚠️ Synchronizing database with current state...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(new Color(220, 53, 69));
        
        JProgressBar progressBar = new JProgressBar(0, sampleInvoices.size());
        progressBar.setStringPainted(true);
        progressBar.setString("0 / " + sampleInvoices.size() + " invoices synchronized");
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel warningLabel = new JLabel("⚠️ DO NOT close this window until complete!");
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        warningLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        warningLabel.setForeground(new Color(220, 53, 69));
        
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(warningLabel, BorderLayout.SOUTH);
        
        progressDialog.add(progressPanel);
        
        // Synchronize state in background thread
        SwingWorker<Boolean, String> permanentSaveWorker = new SwingWorker<Boolean, String>() {
            private int processedCount = 0;
            private int successCount = 0;
            private int updatedCount = 0;
            private int createdCount = 0;
            private int skippedCount = 0;
            private List<String> errors = new ArrayList<>();
            
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    System.out.println("INVOICE PANEL: Starting permanent database synchronization for " + sampleInvoices.size() + " invoices");
                    
                    // Debug: Show what invoices we're trying to synchronize
                    System.out.println("INVOICE PANEL: Invoices to synchronize:");
                    for (SampleInvoice inv : sampleInvoices) {
                        System.out.println("  - " + inv.getInvoiceNumber() + " (" + inv.getCustomerName() + ")");
                    }
                    
                    publish("Initializing database synchronization...");
                    Thread.sleep(500); // Brief pause for user to see the message
                    
                    for (SampleInvoice sampleInvoice : new ArrayList<>(sampleInvoices)) {
                        if (isCancelled()) {
                            break;
                        }
                        
                        try {
                            publish("Synchronizing: " + sampleInvoice.getInvoiceNumber());
                            
                            // Check if invoice exists in database
                            Invoice existingInvoice = null;
                            boolean invoiceExistsInDb = false;
                            try {
                                existingInvoice = invoiceService.findInvoiceByNumber(sampleInvoice.getInvoiceNumber());
                                invoiceExistsInDb = (existingInvoice != null);
                                if (invoiceExistsInDb) {
                                    System.out.println("INVOICE PANEL: Found existing invoice " + sampleInvoice.getInvoiceNumber() + " in database");
                                }
                            } catch (Exception e) {
                                System.out.println("INVOICE PANEL: Invoice " + sampleInvoice.getInvoiceNumber() + " not found in database, will create new");
                                existingInvoice = null;
                                invoiceExistsInDb = false;
                            }
                            
                            // Determine if we need to do anything
                            boolean needsUpdate = false;
                            
                            if (!invoiceExistsInDb) {
                                // Invoice doesn't exist, we need to create it
                                needsUpdate = true;
                                System.out.println("INVOICE PANEL: Invoice " + sampleInvoice.getInvoiceNumber() + " needs to be created");
                            } else {
                                // Invoice exists, check if it was modified in this session
                                if (modifiedInvoiceNumbers.contains(sampleInvoice.getInvoiceNumber())) {
                                    needsUpdate = true;
                                    System.out.println("INVOICE PANEL: Invoice " + sampleInvoice.getInvoiceNumber() + " was modified and needs update");
                                } else {
                                    // Check if the data actually differs
                                    if (invoiceDataDiffers(sampleInvoice, existingInvoice)) {
                                        needsUpdate = true;
                                        System.out.println("INVOICE PANEL: Invoice " + sampleInvoice.getInvoiceNumber() + " data differs from database");
                                    } else {
                                        System.out.println("INVOICE PANEL: Invoice " + sampleInvoice.getInvoiceNumber() + " is already up to date, skipping");
                                        skippedCount++;
                                    }
                                }
                            }
                            
                            if (needsUpdate) {
                                Invoice dbInvoice;
                                boolean isUpdate = false;
                                
                                if (existingInvoice != null) {
                                    // Update existing invoice - use the existing invoice object
                                    dbInvoice = existingInvoice;
                                    isUpdate = true;
                                    System.out.println("INVOICE PANEL: Updating existing invoice " + sampleInvoice.getInvoiceNumber() + " (ID: " + existingInvoice.getInvoiceId() + ")");
                                } else {
                                    // Create new invoice
                                    dbInvoice = new Invoice();
                                    dbInvoice.setInvoiceNumber(sampleInvoice.getInvoiceNumber());
                                    dbInvoice.setCreatedBy(currentUser != null ? currentUser.getEmail() : "System");
                                    System.out.println("INVOICE PANEL: Creating new invoice " + sampleInvoice.getInvoiceNumber());
                                }
                                
                                // Update/set all fields from sample invoice
                                try {
                                    dbInvoice.setInvoiceDate(LocalDate.parse(sampleInvoice.getDate()));
                                    dbInvoice.setDueDate(LocalDate.parse(sampleInvoice.getDueDate()));
                                    dbInvoice.setStatus(Invoice.InvoiceStatus.valueOf(sampleInvoice.getStatus()));
                                    dbInvoice.setTotalAmount(sampleInvoice.getAmount());
                                    dbInvoice.setPaidAmount(sampleInvoice.getPaid());
                                    dbInvoice.setBalanceAmount(sampleInvoice.getBalance());
                                    
                                    // Set modification info for updates
                                    if (isUpdate) {
                                        dbInvoice.setLastModifiedBy(currentUser != null ? currentUser.getEmail() : "System");
                                    }
                                    
                                    // Debug: Log the exact values being saved
                                    System.out.println("INVOICE PANEL: Saving invoice " + sampleInvoice.getInvoiceNumber() + " with values:");
                                    System.out.println("  - Total Amount: " + sampleInvoice.getAmount());
                                    System.out.println("  - Paid Amount: " + sampleInvoice.getPaid());
                                    System.out.println("  - Balance Amount: " + sampleInvoice.getBalance());
                                    System.out.println("  - Status: " + sampleInvoice.getStatus());
                                    System.out.println("  - Customer: " + sampleInvoice.getCustomerName());
                                    System.out.println("  - Date: " + sampleInvoice.getDate());
                                    System.out.println("  - Due Date: " + sampleInvoice.getDueDate());
                                    
                                    // Handle customer - find or create, but only if needed
                                    Customer currentCustomer = dbInvoice.getCustomer();
                                    String currentCustomerName = currentCustomer != null ? currentCustomer.getCompanyName() : null;
                                    
                                    // Only update customer if it's different from what we have
                                    if (currentCustomerName == null || !currentCustomerName.equals(sampleInvoice.getCustomerName())) {
                                        Customer customer = findOrCreateCustomer(sampleInvoice.getCustomerName());
                                        if (customer != null) {
                                            dbInvoice.setCustomer(customer);
                                            System.out.println("INVOICE PANEL: Updated customer to: " + sampleInvoice.getCustomerName());
                                        } else {
                                            System.err.println("INVOICE PANEL: Warning - could not find/create customer: " + sampleInvoice.getCustomerName());
                                        }
                                    } else {
                                        System.out.println("INVOICE PANEL: Customer unchanged: " + currentCustomerName);
                                    }
                                    
                                } catch (Exception e) {
                                    System.err.println("INVOICE PANEL: Error parsing invoice data for " + sampleInvoice.getInvoiceNumber() + ": " + e.getMessage());
                                    errors.add("Error parsing data for " + sampleInvoice.getInvoiceNumber() + ": " + e.getMessage());
                                    processedCount++;
                                    continue;
                                }
                                
                                // Save to database with better error handling
                                try {
                                    Invoice savedInvoice = null;
                                    
                                    if (isUpdate) {
                                        // Use the specialized method for updating basic fields
                                        savedInvoice = invoiceService.updateInvoiceBasicFields(dbInvoice);
                                        System.out.println("INVOICE PANEL: Used updateInvoiceBasicFields for: " + sampleInvoice.getInvoiceNumber());
                                    } else {
                                        // For new invoices, use regular save
                                        savedInvoice = invoiceService.saveInvoice(dbInvoice);
                                        System.out.println("INVOICE PANEL: Used saveInvoice for new: " + sampleInvoice.getInvoiceNumber());
                                    }
                                    
                                    if (savedInvoice != null) {
                                        successCount++;
                                        if (isUpdate) {
                                            updatedCount++;
                                            System.out.println("INVOICE PANEL: Successfully updated: " + sampleInvoice.getInvoiceNumber());
                                        } else {
                                            createdCount++;
                                            System.out.println("INVOICE PANEL: Successfully created: " + sampleInvoice.getInvoiceNumber());
                                        }
                                    } else {
                                        errors.add("Failed to save invoice " + sampleInvoice.getInvoiceNumber() + " to database - service returned null");
                                        System.err.println("INVOICE PANEL: Save operation returned null for: " + sampleInvoice.getInvoiceNumber());
                                    }
                                } catch (Exception saveEx) {
                                    String errorMsg = "Failed to save invoice " + sampleInvoice.getInvoiceNumber() + ": " + saveEx.getMessage();
                                    errors.add(errorMsg);
                                    System.err.println("INVOICE PANEL: " + errorMsg);
                                    saveEx.printStackTrace();
                                }
                            } else {
                                successCount++; // Count skipped as successful since they were already correct
                            }
                            
                            processedCount++;
                            
                            // Update progress
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setValue(processedCount);
                                progressBar.setString(processedCount + " / " + sampleInvoices.size() + " invoices synchronized");
                            });
                            
                            // Brief pause between updates to show progress
                            Thread.sleep(100);
                            
                        } catch (Exception e) {
                            System.err.println("INVOICE PANEL: Error synchronizing " + sampleInvoice.getInvoiceNumber() + ": " + e.getMessage());
                            errors.add("Error synchronizing " + sampleInvoice.getInvoiceNumber() + ": " + e.getMessage());
                            processedCount++;
                        }
                    }
                    
                    return true;
                    
                } catch (Exception e) {
                    System.err.println("INVOICE PANEL: Critical error in permanent save operation: " + e.getMessage());
                    e.printStackTrace();
                    errors.add("Critical error: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    String lastMessage = chunks.get(chunks.size() - 1);
                    statusLabel.setText(lastMessage);
                }
            }
            
            @Override
            protected void done() {
                try {
                    progressDialog.dispose();
                    
                    boolean success = get();
                    
                    // Show results
                    if (success && errors.isEmpty()) {
                        // Complete success - clear change tracking since everything is now permanent
                        modifiedInvoiceNumbers.clear();
                        hasUnsavedChanges = false;
                        updateSaveChangesButtonState();
                        
                        // CRITICAL FIX: Refresh data from database to show the actual saved values
                        // This ensures the UI reflects what was actually saved to the database
                        System.out.println("INVOICE PANEL: Refreshing data from database after successful save...");
                        try {
                            loadInvoices();
                            updateTableData();
                            updateStatistics();
                            updatePaginationInfo();
                            System.out.println("INVOICE PANEL: Successfully refreshed data from database");
                        } catch (Exception refreshEx) {
                            System.err.println("INVOICE PANEL: Warning - failed to refresh data after save: " + refreshEx.getMessage());
                        }
                        
                        String resultMessage = "✅ SUCCESS!\n\n" +
                            "Database has been synchronized with your current session state!\n\n" +
                            "Results:\n";
                        
                        if (createdCount > 0) {
                            resultMessage += "• Created: " + createdCount + " new invoices\n";
                        }
                        if (updatedCount > 0) {
                            resultMessage += "• Updated: " + updatedCount + " existing invoices\n";
                        }
                        if (skippedCount > 0) {
                            resultMessage += "• Skipped: " + skippedCount + " already up-to-date invoices\n";
                        }
                        
                        resultMessage += "• Total: " + successCount + " invoices synchronized\n\n" +
                                       "All changes are now permanent and will persist after logout/login.\n\n" +
                                       "The display has been refreshed to show the actual database values.";
                        
                        JOptionPane.showMessageDialog(InvoicePanel.this,
                            resultMessage,
                            "✅ State Made Permanent",
                            JOptionPane.INFORMATION_MESSAGE);
                            
                        System.out.println("INVOICE PANEL: Permanent save operation completed successfully - " + 
                                         createdCount + " created, " + updatedCount + " updated, " + skippedCount + " skipped");
                        
                    } else if (successCount > 0) {
                        // Partial success
                        modifiedInvoiceNumbers.clear(); // Clear tracking for successfully saved invoices
                        hasUnsavedChanges = false;
                        updateSaveChangesButtonState();
                        
                        // CRITICAL FIX: Refresh data from database to show the actual saved values
                        // This ensures the UI reflects what was actually saved to the database
                        System.out.println("INVOICE PANEL: Refreshing data from database after partial save...");
                        try {
                            loadInvoices();
                            updateTableData();
                            updateStatistics();
                            updatePaginationInfo();
                            System.out.println("INVOICE PANEL: Successfully refreshed data from database");
                        } catch (Exception refreshEx) {
                            System.err.println("INVOICE PANEL: Warning - failed to refresh data after save: " + refreshEx.getMessage());
                        }
                        
                        String errorDetails = errors.size() <= 3 ? String.join("\n", errors) : 
                                            String.join("\n", errors.subList(0, 3)) + "\n... and " + (errors.size() - 3) + " more errors";
                        
                        JOptionPane.showMessageDialog(InvoicePanel.this,
                            "⚠️ PARTIAL SUCCESS\n\n" +
                            "Successfully synchronized: " + successCount + " invoices\n" +
                            "Failed to synchronize: " + errors.size() + " invoices\n\n" +
                            "Results:\n" +
                            "• Created: " + createdCount + " new invoices\n" +
                            "• Updated: " + updatedCount + " existing invoices\n" +
                            "• Skipped: " + skippedCount + " already up-to-date invoices\n\n" +
                            "The display has been refreshed to show actual database values.\n\n" +
                            "Errors:\n" + errorDetails,
                            "⚠️ Partial Success",
                            JOptionPane.WARNING_MESSAGE);
                            
                    } else {
                        // Complete failure
                        String errorDetails = errors.size() <= 5 ? String.join("\n", errors) : 
                                            String.join("\n", errors.subList(0, 5)) + "\n... and " + (errors.size() - 5) + " more errors";
                        
                        JOptionPane.showMessageDialog(InvoicePanel.this,
                            "❌ OPERATION FAILED\n\n" +
                            "No invoices were synchronized to the database.\n" +
                            "Your current session state remains unchanged.\n\n" +
                            "Errors:\n" + errorDetails,
                            "❌ Operation Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    progressDialog.dispose();
                    System.err.println("INVOICE PANEL: Error in permanent save completion: " + e.getMessage());
                    JOptionPane.showMessageDialog(InvoicePanel.this,
                        "Error completing permanent save operation: " + e.getMessage(),
                        "Operation Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        // Show progress dialog and start the operation
        permanentSaveWorker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * Check if the data in SampleInvoice differs from the database Invoice
     */
    private boolean invoiceDataDiffers(SampleInvoice sampleInvoice, Invoice dbInvoice) {
        try {
            // Compare basic fields
            if (!sampleInvoice.getInvoiceNumber().equals(dbInvoice.getInvoiceNumber())) {
                return true;
            }
            
            if (!sampleInvoice.getDate().equals(dbInvoice.getInvoiceDate().toString())) {
                return true;
            }
            
            if (!sampleInvoice.getDueDate().equals(dbInvoice.getDueDate().toString())) {
                return true;
            }
            
            if (!sampleInvoice.getStatus().equals(dbInvoice.getStatus().toString())) {
                return true;
            }
            
            // Compare amounts
            if (sampleInvoice.getAmount().compareTo(dbInvoice.getTotalAmount()) != 0) {
                return true;
            }
            
            if (sampleInvoice.getPaid().compareTo(dbInvoice.getPaidAmount()) != 0) {
                return true;
            }
            
            if (sampleInvoice.getBalance().compareTo(dbInvoice.getBalanceAmount()) != 0) {
                return true;
            }
            
            // Compare customer name
            String dbCustomerName = dbInvoice.getCustomer() != null ? dbInvoice.getCustomer().getCompanyName() : "Unknown";
            if (!sampleInvoice.getCustomerName().equals(dbCustomerName)) {
                return true;
            }
            
            return false; // No differences found
            
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error comparing invoice data: " + e.getMessage());
            // If we can't compare properly, assume they differ to be safe
            return true;
        }
    }
    
    /**
     * Check if there are new invoices in the session that don't exist in the database
     */
    private boolean hasNewInvoicesNotInDatabase() {
        if (invoiceService == null) {
            return false;
        }
        
        try {
            for (SampleInvoice sampleInvoice : sampleInvoices) {
                try {
                    Invoice dbInvoice = invoiceService.findInvoiceByNumber(sampleInvoice.getInvoiceNumber());
                    // If we can't find it, it's new
                    if (dbInvoice == null) {
                        return true;
                    }
                } catch (Exception e) {
                    // If findInvoiceByNumber throws an exception, the invoice doesn't exist
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error checking for new invoices: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convert SampleInvoice back to database Invoice for saving
     */
    private Invoice convertSampleToDbInvoice(SampleInvoice sampleInvoice) {
        try {
            System.out.println("INVOICE PANEL: Converting sample invoice " + sampleInvoice.getInvoiceNumber() + " to database invoice");
            
            // Try to find existing invoice by invoice number
            Invoice existingInvoice = null;
            try {
                existingInvoice = invoiceService.findInvoiceByNumber(sampleInvoice.getInvoiceNumber());
                if (existingInvoice != null) {
                    System.out.println("INVOICE PANEL: Found existing invoice in database with ID: " + existingInvoice.getInvoiceId());
                    return updateExistingInvoice(existingInvoice, sampleInvoice);
                } else {
                    System.out.println("INVOICE PANEL: Invoice " + sampleInvoice.getInvoiceNumber() + " not found in database");
                }
            } catch (Exception e) {
                System.err.println("INVOICE PANEL: Error finding invoice by number: " + e.getMessage());
            }
            
            // Invoice doesn't exist in database - this should not happen for the current use case
            // In the context of saving session changes, we should only be updating existing invoices
            throw new RuntimeException("Invoice " + sampleInvoice.getInvoiceNumber() + " does not exist in database and cannot be updated. Only existing invoices can be modified in this operation.");
            
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error converting SampleInvoice to database Invoice: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to convert invoice " + sampleInvoice.getInvoiceNumber() + " for database update: " + e.getMessage());
        }
    }
    
    /**
     * Update an existing database invoice with values from sample invoice
     */
    private Invoice updateExistingInvoice(Invoice existingInvoice, SampleInvoice sampleInvoice) {
        try {
            System.out.println("INVOICE PANEL: Updating existing invoice " + existingInvoice.getInvoiceNumber() + " (ID: " + existingInvoice.getInvoiceId() + ")");
            
            // Update status
            try {
                Invoice.InvoiceStatus newStatus = Invoice.InvoiceStatus.valueOf(sampleInvoice.getStatus());
                existingInvoice.setStatus(newStatus);
                System.out.println("INVOICE PANEL: Updated status to: " + newStatus);
            } catch (IllegalArgumentException e) {
                System.err.println("INVOICE PANEL: Invalid status value: " + sampleInvoice.getStatus() + ", keeping existing status");
            }
            
            // Update amounts
            existingInvoice.setTotalAmount(sampleInvoice.getAmount());
            existingInvoice.setPaidAmount(sampleInvoice.getPaid());
            existingInvoice.setBalanceAmount(sampleInvoice.getBalance());
            
            System.out.println("INVOICE PANEL: Updated amounts - Total: " + sampleInvoice.getAmount() + 
                             ", Paid: " + sampleInvoice.getPaid() + ", Balance: " + sampleInvoice.getBalance());
            
            // Update dates if they changed
            try {
                LocalDate newInvoiceDate = LocalDate.parse(sampleInvoice.getDate());
                LocalDate newDueDate = LocalDate.parse(sampleInvoice.getDueDate());
                existingInvoice.setInvoiceDate(newInvoiceDate);
                existingInvoice.setDueDate(newDueDate);
                System.out.println("INVOICE PANEL: Updated dates - Invoice: " + newInvoiceDate + ", Due: " + newDueDate);
            } catch (Exception e) {
                System.err.println("INVOICE PANEL: Error parsing dates for invoice " + sampleInvoice.getInvoiceNumber() + ": " + e.getMessage());
                // Continue with other updates even if date parsing fails
            }
            
            // Update customer if name changed
            if (existingInvoice.getCustomer() == null || 
                !existingInvoice.getCustomer().getCompanyName().equals(sampleInvoice.getCustomerName())) {
                System.out.println("INVOICE PANEL: Customer name changed to: " + sampleInvoice.getCustomerName());
                Customer customer = findOrCreateCustomer(sampleInvoice.getCustomerName());
                if (customer != null) {
                    existingInvoice.setCustomer(customer);
                    System.out.println("INVOICE PANEL: Updated customer to: " + customer.getCompanyName());
                } else {
                    System.err.println("INVOICE PANEL: Failed to find/create customer: " + sampleInvoice.getCustomerName());
                }
            } else {
                System.out.println("INVOICE PANEL: Customer unchanged: " + sampleInvoice.getCustomerName());
            }
            
            // Set the last modified by field
            existingInvoice.setLastModifiedBy(currentUser != null ? currentUser.getEmail() : "system");
            
            return existingInvoice;
            
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error updating existing invoice: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update existing invoice " + sampleInvoice.getInvoiceNumber() + ": " + e.getMessage());
        }
    }
    
    public void addNewInvoice(SampleInvoice invoice) {
        addConvertedInvoice(invoice);
    }
    
    /**
     * Refresh data from database after saving to ensure UI reflects actual database state
     * NOTE: This method is kept for potential future use but currently not called to prevent
     * data rollback issues after saving changes.
     */
    private void refreshFromDatabase() {
        try {
            System.out.println("INVOICE PANEL: Refreshing data from database...");
            
            // Reload all invoices from database
            loadInvoices();
            
            // Update all UI components
            updateTableData();
            updateStatistics();
            updatePaginationInfo();
            
            System.out.println("INVOICE PANEL: Database refresh completed successfully");
            
        } catch (Exception e) {
            System.err.println("INVOICE PANEL: Error refreshing from database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Explicitly refresh from database - only call this when user explicitly requests it
     * or when loading initial data
     */
    public void forceRefreshFromDatabase() {
        // Warn user if there are unsaved changes
        if (hasUnsavedChanges && !modifiedInvoiceNumbers.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                "You have " + modifiedInvoiceNumbers.size() + " unsaved changes that will be lost.\n" +
                "Are you sure you want to reload from database?",
                "Confirm Refresh",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        refreshFromDatabase();
    }
    
    /**
     * Force synchronization with database to ensure UI reflects actual database state
     */
}
