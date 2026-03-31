package com.graphing.math;

import java.awt.Color;
import com.graphing.math.parser.Expression;
import com.graphing.math.parser.SimpleExpressionParser;

/**
 * Represents a mathematical function with expression, color, and type
 */
public class Function {
    private String expression;
    private Color color;
    private FunctionType type;
    private boolean visible;
    private String name;
    private Expression parsedExpression;
    // For implicit/inequality
    private Expression leftExpression;
    private Expression rightExpression;
    private String operator; // =, <, >, <=, >=
    
    public Function(String expression, Color color, FunctionType type) {
        this.expression = expression;
        this.color = color;
        this.type = type;
        this.visible = true;
        this.name = generateName();
        parseForImplicitOrInequality();
    }
    
    public Function(String expression, Color color, FunctionType type, String name) {
        this.expression = expression;
        this.color = color;
        this.type = type;
        this.visible = true;
        this.name = name;
        parseForImplicitOrInequality();
    }
    
    private String generateName() {
        return "f" + System.currentTimeMillis() % 10000;
    }
    
    private void parseForImplicitOrInequality() {
        // Detect and parse implicit/inequality
        String[] ops = {">=", "<=", "=", ">", "<"};
        for (String op : ops) {
            int idx = expression.indexOf(op);
            if (idx > 0) {
                String left = expression.substring(0, idx).trim();
                String right = expression.substring(idx + op.length()).trim();
                SimpleExpressionParser parser = new SimpleExpressionParser();
                leftExpression = parser.parse(left); // Allow both x and y
                rightExpression = parser.parse(right);
                operator = op;
                if (op.equals("=")) {
                    this.type = FunctionType.IMPLICIT;
                } else {
                    this.type = FunctionType.INEQUALITY;
                }
                parsedExpression = null;
                return;
            }
        }
        // Otherwise, treat as explicit y=f(x)
        try {
            this.parsedExpression = new SimpleExpressionParser().parse(expression);
        } catch (Exception e) {
            this.parsedExpression = null;
        }
        leftExpression = null;
        rightExpression = null;
        operator = null;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public FunctionType getType() {
        return type;
    }
    
    public void setType(FunctionType type) {
        this.type = type;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Expression getParsedExpression() {
        return parsedExpression;
    }
    
    public Expression getLeftExpression() { return leftExpression; }
    public Expression getRightExpression() { return rightExpression; }
    public String getOperator() { return operator; }
    
    @Override
    public String toString() {
        return name + ": " + expression;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Function function = (Function) obj;
        return expression.equals(function.expression) && 
               color.equals(function.color) && 
               type == function.type;
    }
    
    @Override
    public int hashCode() {
        return expression.hashCode() * 31 + color.hashCode() * 17 + type.hashCode();
    }
} 