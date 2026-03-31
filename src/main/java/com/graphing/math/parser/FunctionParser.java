package com.graphing.math.parser;

import com.graphing.math.Function;
import com.graphing.math.FunctionType;
import com.graphing.utils.ColorManager;
import java.awt.Color;
import java.util.regex.Pattern;

/**
 * Parses string input into Function objects
 */
public class FunctionParser {
    
    private static final Pattern POLYNOMIAL_PATTERN = Pattern.compile(
        "^[\\d\\s\\+\\-\\*\\^x\\(\\)]+$"
    );
    
    private static final Pattern TRIGONOMETRIC_PATTERN = Pattern.compile(
        ".*(sin|cos|tan|asin|acos|atan|sinh|cosh|tanh).*"
    );
    
    private static final Pattern EXPONENTIAL_PATTERN = Pattern.compile(
        ".*(exp|e\\^).*"
    );
    
    private static final Pattern LOGARITHMIC_PATTERN = Pattern.compile(
        ".*(log|ln).*"
    );
    
    private static final Pattern STEP_PATTERN = Pattern.compile(
        ".*(floor|ceil|round).*"
    );
    
    private final ColorManager colorManager;
    
    public FunctionParser() {
        this.colorManager = new ColorManager();
    }
    
    /**
     * Parse a string expression into a Function object
     */
    public Function parse(String expression) throws IllegalArgumentException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }
        
        String cleanedExpression = cleanExpression(expression);
        FunctionType type = determineFunctionType(cleanedExpression);
        Color color = colorManager.getNextColor();
        
        return new Function(cleanedExpression, color, type);
    }
    
    /**
     * Parse a string expression with custom name
     */
    public Function parse(String expression, String name) throws IllegalArgumentException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }
        
        String cleanedExpression = cleanExpression(expression);
        FunctionType type = determineFunctionType(cleanedExpression);
        Color color = colorManager.getNextColor();
        
        return new Function(cleanedExpression, color, type, name);
    }
    
    /**
     * Clean the expression by removing extra whitespace and standardizing format
     */
    private String cleanExpression(String expression) {
        return expression.trim()
                        .replaceAll("\\s+", "")
                        .replace("**", "^")
                        .replace("×", "*")
                        .replace("÷", "/");
    }
    
    /**
     * Determine the function type based on the expression content
     */
    private FunctionType determineFunctionType(String expression) {
        String lowerExpression = expression.toLowerCase();
        
        if (TRIGONOMETRIC_PATTERN.matcher(lowerExpression).matches()) {
            return FunctionType.TRIGONOMETRIC;
        } else if (EXPONENTIAL_PATTERN.matcher(lowerExpression).matches()) {
            return FunctionType.EXPONENTIAL;
        } else if (LOGARITHMIC_PATTERN.matcher(lowerExpression).matches()) {
            return FunctionType.LOGARITHMIC;
        } else if (STEP_PATTERN.matcher(lowerExpression).matches()) {
            return FunctionType.STEP;
        } else if (POLYNOMIAL_PATTERN.matcher(lowerExpression).matches()) {
            return FunctionType.POLYNOMIAL;
        } else {
            return FunctionType.CUSTOM;
        }
    }
    
    /**
     * Validate if an expression is syntactically correct
     */
    public boolean isValidExpression(String expression) {
        try {
            // Test evaluation at a few points
            double[] testPoints = {-1.0, 0.0, 1.0};
            for (double x : testPoints) {
                ExpressionEvaluator.evaluate(expression, x);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get a list of supported mathematical functions
     */
    public static String[] getSupportedFunctions() {
        return new String[] {
            "sin(x)", "cos(x)", "tan(x)",
            "asin(x)", "acos(x)", "atan(x)",
            "sinh(x)", "cosh(x)", "tanh(x)",
            "log(x)", "ln(x)", "sqrt(x)",
            "abs(x)", "floor(x)", "ceil(x)",
            "round(x)", "exp(x)", "x^n"
        };
    }
    
    /**
     * Get examples of valid expressions
     */
    public static String[] getExpressionExamples() {
        return new String[] {
            "x^2 + 2*x + 1",
            "sin(x)",
            "cos(x) * exp(-x)",
            "log(x) / sqrt(x)",
            "abs(x - 3)",
            "x^3 - 6*x^2 + 11*x - 6"
        };
    }
} 