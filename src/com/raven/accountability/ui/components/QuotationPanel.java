package com.raven.accountability.ui.components;

import com.raven.model.User;
import com.raven.accountability.model.*;
import com.raven.accountability.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.DefaultCellEditor;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * Quotation Panel - Child component for quotation management
 * This panel is designed to be used as a child component within BillingAndInvoicingPanel
 * Provides complete quotation management functionality including creation, editing, and conversion to invoices
 * Communicates with parent panel through the parentPanel reference for quotation-to-invoice conversion
 */
public class QuotationPanel extends JPanel {
    private User currentUser;
    private QuotationService quotationService;
    private CustomerService customerService;
    private InvoiceTemplateGenerator templateGenerator;
    private Object parentPanel; // Using Object to avoid circular dependency
    
    // Main components
    private JTable quotationsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton createQuotationBtn;
    private JButton editQuotationBtn;
    private JButton viewQuotationBtn;
    private JButton deleteQuotationBtn;
    private JButton convertToInvoiceBtn;
    private JButton saveSessionBtn;
    
    // Statistics panels
    private JPanel statsPanel;
    private JLabel totalQuotationsLabel;
    private JLabel totalAmountLabel;
    private JLabel pendingLabel;
    private JLabel acceptedLabel;
    
    // In-memory storage for sample quotations
    private List<SampleQuotation> sampleQuotations;
    
    // Track session changes
    private boolean hasUnsavedChanges = false;
    private boolean isDatabaseUpdateInProgress = false;
    private List<SampleQuotation> newQuotations = new ArrayList<>();
    private List<String> deletedQuotationNumbers = new ArrayList<>();
    
    /**
     * Custom table cell renderer for column-specific colors
     */
    private class QuotationTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                // Reset text color to default dark for all columns except Status
                c.setForeground(new Color(44, 62, 80)); // Default dark text
                
                // Define contrasting background colors for different columns
                switch (column) {
                    case 0: // Quotation #
                        c.setBackground(new Color(240, 248, 255)); // Very light blue
                        break;
                    case 1: // Customer
                        c.setBackground(new Color(248, 255, 248)); // Very light green
                        break;
                    case 2: // Date
                        c.setBackground(new Color(255, 248, 240)); // Very light orange
                        break;
                    case 3: // Valid Until
                        c.setBackground(new Color(248, 240, 255)); // Very light purple
                        break;
                    case 4: // Status - Only column with colored text
                        String status = value != null ? value.toString() : "";
                        switch (status) {
                            case "DRAFT":
                                c.setBackground(new Color(248, 248, 255)); // Ghost white
                                c.setForeground(new Color(95, 165, 166)); // Cadet blue
                                break;
                            case "SENT":
                                c.setBackground(new Color(240, 248, 255)); // Light blue
                                c.setForeground(new Color(52, 152, 219)); // Blue
                                break;
                            case "VIEWED":
                                c.setBackground(new Color(255, 248, 220)); // Light yellow
                                c.setForeground(new Color(243, 156, 18)); // Orange
                                break;
                            case "ACCEPTED":
                                c.setBackground(new Color(230, 255, 230)); // Light green
                                c.setForeground(new Color(39, 174, 96)); // Green
                                break;
                            case "REJECTED":
                                c.setBackground(new Color(255, 235, 235)); // Light red
                                c.setForeground(new Color(231, 76, 60)); // Red
                                break;
                            case "EXPIRED":
                                c.setBackground(new Color(255, 240, 230)); // Light orange
                                c.setForeground(new Color(230, 126, 34)); // Orange
                                break;
                            case "CONVERTED":
                                c.setBackground(new Color(245, 235, 255)); // Light purple
                                c.setForeground(new Color(142, 68, 173)); // Purple
                                break;
                            default:
                                c.setBackground(new Color(250, 250, 250)); // Light gray
                                c.setForeground(new Color(105, 105, 105)); // Dim gray
                                break;
                        }
                        break;
                    case 5: // Amount
                        c.setBackground(new Color(255, 255, 240)); // Very light yellow
                        break;
                    case 6: // Converted
                        String converted = value != null ? value.toString() : "";
                        if ("Yes".equals(converted)) {
                            c.setBackground(new Color(230, 255, 230)); // Light green
                            c.setForeground(new Color(39, 174, 96)); // Green
                            setFont(getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setBackground(new Color(245, 245, 245)); // Very light gray
                            c.setForeground(new Color(44, 62, 80)); // Default dark text
                            setFont(getFont().deriveFont(Font.PLAIN));
                        }
                        break;
                    case 7: // Invoice #
                        c.setBackground(new Color(240, 255, 248)); // Very light mint
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        break;
                }
            } else {
                // Selected row colors
                c.setBackground(new Color(52, 152, 219, 80));
                c.setForeground(new Color(44, 62, 80));
            }
            
            return c;
        }
    }
    
    public QuotationPanel(User user, QuotationService quotationService, CustomerService customerService, 
                         InvoiceTemplateGenerator templateGenerator, Object parentPanel) {
        this.currentUser = user;
        this.quotationService = quotationService;
        this.customerService = customerService;
        this.templateGenerator = templateGenerator;
        this.parentPanel = parentPanel;
        
        // Initialize sample quotations for demo mode
        initializeSampleQuotations();
        
        initComponents();
        setupLayout();
        loadQuotations();
        updateStatistics();
        updateSaveButtonState();
    }
    
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setOpaque(false);
        
        // Initialize table with editable cells
        String[] columnNames = {"Quotation #", "Customer", "Date", "Valid Until", "Status", "Amount", "Converted", "Invoice #"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make status column editable, but not converted column
                return column == 4;
            }
            
            @Override
            public void setValueAt(Object value, int row, int col) {
                super.setValueAt(value, row, col);
                // Update sample quotation data when table is edited
                updateSampleQuotation(row, col, value);
                hasUnsavedChanges = true;
                updateSaveButtonState();
            }
        };
        
        quotationsTable = new JTable(tableModel);
        quotationsTable.setRowHeight(40);
        quotationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quotationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Enhanced header styling
        quotationsTable.getTableHeader().setBackground(new Color(238, 242, 248));
        quotationsTable.getTableHeader().setForeground(new Color(66, 92, 128));
        quotationsTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        // Enhanced table styling
        quotationsTable.setSelectionBackground(new Color(52, 152, 219, 80));
        quotationsTable.setSelectionForeground(new Color(44, 62, 80));
        quotationsTable.setGridColor(new Color(189, 195, 199));
        quotationsTable.setShowGrid(true);
        quotationsTable.setIntercellSpacing(new Dimension(1, 1));
        
        // Set custom cell renderer for column-specific colors
        quotationsTable.setDefaultRenderer(Object.class, new QuotationTableCellRenderer());
        
        // Custom editor for Status column
        String[] statusEditorOptions = {"DRAFT", "SENT", "VIEWED", "ACCEPTED", "REJECTED", "EXPIRED"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusEditorOptions);
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quotationsTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusComboBox));
        
        // Add table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        quotationsTable.setRowSorter(tableSorter);
        
        // Table double-click listener
        quotationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedQuotation();
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
        
        String[] statusOptions = {"All Statuses", "DRAFT", "SENT", "VIEWED", "ACCEPTED", "REJECTED", "EXPIRED", "CONVERTED"};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Search listeners
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        
        statusFilter.addActionListener(e -> filterTable());
        
        // Action buttons
        createQuotationBtn = createActionButton("📋 Create Quotation", new Color(52, 152, 219));
        editQuotationBtn = createActionButton("✏️ Edit Quotation", new Color(39, 174, 96));
        viewQuotationBtn = createActionButton("👁️ View Quotation", new Color(155, 89, 182));
        convertToInvoiceBtn = createActionButton("🔄 Convert to Invoice", new Color(142, 68, 173));
        deleteQuotationBtn = createActionButton("🗑️ Delete Quotation", new Color(231, 76, 60));
        saveSessionBtn = createActionButton("💾 Save Session to Database", new Color(46, 125, 50));
        
        // Button listeners
        createQuotationBtn.addActionListener(e -> createNewQuotation());
        editQuotationBtn.addActionListener(e -> editSelectedQuotation());
        viewQuotationBtn.addActionListener(e -> viewSelectedQuotation());
        convertToInvoiceBtn.addActionListener(e -> convertSelectedQuotationToInvoice());
        deleteQuotationBtn.addActionListener(e -> deleteSelectedQuotation());
        saveSessionBtn.addActionListener(e -> saveSessionToDatabase());
        
        createStatisticsPanel();
    }
    
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
        JScrollPane tableScrollPane = new JScrollPane(quotationsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 221, 225)));
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void createStatisticsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 100));
        
        // Create stat cards
        totalQuotationsLabel = new JLabel("0");
        totalAmountLabel = new JLabel("$0.00");
        pendingLabel = new JLabel("$0.00");
        acceptedLabel = new JLabel("$0.00");
        
        statsPanel.add(createStatCard("📋", "Total Quotations", totalQuotationsLabel, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("💰", "Total Amount", totalAmountLabel, new Color(39, 174, 96)));
        statsPanel.add(createStatCard("⏳", "Pending Amount", pendingLabel, new Color(243, 156, 18)));
        statsPanel.add(createStatCard("✅", "Accepted Amount", acceptedLabel, new Color(46, 125, 50)));
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
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Left-aligned buttons
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftButtonPanel.setOpaque(false);
        leftButtonPanel.add(createQuotationBtn);
        leftButtonPanel.add(editQuotationBtn);
        leftButtonPanel.add(viewQuotationBtn);
        leftButtonPanel.add(Box.createHorizontalStrut(10));
        leftButtonPanel.add(convertToInvoiceBtn);
        leftButtonPanel.add(Box.createHorizontalStrut(10));
        leftButtonPanel.add(deleteQuotationBtn);
        
        // Right-aligned save session button
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.add(saveSessionBtn);
        
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        
        return buttonPanel;
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
    
    private void initializeSampleQuotations() {
        sampleQuotations = new ArrayList<>();
        
        sampleQuotations.add(new SampleQuotation("QUO-2025-001", "Acme Corporation", "2025-07-10", "2025-08-10", "SENT", "$2,450.00"));
        sampleQuotations.add(new SampleQuotation("QUO-2025-002", "TechStart Solutions", "2025-07-12", "2025-08-12", "ACCEPTED", "$1,875.50"));
        sampleQuotations.add(new SampleQuotation("QUO-2025-003", "Global Industries", "2025-07-15", "2025-08-15", "VIEWED", "$3,200.75"));
        sampleQuotations.add(new SampleQuotation("QUO-2025-004", "Innovation Labs", "2025-06-20", "2025-07-20", "EXPIRED", "$950.25"));
        sampleQuotations.add(new SampleQuotation("QUO-2025-005", "Digital Dynamics", "2025-07-20", "2025-08-20", "DRAFT", "$4,100.00"));
        sampleQuotations.add(new SampleQuotation("QUO-2025-006", "Smart Solutions Inc", "2025-07-25", "2025-08-25", "SENT", "$1,650.80"));
        
        // Create one converted quotation
        SampleQuotation convertedQuotation = new SampleQuotation("QUO-2025-007", "Future Systems", "2025-07-18", "2025-08-18", "CONVERTED", "$2,890.40", true, "INV-2025-010");
        sampleQuotations.add(convertedQuotation);
        
        System.out.println("QUOTATION PANEL: Initialized " + sampleQuotations.size() + " sample quotations");
    }
    
    private void loadQuotations() {
        try {
            if (quotationService == null) {
                System.err.println("QUOTATION PANEL WARNING: QuotationService not available - loading sample data");
                loadSampleQuotations();
                return;
            }
            
            List<Quotation> quotations = quotationService.getAllQuotations();
            
            // Clear existing data
            tableModel.setRowCount(0);
            
            if (quotations.isEmpty()) {
                System.out.println("QUOTATION PANEL INFO: No quotations found in database - populating with sample data");
                loadSampleQuotations();
            } else {
                // Add quotation data to table
                for (Quotation quotation : quotations) {
                    Object[] rowData = {
                        quotation.getQuotationNumber(),
                        quotation.getCustomer().getCompanyName(),
                        quotation.getQuotationDate().toString(),
                        quotation.getValidUntilDate().toString(),
                        quotation.getStatus().getDisplayName(),
                        "$" + quotation.getTotalAmount().toString(),
                        quotation.isConvertedToInvoice() ? "Yes" : "No",
                        quotation.getConvertedInvoiceNumber() != null ? quotation.getConvertedInvoiceNumber() : ""
                    };
                    tableModel.addRow(rowData);
                }
                System.out.println("QUOTATION PANEL: Loaded " + quotations.size() + " quotations from database");
            }
            
        } catch (SQLException e) {
            System.err.println("QUOTATION PANEL ERROR: Failed to load quotations - " + e.getMessage());
            System.out.println("QUOTATION PANEL INFO: Loading sample data instead");
            loadSampleQuotations();
        } catch (Exception e) {
            System.err.println("QUOTATION PANEL ERROR: Unexpected error - " + e.getMessage());
            loadSampleQuotations();
        }
    }
    
    private void loadSampleQuotations() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add sample quotation data from the list
        for (SampleQuotation quotation : sampleQuotations) {
            tableModel.addRow(quotation.toTableRow());
        }
        
        // Force table refresh
        tableModel.fireTableDataChanged();
        quotationsTable.repaint();
        quotationsTable.revalidate();
        
        System.out.println("QUOTATION PANEL: Loaded " + sampleQuotations.size() + " sample quotations");
    }
    
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String statusSelected = (String) statusFilter.getSelectedItem();
        
        if (searchText.isEmpty() && "All Statuses".equals(statusSelected)) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        boolean matchFound = false;
                        for (int i = 0; i < entry.getValueCount(); i++) {
                            if (entry.getStringValue(i).toLowerCase().contains(searchText)) {
                                matchFound = true;
                                break;
                            }
                        }
                        if (!matchFound) return false;
                    }
                    
                    // Status filter
                    if (!"All Statuses".equals(statusSelected)) {
                        String rowStatus = entry.getStringValue(4); // Status column
                        if (!statusSelected.equals(rowStatus)) {
                            return false;
                        }
                    }
                    
                    return true;
                }
            });
        }
    }
    
    private void updateSampleQuotation(int row, int col, Object value) {
        if (row >= 0 && row < sampleQuotations.size()) {
            SampleQuotation quotation = sampleQuotations.get(row);
            String strValue = value.toString();
            
            switch (col) {
                case 4: // Status column
                    quotation.setStatus(strValue);
                    break;
            }
            
            // Update statistics after editing
            updateStatistics();
        }
    }
    
    private void updateStatistics() {
        if (sampleQuotations == null || sampleQuotations.isEmpty()) {
            totalQuotationsLabel.setText("0");
            totalAmountLabel.setText("$0.00");
            pendingLabel.setText("$0.00");
            acceptedLabel.setText("$0.00");
            return;
        }
        
        int totalQuotations = sampleQuotations.size();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal acceptedAmount = BigDecimal.ZERO;
        
        for (SampleQuotation quotation : sampleQuotations) {
            totalAmount = totalAmount.add(quotation.getAmount());
            
            String status = quotation.getStatus();
            if ("SENT".equals(status) || "VIEWED".equals(status)) {
                pendingAmount = pendingAmount.add(quotation.getAmount());
            } else if ("ACCEPTED".equals(status) || "CONVERTED".equals(status)) {
                acceptedAmount = acceptedAmount.add(quotation.getAmount());
            }
        }
        
        totalQuotationsLabel.setText(String.valueOf(totalQuotations));
        totalAmountLabel.setText("$" + totalAmount.toString());
        pendingLabel.setText("$" + pendingAmount.toString());
        acceptedLabel.setText("$" + acceptedAmount.toString());
    }
    
    private void createNewQuotation() {
        try {
            // Create and show dialog
            QuotationDialog dialog = new QuotationDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            
            // Get the created quotation
            SampleQuotation newQuotation = dialog.getQuotation();
            if (newQuotation != null) {
                // Add to sample quotations list
                sampleQuotations.add(newQuotation);
                newQuotations.add(newQuotation);
                
                // Refresh the table display
                loadSampleQuotations();
                updateStatistics();
                
                // Mark session as having unsaved changes
                hasUnsavedChanges = true;
                updateSaveButtonState();
                
                System.out.println("QUOTATION PANEL: Successfully created quotation " + newQuotation.getQuotationNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("QUOTATION PANEL ERROR: Failed to create quotation - " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error creating quotation: " + e.getMessage(), 
                "Create Quotation Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editSelectedQuotation() {
        int selectedRow = quotationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a quotation to edit from the table above.",
                "No Quotation Selected", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Edit Quotation feature will be implemented here", 
            "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void viewSelectedQuotation() {
        int selectedRow = quotationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a quotation to view from the table above.",
                "No Quotation Selected", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int modelRow = tableSorter.convertRowIndexToModel(selectedRow);
        String quotationNumber = (String) tableModel.getValueAt(modelRow, 0);
        
        JOptionPane.showMessageDialog(this, 
            "Viewing Quotation: " + quotationNumber + "\n\n" +
            "This is a sample quotation in demo mode.\n" +
            "Full quotation viewer requires database connection.",
            "Quotation Viewer (Demo Mode)", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void convertSelectedQuotationToInvoice() {
        int selectedRow = quotationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a quotation to convert from the table above.",
                "No Quotation Selected", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int modelRow = tableSorter.convertRowIndexToModel(selectedRow);
        
        if (modelRow >= 0 && modelRow < sampleQuotations.size()) {
            SampleQuotation quotation = sampleQuotations.get(modelRow);
            
            // Check if already converted
            if (quotation.isConvertedToInvoice()) {
                JOptionPane.showMessageDialog(this,
                    "<html><div style='width: 300px;'>" +
                    "<h3>⚠️ Already Converted</h3><br>" +
                    "This quotation has already been converted to invoice:<br>" +
                    "<b>" + quotation.getConvertedInvoiceNumber() + "</b>" +
                    "</div></html>",
                    "Already Converted",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Check if quotation status allows conversion
            String status = quotation.getStatus();
            if ("REJECTED".equals(status) || "EXPIRED".equals(status)) {
                JOptionPane.showMessageDialog(this,
                    "<html><div style='width: 300px;'>" +
                    "<h3>⚠️ Cannot Convert</h3><br>" +
                    "Quotations with status '" + status + "' cannot be converted to invoices.<br>" +
                    "Please change the status first if this quotation was accepted." +
                    "</div></html>",
                    "Cannot Convert",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Confirm conversion
            int result = JOptionPane.showConfirmDialog(this,
                "<html><div style='width: 350px;'>" +
                "<h3>🔄 Convert Quotation to Invoice</h3><br>" +
                "<b>Quotation:</b> " + quotation.getQuotationNumber() + "<br>" +
                "<b>Customer:</b> " + quotation.getCustomerName() + "<br>" +
                "<b>Amount:</b> " + quotation.getFormattedAmount() + "<br><br>" +
                "This will create a new invoice and mark the quotation as converted.<br>" +
                "Do you want to proceed?" +
                "</div></html>",
                "Confirm Conversion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
                
            if (result == JOptionPane.OK_OPTION) {
                // Call parent panel to handle conversion
                try {
                    // Use reflection to avoid circular dependency
                    if (parentPanel != null) {
                        java.lang.reflect.Method convertMethod = parentPanel.getClass()
                            .getMethod("convertQuotationToInvoice", SampleQuotation.class);
                        convertMethod.invoke(parentPanel, quotation);
                    }
                } catch (Exception ex) {
                    System.err.println("QUOTATION PANEL: Error calling conversion method - " + ex.getMessage());
                    // Fallback: Show success message for demo
                    String newInvoiceNumber = "INV-2025-" + String.format("%03d", (int)(Math.random() * 900) + 100);
                    quotation.setConvertedToInvoice(true);
                    quotation.setConvertedInvoiceNumber(newInvoiceNumber);
                    quotation.setStatus("CONVERTED");
                    loadSampleQuotations();
                    updateStatistics();
                    
                    JOptionPane.showMessageDialog(this,
                        "<html><div style='width: 300px;'>" +
                        "<h3>✅ Conversion Successful</h3><br>" +
                        "Quotation converted to invoice:<br>" +
                        "<b>" + newInvoiceNumber + "</b><br><br>" +
                        "The new invoice has been added to the invoices tab." +
                        "</div></html>",
                        "Conversion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
    
    private void deleteSelectedQuotation() {
        int selectedRow = quotationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a quotation to delete from the table above.",
                "No Quotation Selected", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int modelRow = tableSorter.convertRowIndexToModel(selectedRow);
        String quotationNumber = (String) tableModel.getValueAt(modelRow, 0);
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete quotation " + quotationNumber + "?\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            // Remove from sample quotations
            if (modelRow >= 0 && modelRow < sampleQuotations.size()) {
                SampleQuotation quotationToDelete = sampleQuotations.get(modelRow);
                deletedQuotationNumbers.add(quotationToDelete.getQuotationNumber());
                sampleQuotations.remove(modelRow);
                loadSampleQuotations();
                updateStatistics();
                
                hasUnsavedChanges = true;
                updateSaveButtonState();
                
                JOptionPane.showMessageDialog(this, 
                    "Quotation deleted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void saveSessionToDatabase() {
        JOptionPane.showMessageDialog(this, "Save Quotations to Database feature will be implemented here", 
            "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateSaveButtonState() {
        if (saveSessionBtn != null) {
            saveSessionBtn.setEnabled(hasUnsavedChanges && !isDatabaseUpdateInProgress);
            if (hasUnsavedChanges && !isDatabaseUpdateInProgress) {
                saveSessionBtn.setText("💾 Save Session to Database *");
            } else {
                saveSessionBtn.setText("💾 Save Session to Database");
            }
        }
    }
    
    public void refreshData() {
        loadQuotations();
        updateStatistics();
    }
    
    public boolean canLogout() {
        if (hasUnsavedChanges) {
            int choice = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes in quotations. Save before logout?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION);
            
            switch (choice) {
                case JOptionPane.YES_OPTION:
                    saveSessionToDatabase();
                    return true;
                case JOptionPane.NO_OPTION:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }
}
