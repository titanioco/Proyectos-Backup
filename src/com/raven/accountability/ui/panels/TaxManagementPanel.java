package com.raven.accountability.ui.panels;

import com.raven.accountability.model.TaxJurisdiction;
import com.raven.model.User;
import com.raven.accountability.service.TaxService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.math.BigDecimal;

/**
 * Tax Management Panel - comprehensive tax jurisdiction and rate management
 */
public class TaxManagementPanel extends JPanel {
    private User currentUser;
    private TaxService taxService;
    private JTable taxTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JLabel totalTaxesLabel;
    private JLabel activeTaxesLabel;
    private JLabel averageRateLabel;
    
    public TaxManagementPanel(User user) {
        this.currentUser = user;
        this.taxService = new TaxService();
        initComponents();
        setupLayout();
        loadTaxData();
        updateStatistics();
    }
    
    private void initComponents() {
        // Search and filter components
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.addActionListener(e -> filterTaxes());
        
        typeFilter = new JComboBox<>(new String[]{"All Types", "Federal", "State", "Local", "VAT", "Sales"});
        typeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeFilter.addActionListener(e -> filterTaxes());
        
        // Statistics labels
        totalTaxesLabel = new JLabel("0");
        totalTaxesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTaxesLabel.setForeground(new Color(52, 73, 94));
        
        activeTaxesLabel = new JLabel("0");
        activeTaxesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        activeTaxesLabel.setForeground(new Color(39, 174, 96));
        
        averageRateLabel = new JLabel("0.00%");
        averageRateLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        averageRateLabel.setForeground(new Color(142, 68, 173));
        
        // Table setup
        String[] columns = {"Jurisdiction", "Type", "Rate", "Status", "Effective Date", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        taxTable = new JTable(tableModel);
        taxTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        taxTable.setRowHeight(35);
        taxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taxTable.setGridColor(new Color(236, 240, 241));
        taxTable.setShowGrid(true);
        
        // Custom cell renderer for status column
        taxTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        
        // Set column widths
        taxTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Jurisdiction
        taxTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Type
        taxTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Rate
        taxTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Status
        taxTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Effective Date
        taxTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Description
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 221, 225)),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel titleLabel = new JLabel("🏛️ Tax Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel subtitleLabel = new JLabel("Manage tax jurisdictions, rates, and compliance");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        
        // Control panel
        JPanel controlPanel = createControlPanel();
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(new Color(248, 249, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));
        
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        
        JPanel middlePanel = new JPanel(new BorderLayout(0, 15));
        middlePanel.setOpaque(false);
        middlePanel.add(controlPanel, BorderLayout.NORTH);
        middlePanel.add(tablePanel, BorderLayout.CENTER);
        
        contentPanel.add(middlePanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        
        // Total taxes card
        JPanel totalCard = createStatCard("📊 Total Taxes", totalTaxesLabel, 
            "All tax jurisdictions", new Color(52, 73, 94));
        
        // Active taxes card  
        JPanel activeCard = createStatCard("✅ Active Taxes", activeTaxesLabel,
            "Currently effective", new Color(39, 174, 96));
        
        // Average rate card
        JPanel averageCard = createStatCard("📈 Average Rate", averageRateLabel,
            "Weighted average", new Color(142, 68, 173));
        
        statsPanel.add(totalCard);
        statsPanel.add(activeCard);
        statsPanel.add(averageCard);
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(236, 240, 241), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Icon and title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(accentColor);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Value
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(236, 240, 241), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Left side - search and filters
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("🔍");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        typeFilter.setPreferredSize(new Dimension(120, 30));
        
        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(new JLabel("Type:"));
        leftPanel.add(typeFilter);
        
        // Right side - action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JButton addBtn = createActionButton("➕ Add Tax", new Color(39, 174, 96));
        addBtn.addActionListener(e -> addTax());
        
        JButton editBtn = createActionButton("✏️ Edit", new Color(52, 152, 219));
        editBtn.addActionListener(e -> editTax());
        
        JButton deleteBtn = createActionButton("🗑️ Delete", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> deleteTax());
        
        JButton refreshBtn = createActionButton("🔄 Refresh", new Color(149, 165, 166));
        refreshBtn.addActionListener(e -> refreshData());
        
        rightPanel.add(addBtn);
        rightPanel.add(editBtn);
        rightPanel.add(deleteBtn);
        rightPanel.add(refreshBtn);
        
        controlPanel.add(leftPanel, BorderLayout.WEST);
        controlPanel.add(rightPanel, BorderLayout.EAST);
        
        return controlPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(236, 240, 241), 1));
        
        JScrollPane scrollPane = new JScrollPane(taxTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JButton createActionButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void loadTaxData() {
        tableModel.setRowCount(0);
        
        try {
            List<TaxJurisdiction> taxes = taxService.getAllTaxJurisdictions();
            
            for (TaxJurisdiction tax : taxes) {
                Object[] row = {
                    tax.getJurisdictionName(),
                    tax.getTaxType(),
                    tax.getTaxRate().toString() + "%",
                    tax.isActive() ? "Active" : "Inactive",
                    tax.getEffectiveDate().toString(),
                    tax.getDescription()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading tax data: " + e.getMessage(),
                "Data Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterTaxes() {
        // Implementation for filtering based on search and type
        loadTaxData(); // For now, just reload all data
    }
    
    private void updateStatistics() {
        try {
            List<TaxJurisdiction> allTaxes = taxService.getAllTaxJurisdictions();
            
            int totalTaxes = allTaxes.size();
            int activeTaxes = (int) allTaxes.stream().filter(TaxJurisdiction::isActive).count();
            
            // Calculate average rate
            BigDecimal averageRate = BigDecimal.ZERO;
            if (!allTaxes.isEmpty()) {
                BigDecimal totalRate = allTaxes.stream()
                    .map(TaxJurisdiction::getTaxRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                averageRate = totalRate.divide(new BigDecimal(totalTaxes), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            totalTaxesLabel.setText(String.valueOf(totalTaxes));
            activeTaxesLabel.setText(String.valueOf(activeTaxes));
            averageRateLabel.setText(averageRate.toString() + "%");
            
        } catch (Exception e) {
            totalTaxesLabel.setText("Error");
            activeTaxesLabel.setText("Error");
            averageRateLabel.setText("Error");
        }
    }
    
    private void addTax() {
        // Open tax dialog for creating new tax jurisdiction
        JOptionPane.showMessageDialog(this, 
            "Tax creation dialog will be implemented here",
            "Add Tax", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void editTax() {
        int selectedRow = taxTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a tax jurisdiction to edit",
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Open tax dialog for editing selected tax
        JOptionPane.showMessageDialog(this, 
            "Tax editing dialog will be implemented here",
            "Edit Tax", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteTax() {
        int selectedRow = taxTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a tax jurisdiction to delete",
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this tax jurisdiction?\nThis action cannot be undone.",
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            // Delete the tax jurisdiction
            JOptionPane.showMessageDialog(this, 
                "Tax deletion will be implemented here",
                "Delete Tax", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void refreshData() {
        loadTaxData();
        updateStatistics();
    }
    
    // Custom cell renderer for status column
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                if ("Active".equals(status)) {
                    setBackground(isSelected ? new Color(39, 174, 96) : new Color(212, 237, 223));
                    setForeground(isSelected ? Color.WHITE : new Color(39, 174, 96));
                } else {
                    setBackground(isSelected ? new Color(231, 76, 60) : new Color(242, 215, 213));
                    setForeground(isSelected ? Color.WHITE : new Color(231, 76, 60));
                }
            }
            
            return c;
        }
    }
}
