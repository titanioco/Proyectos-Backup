package com.raven.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import com.raven.service.AIAssistantService;
import com.raven.model.AIMessage;
import com.raven.util.ConversationHistorySaver;

public class AIAssistantFrame extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton saveHistoryButton;
    private JButton voiceButton;
    private JButton videoButton;
    private JButton documentButton;
    private JButton logoutButton;
    private JLabel statusLabel;
    private JComboBox<String> aiProviderCombo;
    private JComboBox<String> modeCombo;
    private JComboBox<String> modelCombo;
    private JCheckBox paidModeCheckbox;
    private AIAssistantService aiService;
    private List<ConversationHistorySaver.ConversationEntry> conversationHistory;
    
    // AI Models with pricing info
    private final String[] OPENAI_MODELS = {"gpt-4 (PAID)", "gpt-4-turbo (PAID)", "gpt-3.5-turbo (FREE)"};
    private final String[] GITHUB_MODELS = {"gpt-4 (PAID)", "gpt-3.5-turbo (FREE)", "claude-3-sonnet (PAID)"};
    
    public AIAssistantFrame() {
        aiService = new AIAssistantService();
        conversationHistory = new ArrayList<>();
        initComponents();
        setupEvents();
    }
    
    private void initComponents() {
        setTitle("AI Assistant - University Project Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(245, 245, 245));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        
        // Content Panel
        JPanel contentPanel = createContentPanel();
        
        // Tools Panel
        JPanel toolsPanel = createToolsPanel();
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(toolsPanel, BorderLayout.SOUTH);
        
        setContentPane(mainContainer);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setPreferredSize(new Dimension(1000, 120));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Left side - Title
        JLabel titleLabel = new JLabel("🤖 AI ASSISTANT", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        // Center - AI Settings Panel
        JPanel settingsPanel = createSettingsPanel();
        
        // Right side - Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        
        logoutButton = new JButton("🚪 Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        
        JButton backButton = new JButton("← Back to Menu");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setBackground(new Color(155, 89, 182));
        backButton.setForeground(Color.WHITE);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> goBackToMenu());
        
        buttonsPanel.add(logoutButton);
        buttonsPanel.add(backButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(settingsPanel, BorderLayout.CENTER);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        
        // First row
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel providerLabel = new JLabel("Provider:");
        providerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        providerLabel.setForeground(Color.WHITE);
        settingsPanel.add(providerLabel, gbc);
        
        gbc.gridx = 1;
        aiProviderCombo = new JComboBox<>(new String[]{"OpenAI", "GitHub Models", "Auto"});
        aiProviderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        aiProviderCombo.setPreferredSize(new Dimension(120, 25));
        aiProviderCombo.addActionListener(e -> updateModelOptions());
        settingsPanel.add(aiProviderCombo, gbc);
        
        gbc.gridx = 2;
        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modeLabel.setForeground(Color.WHITE);
        settingsPanel.add(modeLabel, gbc);
        
        gbc.gridx = 3;
        modeCombo = new JComboBox<>(new String[]{"Chat", "Research", "Code", "Academic"});
        modeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        modeCombo.setPreferredSize(new Dimension(100, 25));
        settingsPanel.add(modeCombo, gbc);
        
        // Second row
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel modelLabel = new JLabel("Model:");
        modelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelLabel.setForeground(Color.WHITE);
        settingsPanel.add(modelLabel, gbc);
        
        gbc.gridx = 1;
        modelCombo = new JComboBox<>(OPENAI_MODELS);
        modelCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        modelCombo.setPreferredSize(new Dimension(120, 25));
        settingsPanel.add(modelCombo, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 2;
        paidModeCheckbox = new JCheckBox("Paid Mode (Higher Quality)");
        paidModeCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        paidModeCheckbox.setForeground(Color.WHITE);
        paidModeCheckbox.setOpaque(false);
        paidModeCheckbox.setSelected(true);
        settingsPanel.add(paidModeCheckbox, gbc);
        
        return settingsPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Chat Area with enhanced styling
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(248, 249, 250));
        chatArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Input Panel
        JPanel inputPanel = createInputPanel();
        
        contentPanel.add(chatScrollPane, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Enhanced welcome message with real AI system info
        displayWelcomeMessage();
        
        return contentPanel;
    }
    
    private void displayWelcomeMessage() {
        appendToChatArea("🤖 AI Assistant: Welcome to your AI-powered academic assistant!\n\n", new Color(25, 135, 84));
        
        appendToChatArea("📋 Available Features:\n", new Color(52, 152, 219));
        appendToChatArea("• 💬 Interactive chat with OpenAI GPT models\n", new Color(108, 117, 125));
        appendToChatArea("• 📚 Academic research and writing assistance\n", new Color(108, 117, 125));
        appendToChatArea("• 💻 Code review and programming help\n", new Color(108, 117, 125));
        appendToChatArea("• 📄 Document analysis (text files)\n", new Color(108, 117, 125));
        appendToChatArea("• 🔍 Research mode for in-depth analysis\n", new Color(108, 117, 125));
        
        appendToChatArea("\n⚙️ Current Configuration:\n", new Color(52, 152, 219));
        String provider = (String) aiProviderCombo.getSelectedItem();
        String model = (String) modelCombo.getSelectedItem();
        String mode = (String) modeCombo.getSelectedItem();
        boolean isPaid = paidModeCheckbox.isSelected();
        
        appendToChatArea("• Provider: " + provider + "\n", new Color(108, 117, 125));
        appendToChatArea("• Model: " + model + "\n", new Color(108, 117, 125));
        appendToChatArea("• Mode: " + mode + "\n", new Color(108, 117, 125));
        appendToChatArea("• Quality: " + (isPaid ? "Premium" : "Standard") + "\n", new Color(108, 117, 125));
        
        appendToChatArea("\n💡 Tips:\n", new Color(255, 193, 7));
        appendToChatArea("• Use different modes for specialized assistance\n", new Color(108, 117, 125));
        appendToChatArea("• Academic mode provides scholarly responses\n", new Color(108, 117, 125));
        appendToChatArea("• Code mode offers programming expertise\n", new Color(108, 117, 125));
        appendToChatArea("• Research mode delivers detailed analysis\n", new Color(108, 117, 125));
        
        appendToChatArea("\n" + createSeparatorLine(50) + "\n", new Color(222, 226, 230));
        appendToChatArea("How can I assist you today? 🚀\n\n", new Color(25, 135, 84));
    }
    
    private String createSeparatorLine(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("━");
        }
        return sb.toString();
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setOpaque(false);
        
        saveHistoryButton = new JButton("💾 Save");
        saveHistoryButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveHistoryButton.setBackground(new Color(40, 167, 69));
        saveHistoryButton.setForeground(Color.WHITE);
        saveHistoryButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        saveHistoryButton.setFocusPainted(false);
        saveHistoryButton.setToolTipText("Save conversation history to HTML file");
        saveHistoryButton.addActionListener(e -> saveConversationHistory());
        
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(52, 152, 219));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());
        
        buttonsPanel.add(saveHistoryButton);
        buttonsPanel.add(sendButton);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonsPanel, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    private JPanel createToolsPanel() {
        JPanel toolsPanel = new JPanel(new BorderLayout());
        toolsPanel.setBackground(new Color(52, 73, 94));
        toolsPanel.setPreferredSize(new Dimension(0, 120));
        toolsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel toolsLabel = new JLabel("AI Tools:", SwingConstants.LEFT);
        toolsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        toolsLabel.setForeground(Color.WHITE);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        buttonsPanel.setOpaque(false);
        
        voiceButton = createToolButton("🎤 Voice", "Transcribe & Translate", new Color(46, 204, 113));
        videoButton = createToolButton("🎥 Video", "Analyze & Extract Topics", new Color(155, 89, 182));
        documentButton = createToolButton("📄 Document", "Process & Analyze", new Color(230, 126, 34));
        
        buttonsPanel.add(voiceButton);
        buttonsPanel.add(videoButton);
        buttonsPanel.add(documentButton);
        
        statusLabel = new JLabel("Ready", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 200, 200));
        
        toolsPanel.add(toolsLabel, BorderLayout.NORTH);
        toolsPanel.add(buttonsPanel, BorderLayout.CENTER);
        toolsPanel.add(statusLabel, BorderLayout.SOUTH);
        
        return toolsPanel;
    }
    
    private JButton createToolButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void setupEvents() {
        // Send button click
        sendButton.addActionListener(e -> sendMessage());
        
        // Enter key in input field
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        // Tool buttons
        voiceButton.addActionListener(e -> startVoiceTranscription());
        videoButton.addActionListener(e -> processVideo());
        documentButton.addActionListener(e -> processDocument());
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // Add user message to history
            conversationHistory.add(new ConversationHistorySaver.ConversationEntry(true, message));
            
            // Display user message with better formatting
            appendToChatArea("👤 You: " + message + "\n\n", new Color(33, 37, 41));
            inputField.setText("");
            
            // Show enhanced thinking indicator
            appendToChatArea("🤖 AI Assistant: ", new Color(52, 152, 219));
            appendToChatArea("Processing your request", new Color(108, 117, 125));
            
            // Animate thinking dots
            Timer thinkingTimer = new Timer(500, null);
            final int[] dotCount = {0};
            thinkingTimer.addActionListener(e -> {
                if (dotCount[0] < 3) {
                    appendToChatArea(".", new Color(108, 117, 125));
                    dotCount[0]++;
                } else {
                    appendToChatArea("\n", new Color(108, 117, 125));
                    thinkingTimer.stop();
                }
            });
            thinkingTimer.start();
            
            // Get current settings
            String selectedMode = (String) modeCombo.getSelectedItem();
            String provider = (String) aiProviderCombo.getSelectedItem();
            String model = (String) modelCombo.getSelectedItem();
            
            // Create enhanced prompt based on mode
            String enhancedPrompt = createEnhancedPrompt(message, selectedMode);
            
            // Send message to AI service
            CompletableFuture<AIMessage> future = aiService.sendMessage(enhancedPrompt);
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    thinkingTimer.stop();
                    // Remove thinking indicator
                    removeLastThinkingMessage();
                    
                    // Add AI response to history
                    String responseContent = response.getContent();
                    conversationHistory.add(new ConversationHistorySaver.ConversationEntry(false, responseContent));
                    
                    // Format and display AI response
                    displayAIResponse(response, selectedMode, provider, model);
                    scrollToBottom();
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    thinkingTimer.stop();
                    removeLastThinkingMessage();
                    
                    String errorMessage = "❌ I encountered an error while processing your request.\nError: " + throwable.getMessage() + "\n💡 Please try again or rephrase your question.";
                    conversationHistory.add(new ConversationHistorySaver.ConversationEntry(false, errorMessage));
                    
                    appendToChatArea("🤖 AI Assistant: ", new Color(220, 53, 69));
                    appendToChatArea("❌ I encountered an error while processing your request.\n", new Color(220, 53, 69));
                    appendToChatArea("Error: " + throwable.getMessage() + "\n\n", new Color(108, 117, 125));
                    appendToChatArea("� Please try again or rephrase your question.\n\n", new Color(255, 193, 7));
                    
                    scrollToBottom();
                });
                return null;
            });
            
            // Update status with more detail
            statusLabel.setText("Processing with " + provider + " (" + cleanModelName(model) + ") in " + selectedMode + " mode...");
        }
    }
    
    private String cleanModelName(String modelWithPricing) {
        // Remove pricing info for internal use
        return modelWithPricing.replaceAll(" \\(PAID\\)|\\(FREE\\)", "");
    }
    
    private void saveConversationHistory() {
        if (conversationHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No conversation to save yet. Start chatting with the AI assistant first!", 
                "No Conversation", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String provider = (String) aiProviderCombo.getSelectedItem();  
        String model = cleanModelName((String) modelCombo.getSelectedItem());
        String mode = (String) modeCombo.getSelectedItem();
        
        String savedFilePath = ConversationHistorySaver.saveConversationToHTML(
            conversationHistory, provider, model, mode);
        
        if (savedFilePath != null) {
            int result = JOptionPane.showConfirmDialog(this,
                "Conversation saved successfully!\n\nFile: " + savedFilePath + 
                "\n\nWould you like to open the saved file?",
                "Conversation Saved",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().open(new File(savedFilePath));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Could not open the file automatically.\nFile saved at: " + savedFilePath,
                        "File Saved",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
            
            statusLabel.setText("Conversation saved to HTML file");
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to save conversation. Please try again.",
                "Save Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String createEnhancedPrompt(String originalMessage, String mode) {
        StringBuilder prompt = new StringBuilder();
        
        switch (mode) {
            case "Academic":
                prompt.append("As an academic assistant for university students, please provide a scholarly and well-researched response to: ");
                break;
            case "Code":
                prompt.append("As a programming expert, please provide a detailed technical response with code examples when appropriate for: ");
                break;
            case "Research":
                prompt.append("As a research assistant, please provide an in-depth analytical response with multiple perspectives on: ");
                break;
            default: // Chat
                prompt.append("Please provide a helpful and conversational response to: ");
                break;
        }
        
        prompt.append(originalMessage);
        return prompt.toString();
    }
    
    private void displayAIResponse(AIMessage response, String mode, String provider, String model) {
        // Header with mode and provider info
        appendToChatArea("🤖 AI Assistant", new Color(25, 135, 84));
        appendToChatArea(" (" + provider + " • " + model + " • " + mode + " mode)", new Color(108, 117, 125));
        appendToChatArea(":\n\n", new Color(25, 135, 84));
        
        // Mode-specific icon and formatting
        String modeIcon = getModeIcon(mode);
        appendToChatArea(modeIcon + " ", new Color(52, 152, 219));
        
        // Response content with better formatting
        String responseText = response.getContent();
        if (responseText != null && !responseText.trim().isEmpty()) {
            // Format the response for better readability
            responseText = formatResponse(responseText);
            appendToChatArea(responseText, new Color(33, 37, 41));
        } else {
            appendToChatArea("I apologize, but I couldn't generate a proper response. Please try again.", new Color(220, 53, 69));
        }
        
        appendToChatArea("\n\n" + createSeparatorLine(30) + "\n\n", new Color(222, 226, 230));
        
        // Update status
        statusLabel.setText("Response received from " + provider + " (" + model + ")");
    }
    
    private String getModeIcon(String mode) {
        switch (mode) {
            case "Academic": return "📚";
            case "Code": return "💻";
            case "Research": return "🔍";
            default: return "💬";
        }
    }
    
    private String formatResponse(String response) {
        // Basic formatting for better readability
        return response
            .replaceAll("\\n\\n", "\n\n") // Preserve paragraph breaks
            .replaceAll("(?m)^([0-9]+\\.|[-*])\\s", "  $1 ") // Indent lists
            .trim();
    }
    
    private void appendToChatArea(String text, Color color) {
        chatArea.append(text);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    private void removeLastThinkingMessage() {
        String currentText = chatArea.getText();
        // Look for the thinking pattern
        int lastThinkingIndex = currentText.lastIndexOf("🤖 AI Assistant: Processing your request");
        if (lastThinkingIndex != -1) {
            // Find the end of the thinking message (next newline after dots)
            int endIndex = currentText.indexOf("\n", lastThinkingIndex);
            if (endIndex != -1) {
                String newText = currentText.substring(0, lastThinkingIndex) + 
                               currentText.substring(endIndex + 1);
                chatArea.setText(newText);
            } else {
                // If no newline found, just remove from thinking start
                chatArea.setText(currentText.substring(0, lastThinkingIndex));
            }
        }
    }
    
    private void scrollToBottom() {
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    private void updateModelOptions() {
        String selectedProvider = (String) aiProviderCombo.getSelectedItem();
        modelCombo.removeAllItems();
        
        if ("OpenAI".equals(selectedProvider)) {
            for (String model : OPENAI_MODELS) {
                modelCombo.addItem(model);
            }
        } else if ("GitHub Models".equals(selectedProvider)) {
            for (String model : GITHUB_MODELS) {
                modelCombo.addItem(model);
            }
        } else { // Auto
            modelCombo.addItem("Auto-Select");
        }
    }
    
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout and return to the login screen?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Close AI service
            if (aiService != null) {
                System.out.println("AI Service cleanup requested");
            }
            
            SwingUtilities.invokeLater(() -> {
                try {
                    // Try to close main window and return to login
                    Class<?> mainClass = Class.forName("com.raven.main.Main");
                    java.lang.reflect.Method closeMethod = mainClass.getMethod("closeMainWindow");
                    this.dispose();
                    closeMethod.invoke(null);
                    
                    // Start fresh login session
                    java.lang.reflect.Method mainMethod = mainClass.getMethod("main", String[].class);
                    mainMethod.invoke(null, (Object) new String[0]);
                } catch (Exception ex) {
                    System.out.println("Could not return to login screen via Main: " + ex.getMessage());
                    // Fallback: try to show the main frame directly
                    try {
                        com.raven.main.Main mainFrame = new com.raven.main.Main();
                        mainFrame.setVisible(true);
                        this.dispose();
                    } catch (Exception ex2) {
                        System.out.println("Could not create new Main frame: " + ex2.getMessage());
                        System.exit(0);
                    }
                }
            });
        }
    }
    
    private void startVoiceTranscription() {
        statusLabel.setText("Voice transcription starting...");
        appendToChatArea("🎤 Starting voice transcription...\n", new Color(52, 152, 219));
        appendToChatArea("Note: Voice transcription is not yet fully implemented.\n", new Color(108, 117, 125));
        appendToChatArea("Please use text input for now.\n\n", new Color(108, 117, 125));
        
        // Simulate processing
        Timer timer = new Timer(2000, e -> {
            statusLabel.setText("Voice transcription completed (demo)");
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void processVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Video Files", "mp4", "avi", "mov", "wmv", "flv", "mkv"
        ));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            appendToChatArea("🎥 Processing video: " + selectedFile.getName() + "\n", new Color(155, 89, 182));
            appendToChatArea("Note: Video analysis is not yet fully implemented.\n", new Color(108, 117, 125));
            appendToChatArea("This feature will be available in a future update.\n\n", new Color(108, 117, 125));
            
            statusLabel.setText("Video processing completed (demo)");
        }
    }
    
    private void processDocument() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Document Files", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "png", "jpg", "jpeg"
        ));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            appendToChatArea("📄 Processing document: " + selectedFile.getName() + "\n", new Color(230, 126, 34));
            
            // For text files, we can actually read and analyze them
            if (selectedFile.getName().toLowerCase().endsWith(".txt")) {
                try {
                    java.nio.file.Path path = selectedFile.toPath();
                    String content = new String(java.nio.file.Files.readAllBytes(path));
                    
                    // Send the document content to AI for analysis
                    String prompt = "Please analyze this document and provide a summary:\n\n" + content;
                    CompletableFuture<AIMessage> future = aiService.sendMessage(prompt);
                    
                    appendToChatArea("🤖 AI Assistant: Analyzing document...\n\n", new Color(52, 152, 219));
                    
                    future.thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            appendToChatArea("📊 Document Analysis Results:\n" + response.getContent() + "\n\n", 
                                           new Color(25, 135, 84));
                            statusLabel.setText("Document analysis completed");
                            scrollToBottom();
                        });
                    }).exceptionally(throwable -> {
                        SwingUtilities.invokeLater(() -> {
                            appendToChatArea("❌ Error analyzing document: " + throwable.getMessage() + "\n\n", 
                                           new Color(220, 53, 69));
                            statusLabel.setText("Document analysis failed");
                            scrollToBottom();
                        });
                        return null;
                    });
                    
                } catch (Exception e) {
                    appendToChatArea("❌ Error reading file: " + e.getMessage() + "\n\n", new Color(220, 53, 69));
                    statusLabel.setText("Document processing failed");
                }
            } else {
                appendToChatArea("Note: Full document analysis for this file type is not yet implemented.\n", 
                               new Color(108, 117, 125));
                appendToChatArea("Currently only .txt files are supported for analysis.\n\n", 
                               new Color(108, 117, 125));
                statusLabel.setText("Document processing completed (limited)");
            }
        }
    }
    
    private void goBackToMenu() {
        // Clean up resources if needed
        if (aiService != null) {
            System.out.println("AI Service cleanup requested");
        }
        
        SwingUtilities.invokeLater(() -> {
            MainSelectionFrame mainMenu = new MainSelectionFrame();
            mainMenu.setVisible(true);
            this.dispose();
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AIAssistantFrame frame = new AIAssistantFrame();
            frame.setVisible(true);
        });
    }
} 