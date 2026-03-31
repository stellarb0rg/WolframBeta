package com.graphing.ui;

import com.graphing.ui.Theme;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.plaf.basic.BasicSpinnerUI;

/**
 * Axis limits, zoom, reset controls
 */
public class ControlPanel extends JPanel {
    
    private final JSpinner minXSpinner;
    private final JSpinner maxXSpinner;
    private final JSpinner minYSpinner;
    private final JSpinner maxYSpinner;
    private final JButton applyButton;
    private final JButton resetButton;
    
    // New analysis buttons
    private final JButton intersectionButton;
    private final JButton integralButton;
    private final JButton derivativeButton;
    private final JButton extremaButton;
    private final JButton interactivePointsButton;
    
    private ControlPanelListener listener;
    
    public ControlPanel() {
        // Initialize components
        this.minXSpinner = new JSpinner(new SpinnerNumberModel(-10.0, -1e9, 1e9, 0.1));
        this.maxXSpinner = new JSpinner(new SpinnerNumberModel(10.0, -1e9, 1e9, 0.1));
        this.minYSpinner = new JSpinner(new SpinnerNumberModel(-10.0, -1e9, 1e9, 0.1));
        this.maxYSpinner = new JSpinner(new SpinnerNumberModel(10.0, -1e9, 1e9, 0.1));
        this.applyButton = new JButton("Apply");
        this.resetButton = new JButton("Reset");
        
        // Initialize new analysis buttons
        this.intersectionButton = new JButton("Find Intersections");
        this.integralButton = new JButton("Definite Integral");
        this.derivativeButton = new JButton("Derivative");
        this.extremaButton = new JButton("Find Extrema");
        this.interactivePointsButton = new JButton("Interactive Points");
        
        setupComponents();
        setupLayout();
        setupListeners();
    }
    
    private void setupComponents() {
        // Set default values
        minXSpinner.setValue(-10.0);
        maxXSpinner.setValue(10.0);
        minYSpinner.setValue(-10.0);
        maxYSpinner.setValue(10.0);
        
        // Set tooltips
        minXSpinner.setToolTipText("Minimum X value");
        maxXSpinner.setToolTipText("Maximum X value");
        minYSpinner.setToolTipText("Minimum Y value");
        maxYSpinner.setToolTipText("Maximum Y value");
        applyButton.setToolTipText("Apply new axis limits");
        resetButton.setToolTipText("Reset to default view");
        
        // Set tooltips for new buttons
        intersectionButton.setToolTipText("Find intersection points between functions");
        integralButton.setToolTipText("Calculate area under curve");
        derivativeButton.setToolTipText("Calculate derivative of selected function");
        extremaButton.setToolTipText("Find local maxima, minima, and inflection points");
        interactivePointsButton.setToolTipText("Click on curves to get exact coordinates");
        
        // Set colors and fonts
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setFont(Theme.font(13));
        
        applyButton.setBackground(Theme.ACCENT);
        applyButton.setForeground(Color.WHITE);
        resetButton.setBackground(Theme.DANGER_SOFT);
        resetButton.setForeground(Theme.DANGER.darker());
        intersectionButton.setBackground(Theme.SURFACE);
        intersectionButton.setForeground(Theme.TEXT);
        integralButton.setBackground(Theme.SURFACE);
        integralButton.setForeground(Theme.TEXT);
        derivativeButton.setBackground(Theme.SURFACE);
        derivativeButton.setForeground(Theme.TEXT);
        extremaButton.setBackground(Theme.SURFACE);
        extremaButton.setForeground(Theme.TEXT);
        interactivePointsButton.setBackground(Theme.SURFACE);
        interactivePointsButton.setForeground(Theme.TEXT);

        Dimension spinnerSize = new Dimension(40, 16); // Decreased width, same height
        minXSpinner.setPreferredSize(spinnerSize);
        maxXSpinner.setPreferredSize(spinnerSize);
        minYSpinner.setPreferredSize(spinnerSize);
        maxYSpinner.setPreferredSize(spinnerSize);

        applyButton.setFont(Theme.font(Font.BOLD, 12));
        resetButton.setFont(Theme.font(12));
        intersectionButton.setFont(Theme.font(12));
        integralButton.setFont(Theme.font(12));
        derivativeButton.setFont(Theme.font(12));
        extremaButton.setFont(Theme.font(12));
        interactivePointsButton.setFont(Theme.font(12));
        applyButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        resetButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        intersectionButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        integralButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        derivativeButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        extremaButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        interactivePointsButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        minXSpinner.setFont(Theme.font(13));
        maxXSpinner.setFont(Theme.font(13));
        minYSpinner.setFont(Theme.font(13));
        maxYSpinner.setFont(Theme.font(13));

        // Custom spinner button color: subtle light blue for arrow buttons only
        Color spinnerButtonColor = Theme.SURFACE_ALT;
        setSpinnerButtonColor(minXSpinner, spinnerButtonColor);
        setSpinnerButtonColor(maxXSpinner, spinnerButtonColor);
        setSpinnerButtonColor(minYSpinner, spinnerButtonColor);
        setSpinnerButtonColor(maxYSpinner, spinnerButtonColor);
    }

    // Utility to set spinner button color only
    private void setSpinnerButtonColor(JSpinner spinner, Color color) {
        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                Component c = super.createNextButton();
                if (c != null) c.setBackground(color);
                return c;
            }
            @Override
            protected Component createPreviousButton() {
                Component c = super.createPreviousButton();
                if (c != null) c.setBackground(color);
                return c;
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        // Axis limits panel (single horizontal strip, right-aligned buttons)
        JPanel axisPanel = new JPanel();
        axisPanel.setLayout(new BoxLayout(axisPanel, BoxLayout.X_AXIS));
        axisPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        axisPanel.setBackground(Theme.SURFACE);
        JLabel xMaxLabel = new JLabel("X-max:");
        xMaxLabel.setFont(Theme.font(12));
        JLabel xMinLabel = new JLabel("X-min:");
        xMinLabel.setFont(Theme.font(12));
        JLabel yMaxLabel = new JLabel("Y-max:");
        yMaxLabel.setFont(Theme.font(12));
        JLabel yMinLabel = new JLabel("Y-min:");
        yMinLabel.setFont(Theme.font(12));
        axisPanel.add(xMaxLabel);
        axisPanel.add(Box.createHorizontalStrut(2));
        axisPanel.add(maxXSpinner);
        axisPanel.add(Box.createHorizontalStrut(24));
        axisPanel.add(xMinLabel);
        axisPanel.add(Box.createHorizontalStrut(2));
        axisPanel.add(minXSpinner);
        axisPanel.add(Box.createHorizontalStrut(120)); // Increased padding for alignment with Find Extrema
        axisPanel.add(yMaxLabel);
        axisPanel.add(Box.createHorizontalStrut(2));
        axisPanel.add(maxYSpinner);
        axisPanel.add(Box.createHorizontalStrut(24));
        axisPanel.add(yMinLabel);
        axisPanel.add(Box.createHorizontalStrut(2));
        axisPanel.add(minYSpinner);
        axisPanel.add(Box.createHorizontalGlue());
        axisPanel.add(applyButton);
        axisPanel.add(Box.createHorizontalStrut(8));
        axisPanel.add(resetButton);
        // Analysis controls panel
        JPanel analysisPanel = new JPanel(new GridLayout(1, 4, 48, 0));
        analysisPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        analysisPanel.setBackground(Theme.SURFACE);
        intersectionButton.setPreferredSize(null);
        integralButton.setPreferredSize(null);
        derivativeButton.setPreferredSize(null);
        extremaButton.setPreferredSize(null);
        // Remove interactivePointsButton from analysisPanel and do not set its properties or listeners
        // analysisPanel.add(interactivePointsButton); // REMOVE THIS LINE
        analysisPanel.add(intersectionButton);
        analysisPanel.add(integralButton);
        analysisPanel.add(derivativeButton);
        analysisPanel.add(extremaButton);
        // Do not set tooltip, background, foreground, font, or border for interactivePointsButton
        // interactivePointsButton.setToolTipText(...);
        // interactivePointsButton.setBackground(...);
        // interactivePointsButton.setForeground(...);
        // interactivePointsButton.setFont(...);
        // interactivePointsButton.setBorder(...);
        // Do not add ActionListener for interactivePointsButton
        // interactivePointsButton.addActionListener(...);
        // Combine panels
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.SURFACE);
        topPanel.add(axisPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        add(analysisPanel, BorderLayout.CENTER);
    }
    
    private void setupListeners() {
        // Apply button
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyAxisLimits();
            }
        });
        
        // Reset button
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetView();
            }
        });
        
        // Analysis buttons
        intersectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onFindIntersections();
                }
            }
        });
        
        integralButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onDefiniteIntegral();
                }
            }
        });
        
        derivativeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onDerivative();
                }
            }
        });
        
        extremaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) {
                    listener.onFindExtrema();
                }
            }
        });
        
        // Enter/Change in spinners
        ChangeListener spinnerChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Optionally, auto-apply on change or keep manual apply
            }
        };
        minXSpinner.addChangeListener(spinnerChangeListener);
        maxXSpinner.addChangeListener(spinnerChangeListener);
        minYSpinner.addChangeListener(spinnerChangeListener);
        maxYSpinner.addChangeListener(spinnerChangeListener);
    }
    
    private void applyAxisLimits() {
        double minX = ((Number) minXSpinner.getValue()).doubleValue();
        double maxX = ((Number) maxXSpinner.getValue()).doubleValue();
        double minY = ((Number) minYSpinner.getValue()).doubleValue();
        double maxY = ((Number) maxYSpinner.getValue()).doubleValue();
        if (minX >= maxX || minY >= maxY) {
            JOptionPane.showMessageDialog(this, 
                "Invalid axis limits. Min values must be less than max values.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (listener != null) {
            listener.onAxisLimitsChanged(minX, maxX, minY, maxY);
        }
    }
    
    private void resetView() {
        minXSpinner.setValue(-10.0);
        maxXSpinner.setValue(10.0);
        minYSpinner.setValue(-10.0);
        maxYSpinner.setValue(10.0);
        
        if (listener != null) {
            listener.onResetView();
        }
    }
    
    /**
     * Update the axis limit fields with current values
     */
    public void updateAxisLimits(double minX, double maxX, double minY, double maxY) {
        minXSpinner.setValue(minX);
        maxXSpinner.setValue(maxX);
        minYSpinner.setValue(minY);
        maxYSpinner.setValue(maxY);
    }
    
    /**
     * Set the control panel listener
     */
    public void setControlPanelListener(ControlPanelListener listener) {
        this.listener = listener;
    }
    
    /**
     * Interface for control panel events
     */
    public interface ControlPanelListener {
        void onAxisLimitsChanged(double minX, double maxX, double minY, double maxY);
        void onResetView();
        void onFindIntersections();
        void onDefiniteIntegral();
        void onDerivative();
        void onFindExtrema();
        void onToggleInteractivePoints();
    }
} 
