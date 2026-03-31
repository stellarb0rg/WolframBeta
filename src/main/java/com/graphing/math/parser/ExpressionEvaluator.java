package com.graphing.math.parser;

import com.graphing.utils.MathUtils;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

/**
 * Evaluates mathematical expressions at given x values
 */
public class ExpressionEvaluator {
    
    private static final Map<String, Double> CONSTANTS = new HashMap<>();
    private static final Map<String, MathFunction> FUNCTIONS = new HashMap<>();
    
    static {
        // Mathematical constants
        CONSTANTS.put("pi", Math.PI);
        CONSTANTS.put("e", Math.E);
        CONSTANTS.put("inf", Double.POSITIVE_INFINITY);
        CONSTANTS.put("-inf", Double.NEGATIVE_INFINITY);
        
        // Mathematical functions
        FUNCTIONS.put("sin", x -> Math.sin(x[0]));
        FUNCTIONS.put("cos", x -> Math.cos(x[0]));
        FUNCTIONS.put("tan", x -> Math.tan(x[0]));
        FUNCTIONS.put("asin", x -> Math.asin(x[0]));
        FUNCTIONS.put("acos", x -> Math.acos(x[0]));
        FUNCTIONS.put("atan", x -> Math.atan(x[0]));
        FUNCTIONS.put("sinh", x -> Math.sinh(x[0]));
        FUNCTIONS.put("cosh", x -> Math.cosh(x[0]));
        FUNCTIONS.put("tanh", x -> Math.tanh(x[0]));
        // --- Add aliases for cot(x), sec(x), cosec(x) ---
        FUNCTIONS.put("cot", x -> 1.0 / Math.tan(x[0]));
        FUNCTIONS.put("sec", x -> 1.0 / Math.cos(x[0]));
        FUNCTIONS.put("cosec", x -> 1.0 / Math.sin(x[0]));
        FUNCTIONS.put("csc", x -> 1.0 / Math.sin(x[0])); // csc as alternate for cosec
        FUNCTIONS.put("log", x -> Math.log10(x[0]));
        FUNCTIONS.put("ln", x -> Math.log(x[0]));
        FUNCTIONS.put("sqrt", x -> Math.sqrt(x[0]));
        FUNCTIONS.put("abs", x -> Math.abs(x[0]));
        FUNCTIONS.put("floor", x -> Math.floor(x[0]));
        FUNCTIONS.put("ceil", x -> Math.ceil(x[0]));
        FUNCTIONS.put("round", x -> Math.round(x[0]));
        FUNCTIONS.put("exp", x -> Math.exp(x[0]));
        FUNCTIONS.put("pow", x -> Math.pow(x[0], x[1]));
        FUNCTIONS.put("max", x -> Math.max(x[0], x[1]));
        FUNCTIONS.put("min", x -> Math.min(x[0], x[1]));
        // step(x) now behaves as ceil(x)
        FUNCTIONS.put("step", x -> Math.ceil(x[0]));
    }
    
    @FunctionalInterface
    private interface MathFunction {
        double apply(double... args);
    }
    
    /**
     * Evaluate a mathematical expression at a given x value
     */
    public static double evaluate(String expression, double x) throws IllegalArgumentException {
        try {
            // Support for DERIVATIVE_OF: prefix (numerical derivative)
            if (expression.startsWith("DERIVATIVE_OF:")) {
                String baseExpr = expression.substring("DERIVATIVE_OF:".length());
                double h = 1e-5;
                double f1 = evaluate(baseExpr, x + h);
                double f2 = evaluate(baseExpr, x - h);
                return (f1 - f2) / (2 * h);
            }
            String cleaned = cleanExpression(expression);
            return evaluatePostfix(infixToPostfix(cleaned), x);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expression: " + expression, e);
        }
    }
    
    /**
     * Clean the expression by removing spaces and standardizing operators
     */
    private static String cleanExpression(String expression) {
        return expression.toLowerCase()
                        .replaceAll("\\s+", "")
                        .replace("**", "^")
                        .replace("×", "*")
                        .replace("÷", "/");
    }
    
    /**
     * Convert infix expression to postfix (Reverse Polish Notation)
     */
    private static String[] infixToPostfix(String expression) {
        Stack<String> operators = new Stack<>();
        Stack<String> output = new Stack<>();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.') {
                // Handle numbers
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && 
                       (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                i--;
                output.push(num.toString());
            } else if (Character.isLetter(c)) {
                // Handle variables and functions
                StringBuilder token = new StringBuilder();
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    token.append(expression.charAt(i));
                    i++;
                }
                i--;
                String func = token.toString();
                
                if (FUNCTIONS.containsKey(func)) {
                    operators.push(func);
                } else if (func.equals("x")) {
                    output.push("x");
                } else if (CONSTANTS.containsKey(func)) {
                    output.push(CONSTANTS.get(func).toString());
                }
            } else if (c == '(') {
                operators.push("(");
            } else if (c == ')') {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.push(operators.pop());
                }
                if (!operators.isEmpty()) {
                    operators.pop(); // Remove '('
                }
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && 
                       !operators.peek().equals("(") && 
                       precedence(operators.peek()) >= precedence(String.valueOf(c))) {
                    output.push(operators.pop());
                }
                operators.push(String.valueOf(c));
            }
        }
        
        while (!operators.isEmpty()) {
            output.push(operators.pop());
        }
        
        return output.toArray(new String[0]);
    }
    
    /**
     * Evaluate postfix expression
     */
    private static double evaluatePostfix(String[] postfix, double x) {
        Stack<Double> stack = new Stack<>();
        
        for (String token : postfix) {
            if (token.equals("x")) {
                stack.push(x);
            } else if (isNumber(token)) {
                stack.push(Double.parseDouble(token));
            } else if (FUNCTIONS.containsKey(token)) {
                MathFunction func = FUNCTIONS.get(token);
                if (token.equals("pow") || token.equals("max") || token.equals("min")) {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(func.apply(a, b));
                } else {
                    double arg = stack.pop();
                    stack.push(func.apply(arg));
                }
            } else if (isOperator(token.charAt(0))) {
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperator(a, b, token.charAt(0)));
            }
        }
        
        return stack.pop();
    }
    
    private static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }
    
    private static int precedence(String operator) {
        switch (operator) {
            case "^": return 3;
            case "*":
            case "/": return 2;
            case "+":
            case "-": return 1;
            default: return 0;
        }
    }
    
    private static double applyOperator(double a, double b, char operator) {
        switch (operator) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': 
                if (MathUtils.isZero(b)) {
                    // Return NaN instead of throwing exception for division by zero
                    // This allows functions like 1/tan(x) to be plotted with gaps at asymptotes
                    return Double.NaN;
                }
                return a / b;
            case '^': return Math.pow(a, b);
            default: throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
} 