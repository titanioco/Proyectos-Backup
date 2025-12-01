package com.raven.ds.modules.dynamicarray;

import com.raven.ds.core.AnimationEngine;
import com.raven.swing.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Control panel for Dynamic Array operations
 */
public class DynamicArrayControls extends JPanel {
    private DynamicArrayPanel visualizer;
    private AnimationEngine animationEngine;
    
    private JTextField valueField;
    private JTextField indexField;
    private Button addBtn;
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
    private JProgressBar animationProgress;
    
    public DynamicArrayControls(DynamicArrayPanel visualizer, AnimationEngine animationEngine) {
        this.visualizer = visualizer;
        this.animationEngine = animationEngine;
        
        initComponents();
        setupLayout();
        setupListeners();
    }
    
    private void initComponents() {
        // Input fields
        valueField = new JTextField(10);
        valueField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        valueField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        valueField.setToolTipText("Enter value to add");
        
        indexField = new JTextField(5);
        indexField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        indexField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        indexField.setToolTipText("Enter index to remove");
        
        // Operation buttons
        addBtn = new Button();
        addBtn.setText("Add");
        addBtn.setBackground(new Color(39, 174, 96)); // Green
        addBtn.setToolTipText("Add value to array");
        
        removeBtn = new Button();
        removeBtn.setText("Remove");
        removeBtn.setBackground(new Color(231, 76, 60)); // Red
        removeBtn.setToolTipText("Remove value at index");
        
        clearBtn = new Button();
        clearBtn.setText("Clear");
        clearBtn.setBackground(new Color(149, 165, 166)); // Gray
        clearBtn.setToolTipText("Clear entire array");
        
        loadSampleBtn = new Button();
        loadSampleBtn.setText("Sample");
        loadSampleBtn.setBackground(new Color(155, 89, 182)); // Purple
        loadSampleBtn.setToolTipText("Load sample data");
        
        // Animation control buttons
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
        
        // Speed control
        speedSlider = new JSlider(50, 3000, 1000);
        speedSlider.setInverted(true);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(Color.WHITE);
        
        // Status labels
        statusLabel = new JLabel("Ready for array operations");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
        
        stepLabel = new JLabel("Step: 0/0");
        stepLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // Animation progress bar
        animationProgress = new JProgressBar(0, 100);
        animationProgress.setStringPainted(true);
        animationProgress.setString("Ready");
        animationProgress.setBackground(Color.WHITE);
        animationProgress.setForeground(new Color(52, 152, 219));
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
            "Dynamic Array Controls",
            0, 0, new Font("SansSerif", Font.BOLD, 14),
            new Color(52, 73, 94)
        ));
    }
    
    private void setupLayout() {
        setLayout(new MigLayout("fillx, wrap 6", "[grow][grow][grow][grow][grow][grow]", "[]5[]5[]5[]"));
        
        // Input row
        add(new JLabel("Value:"), "");
        add(valueField, "growx");
        add(new JLabel("Index:"), "");
        add(indexField, "growx");
        add(addBtn, "growx");
        add(removeBtn, "growx");
        
        // Animation controls row
        add(new JLabel("Animation:"), "");
        add(playBtn, "growx");
        add(pauseBtn, "growx");
        add(stepBtn, "growx");
        add(resetBtn, "growx");
        add(clearBtn, "growx");
        
        // Speed control row
        add(loadSampleBtn, "growx");
        add(new JLabel("Speed:"), "right");
        add(speedSlider, "span 4, growx");
        
        // Progress row
        add(new JLabel("Progress:"), "");
        add(animationProgress, "span 5, growx");
        
        // Status row
        add(statusLabel, "span 4, growx");
        add(stepLabel, "span 2, right");
    }
    
    private void setupListeners() {
        addBtn.addActionListener(e -> {
            String valueStr = valueField.getText().trim();
            if (valueStr.isEmpty()) return;
            
            try {
                int value = Integer.parseInt(valueStr);
                visualizer.add(value);
                statusLabel.setText("Adding: " + value);
                updateAnimationControls();
                valueField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid integer");
            }
        });
        
        removeBtn.addActionListener(e -> {
            String indexStr = indexField.getText().trim();
            if (indexStr.isEmpty()) return;
            
            try {
                int index = Integer.parseInt(indexStr);
                if (index < 0 || index >= visualizer.getArraySize()) {
                     JOptionPane.showMessageDialog(this, "Index out of bounds");
                     return;
                }
                visualizer.remove(index);
                statusLabel.setText("Removing at index: " + index);
                updateAnimationControls();
                indexField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid index");
            }
        });
        
        clearBtn.addActionListener(e -> {
            visualizer.clear();
            animationEngine.clearSteps();
            statusLabel.setText("Array cleared");
            updateAnimationControls();
        });
        
        loadSampleBtn.addActionListener(e -> {
            visualizer.loadSample();
            statusLabel.setText("Sample loaded");
            updateAnimationControls();
        });
        
        // Animation controls
        playBtn.addActionListener(e -> {
            animationEngine.play();
            statusLabel.setText("Playing...");
        });
        
        pauseBtn.addActionListener(e -> {
            animationEngine.pause();
            statusLabel.setText("Paused");
        });
        
        stepBtn.addActionListener(e -> {
            animationEngine.nextStep();
            statusLabel.setText("Step executed");
        });
        
        resetBtn.addActionListener(e -> {
            animationEngine.reset();
            statusLabel.setText("Reset");
            updateAnimationControls();
        });
        
        speedSlider.addChangeListener(e -> {
            animationEngine.setSpeed(speedSlider.getValue());
        });
        
        valueField.addActionListener(e -> addBtn.doClick());
        
        animationEngine.addListener(new AnimationEngine.AnimationListener() {
            @Override
            public void onStepChanged(int currentStep, int totalSteps) {
                stepLabel.setText("Step: " + currentStep + "/" + totalSteps);
                if (totalSteps > 0) {
                    int progress = (int) ((double) currentStep / totalSteps * 100);
                    animationProgress.setValue(progress);
                    animationProgress.setString("Step " + currentStep + "/" + totalSteps + " (" + progress + "%)");
                }
                visualizer.repaint();
            }
            
            @Override
            public void onPlayStateChanged(boolean isPlaying) {
                playBtn.setEnabled(!isPlaying);
                pauseBtn.setEnabled(isPlaying);
                stepBtn.setEnabled(!isPlaying);
                
                addBtn.setEnabled(!isPlaying);
                removeBtn.setEnabled(!isPlaying);
                clearBtn.setEnabled(!isPlaying);
                loadSampleBtn.setEnabled(!isPlaying);
            }
            
            @Override
            public void onAnimationComplete() {
                statusLabel.setText("Operation completed!");
                animationProgress.setValue(100);
                animationProgress.setString("Complete");
            }
            
            @Override
            public void onReset() {
                stepLabel.setText("Step: 0/0");
                animationProgress.setValue(0);
                animationProgress.setString("Ready");
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
        statusLabel.setText("Demo data loaded");
    }
}
