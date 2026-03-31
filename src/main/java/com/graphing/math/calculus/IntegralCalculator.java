package com.graphing.math.calculus;

import com.graphing.math.parser.ExpressionEvaluator;
import com.graphing.utils.MathUtils;

/**
 * Computes definite integrals numerically using various methods
 */
public class IntegralCalculator {
    
    private static final int DEFAULT_PARTITIONS = 1000;
    
    /**
     * Calculate definite integral using Simpson's rule
     */
    public static double definiteIntegral(String expression, double a, double b) throws IllegalArgumentException {
        return definiteIntegral(expression, a, b, DEFAULT_PARTITIONS);
    }
    
    /**
     * Calculate definite integral using Simpson's rule with custom partitions
     */
    public static double definiteIntegral(String expression, double a, double b, int n) throws IllegalArgumentException {
        if (n % 2 != 0) {
            n++; // Ensure even number of partitions for Simpson's rule
        }
        
        double h = (b - a) / n;
        double sum = 0.0;
        
        try {
            // Simpson's rule: ∫[a,b] f(x)dx ≈ (h/3)[f(a) + 4f(a+h) + 2f(a+2h) + 4f(a+3h) + ... + f(b)]
            sum += ExpressionEvaluator.evaluate(expression, a);
            
            for (int i = 1; i < n; i++) {
                double x = a + i * h;
                double fx = ExpressionEvaluator.evaluate(expression, x);
                sum += (i % 2 == 0 ? 2 : 4) * fx;
            }
            
            sum += ExpressionEvaluator.evaluate(expression, b);
            return (h / 3.0) * sum;
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Error calculating integral: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate definite integral using trapezoidal rule
     */
    public static double trapezoidalIntegral(String expression, double a, double b, int n) throws IllegalArgumentException {
        double h = (b - a) / n;
        double sum = 0.0;
        
        try {
            sum += ExpressionEvaluator.evaluate(expression, a) / 2.0;
            
            for (int i = 1; i < n; i++) {
                double x = a + i * h;
                sum += ExpressionEvaluator.evaluate(expression, x);
            }
            
            sum += ExpressionEvaluator.evaluate(expression, b) / 2.0;
            return h * sum;
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Error calculating trapezoidal integral: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate definite integral using midpoint rule
     */
    public static double midpointIntegral(String expression, double a, double b, int n) throws IllegalArgumentException {
        double h = (b - a) / n;
        double sum = 0.0;
        
        try {
            for (int i = 0; i < n; i++) {
                double x = a + (i + 0.5) * h;
                sum += ExpressionEvaluator.evaluate(expression, x);
            }
            
            return h * sum;
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Error calculating midpoint integral: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate the area between two functions over an interval
     */
    public static double areaBetweenFunctions(String f1, String f2, double a, double b) throws IllegalArgumentException {
        return definiteIntegral("abs(" + f1 + " - " + f2 + ")", a, b);
    }
    
    /**
     * Calculate the area under a curve (absolute value to handle negative areas)
     */
    public static double areaUnderCurve(String expression, double a, double b) throws IllegalArgumentException {
        return definiteIntegral("abs(" + expression + ")", a, b);
    }
    
    /**
     * Calculate the signed area under a curve
     */
    public static double signedAreaUnderCurve(String expression, double a, double b) throws IllegalArgumentException {
        return definiteIntegral(expression, a, b);
    }
    
    /**
     * Calculate the average value of a function over an interval
     */
    public static double averageValue(String expression, double a, double b) throws IllegalArgumentException {
        if (MathUtils.approximatelyEqual(a, b)) {
            return ExpressionEvaluator.evaluate(expression, a);
        }
        return definiteIntegral(expression, a, b) / (b - a);
    }
    
    /**
     * Calculate the root mean square (RMS) value of a function over an interval
     */
    public static double rootMeanSquare(String expression, double a, double b) throws IllegalArgumentException {
        if (MathUtils.approximatelyEqual(a, b)) {
            double value = ExpressionEvaluator.evaluate(expression, a);
            return Math.abs(value);
        }
        
        double integral = definiteIntegral("(" + expression + ")^2", a, b);
        return Math.sqrt(integral / (b - a));
    }
    
    /**
     * Check if an integral converges (for improper integrals)
     */
    public static boolean isConvergent(String expression, double a, double b) {
        try {
            double result = definiteIntegral(expression, a, b);
            return !Double.isNaN(result) && !Double.isInfinite(result);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Calculate the arc length of a function over an interval
     */
    public static double arcLength(String expression, double a, double b) throws IllegalArgumentException {
        return definiteIntegral("sqrt(1 + (" + DerivativeCalculator.derivative(expression, (a + b) / 2) + ")^2)", a, b);
    }
    
    /**
     * Calculate the surface area of revolution around x-axis
     */
    public static double surfaceAreaOfRevolution(String expression, double a, double b) throws IllegalArgumentException {
        return 2 * Math.PI * definiteIntegral(expression + " * sqrt(1 + (" + 
               DerivativeCalculator.derivative(expression, (a + b) / 2) + ")^2)", a, b);
    }
    
    /**
     * Calculate the volume of revolution around x-axis
     */
    public static double volumeOfRevolution(String expression, double a, double b) throws IllegalArgumentException {
        return Math.PI * definiteIntegral("(" + expression + ")^2", a, b);
    }
} 