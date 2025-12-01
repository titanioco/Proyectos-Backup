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
    
    private JTextField valueField;
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
    private JProgressBar animationProgress;
    private JComboBox<String> heapTypeCombo;
    
    public BinaryHeapControls(BinaryHeapPanel visualizer, AnimationEngine animationEngine) {
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
        valueField.setToolTipText("Enter value to insert");
        
        // Operation buttons
        insertBtn = new Button();
        insertBtn.setText("Insert");
        insertBtn.setBackground(new Color(39, 174, 96)); // Green
        insertBtn.setToolTipText("Insert value into Heap");
        
        extractBtn = new Button();
        extractBtn.setText("Extract");
        extractBtn.setBackground(new Color(231, 76, 60)); // Red
        extractBtn.setToolTipText("Extract root element");
        
        clearBtn = new Button();
        clearBtn.setText("Clear");
        clearBtn.setBackground(new Color(149, 165, 166)); // Gray
        clearBtn.setToolTipText("Clear entire Heap");
        
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
        
        // Heap type selector
        heapTypeCombo = new JComboBox<>(new String[]{"Max Heap", "Min Heap"});
        heapTypeCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        heapTypeCombo.setToolTipText("Select heap type");
        
        // Status labels
        statusLabel = new JLabel("Ready for heap operations");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
        
        stepLabel = new JLabel("Step: 0/0");
        stepLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // Animation progress bar
        animationProgress = new JProgressBar(0, 100);
        animationProgress.setStringPainted(true);
        animationProgress.setString("Ready");
        animationProgress.setBackground(Color.WHITE);
        animationProgress.setForeground(new Color(7, 164, 121));
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 2),
            "Binary Heap Controls",
            0, 0, new Font("SansSerif", Font.BOLD, 14),
            new Color(52, 73, 94)
        ));
    }
    
    private void setupLayout() {
        setLayout(new MigLayout("fillx, wrap 6", "[grow][grow][grow][grow][grow][grow]", "[]5[]5[]5[]"));
        
        // Input row
        add(new JLabel("Value:"), "");
        add(valueField, "growx");
        add(new JLabel("Type:"), "");
        add(heapTypeCombo, "growx");
        add(insertBtn, "growx");
        add(extractBtn, "growx");
        
        // Animation controls row
        add(new JLabel("Animation:"), "");
        add(playBtn, "growx");
        add(pauseBtn, "growx");
        add(stepBtn, "growx");
        add(resetBtn, "growx");
        
        // Speed control row
        add(clearBtn, "growx");
        add(loadSampleBtn, "growx");
        add(new JLabel("Speed:"), "right");
        add(speedSlider, "span 3, growx");
        
        // Progress row
        add(new JLabel("Progress:"), "");
        add(animationProgress, "span 5, growx");
        
        // Status row
        add(statusLabel, "span 4, growx");
        add(stepLabel, "span 2, right");
    }
    
    private void setupListeners() {
        insertBtn.addActionListener(e -> {
            String valueStr = valueField.getText().trim();
            if (valueStr.isEmpty()) return;
            
            try {
                int value = Integer.parseInt(valueStr);
                visualizer.insert(value);
                statusLabel.setText("Inserting: " + value);
                updateAnimationControls();
                valueField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid integer");
            }
        });
        
        extractBtn.addActionListener(e -> {
            if (visualizer.getHeap().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Heap is empty");
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
            animationProgress.setValue(0);
            animationProgress.setString("Ready");
            updateAnimationControls();
        });
        
        loadSampleBtn.addActionListener(e -> {
            visualizer.loadSample();
            statusLabel.setText("Sample loaded");
            updateAnimationControls();
        });
        
        heapTypeCombo.addActionListener(e -> {
            String selected = (String) heapTypeCombo.getSelectedItem();
            boolean isMaxHeap = "Max Heap".equals(selected);
            visualizer.setMaxHeap(isMaxHeap);
            statusLabel.setText("Switched to " + selected);
        });
        
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
        
        valueField.addActionListener(e -> insertBtn.doClick());
        
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
                
                insertBtn.setEnabled(!isPlaying);
                extractBtn.setEnabled(!isPlaying);
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
