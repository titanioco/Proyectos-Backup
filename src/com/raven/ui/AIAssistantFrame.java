package com.raven.ui;

import com.raven.model.User;
import com.raven.service.AIService;
import com.raven.service.FirebaseService;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * AI Assistant Frame for text processing and chat functionality.
 * This frame provides an interface for AI-powered assistance including
 * text analysis, document processing, and interactive chat.
 */
public class AIAssistantFrame extends JFrame {
    private User currentUser;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton audioButton;
    private JButton videoButton;
    private JButton fileButton;
    private AIService aiService;
    private FirebaseService firebaseService;
    
    public AIAssistantFrame() {
        this(null);
    }
    
    public AIAssistantFrame(User user) {
        this.currentUser = user;
        this.aiService = AIService.getInstance();
        this.firebaseService = FirebaseService.getInstance();
        initComponents();
        setupUI();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("AI Assistant - " + (currentUser != null ? currentUser.getFullName() : "Guest"));
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(231, 76, 60));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("🤖 AI ASSISTANT", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        // User panel with navigation buttons
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("Hello, " + (currentUser != null ? currentUser.getFullName() : "Guest"));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        JButton backButton = new JButton("← Back to Menu");
        backButton.setBackground(new Color(52, 152, 219));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> goBackToMenu());
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(52, 152, 219));
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
        
        // Main content panel
        JPanel mainContentPanel = createMainContentPanel();
        
        // Footer panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(44, 62, 80));
        footerPanel.setPreferredSize(new Dimension(0, 40));
        
        JLabel footerLabel = new JLabel("AI Assistant - Powered by Advanced Language Models");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerPanel.add(footerLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the main content panel with chat interface
     */
    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chatArea.setText("Welcome to the AI Assistant!\n\n" +
                "🤖 AI Service Status: " + aiService.getServiceStatus() + "\n" +
                "👤 User: " + (currentUser != null ? currentUser.getFullName() + " (" + currentUser.getUserType() + ")" : "Guest") + "\n\n" +
                "Features available:\n" +
                "• 💬 Text conversations with AI\n" +
                "• 🎵 Audio file processing (upload and analyze)\n" +
                "• 🎥 Video file analysis (upload and analyze)\n" +
                "• 📁 Document processing\n" +
                "• 🎯 Academic assistance and research help\n\n" +
                "Start typing below or use the media buttons to upload files...\n");
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("AI Chat Interface"));
        
        // Input panel with media buttons
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Media buttons panel
        JPanel mediaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        audioButton = new JButton("🎵 Audio");
        audioButton.setToolTipText("Upload and process audio files");
        audioButton.setBackground(new Color(52, 152, 219));
        audioButton.setForeground(Color.WHITE);
        audioButton.setFocusPainted(false);
        audioButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        audioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        audioButton.addActionListener(e -> handleAudioUpload());
        
        videoButton = new JButton("🎥 Video");
        videoButton.setToolTipText("Upload and analyze video files");
        videoButton.setBackground(new Color(155, 89, 182));
        videoButton.setForeground(Color.WHITE);
        videoButton.setFocusPainted(false);
        videoButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        videoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        videoButton.addActionListener(e -> handleVideoUpload());
        
        fileButton = new JButton("📁 File");
        fileButton.setToolTipText("Upload and process documents");
        fileButton.setBackground(new Color(46, 204, 113));
        fileButton.setForeground(Color.WHITE);
        fileButton.setFocusPainted(false);
        fileButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        fileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fileButton.addActionListener(e -> handleFileUpload());
        
        mediaPanel.add(audioButton);
        mediaPanel.add(videoButton);
        mediaPanel.add(fileButton);
        
        // Text input panel
        JPanel textInputPanel = new JPanel(new BorderLayout(10, 0));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(231, 76, 60));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add action listeners
        ActionListener sendAction = e -> sendMessage();
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction); // Enter key support
        
        textInputPanel.add(new JLabel("Message:"), BorderLayout.WEST);
        textInputPanel.add(inputField, BorderLayout.CENTER);
        textInputPanel.add(sendButton, BorderLayout.EAST);
        
        inputPanel.add(mediaPanel, BorderLayout.NORTH);
        inputPanel.add(textInputPanel, BorderLayout.SOUTH);
        
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.append("You: " + message + "\n");
            inputField.setText("");
            
            // Disable input while processing
            inputField.setEnabled(false);
            sendButton.setEnabled(false);
            chatArea.append("AI Assistant: Thinking...\n");
            
            // Send to AI service
            CompletableFuture<String> response = aiService.sendMessage(message, currentUser);
            response.thenAccept(aiResponse -> {
                SwingUtilities.invokeLater(() -> {
                    // Remove "Thinking..." message
                    String currentText = chatArea.getText();
                    int lastThinking = currentText.lastIndexOf("AI Assistant: Thinking...\n");
                    if (lastThinking != -1) {
                        chatArea.setText(currentText.substring(0, lastThinking));
                    }
                    
                    chatArea.append("AI Assistant: " + aiResponse + "\n\n");
                    
                    // Auto-scroll to bottom
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    
                    // Re-enable input
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocus();
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    // Remove "Thinking..." message
                    String currentText = chatArea.getText();
                    int lastThinking = currentText.lastIndexOf("AI Assistant: Thinking...\n");
                    if (lastThinking != -1) {
                        chatArea.setText(currentText.substring(0, lastThinking));
                    }
                    
                    chatArea.append("AI Assistant: I apologize, but I encountered an error: " + 
                                  throwable.getMessage() + "\n\n");
                    
                    // Auto-scroll to bottom
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    
                    // Re-enable input
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocus();
                });
                return null;
            });
        }
    }
    
    private void handleAudioUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Audio File");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Audio Files", "mp3", "wav", "m4a", "ogg", "flac"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            chatArea.append("You: [Uploaded audio file: " + selectedFile.getName() + "]\n");
            chatArea.append("AI Assistant: Processing audio file...\n");
            
            // Disable buttons while processing
            audioButton.setEnabled(false);
            
            CompletableFuture<String> response = aiService.processAudio(selectedFile, currentUser);
            response.thenAccept(aiResponse -> {
                SwingUtilities.invokeLater(() -> {
                    // Remove "Processing..." message  
                    String currentText = chatArea.getText();
                    int lastProcessing = currentText.lastIndexOf("AI Assistant: Processing audio file...\n");
                    if (lastProcessing != -1) {
                        chatArea.setText(currentText.substring(0, lastProcessing));
                    }
                    
                    chatArea.append("AI Assistant: " + aiResponse + "\n\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    audioButton.setEnabled(true);
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("AI Assistant: Audio processing failed: " + 
                                  throwable.getMessage() + "\n\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    audioButton.setEnabled(true);
                });
                return null;
            });
        }
    }
    
    private void handleVideoUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Video File");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Video Files", "mp4", "avi", "mov", "mkv", "webm"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            chatArea.append("You: [Uploaded video file: " + selectedFile.getName() + "]\n");
            chatArea.append("AI Assistant: Analyzing video file...\n");
            
            // Disable buttons while processing
            videoButton.setEnabled(false);
            
            CompletableFuture<String> response = aiService.processVideo(selectedFile, currentUser);
            response.thenAccept(aiResponse -> {
                SwingUtilities.invokeLater(() -> {
                    // Remove "Analyzing..." message
                    String currentText = chatArea.getText();
                    int lastAnalyzing = currentText.lastIndexOf("AI Assistant: Analyzing video file...\n");
                    if (lastAnalyzing != -1) {
                        chatArea.setText(currentText.substring(0, lastAnalyzing));
                    }
                    
                    chatArea.append("AI Assistant: " + aiResponse + "\n\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    videoButton.setEnabled(true);
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("AI Assistant: Video analysis failed: " + 
                                  throwable.getMessage() + "\n\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    videoButton.setEnabled(true);
                });
                return null;
            });
        }
    }
    
    private void handleFileUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Document File");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Document Files", "txt", "pdf", "doc", "docx", "rtf"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            chatArea.append("You: [Uploaded document: " + selectedFile.getName() + "]\n");
            
            // For now, just acknowledge the file upload
            // In a full implementation, this would extract text and send to AI
            chatArea.append("AI Assistant: I received your document '" + selectedFile.getName() + 
                          "'. Document processing will be available in the next update. " +
                          "For now, you can ask me questions about the content by typing them below.\n\n");
        }
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
            // Close current window
            dispose();
            // Use SessionManager to return to original login screen
            com.raven.service.SessionManager.getInstance().logout();
        }
    }
    
    private void goBackToMenu() {
        SwingUtilities.invokeLater(() -> {
            this.dispose();
            // Use SessionManager to return to existing MainSelectionFrame
            com.raven.service.SessionManager.getInstance().showMainSelectionFrame();
        });
    }
    
    /**
     * Static factory method for creating and showing the frame with user data
     */
    public static void showFor(User user) {
        SwingUtilities.invokeLater(() -> {
            new AIAssistantFrame(user).setVisible(true);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AIAssistantFrame frame = new AIAssistantFrame();
            frame.setVisible(true);
        });
    }
}
