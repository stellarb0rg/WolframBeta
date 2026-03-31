package com.graphing;

import com.graphing.ui.MainFrame;
import com.graphing.ui.Theme;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Main entry point for WolframBeta Graphing Calculator
 */
public class GraphingCalculatorApp {
    
    public static void main(String[] args) {
        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            Theme.apply();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
        }
        // Launch the main application on the EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
} 
