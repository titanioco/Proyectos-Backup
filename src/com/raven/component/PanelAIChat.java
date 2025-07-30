package com.raven.component;

import com.raven.service.AIAssistantService;
import com.raven.model.AIMessage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.CompletableFuture;

/**
 * AI Chat Panel Component
 * Provides an interactive chat interface for the AI assistant
 * 
 * @author RAVEN
 * @version 1.0
 */
public class PanelAIChat extends JPanel {
    
    private AIAssistantService aiService;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private boolean isProcessing = false;
    
    public PanelAIChat() {
        initComponents();
        initAIService();
        setupEventListeners();
    }
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Chat display area
        createChatArea();
        add(scrollPane, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
        
        // Initial welcome message
        addWelcomeMessage();
    }
    
    /**
     * Create header panel with title and status
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(getBackground());
        
        // Title
        JLabel titleLabel = new JLabel("🤖 AI Academic Assistant");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(64, 64, 64));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Status
        statusLabel = new JLabel("⚡ Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create chat display area
     */
    private void createChatArea() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.setPreferredSize(new Dimension(600, 400));
    }
    
    /**
     * Create input panel with text field and send button
     */
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(getBackground());
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Input field
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        inputField.setBackground(Color.WHITE);
        
        // Send button
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    /**
     * Initialize AI service
     */
    private void initAIService() {
        try {
            aiService = new AIAssistantService();
            updateStatus("🟢 AI services initialized");
        } catch (Exception e) {
            updateStatus("🔴 AI services unavailable");
            appendMessage("System", "AI services are currently unavailable. Please check your configuration.", true);
        }
    }
    
    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        // Send button action
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Enter key in input field
        inputField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !isProcessing) {
                    sendMessage();
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }
    
    /**
     * Add welcome message
     */
    private void addWelcomeMessage() {
        String welcomeMessage = "Welcome to your AI Academic Assistant! 🎓\\n\\n" +
                              "I'm here to help you with:\\n" +
                              "• Research methodologies and best practices\\n" +
                              "• Project planning and organization\\n" +
                              "• Academic writing and formatting\\n" +
                              "• Study strategies and learning techniques\\n\\n" +
                              "Feel free to ask me anything related to your academic work!";
        
        appendMessage("AI Assistant", welcomeMessage, false);
    }
    
    /**
     * Send user message to AI
     */
    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        
        if (userMessage.isEmpty() || isProcessing) {
            return;
        }
        
        // Clear input and disable controls
        inputField.setText("");
        setProcessing(true);
        
        // Display user message
        appendMessage("You", userMessage, false);
        
        // Send to AI service
        if (aiService != null) {
            updateStatus("🔄 Processing...");
            
            CompletableFuture<AIMessage> future = aiService.sendConversationMessage(userMessage);
            
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    appendMessage(response.getSenderDisplayName(), response.getContent(), response.isError());
                    
                    // Update status with response time
                    if (!response.isError()) {
                        updateStatus("✅ Response received (" + response.getResponseTimeMs() + "ms)");
                    } else {
                        updateStatus("❌ Error occurred");
                    }
                    
                    setProcessing(false);
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    appendMessage("System", "Sorry, I encountered an error: " + throwable.getMessage(), true);
                    updateStatus("❌ Error occurred");
                    setProcessing(false);
                });
                return null;
            });
            
        } else {
            appendMessage("System", "AI service is not available. Please check your configuration.", true);
            setProcessing(false);
        }
    }
    
    /**
     * Append message to chat area
     */
    private void appendMessage(String sender, String message, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            String prefix = "[" + timestamp + "] " + sender + ":\\n";
            
            // Add styling based on sender
            if (chatArea.getText().length() > 0) {
                chatArea.append("\\n\\n");
            }
            
            chatArea.append(prefix);
            chatArea.append(message);
            
            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    /**
     * Update status label
     */
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
        });
    }
    
    /**
     * Set processing state
     */
    private void setProcessing(boolean processing) {
        this.isProcessing = processing;
        sendButton.setEnabled(!processing);
        inputField.setEnabled(!processing);
        
        if (processing) {
            sendButton.setText("Sending...");
            inputField.setBackground(new Color(245, 245, 245));
        } else {
            sendButton.setText("Send");
            inputField.setBackground(Color.WHITE);
            inputField.requestFocus();
        }
    }
    
    /**
     * Clear chat history
     */
    public void clearChat() {
        chatArea.setText("");
        if (aiService != null) {
            aiService.clearHistory();
        }
        addWelcomeMessage();
        updateStatus("🧹 Chat cleared");
    }
    
    /**
     * Get specialized help for academic topics
     */
    public void getAcademicHelp(String query, String context) {
        if (aiService != null && !isProcessing) {
            setProcessing(true);
            updateStatus("🔄 Getting academic help...");
            
            CompletableFuture<AIMessage> future = aiService.getAcademicHelp(query, context);
            
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    appendMessage("Academic Assistant", response.getContent(), response.isError());
                    updateStatus("✅ Academic guidance provided");
                    setProcessing(false);
                });
            });
        }
    }
    
    /**
     * Get project-specific help
     */
    public void getProjectHelp(String projectTitle, String projectDescription, String question) {
        if (aiService != null && !isProcessing) {
            setProcessing(true);
            updateStatus("🔄 Analyzing project...");
            
            CompletableFuture<AIMessage> future = aiService.getProjectHelp(projectTitle, projectDescription, question);
            
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    appendMessage("Project Assistant", response.getContent(), response.isError());
                    updateStatus("✅ Project guidance provided");
                    setProcessing(false);
                });
            });
        }
    }
    
    /**
     * Get service status
     */
    public String getServiceStatus() {
        if (aiService != null) {
            return aiService.getServiceStatus();
        }
        return "❌ AI service not initialized";
    }
}
