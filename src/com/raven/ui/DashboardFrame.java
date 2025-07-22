package com.raven.ui;

import com.raven.model.User;
import com.raven.ds.core.AnimationEngine;
import com.raven.ds.core.PDFExporter;
import com.raven.ds.modules.bst.BSTPanel;
import com.raven.ds.modules.bst.BSTControls;
import com.raven.ds.modules.graph.GraphPanel;
import com.raven.ds.modules.graph.GraphControls;
import com.raven.ds.modules.hashtable.HashTablePanel;
import com.raven.ds.modules.hashtable.HashTableControls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardFrame extends JFrame {
    private User currentUser;
    private JTabbedPane mainTabbedPane;
    
    public DashboardFrame(User user) {
        this.currentUser = user;
        initComponents();
        setupUI();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Dashboard - " + currentUser.getFullName());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        
        JLabel titleLabel = new JLabel("Interactive Data Structures Learning Suite");
        titleLabel.setFont(new Font("sansserif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("Hello, " + currentUser.getFullName());
        userLabel.setFont(new Font("sansserif", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        JButton exportBtn = new JButton("Export PDF");
        exportBtn.setBackground(new Color(7, 164, 121)); // Raven green
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFocusPainted(false);
        exportBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportCurrentTab());
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(exportBtn);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(logoutButton);
        userPanel.add(Box.createHorizontalStrut(20));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        // Create tabbed pane for data structures
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("sansserif", Font.PLAIN, 14));
        
        // Create data structure tabs
        createDataStructureTabs(mainTabbedPane);
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(44, 62, 80));
        footerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel footerLabel = new JLabel("Interactive Data Structures Learning Suite - College Edition");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        footerPanel.add(footerLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainTabbedPane, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Close dashboard window
            dispose();
            // Restart the main application to show login/register UI
            SwingUtilities.invokeLater(() -> {
                try {
                    new com.raven.main.Main().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    private void createDataStructureTabs(JTabbedPane tabbedPane) {
        // Tab 1: Binary Search Tree
        createBSTTab(tabbedPane);
        
        // Tab 2: Graph (Dijkstra)
        createGraphTab(tabbedPane);
        
        // Tab 3: Hash Table
        createHashTableTab(tabbedPane);
        
        // Tab 4-7: Placeholder tabs for other data structures
        createPlaceholderTab(tabbedPane, "Binary Heap", "Min/Max heap operations");
        createPlaceholderTab(tabbedPane, "Heapsort", "Heap-based sorting algorithm");
        createPlaceholderTab(tabbedPane, "AVL Tree", "Self-balancing binary search tree");
        createPlaceholderTab(tabbedPane, "Dynamic Array", "ArrayList implementation with resizing");
    }
    
    private void createBSTTab(JTabbedPane tabbedPane) {
        // Create animation engine for BST
        AnimationEngine bstEngine = new AnimationEngine();
        
        // Create BST panel and controls
        BSTPanel bstPanel = new BSTPanel(bstEngine);
        BSTControls bstControls = new BSTControls(bstPanel, bstEngine);
        
        // Create main BST container
        JPanel bstContainer = new JPanel(new BorderLayout());
        bstContainer.add(bstPanel, BorderLayout.CENTER);
        bstContainer.add(bstControls, BorderLayout.SOUTH);
        
        // Add demo button
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton demoBtn = new JButton("Load Demo Tree");
        demoBtn.setBackground(new Color(7, 164, 121));
        demoBtn.setForeground(Color.WHITE);
        demoBtn.setFocusPainted(false);
        demoBtn.addActionListener(e -> bstControls.loadDemo());
        demoPanel.add(demoBtn);
        demoPanel.setBackground(Color.WHITE);
        
        bstContainer.add(demoPanel, BorderLayout.NORTH);
        
        tabbedPane.addTab("BST", bstContainer);
    }
    
    private void createGraphTab(JTabbedPane tabbedPane) {
        // Create animation engine for Graph
        AnimationEngine graphEngine = new AnimationEngine();
        
        // Create Graph panel and controls
        GraphPanel graphPanel = new GraphPanel(graphEngine);
        GraphControls graphControls = new GraphControls(graphPanel, graphEngine);
        
        // Create main Graph container
        JPanel graphContainer = new JPanel(new BorderLayout());
        graphContainer.add(graphPanel, BorderLayout.CENTER);
        graphContainer.add(graphControls, BorderLayout.SOUTH);
        
        // Add demo button
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton demoBtn = new JButton("Load Demo Graph");
        demoBtn.setBackground(new Color(7, 164, 121));
        demoBtn.setForeground(Color.WHITE);
        demoBtn.setFocusPainted(false);
        demoBtn.addActionListener(e -> graphControls.loadDemo());
        demoPanel.add(demoBtn);
        demoPanel.setBackground(Color.WHITE);
        
        graphContainer.add(demoPanel, BorderLayout.NORTH);
        
        tabbedPane.addTab("Graph (Dijkstra)", graphContainer);
    }
    
    private void createHashTableTab(JTabbedPane tabbedPane) {
        // Create animation engine for Hash Table
        AnimationEngine hashEngine = new AnimationEngine();
        
        // Create Hash Table panel and controls
        HashTablePanel hashPanel = new HashTablePanel(hashEngine);
        HashTableControls hashControls = new HashTableControls(hashPanel, hashEngine);
        
        // Create main Hash Table container
        JPanel hashContainer = new JPanel(new BorderLayout());
        hashContainer.add(hashPanel, BorderLayout.CENTER);
        hashContainer.add(hashControls, BorderLayout.SOUTH);
        
        // Add demo button
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton demoBtn = new JButton("Load Demo Data");
        demoBtn.setBackground(new Color(7, 164, 121));
        demoBtn.setForeground(Color.WHITE);
        demoBtn.setFocusPainted(false);
        demoBtn.addActionListener(e -> hashControls.loadDemo());
        demoPanel.add(demoBtn);
        demoPanel.setBackground(Color.WHITE);
        
        hashContainer.add(demoPanel, BorderLayout.NORTH);
        
        tabbedPane.addTab("Hash Table", hashContainer);
    }
    
    private void createPlaceholderTab(JTabbedPane tabbedPane, String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("sansserif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel descLabel = new JLabel("<html><center>" + description + "<br><br>Coming soon!</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("sansserif", Font.PLAIN, 16));
        descLabel.setForeground(new Color(85, 85, 85));
        
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(descLabel, BorderLayout.SOUTH);
        
        tabbedPane.addTab(title, panel);
    }
    
    private void exportCurrentTab() {
        int selectedIndex = mainTabbedPane.getSelectedIndex();
        String tabTitle = mainTabbedPane.getTitleAt(selectedIndex);
        Component selectedComponent = mainTabbedPane.getSelectedComponent();
        
        if (selectedComponent instanceof JPanel) {
            JPanel panel = (JPanel) selectedComponent;
            String content = "This PDF contains the visualization and explanation for " + tabTitle + 
                           ". The interactive data structures learning suite helps college students " +
                           "understand algorithms through step-by-step animations.";
            
            PDFExporter.exportToPDF(panel, tabTitle, content);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cannot export this tab to PDF", 
                "Export Error", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public static void showFor(User user) {
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame(user).setVisible(true);
        });
    }
}
