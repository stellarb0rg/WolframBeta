package com.graphing.math.calculus;

import com.graphing.math.Point2D;
import com.graphing.math.parser.ExpressionEvaluator;
import com.graphing.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes derivatives numerically using finite difference methods.
 * Supports first and second derivatives with configurable precision.
 */
public class DerivativeCalculator {
    
    private static final double DEFAULT_H = 1e-8;
    
    /**
     * Calculate the derivative of a function at a given point using central difference
     */
    public static double derivative(String expression, double x) throws IllegalArgumentException {
        return derivative(expression, x, DEFAULT_H);
    }
    
    /**
     * Calculate the derivative using central difference with custom step size
     */
    public static double derivative(String expression, double x, double h) throws IllegalArgumentException {
        try {
            double fPlusH = ExpressionEvaluator.evaluate(expression, x + h);
            double fMinusH = ExpressionEvaluator.evaluate(expression, x - h);
            return (fPlusH - fMinusH) / (2 * h);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error calculating derivative: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate the derivative function over a range
     */
    public static List<Point2D> derivativeFunction(String expression, double startX, double endX, int points) {
        List<Point2D> derivativePoints = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            try {
                double derivative = derivative(expression, x);
                if (!Double.isNaN(derivative) && !Double.isInfinite(derivative)) {
                    derivativePoints.add(new Point2D(x, derivative));
                }
            } catch (Exception e) {
                // Skip points where derivative cannot be calculated
            }
        }
        
        return derivativePoints;
    }
    
    /**
     * Calculate the second derivative at a point
     */
    public static double secondDerivative(String expression, double x) throws IllegalArgumentException {
        return secondDerivative(expression, x, DEFAULT_H);
    }
    
    /**
     * Calculate the second derivative using central difference
     */
    public static double secondDerivative(String expression, double x, double h) throws IllegalArgumentException {
        try {
            double fPlusH = ExpressionEvaluator.evaluate(expression, x + h);
            double fMinusH = ExpressionEvaluator.evaluate(expression, x - h);
            double fX = ExpressionEvaluator.evaluate(expression, x);
            return (fPlusH - 2 * fX + fMinusH) / (h * h);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error calculating second derivative: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a function is differentiable at a point
     */
    public static boolean isDifferentiable(String expression, double x) {
        try {
            double derivative = derivative(expression, x);
            return !Double.isNaN(derivative) && !Double.isInfinite(derivative);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Find points where the function is not differentiable
     */
    public static List<Double> findNonDifferentiablePoints(String expression, double startX, double endX, int points) {
        List<Double> nonDifferentiablePoints = new ArrayList<>();
        double step = (endX - startX) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            double x = startX + i * step;
            if (!isDifferentiable(expression, x)) {
                nonDifferentiablePoints.add(x);
            }
        }
        
        return nonDifferentiablePoints;
    }
    
    /**
     * Calculate the slope of the tangent line at a point
     */
    public static double slopeAtPoint(String expression, double x) throws IllegalArgumentException {
        return derivative(expression, x);
    }
    
    /**
     * Calculate the equation of the tangent line at a point
     */
    public static String tangentLineEquation(String expression, double x) throws IllegalArgumentException {
        try {
            double y = ExpressionEvaluator.evaluate(expression, x);
            double slope = derivative(expression, x);
            double b = y - slope * x;
            
            if (MathUtils.approximatelyEqual(b, 0)) {
                return String.format("y = %.4f * x", slope);
            } else if (b > 0) {
                return String.format("y = %.4f * x + %.4f", slope, b);
            } else {
                return String.format("y = %.4f * x - %.4f", slope, Math.abs(b));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error calculating tangent line: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculates the normal line slope at a point.
     */
    public double normalSlope(String expression, double x) {
        double derivative = derivative(expression, x);
        if (MathUtils.isZero(derivative)) {
            return Double.POSITIVE_INFINITY; // Vertical line
        }
        return -1.0 / derivative;
    }
} 