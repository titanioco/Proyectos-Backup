package com.raven.ds.modules.bst;

import com.raven.ds.core.AnimationEngine;
import com.raven.ds.core.PDFExporter;
import com.raven.swing.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Enhanced Control panel for Binary Search Tree operations
 * Features comprehensive controls with export functionality
 */
public class BSTControls extends JPanel {
    private BSTPanel visualizer;
    private AnimationEngine animationEngine;
    
    private JTextField inputField;
    private Button insertBtn;
    private Button deleteBtn;
    private Button findBtn;
    private Button clearBtn;
    private Button playBtn;
    private Button pauseBtn;
    private Button stepBtn;
    private Button resetBtn;
    private Button downloadBtn;
    private JSlider speedSlider;
    private JLabel statusLabel;
    private JLabel speedLabel;
    private JComboBox<String> traversalCombo;
    private JProgressBar animationProgress;
    
    public BSTControls(BSTPanel visualizer, AnimationEngine animationEngine) {
        this.visualizer = visualizer;
        this.animationEngine = animationEngine;
        
        initComponents();
        setupLayout();
        setupListeners();
    }
    
    private void initComponents() {
        // Input field with enhanced styling
        inputField = new JTextField(10);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        inputField.setToolTipText("Enter a number to insert, delete, or search");
        
        // Operation buttons with enhanced styling
        insertBtn = new Button();
        insertBtn.setText("Insert");
        insertBtn.setBackground(new Color(39, 174, 96)); // Green
        insertBtn.setToolTipText("Insert value into BST");
        
        deleteBtn = new Button();
        deleteBtn.setText("Delete");
        deleteBtn.setBackground(new Color(231, 76, 60)); // Red
        deleteBtn.setToolTipText("Delete value from BST");
        
        findBtn = new Button();
        findBtn.setText("Find");
        findBtn.setBackground(new Color(52, 152, 219)); // Blue
        findBtn.setToolTipText("Search for value in BST");
        
        clearBtn = new Button();
        clearBtn.setText("Clear");
        clearBtn.setBackground(new Color(149, 165, 166)); // Gray
        clearBtn.setToolTipText("Clear entire BST");
        
        // Animation controls with enhanced styling
        playBtn = new Button();
        playBtn.setText("▶ Play");
        playBtn.setBackground(new Color(46, 204, 113)); // Green
        playBtn.setToolTipText("Play animation sequence");
        
        pauseBtn = new Button();
        pauseBtn.setText("⏸ Pause");
        pauseBtn.setBackground(new Color(241, 196, 15)); // Yellow
        pauseBtn.setToolTipText("Pause current animation");
        
        stepBtn = new Button();
        stepBtn.setText("⏭ Step");
        stepBtn.setBackground(new Color(52, 152, 219)); // Blue
        stepBtn.setToolTipText("Execute next animation step");
        
        resetBtn = new Button();
        resetBtn.setText("🔄 Reset");
        resetBtn.setBackground(new Color(149, 165, 166)); // Gray
        resetBtn.setToolTipText("Reset animation to beginning");
        
        // Download/Export button
        downloadBtn = new Button();
        downloadBtn.setText("📁 Download Analysis");
        downloadBtn.setBackground(new Color(155, 89, 182)); // Purple
        downloadBtn.setToolTipText("Download detailed analysis report");
        
        // Speed control with labels
        speedSlider = new JSlider(100, 3000, 1000);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(Color.WHITE);
        speedSlider.setToolTipText("Adjust animation speed");
        
        speedLabel = new JLabel("Speed:");
        speedLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        // Traversal options
        traversalCombo = new JComboBox<>(new String[]{
            "In-Order", "Pre-Order", "Post-Order"
        });
        traversalCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        traversalCombo.setToolTipText("Select traversal type");
        
        // Animation progress bar
        animationProgress = new JProgressBar(0, 100);
        animationProgress.setStringPainted(true);
        animationProgress.setString("Ready");
        animationProgress.setBackground(Color.WHITE);
        animationProgress.setForeground(new Color(52, 152, 219));
        
        // Status label with enhanced styling
        statusLabel = new JLabel("Enter a number and select an operation");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
    }
    
    private void setupLayout() {
        setLayout(new MigLayout("fillx, wrap 6", "[grow][grow][grow][grow][grow][grow]", "[]5[]5[]5[]"));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
            "BST Controls & Animation",
            0, 0, new Font("SansSerif", Font.BOLD, 14),
            new Color(52, 73, 94)
        ));
        
        // Input row
        add(new JLabel("Value:"), "");
        add(inputField, "growx");
        add(insertBtn, "growx");
        add(deleteBtn, "growx");
        add(findBtn, "growx");
        add(clearBtn, "growx");
        
        // Animation controls row
        add(new JLabel("Animation:"), "");
        add(playBtn, "growx");
        add(pauseBtn, "growx");
        add(stepBtn, "growx");
        add(resetBtn, "growx");
        add(traversalCombo, "growx");
        
        // Speed control and download row
        add(speedLabel, "");
        add(speedSlider, "span 4, growx");
        add(downloadBtn, "growx");
        
        // Progress and status row
        add(new JLabel("Progress:"), "");
        add(animationProgress, "span 5, growx");
        
        // Status row
        add(statusLabel, "span 6, growx, center");
    }
    
    private void setupListeners() {
        insertBtn.addActionListener(e -> insertValue());
        deleteBtn.addActionListener(e -> deleteValue());
        findBtn.addActionListener(e -> findValue());
        clearBtn.addActionListener(e -> clearTree());
        
        playBtn.addActionListener(e -> animationEngine.play());
        pauseBtn.addActionListener(e -> animationEngine.pause());
        stepBtn.addActionListener(e -> animationEngine.nextStep());
        resetBtn.addActionListener(e -> animationEngine.reset());
        
        // Enhanced download functionality
        downloadBtn.addActionListener(e -> downloadAnalysis());
        
        speedSlider.addChangeListener(e -> {
            int speed = 3100 - speedSlider.getValue(); // Invert so right = faster
            animationEngine.setSpeed(speed);
        });
        
        traversalCombo.addActionListener(e -> performTraversal());
        
        // Enhanced animation engine listener with progress tracking
        animationEngine.addListener(new AnimationEngine.AnimationListener() {
            @Override
            public void onStepChanged(int currentStep, int totalSteps) {
                updateStatus("Step " + (currentStep + 1) + " of " + totalSteps);
                if (totalSteps > 0) {
                    int progress = (int) ((double) (currentStep + 1) / totalSteps * 100);
                    animationProgress.setValue(progress);
                    animationProgress.setString("Step " + (currentStep + 1) + "/" + totalSteps + " (" + progress + "%)");
                }
            }
            
            @Override
            public void onPlayStateChanged(boolean isPlaying) {
                playBtn.setEnabled(!isPlaying);
                pauseBtn.setEnabled(isPlaying);
                stepBtn.setEnabled(!isPlaying);
                
                // Disable operation buttons during animation for safety
                insertBtn.setEnabled(!isPlaying);
                deleteBtn.setEnabled(!isPlaying);
                findBtn.setEnabled(!isPlaying);
                clearBtn.setEnabled(!isPlaying);
                traversalCombo.setEnabled(!isPlaying);
                
                if (isPlaying) {
                    animationProgress.setString("Playing...");
                }
            }
            
            @Override
            public void onAnimationComplete() {
                updateStatus("Animation complete");
                animationProgress.setValue(100);
                animationProgress.setString("Complete");
            }
            
            @Override
            public void onReset() {
                updateStatus("Animation reset");
                animationProgress.setValue(0);
                animationProgress.setString("Ready");
            }
        });
        
        // Input field enter key
        inputField.addActionListener(e -> insertValue());
        
        // Add input validation
        inputField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '-' && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    evt.consume(); // Ignore non-numeric input
                }
            }
        });
    }
    
    private void insertValue() {
        try {
            int value = Integer.parseInt(inputField.getText().trim());
            if (value < -999 || value > 999) {
                updateStatus("Please enter a value between -999 and 999");
                return;
            }
            visualizer.insertValue(value);
            inputField.setText("");
            updateStatus("Initiating insertion of " + value);
        } catch (NumberFormatException e) {
            updateStatus("Please enter a valid integer");
            inputField.selectAll();
        }
    }
    
    private void deleteValue() {
        try {
            int value = Integer.parseInt(inputField.getText().trim());
            visualizer.deleteValue(value);
            inputField.setText("");
            updateStatus("Initiating deletion of " + value);
        } catch (NumberFormatException e) {
            updateStatus("Please enter a valid integer");
            inputField.selectAll();
        }
    }
    
    private void findValue() {
        try {
            int value = Integer.parseInt(inputField.getText().trim());
            boolean found = visualizer.findValue(value);
            updateStatus("Searching for " + value + " - " + (found ? "Found" : "Not found"));
        } catch (NumberFormatException e) {
            updateStatus("Please enter a valid integer");
            inputField.selectAll();
        }
    }
    
    private void clearTree() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear the entire tree?\nThis action cannot be undone.",
            "Clear Tree Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            visualizer.clear();
            animationEngine.clearSteps();
            animationProgress.setValue(0);
            animationProgress.setString("Tree cleared");
            updateStatus("Tree cleared successfully");
        }
    }
    
    private void performTraversal() {
        String traversalType = (String) traversalCombo.getSelectedItem();
        if (visualizer.getBSTAlgorithm().getRoot() == null) {
            updateStatus("Tree is empty - cannot perform traversal");
            return;
        }
        visualizer.performTraversal(traversalType);
        updateStatus("Initiating " + traversalType + " traversal");
    }
    
    private void downloadAnalysis() {
        try {
            // Check if tree has content
            if (visualizer.getBSTAlgorithm().getRoot() == null) {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "The BST is currently empty.\nWould you like to load a demo tree first?",
                    "Empty Tree",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    loadDemo();
                    // Wait a moment for the tree to render
                    SwingUtilities.invokeLater(() -> {
                        PDFExporter.exportToPDF(visualizer, "BST", 
                            "Binary Search Tree interactive learning visualization with comprehensive analysis and explanations.");
                    });
                } else {
                    return;
                }
            } else {
                PDFExporter.exportToPDF(visualizer, "BST", 
                    "Binary Search Tree interactive learning visualization with comprehensive analysis and explanations.");
            }
            
            updateStatus("Analysis report generation initiated");
            
        } catch (Exception e) {
            updateStatus("Failed to generate analysis report: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Failed to generate analysis report:\n" + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void updateStatus(String message) {
        statusLabel.setText(message);
        // Auto-clear status after 5 seconds
        Timer timer = new Timer(5000, e -> {
            if (statusLabel.getText().equals(message)) {
                statusLabel.setText("Ready for operation");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    // Enhanced demo with more comprehensive tree
    public void loadDemo() {
        visualizer.clear();
        
        // Create a more interesting demo tree
        int[] demoValues = {50, 30, 70, 20, 40, 60, 80, 10, 25, 35, 45, 55, 65, 75, 90};
        
        // Show progress dialog for demo loading
        JDialog progressDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
            "Loading Demo Tree", true);
        JProgressBar progressBar = new JProgressBar(0, demoValues.length);
        progressBar.setStringPainted(true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);
        
        // Load demo values with a small delay for visual effect
        Timer demoTimer = new Timer(200, null);
        final int[] index = {0};
        
        demoTimer.addActionListener(e -> {
            if (index[0] < demoValues.length) {
                visualizer.insertValueInstant(demoValues[index[0]]);
                progressBar.setValue(index[0] + 1);
                progressBar.setString("Inserting " + demoValues[index[0]] + "...");
                index[0]++;
            } else {
                demoTimer.stop();
                progressDialog.dispose();
                updateStatus("Demo tree loaded with " + demoValues.length + " nodes");
                animationProgress.setString("Demo loaded");
            }
        });
        
        // Show progress dialog and start loading
        SwingUtilities.invokeLater(() -> {
            progressDialog.setVisible(true);
        });
        demoTimer.start();
    }
}
