package com.raven.ds.modules.heap;

import com.raven.ds.core.AnimationEngine;
import com.raven.swing.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Control panel for Binary Heap operations
 */
public class BinaryHeapControls extends JPanel {
    private BinaryHeapPanel visualizer;
    private AnimationEngine animationEngine;
    
    private JTextField inputField;
    private Button insertBtn;
    private Button extractBtn;
    private Button clearBtn;
    private Button loadSampleBtn;
    private Button playBtn;
    private Button pauseBtn;
    private Button stepBtn;
    private Button resetBtn;
    private JSlider speedSlider;
    private JLabel statusLabel;
    private JLabel stepLabel;
    private JCheckBox maxHeapCheckBox;
    private JTextField buildHeapField;
    private Button buildHeapBtn;
    
    public BinaryHeapControls(BinaryHeapPanel visualizer, AnimationEngine animationEngine) {
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
        inputField.setBorder(BorderFactory.createTitledBorder("Value"));
        
        // Operation buttons
        insertBtn = new Button();
        insertBtn.setText("Insert");
        insertBtn.setBackground(new Color(7, 164, 121)); // Raven green
        
        extractBtn = new Button();
        extractBtn.setText("Extract Root");
        extractBtn.setBackground(new Color(231, 76, 60)); // Red
        
        clearBtn = new Button();
        clearBtn.setText("Clear");
        clearBtn.setBackground(new Color(149, 165, 166)); // Gray
        
        loadSampleBtn = new Button();
        loadSampleBtn.setText("Load Sample");
        loadSampleBtn.setBackground(new Color(155, 89, 182)); // Purple
        
        // Animation control buttons
        playBtn = new Button();
        playBtn.setText("Play");
        playBtn.setBackground(new Color(46, 204, 113)); // Green
        
        pauseBtn = new Button();
        pauseBtn.setText("Pause");
        pauseBtn.setBackground(new Color(230, 126, 34)); // Orange
        
        stepBtn = new Button();
        stepBtn.setText("Step");
        stepBtn.setBackground(new Color(52, 73, 94)); // Dark blue
        
        resetBtn = new Button();
        resetBtn.setText("Reset");
        resetBtn.setBackground(new Color(149, 165, 166)); // Gray
        
        // Speed control
        speedSlider = new JSlider(50, 3000, 1000);
        speedSlider.setInverted(true);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        // Heap type control
        maxHeapCheckBox = new JCheckBox("Max Heap", true);
        maxHeapCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // Build heap from array
        buildHeapField = new JTextField(15);
        buildHeapField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        buildHeapField.setBorder(BorderFactory.createTitledBorder("Array (comma-separated)"));
        buildHeapField.setText("10,20,15,30,40");
        
        buildHeapBtn = new Button();
        buildHeapBtn.setText("Build Heap");
        buildHeapBtn.setBackground(new Color(52, 152, 219)); // Blue
        
        // Status labels
        statusLabel = new JLabel("Ready for heap operations");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        stepLabel = new JLabel("Step: 0/0");
        stepLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Binary Heap Controls"));
    }
    
    private void setupLayout() {
        setLayout(new MigLayout("", "[][grow][]", "[]5[]5[]5[]"));
        
        // Input row
        add(new JLabel("Insert:"), "cell 0 0");
        add(inputField, "cell 1 0");
        add(insertBtn, "cell 1 0");
        add(extractBtn, "cell 1 0");
        add(clearBtn, "cell 1 0");
        add(loadSampleBtn, "cell 2 0");
        
        // Build heap row
        add(new JLabel("Build:"), "cell 0 1");
        add(buildHeapField, "cell 1 1");
        add(buildHeapBtn, "cell 1 1");
        add(maxHeapCheckBox, "cell 2 1");
        
        // Animation controls row
        add(new JLabel("Animation:"), "cell 0 2");
        add(playBtn, "cell 1 2");
        add(pauseBtn, "cell 1 2");
        add(stepBtn, "cell 1 2");
        add(resetBtn, "cell 1 2");
        add(new JLabel("Speed:"), "cell 2 2");
        add(speedSlider, "cell 2 2, w 120!");
        
        // Status row
        add(statusLabel, "cell 0 3, span 2");
        add(stepLabel, "cell 2 3");
    }
    
    private void setupListeners() {
        insertBtn.addActionListener(e -> {
            try {
                int value = Integer.parseInt(inputField.getText().trim());
                visualizer.insert(value);
                statusLabel.setText("Inserting: " + value);
                updateAnimationControls();
                inputField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid integer", 
                    "Input Error", 
                    JOptionPane.ERROR_MESSAGE);
                inputField.selectAll();
            }
        });
        
        extractBtn.addActionListener(e -> {
            if (visualizer.getHeap().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Heap is empty - nothing to extract", 
                    "Empty Heap", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            visualizer.extractRoot();
            statusLabel.setText("Extracting root");
            updateAnimationControls();
        });
        
        clearBtn.addActionListener(e -> {
            visualizer.clear();
            animationEngine.clearSteps();
            statusLabel.setText("Heap cleared");
            updateAnimationControls();
        });
        
        loadSampleBtn.addActionListener(e -> {
            visualizer.loadSample();
            statusLabel.setText("Sample data loaded");
            updateAnimationControls();
        });
        
        buildHeapBtn.addActionListener(e -> {
            try {
                String input = buildHeapField.getText().trim();
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Please enter comma-separated values", 
                        "Input Required", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String[] parts = input.split(",");
                int[] values = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    values[i] = Integer.parseInt(parts[i].trim());
                }
                
                visualizer.buildHeap(values);
                statusLabel.setText("Building heap from array");
                updateAnimationControls();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter valid comma-separated integers", 
                    "Input Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        maxHeapCheckBox.addActionListener(e -> {
            visualizer.setMaxHeap(maxHeapCheckBox.isSelected());
            statusLabel.setText("Switched to " + (maxHeapCheckBox.isSelected() ? "Max" : "Min") + " Heap");
        });
        
        // Animation controls
        playBtn.addActionListener(e -> {
            animationEngine.play();
            statusLabel.setText("Playing animation...");
        });
        
        pauseBtn.addActionListener(e -> {
            animationEngine.pause();
            statusLabel.setText("Animation paused");
        });
        
        stepBtn.addActionListener(e -> {
            animationEngine.nextStep();
            statusLabel.setText("Step executed");
        });
        
        resetBtn.addActionListener(e -> {
            animationEngine.reset();
            statusLabel.setText("Animation reset");
            updateAnimationControls();
        });
        
        speedSlider.addChangeListener(e -> {
            animationEngine.setSpeed(speedSlider.getValue());
        });
        
        // Enter key support for input fields
        inputField.addActionListener(e -> insertBtn.doClick());
        buildHeapField.addActionListener(e -> buildHeapBtn.doClick());
        
        // Animation engine listeners
        animationEngine.addListener(new AnimationEngine.AnimationListener() {
            @Override
            public void onStepChanged(int currentStep, int totalSteps) {
                stepLabel.setText("Step: " + currentStep + "/" + totalSteps);
                visualizer.repaint();
            }
            
            @Override
            public void onPlayStateChanged(boolean isPlaying) {
                playBtn.setEnabled(!isPlaying);
                pauseBtn.setEnabled(isPlaying);
            }
            
            @Override
            public void onAnimationComplete() {
                statusLabel.setText("Operation completed!");
            }
            
            @Override
            public void onReset() {
                stepLabel.setText("Step: 0/0");
                visualizer.repaint();
            }
        });
    }
    
    private void updateAnimationControls() {
        boolean hasSteps = animationEngine.getTotalSteps() > 0;
        playBtn.setEnabled(hasSteps && !animationEngine.isPlaying());
        pauseBtn.setEnabled(hasSteps && animationEngine.isPlaying());
        stepBtn.setEnabled(hasSteps);
        resetBtn.setEnabled(hasSteps);
    }
    
    public void loadDemo() {
        visualizer.loadSample();
        statusLabel.setText("Demo data loaded - try different operations!");
    }
}
