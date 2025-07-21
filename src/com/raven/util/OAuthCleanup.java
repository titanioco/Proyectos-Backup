package com.raven.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for OAuth credential cleanup
 * Ensures OAuth files are removed to prevent git issues
 */
public class OAuthCleanup {
    
    // Files to clean up (actual credential files, not templates)
    private static final List<String> CLEANUP_FILES = Arrays.asList(
        "oauth.properties",
        "oauth.properties.fixed", 
        "oauth.properties.local",
        "oauth.properties.backup"
    );
    
    // File patterns to clean up
    private static final List<String> CLEANUP_PATTERNS = Arrays.asList(
        "*.credentials",
        "*.secret",
        "client_secret*.json"
    );
    
    /**
     * Clean up OAuth credential files
     * @return true if cleanup was successful, false otherwise
     */
    public static boolean cleanupOAuthFiles() {
        System.out.println("🧹 Starting OAuth credential cleanup...");
        boolean success = true;
        int filesRemoved = 0;
        
        try {
            String currentDir = System.getProperty("user.dir");
            
            // Clean up specific files
            for (String fileName : CLEANUP_FILES) {
                Path filePath = Paths.get(currentDir, fileName);
                if (Files.exists(filePath)) {
                    try {
                        Files.delete(filePath);
                        System.out.println("✅ Removed: " + fileName);
                        filesRemoved++;
                    } catch (IOException e) {
                        System.err.println("❌ Failed to remove: " + fileName + " - " + e.getMessage());
                        success = false;
                    }
                }
            }
            
            // Clean up pattern-based files
            File dir = new File(currentDir);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName();
                        for (String pattern : CLEANUP_PATTERNS) {
                            if (matchesPattern(fileName, pattern)) {
                                try {
                                    if (file.delete()) {
                                        System.out.println("✅ Removed pattern file: " + fileName);
                                        filesRemoved++;
                                    } else {
                                        System.err.println("❌ Failed to remove pattern file: " + fileName);
                                        success = false;
                                    }
                                } catch (Exception e) {
                                    System.err.println("❌ Error removing pattern file: " + fileName + " - " + e.getMessage());
                                    success = false;
                                }
                                break;
                            }
                        }
                    }
                }
            }
            
            if (filesRemoved > 0) {
                System.out.println("✅ OAuth cleanup completed: " + filesRemoved + " files removed");
            } else {
                System.out.println("✅ OAuth cleanup completed: No credential files found");
            }
            
            // Verify git status
            verifyGitStatus();
            
        } catch (Exception e) {
            System.err.println("❌ OAuth cleanup failed: " + e.getMessage());
            success = false;
        }
        
        return success;
    }
    
    /**
     * Simple pattern matching for file cleanup
     */
    private static boolean matchesPattern(String fileName, String pattern) {
        if (pattern.startsWith("*") && pattern.endsWith("*")) {
            String middle = pattern.substring(1, pattern.length() - 1);
            return fileName.contains(middle);
        } else if (pattern.startsWith("*")) {
            String suffix = pattern.substring(1);
            return fileName.endsWith(suffix);
        } else if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return fileName.startsWith(prefix);
        } else {
            return fileName.equals(pattern);
        }
    }
    
    /**
     * Verify that no OAuth files are in git staging area
     */
    private static void verifyGitStatus() {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "status", "--porcelain");
            pb.directory(new File(System.getProperty("user.dir")));
            Process process = pb.start();
            
            // Read output using BufferedReader for Java 8 compatibility
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            
            StringBuilder gitStatus = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                gitStatus.append(line).append("\n");
            }
            reader.close();
            
            String statusOutput = gitStatus.toString();
            boolean hasOAuthFiles = statusOutput.contains("oauth") || 
                                  statusOutput.contains(".properties") || 
                                  statusOutput.contains(".credentials") || 
                                  statusOutput.contains(".secret");
            
            if (hasOAuthFiles) {
                System.out.println("⚠️ Warning: OAuth-related files still detected in git staging area");
                System.out.println("💡 Run 'git reset HEAD *.properties' to unstage them");
            } else {
                System.out.println("✅ Git status clean: No OAuth files in staging area");
            }
            
        } catch (Exception e) {
            // Git verification failed, but not critical
            System.out.println("ℹ️ Could not verify git status (git may not be available)");
        }
    }
    
    /**
     * Register cleanup as shutdown hook
     */
    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔄 Application shutdown detected...");
            cleanupOAuthFiles();
            System.out.println("👋 OAuth cleanup completed on shutdown");
        }));
    }
}
