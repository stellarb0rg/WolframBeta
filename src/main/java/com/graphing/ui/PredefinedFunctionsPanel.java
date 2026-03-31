package com.graphing.ui;

import com.graphing.math.Function;
import com.graphing.math.FunctionType;
import com.graphing.utils.ColorManager;
import com.graphing.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel containing predefined function buttons like a calculator
 */
public class PredefinedFunctionsPanel extends JPanel {
    
    private final ColorManager colorManager;
    private final List<Function> functions;
    private PredefinedFunctionsListener listener;
    private FunctionInputPanel functionInputPanel;
    
    // Predefined function buttons
    private final JButton sinButton;
    private final JButton cosButton;
    private final JButton tanButton;
    private final JButton logButton;
    private final JButton lnButton;
    private final JButton expButton;
    private final JButton sqrtButton;
    private final JButton absButton;
    private final JButton xSquaredButton;
    private final JButton xCubedButton;
    private final JButton linearButton;
    private final JButton quadraticButton;
    private final JButton cubicButton;
    private final JButton stepButton;
    private final JButton floorButton;
    
    public PredefinedFunctionsPanel() {
        this.colorManager = new ColorManager();
        this.functions = new ArrayList<>();
        
        // Initialize buttons
        this.sinButton = new JButton("sin(x)");
        this.cosButton = new JButton("cos(x)");
        this.tanButton = new JButton("tan(x)");
        this.logButton = new JButton("log(x)");
        this.lnButton = new JButton("ln(x)");
        this.expButton = new JButton("exp(x)");
        this.sqrtButton = new JButton("√x");
        this.absButton = new JButton("|x|");
        this.xSquaredButton = new JButton("x²");
        this.xCubedButton = new JButton("x³");
        this.linearButton = new JButton("2x+1");
        this.quadraticButton = new JButton("x²+2x+1");
        this.cubicButton = new JButton("x³+x²+x+1");
        this.stepButton = new JButton("ceil(x)");
        this.floorButton = new JButton("floor(x)");
        
        setupComponents();
        setupLayout();
        setupListeners();
    }
    
    private void setupComponents() {
        // Set button properties
        JButton[] buttons = {sinButton, cosButton, tanButton, logButton, lnButton, 
                           expButton, sqrtButton, absButton, xSquaredButton, xCubedButton,
                           linearButton, quadraticButton, cubicButton, stepButton,
                           floorButton};
        
        for (JButton button : buttons) {
            button.setPreferredSize(new Dimension(80, 30));
            button.setFont(Theme.font(11));
            button.setBackground(Theme.SURFACE);
            button.setForeground(Theme.TEXT);
            button.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
            button.setFocusPainted(false);
            button.putClientProperty("JButton.buttonType", "roundRect");
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(Theme.SURFACE);
        
        // Create grid for function buttons
        JPanel functionGrid = new JPanel(new GridLayout(0, 3, 5, 5));
        functionGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        functionGrid.setBackground(Theme.SURFACE);
        
        // Add basic functions
        functionGrid.add(sinButton);
        functionGrid.add(cosButton);
        functionGrid.add(tanButton);
        
        functionGrid.add(logButton);
        functionGrid.add(lnButton);
        functionGrid.add(expButton);
        
        functionGrid.add(sqrtButton);
        functionGrid.add(absButton);
        functionGrid.add(xSquaredButton);
        
        functionGrid.add(xCubedButton);
        functionGrid.add(linearButton);
        functionGrid.add(quadraticButton);
        functionGrid.add(stepButton);
        functionGrid.add(floorButton);
        functionGrid.add(cubicButton); // Move cubicButton to the last cell in the grid
        
        add(functionGrid, BorderLayout.CENTER);
    }
    
    private void setupListeners() {
        // Basic trigonometric functions
        sinButton.addActionListener(e -> addFunctionWithCoefficient("sin", "sin(x)", "Enter coefficient for sin:"));
        cosButton.addActionListener(e -> addFunctionWithCoefficient("cos", "cos(x)", "Enter coefficient for cos:"));
        tanButton.addActionListener(e -> addFunctionWithCoefficient("tan", "tan(x)", "Enter coefficient for tan:"));
        
        // Logarithmic and exponential functions
        logButton.addActionListener(e -> addFunctionWithCoefficient("log", "log(x)", "Enter coefficient for log:"));
        lnButton.addActionListener(e -> addFunctionWithCoefficient("ln", "ln(x)", "Enter coefficient for ln:"));
        expButton.addActionListener(e -> addFunctionWithCoefficient("exp", "exp(x)", "Enter coefficient for exp:"));
        
        // Other basic functions
        sqrtButton.addActionListener(e -> addFunctionWithCoefficient("sqrt", "sqrt(x)", "Enter coefficient for sqrt:"));
        absButton.addActionListener(e -> addFunctionWithCoefficient("abs", "abs(x)", "Enter coefficient for abs:"));
        xSquaredButton.addActionListener(e -> addFunctionDirectly("x^2"));
        xCubedButton.addActionListener(e -> addFunctionDirectly("x^3"));
        
        // Polynomial functions with coefficient popups
        linearButton.addActionListener(e -> addPolynomialFunction("linear", "ax + b"));
        quadraticButton.addActionListener(e -> addPolynomialFunction("quadratic", "ax^2 + bx + c"));
        cubicButton.addActionListener(e -> addPolynomialFunction("cubic", "ax^3 + bx^2 + cx + d"));
        // Ceil function (was step)
        stepButton.addActionListener(e -> addFunctionDirectly("ceil(x)"));
        // Floor function
        floorButton.addActionListener(e -> addFunctionDirectly("floor(x)"));
    }
    
    private void showMinimalDialog(Component parent, String message) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent));
        dialog.setUndecorated(true);
        dialog.setModal(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        panel.setBackground(Theme.SURFACE);
        JLabel label = new JLabel(message);
        label.setFont(Theme.font(12));
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(12));
        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(12));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.setMargin(new Insets(2, 18, 2, 18));
        okButton.setBackground(Theme.ACCENT_SOFT);
        okButton.setForeground(Theme.TEXT);
        okButton.addActionListener(e -> dialog.dispose());
        panel.add(okButton);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
    private String showMinimalInputDialog(Component parent, String prompt, String defaultValue) {
        final String[] result = {null};
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent));
        dialog.setUndecorated(true);
        dialog.setModal(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        panel.setBackground(Theme.SURFACE);
        JLabel label = new JLabel(prompt);
        label.setFont(Theme.font(12));
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        JTextField textField = new JTextField(defaultValue, 12);
        textField.setFont(Theme.font(12));
        textField.setMaximumSize(new Dimension(120, 24));
        panel.add(textField);
        panel.add(Box.createVerticalStrut(10));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(12));
        okButton.setMargin(new Insets(2, 18, 2, 18));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(Theme.font(12));
        cancelButton.setMargin(new Insets(2, 18, 2, 18));
        okButton.setBackground(Theme.ACCENT_SOFT);
        okButton.setForeground(Theme.TEXT);
        cancelButton.setBackground(Theme.SURFACE);
        cancelButton.setForeground(Theme.TEXT);
        okButton.addActionListener(e -> { result[0] = textField.getText(); dialog.dispose(); });
        cancelButton.addActionListener(e -> { result[0] = null; dialog.dispose(); });
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }
    
    private void addFunctionWithCoefficient(String functionName, String baseExpression, String prompt) {
        String coefficient = showMinimalInputDialog(this, prompt, "1");
        if (coefficient != null && !coefficient.trim().isEmpty()) {
            try {
                double coeff = Double.parseDouble(coefficient);
                String coeffStr = formatCoeff(coeff);
                String expression = coeffStr.isEmpty() ? baseExpression : coeffStr + baseExpression;
                addFunctionDirectly(expression);
            } catch (NumberFormatException e) {
                showMinimalDialog(this, "Invalid coefficient. Using 1.");
                addFunctionDirectly(baseExpression);
            }
        }
    }
    
    private void addPolynomialFunction(String type, String template) {
        String[] coefficients;
        String[] prompts;
        
        switch (type) {
            case "linear":
                coefficients = new String[2];
                prompts = new String[]{"Enter 'a' coefficient:", "Enter 'b' coefficient:"};
                break;
            case "quadratic":
                coefficients = new String[3];
                prompts = new String[]{"Enter 'a' coefficient:", "Enter 'b' coefficient:", "Enter 'c' coefficient:"};
                break;
            case "cubic":
                coefficients = new String[4];
                prompts = new String[]{"Enter 'a' coefficient:", "Enter 'b' coefficient:", "Enter 'c' coefficient:", "Enter 'd' coefficient:"};
                break;
            default:
                return;
        }
        
        // Get coefficients from user using minimal input dialog
        for (int i = 0; i < coefficients.length; i++) {
            String input = showMinimalInputDialog(this, prompts[i], "1");
            if (input == null) return; // User cancelled
            coefficients[i] = input.trim().isEmpty() ? "1" : input;
        }
        
        // Build expression
        String expression = buildPolynomialExpression(type, coefficients);
        addFunctionDirectly(expression);
    }
    
    private String buildPolynomialExpression(String type, String[] coefficients) {
        try {
            switch (type) {
                case "linear":
                    double a1 = Double.parseDouble(coefficients[0]);
                    double b1 = Double.parseDouble(coefficients[1]);
                    return formatCoeff(a1) + "x + " + b1;
                case "quadratic":
                    double a2 = Double.parseDouble(coefficients[0]);
                    double b2 = Double.parseDouble(coefficients[1]);
                    double c2 = Double.parseDouble(coefficients[2]);
                    return formatCoeff(a2) + "x^2 + " + formatCoeff(b2) + "x + " + c2;
                case "cubic":
                    double a3 = Double.parseDouble(coefficients[0]);
                    double b3 = Double.parseDouble(coefficients[1]);
                    double c3 = Double.parseDouble(coefficients[2]);
                    double d3 = Double.parseDouble(coefficients[3]);
                    return formatCoeff(a3) + "x^3 + " + formatCoeff(b3) + "x^2 + " + formatCoeff(c3) + "x + " + d3;
                default:
                    return "x";
            }
        } catch (NumberFormatException e) {
            showMinimalDialog(this, "Invalid coefficients. Using default values.");
            return type.equals("linear") ? "x + 1" : type.equals("quadratic") ? "x^2 + x + 1" : "x^3 + x^2 + x + 1";
        }
    }
    
    /**
     * Format coefficient for output (avoid 1*x, -1*x, etc.)
     */
    private String formatCoeff(double coeff) {
        if (coeff == 1.0) return "";
        if (coeff == -1.0) return "-";
        if (coeff == (int) coeff) return Integer.toString((int) coeff);
        return Double.toString(coeff);
    }
    
    private void addFunctionDirectly(String expression) {
        try {
            Color color = colorManager.getNextColor();
            Function function = new Function(expression, color, null, expression);
            function.setVisible(true);
            
            if (listener != null) {
                listener.onFunctionAdded(function);
            }
        } catch (Exception e) {
            showMinimalDialog(this, "Error adding function: " + e.getMessage());
        }
    }
    
    public void setPredefinedFunctionsListener(PredefinedFunctionsListener listener) {
        this.listener = listener;
    }
    
    public void setFunctionInputPanel(FunctionInputPanel functionInputPanel) {
        this.functionInputPanel = functionInputPanel;
    }
    
    public interface PredefinedFunctionsListener {
        void onFunctionAdded(Function function);
    }
} 
