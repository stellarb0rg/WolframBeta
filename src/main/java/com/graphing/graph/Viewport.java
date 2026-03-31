package com.graphing.graph;

import com.graphing.math.Point2D;

/**
 * Manages panning and zoom transformations between screen space and mathematical space
 */
public class Viewport {
    
    private double centerX = 0.0;
    private double centerY = 0.0;
    private double zoomLevel = 1.0;
    private int screenWidth;
    private int screenHeight;
    private double aspectRatio;
    
    public Viewport(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.aspectRatio = (double) screenWidth / screenHeight;
    }
    
    /**
     * Convert screen coordinates to mathematical coordinates
     */
    public Point2D screenToMath(int screenX, int screenY) {
        // Convert screen coordinates to normalized coordinates (-1 to 1)
        double normalizedX = (2.0 * screenX / screenWidth) - 1.0;
        double normalizedY = 1.0 - (2.0 * screenY / screenHeight); // Flip Y axis
        
        // Apply zoom and pan transformations
        double mathX = centerX + (normalizedX / zoomLevel);
        double mathY = centerY + (normalizedY / zoomLevel);
        
        return new Point2D(mathX, mathY);
    }
    
    /**
     * Convert mathematical coordinates to screen coordinates
     */
    public Point2D mathToScreen(double mathX, double mathY) {
        // Apply inverse zoom and pan transformations
        double normalizedX = (mathX - centerX) * zoomLevel;
        double normalizedY = (mathY - centerY) * zoomLevel;
        
        // Convert normalized coordinates to screen coordinates
        int screenX = (int) ((normalizedX + 1.0) * screenWidth / 2.0);
        int screenY = (int) ((1.0 - normalizedY) * screenHeight / 2.0);
        
        return new Point2D(screenX, screenY);
    }
    
    /**
     * Convert mathematical distance to screen distance
     */
    public double mathToScreenDistance(double mathDistance) {
        return mathDistance * zoomLevel * Math.min(screenWidth, screenHeight) / 2.0;
    }
    
    /**
     * Convert screen distance to mathematical distance
     */
    public double screenToMathDistance(double screenDistance) {
        return screenDistance / (zoomLevel * Math.min(screenWidth, screenHeight) / 2.0);
    }
    
    /**
     * Pan the viewport by the given mathematical distances
     */
    public void pan(double deltaX, double deltaY) {
        centerX += deltaX;
        centerY += deltaY;
    }
    
    /**
     * Pan the viewport by screen distances
     */
    public void panByScreen(int deltaScreenX, int deltaScreenY) {
        double deltaMathX = screenToMathDistance(deltaScreenX);
        double deltaMathY = screenToMathDistance(deltaScreenY);
        pan(deltaMathX, deltaMathY);
    }
    
    /**
     * Zoom in/out around a specific point
     */
    public void zoom(double factor, double centerMathX, double centerMathY) {
        // Calculate the mathematical coordinates of the zoom center
        double oldCenterX = centerX;
        double oldCenterY = centerY;
        
        // Update zoom level
        zoomLevel *= factor;
        
        // Adjust center to keep the zoom center point fixed
        centerX = centerMathX - (centerMathX - oldCenterX) * factor;
        centerY = centerMathY - (centerMathY - oldCenterY) * factor;
    }
    
    /**
     * Zoom in/out around the center of the screen
     */
    public void zoom(double factor) {
        zoom(factor, centerX, centerY);
    }
    
    /**
     * Zoom in/out around a screen point
     */
    public void zoomAtScreenPoint(double factor, int screenX, int screenY) {
        Point2D mathPoint = screenToMath(screenX, screenY);
        zoom(factor, mathPoint.getX(), mathPoint.getY());
    }
    
    /**
     * Set the viewport to show a specific mathematical range
     */
    public void setView(double minX, double maxX, double minY, double maxY) {
        centerX = (minX + maxX) / 2.0;
        centerY = (minY + maxY) / 2.0;
        
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        
        // Calculate zoom level to fit the range
        double zoomX = 2.0 / rangeX;
        double zoomY = 2.0 / rangeY;
        
        // Use the smaller zoom to ensure the entire range is visible
        zoomLevel = Math.min(zoomX, zoomY);
    }
    
    /**
     * Reset the viewport to default settings
     */
    public void reset() {
        centerX = 0.0;
        centerY = 0.0;
        zoomLevel = 1.0;
    }
    
    /**
     * Get the current mathematical bounds visible in the viewport
     */
    public double[] getVisibleBounds() {
        Point2D topLeft = screenToMath(0, 0);
        Point2D bottomRight = screenToMath(screenWidth, screenHeight);
        
        return new double[] {
            topLeft.getX(),
            bottomRight.getX(),
            bottomRight.getY(),
            topLeft.getY()
        };
    }
    
    /**
     * Check if a mathematical point is visible in the current viewport
     */
    public boolean isPointVisible(double mathX, double mathY) {
        double[] bounds = getVisibleBounds();
        return mathX >= bounds[0] && mathX <= bounds[1] && 
               mathY >= bounds[2] && mathY <= bounds[3];
    }
    
    /**
     * Update the screen dimensions (called when window is resized)
     */
    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.aspectRatio = (double) width / height;
    }
    
    /**
     * Set the viewport size (alias for setScreenDimensions)
     */
    public void setSize(int width, int height) {
        setScreenDimensions(width, height);
    }
    
    // Getters and setters
    public double getCenterX() { return centerX; }
    public void setCenterX(double centerX) { this.centerX = centerX; }
    
    public double getCenterY() { return centerY; }
    public void setCenterY(double centerY) { this.centerY = centerY; }
    
    public double getZoomLevel() { return zoomLevel; }
    public void setZoomLevel(double zoomLevel) { this.zoomLevel = zoomLevel; }
    
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    
    public double getAspectRatio() { return aspectRatio; }
    
    /**
     * Get the mathematical step size for plotting based on screen resolution
     */
    public double getPlotStepSize() {
        return screenToMathDistance(1.0); // One pixel in mathematical units
    }
} 