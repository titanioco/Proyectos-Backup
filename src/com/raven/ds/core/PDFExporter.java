package com.raven.ds.core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * PDF Exporter for data structure visualizations
 * Note: This is a lightweight implementation without iText dependency
 * In production, you would add iText 7 dependency and use proper PDF generation
 */
public class PDFExporter {
    private static final String EXPORT_DIR = "exported";
    
    public static boolean exportToPDF(JPanel panel, String title, String content) {
        try {
            // Create export directory if it doesn't exist
            Files.createDirectories(Paths.get(EXPORT_DIR));
            
            // Generate filename
            String filename = title.replaceAll("[^a-zA-Z0-9]", "_") + "_" + 
                             System.currentTimeMillis() + ".html";
            String filepath = EXPORT_DIR + File.separator + filename;
            
            // For now, export as HTML (in production, use iText 7 for PDF)
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head>");
            html.append("<title>").append(title).append("</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 40px; }");
            html.append("h1 { color: #07A479; border-bottom: 2px solid #07A479; }");
            html.append("h2 { color: #333; margin-top: 30px; }");
            html.append("pre { background: #f5f5f5; padding: 15px; border-left: 4px solid #07A479; }");
            html.append("img { max-width: 100%; border: 1px solid #ddd; margin: 10px 0; }");
            html.append(".complexity { background: #fff3cd; padding: 10px; border: 1px solid #ffeaa7; }");
            html.append("</style>");
            html.append("</head><body>");
            
            // Title page
            html.append("<h1>").append(title).append("</h1>");
            html.append("<p><strong>Generated on:</strong> ").append(new java.util.Date()).append("</p>");
            
            // Content
            html.append("<h2>Algorithm Overview</h2>");
            html.append("<p>").append(content).append("</p>");
            
            // Capture panel screenshot
            String imageFile = capturePanel(panel, title);
            if (imageFile != null) {
                html.append("<h2>Visualization</h2>");
                html.append("<img src=\"").append(imageFile).append("\" alt=\"Visualization Screenshot\"/>");
            }
            
            // Complexity analysis placeholder
            html.append("<h2>Complexity Analysis</h2>");
            html.append("<div class=\"complexity\">");
            html.append("<p><strong>Time Complexity:</strong> Varies by algorithm</p>");
            html.append("<p><strong>Space Complexity:</strong> Varies by algorithm</p>");
            html.append("</div>");
            
            // Pseudo-code placeholder
            html.append("<h2>Pseudo-code</h2>");
            html.append("<pre>");
            html.append("// Algorithm pseudo-code would go here\n");
            html.append("// This is generated based on the specific algorithm\n");
            html.append("function algorithm(input):\n");
            html.append("    // Implementation steps\n");
            html.append("    return result");
            html.append("</pre>");
            
            html.append("</body></html>");
            
            // Write to file
            try (FileWriter writer = new FileWriter(filepath)) {
                writer.write(html.toString());
            }
            
            // Show success message
            JOptionPane.showMessageDialog(null, 
                "Export successful!\n\nFile saved as: " + filepath + "\n\n" +
                "Note: This is an HTML export. In production version,\n" +
                "this would generate a proper PDF using iText 7.",
                "Export Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Export failed: " + e.getMessage(),
                "Export Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private static String capturePanel(JPanel panel, String title) {
        try {
            // Create buffered image of the panel
            BufferedImage image = new BufferedImage(
                panel.getWidth(), 
                panel.getHeight(), 
                BufferedImage.TYPE_INT_RGB);
            
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            panel.paint(g2d);
            g2d.dispose();
            
            // Save as PNG
            String filename = title.replaceAll("[^a-zA-Z0-9]", "_") + "_screenshot.png";
            String filepath = EXPORT_DIR + File.separator + filename;
            
            javax.imageio.ImageIO.write(image, "PNG", new File(filepath));
            
            return filename; // Return relative path for HTML
            
        } catch (Exception e) {
            System.err.println("Failed to capture panel screenshot: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create a comprehensive guide PDF with all data structure topics
     */
    public static void createComprehensiveGuide() {
        try {
            Files.createDirectories(Paths.get("docs"));
            String filepath = "docs" + File.separator + "DataStructuresGuide.html";
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head>");
            html.append("<title>Interactive Data Structures Guide</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }");
            html.append("h1 { color: #07A479; text-align: center; }");
            html.append("h2 { color: #333; border-bottom: 1px solid #ddd; }");
            html.append(".toc { background: #f9f9f9; padding: 20px; margin: 20px 0; }");
            html.append(".algorithm { margin: 20px 0; padding: 15px; border: 1px solid #ddd; }");
            html.append("</style>");
            html.append("</head><body>");
            
            html.append("<h1>Interactive Data Structures Learning Guide</h1>");
            
            html.append("<div class=\"toc\">");
            html.append("<h2>Table of Contents</h2>");
            html.append("<ol>");
            html.append("<li>Graph - Shortest Path (Dijkstra)</li>");
            html.append("<li>Hashing & Hash Functions</li>");
            html.append("<li>Binary Heap</li>");
            html.append("<li>Heapsort</li>");
            html.append("<li>Binary Search Tree (BST)</li>");
            html.append("<li>AVL Tree</li>");
            html.append("<li>Dynamic Array (ArrayList)</li>");
            html.append("</ol>");
            html.append("</div>");
            
            // Add content for each algorithm
            String[] topics = {
                "Graph - Shortest Path (Dijkstra)",
                "Hashing & Hash Functions", 
                "Binary Heap",
                "Heapsort",
                "Binary Search Tree (BST)",
                "AVL Tree",
                "Dynamic Array (ArrayList)"
            };
            
            for (String topic : topics) {
                html.append("<div class=\"algorithm\">");
                html.append("<h2>").append(topic).append("</h2>");
                html.append("<p>Detailed explanation of ").append(topic).append(" would go here.</p>");
                html.append("</div>");
            }
            
            html.append("</body></html>");
            
            try (FileWriter writer = new FileWriter(filepath)) {
                writer.write(html.toString());
            }
            
            JOptionPane.showMessageDialog(null, 
                "Comprehensive guide created: " + filepath,
                "Guide Generated", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Failed to create guide: " + e.getMessage(),
                "Guide Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
