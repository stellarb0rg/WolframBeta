package com.graphing.graph;

import com.graphing.math.Function;
import com.graphing.math.Point2D;
import com.graphing.math.IntersectionPoint;
import com.graphing.graph.Viewport;
import com.graphing.math.analysis.FunctionAnalyzer;
import com.graphing.math.calculus.CriticalPointFinder;
import com.graphing.math.calculus.IntegralCalculator;
import com.graphing.math.parser.ExpressionEvaluator;
import com.graphing.math.analysis.IntersectionFinder;
import com.graphing.utils.MathUtils;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * High-level graph drawing that coordinates all rendering components.
 * Manages the complete graph rendering pipeline.
 */
public class GraphRenderer {
    
    private final GraphSettings settings;
    private final Viewport viewport;
    private final CoordinateSystem coordinateSystem;
    private final CurveRenderer curveRenderer;
    private final FunctionAnalyzer functionAnalyzer;
    private final CriticalPointFinder criticalPointFinder;
    private final IntegralCalculator integralCalculator;
    private final IntersectionFinder intersectionFinder;
    
    // Store intersection points with colors for manual display
    private List<IntersectionPoint> intersectionPoints = new ArrayList<>();
    
    // Store extrema points for display
    private List<Point2D> localMaxima = new ArrayList<>();
    private List<Point2D> localMinima = new ArrayList<>();
    
    public GraphRenderer(Viewport viewport, GraphSettings settings) {
        this.viewport = viewport;
        this.settings = settings;
        this.coordinateSystem = new CoordinateSystem(viewport, settings);
        this.curveRenderer = new CurveRenderer(viewport, settings);
        this.functionAnalyzer = new FunctionAnalyzer();
        this.criticalPointFinder = new CriticalPointFinder();
        this.integralCalculator = new IntegralCalculator();
        this.intersectionFinder = new IntersectionFinder();
    }
    
    /**
     * Renders the complete graph with all components.
     */
    public void render(Graphics2D g2d, List<Function> functions) {
        // Set rendering hints for better quality
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, 
                            java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        
        // Clear background
        g2d.setColor(settings.getBackgroundColor());
        g2d.fillRect(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
        
        // Draw coordinate system
        coordinateSystem.draw(g2d);
        
        // Draw functions
        for (Function function : functions) {
            if (function.isVisible()) {
                drawFunction(g2d, function);
            }
        }
        
        // Draw stored intersection points with colored dots
        for (IntersectionPoint intersectionPoint : intersectionPoints) {
            Point2D point = intersectionPoint.getPoint();
            if (viewport.isPointVisible(point.getX(), point.getY())) {
                drawPoint(g2d, point, intersectionPoint.getColor());
            }
        }
        
        // Draw extrema points as black dots
        for (Point2D point : localMaxima) {
            if (viewport.isPointVisible(point.getX(), point.getY())) {
                drawPoint(g2d, point, Color.BLACK);
            }
        }
        for (Point2D point : localMinima) {
            if (viewport.isPointVisible(point.getX(), point.getY())) {
                drawPoint(g2d, point, Color.BLACK);
            }
        }
        
        // Note: drawAnalysisFeatures is disabled to avoid showing x-axis intersections
        // Only the stored intersection points (black dots) will be shown
    }
    
    /**
     * Draw a single function
     */
    public void drawFunction(Graphics2D g2d, Function function) {
        curveRenderer.drawFunction(g2d, function.getExpression(), function.getColor(), settings.getLineThickness(), function);
        
        // Draw function label if enabled
        if (settings.isShowFunctionLabels()) {
            drawFunctionLabel(g2d, function);
        }
    }
    
    /**
     * Draw function labels
     */
    private void drawFunctionLabel(Graphics2D g2d, Function function) {
        // Find a good position for the label (top-right of the visible area)
        double[] bounds = viewport.getVisibleBounds();
        double labelX = bounds[1] - (bounds[1] - bounds[0]) * 0.1;
        double labelY = Double.NaN;
        boolean found = false;
        try {
            labelY = ExpressionEvaluator.evaluate(function.getExpression(), labelX);
            if (!Double.isNaN(labelY) && !Double.isInfinite(labelY) && viewport.isPointVisible(labelX, labelY)) {
                found = true;
            } else {
                // Search leftward for a visible, finite y
                int maxTries = 50;
                double step = (bounds[1] - bounds[0]) * 0.02;
                for (int i = 1; i <= maxTries; i++) {
                    double tryX = labelX - i * step;
                    double tryY = ExpressionEvaluator.evaluate(function.getExpression(), tryX);
                    if (!Double.isNaN(tryY) && !Double.isInfinite(tryY) && viewport.isPointVisible(tryX, tryY)) {
                        labelX = tryX;
                        labelY = tryY;
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                Point2D screenPoint = viewport.mathToScreen(labelX, labelY);
                g2d.setColor(function.getColor());
                Font baseFont = UIManager.getFont("defaultFont");
                if (baseFont != null) {
                    g2d.setFont(baseFont.deriveFont(Font.BOLD, 12f));
                } else {
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                }
                // Use a short label for derivatives, otherwise use the function's name
                String label;
                if (function.getName().startsWith("d/dx(")) {
                    label = function.getName(); // e.g., d/dx(x^3 - 2x + 1)
                } else {
                    label = function.getName(); // For normal functions, this is the formula
                }
                // Truncate if too long
                if (label.length() > 32) {
                    label = label.substring(0, 29) + "...";
                }
                g2d.drawString(label, (int) screenPoint.getX() + 5, (int) screenPoint.getY() - 5);
            }
        } catch (Exception e) {
            // Skip label if function evaluation fails
        }
    }
    
    /**
     * Draws analysis features like critical points, intersections, etc.
     */
    private void drawAnalysisFeatures(Graphics2D g2d, List<Function> functions) {
        if (functions.isEmpty()) {
            return;
        }
        
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        double minY = bounds[2];
        double maxY = bounds[3];
        
        // Draw critical points for each function
        for (Function function : functions) {
            if (function.isVisible()) {
                List<Point2D> criticalPoints = criticalPointFinder.findCriticalPoints(
                    function.getExpression(), minX, maxX, 100);
                
                for (Point2D point : criticalPoints) {
                    if (viewport.isPointVisible(point.getX(), point.getY())) {
                        drawPoint(g2d, point, Color.RED);
                    }
                }
            }
        }
        
        // Draw intersections between functions
        if (functions.size() > 1) {
            for (int i = 0; i < functions.size() - 1; i++) {
                for (int j = i + 1; j < functions.size(); j++) {
                    Function f1 = functions.get(i);
                    Function f2 = functions.get(j);
                    
                    if (f1.isVisible() && f2.isVisible()) {
                        List<Point2D> intersections = intersectionFinder.findIntersections(
                            f1.getExpression(), f2.getExpression(), minX, maxX, 100);
                        
                        for (Point2D point : intersections) {
                            if (viewport.isPointVisible(point.getX(), point.getY())) {
                                drawPoint(g2d, point, Color.GREEN);
                            }
                        }
                    }
                }
            }
        }
        
        // Draw roots (x-axis intersections) for each function
        for (Function function : functions) {
            if (function.isVisible()) {
                List<Point2D> roots = intersectionFinder.findRoots(
                    function.getExpression(), minX, maxX, 100);
                
                for (Point2D point : roots) {
                    if (viewport.isPointVisible(point.getX(), point.getY())) {
                        drawPoint(g2d, point, Color.ORANGE);
                    }
                }
            }
        }
    }
    
    /**
     * Draw a point on the graph
     */
    private void drawPoint(Graphics2D g2d, Point2D point, Color color) {
        Point2D screenPoint = viewport.mathToScreen(point.getX(), point.getY());
        int x = (int) screenPoint.getX();
        int y = (int) screenPoint.getY();
        
        g2d.setColor(color);
        g2d.fillOval(x - 3, y - 3, 6, 6);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x - 3, y - 3, 6, 6);
    }
    
    /**
     * Renders a function with its derivative.
     */
    public void renderFunctionWithDerivative(Graphics2D g2d, Function function) {
        // Draw the main function
        curveRenderer.drawFunction(g2d, function.getExpression(), function.getColor());
        
        // Draw the derivative
        List<Point2D> derivativePoints = generateDerivativePoints(function.getExpression());
        Color derivativeColor = new Color(
            function.getColor().getRed(),
            function.getColor().getGreen(),
            function.getColor().getBlue(),
            128 // Semi-transparent
        );
        curveRenderer.drawCurve(g2d, derivativePoints, derivativeColor, 1.0f);
    }
    
    /**
     * Generate points for the derivative of a function
     */
    private List<Point2D> generateDerivativePoints(String expression) {
        List<Point2D> points = new ArrayList<>();
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        
        int numPoints = 200;
        double step = (maxX - minX) / (numPoints - 1);
        
        for (int i = 0; i < numPoints; i++) {
            double x = minX + i * step;
            try {
                double derivative = calculateDerivative(expression, x);
                if (MathUtils.isFinite(derivative)) {
                    points.add(new Point2D(x, derivative));
                }
            } catch (Exception e) {
                // Skip points where derivative calculation fails
            }
        }
        
        return points;
    }
    
    /**
     * Calculate derivative at a point using numerical differentiation
     */
    private double calculateDerivative(String expression, double x) {
        double h = 1e-6;
        try {
            double f1 = ExpressionEvaluator.evaluate(expression, x + h);
            double f2 = ExpressionEvaluator.evaluate(expression, x - h);
            return (f1 - f2) / (2 * h);
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    /**
     * Renders a function with area under the curve filled.
     */
    public void renderFunctionWithArea(Graphics2D g2d, Function function, double areaStart, double areaEnd) {
        // Fill the area under the curve
        Color fillColor = new Color(
            function.getColor().getRed(),
            function.getColor().getGreen(),
            function.getColor().getBlue(),
            64 // Very transparent
        );
        // Create a polygon for the area
        List<Point2D> areaPoints = new ArrayList<>();
        // Add points along the function
        int numPoints = 100;
        double step = (areaEnd - areaStart) / (numPoints - 1);
        for (int i = 0; i < numPoints; i++) {
            double x = areaStart + i * step;
            try {
                double y = ExpressionEvaluator.evaluate(function.getExpression(), x);
                if (MathUtils.isFinite(y)) {
                    areaPoints.add(new Point2D(x, y));
                }
            } catch (Exception e) {
                // Skip evaluation errors
            }
        }
        // Add points to complete the polygon
        if (!areaPoints.isEmpty()) {
            // Add bottom-right corner
            areaPoints.add(new Point2D(areaEnd, 0));
            // Add bottom-left corner
            areaPoints.add(new Point2D(areaStart, 0));
            // Convert to screen coordinates and fill
            List<Point2D> screenPoints = new ArrayList<>();
            for (Point2D mathPoint : areaPoints) {
                Point2D screenPoint = viewport.mathToScreen(mathPoint.getX(), mathPoint.getY());
                if (MathUtils.isFinite(screenPoint.getX()) && MathUtils.isFinite(screenPoint.getY())) {
                    screenPoints.add(screenPoint);
                }
            }
            if (screenPoints.size() > 2) {
                int[] xPoints = new int[screenPoints.size()];
                int[] yPoints = new int[screenPoints.size()];
                for (int i = 0; i < screenPoints.size(); i++) {
                    Point2D p = screenPoints.get(i);
                    xPoints[i] = (int) p.getX();
                    yPoints[i] = (int) p.getY();
                }
                g2d.setColor(fillColor);
                g2d.fillPolygon(xPoints, yPoints, screenPoints.size());
            }
        }
    }
    
    /**
     * Shows coordinate information at a specific screen point.
     */
    public void showCoordinates(Graphics2D g2d, int screenX, int screenY, List<Function> functions) {
        Point2D mathPoint = viewport.screenToMath(screenX, screenY);
        
        if (MathUtils.isFinite(mathPoint.getX()) && MathUtils.isFinite(mathPoint.getY())) {
            // Draw coordinate crosshair
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, 
                                                 java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            
            // Vertical line
            g2d.drawLine(screenX, 0, screenX, viewport.getScreenHeight());
            // Horizontal line
            g2d.drawLine(0, screenY, viewport.getScreenWidth(), screenY);
            
            // Show coordinate text
            String coordText = String.format("(%.3f, %.3f)", mathPoint.getX(), mathPoint.getY());
            g2d.setColor(Color.BLACK);
            g2d.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            
            int textX = screenX + 10;
            int textY = screenY - 10;
            
            // Ensure text doesn't go off screen
            if (textX + 100 > viewport.getScreenWidth()) {
                textX = screenX - 110;
            }
            if (textY < 20) {
                textY = screenY + 20;
            }
            
            g2d.drawString(coordText, textX, textY);
            
            // Show function values if functions are present
            if (!functions.isEmpty()) {
                int yOffset = 20;
                for (Function function : functions) {
                    if (function.isVisible()) {
                        try {
                            double y = ExpressionEvaluator.evaluate(function.getExpression(), mathPoint.getX());
                            if (MathUtils.isFinite(y)) {
                                String funcText = String.format("%s: %.3f", function.getName(), y);
                                g2d.setColor(function.getColor());
                                g2d.drawString(funcText, textX, textY + yOffset);
                                yOffset += 15;
                            }
                        } catch (Exception e) {
                            // Skip function evaluation errors
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Calculates and displays the area under a function.
     */
    public double calculateAreaUnderCurve(String expression, double startX, double endX) {
        return integralCalculator.areaUnderCurve(expression, startX, endX);
    }
    
    /**
     * Gets the viewport for external access.
     */
    public Viewport getViewport() {
        return viewport;
    }
    
    /**
     * Gets the coordinate system for external access.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }
    
    /**
     * Gets the curve renderer for external access.
     */
    public CurveRenderer getCurveRenderer() {
        return curveRenderer;
    }
    
    /**
     * Update the viewport size
     */
    public void setSize(int width, int height) {
        viewport.setSize(width, height);
    }
    
    /**
     * Get the graph settings
     */
    public GraphSettings getSettings() {
        return settings;
    }
    
    /**
     * Find all intersections between the given functions
     */
    public List<IntersectionPoint> findIntersections(List<Function> functions) {
        intersectionPoints.clear();
        
        if (functions.size() < 2) {
            return new ArrayList<>(intersectionPoints);
        }
        
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        
        Random random = new Random();
        int intersectionIndex = 0;
        double tolerance = 1e-6;
        // Temporary list to group intersections by location
        java.util.List<IntersectionPoint> tempPoints = new java.util.ArrayList<>();
        
        // Find intersections between all pairs of functions
        for (int i = 0; i < functions.size() - 1; i++) {
            for (int j = i + 1; j < functions.size(); j++) {
                Function f1 = functions.get(i);
                Function f2 = functions.get(j);
                
                if (f1.isVisible() && f2.isVisible()) {
                    List<Point2D> intersections = intersectionFinder.findIntersections(
                        f1.getExpression(), f2.getExpression(), minX, maxX, 200);
                    
                    for (Point2D point : intersections) {
                        // Check if this point is already in tempPoints (within tolerance)
                        boolean found = false;
                        for (IntersectionPoint ip : tempPoints) {
                            if (Math.abs(ip.getX() - point.getX()) < tolerance && Math.abs(ip.getY() - point.getY()) < tolerance) {
                                // Add these functions to the group if not already present
                                java.util.List<String> exprs = ip.getFunctionExprs();
                                if (!exprs.contains(f1.getExpression())) exprs.add(f1.getExpression());
                                if (!exprs.contains(f2.getExpression())) exprs.add(f2.getExpression());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            java.util.List<String> exprs = new java.util.ArrayList<>();
                            exprs.add(f1.getExpression());
                            exprs.add(f2.getExpression());
                            Color randomColor = generateRandomColor(random);
                            tempPoints.add(new IntersectionPoint(point, randomColor, intersectionIndex++, exprs));
                        }
                    }
                }
            }
        }
        intersectionPoints.addAll(tempPoints);
        return new ArrayList<>(intersectionPoints);
    }
    
    /**
     * Generate a random color for intersection points
     */
    private Color generateRandomColor(Random random) {
        // Generate bright, distinct colors
        float hue = random.nextFloat();
        float saturation = 0.7f + random.nextFloat() * 0.3f; // 0.7-1.0
        float brightness = 0.8f + random.nextFloat() * 0.2f; // 0.8-1.0
        
        return Color.getHSBColor(hue, saturation, brightness);
    }
    
    /**
     * Clear all stored intersection points
     */
    public void clearIntersections() {
        intersectionPoints.clear();
    }
    
    /**
     * Get the current intersection points
     */
    public List<IntersectionPoint> getIntersectionPoints() {
        return new ArrayList<>(intersectionPoints);
    }
    
    /**
     * Update graph settings
     */
    public void updateSettings(GraphSettings newSettings) {
        // Update the settings
        settings.setXRange(newSettings.getMinX(), newSettings.getMaxX());
        settings.setYRange(newSettings.getMinY(), newSettings.getMaxY());
        settings.setShowGrid(newSettings.isShowGrid());
        settings.setShowAxes(newSettings.isShowAxes());
        settings.setGridSpacing(newSettings.getGridSpacing());
        settings.setGridColor(newSettings.getGridColor());
        settings.setAxesColor(newSettings.getAxesColor());
        settings.setBackgroundColor(newSettings.getBackgroundColor());
        settings.setShowCoordinates(newSettings.isShowCoordinates());
        settings.setAntiAliasing(newSettings.isAntiAliasing());
    }
    
    /**
     * Reset the view to default
     */
    public void resetView() {
        settings.resetView();
        viewport.reset();
    }
    
    /**
     * Zoom in
     */
    public void zoomIn() {
        viewport.zoom(1.2);
    }
    
    /**
     * Zoom out
     */
    public void zoomOut() {
        viewport.zoom(1.0 / 1.2);
    }
    
    /**
     * Pan the view
     */
    public void pan(double deltaX, double deltaY) {
        viewport.pan(deltaX, deltaY);
    }
    
    /**
     * Fit the view to show all functions
     */
    public void fitToFunctions(List<Function> functions) {
        if (functions.isEmpty()) {
            return;
        }
        
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        // Sample points from each function to find bounds
        for (Function function : functions) {
            if (!function.isVisible()) continue;
            
            double[] bounds = viewport.getVisibleBounds();
            double step = (bounds[1] - bounds[0]) / 100;
            
            for (double x = bounds[0]; x <= bounds[1]; x += step) {
                try {
                    double y = ExpressionEvaluator.evaluate(function.getExpression(), x);
                    if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                } catch (Exception e) {
                    // Skip evaluation errors
                }
            }
        }
        
        if (minX < Double.POSITIVE_INFINITY && maxX > Double.NEGATIVE_INFINITY &&
            minY < Double.POSITIVE_INFINITY && maxY > Double.NEGATIVE_INFINITY) {
            
            // Add padding
            double xPadding = (maxX - minX) * 0.1;
            double yPadding = (maxY - minY) * 0.1;
            
            settings.setXRange(minX - xPadding, maxX + xPadding);
            settings.setYRange(minY - yPadding, maxY + yPadding);
        }
    }
    
    /**
     * Set extrema points for display
     */
    public void setExtremaPoints(List<Point2D> maxima, List<Point2D> minima) {
        this.localMaxima = new ArrayList<>(maxima);
        this.localMinima = new ArrayList<>(minima);
    }
    
    /**
     * Clear extrema points
     */
    public void clearExtremaPoints() {
        localMaxima.clear();
        localMinima.clear();
    }
} 
