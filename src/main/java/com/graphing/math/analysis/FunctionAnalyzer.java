package com.graphing.math.analysis;

import com.graphing.math.Point2D;
import com.graphing.math.calculus.CriticalPointFinder;
import com.graphing.math.calculus.DerivativeCalculator;
import com.graphing.math.calculus.IntegralCalculator;
import com.graphing.math.parser.ExpressionEvaluator;
import com.graphing.utils.MathUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * General analysis utilities for mathematical functions.
 * Provides comprehensive analysis including domain, range, behavior, and properties.
 */
public class FunctionAnalyzer {
    
    private static final int DEFAULT_POINTS = 1000;
    
    /**
     * Analyze a function and return comprehensive information
     */
    public static Map<String, Object> analyzeFunction(String expression, double startX, double endX) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Basic function properties
            analysis.put("expression", expression);
            analysis.put("domain", new double[]{startX, endX});
            
            // Critical points
            List<Point2D> criticalPoints = CriticalPointFinder.findCriticalPoints(expression, startX, endX, DEFAULT_POINTS);
            analysis.put("criticalPoints", criticalPoints);
            
            // Extrema
            List<Point2D> maxima = CriticalPointFinder.findLocalMaxima(expression, startX, endX, DEFAULT_POINTS);
            List<Point2D> minima = CriticalPointFinder.findLocalMinima(expression, startX, endX, DEFAULT_POINTS);
            analysis.put("localMaxima", maxima);
            analysis.put("localMinima", minima);
            
            // Global extrema
            Point2D globalMax = CriticalPointFinder.findGlobalMaximum(expression, startX, endX, DEFAULT_POINTS);
            Point2D globalMin = CriticalPointFinder.findGlobalMinimum(expression, startX, endX, DEFAULT_POINTS);
            analysis.put("globalMaximum", globalMax);
            analysis.put("globalMinimum", globalMin);
            
            // Inflection points
            List<Point2D> inflectionPoints = CriticalPointFinder.findInflectionPoints(expression, startX, endX, DEFAULT_POINTS);
            analysis.put("inflectionPoints", inflectionPoints);
            
            // Roots
            List<Point2D> roots = IntersectionFinder.findRoots(expression, startX, endX, DEFAULT_POINTS);
            analysis.put("roots", roots);
            
            // Y-intercept
            Point2D yIntercept = findYIntercept(expression);
            analysis.put("yIntercept", yIntercept);
            
            // Function behavior
            analysis.put("isContinuous", isContinuous(expression, startX, endX, DEFAULT_POINTS));
            analysis.put("isDifferentiable", isDifferentiable(expression, startX, endX, DEFAULT_POINTS));
            
            // Calculus properties
            analysis.put("averageValue", IntegralCalculator.averageValue(expression, startX, endX));
            analysis.put("rootMeanSquare", IntegralCalculator.rootMeanSquare(expression, startX, endX));
            
            // Symmetry
            analysis.put("isEven", isEvenFunction(expression));
            analysis.put("isOdd", isOddFunction(expression));
            
            // Asymptotes
            analysis.put("verticalAsymptotes", findVerticalAsymptotes(expression, startX, endX, DEFAULT_POINTS));
            analysis.put("horizontalAsymptotes", findHorizontalAsymptotes(expression));
            
        } catch (Exception e) {
            analysis.put("error", e.getMessage());
        }
        
        return analysis;
    }
    
    /**
     * Find the y-intercept of a function
     */
    public static Point2D findYIntercept(String expression) {
        try {
            double y = ExpressionEvaluator.evaluate(expression, 0);
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                return new Point2D(0, y);
            }
        } catch (Exception e) {
            // Function evaluation failed
        }
        return null;
    }
    
    /**
     * Check if a function is continuous over a range
     */
    public static boolean isContinuous(String expression, double startX, double endX, int points) {
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double y = ExpressionEvaluator.evaluate(expression, x);
                if (Double.isNaN(y) || Double.isInfinite(y)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if a function is differentiable over a range
     */
    public static boolean isDifferentiable(String expression, double startX, double endX, int points) {
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            if (!DerivativeCalculator.isDifferentiable(expression, x)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if a function is even (f(-x) = f(x))
     */
    public static boolean isEvenFunction(String expression) {
        try {
            double testX = 1.0;
            double fX = ExpressionEvaluator.evaluate(expression, testX);
            double fNegX = ExpressionEvaluator.evaluate(expression, -testX);
            return MathUtils.approximatelyEqual(fX, fNegX);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a function is odd (f(-x) = -f(x))
     */
    public static boolean isOddFunction(String expression) {
        try {
            double testX = 1.0;
            double fX = ExpressionEvaluator.evaluate(expression, testX);
            double fNegX = ExpressionEvaluator.evaluate(expression, -testX);
            return MathUtils.approximatelyEqual(fX, -fNegX);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Find vertical asymptotes in a range
     */
    public static List<Double> findVerticalAsymptotes(String expression, double startX, double endX, int points) {
        List<Double> asymptotes = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points - 1; i++) {
            double x1 = startX + i * step;
            double x2 = startX + (i + 1) * step;
            
            try {
                double y1 = ExpressionEvaluator.evaluate(expression, x1);
                double y2 = ExpressionEvaluator.evaluate(expression, x2);
                
                // Check for vertical asymptote (function approaches infinity)
                if (Double.isInfinite(y1) || Double.isInfinite(y2) ||
                    Math.abs(y1 - y2) > 1000) { // Large jump indicates asymptote
                    asymptotes.add((x1 + x2) / 2);
                }
            } catch (Exception e) {
                // Skip points where evaluation fails
            }
        }
        
        return asymptotes;
    }
    
    /**
     * Find horizontal asymptotes
     */
    public static List<Double> findHorizontalAsymptotes(String expression) {
        List<Double> asymptotes = new ArrayList<>();
        
        try {
            // Check limits as x approaches infinity
            double limitPosInf = evaluateLimit(expression, Double.POSITIVE_INFINITY);
            double limitNegInf = evaluateLimit(expression, Double.NEGATIVE_INFINITY);
            
            if (!Double.isNaN(limitPosInf) && !Double.isInfinite(limitPosInf)) {
                asymptotes.add(limitPosInf);
            }
            
            if (!Double.isNaN(limitNegInf) && !Double.isInfinite(limitNegInf) &&
                !MathUtils.approximatelyEqual(limitPosInf, limitNegInf)) {
                asymptotes.add(limitNegInf);
            }
        } catch (Exception e) {
            // Limit calculation failed
        }
        
        return asymptotes;
    }
    
    /**
     * Evaluate the limit of a function as x approaches a value
     */
    private static double evaluateLimit(String expression, double limit) {
        try {
            // For infinity limits, use large numbers
            double x;
            if (limit == Double.POSITIVE_INFINITY) {
                x = 1e6;
            } else if (limit == Double.NEGATIVE_INFINITY) {
                x = -1e6;
            } else {
                x = limit;
            }
            
            return ExpressionEvaluator.evaluate(expression, x);
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    /**
     * Get the period of a periodic function
     */
    public static double getPeriod(String expression) {
        // This is a simplified implementation
        // For trigonometric functions, we can detect the period
        String lowerExpr = expression.toLowerCase();
        
        if (lowerExpr.contains("sin") || lowerExpr.contains("cos")) {
            // Check for sin(ax) or cos(ax) where period = 2π/a
            if (lowerExpr.contains("sin(x)") || lowerExpr.contains("cos(x)")) {
                return 2 * Math.PI;
            }
            // For more complex cases, return a default
            return 2 * Math.PI;
        }
        
        return Double.NaN; // Not periodic or unknown period
    }
    
    /**
     * Check if a function is periodic
     */
    public static boolean isPeriodic(String expression) {
        return !Double.isNaN(getPeriod(expression));
    }
    
    /**
     * Get the range of a function over a domain
     */
    public static double[] getRange(String expression, double startX, double endX, int points) {
        Point2D globalMax = CriticalPointFinder.findGlobalMaximum(expression, startX, endX, points);
        Point2D globalMin = CriticalPointFinder.findGlobalMinimum(expression, startX, endX, points);
        
        if (globalMax != null && globalMin != null) {
            return new double[]{globalMin.getY(), globalMax.getY()};
        }
        
        return new double[]{Double.NaN, Double.NaN};
    }
} 