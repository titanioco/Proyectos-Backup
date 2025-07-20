package com.raven.ui;

import com.raven.swing.Button;
import java.awt.*;
import java.awt.event.ActionListener;

public class GoogleSignInButton extends Button {
    
    public GoogleSignInButton() {
        setText("Sign in with Google");
        setBackground(new Color(66, 133, 244));
        setForeground(Color.WHITE);
        setFont(new Font("sansserif", Font.BOLD, 14));
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setEffectColor(new Color(255, 255, 255, 60));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw Google colors background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        
        // Draw Google "G" icon (simplified)
        int iconSize = 20;
        int iconX = 15;
        int iconY = (getHeight() - iconSize) / 2;
        
        // White circle background for the G
        g2.setColor(Color.WHITE);
        g2.fillOval(iconX, iconY, iconSize, iconSize);
        
        // Draw simplified "G"
        g2.setColor(new Color(66, 133, 244));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("G", iconX + 6, iconY + 14);
        
        // Draw text
        g2.setColor(getForeground());
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = iconX + iconSize + 10;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);
        
        g2.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 45);
    }
}
