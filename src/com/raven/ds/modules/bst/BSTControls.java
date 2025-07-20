package com.raven.ds.modules.bst;

import com.raven.ds.core.AnimationEngine;
import com.raven.swing.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Control panel for Binary Search Tree operations
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
    private JSlider speedSlider;
    private JLabel statusLabel;
    private JComboBox<String> traversalCombo;
    
    public BSTControls(BSTPanel visualizer, AnimationEngine animationEngine) {
        this.visualizer = visualizer;
        this.animationEngine = animationEngine;
        
        initComponents();
        setupLayout();
        setupListeners();
    }
    
    private void initComponents() {
        // Input field
        inputField = new JTextField(10);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Operation buttons
        insertBtn = new Button();
        insertBtn.setText("Insert");
        insertBtn.setBackground(new Color(7, 164, 121)); // Raven green
        
        deleteBtn = new Button();
        deleteBtn.setText("Delete");
        deleteBtn.setBackground(new Color(220, 53, 69)); // Red
        
        findBtn = new Button();
        findBtn.setText("Find");
        findBtn.setBackground(new Color(0, 123, 255)); // Blue
        
        clearBtn = new Button();
        clearBtn.setText("Clear");
        clearBtn.setBackground(new Color(108, 117, 125)); // Gray
        
        // Animation controls
        playBtn = new Button();
        playBtn.setText("▶ Play");
        playBtn.setBackground(new Color(40, 167, 69)); // Green
        
        pauseBtn = new Button();
        pauseBtn.setText("⏸ Pause");
        pauseBtn.setBackground(new Color(255, 193, 7)); // Yellow
        
        stepBtn = new Button();
        stepBtn.setText("⏭ Step");
        stepBtn.setBackground(new Color(23, 162, 184)); // Cyan
        
        resetBtn = new Button();
        resetBtn.setText("🔄 Reset");
        resetBtn.setBackground(new Color(108, 117, 125)); // Gray
        
        // Speed control
        speedSlider = new JSlider(100, 3000, 1000);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        // Traversal options
        traversalCombo = new JComboBox<>(new String[]{
            "In-Order", "Pre-Order", "Post-Order"
        });
        
        // Status label
        statusLabel = new JLabel("Enter a number and select an operation");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }
    
    private void setupLayout() {
        setLayout(new MigLayout("fillx, wrap 6", "[grow][grow][grow][grow][grow][grow]", "[]10[]10[]"));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("BST Controls"));
        
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
        
        // Speed control row
        add(new JLabel("Speed:"), "");
        add(speedSlider, "span 4, growx");
        add(new JLabel("Fast"), "");
        
        // Status row
        add(statusLabel, "span 6, growx");
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
        
        speedSlider.addChangeListener(e -> {
            int speed = 3100 - speedSlider.getValue(); // Invert so right = faster
            animationEngine.setSpeed(speed);
        });
        
        traversalCombo.addActionListener(e -> performTraversal());
        
        // Animation engine listener
        animationEngine.addListener(new AnimationEngine.AnimationListener() {
            @Override
            public void onStepChanged(int currentStep, int totalSteps) {
                updateStatus("Step " + (currentStep + 1) + " of " + totalSteps);
            }
            
            @Override
            public void onPlayStateChanged(boolean isPlaying) {
                playBtn.setEnabled(!isPlaying);
                pauseBtn.setEnabled(isPlaying);
            }
            
            @Override
            public void onAnimationComplete() {
                updateStatus("Animation complete");
            }
            
            @Override
            public void onReset() {
                updateStatus("Animation reset");
            }
        });
        
        // Input field enter key
        inputField.addActionListener(e -> insertValue());
    }
    
    private void insertValue() {
        try {
            int value = Integer.parseInt(inputField.getText().trim());
            visualizer.insertValue(value);
            inputField.setText("");
            updateStatus("Inserted: " + value);
        } catch (NumberFormatException e) {
            updateStatus("Please enter a valid number");
        }
    }
    
    private void deleteValue() {
        try {
            int value = Integer.parseInt(inputField.getText().trim());
            visualizer.deleteValue(value);
            inputField.setText("");
            updateStatus("Deleted: " + value);
        } catch (NumberFormatException e) {
            updateStatus("Please enter a valid number");
        }
    }
    
    private void findValue() {
        try {
            int value = Integer.parseInt(inputField.getText().trim());
            boolean found = visualizer.findValue(value);
            updateStatus("Find " + value + ": " + (found ? "Found" : "Not found"));
        } catch (NumberFormatException e) {
            updateStatus("Please enter a valid number");
        }
    }
    
    private void clearTree() {
        visualizer.clear();
        animationEngine.clearSteps();
        updateStatus("Tree cleared");
    }
    
    private void performTraversal() {
        String traversalType = (String) traversalCombo.getSelectedItem();
        visualizer.performTraversal(traversalType);
        updateStatus("Performing " + traversalType + " traversal");
    }
    
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    // Add some preset values for demo
    public void loadDemo() {
        visualizer.clear();
        int[] demoValues = {50, 30, 70, 20, 40, 60, 80};
        for (int value : demoValues) {
            visualizer.insertValueInstant(value);
        }
        updateStatus("Demo tree loaded with values: 50, 30, 70, 20, 40, 60, 80");
    }
}
