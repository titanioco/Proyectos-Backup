package com.raven.ui;

import com.raven.model.User;
import com.raven.ds.core.AnimationEngine;
import com.raven.ds.modules.bst.BSTPanel;
import com.raven.ds.modules.bst.BSTControls;
import com.raven.ds.modules.graph.GraphPanel;
import com.raven.ds.modules.graph.GraphControls;
import com.raven.ds.modules.hashtable.HashTablePanel;
import com.raven.ds.modules.hashtable.HashTableControls;
import com.raven.ds.modules.heap.BinaryHeapPanel;
import com.raven.ds.modules.heap.BinaryHeapControls;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardFrame extends JFrame {
    private User currentUser;
    private JTabbedPane mainTabbedPane;
    
    public DashboardFrame() {
        this(null);
    }
    
    public DashboardFrame(User user) {
        this.currentUser = user;
        initComponents();
        setupUI();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Learning Dashboard - " + (currentUser != null ? currentUser.getFullName() : "Guest"));
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("🎓 LEARNING DASHBOARD", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("Hello, " + (currentUser != null ? currentUser.getFullName() : "Guest"));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        JButton backButton = new JButton("← Back to Menu");
        backButton.setBackground(new Color(231, 76, 60));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> goBackToMenu());
        
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
        userPanel.add(backButton);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(logoutButton);
        userPanel.add(Box.createHorizontalStrut(20));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        // Create tabbed pane for data structures
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Create data structure tabs
        createDataStructureTabs(mainTabbedPane);
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(44, 62, 80));
        footerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel footerLabel = new JLabel("Interactive Data Structures Learning Suite - College Edition");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
    
    private void goBackToMenu() {
        SwingUtilities.invokeLater(() -> {
            MainSelectionFrame mainMenu = new MainSelectionFrame(currentUser);
            mainMenu.setVisible(true);
            this.dispose();
        });
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Create animation engine for BST
        AnimationEngine animationEngine = new AnimationEngine();
        
        // Create BST visualization components
        BSTPanel bstPanel = new BSTPanel(animationEngine);
        BSTControls bstControls = new BSTControls(bstPanel, animationEngine);
        
        // Create bottom panel to hold controls and info
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        
        // Add info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        JLabel infoLabel = new JLabel("<html><center><b>🌳 Binary Search Tree - Interactive Learning</b><br>" +
            "Use the controls below to insert, delete, search, and traverse the tree.<br>" +
            "Click 'Play' to see step-by-step animations!</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(85, 85, 85));
        infoPanel.add(infoLabel);
        
        // Setup layout - main content at center, controls at bottom
        bottomPanel.add(infoPanel, BorderLayout.NORTH);
        bottomPanel.add(bstControls, BorderLayout.SOUTH);
        
        panel.add(bstPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("BST", panel);
    }
    
    private void createGraphTab(JTabbedPane tabbedPane) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Create animation engine for Graph
        AnimationEngine animationEngine = new AnimationEngine();
        
        // Create Graph visualization components
        GraphPanel graphPanel = new GraphPanel(animationEngine);
        GraphControls graphControls = new GraphControls(graphPanel, animationEngine);
        
        // Create bottom panel to hold controls and info
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        
        // Add info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        JLabel infoLabel = new JLabel("<html><center><b>🕸️ Graph (Dijkstra) - Interactive Learning</b><br>" +
            "Use the controls below to create graphs and run Dijkstra's shortest path algorithm.<br>" +
            "Click 'Play' to see step-by-step path finding animations!</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(85, 85, 85));
        infoPanel.add(infoLabel);
        
        // Setup layout - main content at center, controls at bottom
        bottomPanel.add(infoPanel, BorderLayout.NORTH);
        bottomPanel.add(graphControls, BorderLayout.SOUTH);
        
        panel.add(graphPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Graph (Dijkstra)", panel);
    }
    
    private void createHashTableTab(JTabbedPane tabbedPane) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Create animation engine for Hash Table
        AnimationEngine animationEngine = new AnimationEngine();
        
        // Create Hash Table visualization components
        HashTablePanel hashTablePanel = new HashTablePanel(animationEngine);
        HashTableControls hashTableControls = new HashTableControls(hashTablePanel, animationEngine);
        
        // Create bottom panel to hold controls and info
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        
        // Add info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(Color.WHITE);
        JLabel infoLabel = new JLabel("<html><center><b>📊 Hash Table - Interactive Learning</b><br>" +
            "Use the controls below to insert, search, and delete key-value pairs.<br>" +
            "Explore different hash functions and collision resolution strategies!</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(85, 85, 85));
        infoPanel.add(infoLabel);
        
        // Setup layout - main content at center, controls at bottom
        bottomPanel.add(infoPanel, BorderLayout.NORTH);
        bottomPanel.add(hashTableControls, BorderLayout.SOUTH);
        
        panel.add(hashTablePanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Hash Table", panel);
    }
    
    private void createPlaceholderTab(JTabbedPane tabbedPane, String title, String description) {
        if (title.equals("Binary Heap")) {
            // Create full implementation for Binary Heap
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            
            // Create animation engine for Binary Heap
            AnimationEngine animationEngine = new AnimationEngine();
            
            // Create Binary Heap visualization components
            BinaryHeapPanel heapPanel = new BinaryHeapPanel(animationEngine);
            BinaryHeapControls heapControls = new BinaryHeapControls(heapPanel, animationEngine);
            
            // Create bottom panel to hold controls and info
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(Color.WHITE);
            
            // Add info panel
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            infoPanel.setBackground(Color.WHITE);
            JLabel infoLabel = new JLabel("<html><center><b>🌲 Binary Heap - Interactive Learning</b><br>" +
                "Use the controls below to insert, extract, and build heaps with visualization.<br>" +
                "Toggle between Min and Max heap modes!</center></html>");
            infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            infoLabel.setForeground(new Color(85, 85, 85));
            infoPanel.add(infoLabel);
            
            // Setup layout - main content at center, controls at bottom
            bottomPanel.add(infoPanel, BorderLayout.NORTH);
            bottomPanel.add(heapControls, BorderLayout.SOUTH);
            
            panel.add(heapPanel, BorderLayout.CENTER);
            panel.add(bottomPanel, BorderLayout.SOUTH);
            
            tabbedPane.addTab(title, panel);
        } else {
            // Create placeholder for other data structures
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setForeground(new Color(52, 73, 94));
            
            JLabel descLabel = new JLabel("<html><center>" + description + "<br><br><b>Implementation coming soon!</b><br><br>" +
                "This data structure will include:<br>" +
                "• Interactive visualization<br>" +
                "• Step-by-step animations<br>" +
                "• Educational explanations<br>" +
                "• Export capabilities</center></html>", SwingConstants.CENTER);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            descLabel.setForeground(new Color(85, 85, 85));
            
            panel.add(titleLabel, BorderLayout.CENTER);
            panel.add(descLabel, BorderLayout.SOUTH);
            
            tabbedPane.addTab(title, panel);
        }
    }
    
    public static void showFor(User user) {
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame(user).setVisible(true);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame();
            frame.setVisible(true);
        });
    }
}
