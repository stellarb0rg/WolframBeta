package com.graphing.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages function colors for the graphing calculator
 */
public class ColorManager {
    
    private static final List<Color> DEFAULT_COLORS = List.of(
        darker(new Color(255, 0, 0), 0.7),      // Red
        darker(new Color(0, 0, 255), 0.7),      // Blue
        darker(new Color(0, 128, 0), 0.7),      // Green
        darker(new Color(255, 165, 0), 0.7),    // Orange
        darker(new Color(128, 0, 128), 0.7),    // Purple
        darker(new Color(255, 0, 255), 0.7),    // Magenta
        darker(new Color(0, 255, 255), 0.7),    // Cyan
        darker(new Color(255, 255, 0), 0.7),    // Yellow
        darker(new Color(128, 128, 128), 0.7),  // Gray
        darker(new Color(255, 192, 203), 0.7),  // Pink
        darker(new Color(165, 42, 42), 0.7),    // Brown
        darker(new Color(0, 255, 0), 0.7),      // Lime
        darker(new Color(255, 20, 147), 0.7),   // Deep Pink
        darker(new Color(30, 144, 255), 0.7),   // Dodger Blue
        darker(new Color(255, 69, 0), 0.7)      // Red Orange
    );
    
    private int currentColorIndex = 0;
    
    /**
     * Get the next available color
     */
    public Color getNextColor() {
        Color color = DEFAULT_COLORS.get(currentColorIndex % DEFAULT_COLORS.size());
        currentColorIndex++;
        return color;
    }
    
    /**
     * Reset the color index
     */
    public void resetColorIndex() {
        currentColorIndex = 0;
    }
    
    /**
     * Get a specific color by index
     */
    public Color getColor(int index) {
        return DEFAULT_COLORS.get(index % DEFAULT_COLORS.size());
    }
    
    /**
     * Get all available colors
     */
    public List<Color> getAllColors() {
        return new ArrayList<>(DEFAULT_COLORS);
    }
    
    /**
     * Generate a color with given RGB values
     */
    public static Color createColor(int r, int g, int b) {
        return new Color(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        );
    }
    
    /**
     * Create a darker version of a color
     */
    public static Color darker(Color color, double factor) {
        return new Color(
            Math.max(0, (int) (color.getRed() * factor)),
            Math.max(0, (int) (color.getGreen() * factor)),
            Math.max(0, (int) (color.getBlue() * factor))
        );
    }
    
    /**
     * Create a lighter version of a color
     */
    public static Color lighter(Color color, double factor) {
        return new Color(
            Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * factor)),
            Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * factor)),
            Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * factor))
        );
    }
} 