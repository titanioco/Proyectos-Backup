package com.raven.ds.modules.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the graph for Dijkstra's algorithm visualization
 */
public class GraphNode {
    private String id;
    private int x, y;
    private int distance;
    private GraphNode predecessor;
    private boolean visited;
    private boolean highlighted;
    private boolean inPath;  // Added to track if node is part of the shortest path
    private Color color;
    private List<GraphEdge> edges;
    
    public static final int NODE_RADIUS = 25;
    // High contrast color scheme using shades of blue for consistency
    public static final Color DEFAULT_COLOR = new Color(58, 123, 176);      // Medium blue
    public static final Color VISITED_COLOR = new Color(106, 168, 210);     // Light blue
    public static final Color CURRENT_COLOR = new Color(33, 84, 124);       // Dark blue
    public static final Color PATH_COLOR = new Color(255, 215, 0);          // Gold for high contrast path
    
    public GraphNode(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.distance = Integer.MAX_VALUE;
        this.predecessor = null;
        this.visited = false;
        this.highlighted = false;
        this.inPath = false;
        this.color = DEFAULT_COLOR;
        this.edges = new ArrayList<>();
    }
    
    public void addEdge(GraphNode target, int weight) {
        edges.add(new GraphEdge(this, target, weight));
    }
    
    public void draw(Graphics2D g2d) {
        // Draw node circle
        g2d.setColor(color);
        g2d.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw border with high contrast
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3)); // Thicker border for better visibility
        g2d.drawOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw node ID with high contrast
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14)); // Larger, bolder font
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(id);
        int textHeight = fm.getHeight();
        
        // Add black outline for better readability
        g2d.setColor(Color.BLACK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g2d.drawString(id, x - textWidth/2 + dx, y + textHeight/4 + dy);
                }
            }
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString(id, x - textWidth/2, y + textHeight/4);
        
        // Draw distance label with high contrast background
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        String distText = distance == Integer.MAX_VALUE ? "∞" : String.valueOf(distance);
        FontMetrics distFm = g2d.getFontMetrics();
        int distWidth = distFm.stringWidth(distText);
        
        // Background for distance label
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(x - distWidth/2 - 3, y - NODE_RADIUS - 18, distWidth + 6, 14, 4, 4);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x - distWidth/2 - 3, y - NODE_RADIUS - 18, distWidth + 6, 14, 4, 4);
        g2d.drawString(distText, x - distWidth/2, y - NODE_RADIUS - 7);
    }
    
    public boolean contains(int px, int py) {
        return Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2)) <= NODE_RADIUS;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    
    public int getDistance() { return distance; }
    public void setDistance(int distance) { this.distance = distance; }
    
    public GraphNode getPredecessor() { return predecessor; }
    public void setPredecessor(GraphNode predecessor) { this.predecessor = predecessor; }
    
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { 
        this.visited = visited;
        updateColor();
    }
    
    public boolean isHighlighted() { return highlighted; }
    public void setHighlighted(boolean highlighted) { 
        this.highlighted = highlighted;
        updateColor();
    }
    
    public boolean isInPath() { return inPath; }
    public void setInPath(boolean inPath) {
        this.inPath = inPath;
        updateColor();
    }
    
    public List<GraphEdge> getEdges() { return edges; }
    
    private void updateColor() {
        if (highlighted) {
            color = CURRENT_COLOR;
        } else if (inPath) {
            color = PATH_COLOR;
        } else if (visited) {
            color = VISITED_COLOR;
        } else {
            color = DEFAULT_COLOR;
        }
    }
    
    public void reset() {
        distance = Integer.MAX_VALUE;
        predecessor = null;
        visited = false;
        highlighted = false;
        inPath = false;
        color = DEFAULT_COLOR;
    }
}
