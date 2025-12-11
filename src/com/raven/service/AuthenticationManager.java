package com.raven.service;

import com.raven.database.LocalDatabaseService;
import com.raven.database.MemoryDatabaseService;
import com.raven.model.User;

/**
 * Unified Authentication Manager
 * Primary: Local SQLite Database (with fallback to Memory DB)
 * Backup: Firebase (for sync purposes only)
 */
public class AuthenticationManager {
    private static AuthenticationManager instance;
    private final Object databaseService; // Either LocalDatabaseService or MemoryDatabaseService
    private final boolean useMemoryFallback;
    private final FirebaseService firebaseService;
    
    private AuthenticationManager() {
        // Try to initialize SQLite first, fallback to memory database if issues
        Object dbService = null;
        boolean memoryFallback = false;
        
        try {
            System.out.println("AUTH_MANAGER: Attempting to initialize SQLite database...");
            dbService = LocalDatabaseService.getInstance();
            System.out.println("AUTH_MANAGER: ✅ SQLite database initialized successfully");
        } catch (Throwable e) { // Catch all throwables including runtime exceptions
            System.out.println("AUTH_MANAGER: ⚠️ SQLite database failed, using memory fallback");
            System.out.println("AUTH_MANAGER: SQLite error: " + e.getMessage());
            try {
                dbService = MemoryDatabaseService.getInstance();
                memoryFallback = true;
                System.out.println("AUTH_MANAGER: ✅ Memory database initialized successfully");
            } catch (Exception memoryException) {
                System.err.println("CRITICAL ERROR: Both SQLite and Memory database failed!");
                memoryException.printStackTrace();
                throw new RuntimeException("Unable to initialize any database system", memoryException);
            }
        }
        
        this.databaseService = dbService;
        this.useMemoryFallback = memoryFallback;
        
        // Initialize Firebase service (non-critical)
        FirebaseService firebaseServiceTemp = null;
        try {
            firebaseServiceTemp = FirebaseService.getInstance();
        } catch (Exception e) {
            System.out.println("AUTH_MANAGER: Firebase service unavailable (non-critical): " + e.getMessage());
        }
        this.firebaseService = firebaseServiceTemp;
        
        System.out.println("AUTH_MANAGER: Database mode: " + (useMemoryFallback ? "Memory (temporary)" : "SQLite (persistent)"));
        if (useMemoryFallback) {
            System.out.println("AUTH_MANAGER: ⚠️ WARNING: Using temporary memory storage - data will be lost on restart");
        }
    }
    
    public static synchronized AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }
    
    /**
     * Register a new user - uses local database as primary
     */
    public boolean registerUser(String email, String password, String fullName) {
        System.out.println("AUTH_MANAGER: Registering user with local database: " + email);
        
        boolean success;
        if (useMemoryFallback) {
            success = ((MemoryDatabaseService) databaseService).registerUser(email, password, fullName);
        } else {
            success = ((LocalDatabaseService) databaseService).registerUser(email, password, fullName);
        }
        
        if (success) {
            System.out.println("AUTH_MANAGER: User registered successfully in local database");
            
            // Optional: Sync to Firebase in background (non-blocking)
            syncToFirebaseAsync(email, password, fullName, "register");
        }
        
        return success;
    }
    
    /**
     * Authenticate user - uses local database as primary
     */
    public User authenticateUser(String email, String password) {
        System.out.println("AUTH_MANAGER: Authenticating user with local database: " + email);
        
        User user;
        if (useMemoryFallback) {
            user = ((MemoryDatabaseService) databaseService).authenticateUser(email, password);
        } else {
            user = ((LocalDatabaseService) databaseService).authenticateUser(email, password);
        }
        
        if (user != null) {
            System.out.println("AUTH_MANAGER: User authenticated successfully with local database");
            
            // Optional: Update Firebase in background (non-blocking)
            syncToFirebaseAsync(user.getEmail(), null, user.getFullName(), "login");
        }
        
        return user;
    }
    
    /**
     * Authenticate or create Google user - uses local database as primary
     */
    public User authenticateOrCreateGoogleUser(String email, String googleSub, String fullName) {
        System.out.println("AUTH_MANAGER: Processing Google user with local database: " + email);
        
        User user;
        if (useMemoryFallback) {
            user = ((MemoryDatabaseService) databaseService).authenticateOrCreateGoogleUser(email, googleSub, fullName);
        } else {
            user = ((LocalDatabaseService) databaseService).authenticateOrCreateGoogleUser(email, googleSub, fullName);
        }
        
        if (user != null) {
            System.out.println("AUTH_MANAGER: Google user processed successfully with local database");
            
            // Optional: Sync to Firebase in background (non-blocking)
            syncToFirebaseAsync(email, "oauth_" + googleSub, fullName, "google_oauth");
        }
        
        return user;
    }
    
    /**
     * Check if email is whitelisted
     */
    public boolean isEmailWhitelisted(String email) {
        if (useMemoryFallback) {
            return ((MemoryDatabaseService) databaseService).isEmailWhitelisted(email);
        } else {
            return ((LocalDatabaseService) databaseService).isEmailWhitelisted(email);
        }
    }
    
    /**
     * Add email to whitelist (admin only)
     */
    public boolean addToWhitelist(String email, User adminUser) {
        boolean success;
        if (useMemoryFallback) {
            success = ((MemoryDatabaseService) databaseService).addToWhitelist(email, adminUser);
        } else {
            success = ((LocalDatabaseService) databaseService).addToWhitelist(email, adminUser);
        }
        
        if (success) {
            // Optional: Sync to Firebase in background
            syncWhitelistToFirebaseAsync();
        }
        
        return success;
    }
    
    /**
     * Remove email from whitelist (admin only)
     */
    public boolean removeFromWhitelist(String email, User adminUser) {
        boolean success;
        if (useMemoryFallback) {
            success = ((MemoryDatabaseService) databaseService).removeFromWhitelist(email, adminUser);
        } else {
            success = ((LocalDatabaseService) databaseService).removeFromWhitelist(email, adminUser);
        }
        
        if (success) {
            // Optional: Sync to Firebase in background
            syncWhitelistToFirebaseAsync();
        }
        
        return success;
    }
    
    /**
     * Get whitelisted emails (admin only)
     */
    public java.util.Set<String> getWhitelistedEmails(User adminUser) {
        if (useMemoryFallback) {
            return ((MemoryDatabaseService) databaseService).getWhitelistedEmails(adminUser);
        } else {
            return ((LocalDatabaseService) databaseService).getWhitelistedEmails(adminUser);
        }
    }
    
    /**
     * Get system statistics (admin only)
     */
    public java.util.Map<String, Object> getSystemStats(User adminUser) {
        java.util.Map<String, Object> stats;
        if (useMemoryFallback) {
            stats = ((MemoryDatabaseService) databaseService).getSystemStats(adminUser);
        } else {
            stats = ((LocalDatabaseService) databaseService).getSystemStats(adminUser);
        }
        
        // Add Firebase status
        try {
            java.util.Map<String, Object> firebaseStats = firebaseService.getSystemStats(adminUser);
            stats.put("firebaseStatus", "Available");
            stats.put("firebaseUsers", firebaseStats.get("totalUsers"));
        } catch (Exception e) {
            stats.put("firebaseStatus", "Unavailable - " + e.getMessage());
            stats.put("firebaseUsers", "N/A");
        }
        
        // Add database mode info
        stats.put("databaseMode", useMemoryFallback ? "Memory (temporary)" : "SQLite (persistent)");
        
        return stats;
    }
    
    /**
     * Non-blocking Firebase sync operations
     */
    private void syncToFirebaseAsync(String email, String password, String fullName, String operation) {
        new Thread(() -> {
            try {
                System.out.println("FIREBASE_SYNC: Starting " + operation + " sync for: " + email);
                
                switch (operation) {
                    case "register":
                        if (password != null) {
                            firebaseService.registerUser(email, password, fullName);
                        }
                        break;
                    case "login":
                        // Just log the login event, don't actually authenticate
                        System.out.println("FIREBASE_SYNC: Login event logged for: " + email);
                        break;
                    case "google_oauth":
                        if (password != null) {
                            firebaseService.registerUser(email, password, fullName);
                        }
                        break;
                }
                
                System.out.println("FIREBASE_SYNC: " + operation + " sync completed for: " + email);
                
            } catch (Exception e) {
                System.out.println("FIREBASE_SYNC: " + operation + " sync failed (non-critical): " + e.getMessage());
            }
        }).start();
    }
    
    private void syncWhitelistToFirebaseAsync() {
        new Thread(() -> {
            try {
                System.out.println("FIREBASE_SYNC: Syncing whitelist to Firebase...");
                // TODO: Implement whitelist sync to Firebase
                System.out.println("FIREBASE_SYNC: Whitelist sync completed (simulated)");
            } catch (Exception e) {
                System.out.println("FIREBASE_SYNC: Whitelist sync failed (non-critical): " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Test connectivity to both databases
     */
    public String testConnectivity() {
        StringBuilder result = new StringBuilder();
        
        // Test local database
        try {
            java.util.Map<String, Object> localStats;
            if (useMemoryFallback) {
                localStats = ((MemoryDatabaseService) databaseService).getSystemStats(null);
                result.append("LOCAL DATABASE: Memory Database (Fallback)\n");
                result.append("  - Status: Connected\n");
                result.append("  - Note: Data will be lost when application closes\n");
            } else {
                localStats = ((LocalDatabaseService) databaseService).getSystemStats(null);
                result.append("LOCAL DATABASE: SQLite Database\n");
                result.append("  - Status: Connected\n");
                result.append("  - Database Location: ").append(localStats.get("databaseLocation")).append("\n");
            }
            result.append("  - Total Users: ").append(localStats.get("totalUsers")).append("\n");
            result.append("  - Whitelisted Emails: ").append(localStats.get("whitelistedEmails")).append("\n");
        } catch (Exception e) {
            result.append("LOCAL DATABASE: Error - ").append(e.getMessage()).append("\n");
        }
        
        // Test Firebase
        try {
            firebaseService.getSystemStats(null);
            result.append("FIREBASE: Available (Backup)\n");
            result.append("  - Status: Ready for sync operations\n");
        } catch (Exception e) {
            result.append("FIREBASE: Unavailable - ").append(e.getMessage()).append("\n");
            result.append("  - This is expected and non-critical\n");
        }
        
        return result.toString();
    }
    
    /**
     * Get database mode information
     */
    public String getDatabaseMode() {
        return useMemoryFallback ? "Memory (temporary)" : "SQLite (persistent)";
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            if (useMemoryFallback) {
                ((MemoryDatabaseService) databaseService).close();
            } else {
                ((LocalDatabaseService) databaseService).close();
            }
            System.out.println("AUTH_MANAGER: Resources cleaned up successfully");
        } catch (Exception e) {
            System.err.println("AUTH_MANAGER: Error during cleanup: " + e.getMessage());
        }
    }
}
