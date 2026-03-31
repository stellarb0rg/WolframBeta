package com.graphing.math;

/**
 * Enumeration of mathematical function types.
 * Used for categorization and specialized handling of different function types.
 */
public enum FunctionType {
    POLYNOMIAL("Polynomial"),
    TRIGONOMETRIC("Trigonometric"),
    EXPONENTIAL("Exponential"),
    LOGARITHMIC("Logarithmic"),
    ALGEBRAIC("Algebraic"),
    STEP("Step"),
    CUSTOM("Custom"),
    IMPLICIT("Implicit"),
    INEQUALITY("Inequality");
    
    private final String displayName;
    
    FunctionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 