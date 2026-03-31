package com.graphing.math.parser;

import java.util.Map;

public class SimpleExpressionParser {
    private String input;
    private int pos;
    private int length;

    public Expression parse(String s) {
        input = preprocess(s.replaceAll("\\s+", ""));
        pos = 0;
        length = input.length();
        Expression expr = parseExpression();
        if (pos < length) throw new IllegalArgumentException("Unexpected: " + peek());
        return expr;
    }

    // Preprocess to insert explicit '*' for implicit multiplication and handle |x| as abs(x)
    private String preprocess(String s) {
        // Handle absolute value: |x| -> abs(x)
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '|') {
                int j = i + 1;
                int depth = 1;
                while (j < s.length() && depth > 0) {
                    if (s.charAt(j) == '|') depth--;
                    else if (s.charAt(j) == '|') depth++;
                    j++;
                }
                if (depth == 0) {
                    String inside = s.substring(i + 1, j - 1);
                    sb.append("abs(").append(preprocess(inside)).append(")");
                    i = j;
                    continue;
                } else {
                    sb.append('|');
                    i++;
                    continue;
                }
            } else {
                sb.append(s.charAt(i));
                i++;
            }
        }
        s = sb.toString();
        // Insert * between:
        // 1. number and variable/function/(: 2x, 2sin(x), 2(x+1)
        s = s.replaceAll("(\\d)([a-zA-Z(])", "$1*$2");
        // 2. single variable or ) and (: x(x+1), )(
        s = s.replaceAll("([a-zA-Z0-9)])(\\()", "$1*$2");
        // Remove * between function name and '(': sin*(x) -> sin(x), cos*(x) -> cos(x), etc.
        s = s.replaceAll("(sin|cos|tan|log|ln|sqrt|exp|abs|floor|ceil)\\*\\(", "$1(");
        // 3. ) and variable/number: (x+1)2, (x+1)x
        s = s.replaceAll("(\\))([a-zA-Z0-9])", "$1*$2");
        return s;
    }

    // Parse lowest precedence: +, -
    private Expression parseExpression() {
        Expression left = parseTerm();
        while (true) {
            if (match('+')) {
                left = new BinaryOperation(BinaryOperation.Operator.ADD, left, parseTerm());
            } else if (match('-')) {
                left = new BinaryOperation(BinaryOperation.Operator.SUBTRACT, left, parseTerm());
            } else {
                break;
            }
        }
        return left;
    }

    // Parse next precedence: *, /
    private Expression parseTerm() {
        Expression left = parseFactor();
        while (true) {
            if (match('*')) {
                left = new BinaryOperation(BinaryOperation.Operator.MULTIPLY, left, parseFactor());
            } else if (match('/')) {
                left = new BinaryOperation(BinaryOperation.Operator.DIVIDE, left, parseFactor());
            } else {
                break;
            }
        }
        return left;
    }

    // Parse next precedence: ^
    private Expression parseFactor() {
        Expression left = parseUnary();
        while (true) {
            if (match('^')) {
                left = new BinaryOperation(BinaryOperation.Operator.POWER, left, parseUnary());
            } else {
                break;
            }
        }
        return left;
    }

    // Parse unary: -
    private Expression parseUnary() {
        if (match('-')) {
            return new UnaryOperation(UnaryOperation.Operator.NEGATE, parseUnary());
        }
        return parsePrimary();
    }

    // Parse constants, variables, functions, parentheses
    private Expression parsePrimary() {
        if (match('(')) {
            Expression expr = parseExpression();
            if (!match(')')) throw new IllegalArgumentException("Expected )");
            return expr;
        }
        if (isDigit(peek()) || peek() == '.') {
            return new Constant(parseNumber());
        }
        if (isLetter(peek())) {
            String name = parseName();
            // Check for function call
            if (match('(')) {
                Expression arg = parseExpression();
                if (!match(')')) throw new IllegalArgumentException("Expected ) after function argument");
                FunctionNode.FunctionType type;
                switch (name.toLowerCase()) {
                    case "sin": type = FunctionNode.FunctionType.SIN; break;
                    case "cos": type = FunctionNode.FunctionType.COS; break;
                    case "tan": type = FunctionNode.FunctionType.TAN; break;
                    case "log": type = FunctionNode.FunctionType.LOG; break;
                    case "ln": type = FunctionNode.FunctionType.LN; break;
                    case "sqrt": type = FunctionNode.FunctionType.SQRT; break;
                    case "exp": type = FunctionNode.FunctionType.EXP; break;
                    case "abs": type = FunctionNode.FunctionType.ABS; break;
                    case "floor": type = FunctionNode.FunctionType.FLOOR; break;
                    case "ceil": type = FunctionNode.FunctionType.CEIL; break;
                    default: throw new IllegalArgumentException("Unknown function: " + name);
                }
                return new FunctionNode(type, arg);
            } else {
                if (name.equals("e")) {
                    return new Constant(Math.E);
                } else {
                    return new Variable(name);
                }
            }
        }
        throw new IllegalArgumentException("Unexpected: " + peek());
    }

    private char peek() {
        return pos < length ? input.charAt(pos) : '\0';
    }
    private boolean match(char c) {
        if (peek() == c) { pos++; return true; }
        return false;
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    private double parseNumber() {
        int start = pos;
        while (isDigit(peek())) pos++;
        if (peek() == '.') {
            pos++;
            while (isDigit(peek())) pos++;
        }
        return Double.parseDouble(input.substring(start, pos));
    }
    private String parseName() {
        int start = pos;
        while (isLetter(peek()) || isDigit(peek())) pos++;
        return input.substring(start, pos);
    }
} 