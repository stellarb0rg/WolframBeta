package com.graphing.math.parser;

import java.util.*;

public class SimpleExpressionParserTest {
    public static void main(String[] args) {
        SimpleExpressionParser parser = new SimpleExpressionParser();
        Map<String, Double> vars = new HashMap<>();
        vars.put("x", 2.0);
        vars.put("y", 3.0);

        // Test constants
        assertEquals(5.0, parser.parse("5").evaluate(vars));
        assertEquals(-3.0, parser.parse("-3").evaluate(vars));
        // Test variable
        assertEquals(2.0, parser.parse("x").evaluate(vars));
        assertEquals(3.0, parser.parse("y").evaluate(vars));
        // Test addition
        assertEquals(5.0, parser.parse("2+3").evaluate(vars));
        // Test subtraction
        assertEquals(-1.0, parser.parse("2-3").evaluate(vars));
        // Test multiplication
        assertEquals(6.0, parser.parse("2*3").evaluate(vars));
        // Test division
        assertEquals(2.0/3.0, parser.parse("2/3").evaluate(vars));
        // Test power
        assertEquals(8.0, parser.parse("2^3").evaluate(vars));
        // Test parentheses
        assertEquals(10.0, parser.parse("2*(3+2)").evaluate(vars));
        // Test precedence
        assertEquals(8.0, parser.parse("2+3*2").evaluate(vars));
        assertEquals(10.0, parser.parse("(2+3)*2").evaluate(vars));
        // Test unary minus
        assertEquals(-5.0, parser.parse("-(2+3)").evaluate(vars));
        // Test variable in expression
        assertEquals(7.0, parser.parse("2*x+3").evaluate(vars));
        assertEquals(10.0, parser.parse("2*x+y*2").evaluate(vars));
        // Test nested parentheses
        assertEquals(19.0, parser.parse("2*(x+(y*2)) + 3").evaluate(vars));
        // Test complex expression
        assertEquals(Math.pow(2+3*2,2), parser.parse("(2+3*2)^2").evaluate(vars));
        // Edge: multiple unary minuses
        assertEquals(5.0, parser.parse("--5").evaluate(vars));
        // Edge: variable not provided
        try {
            parser.parse("z+1").evaluate(vars);
            throw new AssertionError("Expected exception for missing variable");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Test math functions
        assertEquals(Math.sin(2.0), parser.parse("sin(x)").evaluate(vars));
        assertEquals(Math.cos(2.0), parser.parse("cos(x)").evaluate(vars));
        assertEquals(Math.tan(2.0), parser.parse("tan(x)").evaluate(vars));
        assertEquals(Math.log10(3.0), parser.parse("log(y)").evaluate(vars));
        assertEquals(Math.log(2.0), parser.parse("ln(x)").evaluate(vars));
        assertEquals(Math.sqrt(3.0), parser.parse("sqrt(y)").evaluate(vars));
        assertEquals(Math.exp(2.0), parser.parse("exp(x)").evaluate(vars));
        assertEquals(Math.abs(-2.0), parser.parse("abs(-x)").evaluate(vars));
        assertEquals(Math.floor(2.7), parser.parse("floor(2.7)").evaluate(vars));
        assertEquals(Math.ceil(2.1), parser.parse("ceil(2.1)").evaluate(vars));
        // Test function with expression argument
        assertEquals(Math.sin(2.0+3.0), parser.parse("sin(x+y)").evaluate(vars));
        assertEquals(Math.log10(2.0*3.0+1), parser.parse("log(x*y+1)").evaluate(vars));
        // Test implicit multiplication and absolute value
        assertEquals(4.0, parser.parse("2x").evaluate(vars));
        assertEquals(8.0, parser.parse("2(x+2)").evaluate(vars));
        assertEquals(10.0, parser.parse("x(x+3)").evaluate(vars));
        assertEquals(8.0, parser.parse("(x+2)2").evaluate(vars));
        assertEquals(10.0, parser.parse("(x+2)x").evaluate(vars));
        assertEquals(Math.abs(2.0), parser.parse("|x|").evaluate(vars));
        assertEquals(Math.abs(2.0+3.0), parser.parse("|x+y|").evaluate(vars));
        assertEquals(9.0, parser.parse("x^2+2x+1").evaluate(vars));
        assertEquals(8.0, parser.parse("x^3").evaluate(vars));
        System.out.println("All tests passed.");
    }

    private static void assertEquals(double expected, double actual) {
        if (Math.abs(expected - actual) > 1e-9) {
            throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
        }
    }
} 