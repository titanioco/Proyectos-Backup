package com.raven.ds.modules.graph;

import com.raven.ds.core.AnimationEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Visual panel for Dijkstra's algorithm visualization
 */
public class GraphPanel extends JPanel {
    private DijkstraAlgorithm dijkstraAlgorithm;
    private BellmanFordAlgorithm bellmanFordAlgorithm;
    private AnimationEngine animationEngine;
    private GraphNode draggedNode;
    private GraphNode selectedNode;
    private boolean isAddingEdge;
    private GraphNode edgeStartNode;
    private String currentAlgorithm = "DIJKSTRA"; // "DIJKSTRA" or "BELLMAN_FORD"
    
    // Instruction panel dragging and resizing
    private boolean isDraggingInstructions = false;
    private boolean isResizingInstructions = false;
    private int instructionPanelX = 10;
    private int instructionPanelY = -1; // Will be calculated
    private int instructionPanelWidth = 500;
    private int instructionPanelHeight = 92;
    private int dragOffsetX, dragOffsetY;
    private String resizeDirection = "";
    
    // Step explanation for animations - resizable and draggable
    private String currentStep = "";
    private String stepExplanation = "";
    private boolean isDraggingExplanation = false;
    private boolean isResizingExplanation = false;
    private int explanationPanelX = -1; // Will be calculated
    private int explanationPanelY = 20;
    private int explanationPanelWidth = 400; // Fixed width as requested
    private int explanationPanelHeight = 200;
    private int explanationDragOffsetX, explanationDragOffsetY;
    private String explanationResizeDirection = "";
    
    // Scrolling for overflow content
    private int instructionScrollY = 0;
    private int explanationScrollY = 0;
    
    private Color backgroundColor = Color.WHITE;
    private Font infoFont = new Font("SansSerif", Font.PLAIN, 12);
    private Font explanationFont = new Font("SansSerif", Font.PLAIN, 14);
    
    public GraphPanel(AnimationEngine animationEngine) {
        this.animationEngine = animationEngine;
        this.dijkstraAlgorithm = new DijkstraAlgorithm(animationEngine);
        this.bellmanFordAlgorithm = new BellmanFordAlgorithm(animationEngine);
        
        setBackground(backgroundColor);
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createTitledBorder("Graph Shortest Path Visualization"));
        
        setupMouseListeners();
    }
    
    
    private GraphNode getNodeAt(int x, int y) {
        for (GraphNode node : dijkstraAlgorithm.getNodes()) {
            if (node.contains(x, y)) {
                return node;
            }
        }
        return null;
    }
    
    private boolean isClickOnInstructionPanel(int x, int y) {
        if (instructionPanelY == -1) return false;
        
        return x >= instructionPanelX && x <= instructionPanelX + instructionPanelWidth &&
               y >= instructionPanelY && y <= instructionPanelY + instructionPanelHeight;
    }
    
    private boolean isClickOnExplanationPanel(int x, int y) {
        if (explanationPanelX == -1 || stepExplanation.isEmpty()) return false;
        
        return x >= explanationPanelX && x <= explanationPanelX + explanationPanelWidth &&
               y >= explanationPanelY && y <= explanationPanelY + explanationPanelHeight;
    }
    
    private String getInstructionResizeDirection(int x, int y) {
        if (!isClickOnInstructionPanel(x, y)) return "";
        
        int resizeMargin = 8;
        boolean nearLeft = x <= instructionPanelX + resizeMargin;
        boolean nearRight = x >= instructionPanelX + instructionPanelWidth - resizeMargin;
        boolean nearTop = y <= instructionPanelY + resizeMargin;
        boolean nearBottom = y >= instructionPanelY + instructionPanelHeight - resizeMargin;
        
        if (nearTop && nearLeft) return "NW";
        if (nearTop && nearRight) return "NE";
        if (nearBottom && nearLeft) return "SW";
        if (nearBottom && nearRight) return "SE";
        if (nearTop) return "N";
        if (nearBottom) return "S";
        if (nearLeft) return "W";
        if (nearRight) return "E";
        
        return "";
    }
    
    private String getExplanationResizeDirection(int x, int y) {
        if (!isClickOnExplanationPanel(x, y)) return "";
        
        int resizeMargin = 8;
        boolean nearLeft = x <= explanationPanelX + resizeMargin;
        boolean nearRight = x >= explanationPanelX + explanationPanelWidth - resizeMargin;
        boolean nearTop = y <= explanationPanelY + resizeMargin;
        boolean nearBottom = y >= explanationPanelY + explanationPanelHeight - resizeMargin;
        
        if (nearTop && nearLeft) return "NW";
        if (nearTop && nearRight) return "NE";
        if (nearBottom && nearLeft) return "SW";
        if (nearBottom && nearRight) return "SE";
        if (nearTop) return "N";
        if (nearBottom) return "S";
        if (nearLeft) return "W";
        if (nearRight) return "E";
        
        return "";
    }
    
    private Cursor getResizeCursor(String direction) {
        switch (direction) {
            case "N": case "S": return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            case "E": case "W": return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            case "NE": case "SW": return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            case "NW": case "SE": return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            default: return Cursor.getDefaultCursor();
        }
    }
    
    private void resizeInstructionPanel(int mouseX, int mouseY) {
        int minWidth = 200, minHeight = 80;
        int maxWidth = getWidth() - instructionPanelX;
        int maxHeight = getHeight() - instructionPanelY;
        
        switch (resizeDirection) {
            case "E":
                instructionPanelWidth = Math.max(minWidth, Math.min(maxWidth, mouseX - instructionPanelX));
                break;
            case "W":
                int newWidth = instructionPanelX + instructionPanelWidth - mouseX;
                if (newWidth >= minWidth) {
                    instructionPanelX = mouseX;
                    instructionPanelWidth = newWidth;
                }
                break;
            case "S":
                instructionPanelHeight = Math.max(minHeight, Math.min(maxHeight, mouseY - instructionPanelY));
                break;
            case "N":
                int newHeight = instructionPanelY + instructionPanelHeight - mouseY;
                if (newHeight >= minHeight) {
                    instructionPanelY = mouseY;
                    instructionPanelHeight = newHeight;
                }
                break;
            case "SE":
                instructionPanelWidth = Math.max(minWidth, Math.min(maxWidth, mouseX - instructionPanelX));
                instructionPanelHeight = Math.max(minHeight, Math.min(maxHeight, mouseY - instructionPanelY));
                break;
            case "SW":
                newWidth = instructionPanelX + instructionPanelWidth - mouseX;
                if (newWidth >= minWidth) {
                    instructionPanelX = mouseX;
                    instructionPanelWidth = newWidth;
                }
                instructionPanelHeight = Math.max(minHeight, Math.min(maxHeight, mouseY - instructionPanelY));
                break;
            case "NE":
                instructionPanelWidth = Math.max(minWidth, Math.min(maxWidth, mouseX - instructionPanelX));
                newHeight = instructionPanelY + instructionPanelHeight - mouseY;
                if (newHeight >= minHeight) {
                    instructionPanelY = mouseY;
                    instructionPanelHeight = newHeight;
                }
                break;
            case "NW":
                newWidth = instructionPanelX + instructionPanelWidth - mouseX;
                newHeight = instructionPanelY + instructionPanelHeight - mouseY;
                if (newWidth >= minWidth) {
                    instructionPanelX = mouseX;
                    instructionPanelWidth = newWidth;
                }
                if (newHeight >= minHeight) {
                    instructionPanelY = mouseY;
                    instructionPanelHeight = newHeight;
                }
                break;
        }
    }
    
    private void resizeExplanationPanel(int mouseX, int mouseY) {
        int minHeight = 100;
        int maxHeight = getHeight() - explanationPanelY;
        
        // Fixed width as requested, only allow height resizing
        switch (explanationResizeDirection) {
            case "S": case "SE": case "SW":
                explanationPanelHeight = Math.max(minHeight, Math.min(maxHeight, mouseY - explanationPanelY));
                break;
            case "N": case "NE": case "NW":
                int newHeight = explanationPanelY + explanationPanelHeight - mouseY;
                if (newHeight >= minHeight) {
                    explanationPanelY = mouseY;
                    explanationPanelHeight = newHeight;
                }
                break;
        }
    }
    
    public void addNode(GraphNode node) {
        dijkstraAlgorithm.addNode(node);
        bellmanFordAlgorithm.setNodes(dijkstraAlgorithm.getNodes());
    }
    
    private void removeNode(GraphNode node) {
        dijkstraAlgorithm.removeNode(node);
        bellmanFordAlgorithm.setNodes(dijkstraAlgorithm.getNodes());
    }
    
    public void addEdge(GraphNode source, GraphNode target, int weight) {
        dijkstraAlgorithm.addEdge(source, target, weight);
        bellmanFordAlgorithm.setNodes(dijkstraAlgorithm.getNodes());
    }
    
    public boolean hasEdge(GraphNode source, GraphNode target) {
        for (GraphEdge edge : source.getEdges()) {
            if (edge.getTarget() == target) {
                return true;
            }
        }
        return false;
    }
    
    private void setStartNode(GraphNode node) {
        // Clear previous start node
        for (GraphNode n : getNodes()) {
            if (n != node && isStartNode(n)) {
                n.updateColor();
            }
        }
        dijkstraAlgorithm.setStartNode(node);
        bellmanFordAlgorithm.setStartNode(node);
        node.setAsStartNode();
    }
    
    private void setEndNode(GraphNode node) {
        // Clear previous end node
        for (GraphNode n : getNodes()) {
            if (n != node && isEndNode(n)) {
                n.updateColor();
            }
        }
        dijkstraAlgorithm.setEndNode(node);
        bellmanFordAlgorithm.setEndNode(node);
        node.setAsEndNode();
    }
    
    private boolean isStartNode(GraphNode node) {
        return dijkstraAlgorithm.getStartNode() == node;
    }
    
    private boolean isEndNode(GraphNode node) {
        return dijkstraAlgorithm.getEndNode() == node;
    }
    
    private GraphNode getStartNode() {
        return dijkstraAlgorithm.getStartNode();
    }
    
    private GraphNode getEndNode() {
        return dijkstraAlgorithm.getEndNode();
    }
    
    private List<GraphNode> getNodes() {
        return dijkstraAlgorithm.getNodes();
    }
    
    public void setCurrentAlgorithm(String algorithm) {
        this.currentAlgorithm = algorithm;
        setBorder(BorderFactory.createTitledBorder(
            algorithm.equals("DIJKSTRA") ? 
            "Dijkstra's Shortest Path Visualization" : 
            "Bellman-Ford Shortest Path Visualization"));
    }
    
    public String getCurrentAlgorithm() {
        return currentAlgorithm;
    }
    
    private void setupMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Initialize panel positions if not set
                if (instructionPanelY == -1) {
                    instructionPanelY = getHeight() - instructionPanelHeight - 10;
                }
                if (explanationPanelX == -1) {
                    explanationPanelX = getWidth() - explanationPanelWidth - 10;
                }
                
                // Check for explanation panel interactions first
                if (isClickOnExplanationPanel(e.getX(), e.getY())) {
                    explanationResizeDirection = getExplanationResizeDirection(e.getX(), e.getY());
                    if (!explanationResizeDirection.isEmpty()) {
                        isResizingExplanation = true;
                        setCursor(getResizeCursor(explanationResizeDirection));
                    } else {
                        isDraggingExplanation = true;
                        explanationDragOffsetX = e.getX() - explanationPanelX;
                        explanationDragOffsetY = e.getY() - explanationPanelY;
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                    return;
                }
                
                // Check for instruction panel interactions
                if (isClickOnInstructionPanel(e.getX(), e.getY())) {
                    resizeDirection = getInstructionResizeDirection(e.getX(), e.getY());
                    if (!resizeDirection.isEmpty()) {
                        isResizingInstructions = true;
                        setCursor(getResizeCursor(resizeDirection));
                    } else {
                        isDraggingInstructions = true;
                        dragOffsetX = e.getX() - instructionPanelX;
                        dragOffsetY = e.getY() - instructionPanelY;
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                    return;
                }
                
                setCursor(Cursor.getDefaultCursor());
                
                // Regular node interaction logic
                GraphNode clickedNode = getNodeAt(e.getX(), e.getY());
                
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (isAddingEdge) {
                        if (clickedNode != null) {
                            if (edgeStartNode == null) {
                                edgeStartNode = clickedNode;
                                clickedNode.setHighlighted(true);
                            } else if (clickedNode != edgeStartNode) {
                                // Add edge dialog
                                String weightStr = JOptionPane.showInputDialog(
                                    GraphPanel.this,
                                    "Enter edge weight:",
                                    "Add Edge",
                                    JOptionPane.QUESTION_MESSAGE
                                );
                                
                                if (weightStr != null && !weightStr.trim().isEmpty()) {
                                    try {
                                        int weight = Integer.parseInt(weightStr.trim());
                                        addEdge(edgeStartNode, clickedNode, weight);
                                        repaint();
                                    } catch (NumberFormatException ex) {
                                        JOptionPane.showMessageDialog(
                                            GraphPanel.this,
                                            "Invalid weight. Please enter a number.",
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE
                                        );
                                    }
                                }
                                
                                edgeStartNode.setHighlighted(false);
                                edgeStartNode = null;
                                // Keep isAddingEdge = true to allow multiple edge additions
                            }
                        }
                    } else {
                        draggedNode = clickedNode;
                        selectedNode = clickedNode;
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (clickedNode != null) {
                        showNodeContextMenu(clickedNode, e.getX(), e.getY());
                    } else {
                        showBackgroundContextMenu(e.getX(), e.getY());
                    }
                }
                
                repaint();
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // Update cursor based on position
                if (isClickOnExplanationPanel(e.getX(), e.getY())) {
                    String resizeDir = getExplanationResizeDirection(e.getX(), e.getY());
                    if (!resizeDir.isEmpty()) {
                        setCursor(getResizeCursor(resizeDir));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                } else if (isClickOnInstructionPanel(e.getX(), e.getY())) {
                    String resizeDir = getInstructionResizeDirection(e.getX(), e.getY());
                    if (!resizeDir.isEmpty()) {
                        setCursor(getResizeCursor(resizeDir));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isResizingExplanation) {
                    resizeExplanationPanel(e.getX(), e.getY());
                    repaint();
                } else if (isDraggingExplanation) {
                    explanationPanelX = e.getX() - explanationDragOffsetX;
                    explanationPanelY = e.getY() - explanationDragOffsetY;
                    
                    // Keep panel within bounds
                    explanationPanelX = Math.max(0, Math.min(explanationPanelX, getWidth() - explanationPanelWidth));
                    explanationPanelY = Math.max(0, Math.min(explanationPanelY, getHeight() - explanationPanelHeight));
                    
                    repaint();
                } else if (isResizingInstructions) {
                    resizeInstructionPanel(e.getX(), e.getY());
                    repaint();
                } else if (isDraggingInstructions) {
                    instructionPanelX = e.getX() - dragOffsetX;
                    instructionPanelY = e.getY() - dragOffsetY;
                    
                    // Keep panel within bounds
                    instructionPanelX = Math.max(0, Math.min(instructionPanelX, getWidth() - instructionPanelWidth));
                    instructionPanelY = Math.max(0, Math.min(instructionPanelY, getHeight() - instructionPanelHeight));
                    
                    repaint();
                } else if (draggedNode != null && !isAddingEdge) {
                    draggedNode.setPosition(e.getX(), e.getY());
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDraggingInstructions = false;
                isResizingInstructions = false;
                isDraggingExplanation = false;
                isResizingExplanation = false;
                draggedNode = null;
                setCursor(Cursor.getDefaultCursor());
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        
        // Add mouse wheel scrolling support
        addMouseWheelListener(e -> {
            if (isClickOnInstructionPanel(e.getX(), e.getY())) {
                instructionScrollY += e.getWheelRotation() * 20;
                repaint();
            } else if (isClickOnExplanationPanel(e.getX(), e.getY())) {
                explanationScrollY += e.getWheelRotation() * 20;
                repaint();
            }
        });
    }
    
    private void showNodeContextMenu(GraphNode node, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem setStart = new JMenuItem("Set as Start Node");
        setStart.addActionListener(e -> {
            setStartNode(node);
            repaint();
        });
        
        JMenuItem setEnd = new JMenuItem("Set as End Node");
        setEnd.addActionListener(e -> {
            setEndNode(node);
            repaint();
        });
        
        JMenuItem delete = new JMenuItem("Delete Node");
        delete.addActionListener(e -> {
            removeNode(node);
            repaint();
        });
        
        menu.add(setStart);
        menu.add(setEnd);
        menu.addSeparator();
        menu.add(delete);
        
        menu.show(this, x, y);
    }
    
    private void showBackgroundContextMenu(int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem addNode = new JMenuItem("Add Node");
        addNode.addActionListener(e -> {
            String nodeId = JOptionPane.showInputDialog(
                this,
                "Enter node ID:",
                "Add Node",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (nodeId != null && !nodeId.trim().isEmpty()) {
                GraphNode newNode = new GraphNode(nodeId.trim(), x, y);
                addNode(newNode);
                repaint();
            }
        });
        
        menu.add(addNode);
        menu.show(this, x, y);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw edges first (so they appear behind nodes)
        for (GraphNode node : getNodes()) {
            for (GraphEdge edge : node.getEdges()) {
                edge.draw(g2d);
            }
        }
        
        // Draw nodes
        for (GraphNode node : getNodes()) {
            node.draw(g2d);
        }
        
        // Draw start/end node indicators
        drawNodeIndicators(g2d);
        
        // Draw step explanation (if algorithm is running)
        if (!stepExplanation.isEmpty()) {
            drawStepExplanation(g2d);
        }
        
        // Draw draggable instructions
        drawInstructions(g2d);
    }
    
    private void drawNodeIndicators(Graphics2D g2d) {
        if (getStartNode() != null) {
            GraphNode start = getStartNode();
            g2d.setColor(new Color(46, 204, 113));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            
            // Enhanced start node label with background
            String startText = "START";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(startText);
            
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(start.getX() - textWidth/2 - 3, start.getY() + GraphNode.NODE_RADIUS + 8, textWidth + 6, 16, 4, 4);
            g2d.setColor(new Color(46, 204, 113));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(start.getX() - textWidth/2 - 3, start.getY() + GraphNode.NODE_RADIUS + 8, textWidth + 6, 16, 4, 4);
            g2d.drawString(startText, start.getX() - textWidth/2, start.getY() + GraphNode.NODE_RADIUS + 20);
        }
        
        if (getEndNode() != null) {
            GraphNode end = getEndNode();
            g2d.setColor(new Color(231, 76, 60));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            
            // Enhanced end node label with background
            String endText = "END";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(endText);
            
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(end.getX() - textWidth/2 - 3, end.getY() + GraphNode.NODE_RADIUS + 8, textWidth + 6, 16, 4, 4);
            g2d.setColor(new Color(231, 76, 60));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(end.getX() - textWidth/2 - 3, end.getY() + GraphNode.NODE_RADIUS + 8, textWidth + 6, 16, 4, 4);
            g2d.drawString(endText, end.getX() - textWidth/2, end.getY() + GraphNode.NODE_RADIUS + 20);
        }
    }
    
    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        
        String[] instructions = {
            "🖱️ Right-click: Node context menu (set start/end, delete)",
            "🖱️ Left-click: Select and drag nodes around",
            "🎮 Use controls below to run Dijkstra or Bellman-Ford algorithms",
            "📊 Watch the step-by-step algorithm animation with explanations",
            "↔️ Drag panels to move, resize using edges",
            "🎯 Use step controls for detailed algorithm walkthrough"
        };
        
        // Initialize position if not set
        if (instructionPanelY == -1) {
            instructionPanelY = getHeight() - instructionPanelHeight - 10;
        }
        
        // Draw instruction panel background
        Color bgColor = isDraggingInstructions || isResizingInstructions ? 
            new Color(248, 249, 250, 200) : new Color(248, 249, 250, 230);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(instructionPanelX, instructionPanelY, instructionPanelWidth, instructionPanelHeight, 10, 10);
        
        Color borderColor = isDraggingInstructions || isResizingInstructions ? 
            new Color(52, 152, 219) : new Color(52, 73, 94);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(isDraggingInstructions || isResizingInstructions ? 3 : 2));
        g2d.drawRoundRect(instructionPanelX, instructionPanelY, instructionPanelWidth, instructionPanelHeight, 10, 10);
        
        // Draw title bar
        g2d.setColor(borderColor);
        g2d.fillRoundRect(instructionPanelX, instructionPanelY, instructionPanelWidth, 25, 10, 10);
        g2d.fillRect(instructionPanelX, instructionPanelY + 12, instructionPanelWidth, 13);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString("Instructions", instructionPanelX + 10, instructionPanelY + 18);
        
        // Draw drag/resize indicator
        if (!isDraggingInstructions && !isResizingInstructions) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2d.drawString("📌↔️", instructionPanelX + instructionPanelWidth - 30, instructionPanelY + 18);
        }
        
        // Create clipping area for scrollable content
        Shape oldClip = g2d.getClip();
        g2d.setClip(instructionPanelX + 5, instructionPanelY + 30, instructionPanelWidth - 10, instructionPanelHeight - 35);
        
        // Draw instructions with scrolling
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int totalContentHeight = instructions.length * lineHeight;
        int visibleHeight = instructionPanelHeight - 35;
        
        // Adjust scroll if needed
        int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
        instructionScrollY = Math.max(0, Math.min(instructionScrollY, maxScroll));
        
        for (int i = 0; i < instructions.length; i++) {
            int y = instructionPanelY + 30 + (i * lineHeight) - instructionScrollY + fm.getAscent();
            if (y >= instructionPanelY + 30 - lineHeight && y <= instructionPanelY + instructionPanelHeight) {
                g2d.drawString(instructions[i], instructionPanelX + 10, y);
            }
        }
        
        // Draw scrollbar if needed
        if (totalContentHeight > visibleHeight) {
            drawScrollbar(g2d, instructionPanelX + instructionPanelWidth - 15, instructionPanelY + 30, 
                         10, visibleHeight, instructionScrollY, maxScroll);
        }
        
        g2d.setClip(oldClip);
    }
    
    private void drawScrollbar(Graphics2D g2d, int x, int y, int width, int height, int scrollPos, int maxScroll) {
        if (maxScroll <= 0) return;
        
        // Draw background
        g2d.setColor(new Color(200, 200, 200, 100));
        g2d.fillRoundRect(x, y, width, height, 5, 5);
        
        // Calculate thumb position and size
        int thumbSize = Math.max(20, (int)((float)height * height / (height + maxScroll)));
        int thumbPos = (int)((float)scrollPos / maxScroll * (height - thumbSize));
        
        // Draw thumb
        g2d.setColor(new Color(100, 100, 100, 150));
        g2d.fillRoundRect(x + 1, y + thumbPos, width - 2, thumbSize, 4, 4);
    }
    
    private void drawStepExplanation(Graphics2D g2d) {
        // Initialize position if not set
        if (explanationPanelX == -1) {
            explanationPanelX = getWidth() - explanationPanelWidth - 10;
        }
        
        // Draw background with enhanced styling
        Color bgColor = isDraggingExplanation || isResizingExplanation ? 
            new Color(255, 255, 255, 220) : new Color(255, 255, 255, 250);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(explanationPanelX, explanationPanelY, explanationPanelWidth, explanationPanelHeight, 15, 15);
        
        // Draw border with algorithm-specific color
        Color borderColor = currentAlgorithm.equals("DIJKSTRA") ? 
            new Color(53, 106, 230) : new Color(55, 53, 230);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(isDraggingExplanation || isResizingExplanation ? 4 : 3));
        g2d.drawRoundRect(explanationPanelX, explanationPanelY, explanationPanelWidth, explanationPanelHeight, 15, 15);
        
        // Draw title bar
        g2d.setColor(borderColor);
        g2d.fillRoundRect(explanationPanelX, explanationPanelY, explanationPanelWidth, 25, 15, 15);
        g2d.fillRect(explanationPanelX, explanationPanelY + 12, explanationPanelWidth, 13);
        
        // Draw title text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        String title = currentAlgorithm.equals("DIJKSTRA") ? 
            "Dijkstra's Algorithm Progress" : "Bellman-Ford Algorithm Progress";
        g2d.drawString(title, explanationPanelX + 10, explanationPanelY + 18);
        
        // Draw drag/resize indicator
        if (!isDraggingExplanation && !isResizingExplanation) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2d.drawString("📌↕️", explanationPanelX + explanationPanelWidth - 30, explanationPanelY + 18);
        }
        
        // Create clipping area for scrollable content
        Shape oldClip = g2d.getClip();
        g2d.setClip(explanationPanelX + 5, explanationPanelY + 30, explanationPanelWidth - 10, explanationPanelHeight - 35);
        
        // Draw current step info
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics stepFm = g2d.getFontMetrics();
        int currentY = explanationPanelY + 30 - explanationScrollY + stepFm.getAscent();
        
        if (!currentStep.isEmpty() && currentY >= explanationPanelY + 30 - stepFm.getHeight() && 
            currentY <= explanationPanelY + explanationPanelHeight) {
            g2d.drawString(currentStep, explanationPanelX + 10, currentY);
        }
        currentY += stepFm.getHeight() + 5;
        
        // Draw explanation text with scrolling
        g2d.setFont(explanationFont);
        FontMetrics fm = g2d.getFontMetrics();
        String[] lines = stepExplanation.split("\n");
        
        int totalContentHeight = stepFm.getHeight() + 5 + (lines.length * fm.getHeight());
        int visibleHeight = explanationPanelHeight - 35;
        
        // Adjust scroll if needed
        int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
        explanationScrollY = Math.max(0, Math.min(explanationScrollY, maxScroll));
        
        for (int i = 0; i < lines.length; i++) {
            int y = currentY + (i * fm.getHeight());
            if (y >= explanationPanelY + 30 - fm.getHeight() && y <= explanationPanelY + explanationPanelHeight) {
                // Word wrap for long lines
                String line = lines[i];
                if (fm.stringWidth(line) > explanationPanelWidth - 30) {
                    String[] words = line.split(" ");
                    StringBuilder currentLine = new StringBuilder();
                    
                    for (String word : words) {
                        if (fm.stringWidth(currentLine + word + " ") < explanationPanelWidth - 30) {
                            currentLine.append(word).append(" ");
                        } else {
                            if (currentLine.length() > 0) {
                                g2d.drawString(currentLine.toString().trim(), explanationPanelX + 10, y);
                                y += fm.getHeight();
                                currentLine = new StringBuilder(word + " ");
                            } else {
                                g2d.drawString(word, explanationPanelX + 10, y);
                                y += fm.getHeight();
                            }
                        }
                    }
                    if (currentLine.length() > 0) {
                        g2d.drawString(currentLine.toString().trim(), explanationPanelX + 10, y);
                    }
                } else {
                    g2d.drawString(line, explanationPanelX + 10, y);
                }
            }
        }
        
        // Draw scrollbar if needed
        if (totalContentHeight > visibleHeight) {
            drawScrollbar(g2d, explanationPanelX + explanationPanelWidth - 15, explanationPanelY + 30, 
                         10, visibleHeight, explanationScrollY, maxScroll);
        }
        
        g2d.setClip(oldClip);
    }
    
    public void startAddingEdge() {
        isAddingEdge = true;
        edgeStartNode = null;
        // Clear any existing highlights
        for (GraphNode node : getNodes()) {
            node.setHighlighted(false);
        }
        repaint();
    }
    
    public void cancelAddingEdge() {
        isAddingEdge = false;
        if (edgeStartNode != null) {
            edgeStartNode.setHighlighted(false);
            edgeStartNode = null;
        }
        repaint();
    }
    
    public void loadSampleGraph() {
        dijkstraAlgorithm.loadSampleGraph();
        bellmanFordAlgorithm.setNodes(dijkstraAlgorithm.getNodes());
        repaint();
    }
    
    public void clearGraph() {
        dijkstraAlgorithm.reset();
        dijkstraAlgorithm.getNodes().clear();
        bellmanFordAlgorithm.setNodes(dijkstraAlgorithm.getNodes());
        clearStepExplanation();
        repaint();
    }
    
    public void runDijkstra() {
        if (getStartNode() == null) {
            JOptionPane.showMessageDialog(this, 
                "Please set a start node first (right-click on a node)", 
                "No Start Node", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            dijkstraAlgorithm.runDijkstra();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error running algorithm: " + e.getMessage(), 
                "Algorithm Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void runBellmanFord() {
        if (getStartNode() == null) {
            JOptionPane.showMessageDialog(this, 
                "Please set a start node first (right-click on a node)", 
                "No Start Node", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            bellmanFordAlgorithm.setNodes(dijkstraAlgorithm.getNodes());
            bellmanFordAlgorithm.runBellmanFord();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error running algorithm: " + e.getMessage(), 
                "Algorithm Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public DijkstraAlgorithm getDijkstraAlgorithm() {
        return dijkstraAlgorithm;
    }
    
    public BellmanFordAlgorithm getBellmanFordAlgorithm() {
        return bellmanFordAlgorithm;
    }
    
    // Methods for step-by-step animation explanations
    public void updateStepExplanation(String step, String explanation) {
        this.currentStep = step;
        this.stepExplanation = explanation;
        repaint();
    }
    
    public void clearStepExplanation() {
        this.currentStep = "";
        this.stepExplanation = "";
        repaint();
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public String getStepExplanation() {
        return stepExplanation;
    }
}
