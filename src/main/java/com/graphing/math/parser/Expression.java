package com.graphing.math.parser;

import java.util.Map;

public interface Expression {
    double evaluate(Map<String, Double> variables);
}

class Constant implements Expression {
    private final double value;
    public Constant(double value) { this.value = value; }
    public double evaluate(Map<String, Double> variables) { return value; }
}

class Variable implements Expression {
    private final String name;
    public Variable(String name) { this.name = name; }
    public double evaluate(Map<String, Double> variables) {
        if (!variables.containsKey(name)) throw new IllegalArgumentException("Variable '" + name + "' not provided");
        return variables.get(name);
    }
}

class BinaryOperation implements Expression {
    public enum Operator { ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER }
    private final Operator op;
    private final Expression left, right;
    public BinaryOperation(Operator op, Expression left, Expression right) {
        this.op = op; this.left = left; this.right = right;
    }
    public double evaluate(Map<String, Double> variables) {
        double l = left.evaluate(variables);
        double r = right.evaluate(variables);
        switch (op) {
            case ADD: return l + r;
            case SUBTRACT: return l - r;
            case MULTIPLY: return l * r;
            case DIVIDE: return l / r;
            case POWER: return Math.pow(l, r);
            default: throw new IllegalStateException("Unknown operator");
        }
    }
}

class UnaryOperation implements Expression {
    public enum Operator { NEGATE }
    private final Operator op;
    private final Expression expr;
    public UnaryOperation(Operator op, Expression expr) {
        this.op = op; this.expr = expr;
    }
    public double evaluate(Map<String, Double> variables) {
        double v = expr.evaluate(variables);
        switch (op) {
            case NEGATE: return -v;
            default: throw new IllegalStateException("Unknown unary operator");
        }
    }
}

class FunctionNode implements Expression {
    public enum FunctionType {
        SIN, COS, TAN, LOG, LN, SQRT, EXP, ABS, FLOOR, CEIL
    }
    private final FunctionType functionType;
    private final Expression argument;
    public FunctionNode(FunctionType functionType, Expression argument) {
        this.functionType = functionType;
        this.argument = argument;
    }
    public double evaluate(Map<String, Double> variables) {
        double arg = argument.evaluate(variables);
        switch (functionType) {
            case SIN: return Math.sin(arg);
            case COS: return Math.cos(arg);
            case TAN: return Math.tan(arg);
            case LOG: return Math.log10(arg);
            case LN: return Math.log(arg);
            case SQRT: return Math.sqrt(arg);
            case EXP: return Math.exp(arg);
            case ABS: return Math.abs(arg);
            case FLOOR: return Math.floor(arg);
            case CEIL: return Math.ceil(arg);
            default: throw new IllegalStateException("Unknown function");
        }
    }
} 