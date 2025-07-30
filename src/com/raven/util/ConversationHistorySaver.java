package com.raven.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utility class for saving AI conversation history to HTML files
 */
public class ConversationHistorySaver {
    
    private static final String EXPORT_DIRECTORY = "exported/conversations/";
    
    static {
        // Create export directory if it doesn't exist
        new File(EXPORT_DIRECTORY).mkdirs();
    }
    
    /**
     * Save conversation history to an HTML file
     */
    public static String saveConversationToHTML(List<ConversationEntry> conversation, 
                                               String provider, String model, String mode) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("AI_Conversation_%s_%s.html", mode, timestamp);
            String filepath = EXPORT_DIRECTORY + filename;
            
            FileWriter writer = new FileWriter(filepath);
            writer.write(generateHTMLContent(conversation, provider, model, mode, timestamp));
            writer.close();
            
            System.out.println("💾 Conversation saved to: " + filepath);
            return filepath;
            
        } catch (IOException e) {
            System.err.println("❌ Error saving conversation: " + e.getMessage());
            return null;
        }
    }
    
    private static String generateHTMLContent(List<ConversationEntry> conversation, 
                                            String provider, String model, String mode, String timestamp) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>AI Conversation History - ").append(mode).append(" Mode</title>\n")
            .append("    <style>\n")
            .append("        body {\n")
            .append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n")
            .append("            max-width: 1200px;\n")
            .append("            margin: 0 auto;\n")
            .append("            padding: 20px;\n")
            .append("            background-color: #f8f9fa;\n")
            .append("            line-height: 1.6;\n")
            .append("        }\n")
            .append("        .header {\n")
            .append("            background: linear-gradient(135deg, #3498db, #2c3e50);\n")
            .append("            color: white;\n")
            .append("            padding: 30px;\n")
            .append("            border-radius: 12px;\n")
            .append("            margin-bottom: 30px;\n")
            .append("            box-shadow: 0 4px 6px rgba(0,0,0,0.1);\n")
            .append("        }\n")
            .append("        .header h1 {\n")
            .append("            margin: 0 0 10px 0;\n")
            .append("            font-size: 2.5em;\n")
            .append("        }\n")
            .append("        .meta-info {\n")
            .append("            display: grid;\n")
            .append("            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n")
            .append("            gap: 15px;\n")
            .append("            margin-top: 20px;\n")
            .append("        }\n")
            .append("        .meta-item {\n")
            .append("            background: rgba(255,255,255,0.1);\n")
            .append("            padding: 10px 15px;\n")
            .append("            border-radius: 8px;\n")
            .append("            backdrop-filter: blur(10px);\n")
            .append("        }\n")
            .append("        .conversation {\n")
            .append("            background: white;\n")
            .append("            border-radius: 12px;\n")
            .append("            padding: 30px;\n")
            .append("            box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n")
            .append("        }\n")
            .append("        .message {\n")
            .append("            margin-bottom: 25px;\n")
            .append("            padding: 20px;\n")
            .append("            border-radius: 12px;\n")
            .append("            position: relative;\n")
            .append("        }\n")
            .append("        .user-message {\n")
            .append("            background: linear-gradient(135deg, #e3f2fd, #bbdefb);\n")
            .append("            border-left: 4px solid #2196f3;\n")
            .append("            margin-left: 0;\n")
            .append("            margin-right: 50px;\n")
            .append("        }\n")
            .append("        .ai-message {\n")
            .append("            background: linear-gradient(135deg, #f1f8e9, #dcedc8);\n")
            .append("            border-left: 4px solid #4caf50;\n")
            .append("            margin-left: 50px;\n")
            .append("            margin-right: 0;\n")
            .append("        }\n")
            .append("        .message-header {\n")
            .append("            font-weight: bold;\n")
            .append("            margin-bottom: 10px;\n")
            .append("            display: flex;\n")
            .append("            align-items: center;\n")
            .append("            gap: 10px;\n")
            .append("        }\n")
            .append("        .message-content {\n")
            .append("            white-space: pre-wrap;\n")
            .append("            word-wrap: break-word;\n")
            .append("            font-size: 1.1em;\n")
            .append("        }\n")
            .append("        .timestamp {\n")
            .append("            font-size: 0.85em;\n")
            .append("            color: #666;\n")
            .append("            margin-left: auto;\n")
            .append("        }\n")
            .append("        .mode-badge {\n")
            .append("            background: #ff9800;\n")
            .append("            color: white;\n")
            .append("            padding: 4px 12px;\n")
            .append("            border-radius: 15px;\n")
            .append("            font-size: 0.8em;\n")
            .append("            font-weight: bold;\n")
            .append("        }\n")
            .append("        .provider-info {\n")
            .append("            background: #9c27b0;\n")
            .append("            color: white;\n")
            .append("            padding: 4px 12px;\n")
            .append("            border-radius: 15px;\n")
            .append("            font-size: 0.8em;\n")
            .append("        }\n")
            .append("        .paid-model {\n")
            .append("            background: #d4af37;\n")
            .append("            color: white;\n")
            .append("            padding: 2px 8px;\n")
            .append("            border-radius: 10px;\n")
            .append("            font-size: 0.7em;\n")
            .append("            margin-left: 5px;\n")
            .append("        }\n")
            .append("        .free-model {\n")
            .append("            background: #28a745;\n")
            .append("            color: white;\n")
            .append("            padding: 2px 8px;\n")
            .append("            border-radius: 10px;\n")
            .append("            font-size: 0.7em;\n")
            .append("            margin-left: 5px;\n")
            .append("        }\n")
            .append("        .stats {\n")
            .append("            background: #f5f5f5;\n")
            .append("            padding: 20px;\n")
            .append("            border-radius: 8px;\n")
            .append("            margin-top: 30px;\n")
            .append("            display: grid;\n")
            .append("            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));\n")
            .append("            gap: 15px;\n")
            .append("        }\n")
            .append("        .stat-item {\n")
            .append("            text-align: center;\n")
            .append("            padding: 10px;\n")
            .append("            background: white;\n")
            .append("            border-radius: 8px;\n")
            .append("            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n")
            .append("        }\n")
            .append("        .stat-value {\n")
            .append("            font-size: 1.5em;\n")
            .append("            font-weight: bold;\n")
            .append("            color: #3498db;\n")
            .append("        }\n")
            .append("        .stat-label {\n")
            .append("            font-size: 0.9em;\n")
            .append("            color: #666;\n")
            .append("            margin-top: 5px;\n")
            .append("        }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <div class=\"header\">\n")
            .append("        <h1>🤖 AI Conversation History</h1>\n")
            .append("        <div class=\"meta-info\">\n")
            .append("            <div class=\"meta-item\">\n")
            .append("                <strong>📅 Date:</strong> ")
            .append(new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss").format(new Date()))
            .append("\n            </div>\n")
            .append("            <div class=\"meta-item\">\n")
            .append("                <strong>🔧 Provider:</strong> ").append(provider).append("\n")
            .append("            </div>\n")
            .append("            <div class=\"meta-item\">\n")
            .append("                <strong>🧠 Model:</strong> ").append(model)
            .append(isPaidModel(model) ? " <span class=\"paid-model\">PAID</span>" : " <span class=\"free-model\">FREE</span>")
            .append("\n            </div>\n")
            .append("            <div class=\"meta-item\">\n")
            .append("                <strong>🎯 Mode:</strong> ").append(mode).append("\n")
            .append("            </div>\n")
            .append("        </div>\n")
            .append("    </div>\n")
            .append("    \n")
            .append("    <div class=\"conversation\">\n");
        
        // Add conversation messages
        for (int i = 0; i < conversation.size(); i++) {
            ConversationEntry entry = conversation.get(i);
            String messageClass = entry.isUser ? "user-message" : "ai-message";
            String icon = entry.isUser ? "👤" : "🤖";
            String sender = entry.isUser ? "You" : "AI Assistant";
            
            html.append("        <div class=\"message ").append(messageClass).append("\">\n")
                .append("            <div class=\"message-header\">\n")
                .append("                <span>").append(icon).append(" ").append(sender).append("</span>\n");
            
            if (!entry.isUser) {
                html.append("                <span class=\"mode-badge\">").append(mode).append("</span>\n")
                    .append("                <span class=\"provider-info\">").append(provider).append("</span>\n");
            }
            
            html.append("                <span class=\"timestamp\">").append(entry.timestamp).append("</span>\n")
                .append("            </div>\n")
                .append("            <div class=\"message-content\">").append(escapeHtml(entry.content)).append("</div>\n")
                .append("        </div>\n");
        }
        
        // Add statistics
        int userMessages = (int) conversation.stream().filter(e -> e.isUser).count();
        int aiMessages = conversation.size() - userMessages;
        int totalWords = conversation.stream().mapToInt(e -> e.content.split("\\s+").length).sum();
        
        html.append("    </div>\n")
            .append("    \n")
            .append("    <div class=\"stats\">\n")
            .append("        <div class=\"stat-item\">\n")
            .append("            <div class=\"stat-value\">").append(conversation.size()).append("</div>\n")
            .append("            <div class=\"stat-label\">Total Messages</div>\n")
            .append("        </div>\n")
            .append("        <div class=\"stat-item\">\n")
            .append("            <div class=\"stat-value\">").append(userMessages).append("</div>\n")
            .append("            <div class=\"stat-label\">Your Messages</div>\n")
            .append("        </div>\n")
            .append("        <div class=\"stat-item\">\n")
            .append("            <div class=\"stat-value\">").append(aiMessages).append("</div>\n")
            .append("            <div class=\"stat-label\">AI Responses</div>\n")
            .append("        </div>\n")
            .append("        <div class=\"stat-item\">\n")
            .append("            <div class=\"stat-value\">").append(totalWords).append("</div>\n")
            .append("            <div class=\"stat-label\">Total Words</div>\n")
            .append("        </div>\n")
            .append("    </div>\n")
            .append("    \n")
            .append("    <div style=\"text-align: center; margin-top: 30px; color: #666; font-size: 0.9em;\">\n")
            .append("        Generated by AI Assistant - University Project Manager<br>\n")
            .append("        Saved on ").append(new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss").format(new Date())).append("\n")
            .append("    </div>\n")
            .append("</body>\n")
            .append("</html>");
        
        return html.toString();
    }
    
    private static boolean isPaidModel(String model) {
        // Define which models are paid vs free
        String[] paidModels = {"gpt-4", "gpt-4-turbo", "gpt-4o", "claude-3-opus", "claude-3-sonnet"};
        
        String modelLower = model.toLowerCase();
        for (String paidModel : paidModels) {
            if (modelLower.contains(paidModel.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    /**
     * Represents a single conversation entry
     */
    public static class ConversationEntry {
        public final boolean isUser;
        public final String content;
        public final String timestamp;
        
        public ConversationEntry(boolean isUser, String content) {
            this.isUser = isUser;
            this.content = content;
            this.timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        }
    }
}
