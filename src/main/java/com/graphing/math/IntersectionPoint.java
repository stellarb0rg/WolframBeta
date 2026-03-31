package com.graphing.math;

import java.awt.Color;

/**
 * Represents an intersection point with a specific color
 */
public class IntersectionPoint {
    private final Point2D point;
    private final Color color;
    private final int index;
    // Now store all intersecting function expressions
    private final java.util.List<String> functionExprs;
    
    public IntersectionPoint(Point2D point, Color color, int index, java.util.List<String> functionExprs) {
        this.point = point;
        this.color = color;
        this.index = index;
        this.functionExprs = functionExprs;
    }
    // Backward compatibility constructor for two functions
    public IntersectionPoint(Point2D point, Color color, int index, String functionExpr1, String functionExpr2) {
        this(point, color, index, java.util.Arrays.asList(functionExpr1, functionExpr2));
    }
    // Backward compatibility constructor for old usage
    public IntersectionPoint(Point2D point, Color color, int index) {
        this(point, color, index, new java.util.ArrayList<>());
    }
    
    public Point2D getPoint() {
        return point;
    }
    
    public Color getColor() {
        return color;
    }
    
    public int getIndex() {
        return index;
    }
    
    public double getX() {
        return point.getX();
    }
    
    public double getY() {
        return point.getY();
    }
    // New getter
    public java.util.List<String> getFunctionExprs() {
        return functionExprs;
    }
} 