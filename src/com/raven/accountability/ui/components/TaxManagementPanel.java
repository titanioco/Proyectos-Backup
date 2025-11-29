package com.raven.accountability.ui.components;

import com.raven.model.User;
import com.raven.accountability.model.*;
import com.raven.accountability.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;

/**
 * Tax Management Panel
 * Comprehensive tax management and compliance system
 */
public class TaxManagementPanel extends JPanel {
    private User currentUser;
    private TaxService taxService;
    
    // Main components
    private JTable taxJurisdictionsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JButton addJurisdictionBtn;
    private JButton editJurisdictionBtn;
    private JButton deleteJurisdictionBtn;
    private JButton calculateTaxBtn;
    
    // Statistics panels
    private JPanel statsPanel;
    private JLabel totalJurisdictionsLabel;
    private JLabel activeJurisdictionsLabel;
    private JLabel avgTaxRateLabel;
    private JLabel lastUpdatedLabel;
    
    public TaxManagementPanel(User user) {
        this.currentUser = user;
        
        // Run in demo mode without database connection
        this.taxService = null;
        System.out.println("TAX: Initializing in demo mode (database-free)");
        
        try {
            initComponents();
            setupLayout();
            loadTaxJurisdictions();
            updateStatistics();
        } catch (Exception e) {
            System.err.println("TAX ERROR: Failed to initialize TaxManagementPanel - " + e.getMessage());
            e.printStackTrace();
            // Show simplified interface if initialization fails
            initializeBasicInterface();
        }
    }
    
    private void initComponents() {
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Initialize table
        String[] columnNames = {"Jurisdiction", "Type", "Tax Rate", "Status", "Effective Date", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        taxJurisdictionsTable = new JTable(tableModel);
        taxJurisdictionsTable.setRowHeight(35);
        taxJurisdictionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        taxJurisdictionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        taxJurisdictionsTable.getTableHeader().setBackground(new Color(238, 242, 248)); // Very light shade of #425c80
        taxJurisdictionsTable.getTableHeader().setForeground(new Color(66, 92, 128)); // #425c80 for text
        
        // Custom header renderer that preserves Nimbus functionality while changing colors
        javax.swing.table.TableCellRenderer originalRenderer = taxJurisdictionsTable.getTableHeader().getDefaultRenderer();
        taxJurisdictionsTable.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                // Get the original Nimbus renderer component (preserves sorting icons, hover effects)
                java.awt.Component component = originalRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                // Override just the colors while preserving all other functionality
                if (component instanceof javax.swing.JLabel) {
                    javax.swing.JLabel label = (javax.swing.JLabel) component;
                    label.setOpaque(true);
                    label.setBackground(new Color(238, 242, 248)); // Very light shade of #425c80
                    label.setForeground(new Color(66, 92, 128)); // #425c80 for text
                }
                
                return component;
            }
        });
        
        // Set table background and selection colors using shades of #425c80
        taxJurisdictionsTable.setBackground(new Color(223, 230, 240)); // Very light shade of #425c80
        taxJurisdictionsTable.setSelectionBackground(new Color(66, 92, 128)); // #425c80
        taxJurisdictionsTable.setSelectionForeground(Color.WHITE);
        taxJurisdictionsTable.setGridColor(new Color(189, 203, 220)); // Medium shade of #425c80
        
        // Custom cell renderer for alternating row colors with #425c80 theme
        taxJurisdictionsTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
                
                return this;
            }
        });
        
        // Add table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        taxJurisdictionsTable.setRowSorter(tableSorter);
        
        // Table double-click listener
        taxJurisdictionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedJurisdiction();
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
        
        String[] typeOptions = {"All Types", "STATE", "COUNTY", "CITY", "FEDERAL", "SPECIAL"};
        typeFilter = new JComboBox<>(typeOptions);
        typeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Search listeners
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        
        typeFilter.addActionListener(e -> filterTable());
        
        // Action buttons
        addJurisdictionBtn = createActionButton("➕ Add Jurisdiction", new Color(52, 152, 219));
        editJurisdictionBtn = createActionButton("✏️ Edit Jurisdiction", new Color(39, 174, 96));
        deleteJurisdictionBtn = createActionButton("🗑️ Delete Jurisdiction", new Color(231, 76, 60));
        calculateTaxBtn = createActionButton("🧮 Calculate Tax", new Color(155, 89, 182));
        
        // Button listeners
        addJurisdictionBtn.addActionListener(e -> addNewJurisdiction());
        editJurisdictionBtn.addActionListener(e -> editSelectedJurisdiction());
        deleteJurisdictionBtn.addActionListener(e -> deleteSelectedJurisdiction());
        calculateTaxBtn.addActionListener(e -> showTaxCalculator());
        
        // Initially disable edit/delete buttons
        editJurisdictionBtn.setEnabled(false);
        deleteJurisdictionBtn.setEnabled(false);
        
        // Table selection listener
        taxJurisdictionsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = taxJurisdictionsTable.getSelectedRow() != -1;
            editJurisdictionBtn.setEnabled(hasSelection);
            deleteJurisdictionBtn.setEnabled(hasSelection);
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
        JScrollPane tableScrollPane = new JScrollPane(taxJurisdictionsTable);
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
        
        JLabel titleLabel = new JLabel("🏛️ Tax Management & Compliance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel subtitleLabel = new JLabel("Manage tax jurisdictions, rates, and compliance requirements");
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
        totalJurisdictionsLabel = new JLabel("0");
        activeJurisdictionsLabel = new JLabel("0");
        avgTaxRateLabel = new JLabel("0.0%");
        lastUpdatedLabel = new JLabel("Never");
        
        statsPanel.add(createStatCard("🌍", "Total Jurisdictions", totalJurisdictionsLabel, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("✅", "Active", activeJurisdictionsLabel, new Color(39, 174, 96)));
        statsPanel.add(createStatCard("📊", "Avg Tax Rate", avgTaxRateLabel, new Color(230, 126, 34)));
        statsPanel.add(createStatCard("🕒", "Last Updated", lastUpdatedLabel, new Color(155, 89, 182)));
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
        
        JLabel filterLabel = new JLabel("Type:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterLabel.setForeground(new Color(52, 73, 94));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(filterLabel);
        searchPanel.add(typeFilter);
        
        return searchPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        buttonPanel.add(addJurisdictionBtn);
        buttonPanel.add(editJurisdictionBtn);
        buttonPanel.add(calculateTaxBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(deleteJurisdictionBtn);
        
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
    
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String typeSelected = (String) typeFilter.getSelectedItem();
        
        if (searchText.isEmpty() && "All Types".equals(typeSelected)) {
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
                    
                    // Type filter
                    if (!"All Types".equals(typeSelected)) {
                        String rowType = entry.getStringValue(1); // Type column
                        if (!typeSelected.equals(rowType)) {
                            return false;
                        }
                    }
                    
                    return true;
                }
            });
        }
    }
    
    private void loadTaxJurisdictions() {
        try {
            if (taxService == null) {
                System.err.println("TAX WARNING: TaxService not available - loading sample data");
                loadSampleJurisdictions();
                return;
            }
            
            List<TaxJurisdiction> jurisdictions = taxService.getAllTaxJurisdictions();
            
            // Clear existing data
            tableModel.setRowCount(0);
            
            if (jurisdictions.isEmpty()) {
                System.out.println("TAX INFO: No jurisdictions found - loading sample data");
                loadSampleJurisdictions();
                return;
            }
            
            // Add jurisdiction data to table
            for (TaxJurisdiction jurisdiction : jurisdictions) {
                Object[] rowData = {
                    jurisdiction.getJurisdictionName(),
                    jurisdiction.getJurisdictionType(),
                    jurisdiction.getTaxRate().multiply(new BigDecimal(100)).toString() + "%",
                    jurisdiction.isActive() ? "Active" : "Inactive",
                    jurisdiction.getEffectiveDate().toString(),
                    jurisdiction.getDescription()
                };
                tableModel.addRow(rowData);
            }
            
            System.out.println("TAX: Loaded " + jurisdictions.size() + " tax jurisdictions");
            
        } catch (SQLException e) {
            System.err.println("TAX ERROR: Failed to load jurisdictions - " + e.getMessage());
            System.out.println("TAX INFO: Loading sample data instead");
            loadSampleJurisdictions();
        } catch (Exception e) {
            System.err.println("TAX ERROR: Unexpected error - " + e.getMessage());
            loadSampleJurisdictions();
        }
    }
    
    private void loadSampleJurisdictions() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add sample jurisdiction data
        Object[][] sampleData = {
            {"California State", "STATE", "7.25%", "Active", "2025-01-01", "California state sales tax"},
            {"Los Angeles County", "COUNTY", "1.00%", "Active", "2025-01-01", "LA County additional tax"},
            {"New York State", "STATE", "8.00%", "Active", "2025-01-01", "New York state sales tax"},
            {"Texas State", "STATE", "6.25%", "Active", "2025-01-01", "Texas state sales tax"},
            {"Federal Income", "FEDERAL", "22.00%", "Active", "2025-01-01", "Federal income tax bracket"},
            {"Miami-Dade County", "COUNTY", "0.75%", "Active", "2025-01-01", "Miami-Dade additional tax"},
            {"Seattle City", "CITY", "3.60%", "Active", "2025-01-01", "Seattle city business tax"},
            {"Special District A", "SPECIAL", "2.50%", "Inactive", "2024-01-01", "Special taxing district"}
        };
        
        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }
        
        System.out.println("TAX: Loaded " + sampleData.length + " sample tax jurisdictions");
    }
    
    private void updateStatistics() {
        try {
            if (taxService == null) {
                System.err.println("TAX WARNING: TaxService not available - using sample statistics");
                updateSampleStatistics();
                return;
            }
            
            // This would be implemented in TaxService
            // TaxService.TaxStats stats = taxService.getTaxStats();
            // For now, use sample statistics
            updateSampleStatistics();
            
        } catch (Exception e) {
            System.err.println("TAX ERROR: Failed to load statistics - " + e.getMessage());
            updateSampleStatistics();
        }
    }
    
    private void updateSampleStatistics() {
        totalJurisdictionsLabel.setText("8");
        activeJurisdictionsLabel.setText("7");
        avgTaxRateLabel.setText("6.19%");
        lastUpdatedLabel.setText("Today");
    }
    
    private void addNewJurisdiction() {
        if (taxService == null) {
            JOptionPane.showMessageDialog(this, 
                "Jurisdiction creation is not available - database service not initialized.\n" +
                "This is a demo mode showing sample data only.",
                "Service Not Available", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // This would open a jurisdiction dialog
        JOptionPane.showMessageDialog(this, 
            "Add New Tax Jurisdiction dialog would open here.\n" +
            "This feature requires full database implementation.",
            "Feature Coming Soon", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void editSelectedJurisdiction() {
        int selectedRow = taxJurisdictionsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int modelRow = tableSorter.convertRowIndexToModel(selectedRow);
        String jurisdictionName = (String) tableModel.getValueAt(modelRow, 0);
        
        if (taxService == null) {
            JOptionPane.showMessageDialog(this, 
                "Editing '" + jurisdictionName + "' is not available - database service not initialized.\n" +
                "This is a demo mode showing sample data only.",
                "Service Not Available", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // This would open an edit jurisdiction dialog
        JOptionPane.showMessageDialog(this, 
            "Edit Tax Jurisdiction dialog for '" + jurisdictionName + "' would open here.\n" +
            "This feature requires full database implementation.",
            "Feature Coming Soon", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteSelectedJurisdiction() {
        int selectedRow = taxJurisdictionsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int modelRow = tableSorter.convertRowIndexToModel(selectedRow);
        String jurisdictionName = (String) tableModel.getValueAt(modelRow, 0);
        
        if (taxService == null) {
            JOptionPane.showMessageDialog(this, 
                "Jurisdiction deletion is not available - database service not initialized.\n" +
                "This is a demo mode showing sample data only.",
                "Service Not Available", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete tax jurisdiction '" + jurisdictionName + "'?\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            // This would delete from database
            JOptionPane.showMessageDialog(this, 
                "Delete functionality requires full database implementation.",
                "Feature Coming Soon", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showTaxCalculator() {
        JOptionPane.showMessageDialog(this, 
            "Tax Calculator\n\n" +
            "This feature would open a comprehensive tax calculator\n" +
            "supporting multiple jurisdictions and tax types.\n\n" +
            "Coming in the next development phase!",
            "Tax Calculator (Coming Soon)", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void refreshData() {
        loadTaxJurisdictions();
        updateStatistics();
    }
    
    private void initializeBasicInterface() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel errorLabel = new JLabel("<html><center><h2>Tax Management Module Initialization Error</h2><br>" +
            "The tax management module could not be fully initialized.<br>" +
            "Please check the database connection and try again.</center></html>");
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setForeground(new Color(231, 76, 60));
        
        messagePanel.add(errorLabel, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.CENTER);
    }
}
