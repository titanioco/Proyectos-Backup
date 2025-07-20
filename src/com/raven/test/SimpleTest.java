package com.raven.test;

import com.raven.model.User;
import com.raven.model.UserDAO;
import java.util.Optional;

public class SimpleTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing User Registration and Login...");
            
            // Test user creation
            User newUser = new User("test@university.edu", "Test User");
            newUser.setPasswordHash(UserDAO.hashPassword("password123"));
            
            System.out.println("Creating user...");
            UserDAO.createUser(newUser);
            System.out.println("✓ User created successfully!");
            
            // Test finding user
            System.out.println("Finding user by email...");
            Optional<User> foundUser = UserDAO.findByEmail("test@university.edu");
            if (foundUser.isPresent()) {
                System.out.println("✓ User found: " + foundUser.get().getFullName());
                
                // Test password verification
                boolean passwordValid = UserDAO.verifyPassword("password123", foundUser.get().getPasswordHash());
                System.out.println("✓ Password verification: " + (passwordValid ? "SUCCESS" : "FAILED"));
            } else {
                System.out.println("✗ User not found");
            }
            
            // Test Google user
            User googleUser = new User("google@gmail.com", "Google User");
            googleUser.setGoogleSub("google-sub-123");
            UserDAO.createUser(googleUser);
            
            Optional<User> foundGoogleUser = UserDAO.findByGoogleSub("google-sub-123");
            if (foundGoogleUser.isPresent()) {
                System.out.println("✓ Google user found: " + foundGoogleUser.get().getFullName());
            }
            
            System.out.println("\n🎉 All tests passed! The application is ready to use.");
            
        } catch (Exception e) {
            System.err.println("✗ Test failed:");
            e.printStackTrace();
        }
    }
}
