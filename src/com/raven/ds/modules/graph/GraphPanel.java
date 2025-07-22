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
    
    private Color backgroundColor = Color.WHITE;
    private Font infoFont = new Font("SansSerif", Font.PLAIN, 12);
    
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
        dijkstraAlgorithm.setStartNode(node);
        bellmanFordAlgorithm.setStartNode(node);
    }
    
    private void setEndNode(GraphNode node) {
        dijkstraAlgorithm.setEndNode(node);
        bellmanFordAlgorithm.setEndNode(node);
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
                                isAddingEdge = false;
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
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null && !isAddingEdge) {
                    draggedNode.setPosition(e.getX(), e.getY());
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
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
        
        // Draw instructions
        drawInstructions(g2d);
    }
    
    private void drawNodeIndicators(Graphics2D g2d) {
        if (getStartNode() != null) {
            GraphNode start = getStartNode();
            g2d.setColor(new Color(46, 204, 113));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2d.drawString("START", start.getX() - 15, start.getY() + GraphNode.NODE_RADIUS + 15);
        }
        
        if (getEndNode() != null) {
            GraphNode end = getEndNode();
            g2d.setColor(new Color(231, 76, 60));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2d.drawString("END", end.getX() - 10, end.getY() + GraphNode.NODE_RADIUS + 15);
        }
    }
    
    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(infoFont);
        
        String[] instructions = {
            "Right-click: Context menu",
            "Left-click: Select/drag node",
            "Use controls below to run algorithm"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            g2d.drawString(instructions[i], 10, getHeight() - 60 + (i * 15));
        }
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
}
