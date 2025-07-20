package com.raven.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory database simulation for testing without external dependencies
 */
public class SimpleUserDatabase {
    private static final Map<String, UserRecord> users = new HashMap<>();
    
    public static class UserRecord {
        public int id;
        public String email;
        public String passwordHash;
        public String googleSub;
        public String fullName;
        public String createdAt;
        
        public UserRecord(int id, String email, String passwordHash, String googleSub, String fullName) {
            this.id = id;
            this.email = email;
            this.passwordHash = passwordHash;
            this.googleSub = googleSub;
            this.fullName = fullName;
            this.createdAt = java.time.LocalDateTime.now().toString();
        }
    }
    
    private static int nextId = 1;
    
    public static void createUser(String email, String passwordHash, String googleSub, String fullName) {
        if (users.containsKey(email)) {
            throw new RuntimeException("User already exists");
        }
        users.put(email, new UserRecord(nextId++, email, passwordHash, googleSub, fullName));
    }
    
    public static UserRecord findByEmail(String email) {
        return users.get(email);
    }
    
    public static UserRecord findByGoogleSub(String googleSub) {
        return users.values().stream()
                .filter(user -> googleSub.equals(user.googleSub))
                .findFirst()
                .orElse(null);
    }
    
    public static void updateUser(String email, String passwordHash, String googleSub, String fullName) {
        UserRecord user = users.get(email);
        if (user != null) {
            user.passwordHash = passwordHash;
            user.googleSub = googleSub;
            user.fullName = fullName;
        }
    }
    
    public static void clear() {
        users.clear();
        nextId = 1;
    }
    
    public static int getUserCount() {
        return users.size();
    }
}
