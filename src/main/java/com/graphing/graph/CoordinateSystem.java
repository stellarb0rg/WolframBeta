package com.graphing.graph;

import com.graphing.math.Point2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * Maps between screen space and math space, handles coordinate system rendering
 */
public class CoordinateSystem {
    
    private final Viewport viewport;
    private final GraphSettings settings;
    
    public CoordinateSystem(Viewport viewport, GraphSettings settings) {
        this.viewport = viewport;
        this.settings = settings;
    }
    
    /**
     * Draw the coordinate system (axes and grid)
     */
    public void draw(Graphics2D g2d) {
        // Enable anti-aliasing if requested
        if (settings.isAntiAliasing()) {
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        // Draw background
        g2d.setColor(settings.getBackgroundColor());
        g2d.fillRect(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
        
        // Draw grid if enabled
        if (settings.isShowGrid()) {
            drawGrid(g2d);
        }
        
        // Draw axes if enabled
        if (settings.isShowAxes()) {
            drawAxes(g2d);
        }
        
        // Draw axis labels
        drawAxisLabels(g2d);
    }
    
    /**
     * Draw the grid lines
     */
    private void drawGrid(Graphics2D g2d) {
        // Use grid color from settings
        g2d.setColor(settings.getGridColor());
        g2d.setStroke(new java.awt.BasicStroke(1.0f));
        
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        double minY = bounds[2];
        double maxY = bounds[3];
        int screenW = viewport.getScreenWidth();
        int screenH = viewport.getScreenHeight();
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        // Allow minGridPx to be set from settings in the future, for now keep as 40.0
        double minGridPx = 40.0;
        // Use gridSpacing as a minimum step size
        double baseGridSpacing = settings.getGridSpacing();
        double xStep = Math.max(getNiceStep(xRange, screenW, minGridPx), baseGridSpacing);
        double yStep = Math.max(getNiceStep(yRange, screenH, minGridPx), baseGridSpacing);
        // Draw vertical grid lines
        double startX = Math.ceil(minX / xStep) * xStep;
        for (double x = startX; x <= maxX; x += xStep) {
            Point2D top = viewport.mathToScreen(x, maxY);
            Point2D bottom = viewport.mathToScreen(x, minY);
            g2d.drawLine((int) top.getX(), (int) top.getY(), 
                        (int) bottom.getX(), (int) bottom.getY());
        }
        // Draw horizontal grid lines
        double startY = Math.ceil(minY / yStep) * yStep;
        for (double y = startY; y <= maxY; y += yStep) {
            Point2D left = viewport.mathToScreen(minX, y);
            Point2D right = viewport.mathToScreen(maxX, y);
            g2d.drawLine((int) left.getX(), (int) left.getY(), 
                        (int) right.getX(), (int) right.getY());
        }
    }
    
    /**
     * Draw the coordinate axes
     */
    private void drawAxes(Graphics2D g2d) {
        g2d.setColor(settings.getAxesColor());
        g2d.setStroke(new java.awt.BasicStroke(0.5f));
        
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        double minY = bounds[2];
        double maxY = bounds[3];
        // Remove arrowOffset, use true axis ends
        // Draw X-axis (y = 0)
        if (minY <= 0 && maxY >= 0) {
            Point2D left = viewport.mathToScreen(minX, 0);
            Point2D right = viewport.mathToScreen(maxX, 0);
            g2d.drawLine((int) left.getX(), (int) left.getY(), 
                        (int) right.getX(), (int) right.getY());
            // Draw arrow at the positive X end
            drawArrow(g2d, right, 0);
            // Draw arrow at the negative X end
            drawArrow(g2d, left, Math.PI);
        }
        
        // Draw Y-axis (x = 0)
        if (minX <= 0 && maxX >= 0) {
            Point2D bottom = viewport.mathToScreen(0, minY);
            Point2D top = viewport.mathToScreen(0, maxY);
            g2d.drawLine((int) bottom.getX(), (int) bottom.getY(), 
                        (int) top.getX(), (int) top.getY());
            // Draw arrow at the positive Y end
            drawArrow(g2d, top, Math.PI / 2);
            // Draw arrow at the negative Y end
            drawArrow(g2d, bottom, -Math.PI / 2);
        }
    }
    
    /**
     * Draw an arrow at the end of an axis
     */
    private void drawArrow(Graphics2D g2d, Point2D tip, double angle) {
        double arrowLength = 10.0;
        double arrowWidth = 7.0;
        // Calculate the three points of the triangle
        double x0 = tip.getX();
        double y0 = tip.getY();
        double x1 = x0 - arrowLength * Math.cos(angle - Math.PI / 8);
        double y1 = y0 + arrowLength * Math.sin(angle - Math.PI / 8);
        double x2 = x0 - arrowLength * Math.cos(angle + Math.PI / 8);
        double y2 = y0 + arrowLength * Math.sin(angle + Math.PI / 8);
        int[] xPoints = {(int) x0, (int) x1, (int) x2};
        int[] yPoints = {(int) y0, (int) y1, (int) y2};
        Color oldColor = g2d.getColor();
        g2d.setColor(settings.getAxesColor());
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(oldColor);
    }
    
    /**
     * Draw axis labels and tick marks
     */
    private void drawAxisLabels(Graphics2D g2d) {
        g2d.setColor(settings.getAxesColor());
        Font baseFont = UIManager.getFont("defaultFont");
        if (baseFont != null) {
            g2d.setFont(baseFont.deriveFont(Font.PLAIN, 10f));
        } else {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        }
        
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        double minY = bounds[2];
        double maxY = bounds[3];
        int screenW = viewport.getScreenWidth();
        int screenH = viewport.getScreenHeight();
        // Target: ~80px between labels
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        double minLabelPx = 140.0;
        double xStep = getNiceStep(xRange, screenW, minLabelPx);
        double yStep = getNiceStep(yRange, screenH, minLabelPx);
        // Draw X-axis labels
        double startX = Math.ceil(minX / xStep) * xStep;
        for (double x = startX; x <= maxX; x += xStep) {
            if (Math.abs(x) < 1e-10) continue; // Skip origin
            Point2D screenPoint = viewport.mathToScreen(x, 0);
            String label = formatNumber(x);
            int labelX = (int) screenPoint.getX() - 10;
            int labelY = (int) screenPoint.getY() + 15;
            g2d.drawString(label, labelX, labelY);
            // Draw tick mark
            Point2D tickTop = viewport.mathToScreen(x, 0.1 * xStep);
            Point2D tickBottom = viewport.mathToScreen(x, -0.1 * xStep);
            g2d.drawLine((int) tickTop.getX(), (int) tickTop.getY(), 
                        (int) tickBottom.getX(), (int) tickBottom.getY());
        }
        // Draw Y-axis labels
        double startY = Math.ceil(minY / yStep) * yStep;
        for (double y = startY; y <= maxY; y += yStep) {
            if (Math.abs(y) < 1e-10) continue; // Skip origin
            Point2D screenPoint = viewport.mathToScreen(0, y);
            String label = formatNumber(y);
            int labelX = (int) screenPoint.getX() - 25;
            int labelY = (int) screenPoint.getY() + 3;
            g2d.drawString(label, labelX, labelY);
            // Draw tick mark
            Point2D tickLeft = viewport.mathToScreen(-0.1 * yStep, y);
            Point2D tickRight = viewport.mathToScreen(0.1 * yStep, y);
            g2d.drawLine((int) tickLeft.getX(), (int) tickLeft.getY(), 
                        (int) tickRight.getX(), (int) tickRight.getY());
        }
        // Draw origin label
        if (minX <= 0 && maxX >= 0 && minY <= 0 && maxY >= 0) {
            Point2D origin = viewport.mathToScreen(0, 0);
            g2d.drawString("0", (int) origin.getX() + 5, (int) origin.getY() - 5);
        }
    }

    // Returns a power-of-10 step size for axis labels, based on range and screen size
    private double getNiceStep(double range, int screenSize, double minLabelPx) {
        double approxStep = range * minLabelPx / screenSize;
        double log10 = Math.floor(Math.log10(approxStep));
        return Math.pow(10, log10);
    }
    
    /**
     * Format a number for display
     */
    private String formatNumber(double value) {
        if (Math.abs(value) < 1e-10) {
            return "0";
        } else if (Math.abs(value) >= 1000 || (Math.abs(value) < 0.01 && value != 0)) {
            return String.format("%.1e", value);
        } else {
            return String.format("%.1f", value);
        }
    }
    
    /**
     * Draw coordinate tooltip at a specific screen position
     */
    public void drawCoordinateTooltip(Graphics2D g2d, int screenX, int screenY) {
        if (!settings.isShowCoordinates()) return;
        
        Point2D mathPoint = viewport.screenToMath(screenX, screenY);
        String coordinateText = String.format("(%.3f, %.3f)", mathPoint.getX(), mathPoint.getY());
        
        // Set up tooltip appearance
        g2d.setColor(new Color(17, 19, 24, 220));
        Font baseFont = UIManager.getFont("defaultFont");
        if (baseFont != null) {
            g2d.setFont(baseFont.deriveFont(Font.PLAIN, 12f));
        } else {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }
        
        // Calculate text bounds
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(coordinateText);
        int textHeight = fm.getHeight();
        
        // Position tooltip near mouse but not under it
        int tooltipX = screenX + 10;
        int tooltipY = screenY - 10;
        
        // Ensure tooltip stays within screen bounds
        if (tooltipX + textWidth > viewport.getScreenWidth()) {
            tooltipX = screenX - textWidth - 10;
        }
        if (tooltipY - textHeight < 0) {
            tooltipY = screenY + textHeight + 10;
        }
        
        // Draw background rectangle
        g2d.fillRect(tooltipX - 2, tooltipY - textHeight - 2, 
                    textWidth + 4, textHeight + 4);
        
        // Draw text
        g2d.setColor(Color.WHITE);
        g2d.drawString(coordinateText, tooltipX, tooltipY);
    }
    
    /**
     * Get the mathematical coordinates at a screen position
     */
    public Point2D getMathCoordinates(int screenX, int screenY) {
        return viewport.screenToMath(screenX, screenY);
    }
    
    /**
     * Get the screen coordinates for a mathematical point
     */
    public Point2D getScreenCoordinates(double mathX, double mathY) {
        return viewport.mathToScreen(mathX, mathY);
    }
    
    /**
     * Check if a mathematical point is visible in the current view
     */
    public boolean isPointVisible(double mathX, double mathY) {
        return viewport.isPointVisible(mathX, mathY);
    }
} 
