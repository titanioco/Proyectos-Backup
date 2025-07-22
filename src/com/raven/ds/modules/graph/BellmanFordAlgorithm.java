package com.raven.ds.modules.graph;

import com.raven.ds.core.AnimationEngine;
import com.raven.ds.core.SimpleAnimationStep;

import java.util.*;

/**
 * Implementation of Bellman-Ford shortest path algorithm with animation support
 */
public class BellmanFordAlgorithm {
    private List<GraphNode> nodes;
    private List<GraphEdge> edges;
    private GraphNode startNode;
    private GraphNode endNode;
    private List<GraphNode> shortestPath;
    private AnimationEngine animationEngine;
    private boolean hasNegativeCycle;
    
    public BellmanFordAlgorithm(AnimationEngine animationEngine) {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.animationEngine = animationEngine;
        this.shortestPath = new ArrayList<>();
        this.hasNegativeCycle = false;
    }
    
    public void setNodes(List<GraphNode> nodes) {
        this.nodes = nodes;
        updateEdgesList();
    }
    
    private void updateEdgesList() {
        edges.clear();
        for (GraphNode node : nodes) {
            edges.addAll(node.getEdges());
        }
    }
    
    public void setStartNode(GraphNode start) {
        this.startNode = start;
    }
    
    public void setEndNode(GraphNode end) {
        this.endNode = end;
    }
    
    public void reset() {
        for (GraphNode node : nodes) {
            node.reset();
            for (GraphEdge edge : node.getEdges()) {
                edge.reset();
            }
        }
        shortestPath.clear();
        hasNegativeCycle = false;
    }
    
    public void runBellmanFord() {
        if (startNode == null || nodes.isEmpty()) {
            throw new IllegalStateException("Start node must be set before running Bellman-Ford");
        }
        
        reset();
        animationEngine.clearSteps();
        hasNegativeCycle = false;
        
        // Initialize distances
        startNode.setDistance(0);
        
        animationEngine.addStep(new SimpleAnimationStep("Initialize", () -> {
            startNode.setHighlighted(true);
        }, "Initialize: Set start node distance to 0, all others to ∞"));
        
        // Relax edges |V| - 1 times
        int V = nodes.size();
        for (int i = 0; i < V - 1; i++) {
            final int iteration = i + 1;
            
            animationEngine.addStep(new SimpleAnimationStep("Iteration " + iteration, () -> {
                clearHighlights();
            }, "Starting iteration " + iteration + " of " + (V - 1) + " (relaxing all edges)"));
            
            boolean relaxed = false;
            for (GraphEdge edge : edges) {
                GraphNode source = edge.getSource();
                GraphNode target = edge.getTarget();
                
                if (source.getDistance() != Integer.MAX_VALUE) {
                    int newDistance = source.getDistance() + edge.getWeight();
                    
                    animationEngine.addStep(new SimpleAnimationStep("Check Edge", () -> {
                        clearHighlights();
                        edge.setHighlighted(true);
                        source.setHighlighted(true);
                        target.setHighlighted(true);
                    }, "Check edge " + source.getId() + " → " + target.getId() + 
                       " (weight: " + edge.getWeight() + ")"));
                    
                    if (newDistance < target.getDistance()) {
                        final int oldDistance = target.getDistance();
                        relaxed = true;
                        
                        animationEngine.addStep(new SimpleAnimationStep("Relax Edge", () -> {
                            target.setDistance(newDistance);
                            target.setPredecessor(source);
                        }, "Relax: Update " + target.getId() + " distance from " + 
                           (oldDistance == Integer.MAX_VALUE ? "∞" : oldDistance) + 
                           " to " + newDistance));
                    } else {
                        animationEngine.addStep(new SimpleAnimationStep("No Relaxation", () -> {
                            // Keep highlights
                        }, "No relaxation needed: " + newDistance + " ≥ " + target.getDistance()));
                    }
                }
            }
            
            if (!relaxed) {
                animationEngine.addStep(new SimpleAnimationStep("Early Termination", () -> {
                    clearHighlights();
                }, "No edges relaxed in iteration " + iteration + ". Algorithm can terminate early."));
                break;
            }
        }
        
        // Check for negative cycles
        animationEngine.addStep(new SimpleAnimationStep("Negative Cycle Check", () -> {
            clearHighlights();
        }, "Checking for negative cycles by trying to relax edges one more time..."));
        
        for (GraphEdge edge : edges) {
            GraphNode source = edge.getSource();
            GraphNode target = edge.getTarget();
            
            if (source.getDistance() != Integer.MAX_VALUE) {
                int newDistance = source.getDistance() + edge.getWeight();
                
                if (newDistance < target.getDistance()) {
                    hasNegativeCycle = true;
                    
                    animationEngine.addStep(new SimpleAnimationStep("Negative Cycle Found", () -> {
                        edge.setHighlighted(true);
                        source.setHighlighted(true);
                        target.setHighlighted(true);
                    }, "NEGATIVE CYCLE DETECTED! Edge " + source.getId() + " → " + target.getId() + 
                       " can still be relaxed."));
                    
                    animationEngine.addStep(new SimpleAnimationStep("Algorithm Failed", () -> {
                        clearHighlights();
                    }, "Bellman-Ford failed: Graph contains a negative cycle."));
                    return;
                }
            }
        }
        
        if (endNode != null && endNode.getDistance() != Integer.MAX_VALUE) {
            buildShortestPath();
            animationEngine.addStep(new SimpleAnimationStep("Path Found", () -> {
                highlightShortestPath();
            }, "Shortest path found! Distance to " + endNode.getId() + ": " + endNode.getDistance()));
        } else if (endNode != null) {
            animationEngine.addStep(new SimpleAnimationStep("No Path", () -> {
                clearHighlights();
                if (endNode != null) {
                    endNode.setHighlighted(true);
                }
            }, "No path exists from " + startNode.getId() + " to " + endNode.getId()));
        }
        
        animationEngine.addStep(new SimpleAnimationStep("Complete", () -> {
            if (!hasNegativeCycle && shortestPath.isEmpty()) {
                clearHighlights();
            }
        }, hasNegativeCycle ? 
           "Bellman-Ford completed with negative cycle detected." :
           "Bellman-Ford completed successfully!"));
    }
    
    private void clearHighlights() {
        for (GraphNode node : nodes) {
            node.setHighlighted(false);
        }
        for (GraphEdge edge : edges) {
            edge.setHighlighted(false);
        }
    }
    
    private void buildShortestPath() {
        shortestPath.clear();
        if (endNode == null || endNode.getDistance() == Integer.MAX_VALUE) return;
        
        GraphNode current = endNode;
        while (current != null) {
            shortestPath.add(0, current);
            current = current.getPredecessor();
        }
    }
    
    private void highlightShortestPath() {
        clearHighlights();
        
        // Highlight path nodes
        for (GraphNode node : shortestPath) {
            node.setHighlighted(true);
        }
        
        // Highlight path edges
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            GraphNode current = shortestPath.get(i);
            GraphNode next = shortestPath.get(i + 1);
            
            for (GraphEdge edge : current.getEdges()) {
                if (edge.getTarget() == next) {
                    edge.setPathColor();
                    break;
                }
            }
        }
    }
    
    // Getters
    public List<GraphNode> getNodes() { return nodes; }
    public GraphNode getStartNode() { return startNode; }
    public GraphNode getEndNode() { return endNode; }
    public List<GraphNode> getShortestPath() { return shortestPath; }
    public boolean hasNegativeCycle() { return hasNegativeCycle; }
}
