package com.raven.ds.modules.graph;

import com.raven.ds.core.AnimationEngine;
import com.raven.ds.core.SimpleAnimationStep;

import java.util.*;

/**
 * Implementation of Dijkstra's shortest path algorithm with animation support
 */
public class DijkstraAlgorithm {
    private List<GraphNode> nodes;
    private GraphNode startNode;
    private GraphNode endNode;
    private List<GraphNode> shortestPath;
    private PriorityQueue<GraphNode> unvisitedNodes;
    private AnimationEngine animationEngine;
    
    public DijkstraAlgorithm(AnimationEngine animationEngine) {
        this.nodes = new ArrayList<>();
        this.animationEngine = animationEngine;
        this.shortestPath = new ArrayList<>();
    }
    
    public void addNode(GraphNode node) {
        nodes.add(node);
    }
    
    public void removeNode(GraphNode node) {
        nodes.remove(node);
        // Remove edges pointing to this node
        for (GraphNode n : nodes) {
            n.getEdges().removeIf(edge -> edge.getTarget() == node);
        }
    }
    
    public void addEdge(GraphNode source, GraphNode target, int weight) {
        source.addEdge(target, weight);
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
        startNode = null;
        endNode = null;
    }
    
    public void runDijkstra() {
        if (startNode == null) {
            throw new IllegalStateException("Start node must be set before running Dijkstra");
        }
        
        // Save start and end nodes before reset
        GraphNode savedStartNode = startNode;
        GraphNode savedEndNode = endNode;
        
        reset();
        animationEngine.clearSteps();
        
        // Restore start and end nodes after reset
        startNode = savedStartNode;
        endNode = savedEndNode;
        
        // Initialize distances
        startNode.setDistance(0);
        
        // Create priority queue
        unvisitedNodes = new PriorityQueue<>(Comparator.comparingInt(GraphNode::getDistance));
        unvisitedNodes.addAll(nodes);
        
        // Add initialization step
        animationEngine.addStep(new SimpleAnimationStep("Initialize", () -> {
            startNode.setHighlighted(true);
        }, "Set start node distance to 0"));
        
        while (!unvisitedNodes.isEmpty()) {
            GraphNode current = unvisitedNodes.poll();
            
            if (current.getDistance() == Integer.MAX_VALUE) {
                break; // No more reachable nodes
            }
            
            final GraphNode currentNode = current;
            animationEngine.addStep(new SimpleAnimationStep("Visit Node", () -> {
                // Clear previous highlights
                clearHighlights();
                currentNode.setHighlighted(true);
                currentNode.setVisited(true);
            }, "Visiting node " + current.getId() + " (distance: " + current.getDistance() + ")"));
            
            // Process each neighbor
            for (GraphEdge edge : current.getEdges()) {
                GraphNode neighbor = edge.getTarget();
                
                if (neighbor.isVisited()) continue;
                
                int newDistance = current.getDistance() + edge.getWeight();
                
                if (newDistance < neighbor.getDistance()) {
                    final int oldDistance = neighbor.getDistance();
                    animationEngine.addStep(new SimpleAnimationStep("Update Distance", () -> {
                        edge.setHighlighted(true);
                        neighbor.setDistance(newDistance);
                        neighbor.setPredecessor(currentNode);
                        // Update priority queue
                        unvisitedNodes.remove(neighbor);
                        unvisitedNodes.add(neighbor);
                    }, "Update " + neighbor.getId() + " distance from " + 
                       (oldDistance == Integer.MAX_VALUE ? "∞" : oldDistance) + 
                       " to " + newDistance));
                } else {
                    animationEngine.addStep(new SimpleAnimationStep("No Update", () -> {
                        edge.setHighlighted(true);
                    }, "No update needed for " + neighbor.getId() + 
                       " (current: " + neighbor.getDistance() + ", new: " + newDistance + ")"));
                }
            }
            
            if (current == endNode) {
                // Found shortest path to end node
                buildShortestPath();
                animationEngine.addStep(new SimpleAnimationStep("Path Found", () -> {
                    highlightShortestPath();
                }, "Shortest path found! Total distance: " + endNode.getDistance()));
                break;
            }
        }
        
        animationEngine.addStep(new SimpleAnimationStep("Complete", () -> {
            clearHighlights();
        }, "Algorithm completed"));
    }
    
    private void clearHighlights() {
        for (GraphNode node : nodes) {
            node.setHighlighted(false);
            for (GraphEdge edge : node.getEdges()) {
                edge.setHighlighted(false);
            }
        }
    }
    
    private void buildShortestPath() {
        shortestPath.clear();
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
    
    public void loadSampleGraph() {
        reset();
        nodes.clear();
        
        // Create sample nodes
        GraphNode nodeA = new GraphNode("A", 100, 100);
        GraphNode nodeB = new GraphNode("B", 300, 100);
        GraphNode nodeC = new GraphNode("C", 200, 200);
        GraphNode nodeD = new GraphNode("D", 400, 200);
        GraphNode nodeE = new GraphNode("E", 300, 300);
        
        addNode(nodeA);
        addNode(nodeB);
        addNode(nodeC);
        addNode(nodeD);
        addNode(nodeE);
        
        // Add edges with weights
        addEdge(nodeA, nodeB, 4);
        addEdge(nodeA, nodeC, 2);
        addEdge(nodeB, nodeC, 1);
        addEdge(nodeB, nodeD, 5);
        addEdge(nodeC, nodeD, 8);
        addEdge(nodeC, nodeE, 10);
        addEdge(nodeD, nodeE, 2);
        
        setStartNode(nodeA);
        setEndNode(nodeE);
    }
    
    // Getters
    public List<GraphNode> getNodes() { return nodes; }
    public GraphNode getStartNode() { return startNode; }
    public GraphNode getEndNode() { return endNode; }
    public List<GraphNode> getShortestPath() { return shortestPath; }
    
    public GraphNode getNodeAt(int x, int y) {
        for (GraphNode node : nodes) {
            if (node.contains(x, y)) {
                return node;
            }
        }
        return null;
    }
}
