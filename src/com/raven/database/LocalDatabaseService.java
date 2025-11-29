package com.raven.database;

import com.raven.model.User;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;

/**
 * Local SQLite Database Service - Primary Database Solution
 * Firebase is used as backup/sync only
 */
public class LocalDatabaseService {
    private static LocalDatabaseService instance;
    private static final String DB_URL = "jdbc:sqlite:university.db";
    private Connection connection;
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    private final Set<String> whitelistedEmails = new HashSet<>();
    
    // Whitelist configuration - easily modifiable
    private static final String[] DEFAULT_WHITELIST = {
        "diacostamo@unal.edu.co",
        "titanioco@hotmail.com", 
        "goanetapp@gmail.com"
    };
    
    private LocalDatabaseService() {
        initialize();
    }
    
    public static synchronized LocalDatabaseService getInstance() {
        if (instance == null) {
            instance = new LocalDatabaseService();
        }
        return instance;
    }
    
    private void initialize() {
        System.out.println("LOCAL_DB: Initializing Local Database Service...");
        
        try {
            // Initialize SQLite connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            
            // Create tables if they don't exist
            createTables();
            
            // Initialize whitelist
            initializeWhitelist();
            
            // Load user cache
            loadUserCache();
            
            System.out.println("SUCCESS: Local Database Service initialized successfully");
            System.out.println("LOCAL_DB: Database location: " + new java.io.File("university.db").getAbsolutePath());
            System.out.println("LOCAL_DB: Whitelisted emails: " + whitelistedEmails.size());
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize local database: " + e.getMessage());
            
            // Throw a runtime exception to trigger fallback
            throw new RuntimeException("SQLite database initialization failed: " + e.getMessage(), e);
        }
    }
    
    private void createTables() throws SQLException {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT, " +
                "google_sub TEXT, " +
                "full_name TEXT NOT NULL, " +
                "user_type TEXT DEFAULT 'FREE', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "last_login DATETIME, " +
                "is_active BOOLEAN DEFAULT 1" +
                ")";
        
        String createWhitelistTable = "CREATE TABLE IF NOT EXISTS whitelist (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "added_by TEXT, " +
                "added_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT 1" +
                ")";
        
        String createSessionsTable = "CREATE TABLE IF NOT EXISTS user_sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "session_token TEXT UNIQUE, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "expires_at DATETIME, " +
                "is_active BOOLEAN DEFAULT 1, " +
                "FOREIGN KEY (user_id) REFERENCES users (id)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createWhitelistTable);
            stmt.execute(createSessionsTable);
            System.out.println("LOCAL_DB: Database tables created/verified successfully");
        }
    }
    
    private void initializeWhitelist() {
        try {
            // Add default whitelist emails if they don't exist
            for (String email : DEFAULT_WHITELIST) {
                addToWhitelistInternal(email.toLowerCase(), "SYSTEM");
                whitelistedEmails.add(email.toLowerCase());
            }
            
            // Load additional whitelist from database
            String query = "SELECT email FROM whitelist WHERE is_active = 1";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    whitelistedEmails.add(rs.getString("email").toLowerCase());
                }
            }
            
            System.out.println("LOCAL_DB: Whitelist initialized with " + whitelistedEmails.size() + " emails");
            
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to initialize whitelist: " + e.getMessage());
        }
    }
    
    private void loadUserCache() {
        try {
            String query = "SELECT * FROM users WHERE is_active = 1";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    userCache.put(user.getEmail().toLowerCase(), user);
                }
            }
            
            System.out.println("LOCAL_DB: Loaded " + userCache.size() + " users into cache");
            
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to load user cache: " + e.getMessage());
        }
    }
    
    /**
     * Register a new user with comprehensive validation
     */
    public boolean registerUser(String email, String password, String fullName) {
        try {
            email = email.toLowerCase().trim();
            
            System.out.println("LOCAL_DB: Attempting to register user: " + email);
            
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
            if (userExists(email)) {
                showError("User Already Exists\n\nAn account with this email already exists.\n" +
                         "Please use the login option instead.");
                return false;
            }
            
            // Create new user
            String hashedPassword = hashPassword(password);
            String userType = determineUserType(email);
            
            String insertQuery = "INSERT INTO users (email, password_hash, full_name, user_type) " +
                                "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, email);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, fullName);
                stmt.setString(4, userType);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Get generated user ID
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int userId = generatedKeys.getInt(1);
                            
                            // Create user object and add to cache
                            User newUser = new User(email, fullName);
                            newUser.setId(userId);
                            newUser.setPasswordHash(hashedPassword);
                            newUser.setUserType(User.UserType.valueOf(userType));
                            newUser.setCreatedAt(new java.util.Date());
                            
                            userCache.put(email, newUser);
                            
                            System.out.println("SUCCESS: User registered successfully: " + email);
                            
                            // Try to sync with Firebase (optional backup)
                            syncUserToFirebase(newUser);
                            
                            JOptionPane.showMessageDialog(null, 
                                "Registration Successful!\n\n" +
                                "Welcome to the University System.\n" +
                                "You can now login with your credentials.",
                                "Welcome!", 
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            return true;
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to register user: " + e.getMessage());
            e.printStackTrace();
            showError("Registration Error\n\nDatabase error occurred.\nPlease try again.");
        }
        
        return false;
    }
    
    /**
     * Authenticate user login - Primary authentication method
     */
    public User authenticateUser(String email, String password) {
        try {
            email = email.toLowerCase().trim();
            
            System.out.println("LOCAL_DB: Authenticating user: " + email);
            
            // Check cache first
            User user = userCache.get(email);
            
            // If not in cache, load from database
            if (user == null) {
                user = loadUserFromDatabase(email);
                if (user != null) {
                    userCache.put(email, user);
                }
            }
            
            // Verify user exists and password is correct
            if (user != null && verifyPassword(password, user.getPasswordHash())) {
                // Update last login
                updateLastLogin(user);
                
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
            
            System.out.println("LOCAL_DB: Processing Google user: " + email);
            
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
            User user = userCache.get(email);
            if (user == null) {
                user = loadUserFromDatabase(email);
            }
            
            if (user != null) {
                // Update Google sub if needed
                if (user.getGoogleSub() == null || !user.getGoogleSub().equals(googleSub)) {
                    updateGoogleSub(user, googleSub);
                }
                
                // Update last login
                updateLastLogin(user);
                
                System.out.println("SUCCESS: Existing Google user authenticated: " + email);
                return user;
            }
            
            // Create new user from Google account
            String userType = determineUserType(email);
            
            String insertQuery = "INSERT INTO users (email, google_sub, full_name, user_type) " +
                                "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, email);
                stmt.setString(2, googleSub);
                stmt.setString(3, fullName);
                stmt.setString(4, userType);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int userId = generatedKeys.getInt(1);
                            
                            // Create user object
                            user = new User(email, fullName);
                            user.setId(userId);
                            user.setGoogleSub(googleSub);
                            user.setUserType(User.UserType.valueOf(userType));
                            user.setCreatedAt(new java.util.Date());
                            
                            userCache.put(email, user);
                            
                            System.out.println("SUCCESS: New Google user created: " + email);
                            
                            // Try to sync with Firebase (optional backup)
                            syncUserToFirebase(user);
                            
                            return user;
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
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
        
        if (addToWhitelistInternal(email, adminUser.getEmail())) {
            whitelistedEmails.add(email);
            System.out.println("SUCCESS: Added to whitelist: " + email);
            return true;
        }
        
        return false;
    }
    
    public boolean removeFromWhitelist(String email, User adminUser) {
        if (!isUserAdmin(adminUser)) {
            System.out.println("ERROR: User is not admin: " + adminUser.getEmail());
            return false;
        }
        
        email = email.toLowerCase().trim();
        
        try {
            String updateQuery = "UPDATE whitelist SET is_active = 0 WHERE email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setString(1, email);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    whitelistedEmails.remove(email);
                    System.out.println("SUCCESS: Removed from whitelist: " + email);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to remove from whitelist: " + e.getMessage());
        }
        
        return false;
    }
    
    public Set<String> getWhitelistedEmails(User adminUser) {
        if (!isUserAdmin(adminUser)) {
            return new HashSet<>();
        }
        return new HashSet<>(whitelistedEmails);
    }
    
    // Helper methods
    private boolean addToWhitelistInternal(String email, String addedBy) {
        try {
            String insertQuery = "INSERT OR IGNORE INTO whitelist (email, added_by) " +
                               "VALUES (?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setString(1, email);
                stmt.setString(2, addedBy);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to add to whitelist: " + e.getMessage());
            return false;
        }
    }
    
    private User loadUserFromDatabase(String email) {
        try {
            String query = "SELECT * FROM users WHERE email = ? AND is_active = 1";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to load user from database: " + e.getMessage());
        }
        return null;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(rs.getString("email"), rs.getString("full_name"));
        user.setId(rs.getInt("id"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setGoogleSub(rs.getString("google_sub"));
        user.setUserType(User.UserType.valueOf(rs.getString("user_type")));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(new java.util.Date(createdAt.getTime()));
        }
        
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(new java.util.Date(lastLogin.getTime()));
        }
        
        return user;
    }
    
    private void updateLastLogin(User user) {
        try {
            String updateQuery = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setInt(1, user.getId());
                stmt.executeUpdate();
                user.setLastLogin(new java.util.Date());
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to update last login: " + e.getMessage());
        }
    }
    
    private void updateGoogleSub(User user, String googleSub) {
        try {
            String updateQuery = "UPDATE users SET google_sub = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setString(1, googleSub);
                stmt.setInt(2, user.getId());
                stmt.executeUpdate();
                user.setGoogleSub(googleSub);
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to update Google sub: " + e.getMessage());
        }
    }
    
    private boolean userExists(String email) {
        return userCache.containsKey(email.toLowerCase()) || loadUserFromDatabase(email) != null;
    }
    
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
     * Firebase backup sync (optional - non-blocking)
     */
    private void syncUserToFirebase(User user) {
        // Run in background thread to not block main authentication
        new Thread(() -> {
            try {
                System.out.println("FIREBASE_SYNC: Attempting to sync user to Firebase: " + user.getEmail());
                // TODO: Implement actual Firebase sync here
                // This is where Firebase backup would happen
                System.out.println("FIREBASE_SYNC: User synced to Firebase (simulated): " + user.getEmail());
            } catch (Exception e) {
                System.out.println("FIREBASE_SYNC: Failed to sync to Firebase (non-critical): " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Get database statistics
     */
    public Map<String, Object> getSystemStats(User adminUser) {
        if (!isUserAdmin(adminUser)) {
            return new HashMap<>();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userCache.size());
        stats.put("whitelistedEmails", whitelistedEmails.size());
        stats.put("databaseLocation", new java.io.File("university.db").getAbsolutePath());
        stats.put("cacheSize", userCache.size());
        
        return stats;
    }
    
    /**
     * Cleanup resources
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("LOCAL_DB: Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to close database connection: " + e.getMessage());
        }
    }
}
