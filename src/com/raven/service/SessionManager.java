package com.raven.service;

import com.raven.main.Main;
import com.raven.ui.MainSelectionFrame;
import javax.swing.SwingUtilities;

/**
 * Centralized session management for the application
 * Handles login/logout flow and prevents multiple instances
 */
public class SessionManager {
    private static SessionManager instance;
    private Main mainLoginWindow;
    private MainSelectionFrame mainSelectionFrame;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Set the main login window instance
     */
    public void setMainLoginWindow(Main mainWindow) {
        this.mainLoginWindow = mainWindow;
        System.out.println("SESSION: Main login window registered");
    }
    
    /**
     * Set the main selection frame instance
     */
    public void setMainSelectionFrame(MainSelectionFrame mainSelectionFrame) {
        this.mainSelectionFrame = mainSelectionFrame;
        System.out.println("SESSION: Main selection frame registered");
    }
    
    /**
     * Get the main login window instance
     */
    public Main getMainLoginWindow() {
        return mainLoginWindow;
    }
    
    /**
     * Get the main selection frame instance
     */
    public MainSelectionFrame getMainSelectionFrame() {
        return mainSelectionFrame;
    }
    
    /**
     * Show the main selection frame (return from module to main menu)
     */
    public void showMainSelectionFrame() {
        SwingUtilities.invokeLater(() -> {
            if (mainSelectionFrame != null) {
                mainSelectionFrame.setVisible(true);
                mainSelectionFrame.toFront();
                mainSelectionFrame.requestFocus();
                System.out.println("SESSION: Returned to existing main selection frame");
            } else {
                System.err.println("ERROR: No main selection frame registered");
            }
        });
    }
    
    /**
     * Handle logout - return to the original login screen
     */
    public void logout() {
        SwingUtilities.invokeLater(() -> {
            // Clean up main selection frame
            if (mainSelectionFrame != null) {
                mainSelectionFrame.dispose();
                mainSelectionFrame = null;
                System.out.println("SESSION: Main selection frame disposed for logout");
            }
            
            if (mainLoginWindow != null) {
                // Reset any login panel state if needed
                if (mainLoginWindow.getLoginPanel() != null) {
                    mainLoginWindow.getLoginPanel().resetOAuthState();
                }
                
                // Show the original login window
                mainLoginWindow.setVisible(true);
                mainLoginWindow.toFront();
                mainLoginWindow.requestFocus();
                System.out.println("SESSION: Returned to original login screen");
            } else {
                System.err.println("ERROR: No main login window registered");
                // Create new instance as fallback (should not happen)
                new Main().setVisible(true);
            }
        });
    }
    
    /**
     * Hide the login window when user successfully logs in
     */
    public void hideLoginWindow() {
        if (mainLoginWindow != null) {
            mainLoginWindow.setVisible(false);
            System.out.println("SESSION: Login window hidden");
        }
    }
    
    /**
     * Check if main login window is available
     */
    public boolean isMainLoginWindowAvailable() {
        return mainLoginWindow != null;
    }
    
    /**
     * Check if main selection frame is available
     */
    public boolean isMainSelectionFrameAvailable() {
        return mainSelectionFrame != null;
    }
}
