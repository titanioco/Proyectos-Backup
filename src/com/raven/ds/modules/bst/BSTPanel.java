package com.raven.ds.modules.bst;

import com.raven.ds.core.AnimationEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Visual panel for Binary Search Tree operations
 */
public class BSTPanel extends JPanel {
    private BSTAlgorithm bst;
    private AnimationEngine animationEngine;
    private Color nodeColor = new Color(7, 164, 121); // Raven green
    private Color highlightColor = new Color(255, 0, 0); // Red for highlights
    private Color textColor = Color.WHITE;
    private Font nodeFont = new Font("SansSerif", Font.BOLD, 14);
    
    private BSTNode highlightedNode = null;
    private String currentOperation = "";
    
    public BSTPanel(AnimationEngine animationEngine) {
        this.animationEngine = animationEngine;
        this.bst = new BSTAlgorithm();
        
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createTitledBorder("Binary Search Tree Visualization"));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (bst.getRoot() != null) {
            drawTree(g2d, bst.getRoot(), getWidth() / 2, 50, getWidth() / 4);
        }
        
        // Draw operation status
        if (!currentOperation.isEmpty()) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString("Current operation: " + currentOperation, 10, getHeight() - 20);
        }
    }
    
    private void drawTree(Graphics2D g2d, BSTNode node, int x, int y, int xOffset) {
        if (node == null) return;
        
        // Draw edges to children first (so they appear behind nodes)
        if (node.left != null) {
            int childX = x - xOffset;
            int childY = y + 80;
            g2d.setColor(Color.BLACK);
            g2d.drawLine(x, y + 15, childX, childY - 15);
            drawTree(g2d, node.left, childX, childY, xOffset / 2);
        }
        
        if (node.right != null) {
            int childX = x + xOffset;
            int childY = y + 80;
            g2d.setColor(Color.BLACK);
            g2d.drawLine(x, y + 15, childX, childY - 15);
            drawTree(g2d, node.right, childX, childY, xOffset / 2);
        }
        
        // Draw the node
        Color currentNodeColor = (node == highlightedNode) ? highlightColor : nodeColor;
        g2d.setColor(currentNodeColor);
        
        Ellipse2D.Double circle = new Ellipse2D.Double(x - 15, y - 15, 30, 30);
        g2d.fill(circle);
        
        // Draw node border
        g2d.setColor(Color.BLACK);
        g2d.draw(circle);
        
        // Draw node value
        g2d.setColor(textColor);
        g2d.setFont(nodeFont);
        FontMetrics fm = g2d.getFontMetrics();
        String valueStr = String.valueOf(node.value);
        int textX = x - fm.stringWidth(valueStr) / 2;
        int textY = y + fm.getAscent() / 2 - 2;
        g2d.drawString(valueStr, textX, textY);
    }
    
    public void insertValue(int value) {
        animationEngine.clearSteps();
        
        // Add animation steps for insertion
        animationEngine.addStep(new AnimationEngine.AnimationStep() {
            @Override
            public void execute() {
                currentOperation = "Inserting " + value;
                highlightedNode = null;
                repaint();
            }
            
            @Override
            public void reset() {
                currentOperation = "";
                highlightedNode = null;
                repaint();
            }
            
            @Override
            public String getDescription() {
                return "Starting insertion of " + value;
            }
        });
        
        List<BSTNode> path = bst.getInsertionPath(value);
        for (int i = 0; i < path.size(); i++) {
            final BSTNode node = path.get(i);
            final int stepNum = i + 1;
            
            animationEngine.addStep(new AnimationEngine.AnimationStep() {
                @Override
                public void execute() {
                    highlightedNode = node;
                    currentOperation = "Step " + stepNum + ": Comparing " + value + " with " + node.value;
                    repaint();
                }
                
                @Override
                public void reset() {
                    highlightedNode = null;
                    repaint();
                }
                
                @Override
                public String getDescription() {
                    return "Comparing " + value + " with " + node.value;
                }
            });
        }
        
        // Final step: actually insert
        animationEngine.addStep(new AnimationEngine.AnimationStep() {
            @Override
            public void execute() {
                bst.insert(value);
                highlightedNode = bst.findNode(value);
                currentOperation = "Inserted " + value;
                repaint();
            }
            
            @Override
            public void reset() {
                bst.delete(value);
                highlightedNode = null;
                currentOperation = "";
                repaint();
            }
            
            @Override
            public String getDescription() {
                return "Inserting " + value + " at the correct position";
            }
        });
    }
    
    public void insertValueInstant(int value) {
        bst.insert(value);
        repaint();
    }
    
    public void deleteValue(int value) {
        if (!bst.contains(value)) {
            JOptionPane.showMessageDialog(this, "Value " + value + " not found in tree");
            return;
        }
        
        animationEngine.clearSteps();
        
        // Add deletion animation steps
        animationEngine.addStep(new AnimationEngine.AnimationStep() {
            @Override
            public void execute() {
                currentOperation = "Deleting " + value;
                highlightedNode = bst.findNode(value);
                repaint();
            }
            
            @Override
            public void reset() {
                bst.insert(value);
                currentOperation = "";
                highlightedNode = null;
                repaint();
            }
            
            @Override
            public String getDescription() {
                return "Finding node to delete: " + value;
            }
        });
        
        animationEngine.addStep(new AnimationEngine.AnimationStep() {
            @Override
            public void execute() {
                bst.delete(value);
                highlightedNode = null;
                currentOperation = "Deleted " + value;
                repaint();
            }
            
            @Override
            public void reset() {
                bst.insert(value);
                repaint();
            }
            
            @Override
            public String getDescription() {
                return "Removing node " + value + " from tree";
            }
        });
    }
    
    public boolean findValue(int value) {
        boolean found = bst.contains(value);
        
        animationEngine.clearSteps();
        
        List<BSTNode> path = bst.getSearchPath(value);
        for (int i = 0; i < path.size(); i++) {
            final BSTNode node = path.get(i);
            final int stepNum = i + 1;
            final boolean isLast = (i == path.size() - 1);
            
            animationEngine.addStep(new AnimationEngine.AnimationStep() {
                @Override
                public void execute() {
                    highlightedNode = node;
                    if (isLast && node.value == value) {
                        currentOperation = "Found " + value + "!";
                    } else {
                        currentOperation = "Step " + stepNum + ": Searching at " + node.value;
                    }
                    repaint();
                }
                
                @Override
                public void reset() {
                    highlightedNode = null;
                    currentOperation = "";
                    repaint();
                }
                
                @Override
                public String getDescription() {
                    return "Searching for " + value + " at node " + node.value;
                }
            });
        }
        
        if (!found) {
            animationEngine.addStep(new AnimationEngine.AnimationStep() {
                @Override
                public void execute() {
                    highlightedNode = null;
                    currentOperation = value + " not found in tree";
                    repaint();
                }
                
                @Override
                public void reset() {
                    currentOperation = "";
                    repaint();
                }
                
                @Override
                public String getDescription() {
                    return value + " not found";
                }
            });
        }
        
        return found;
    }
    
    public void performTraversal(String traversalType) {
        animationEngine.clearSteps();
        
        List<BSTNode> traversalOrder = new ArrayList<>();
        switch (traversalType) {
            case "In-Order":
                bst.inOrderTraversal(bst.getRoot(), traversalOrder);
                break;
            case "Pre-Order":
                bst.preOrderTraversal(bst.getRoot(), traversalOrder);
                break;
            case "Post-Order":
                bst.postOrderTraversal(bst.getRoot(), traversalOrder);
                break;
        }
        
        for (int i = 0; i < traversalOrder.size(); i++) {
            final BSTNode node = traversalOrder.get(i);
            final int stepNum = i + 1;
            
            animationEngine.addStep(new AnimationEngine.AnimationStep() {
                @Override
                public void execute() {
                    highlightedNode = node;
                    currentOperation = traversalType + " Step " + stepNum + ": Visit " + node.value;
                    repaint();
                }
                
                @Override
                public void reset() {
                    highlightedNode = null;
                    currentOperation = "";
                    repaint();
                }
                
                @Override
                public String getDescription() {
                    return traversalType + " traversal: visit " + node.value;
                }
            });
        }
    }
    
    public void clear() {
        bst.clear();
        highlightedNode = null;
        currentOperation = "";
        repaint();
    }
}
