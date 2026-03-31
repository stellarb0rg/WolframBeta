package com.graphing.math.calculus;

import com.graphing.math.Point2D;
import com.graphing.math.parser.ExpressionEvaluator;
import com.graphing.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds maxima, minima, and inflection points of functions
 */
public class CriticalPointFinder {
    
    private static final double DEFAULT_TOLERANCE = 1e-6;
    private static final int DEFAULT_ITERATIONS = 100;
    
    /**
     * Find all critical points (where derivative is zero or undefined)
     */
    public static List<Point2D> findCriticalPoints(String expression, double startX, double endX, int points) {
        List<Point2D> criticalPoints = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double derivative = DerivativeCalculator.derivative(expression, x);
                if (MathUtils.approximatelyEqual(derivative, 0)) {
                    double y = ExpressionEvaluator.evaluate(expression, x);
                    criticalPoints.add(new Point2D(x, y));
                }
            } catch (Exception e) {
                // Skip points where derivative cannot be calculated
            }
        }
        
        return criticalPoints;
    }
    
    /**
     * Find local maxima using Newton's method
     */
    public static List<Point2D> findLocalMaxima(String expression, double startX, double endX, int points) {
        List<Point2D> maxima = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double criticalX = findCriticalPoint(expression, x);
                if (criticalX >= startX && criticalX <= endX) {
                    double secondDerivative = DerivativeCalculator.secondDerivative(expression, criticalX);
                    if (secondDerivative < 0) { // Local maximum
                        double y = ExpressionEvaluator.evaluate(expression, criticalX);
                        maxima.add(new Point2D(criticalX, y));
                    }
                }
            } catch (Exception e) {
                // Skip points where calculation fails
            }
        }
        
        return maxima;
    }
    
    /**
     * Find local minima using Newton's method
     */
    public static List<Point2D> findLocalMinima(String expression, double startX, double endX, int points) {
        List<Point2D> minima = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double criticalX = findCriticalPoint(expression, x);
                if (criticalX >= startX && criticalX <= endX) {
                    double secondDerivative = DerivativeCalculator.secondDerivative(expression, criticalX);
                    if (secondDerivative > 0) { // Local minimum
                        double y = ExpressionEvaluator.evaluate(expression, criticalX);
                        minima.add(new Point2D(criticalX, y));
                    }
                }
            } catch (Exception e) {
                // Skip points where calculation fails
            }
        }
        
        return minima;
    }
    
    /**
     * Find inflection points (where second derivative is zero)
     */
    public static List<Point2D> findInflectionPoints(String expression, double startX, double endX, int points) {
        List<Point2D> inflectionPoints = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double secondDerivative = DerivativeCalculator.secondDerivative(expression, x);
                if (MathUtils.approximatelyEqual(secondDerivative, 0)) {
                    double y = ExpressionEvaluator.evaluate(expression, x);
                    inflectionPoints.add(new Point2D(x, y));
                }
            } catch (Exception e) {
                // Skip points where calculation fails
            }
        }
        
        return inflectionPoints;
    }
    
    /**
     * Find global maximum in a range
     */
    public static Point2D findGlobalMaximum(String expression, double startX, double endX, int points) {
        Point2D globalMax = null;
        double maxValue = Double.NEGATIVE_INFINITY;
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double y = ExpressionEvaluator.evaluate(expression, x);
                if (y > maxValue && !Double.isNaN(y) && !Double.isInfinite(y)) {
                    maxValue = y;
                    globalMax = new Point2D(x, y);
                }
            } catch (Exception e) {
                // Skip invalid points
            }
        }
        
        return globalMax;
    }
    
    /**
     * Find global minimum in a range
     */
    public static Point2D findGlobalMinimum(String expression, double startX, double endX, int points) {
        Point2D globalMin = null;
        double minValue = Double.POSITIVE_INFINITY;
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double y = ExpressionEvaluator.evaluate(expression, x);
                if (y < minValue && !Double.isNaN(y) && !Double.isInfinite(y)) {
                    minValue = y;
                    globalMin = new Point2D(x, y);
                }
            } catch (Exception e) {
                // Skip invalid points
            }
        }
        
        return globalMin;
    }
    
    /**
     * Find critical points using Newton's method
     */
    private static double findCriticalPoint(String expression, double initialGuess) {
        double x = initialGuess;
        double tolerance = DEFAULT_TOLERANCE;
        int maxIterations = DEFAULT_ITERATIONS;
        
        for (int i = 0; i < maxIterations; i++) {
            try {
                double derivative = DerivativeCalculator.derivative(expression, x);
                double secondDerivative = DerivativeCalculator.secondDerivative(expression, x);
                
                if (MathUtils.approximatelyEqual(secondDerivative, 0)) {
                    break; // Avoid division by zero
                }
                
                double nextX = x - derivative / secondDerivative;
                
                if (Math.abs(nextX - x) < tolerance) {
                    return nextX;
                }
                
                x = nextX;
            } catch (Exception e) {
                break;
            }
        }
        
        return x;
    }
    
    /**
     * Classify a critical point (maximum, minimum, or saddle point)
     */
    public static String classifyCriticalPoint(String expression, double x) {
        try {
            double secondDerivative = DerivativeCalculator.secondDerivative(expression, x);
            
            if (secondDerivative > 0) {
                return "Local Minimum";
            } else if (secondDerivative < 0) {
                return "Local Maximum";
            } else {
                return "Saddle Point or Inflection Point";
            }
        } catch (Exception e) {
            return "Undefined";
        }
    }
    
    /**
     * Find all extrema (maxima and minima) in a range
     */
    public static List<Point2D> findAllExtrema(String expression, double startX, double endX, int points) {
        List<Point2D> extrema = new ArrayList<>();
        extrema.addAll(findLocalMaxima(expression, startX, endX, points));
        extrema.addAll(findLocalMinima(expression, startX, endX, points));
        return extrema;
    }
    
    /**
     * Check if a point is a critical point
     */
    public static boolean isCriticalPoint(String expression, double x) {
        try {
            double derivative = DerivativeCalculator.derivative(expression, x);
            return MathUtils.approximatelyEqual(derivative, 0);
        } catch (Exception e) {
            return false;
        }
    }
} 