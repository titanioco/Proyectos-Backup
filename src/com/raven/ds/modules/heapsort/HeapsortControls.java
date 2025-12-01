package com.raven.ds.modules.heapsort;

import com.raven.ds.core.AnimationEngine;
import com.raven.swing.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Control panel for Heapsort operations
 */
public class HeapsortControls extends JPanel {
    private HeapsortPanel visualizer;
    private AnimationEngine animationEngine;
    
    private JTextField inputField;
    private Button loadBtn;
    private Button sortBtn;
    private Button sampleBtn;
    private Button playBtn;
    private Button pauseBtn;
    private Button stepBtn;
    private Button resetBtn;
    private JSlider speedSlider;
    private JLabel statusLabel;
    private JLabel stepLabel;
    private JProgressBar animationProgress;
    
    public HeapsortControls(HeapsortPanel visualizer, AnimationEngine animationEngine) {
        this.visualizer = visualizer;
        this.animationEngine = animationEngine;
        
        initComponents();
        setupLayout();
        setupListeners();
    }
    
    private void initComponents() {
        // Input fields
        inputField = new JTextField(20);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        inputField.setToolTipText("Example: 12, 11, 13, 5, 6");
        
        // Operation buttons
        loadBtn = new Button();
        loadBtn.setText("Load");
        loadBtn.setBackground(new Color(52, 152, 219)); // Blue
        loadBtn.setToolTipText("Load data from input field");
        
        sortBtn = new Button();
        sortBtn.setText("Sort");
        sortBtn.setBackground(new Color(39, 174, 96)); // Green
        sortBtn.setToolTipText("Start Heapsort algorithm");
        
        sampleBtn = new Button();
        sampleBtn.setText("Sample");
        sampleBtn.setBackground(new Color(155, 89, 182)); // Purple
        sampleBtn.setToolTipText("Load sample data");
        
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
        statusLabel = new JLabel("Ready to sort");
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
            "Heapsort Controls",
            0, 0, new Font("SansSerif", Font.BOLD, 14),
            new Color(52, 73, 94)
        ));
    }
    
    private void setupLayout() {
        setLayout(new MigLayout("fillx, wrap 6", "[grow][grow][grow][grow][grow][grow]", "[]5[]5[]5[]"));
        
        // Input row
        add(new JLabel("Data:"), "");
        add(inputField, "span 3, growx");
        add(loadBtn, "growx");
        add(sortBtn, "growx");
        
        // Animation controls row
        add(new JLabel("Animation:"), "");
        add(playBtn, "growx");
        add(pauseBtn, "growx");
        add(stepBtn, "growx");
        add(resetBtn, "growx");
        add(sampleBtn, "growx");
        
        // Speed control row
        add(new JLabel("Speed:"), "");
        add(speedSlider, "span 5, growx");
        
        // Progress row
        add(new JLabel("Progress:"), "");
        add(animationProgress, "span 5, growx");
        
        // Status row
        add(statusLabel, "span 4, growx");
        add(stepLabel, "span 2, right");
    }
    
    private void setupListeners() {
        loadBtn.addActionListener(e -> {
            String input = inputField.getText().trim();
            if (input.isEmpty()) return;
            
            try {
                String[] parts = input.split("[,\\s]+");
                int[] values = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    values[i] = Integer.parseInt(parts[i].trim());
                }
                
                visualizer.loadData(values);
                statusLabel.setText("Data loaded. Click 'Start Sort' to begin.");
                updateAnimationControls();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid input format. Use comma separated numbers.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        sortBtn.addActionListener(e -> {
            visualizer.sort();
            statusLabel.setText("Sorting started...");
            updateAnimationControls();
        });
        
        sampleBtn.addActionListener(e -> {
            visualizer.loadSample();
            statusLabel.setText("Sample data loaded");
            updateAnimationControls();
        });
        
        // Animation controls
        playBtn.addActionListener(e -> {
            animationEngine.play();
            statusLabel.setText("Sorting in progress...");
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
        
        // Animation engine listeners
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
                
                loadBtn.setEnabled(!isPlaying);
                sortBtn.setEnabled(!isPlaying);
                sampleBtn.setEnabled(!isPlaying);
            }
            
            @Override
            public void onAnimationComplete() {
                statusLabel.setText("Sorting completed!");
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
