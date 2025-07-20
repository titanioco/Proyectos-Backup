package com.raven.ui;

import javax.swing.*;
import java.awt.*;

public class PasswordStrengthLabel extends JLabel {
    
    public PasswordStrengthLabel() {
        setText("Password strength will appear here");
        setFont(new Font("sansserif", Font.PLAIN, 12));
        setForeground(Color.GRAY);
    }
    
    public void updateStrength(String password) {
        if (password == null || password.isEmpty()) {
            setText("Password strength will appear here");
            setForeground(Color.GRAY);
            return;
        }
        
        int strength = calculateStrength(password);
        
        switch (strength) {
            case 0:
            case 1:
                setText("Weak password");
                setForeground(new Color(231, 76, 60)); // Red
                break;
            case 2:
                setText("Fair password");
                setForeground(new Color(230, 126, 34)); // Orange
                break;
            case 3:
                setText("Good password");
                setForeground(new Color(241, 196, 15)); // Yellow
                break;
            case 4:
            case 5:
                setText("Strong password");
                setForeground(new Color(46, 204, 113)); // Green
                break;
        }
    }
    
    private int calculateStrength(String password) {
        int strength = 0;
        
        // Length check
        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        
        // Character variety checks
        if (password.matches(".*[a-z].*")) strength++; // lowercase
        if (password.matches(".*[A-Z].*")) strength++; // uppercase
        if (password.matches(".*[0-9].*")) strength++; // numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength++; // special chars
        
        return Math.min(strength, 5);
    }
}
