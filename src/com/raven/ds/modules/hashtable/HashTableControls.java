package com.raven.ds.modules.hashtable;

import com.raven.ds.core.AnimationEngine;
import com.raven.swing.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Control panel for Hash Table operations
 */
public class HashTableControls extends JPanel {
    private HashTablePanel visualizer;
    private AnimationEngine animationEngine;
    
    private JTextField keyField;
    private JTextField valueField;
    private Button insertBtn;
    private Button searchBtn;
    private Button removeBtn;
    private Button clearBtn;
    private Button loadSampleBtn;
    private Button playBtn;
    private Button pauseBtn;
    private Button stepBtn;
    private Button resetBtn;
    private JSlider speedSlider;
    private JLabel statusLabel;
    private JLabel stepLabel;
    private JComboBox<HashTableAlgorithm.HashFunction> hashFunctionCombo;
    private JSpinner capacitySpinner;
    private Button resizeBtn;
    
    public HashTableControls(HashTablePanel visualizer, AnimationEngine animationEngine) {
        this.visualizer = visualizer;
        this.animationEngine = animationEngine;
        
        initComponents();
        setupLayout();
        setupListeners();
    }
    
    private void initComponents() {
        // Input fields
        keyField = new JTextField(10);
        keyField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        keyField.setBorder(BorderFactory.createTitledBorder("Key"));
        
        valueField = new JTextField(10);
        valueField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        valueField.setBorder(BorderFactory.createTitledBorder("Value"));
        
        // Operation buttons
        insertBtn = new Button();
        insertBtn.setText("Insert");
        insertBtn.setBackground(new Color(7, 164, 121)); // Raven green
        
        searchBtn = new Button();
        searchBtn.setText("Search");
        searchBtn.setBackground(new Color(52, 152, 219)); // Blue
        
        removeBtn = new Button();
        removeBtn.setText("Remove");
        removeBtn.setBackground(new Color(231, 76, 60)); // Red
        
        clearBtn = new Button();
        clearBtn.setText("Clear All");
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
        
        // Hash function selector
        hashFunctionCombo = new JComboBox<>(HashTableAlgorithm.HashFunction.values());
        hashFunctionCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // Capacity control
        capacitySpinner = new JSpinner(new SpinnerNumberModel(16, 4, 64, 4));
        capacitySpinner.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        resizeBtn = new Button();
        resizeBtn.setText("Resize");
        resizeBtn.setBackground(new Color(241, 196, 15)); // Yellow
        
        // Status labels
        statusLabel = new JLabel("Ready for hash table operations");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        stepLabel = new JLabel("Step: 0/0");
        stepLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Hash Table Controls"));
    }
    
    private void setupLayout() {
        setLayout(new MigLayout("", "[][grow][]", "[]5[]5[]5[]"));
        
        // Input row
        add(new JLabel("Data:"), "cell 0 0");
        add(keyField, "cell 1 0");
        add(valueField, "cell 1 0");
        
        // Operation buttons row
        add(new JLabel("Operations:"), "cell 0 1");
        add(insertBtn, "cell 1 1");
        add(searchBtn, "cell 1 1");
        add(removeBtn, "cell 1 1");
        add(clearBtn, "cell 1 1");
        add(loadSampleBtn, "cell 2 1");
        
        // Animation controls row
        add(new JLabel("Animation:"), "cell 0 2");
        add(playBtn, "cell 1 2");
        add(pauseBtn, "cell 1 2");
        add(stepBtn, "cell 1 2");
        add(resetBtn, "cell 1 2");
        add(new JLabel("Speed:"), "cell 2 2");
        add(speedSlider, "cell 2 2, w 120!");
        
        // Configuration row
        add(new JLabel("Config:"), "cell 0 3");
        add(new JLabel("Hash Function:"), "cell 1 3");
        add(hashFunctionCombo, "cell 1 3");
        add(new JLabel("Capacity:"), "cell 1 3");
        add(capacitySpinner, "cell 1 3, w 60!");
        add(resizeBtn, "cell 1 3");
        
        // Status row
        add(statusLabel, "cell 0 4, span 2");
        add(stepLabel, "cell 2 4");
    }
    
    private void setupListeners() {
        insertBtn.addActionListener(e -> {
            String key = keyField.getText().trim();
            String value = valueField.getText().trim();
            
            if (key.isEmpty() || value.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter both key and value", 
                    "Input Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            visualizer.insert(key, value);
            statusLabel.setText("Inserting: " + key + " → " + value);
            updateAnimationControls();
            
            // Clear fields after successful insert
            keyField.setText("");
            valueField.setText("");
        });
        
        searchBtn.addActionListener(e -> {
            String key = keyField.getText().trim();
            
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a key to search", 
                    "Input Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            visualizer.search(key);
            statusLabel.setText("Searching for key: " + key);
            updateAnimationControls();
        });
        
        removeBtn.addActionListener(e -> {
            String key = keyField.getText().trim();
            
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a key to remove", 
                    "Input Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            visualizer.remove(key);
            statusLabel.setText("Removing key: " + key);
            updateAnimationControls();
            
            keyField.setText("");
        });
        
        clearBtn.addActionListener(e -> {
            visualizer.clear();
            animationEngine.clearSteps();
            statusLabel.setText("Hash table cleared");
            updateAnimationControls();
        });
        
        loadSampleBtn.addActionListener(e -> {
            visualizer.loadSample();
            statusLabel.setText("Sample data loaded");
            updateAnimationControls();
        });
        
        hashFunctionCombo.addActionListener(e -> {
            HashTableAlgorithm.HashFunction selected = 
                (HashTableAlgorithm.HashFunction) hashFunctionCombo.getSelectedItem();
            visualizer.setHashFunction(selected);
            statusLabel.setText("Hash function changed to: " + selected.name());
        });
        
        resizeBtn.addActionListener(e -> {
            int newCapacity = (Integer) capacitySpinner.getValue();
            visualizer.resize(newCapacity);
            statusLabel.setText("Resized to capacity: " + newCapacity);
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
        keyField.addActionListener(e -> {
            if (!valueField.getText().trim().isEmpty()) {
                insertBtn.doClick();
            } else {
                valueField.requestFocus();
            }
        });
        
        valueField.addActionListener(e -> insertBtn.doClick());
        
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
