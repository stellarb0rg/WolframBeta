package com.graphing.graph;

import com.graphing.math.Point2D;
import com.graphing.math.parser.ExpressionEvaluator;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;

/**
 * Draws smooth curves for functions
 */
public class CurveRenderer {
    
    private final Viewport viewport;
    private final GraphSettings settings;
    
    public CurveRenderer(Viewport viewport, GraphSettings settings) {
        this.viewport = viewport;
        this.settings = settings;
    }
    
    /**
     * Draw a function curve
     */
    public void drawFunction(Graphics2D g2d, String expression, Color color) {
        drawFunction(g2d, expression, color, settings.getLineThickness());
    }
    
    /**
     * Draw a function curve with custom line thickness
     */
    public void drawFunction(Graphics2D g2d, String expression, Color color, double thickness) {
        List<Point2D> points = generateFunctionPoints(expression, null);
        drawCurve(g2d, points, color, thickness);
    }
    
    /**
     * Draw a function curve with custom line thickness and function
     */
    public void drawFunction(Graphics2D g2d, String expression, Color color, double thickness, com.graphing.math.Function function) {
        if (function != null && (function.getType() == com.graphing.math.FunctionType.IMPLICIT || function.getType() == com.graphing.math.FunctionType.INEQUALITY)) {
            drawImplicitOrInequality(g2d, function, color);
        } else {
            // Check if this is an asymptotic function that needs special handling
            boolean hasAsymptotes = expression.contains("1/tan") || expression.contains("1/sin") || 
                                   expression.contains("1/cos") || expression.contains("cot") || 
                                   expression.contains("sec") || expression.contains("csc") ||
                                   expression.contains("cosec") || expression.contains("/tan") ||
                                   expression.contains("/sin") || expression.contains("/cos");
            
            List<Point2D> points;
            if (hasAsymptotes) {
                points = generateAsymptoticFunctionPoints(expression, function);
            } else {
                points = generateFunctionPoints(expression, function);
            }
            drawCurve(g2d, points, color, thickness);
        }
    }
    
    /**
     * Generate points for a function over the visible range
     */
    public List<Point2D> generateFunctionPoints(String expression, com.graphing.math.Function function) {
        List<Point2D> points = new ArrayList<>();
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        
        // Adaptive plot points based on zoom level and range
        double range = maxX - minX;
        int basePoints = settings.getPlotPoints();
        
        // Special handling for functions with asymptotes (like 1/tan(x), 1/sin(x), etc.)
        boolean hasAsymptotes = expression.contains("1/tan") || expression.contains("1/sin") || 
                               expression.contains("1/cos") || expression.contains("cot") || 
                               expression.contains("sec") || expression.contains("csc") ||
                               expression.contains("cosec") || expression.contains("/tan") ||
                               expression.contains("/sin") || expression.contains("/cos");
        
        // Increase plot points when zoomed out (larger range) to maintain quality
        int adaptivePoints;
        if (hasAsymptotes) {
            // Use more points for functions with asymptotes
            if (range <= 20) {
                adaptivePoints = basePoints * 2; // Double for asymptotic functions
            } else if (range <= 100) {
                adaptivePoints = (int)(basePoints * 3.0); 
            } else if (range <= 500) {
                adaptivePoints = (int)(basePoints * 4.0); 
            } else {
                adaptivePoints = (int)(basePoints * 5.0); 
            }
        } else {
            if (range <= 20) {
                adaptivePoints = basePoints; // Normal zoom
            } else if (range <= 100) {
                adaptivePoints = (int)(basePoints * 1.5); // Moderate zoom out
            } else if (range <= 500) {
                adaptivePoints = (int)(basePoints * 2.0); // Significant zoom out
            } else {
                adaptivePoints = (int)(basePoints * 3.0); // Extreme zoom out
            }
        }
        
        // Cap the maximum points to prevent performance issues
        adaptivePoints = Math.min(adaptivePoints, 15000);
        
        double step = (maxX - minX) / (adaptivePoints - 1);
        boolean isDerivative = expression.startsWith("DERIVATIVE_OF:");
        String baseExpr = isDerivative ? expression.substring("DERIVATIVE_OF:".length()) : expression;
        double h = 1e-5;
        
        for (int i = 0; i < adaptivePoints; i++) {
            double x = minX + i * step;
            try {
                double y;
                if (isDerivative) {
                    double f1 = ExpressionEvaluator.evaluate(baseExpr, x + h);
                    double f2 = ExpressionEvaluator.evaluate(baseExpr, x - h);
                    y = (f1 - f2) / (2 * h);
                } else if (function != null && function.getParsedExpression() != null) {
                    y = function.getParsedExpression().evaluate(java.util.Collections.singletonMap("x", x));
                } else {
                    y = ExpressionEvaluator.evaluate(expression, x);
                }
                
                // For asymptotic functions, be more lenient with large values
                if (hasAsymptotes) {
                    // Allow larger values but cap them to prevent rendering issues
                    if (Math.abs(y) > 1000) {
                        y = Math.signum(y) * 1000; // Cap at ±1000
                    }
                }
                
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    points.add(new Point2D(x, y));
                }
            } catch (Exception e) {
                // For asymptotic functions, try to detect if we're near an asymptote
                if (hasAsymptotes) {
                    // Try points slightly to the left and right to see if we're near an asymptote
                    try {
                        double leftY = ExpressionEvaluator.evaluate(expression, x - step * 0.1);
                        double rightY = ExpressionEvaluator.evaluate(expression, x + step * 0.1);
                        
                        // If both sides have large values with opposite signs, it's likely an asymptote
                        if (Math.abs(leftY) > 100 && Math.abs(rightY) > 100 && 
                            Math.signum(leftY) != Math.signum(rightY)) {
                            // Skip this point - it's near a vertical asymptote
                            continue;
                        }
                    } catch (Exception e2) {
                        // Skip points where evaluation fails completely
                    }
                }
                // Skip points where evaluation fails
            }
        }
        return points;
    }
    
    /**
     * Generate points for asymptotic functions with special handling for discontinuities
     */
    private List<Point2D> generateAsymptoticFunctionPoints(String expression, com.graphing.math.Function function) {
        List<Point2D> points = new ArrayList<>();
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        
        // Use even more points for asymptotic functions
        double range = maxX - minX;
        int basePoints = settings.getPlotPoints();
        int adaptivePoints = (int)(basePoints * Math.max(2.0, Math.min(6.0, range / 10.0)));
        adaptivePoints = Math.min(adaptivePoints, 20000); // Higher cap for asymptotic functions
        
        double step = (maxX - minX) / (adaptivePoints - 1);
        
        // For trigonometric functions, also sample around known asymptote locations
        List<Double> asymptoteLocations = new ArrayList<>();
        if (expression.contains("tan") || expression.contains("cot")) {
            // tan(x) has asymptotes at (2n+1)π/2
            double pi = Math.PI;
            for (double k = Math.floor(minX / (pi/2)) - 1; k <= Math.ceil(maxX / (pi/2)) + 1; k++) {
                double asymptote = k * pi / 2;
                if (asymptote >= minX && asymptote <= maxX) {
                    asymptoteLocations.add(asymptote);
                }
            }
        }
        
        // Generate regular points
        for (int i = 0; i < adaptivePoints; i++) {
            double x = minX + i * step;
            addPointIfValid(points, expression, function, x);
        }
        
        // Add extra points around asymptotes
        for (double asymptote : asymptoteLocations) {
            double delta = step * 0.1;
            for (int j = 1; j <= 5; j++) {
                addPointIfValid(points, expression, function, asymptote - j * delta);
                addPointIfValid(points, expression, function, asymptote + j * delta);
            }
        }
        
        // Sort points by x-coordinate
        points.sort((p1, p2) -> Double.compare(p1.getX(), p2.getX()));
        
        return points;
    }
    
    /**
     * Helper method to add a point if it evaluates to a valid value
     */
    private void addPointIfValid(List<Point2D> points, String expression, com.graphing.math.Function function, double x) {
        try {
            double y;
            if (function != null && function.getParsedExpression() != null) {
                y = function.getParsedExpression().evaluate(java.util.Collections.singletonMap("x", x));
            } else {
                y = ExpressionEvaluator.evaluate(expression, x);
            }
            
            // Cap extremely large values to prevent rendering issues
            if (Math.abs(y) > 1000) {
                y = Math.signum(y) * 1000;
            }
            
            if (Double.isFinite(y)) {
                points.add(new Point2D(x, y));
            }
        } catch (Exception e) {
            // Skip points where evaluation fails
        }
    }
    
    /**
     * Draw a curve through a list of points
     */
    public void drawCurve(Graphics2D g2d, List<Point2D> points, Color color, double thickness) {
        if (points.size() < 2) return;
        
        // Set up graphics
        g2d.setColor(color);
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke((float) thickness));
        
        if (settings.isAntiAliasing()) {
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        // Draw the curve
        Point2D prevScreenPoint = null;
        boolean prevVisible = false;
        double prevMathY = Double.NaN; // Initialize to NaN to detect the first point
        
        // Adaptive discontinuity thresholds based on zoom level
        double[] bounds = viewport.getVisibleBounds();
        double range = bounds[1] - bounds[0]; // X range
        double yRange = bounds[3] - bounds[2]; // Y range
        
        // Check if this is an asymptotic function
        boolean isAsymptotic = false;
        if (points.size() > 10) {
            // Check for large value variations that suggest asymptotes
            double maxY = Double.NEGATIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            for (Point2D p : points) {
                if (Double.isFinite(p.getY())) {
                    maxY = Math.max(maxY, p.getY());
                    minY = Math.min(minY, p.getY());
                }
            }
            if (maxY - minY > yRange * 5) {
                isAsymptotic = true;
            }
        }
        
        // Scale thresholds based on zoom level and function type
        double screenDistanceThreshold = Math.max(30, Math.min(200, range * 2)); // Adaptive screen distance threshold
        double yJumpThreshold = isAsymptotic ? Math.max(yRange * 0.5, 50) : Math.max(5, yRange * 0.1); // Larger threshold for asymptotic functions
        
        for (Point2D mathPoint : points) {
            Point2D screenPoint = viewport.mathToScreen(mathPoint.getX(), mathPoint.getY());
            boolean currentVisible = viewport.isPointVisible(mathPoint.getX(), mathPoint.getY());

            if (prevScreenPoint != null) {
                // Check if we should draw a line segment
                boolean shouldDraw = true;

                // Don't draw if both points are outside the visible area
                if (!prevVisible && !currentVisible) {
                    shouldDraw = false;
                }

                // Don't draw if the line segment is too long (discontinuity)
                double distance = prevScreenPoint.distanceTo(screenPoint);
                if (distance > screenDistanceThreshold) { // Adaptive threshold
                    shouldDraw = false;
                }

                // Don't draw if the y-value jumps by more than threshold (likely a discontinuity)
                if (Math.abs(mathPoint.getY() - prevMathY) > yJumpThreshold) { // Adaptive threshold
                    shouldDraw = false;
                }
                
                // For asymptotic functions, be more aggressive about detecting sign changes
                if (isAsymptotic) {
                    // Don't draw if there's a sign change across a large jump (asymptote)
                    if ((Math.abs(mathPoint.getY()) > yJumpThreshold * 0.5 && Math.abs(prevMathY) > yJumpThreshold * 0.5) &&
                        (Math.signum(mathPoint.getY()) != Math.signum(prevMathY))) {
                        shouldDraw = false;
                    }
                } else {
                    // Also don't draw if there's a sign change across a large jump (asymptote)
                    if ((Math.abs(mathPoint.getY()) > yJumpThreshold && Math.abs(prevMathY) > yJumpThreshold) &&
                        (Math.signum(mathPoint.getY()) != Math.signum(prevMathY))) {
                        shouldDraw = false;
                    }
                }
                
                // Never draw a line if either y-value is not finite (asymptote)
                if (!Double.isFinite(mathPoint.getY()) || !Double.isFinite(prevMathY)) {
                    shouldDraw = false;
                }

                if (shouldDraw) {
                    g2d.drawLine((int) prevScreenPoint.getX(), (int) prevScreenPoint.getY(),
                                (int) screenPoint.getX(), (int) screenPoint.getY());
                }
            }

            prevScreenPoint = screenPoint;
            prevVisible = currentVisible;
            prevMathY = mathPoint.getY();
        }
        
        // Restore original stroke
        g2d.setStroke(originalStroke);
    }
    
    /**
     * Draw a function with adaptive sampling for better quality
     */
    public void drawFunctionAdaptive(Graphics2D g2d, String expression, Color color, double thickness) {
        List<Point2D> points = generateAdaptivePoints(expression);
        drawCurve(g2d, points, color, thickness);
    }
    
    /**
     * Generate points with adaptive sampling based on curvature
     */
    private List<Point2D> generateAdaptivePoints(String expression) {
        List<Point2D> points = new ArrayList<>();
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        
        // Base step size - smaller when zoomed out to maintain quality
        double range = maxX - minX;
        double baseStep = viewport.getPlotStepSize();
        
        // Adjust base step for zoom level
        if (range > 100) {
            baseStep = baseStep * 0.5; // Smaller steps when zoomed out
        } else if (range > 50) {
            baseStep = baseStep * 0.75;
        }
        
        double step = baseStep;
        double x = minX;
        
        while (x <= maxX) {
            try {
                double y = ExpressionEvaluator.evaluate(expression, x);
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    points.add(new Point2D(x, y));
                }
                
                // Adaptive step size based on curvature
                double nextX = x + step;
                if (nextX <= maxX) {
                    double nextY = ExpressionEvaluator.evaluate(expression, nextX);
                    if (!Double.isNaN(nextY) && !Double.isInfinite(nextY)) {
                        // Calculate curvature and adjust step size
                        double curvature = Math.abs(nextY - y) / step;
                        double curvatureThreshold = range > 50 ? 0.5 : 1.0; // Lower threshold when zoomed out
                        
                        if (curvature > curvatureThreshold) {
                            step = Math.max(step * 0.5, baseStep * 0.1);
                        } else if (curvature < curvatureThreshold * 0.1) {
                            step = Math.min(step * 1.5, baseStep * 2.0);
                        }
                    }
                }
                
                x += step;
            } catch (Exception e) {
                x += step;
            }
        }
        
        return points;
    }
    
    /**
     * Draw a parametric curve
     */
    public void drawParametricCurve(Graphics2D g2d, String xExpression, String yExpression, 
                                   double tMin, double tMax, Color color, double thickness) {
        List<Point2D> points = new ArrayList<>();
        int numPoints = settings.getPlotPoints();
        double step = (tMax - tMin) / (numPoints - 1);
        
        for (int i = 0; i < numPoints; i++) {
            double t = tMin + i * step;
            try {
                double x = ExpressionEvaluator.evaluate(xExpression, t);
                double y = ExpressionEvaluator.evaluate(yExpression, t);
                if (!Double.isNaN(x) && !Double.isNaN(y) && 
                    !Double.isInfinite(x) && !Double.isInfinite(y)) {
                    points.add(new Point2D(x, y));
                }
            } catch (Exception e) {
                // Skip points where evaluation fails
            }
        }
        
        drawCurve(g2d, points, color, thickness);
    }
    
    /**
     * Draw a polar curve
     */
    public void drawPolarCurve(Graphics2D g2d, String rExpression, double thetaMin, double thetaMax, 
                              Color color, double thickness) {
        List<Point2D> points = new ArrayList<>();
        int numPoints = settings.getPlotPoints();
        double step = (thetaMax - thetaMin) / (numPoints - 1);
        
        for (int i = 0; i < numPoints; i++) {
            double theta = thetaMin + i * step;
            try {
                double r = ExpressionEvaluator.evaluate(rExpression, theta);
                if (!Double.isNaN(r) && !Double.isInfinite(r) && r >= 0) {
                    double x = r * Math.cos(theta);
                    double y = r * Math.sin(theta);
                    points.add(new Point2D(x, y));
                }
            } catch (Exception e) {
                // Skip points where evaluation fails
            }
        }
        
        drawCurve(g2d, points, color, thickness);
    }
    
    /**
     * Draw a scatter plot of points
     */
    public void drawScatterPlot(Graphics2D g2d, List<Point2D> points, Color color, int pointSize) {
        g2d.setColor(color);
        
        for (Point2D mathPoint : points) {
            if (viewport.isPointVisible(mathPoint.getX(), mathPoint.getY())) {
                Point2D screenPoint = viewport.mathToScreen(mathPoint.getX(), mathPoint.getY());
                int x = (int) screenPoint.getX();
                int y = (int) screenPoint.getY();
                
                g2d.fillOval(x - pointSize/2, y - pointSize/2, pointSize, pointSize);
            }
        }
    }
    
    /**
     * Draw a line segment between two mathematical points
     */
    public void drawLineSegment(Graphics2D g2d, Point2D p1, Point2D p2, Color color, double thickness) {
        Point2D screenP1 = viewport.mathToScreen(p1.getX(), p1.getY());
        Point2D screenP2 = viewport.mathToScreen(p2.getX(), p2.getY());
        
        g2d.setColor(color);
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke((float) thickness));
        
        g2d.drawLine((int) screenP1.getX(), (int) screenP1.getY(),
                    (int) screenP2.getX(), (int) screenP2.getY());
        
        g2d.setStroke(originalStroke);
    }
    
    /**
     * Draw a circle at a mathematical point
     */
    public void drawCircle(Graphics2D g2d, Point2D center, double radius, Color color, double thickness) {
        Point2D screenCenter = viewport.mathToScreen(center.getX(), center.getY());
        double screenRadius = viewport.mathToScreenDistance(radius);
        
        g2d.setColor(color);
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke((float) thickness));
        
        int x = (int) (screenCenter.getX() - screenRadius);
        int y = (int) (screenCenter.getY() - screenRadius);
        int diameter = (int) (2 * screenRadius);
        
        g2d.drawOval(x, y, diameter, diameter);
        
        g2d.setStroke(originalStroke);
    }

    private void drawImplicitOrInequality(Graphics2D g2d, com.graphing.math.Function function, Color color) {
        double[] bounds = viewport.getVisibleBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        double minY = bounds[2];
        double maxY = bounds[3];
        int gridSize = 600; // Increased resolution for smoother, more connected contours
        double dx = (maxX - minX) / (gridSize - 1);
        double dy = (maxY - minY) / (gridSize - 1);
        double[][] values = new double[gridSize][gridSize];
        // Sample grid values: left - right
        for (int ix = 0; ix < gridSize; ix++) {
            double x = minX + ix * dx;
            for (int iy = 0; iy < gridSize; iy++) {
                double y = minY + iy * dy;
                try {
                    java.util.Map<String, Double> vars = new java.util.HashMap<>();
                    vars.put("x", x);
                    vars.put("y", y);
                    double left = function.getLeftExpression().evaluate(vars);
                    double right = function.getRightExpression().evaluate(vars);
                    values[ix][iy] = left - right;
                } catch (Exception e) {
                    values[ix][iy] = Double.NaN;
                }
            }
        }
        // Fill inequalities as before
        if (function.getType() == com.graphing.math.FunctionType.INEQUALITY) {
            int fillAlpha = 80;
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
            for (int ix = 0; ix < gridSize; ix++) {
                double x = minX + ix * dx;
                boolean inRegion = false;
                int yStartPix = 0;
                for (int iy = 0; iy < gridSize; iy++) {
                    double y = minY + iy * dy;
                    double v = values[ix][iy];
                    boolean holds = false;
                    String op = function.getOperator();
                    switch (op) {
                        case ">": holds = v > 0; break;
                        case "<": holds = v < 0; break;
                        case ">=": holds = v >= 0; break;
                        case "<=": holds = v <= 0; break;
                    }
                    Point2D screen = viewport.mathToScreen(x, y);
                    int yPix = (int)screen.getY();
                    if (holds && !inRegion) {
                        inRegion = true;
                        yStartPix = yPix;
                    } else if (!holds && inRegion) {
                        inRegion = false;
                        int yEndPix = yPix;
                        g2d.fillRect((int)screen.getX(), Math.min(yStartPix, yEndPix), 2, Math.abs(yEndPix - yStartPix));
                    }
                    if (iy == gridSize - 1 && inRegion) {
                        int yEndPix = yPix;
                        g2d.fillRect((int)screen.getX(), Math.min(yStartPix, yEndPix), 2, Math.abs(yEndPix - yStartPix));
                    }
                }
            }
        }
        // Draw contour using marching squares
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2.5f)); // Thicker boundary for visibility
        java.util.List<java.awt.geom.Path2D> contours = computeMarchingSquaresContours(values, minX, minY, dx, dy, gridSize, 0.0);
        for (java.awt.geom.Path2D path : contours) {
            g2d.draw(path);
        }
        g2d.setStroke(originalStroke);
    }

    // Helper class for spatial hashing of points
    private static class PointKey {
        final double x, y;
        private static final double TOL = 1e-2; // Tolerance for matching
        PointKey(java.awt.geom.Point2D pt) {
            this.x = Math.round(pt.getX() / TOL) * TOL;
            this.y = Math.round(pt.getY() / TOL) * TOL;
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof PointKey)) return false;
            PointKey p = (PointKey)o;
            return Math.abs(x - p.x) < TOL && Math.abs(y - p.y) < TOL;
        }
        @Override public int hashCode() {
            return Double.hashCode(x) * 31 + Double.hashCode(y);
        }
    }

    // Marching squares: returns a list of Path2D contours for the zero level set, with robust contour stitching
    private java.util.List<java.awt.geom.Path2D> computeMarchingSquaresContours(double[][] values, double minX, double minY, double dx, double dy, int gridSize, double level) {
        class Segment {
            java.awt.geom.Point2D a, b;
            Segment(java.awt.geom.Point2D a, java.awt.geom.Point2D b) { this.a = a; this.b = b; }
        }
        java.util.Map<PointKey, java.util.List<Segment>> startMap = new java.util.HashMap<>();
        java.util.Map<PointKey, java.util.List<Segment>> endMap = new java.util.HashMap<>();
        java.util.List<Segment> allSegments = new java.util.ArrayList<>();
        // For each cell, find contour segments and store endpoints
        for (int ix = 0; ix < gridSize - 1; ix++) {
            for (int iy = 0; iy < gridSize - 1; iy++) {
                double v00 = values[ix][iy];
                double v10 = values[ix+1][iy];
                double v01 = values[ix][iy+1];
                double v11 = values[ix+1][iy+1];
                int state = 0;
                if (v00 > level) state |= 1;
                if (v10 > level) state |= 2;
                if (v11 > level) state |= 4;
                if (v01 > level) state |= 8;
                if (state == 0 || state == 15) continue; // No crossing
                double x0 = minX + ix * dx;
                double y0 = minY + iy * dy;
                double x1 = x0 + dx;
                double y1 = y0 + dy;
                java.awt.geom.Point2D[] pts = new java.awt.geom.Point2D[4];
                pts[0] = mathToScreenPoint(interp(x0, y0, x1, y0, v00, v10, level));
                pts[1] = mathToScreenPoint(interp(x1, y0, x1, y1, v10, v11, level));
                pts[2] = mathToScreenPoint(interp(x1, y1, x0, y1, v11, v01, level));
                pts[3] = mathToScreenPoint(interp(x0, y1, x0, y0, v01, v00, level));
                // For each segment, add to segment lists
                java.util.List<int[]> segCases = new java.util.ArrayList<>();
                switch (state) {
                    case 1: case 14: segCases.add(new int[]{0,3}); break;
                    case 2: case 13: segCases.add(new int[]{0,1}); break;
                    case 3: case 12: segCases.add(new int[]{1,3}); break;
                    case 4: case 11: segCases.add(new int[]{1,2}); break;
                    case 5: segCases.add(new int[]{0,1}); segCases.add(new int[]{2,3}); break;
                    case 6: case 9: segCases.add(new int[]{0,2}); break;
                    case 7: case 8: segCases.add(new int[]{2,3}); break;
                    case 10: segCases.add(new int[]{0,1}); segCases.add(new int[]{2,3}); break;
                }
                for (int[] seg : segCases) {
                    Segment s = new Segment(pts[seg[0]], pts[seg[1]]);
                    allSegments.add(s);
                    startMap.computeIfAbsent(new PointKey(s.a), k -> new java.util.ArrayList<>()).add(s);
                    endMap.computeIfAbsent(new PointKey(s.b), k -> new java.util.ArrayList<>()).add(s);
                }
            }
        }
        // Stitch segments into polylines using spatial hash
        java.util.Set<Segment> used = new java.util.HashSet<>();
        java.util.List<java.util.List<java.awt.geom.Point2D>> polylines = new java.util.ArrayList<>();
        for (Segment seg : allSegments) {
            if (used.contains(seg)) continue;
            java.util.List<java.awt.geom.Point2D> poly = new java.util.ArrayList<>();
            poly.add(seg.a);
            poly.add(seg.b);
            used.add(seg);
            // Extend forward
            java.awt.geom.Point2D curr = seg.b;
            while (true) {
                java.util.List<Segment> nexts = startMap.get(new PointKey(curr));
                Segment next = null;
                if (nexts != null) {
                    for (Segment cand : nexts) {
                        if (!used.contains(cand) && (cand.a.distance(curr) < 1e-1)) {
                            next = cand; break;
                        }
                    }
                }
                if (next == null) break;
                poly.add(next.b);
                used.add(next);
                curr = next.b;
            }
            // Extend backward
            curr = seg.a;
            while (true) {
                java.util.List<Segment> prevs = endMap.get(new PointKey(curr));
                Segment prev = null;
                if (prevs != null) {
                    for (Segment cand : prevs) {
                        if (!used.contains(cand) && (cand.b.distance(curr) < 1e-1)) {
                            prev = cand; break;
                        }
                    }
                }
                if (prev == null) break;
                poly.add(0, prev.a);
                used.add(prev);
                curr = prev.a;
            }
            if (poly.size() > 1) polylines.add(poly);
        }
        // Convert polylines to Path2D
        java.util.List<java.awt.geom.Path2D> contours = new java.util.ArrayList<>();
        for (java.util.List<java.awt.geom.Point2D> polyline : polylines) {
            if (polyline.size() < 2) continue;
            java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
            path.moveTo(polyline.get(0).getX(), polyline.get(0).getY());
            for (int i = 1; i < polyline.size(); i++) {
                path.lineTo(polyline.get(i).getX(), polyline.get(i).getY());
            }
            contours.add(path);
        }
        return contours;
    }

    // Add a segment to the map (key: start, value: end)
    private void addSegment(java.util.Map<String, java.awt.geom.Point2D> map, java.awt.geom.Point2D start, java.awt.geom.Point2D end) {
        map.put(keyFor(snapped(start), snapped(end)), snapped(end));
    }
    // Create a unique key for a segment, using snapped (rounded) coordinates
    private String keyFor(java.awt.geom.Point2D a, java.awt.geom.Point2D b) {
        return a.getX() + "," + a.getY() + ":" + b.getX() + "," + b.getY();
    }
    // Parse a key back to a point
    private java.awt.geom.Point2D parseKey(String key) {
        String[] parts = key.split(":")[0].split(",");
        return new java.awt.geom.Point2D.Double(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }
    // Snap a point to a grid by rounding to 2 decimal places
    private java.awt.geom.Point2D snapped(java.awt.geom.Point2D pt) {
        double x = Math.round(pt.getX() * 100.0) / 100.0;
        double y = Math.round(pt.getY() * 100.0) / 100.0;
        return new java.awt.geom.Point2D.Double(x, y);
    }

    // Linear interpolation helper
    private java.awt.geom.Point2D interp(double x0, double y0, double x1, double y1, double v0, double v1, double level) {
        double t = (level - v0) / (v1 - v0 + 1e-12);
        return new java.awt.geom.Point2D.Double(x0 + t * (x1 - x0), y0 + t * (y1 - y0));
    }

    // Helper to convert math point to screen point
    private java.awt.geom.Point2D mathToScreenPoint(java.awt.geom.Point2D mathPt) {
        Point2D screen = viewport.mathToScreen(mathPt.getX(), mathPt.getY());
        return new java.awt.geom.Point2D.Double(screen.getX(), screen.getY());
    }
} 