package com.raven.model;

import com.raven.database.DBManager;
import com.raven.database.SimpleUserDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

public class UserDAO {
    private static boolean useFallbackDB = false;
    
    static {
        // Test if SQLite is available
        try {
            DBManager.getConnection();
            System.out.println("✓ Using SQLite database");
        } catch (Throwable t) {
            // Catch any errors (including NoClassDefFoundError) and fallback
            useFallbackDB = true;
            System.out.println("⚠ Using in-memory fallback database (SQLite not available): " + t.getMessage());
        }
    }
    
    public static void createUser(User user) throws SQLException {
        if (useFallbackDB) {
            try {
                SimpleUserDatabase.createUser(user.getEmail(), user.getPasswordHash(), 
                                             user.getGoogleSub(), user.getFullName());
            } catch (RuntimeException e) {
                throw new SQLException(e.getMessage());
            }
            return;
        }
        
        String query = "INSERT INTO users (email, password_hash, google_sub, full_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getGoogleSub());
            stmt.setString(4, user.getFullName());
            stmt.executeUpdate();
        }
    }
    
    public static Optional<User> findByEmail(String email) throws SQLException {
        if (useFallbackDB) {
            SimpleUserDatabase.UserRecord record = SimpleUserDatabase.findByEmail(email);
            if (record != null) {
                User user = new User();
                user.setId(record.id);
                user.setEmail(record.email);
                user.setPasswordHash(record.passwordHash);
                user.setGoogleSub(record.googleSub);
                user.setFullName(record.fullName);
                return Optional.of(user);
            }
            return Optional.empty();
        }
        
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    public static Optional<User> findByGoogleSub(String googleSub) throws SQLException {
        if (useFallbackDB) {
            SimpleUserDatabase.UserRecord record = SimpleUserDatabase.findByGoogleSub(googleSub);
            if (record != null) {
                User user = new User();
                user.setId(record.id);
                user.setEmail(record.email);
                user.setPasswordHash(record.passwordHash);
                user.setGoogleSub(record.googleSub);
                user.setFullName(record.fullName);
                return Optional.of(user);
            }
            return Optional.empty();
        }
        
        String query = "SELECT * FROM users WHERE google_sub = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, googleSub);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    public static void updateUser(User user) throws SQLException {
        if (useFallbackDB) {
            SimpleUserDatabase.updateUser(user.getEmail(), user.getPasswordHash(), 
                                         user.getGoogleSub(), user.getFullName());
            return;
        }
        
        String query = "UPDATE users SET password_hash = ?, google_sub = ?, full_name = ? WHERE email = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getPasswordHash());
            stmt.setString(2, user.getGoogleSub());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.executeUpdate();
        }
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            // Extract salt from the stored hash (first 16 characters)
            String salt = hashedPassword.substring(0, 16);
            String hash = hashPasswordWithSalt(plainPassword, salt);
            return hash.equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String hashPassword(String plainPassword) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);
            // Take only first 16 characters to ensure consistent length
            salt = salt.substring(0, Math.min(16, salt.length()));
            
            return hashPasswordWithSalt(plainPassword, salt);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    private static String hashPasswordWithSalt(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        byte[] hashedPassword = md.digest(password.getBytes());
        return salt + Base64.getEncoder().encodeToString(hashedPassword);
    }
    
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setGoogleSub(rs.getString("google_sub"));
        user.setFullName(rs.getString("full_name"));
        return user;
    }
}
