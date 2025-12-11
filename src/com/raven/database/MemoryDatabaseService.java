package com.raven.database;

import com.raven.model.User;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple In-Memory Database Service (Fallback)
 * This is used when SQLite has compatibility issues
 */
public class MemoryDatabaseService {
    private static MemoryDatabaseService instance;
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Set<String> whitelistedEmails = new HashSet<>();
    private int nextUserId = 1;
    
    // Whitelist configuration
    private static final String[] DEFAULT_WHITELIST = {
        "diacostamo@unal.edu.co",
        "titanioco@hotmail.com", 
        "goanetapp@gmail.com"
    };
    
    private MemoryDatabaseService() {
        initialize();
    }
    
    public static synchronized MemoryDatabaseService getInstance() {
        if (instance == null) {
            instance = new MemoryDatabaseService();
        }
        return instance;
    }
    
    private void initialize() {
        System.out.println("MEMORY_DB: Initializing In-Memory Database Service...");
        
        // Initialize whitelist
        for (String email : DEFAULT_WHITELIST) {
            whitelistedEmails.add(email.toLowerCase());
        }
        
        System.out.println("SUCCESS: Memory Database Service initialized successfully");
        System.out.println("MEMORY_DB: Whitelisted emails: " + whitelistedEmails.size());
        System.out.println("MEMORY_DB: Note - Data will be lost when application closes");
    }
    
    /**
     * Register a new user with comprehensive validation
     */
    public boolean registerUser(String email, String password, String fullName) {
        try {
            email = email.toLowerCase().trim();
            
            System.out.println("MEMORY_DB: Attempting to register user: " + email);
            
            // Validate email format
            if (!isValidEmail(email)) {
                showError("Invalid email format");
                return false;
            }
            
            // Check whitelist
            if (!isEmailWhitelisted(email)) {
                showError("Access Restricted\n\nYour email is not in the authorized user list.\n" +
                         "This system is currently for personal use only.\n\n" +
                         "Authorized emails:\n" + String.join("\n", DEFAULT_WHITELIST) +
                         "\n\nContact the administrator for access.");
                return false;
            }
            
            // Check if user already exists
            if (users.containsKey(email)) {
                showError("User Already Exists\n\nAn account with this email already exists.\n" +
                         "Please use the login option instead.");
                return false;
            }
            
            // Create new user
            String hashedPassword = hashPassword(password);
            String userType = determineUserType(email);
            
            User newUser = new User(email, fullName);
            newUser.setId(nextUserId++);
            newUser.setPasswordHash(hashedPassword);
            newUser.setUserType(User.UserType.valueOf(userType));
            newUser.setCreatedAt(new Date());
            
            users.put(email, newUser);
            
            System.out.println("SUCCESS: User registered successfully: " + email);
            
            JOptionPane.showMessageDialog(null, 
                "Registration Successful!\n\n" +
                "Welcome to the University System.\n" +
                "You can now login with your credentials.\n\n" +
                "Note: Using in-memory storage - data will be lost on restart.",
                "Welcome!", 
                JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to register user: " + e.getMessage());
            e.printStackTrace();
            showError("Registration Error\n\nAn error occurred.\nPlease try again.");
        }
        
        return false;
    }
    
    /**
     * Authenticate user login
     */
    public User authenticateUser(String email, String password) {
        try {
            email = email.toLowerCase().trim();
            
            System.out.println("MEMORY_DB: Authenticating user: " + email);
            
            User user = users.get(email);
            
            if (user != null && verifyPassword(password, user.getPasswordHash())) {
                user.setLastLogin(new Date());
                System.out.println("SUCCESS: User authenticated: " + email);
                return user;
            }
            
            System.out.println("ERROR: Authentication failed for: " + email);
            return null;
            
        } catch (Exception e) {
            System.err.println("ERROR: Authentication error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Authenticate or create user from Google OAuth
     */
    public User authenticateOrCreateGoogleUser(String email, String googleSub, String fullName) {
        try {
            email = email.toLowerCase().trim();
            
            System.out.println("MEMORY_DB: Processing Google user: " + email);
            
            // Check whitelist
            if (!isEmailWhitelisted(email)) {
                System.out.println("ERROR: Google email not whitelisted: " + email);
                showError("Access Restricted\n\nYour Google account (" + email + ") is not authorized.\n" +
                         "This system is currently for personal use only.\n\n" +
                         "Authorized emails:\n" + String.join("\n", DEFAULT_WHITELIST) +
                         "\n\nContact the administrator for access.");
                return null;
            }
            
            // Check if user already exists
            User user = users.get(email);
            
            if (user != null) {
                // Update Google sub if needed
                if (user.getGoogleSub() == null || !user.getGoogleSub().equals(googleSub)) {
                    user.setGoogleSub(googleSub);
                }
                
                user.setLastLogin(new Date());
                System.out.println("SUCCESS: Existing Google user authenticated: " + email);
                return user;
            }
            
            // Create new user from Google account
            String userType = determineUserType(email);
            
            user = new User(email, fullName);
            user.setId(nextUserId++);
            user.setGoogleSub(googleSub);
            user.setUserType(User.UserType.valueOf(userType));
            user.setCreatedAt(new Date());
            
            users.put(email, user);
            
            System.out.println("SUCCESS: New Google user created: " + email);
            return user;
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to process Google user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Whitelist management methods
     */
    public boolean isEmailWhitelisted(String email) {
        return whitelistedEmails.contains(email.toLowerCase());
    }
    
    public boolean addToWhitelist(String email, User adminUser) {
        if (!isUserAdmin(adminUser)) {
            System.out.println("ERROR: User is not admin: " + adminUser.getEmail());
            return false;
        }
        
        email = email.toLowerCase().trim();
        whitelistedEmails.add(email);
        System.out.println("SUCCESS: Added to whitelist: " + email);
        return true;
    }
    
    public boolean removeFromWhitelist(String email, User adminUser) {
        if (!isUserAdmin(adminUser)) {
            System.out.println("ERROR: User is not admin: " + adminUser.getEmail());
            return false;
        }
        
        email = email.toLowerCase().trim();
        whitelistedEmails.remove(email);
        System.out.println("SUCCESS: Removed from whitelist: " + email);
        return true;
    }
    
    public Set<String> getWhitelistedEmails(User adminUser) {
        if (!isUserAdmin(adminUser)) {
            return new HashSet<>();
        }
        return new HashSet<>(whitelistedEmails);
    }
    
    // Helper methods
    private String determineUserType(String email) {
        if (email.equals("diacostamo@unal.edu.co") || email.equals("titanioco@hotmail.com")) {
            return "ADMIN";
        }
        return "FREE";
    }
    
    private boolean isUserAdmin(User user) {
        return user != null && user.getUserType() == User.UserType.ADMIN;
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    private boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Authentication Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Get database statistics
     */
    public Map<String, Object> getSystemStats(User adminUser) {
        if (!isUserAdmin(adminUser)) {
            return new HashMap<>();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("whitelistedEmails", whitelistedEmails.size());
        stats.put("databaseLocation", "In-Memory (temporary)");
        stats.put("cacheSize", users.size());
        
        return stats;
    }
    
    /**
     * Get all users (for testing)
     */
    public Collection<User> getAllUsers() {
        return users.values();
    }
    
    /**
     * No cleanup needed for memory database
     */
    public void close() {
        System.out.println("MEMORY_DB: Closing memory database (no action needed)");
    }
}
