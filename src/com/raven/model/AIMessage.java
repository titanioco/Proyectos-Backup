package com.raven.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI Chat Message Model
 * Represents a single message in an AI conversation
 * 
 * @author RAVEN
 * @version 1.0
 */
public class AIMessage {
    
    public enum Role {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system");
        
        private final String value;
        
        Role(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum Provider {
        OPENAI("OpenAI"),
        GITHUB("GitHub"),
        FALLBACK("Fallback");
        
        private final String displayName;
        
        Provider(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private String id;
    private Role role;
    private String content;
    private LocalDateTime timestamp;
    private Provider provider;
    private String model;
    private int tokenCount;
    private long responseTimeMs;
    private boolean isError;
    private String errorMessage;
    
    /**
     * Constructor for user messages
     */
    public AIMessage(Role role, String content) {
        this.id = generateId();
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isError = false;
    }
    
    /**
     * Constructor for assistant responses
     */
    public AIMessage(Role role, String content, Provider provider, String model) {
        this(role, content);
        this.provider = provider;
        this.model = model;
    }
    
    /**
     * Constructor for error messages
     */
    public static AIMessage createError(String errorMessage) {
        AIMessage message = new AIMessage(Role.ASSISTANT, "I'm sorry, but I encountered an error while processing your request.");
        message.isError = true;
        message.errorMessage = errorMessage;
        return message;
    }
    
    /**
     * Generate unique ID for message
     */
    private String generateId() {
        return "msg_" + System.currentTimeMillis() + "_" + hashCode();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Provider getProvider() {
        return provider;
    }
    
    public void setProvider(Provider provider) {
        this.provider = provider;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public int getTokenCount() {
        return tokenCount;
    }
    
    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }
    
    public long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public boolean isError() {
        return isError;
    }
    
    public void setError(boolean error) {
        isError = error;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * Get formatted timestamp string
     */
    public String getFormattedTimestamp() {
        return timestamp.toString().replace("T", " ").substring(0, 19);
    }
    
    /**
     * Get display name for the message sender
     */
    public String getSenderDisplayName() {
        switch (role) {
            case USER:
                return "You";
            case ASSISTANT:
                return provider != null ? provider.getDisplayName() + " Assistant" : "AI Assistant";
            case SYSTEM:
                return "System";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Check if this is a user message
     */
    public boolean isUserMessage() {
        return role == Role.USER;
    }
    
    /**
     * Check if this is an assistant message
     */
    public boolean isAssistantMessage() {
        return role == Role.ASSISTANT;
    }
    
    /**
     * Get a preview of the content (first 100 characters)
     */
    public String getContentPreview() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        if (content.length() <= 100) {
            return content;
        }
        
        return content.substring(0, 97) + "...";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIMessage message = (AIMessage) o;
        return Objects.equals(id, message.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "AIMessage{" +
                "id='" + id + '\'' +
                ", role=" + role +
                ", content='" + getContentPreview() + '\'' +
                ", timestamp=" + timestamp +
                ", provider=" + provider +
                ", model='" + model + '\'' +
                ", isError=" + isError +
                '}';
    }
}
