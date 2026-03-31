package com.graphing.math.analysis;

import com.graphing.math.Point2D;
import com.graphing.math.parser.ExpressionEvaluator;
import com.graphing.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes intersections between multiple functions
 */
public class IntersectionFinder {
    
    private static final double DEFAULT_TOLERANCE = 1e-6;
    private static final int DEFAULT_ITERATIONS = 100;
    
    /**
     * Find intersections between two functions in a given range
     */
    public static List<Point2D> findIntersections(String f1, String f2, double startX, double endX, int points) {
        List<Point2D> intersections = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points - 1; i++) {
            double x1 = startX + i * step;
            double x2 = startX + (i + 1) * step;
            
            try {
                double y1_1 = ExpressionEvaluator.evaluate(f1, x1);
                double y1_2 = ExpressionEvaluator.evaluate(f1, x2);
                double y2_1 = ExpressionEvaluator.evaluate(f2, x1);
                double y2_2 = ExpressionEvaluator.evaluate(f2, x2);
                
                // Check if there's a sign change in the difference
                double diff1 = y1_1 - y2_1;
                double diff2 = y1_2 - y2_2;
                
                if ((diff1 > 0 && diff2 < 0) || (diff1 < 0 && diff2 > 0)) {
                    // There's an intersection in this interval
                    double intersectionX = findIntersectionPoint(f1, f2, x1, x2);
                    if (intersectionX >= startX && intersectionX <= endX) {
                        double intersectionY = ExpressionEvaluator.evaluate(f1, intersectionX);
                        intersections.add(new Point2D(intersectionX, intersectionY));
                    }
                }
            } catch (Exception e) {
                // Skip intervals where evaluation fails
            }
        }
        
        return intersections;
    }
    
    /**
     * Find intersections between a function and the x-axis (roots)
     */
    public static List<Point2D> findRoots(String expression, double startX, double endX, int points) {
        List<Point2D> roots = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points - 1; i++) {
            double x1 = startX + i * step;
            double x2 = startX + (i + 1) * step;
            
            try {
                double y1 = ExpressionEvaluator.evaluate(expression, x1);
                double y2 = ExpressionEvaluator.evaluate(expression, x2);
                
                // Check if there's a sign change (root)
                if ((y1 > 0 && y2 < 0) || (y1 < 0 && y2 > 0)) {
                    double rootX = findRootPoint(expression, x1, x2);
                    if (rootX >= startX && rootX <= endX) {
                        roots.add(new Point2D(rootX, 0));
                    }
                }
            } catch (Exception e) {
                // Skip intervals where evaluation fails
            }
        }
        
        return roots;
    }
    
    /**
     * Find intersections between a function and a horizontal line
     */
    public static List<Point2D> findHorizontalIntersections(String expression, double y, double startX, double endX, int points) {
        String horizontalLine = String.valueOf(y);
        return findIntersections(expression, horizontalLine, startX, endX, points);
    }
    
    /**
     * Find intersections between a function and a vertical line
     */
    public static List<Point2D> findVerticalIntersections(String expression, double x, double startY, double endY, int points) {
        List<Point2D> intersections = new ArrayList<>();
        double step = (endY - startY) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double y = startY + i * step;
            try {
                double functionY = ExpressionEvaluator.evaluate(expression, x);
                if (MathUtils.approximatelyEqual(functionY, y)) {
                    intersections.add(new Point2D(x, y));
                }
            } catch (Exception e) {
                // Skip points where evaluation fails
            }
        }
        
        return intersections;
    }
    
    /**
     * Find all intersections between multiple functions
     */
    public static List<Point2D> findAllIntersections(List<String> functions, double startX, double endX, int points) {
        List<Point2D> allIntersections = new ArrayList<>();
        
        for (int i = 0; i < functions.size(); i++) {
            for (int j = i + 1; j < functions.size(); j++) {
                List<Point2D> intersections = findIntersections(functions.get(i), functions.get(j), startX, endX, points);
                allIntersections.addAll(intersections);
            }
        }
        
        return allIntersections;
    }
    
    /**
     * Find the intersection point using bisection method
     */
    private static double findIntersectionPoint(String f1, String f2, double a, double b) {
        double tolerance = DEFAULT_TOLERANCE;
        int maxIterations = DEFAULT_ITERATIONS;
        
        for (int i = 0; i < maxIterations; i++) {
            double c = (a + b) / 2;
            
            try {
                double diff = ExpressionEvaluator.evaluate(f1, c) - ExpressionEvaluator.evaluate(f2, c);
                
                if (Math.abs(diff) < tolerance) {
                    return c;
                }
                
                double diffA = ExpressionEvaluator.evaluate(f1, a) - ExpressionEvaluator.evaluate(f2, a);
                
                if ((diff > 0 && diffA > 0) || (diff < 0 && diffA < 0)) {
                    a = c;
                } else {
                    b = c;
                }
            } catch (Exception e) {
                break;
            }
        }
        
        return (a + b) / 2;
    }
    
    /**
     * Find the root point using bisection method
     */
    private static double findRootPoint(String expression, double a, double b) {
        double tolerance = DEFAULT_TOLERANCE;
        int maxIterations = DEFAULT_ITERATIONS;
        
        for (int i = 0; i < maxIterations; i++) {
            double c = (a + b) / 2;
            
            try {
                double fc = ExpressionEvaluator.evaluate(expression, c);
                
                if (Math.abs(fc) < tolerance) {
                    return c;
                }
                
                double fa = ExpressionEvaluator.evaluate(expression, a);
                
                if ((fc > 0 && fa > 0) || (fc < 0 && fa < 0)) {
                    a = c;
                } else {
                    b = c;
                }
            } catch (Exception e) {
                break;
            }
        }
        
        return (a + b) / 2;
    }
    
    /**
     * Check if two functions intersect at a given point
     */
    public static boolean functionsIntersectAt(String f1, String f2, double x) {
        try {
            double y1 = ExpressionEvaluator.evaluate(f1, x);
            double y2 = ExpressionEvaluator.evaluate(f2, x);
            return MathUtils.approximatelyEqual(y1, y2);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Find the number of intersections between two functions in a range
     */
    public static int countIntersections(String f1, String f2, double startX, double endX, int points) {
        return findIntersections(f1, f2, startX, endX, points).size();
    }
} 