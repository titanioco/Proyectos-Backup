package com.raven.ui;

import com.raven.model.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainSelectionFrame extends JFrame {
    
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel titleLabel;
    private User currentUser;
    
    public MainSelectionFrame() {
        this(null);
    }
    
    public MainSelectionFrame(User user) {
        this.currentUser = user;
        initComponents();
        setupEvents();
    }
    
    private void initComponents() {
        setTitle("University Project Manager - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(245, 245, 245));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 73, 94));
        titlePanel.setPreferredSize(new Dimension(1200, 100));
        titlePanel.setLayout(new BorderLayout());
        
        titleLabel = new JLabel("UNIVERSITY PROJECT MANAGER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // Selection panels container
        JPanel selectionContainer = new JPanel(new BorderLayout());
        selectionContainer.setBackground(new Color(245, 245, 245));
        selectionContainer.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Left panel (Learning Dashboard)
        leftPanel = createSelectionPanel("🎓 LEARNING DASHBOARD", 
                                       "Manage your academic projects, track progress, and organize your studies", 
                                       new Color(52, 152, 219));
        
        // Right panel (AI Assistant)
        rightPanel = createSelectionPanel("🤖 AI ASSISTANT", 
                                        "Get help with transcription, document analysis, and AI-powered assistance", 
                                        new Color(231, 76, 60));
        
        selectionContainer.add(leftPanel, BorderLayout.WEST);
        selectionContainer.add(rightPanel, BorderLayout.EAST);
        
        mainContainer.add(titlePanel, BorderLayout.NORTH);
        mainContainer.add(selectionContainer, BorderLayout.CENTER);
        
        setContentPane(mainContainer);
    }
    
    private JPanel createSelectionPanel(String title, String description, Color accentColor) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Add accent border
                g2.setColor(accentColor);
                g2.setStroke(new java.awt.BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
                
                g2.dispose();
            }
        };
        
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(500, 600));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Title label
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(accentColor);
        
        // Description label
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(new Color(52, 73, 94));
        
        // Icon label (placeholder for now)
        JLabel iconLabel = new JLabel("📚", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        
        // Features list
        JLabel featuresLabel = new JLabel();
        if (title.contains("DASHBOARD")) {
            featuresLabel.setText("<html><div style='text-align: left;'>" +
                "• Project Management<br>" +
                "• Progress Tracking<br>" +
                "• File Organization<br>" +
                "• Collaboration Tools<br>" +
                "• Academic Calendar" +
                "</div></html>");
        } else {
            featuresLabel.setText("<html><div style='text-align: left;'>" +
                "• Voice Transcription<br>" +
                "• Document Analysis<br>" +
                "• Video Summarization<br>" +
                "• File Processing<br>" +
                "• AI Chat Assistant" +
                "</div></html>");
        }
        featuresLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        featuresLabel.setForeground(new Color(52, 73, 94));
        
        // Layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(descLabel, BorderLayout.SOUTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(featuresLabel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupEvents() {
        // Left panel click (Learning Dashboard)
        leftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLearningDashboard();
                
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                leftPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 4),
                    BorderFactory.createEmptyBorder(36, 36, 36, 36)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
            }
        });
        
        // Right panel click (AI Assistant)
        rightPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openAIAssistant();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                rightPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(231, 76, 60), 4),
                    BorderFactory.createEmptyBorder(36, 36, 36, 36)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
            }
        });
    }
    
    private void openLearningDashboard() {
        SwingUtilities.invokeLater(() -> {
            // Open the existing dashboard with user data
            if (currentUser != null) {
                DashboardFrame.showFor(currentUser);
            } else {
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
            }
            this.setVisible(false);
        });
    }
    
    private void openAIAssistant() {
        SwingUtilities.invokeLater(() -> {
            AIAssistantFrame aiAssistant = new AIAssistantFrame();
            aiAssistant.setVisible(true);
            this.setVisible(false);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainSelectionFrame frame = new MainSelectionFrame();
            frame.setVisible(true);
        });
    }
} 