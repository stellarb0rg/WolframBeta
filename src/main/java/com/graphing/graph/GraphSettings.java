package com.graphing.graph;

import java.awt.Color;

/**
 * Holds graph view parameters (axis limits, grid, zoom)
 */
public class GraphSettings {
    
    // Axis limits
    private double minX = -10.0;
    private double maxX = 10.0;
    private double minY = -10.0;
    private double maxY = 10.0;
    
    // Grid settings
    private boolean showGrid = true;
    private boolean showAxes = true;
    private double gridSpacing = 1.0;
    private Color gridColor = new Color(230, 234, 242); // Subtle neutral grid
    private Color axesColor = new Color(36, 38, 44);
    
    // Rendering settings
    private int plotPoints = 2000;
    private double lineThickness = 1.05;
    private boolean antiAliasing = true;
    private Color backgroundColor = new Color(250, 251, 253);
    
    // Zoom and pan settings
    private double zoomLevel = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    
    // Display settings
    private boolean showCoordinates = true;
    private boolean showFunctionLabels = true;
    private boolean showCriticalPoints = false;
    private boolean showIntersections = false;
    
    public GraphSettings() {}
    
    public GraphSettings(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    // Getters and setters for axis limits
    public double getMinX() { return minX; }
    public void setMinX(double minX) { this.minX = minX; }
    
    public double getMaxX() { return maxX; }
    public void setMaxX(double maxX) { this.maxX = maxX; }
    
    public double getMinY() { return minY; }
    public void setMinY(double minY) { this.minY = minY; }
    
    public double getMaxY() { return maxY; }
    public void setMaxY(double maxY) { this.maxY = maxY; }
    
    // Getters and setters for grid settings
    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; }
    
    public boolean isShowAxes() { return showAxes; }
    public void setShowAxes(boolean showAxes) { this.showAxes = showAxes; }
    
    public double getGridSpacing() { return gridSpacing; }
    public void setGridSpacing(double gridSpacing) { this.gridSpacing = gridSpacing; }
    
    public Color getGridColor() { return gridColor; }
    public void setGridColor(Color gridColor) { this.gridColor = gridColor; }
    
    public Color getAxesColor() { return axesColor; }
    public void setAxesColor(Color axesColor) { this.axesColor = axesColor; }
    
    // Getters and setters for rendering settings
    public int getPlotPoints() { return plotPoints; }
    public void setPlotPoints(int plotPoints) { this.plotPoints = plotPoints; }
    
    public double getLineThickness() { return lineThickness; }
    public void setLineThickness(double lineThickness) { this.lineThickness = lineThickness; }
    
    public boolean isAntiAliasing() { return antiAliasing; }
    public void setAntiAliasing(boolean antiAliasing) { this.antiAliasing = antiAliasing; }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }
    
    // Getters and setters for zoom and pan settings
    public double getZoomLevel() { return zoomLevel; }
    public void setZoomLevel(double zoomLevel) { this.zoomLevel = zoomLevel; }
    
    public double getPanX() { return panX; }
    public void setPanX(double panX) { this.panX = panX; }
    
    public double getPanY() { return panY; }
    public void setPanY(double panY) { this.panY = panY; }
    
    // Getters and setters for display settings
    public boolean isShowCoordinates() { return showCoordinates; }
    public void setShowCoordinates(boolean showCoordinates) { this.showCoordinates = showCoordinates; }
    
    public boolean isShowFunctionLabels() { return showFunctionLabels; }
    public void setShowFunctionLabels(boolean showFunctionLabels) { this.showFunctionLabels = showFunctionLabels; }
    
    public boolean isShowCriticalPoints() { return showCriticalPoints; }
    public void setShowCriticalPoints(boolean showCriticalPoints) { this.showCriticalPoints = showCriticalPoints; }
    
    public boolean isShowIntersections() { return showIntersections; }
    public void setShowIntersections(boolean showIntersections) { this.showIntersections = showIntersections; }
    
    // Utility methods
    public double getXRange() { return maxX - minX; }
    public double getYRange() { return maxY - minY; }
    
    public void setXRange(double minX, double maxX) {
        this.minX = minX;
        this.maxX = maxX;
    }
    
    public void setYRange(double minY, double maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }
    
    public void setRange(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    public void resetView() {
        zoomLevel = 1.0;
        panX = 0.0;
        panY = 0.0;
    }
    
    public void zoomIn() {
        zoomLevel *= 1.2;
    }
    
    public void zoomOut() {
        zoomLevel /= 1.2;
    }
    
    public void autoScale(double[] xValues, double[] yValues) {
        if (xValues.length == 0 || yValues.length == 0) return;
        
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        for (double x : xValues) {
            if (!Double.isNaN(x) && !Double.isInfinite(x)) {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
        }
        
        for (double y : yValues) {
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        
        if (minX != Double.POSITIVE_INFINITY && maxX != Double.NEGATIVE_INFINITY) {
            double xMargin = (maxX - minX) * 0.1;
            this.minX = minX - xMargin;
            this.maxX = maxX + xMargin;
        }
        
        if (minY != Double.POSITIVE_INFINITY && maxY != Double.NEGATIVE_INFINITY) {
            double yMargin = (maxY - minY) * 0.1;
            this.minY = minY - yMargin;
            this.maxY = maxY + yMargin;
        }
    }
    
    public GraphSettings copy() {
        GraphSettings copy = new GraphSettings(minX, maxX, minY, maxY);
        copy.showGrid = showGrid;
        copy.showAxes = showAxes;
        copy.gridSpacing = gridSpacing;
        copy.gridColor = gridColor;
        copy.axesColor = axesColor;
        copy.plotPoints = plotPoints;
        copy.lineThickness = lineThickness;
        copy.antiAliasing = antiAliasing;
        copy.backgroundColor = backgroundColor;
        copy.zoomLevel = zoomLevel;
        copy.panX = panX;
        copy.panY = panY;
        copy.showCoordinates = showCoordinates;
        copy.showFunctionLabels = showFunctionLabels;
        copy.showCriticalPoints = showCriticalPoints;
        copy.showIntersections = showIntersections;
        return copy;
    }
} 
