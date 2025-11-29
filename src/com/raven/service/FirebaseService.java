package com.raven.service;

import com.raven.config.FirebaseConfig;
import com.raven.model.User;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Firebase Database Service
 * Manages user data persistence with Firebase integration
 * Includes whitelist management and user type control
 */
public class FirebaseService {
    private static FirebaseService instance;
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    private final Set<String> whitelistedEmails = new HashSet<>();
    private int currentUserCount = 0;
    
    private FirebaseService() {
        initialize();
    }
    
    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }
    
    private void initialize() {
        System.out.println("FIREBASE: Initializing Firebase Service...");
        
        // Load whitelist
        whitelistedEmails.addAll(FirebaseConfig.getWhitelistedEmails());
        
        // Initialize Firebase SDK (simulated for now - in production use Firebase Admin SDK)
        initializeFirebaseConnection();
        
        System.out.println("SUCCESS: Firebase Service initialized with " + whitelistedEmails.size() + " whitelisted emails");
    }
    
    private void initializeFirebaseConnection() {
        // TODO: Initialize Firebase Admin SDK
        // For now, we'll use a local storage simulation
        System.out.println("FIREBASE: Connecting to project: " + FirebaseConfig.getProjectId());
        System.out.println("FIREBASE: Database URL: " + FirebaseConfig.getDatabaseUrl());
        
        // In production, this would be:
        // FirebaseApp.initializeApp(options);
        // FirebaseDatabase database = FirebaseDatabase.getInstance();
    }
    
    /**
     * Register a new user with whitelist validation
     */
    public boolean registerUser(String email, String password, String fullName) {
        try {
            System.out.println("FIREBASE: Attempting to register user: " + email);
            
            // Check whitelist
            if (FirebaseConfig.isWhitelistEnabled() && !isEmailWhitelisted(email)) {
                System.out.println("ERROR: Email not in whitelist: " + email);
                SwingUtilities.invokeLater(() -> {
                    // Find and maximize the main selection frame if it exists
                    java.awt.Component parentComponent = null;
                    for (java.awt.Window window : java.awt.Window.getWindows()) {
                        if (window instanceof com.raven.ui.MainSelectionFrame && window.isDisplayable()) {
                            parentComponent = (java.awt.Component) window;
                            ((com.raven.ui.MainSelectionFrame) window).bringToFrontAndMaximize();
                            break;
                        }
                    }
                    
                    JOptionPane.showMessageDialog(parentComponent, 
                        "Access Restricted\n\n" +
                        "Your email is not in the authorized user list.\n" +
                        "This system is currently for personal use only.\n\n" +
                        "Contact the administrator for access.",
                        "Registration Failed", 
                        JOptionPane.ERROR_MESSAGE);
                });
                return false;
            }
            
            // Check user count limit
            if (currentUserCount >= FirebaseConfig.getMaxUserCount()) {
                System.out.println("ERROR: Maximum user count reached");
                SwingUtilities.invokeLater(() -> {
                    // Find and maximize the main selection frame if it exists
                    java.awt.Component parentComponent = null;
                    for (java.awt.Window window : java.awt.Window.getWindows()) {
                        if (window instanceof com.raven.ui.MainSelectionFrame && window.isDisplayable()) {
                            parentComponent = (java.awt.Component) window;
                            ((com.raven.ui.MainSelectionFrame) window).bringToFrontAndMaximize();
                            break;
                        }
                    }
                    
                    JOptionPane.showMessageDialog(parentComponent, 
                        "Registration Limit Reached\n\n" +
                        "The system has reached its maximum user capacity.\n" +
                        "Please contact the administrator.",
                        "Registration Failed", 
                        JOptionPane.ERROR_MESSAGE);
                });
                return false;
            }
            
            // Check if user already exists
            if (userExists(email)) {
                System.out.println("ERROR: User already exists: " + email);
                SwingUtilities.invokeLater(() -> {
                    // Find and maximize the main selection frame if it exists
                    java.awt.Component parentComponent = null;
                    for (java.awt.Window window : java.awt.Window.getWindows()) {
                        if (window instanceof com.raven.ui.MainSelectionFrame && window.isDisplayable()) {
                            parentComponent = (java.awt.Component) window;
                            ((com.raven.ui.MainSelectionFrame) window).bringToFrontAndMaximize();
                            // Ensure window is focused and on top
                            window.toFront();
                            window.requestFocus();
                            break;
                        }
                    }
                    
                    // Make final for lambda usage
                    final java.awt.Component finalParentComponent = parentComponent;
                    
                    // Since we're already on the EDT via invokeLater, just show the dialog directly
                    JOptionPane.showMessageDialog(finalParentComponent, 
                        "User Already Exists\n\n" +
                        "An account with this email already exists.\n" +
                        "Please use the login option instead.\n\n" +
                        "Click OK to continue.",
                        "Registration Failed", 
                        JOptionPane.WARNING_MESSAGE);
                });
                return false;
            }
            
            // Create new user
            User newUser = new User(email, fullName);
            newUser.setPasswordHash(hashPassword(password));
            newUser.setUserType(determineUserType(email));
            newUser.setCreatedAt(new java.util.Date());
            
            // Save to Firebase (simulated)
            saveUserToFirebase(newUser);
            
            // Cache user
            userCache.put(email, newUser);
            currentUserCount++;
            
            System.out.println("SUCCESS: User registered successfully: " + email);
            
            // Bring main window to front before showing success message
            SwingUtilities.invokeLater(() -> {
                // Find and maximize the main selection frame if it exists
                java.awt.Component parentComponent = null;
                for (java.awt.Window window : java.awt.Window.getWindows()) {
                    if (window instanceof com.raven.ui.MainSelectionFrame && window.isDisplayable()) {
                        parentComponent = (java.awt.Component) window;
                        ((com.raven.ui.MainSelectionFrame) window).bringToFrontAndMaximize();
                        break;
                    }
                }
                
                JOptionPane.showMessageDialog(parentComponent, 
                    "Registration Successful!\n\n" +
                    "Welcome to the InvestSmart University System.\n" +
                    "You can now login with your credentials.",
                    "Welcome!", 
                    JOptionPane.INFORMATION_MESSAGE);
            });
            
            return true;
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to register user: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                // Find and maximize the main selection frame if it exists
                java.awt.Component parentComponent = null;
                for (java.awt.Window window : java.awt.Window.getWindows()) {
                    if (window instanceof com.raven.ui.MainSelectionFrame && window.isDisplayable()) {
                        parentComponent = (java.awt.Component) window;
                        ((com.raven.ui.MainSelectionFrame) window).bringToFrontAndMaximize();
                        break;
                    }
                }
                
                JOptionPane.showMessageDialog(parentComponent, 
                    "Registration Error\n\n" +
                    "An error occurred during registration.\n" +
                    "Please try again or contact support.\n\n" +
                    "Error: " + e.getMessage(),
                    "Registration Failed", 
                    JOptionPane.ERROR_MESSAGE);
            });
            return false;
        }
    }
    
    /**
     * Authenticate user login
     */
    public User authenticateUser(String email, String password) {
        try {
            System.out.println("FIREBASE: Authenticating user: " + email);
            
            // Check cache first
            User user = userCache.get(email);
            if (user == null) {
                // Load from Firebase
                user = loadUserFromFirebase(email);
                if (user != null) {
                    userCache.put(email, user);
                }
            }
            
            if (user != null && verifyPassword(password, user.getPasswordHash())) {
                System.out.println("SUCCESS: User authenticated: " + email);
                user.setLastLogin(new java.util.Date());
                updateUserInFirebase(user);
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
     * Check if email is whitelisted
     */
    public boolean isEmailWhitelisted(String email) {
        return whitelistedEmails.contains(email.toLowerCase());
    }
    
    /**
     * Add email to whitelist (admin only)
     */
    public boolean addToWhitelist(String email, User adminUser) {
        if (!isUserAdmin(adminUser)) {
            System.out.println("ERROR: User is not admin: " + adminUser.getEmail());
            return false;
        }
        
        whitelistedEmails.add(email.toLowerCase());
        saveWhitelistToFirebase();
        System.out.println("SUCCESS: Added to whitelist: " + email);
        return true;
    }
    
    /**
     * Remove email from whitelist (admin only)
     */
    public boolean removeFromWhitelist(String email, User adminUser) {
        if (!isUserAdmin(adminUser)) {
            System.out.println("ERROR: User is not admin: " + adminUser.getEmail());
            return false;
        }
        
        whitelistedEmails.remove(email.toLowerCase());
        saveWhitelistToFirebase();
        System.out.println("SUCCESS: Removed from whitelist: " + email);
        return true;
    }
    
    /**
     * Get all whitelisted emails (admin only)
     */
    public Set<String> getWhitelistedEmails(User adminUser) {
        if (!isUserAdmin(adminUser)) {
            return new HashSet<>();
        }
        return new HashSet<>(whitelistedEmails);
    }
    
    /**
     * Determine user type based on email
     */
    private User.UserType determineUserType(String email) {
        // Admin emails get admin privileges
        if (email.equals("diacostamo@unal.edu.co") || email.equals("titanioco@hotmail.com")) {
            return User.UserType.ADMIN;
        }
        // Default to free user
        return User.UserType.FREE;
    }
    
    /**
     * Check if user exists
     */
    public boolean userExists(String email) {
        return userCache.containsKey(email) || loadUserFromFirebase(email) != null;
    }
    
    /**
     * Get current user count
     */
    public int getCurrentUserCount() {
        return currentUserCount;
    }
    
    /**
     * Get maximum user count
     */
    public int getMaxUserCount() {
        return FirebaseConfig.getMaxUserCount();
    }
    
    private boolean isUserAdmin(User user) {
        return user != null && user.getUserType() == User.UserType.ADMIN;
    }
    
    private String hashPassword(String password) {
        // Simple hash for demo - in production use BCrypt
        return "hash_" + password.hashCode();
    }
    
    private boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
    
    private void saveUserToFirebase(User user) {
        // TODO: Implement Firebase save
        System.out.println("FIREBASE: Saving user to database: " + user.getEmail());
        // firebase.child("users").child(user.getId()).setValue(user);
    }
    
    private User loadUserFromFirebase(String email) {
        // TODO: Implement Firebase load
        System.out.println("FIREBASE: Loading user from database: " + email);
        // return firebase.child("users").orderByChild("email").equalTo(email).getValue();
        return null; // Simulated - not found
    }
    
    private void updateUserInFirebase(User user) {
        // TODO: Implement Firebase update
        System.out.println("FIREBASE: Updating user in database: " + user.getEmail());
    }
    
    private void saveWhitelistToFirebase() {
        // TODO: Implement Firebase whitelist save
        System.out.println("FIREBASE: Saving whitelist to database");
    }
    
    /**
     * Get system statistics (admin only)
     */
    public Map<String, Object> getSystemStats(User adminUser) {
        if (!isUserAdmin(adminUser)) {
            return new HashMap<>();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", currentUserCount);
        stats.put("maxUsers", FirebaseConfig.getMaxUserCount());
        stats.put("whitelistedEmails", whitelistedEmails.size());
        stats.put("registrationMode", FirebaseConfig.getRegistrationMode());
        stats.put("whitelistEnabled", FirebaseConfig.isWhitelistEnabled());
        
        return stats;
    }
}
