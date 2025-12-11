package com.raven.accountability.ui.components.billing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Toolbar Panel - Action buttons for invoice operations
 * Small, focused component with CRUD buttons
 */
public class InvoiceToolbarPanel extends JPanel {
    
    private JButton createBtn, editBtn, viewBtn, deleteBtn, saveBtn, backupBtn;
    
    public InvoiceToolbarPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        setOpaque(false);
        
        // Create buttons
        createBtn = createButton("📄 Create", new Color(52, 152, 219));
        editBtn = createButton("✏️ Edit", new Color(39, 174, 96));
        viewBtn = createButton("👁️ View", new Color(155, 89, 182));
        deleteBtn = createButton("🗑️ Delete", new Color(231, 76, 60));
        saveBtn = createButton("💾 Save Changes", new Color(46, 125, 50));
        backupBtn = createButton("☁️ Cloud Backup", new Color(255, 143, 0));
        
        // Add to panel
        add(createBtn);
        add(editBtn);
        add(viewBtn);
        add(Box.createHorizontalStrut(20));
        add(deleteBtn);
        add(Box.createHorizontalStrut(20));
        add(saveBtn);
        add(backupBtn);
        
        // Initially disable edit/view/delete
        setSelectionButtonsEnabled(false);
        saveBtn.setEnabled(false);
    }
    
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                if (btn.isEnabled()) btn.setBackground(bg.darker()); 
            }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        
        return btn;
    }
    
    // Action listener setters
    public void setCreateAction(ActionListener listener) {
        createBtn.addActionListener(listener);
    }
    
    public void setEditAction(ActionListener listener) {
        editBtn.addActionListener(listener);
    }
    
    public void setViewAction(ActionListener listener) {
        viewBtn.addActionListener(listener);
    }
    
    public void setDeleteAction(ActionListener listener) {
        deleteBtn.addActionListener(listener);
    }
    
    public void setSaveAction(ActionListener listener) {
        saveBtn.addActionListener(listener);
    }
    
    public void setBackupAction(ActionListener listener) {
        backupBtn.addActionListener(listener);
    }
    
    // Enable/disable buttons based on selection
    public void setSelectionButtonsEnabled(boolean enabled) {
        editBtn.setEnabled(enabled);
        viewBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
    }
    
    public void setSaveButtonEnabled(boolean enabled) {
        saveBtn.setEnabled(enabled);
    }
    
    public void setBackupButtonState(boolean enabled, String text) {
        backupBtn.setEnabled(enabled);
        backupBtn.setText(text);
    }
}
