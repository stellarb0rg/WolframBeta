package com.graphing.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Common mathematical helper functions used throughout the application.
 */
public class MathUtils {
    
    public static final double EPSILON = 1e-10;
    
    /**
     * Checks if two double values are approximately equal.
     */
    public static boolean approximatelyEqual(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }
    
    /**
     * Checks if two doubles are approximately equal with custom tolerance
     */
    public static boolean approximatelyEqual(double a, double b, double tolerance) {
        return Math.abs(a - b) < tolerance;
    }
    
    /**
     * Checks if a value is approximately zero.
     */
    public static boolean isZero(double value) {
        return Math.abs(value) < EPSILON;
    }
    
    /**
     * Clamps a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Linear interpolation between two values.
     */
    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
    
    /**
     * Converts degrees to radians.
     */
    public static double toRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
    
    /**
     * Converts radians to degrees.
     */
    public static double toDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }
    
    /**
     * Generates a sequence of evenly spaced values.
     */
    public static List<Double> linspace(double start, double end, int count) {
        List<Double> result = new ArrayList<>();
        if (count <= 1) {
            result.add(start);
            return result;
        }
        
        double step = (end - start) / (count - 1);
        for (int i = 0; i < count; i++) {
            result.add(start + i * step);
        }
        return result;
    }
    
    /**
     * Calculates the factorial of a non-negative integer.
     */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is not defined for negative numbers");
        }
        if (n <= 1) {
            return 1;
        }
        
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    /**
     * Calculates the binomial coefficient C(n, k).
     */
    public static long binomialCoefficient(int n, int k) {
        if (k > n || k < 0) {
            return 0;
        }
        if (k == 0 || k == n) {
            return 1;
        }
        
        k = Math.min(k, n - k); // Use symmetry
        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }
    
    /**
     * Rounds a double to a specified number of decimal places.
     */
    public static double round(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }
    
    /**
     * Checks if a number is finite (not NaN or infinite).
     */
    public static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
    
    /**
     * Returns a finite value or a default if the input is not finite.
     */
    public static double finiteOr(double value, double defaultValue) {
        return isFinite(value) ? value : defaultValue;
    }
    
    /**
     * Calculates the greatest common divisor of two integers.
     */
    public static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    /**
     * Calculates the least common multiple of two integers.
     */
    public static int lcm(int a, int b) {
        return Math.abs(a * b) / gcd(a, b);
    }
    
    /**
     * Checks if a number is prime.
     */
    public static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
} 