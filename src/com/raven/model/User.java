package com.raven.model;

import java.time.LocalDateTime;
import java.util.Date;

public class User {
    
    /**
     * User Types for access control and feature limitations
     */
    public enum UserType {
        FREE,      // Limited features, max 5 projects
        PREMIUM,   // Full features, max 50 projects  
        ADMIN      // Unlimited access, system management
    }
    
    private int id;
    private String email;
    private String passwordHash;
    private String googleSub;
    private String fullName;
    private LocalDateTime createdAt;
    private Date lastLogin;
    private UserType userType;
    private int projectCount;
    
    public User() {}
    
    public User(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }
    
    public User(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getGoogleSub() {
        return googleSub;
    }
    
    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        // Convert Date to LocalDateTime for backward compatibility
        this.createdAt = LocalDateTime.now();
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public int getProjectCount() {
        return projectCount;
    }
    
    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }
    
    public int getMaxProjects() {
        switch (userType) {
            case FREE: return 5;
            case PREMIUM: return 50;
            case ADMIN: return Integer.MAX_VALUE;
            default: return 5;
        }
    }
    
    public boolean canCreateMoreProjects() {
        return projectCount < getMaxProjects();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
