package com.raven.accountability.ui.components.billing;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Statistics Panel - Displays invoice statistics in card format
 * Small, focused component showing 4 key metrics
 */
public class InvoiceStatsPanel extends JPanel {
    
    private JLabel totalInvoicesLabel;
    private JLabel totalAmountLabel;
    private JLabel outstandingLabel;
    private JLabel overdueLabel;
    
    public InvoiceStatsPanel() {
        setLayout(new GridLayout(1, 4, 15, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(0, 100));
        
        // Initialize labels
        totalInvoicesLabel = new JLabel("0");
        totalAmountLabel = new JLabel("$0.00");
        outstandingLabel = new JLabel("$0.00");
        overdueLabel = new JLabel("$0.00");
        
        // Create stat cards
        add(createStatCard("📊", "Total Invoices", totalInvoicesLabel, new Color(52, 152, 219)));
        add(createStatCard("💰", "Total Amount", totalAmountLabel, new Color(39, 174, 96)));
        add(createStatCard("⏳", "Outstanding", outstandingLabel, new Color(230, 126, 34)));
        add(createStatCard("⚠️", "Overdue", overdueLabel, new Color(231, 76, 60)));
    }
    
    private JPanel createStatCard(String icon, String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 221, 225)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
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
        valueLabel.setForeground(accentColor);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Update statistics with new values
     */
    public void updateStats(int totalCount, BigDecimal totalAmount, 
                           BigDecimal outstanding, BigDecimal overdue) {
        totalInvoicesLabel.setText(String.valueOf(totalCount));
        totalAmountLabel.setText("$" + (totalAmount != null ? totalAmount.toString() : "0.00"));
        outstandingLabel.setText("$" + (outstanding != null ? outstanding.toString() : "0.00"));
        overdueLabel.setText("$" + (overdue != null ? overdue.toString() : "0.00"));
    }
    
    /**
     * Reset all statistics to zero
     */
    public void reset() {
        updateStats(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
