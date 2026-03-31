package com.graphing.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.graphing.ui.FunctionInputPanel;
import com.graphing.ui.PredefinedFunctionsPanel;
import com.graphing.ui.Theme;
import com.graphing.io.JSONSerializer;
import com.graphing.io.SVGExporter;
import com.graphing.math.Function;
import com.graphing.math.Point2D;
import com.graphing.math.IntersectionPoint;
import com.graphing.graph.GraphSettings;
import com.graphing.graph.Viewport;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.io.File;


/**
 * Main application window that hosts all UI components
 */
public class MainFrame extends JFrame {
    
    private GraphPanel graphPanel;
    private FunctionInputPanel functionInputPanel;
    private PredefinedFunctionsPanel predefinedFunctionsPanel;
    private ControlPanel controlPanel;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu exportMenu;
    private JMenu helpMenu;
    
    private File userSelectedDirectory = null;

    public MainFrame() {
        initializeFrame();
        createComponents();
        createMenuBar();
        layoutComponents();
        setupEventHandlers();
        connectComponents();
        // requestFilePermissionsOnStartup(); // Removed to prevent dialog at startup
    }
    
    private void initializeFrame() {
        setTitle("WolframBeta - Advanced Graphing Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setBackground(Theme.BG);
    }
    
    private void createComponents() {
        graphPanel = new GraphPanel();
        functionInputPanel = new FunctionInputPanel();
        predefinedFunctionsPanel = new PredefinedFunctionsPanel();
        predefinedFunctionsPanel.setFunctionInputPanel(functionInputPanel);
        controlPanel = new ControlPanel();
    }
    
    private void connectComponents() {
        // Connect FunctionInputPanel to GraphPanel
        functionInputPanel.setFunctionInputListener(new FunctionInputPanel.FunctionInputListener() {
            @Override
            public void onFunctionAdded(Function function) {
                graphPanel.addFunction(function);
            }
            
            @Override
            public void onFunctionRemoved(Function function) {
                graphPanel.removeFunction(function);
            }
            
            @Override
            public void onFunctionUpdated(Function function) {
                // Find and update the function in the graph panel
                List<Function> functions = graphPanel.getFunctions();
                System.out.println("[DEBUG] onFunctionUpdated called for function: " + function.getName() + " (visible: " + function.isVisible() + ")");
                System.out.println("[DEBUG] Graph panel has " + functions.size() + " functions");
                for (int i = 0; i < functions.size(); i++) {
                    Function graphFunc = functions.get(i);
                    System.out.println("[DEBUG] Graph function " + i + ": " + graphFunc.getName() + " (visible: " + graphFunc.isVisible() + ")");
                    if (graphFunc.getName().equals(function.getName())) {
                        System.out.println("[DEBUG] Found matching function, updating at index " + i);
                        // Update the function in the graph panel
                        graphPanel.updateFunction(i, function);
                        break;
                    }
                }
            }
            
            @Override
            public void onFunctionsCleared() {
                graphPanel.clearFunctions();
            }
        });
        
        // Connect PredefinedFunctionsPanel to GraphPanel
        predefinedFunctionsPanel.setPredefinedFunctionsListener(new PredefinedFunctionsPanel.PredefinedFunctionsListener() {
            @Override
            public void onFunctionAdded(Function function) {
                graphPanel.addFunction(function);
                functionInputPanel.addFunction(function);
            }
        });
        
        // Connect ControlPanel to GraphPanel
        controlPanel.setControlPanelListener(new ControlPanel.ControlPanelListener() {
            @Override
            public void onAxisLimitsChanged(double minX, double maxX, double minY, double maxY) {
                graphPanel.setView(minX, maxX, minY, maxY);
                // Also update GraphSettings so SVG export matches current view
                graphPanel.getSettings().setRange(minX, maxX, minY, maxY);
            }
            
            @Override
            public void onResetView() {
                graphPanel.resetView();
                updateControlPanelFromGraph();
            }
            
            @Override
            public void onFindIntersections() {
                List<Function> functions = graphPanel.getFunctions();
                
                if (functions.size() < 2) {
                    showMinimalDialog(MainFrame.this, "Please add at least 2 functions to find intersections.");
                    return;
                }
                
                // Find intersections
                List<IntersectionPoint> intersections = graphPanel.getGraphRenderer().findIntersections(functions);
                // Do not show any dialog, just show colored dots on the graph
                // Redraw the graph to show the intersection points
                graphPanel.repaint();
            }
            
            @Override
            public void onDefiniteIntegral() {
                List<Function> functions = graphPanel.getFunctions();
                if (functions.isEmpty()) {
                    showMinimalDialog(MainFrame.this, "Please add at least one function to calculate its definite integral.");
                    return;
                }
                showDefiniteIntegralDialog(functions);
            }
            
            @Override
            public void onDerivative() {
                List<Function> functions = graphPanel.getFunctions();
                
                if (functions.isEmpty()) {
                    showMinimalDialog(MainFrame.this, "Please add at least one function to calculate its derivative.");
                    return;
                }
                
                // Show function selection dialog
                showDerivativeFunctionDialog(functions);
            }
            
            @Override
            public void onFindExtrema() {
                List<Function> functions = graphPanel.getFunctions();
                
                if (functions.isEmpty()) {
                    showMinimalDialog(MainFrame.this, "Please add at least one function to find extrema.");
                    return;
                }
                
                // Show function selection dialog
                showExtremaFunctionDialog(functions);
            }
            
            @Override
            public void onToggleInteractivePoints() {
                JOptionPane.showMessageDialog(MainFrame.this, 
                    "Interactive Points is now active!\n\n" +
                    "To use this feature:\n" +
                    "• Click anywhere on a plotted function curve\n" +
                    "• A dialog will show the exact x and y coordinates\n" +
                    "• The coordinates are calculated to 6 decimal places\n" +
                    "• Works with all visible functions\n\n" +
                    "Note: Click within 5 pixels of a curve for best results.", 
                    "Interactive Points Active", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Update control panel with initial graph settings
        updateControlPanelFromGraph();
    }
    
    /**
     * Convert a Color to a readable name
     */
    private String getColorName(Color color) {
        if (color.equals(Color.RED)) return "Red";
        if (color.equals(Color.GREEN)) return "Green";
        if (color.equals(Color.BLUE)) return "Blue";
        if (color.equals(Color.YELLOW)) return "Yellow";
        if (color.equals(Color.MAGENTA)) return "Magenta";
        if (color.equals(Color.CYAN)) return "Cyan";
        if (color.equals(Color.ORANGE)) return "Orange";
        if (color.equals(Color.PINK)) return "Pink";
        if (color.equals(Color.GRAY)) return "Gray";
        if (color.equals(Color.DARK_GRAY)) return "Dark Gray";
        if (color.equals(Color.LIGHT_GRAY)) return "Light Gray";
        if (color.equals(Color.BLACK)) return "Black";
        if (color.equals(Color.WHITE)) return "White";
        
        // For random colors, describe the hue
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hue = hsb[0];
        
        if (hue < 0.1f || hue >= 0.9f) return "Red";
        if (hue < 0.1f) return "Red-Orange";
        if (hue < 0.2f) return "Orange";
        if (hue < 0.3f) return "Yellow";
        if (hue < 0.4f) return "Yellow-Green";
        if (hue < 0.5f) return "Green";
        if (hue < 0.6f) return "Cyan";
        if (hue < 0.7f) return "Blue";
        if (hue < 0.8f) return "Purple";
        if (hue < 0.9f) return "Magenta";
        
        return "Colored";
    }
    
    /**
     * Show a custom dialog with colored swatches for intersection points
     */
    private void showColoredIntersectionsDialog(List<IntersectionPoint> intersections) {
        // Create custom panel for the dialog
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add title
        JLabel titleLabel = new JLabel("Found " + intersections.size() + " intersection(s):");
        titleLabel.setFont(Theme.font(Font.BOLD, 14));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Add each intersection point with color swatch
        for (IntersectionPoint intersection : intersections) {
            JPanel pointPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            // Create color swatch
            JPanel colorSwatch = new JPanel();
            colorSwatch.setPreferredSize(new Dimension(20, 20));
            colorSwatch.setBackground(intersection.getColor());
            colorSwatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            
            // Create text label
            String colorName = getColorName(intersection.getColor());
            JLabel pointLabel = new JLabel(String.format("Point %d (%s): (%.3f, %.3f)", 
                intersection.getIndex() + 1, colorName, intersection.getX(), intersection.getY()));
            pointLabel.setFont(Theme.font(13));
            
            pointPanel.add(colorSwatch);
            pointPanel.add(pointLabel);
            panel.add(pointPanel);
            panel.add(Box.createVerticalStrut(5));
        }
        
        // Show the custom dialog
        JOptionPane.showMessageDialog(this, panel, "Intersections Found", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show dialog to select a function for derivative calculation
     */
    private void showDerivativeFunctionDialog(List<Function> functions) {
        // Only show unique expressions
        java.util.LinkedHashSet<String> uniqueExpressions = new java.util.LinkedHashSet<>();
        for (Function func : functions) {
            uniqueExpressions.add(func.getExpression());
        }
        String[] functionExpressions = uniqueExpressions.toArray(new String[0]);

        JComboBox<String> functionCombo = new JComboBox<>(functionExpressions);
        functionCombo.setFont(Theme.font(12));

        // Custom undecorated dialog
        JDialog dialog = new JDialog(this);
        dialog.setUndecorated(true);
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panel.setBackground(Theme.SURFACE);

        JLabel funcLabel = new JLabel("Select a function to calculate its derivative:");
        funcLabel.setFont(Theme.font(12));
        funcLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        funcLabel.setForeground(Theme.TEXT);
        panel.add(funcLabel);
        panel.add(Box.createVerticalStrut(10));
        functionCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(functionCombo);
        panel.add(Box.createVerticalStrut(14));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(12));
        okButton.setMargin(new Insets(2, 18, 2, 18));
        okButton.setBackground(Theme.ACCENT_SOFT);
        okButton.setForeground(Theme.TEXT);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(Theme.font(12));
        cancelButton.setMargin(new Insets(2, 18, 2, 18));
        cancelButton.setBackground(Theme.SURFACE);
        cancelButton.setForeground(Theme.TEXT);
        okButton.addActionListener(e -> {
            String selectedExpression = (String) functionCombo.getSelectedItem();
            if (selectedExpression != null) {
                Function selectedFunc = null;
                for (Function func : functions) {
                    if (func.getExpression().equals(selectedExpression)) {
                        selectedFunc = func;
                        break;
                    }
                }
                if (selectedFunc != null) {
                    dialog.dispose();
                    calculateAndPlotDerivative(selectedFunc);
                }
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    /**
     * Calculate and plot the derivative of the selected function
     */
    private void calculateAndPlotDerivative(Function originalFunction) {
        try {
            String derivativeExpression;
            if (isPolynomial(originalFunction.getExpression())) {
                derivativeExpression = calculateDerivativeExpression(originalFunction.getExpression());
                System.out.println("[DEBUG] Symbolic derivative for " + originalFunction.getExpression() + " is: " + derivativeExpression);
            } else {
                derivativeExpression = "DERIVATIVE_OF:" + originalFunction.getExpression();
            }
            Function derivativeFunction = new Function(
                derivativeExpression,
                generateDerivativeColor(originalFunction.getColor()),
                com.graphing.math.FunctionType.ALGEBRAIC,
                "d/dx(" + originalFunction.getExpression() + ")"
            );
            graphPanel.addFunction(derivativeFunction);
            functionInputPanel.addFunction(derivativeFunction);
            showMinimalDialog(this,
                (isPolynomial(originalFunction.getExpression()) ? "Symbolic" : "Numerical") + " derivative of " + originalFunction.getName() + " has been plotted.\n" +
                "Original: " + originalFunction.getExpression() + "\n" +
                "Derivative: " + derivativeExpression);
        } catch (Exception e) {
            showMinimalDialog(this,
                "Error calculating derivative: " + e.getMessage());
        }
    }
    
    /**
     * Calculate derivative expression using symbolic differentiation for polynomials
     */
    private String calculateDerivativeExpression(String expression) {
        // For now, use a simple numerical approach
        // This is a basic implementation - in a real application, you'd want symbolic differentiation

        // Try to handle polynomials of the form ax^n + bx^(n-1) + ... + c
        if (isPolynomial(expression)) {
            return differentiatePolynomial(expression);
        }
        // Common derivative rules
        if (expression.contains("x^")) {
            // Handle power rule: d/dx(x^n) = n*x^(n-1)
            return handlePowerRule(expression);
        } else if (expression.contains("sin(x)")) {
            return "cos(x)";
        } else if (expression.contains("cos(x)")) {
            return "-sin(x)";
        } else if (expression.contains("exp(x)")) {
            return "exp(x)";
        } else if (expression.contains("ln(x)")) {
            return "1/x";
        } else if (expression.contains("log(x)")) {
            return "1/(x*ln(10))";
        } else if (expression.matches(".*\\d*x.*")) {
            // Handle linear terms: d/dx(ax) = a
            return handleLinearRule(expression);
        } else {
            // For complex expressions, use numerical differentiation
            return "numerical_derivative(" + expression + ")";
        }
    }

    /**
     * Check if the expression is a polynomial (simple check)
     */
    private boolean isPolynomial(String expression) {
        // Preprocess: remove spaces, standardize multiplication
        String expr = expression.replaceAll("\\s+", "").replaceAll("(?<=[0-9])x", "*x");
        // Accepts terms like ax^n, bx, c, separated by + or -
        return expr.matches("[\\-\\+]?([0-9.]*\\*?x(\\^\\d+)?|[0-9.]+)([\\-\\+][0-9.]*\\*?x(\\^\\d+)?|[\\-\\+][0-9.]+)*");
    }

    /**
     * Symbolically differentiate a polynomial string
     */
    private String differentiatePolynomial(String expression) {
        // Remove spaces
        String expr = expression.replace(" ", "");
        // Split into terms by + or - (keep the sign)
        java.util.List<String> terms = new java.util.ArrayList<>();
        int i = 0;
        StringBuilder term = new StringBuilder();
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if ((c == '+' || c == '-') && term.length() > 0) {
                terms.add(term.toString());
                term = new StringBuilder();
            }
            term.append(c);
            i++;
        }
        if (term.length() > 0) terms.add(term.toString());

        java.util.List<String> derivedTerms = new java.util.ArrayList<>();
        for (String t : terms) {
            t = t.trim();
            if (t.isEmpty()) continue;
            // Match ax^n
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([\\-\\+]?[0-9.]*)(\\*?x)(\\^(\\d+))?$", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(t);
            if (m.matches()) {
                String coeffStr = m.group(1);
                String xPart = m.group(2);
                String powPart = m.group(4);
                double coeff = 1.0;
                if (coeffStr != null && !coeffStr.isEmpty() && !coeffStr.equals("+") && !coeffStr.equals("-")) {
                    coeff = Double.parseDouble(coeffStr);
                } else if (coeffStr != null && coeffStr.equals("-")) {
                    coeff = -1.0;
                } else if (coeffStr != null && coeffStr.equals("+")) {
                    coeff = 1.0;
                }
                if (powPart != null) {
                    int pow = Integer.parseInt(powPart);
                    if (pow == 0) {
                        // Derivative of constant is 0
                        continue;
                    } else if (pow == 1) {
                        // ax^1 -> a
                        derivedTerms.add(formatCoeff(coeff));
                    } else {
                        // ax^n -> a*n*x^(n-1)
                        double newCoeff = coeff * pow;
                        int newPow = pow - 1;
                        if (newPow == 1) {
                            String newCoeffStr = formatCoeff(newCoeff);
                            derivedTerms.add(newCoeffStr.isEmpty() ? "x" : newCoeffStr + "x");
                        } else {
                            String newCoeffStr = formatCoeff(newCoeff);
                            derivedTerms.add(newCoeffStr.isEmpty() ? "x^" + newPow : newCoeffStr + "x^" + newPow);
                        }
                    }
                } else {
                    // ax (no ^) -> a
                    derivedTerms.add(formatCoeff(coeff));
                }
            } else {
                // Match constant term
                if (t.matches("^[\\-\\+]?[0-9.]+$")) {
                    // Derivative of constant is 0
                    continue;
                } else {
                    // Not a recognized polynomial term, fallback
                    return "numerical_derivative(" + expression + ")";
                }
            }
        }
        if (derivedTerms.isEmpty()) return "0";
        // Join terms, clean up +-
        String result = String.join(" + ", derivedTerms);
        result = result.replace("+-", "-");
        result = result.replace("-+", "-");
        result = result.replace("+ -", "- ");
        // If result is empty (e.g., derivative of x is ''), return '1'
        if (result.trim().isEmpty()) return "1";
        return result;
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
    
    /**
     * Handle power rule for derivatives
     */
    private String handlePowerRule(String expression) {
        // Simple power rule implementation
        if (expression.equals("x^2")) return "2x";
        if (expression.equals("x^3")) return "3x^2";
        if (expression.equals("x^4")) return "4x^3";
        if (expression.equals("x^5")) return "5x^4";
        if (expression.equals("x^6")) return "6x^5";
        if (expression.equals("x^7")) return "7x^6";
        if (expression.equals("x^8")) return "8x^7";
        if (expression.equals("x^9")) return "9x^8";
        if (expression.equals("x^10")) return "10x^9";
        
        // For other powers, try to extract the exponent
        if (expression.matches("x\\^\\d+")) {
            String exponent = expression.substring(2);
            int exp = Integer.parseInt(exponent);
            if (exp == 1) return "1";
            if (exp == 2) return "2x";
            return exp + "x^" + (exp - 1);
        }
        
        return "numerical_derivative(" + expression + ")";
    }
    
    /**
     * Handle linear rule for derivatives
     */
    private String handleLinearRule(String expression) {
        // Handle expressions like "2x", "3x", etc.
        if (expression.equals("x")) return "1";
        if (expression.equals("2x")) return "2";
        if (expression.equals("3x")) return "3";
        if (expression.equals("4x")) return "4";
        if (expression.equals("5x")) return "5";
        if (expression.equals("6x")) return "6";
        if (expression.equals("7x")) return "7";
        if (expression.equals("8x")) return "8";
        if (expression.equals("9x")) return "9";
        if (expression.equals("10x")) return "10";
        
        // Also handle expressions with explicit multiplication
        if (expression.equals("2*x")) return "2";
        if (expression.equals("3*x")) return "3";
        if (expression.equals("4*x")) return "4";
        if (expression.equals("5*x")) return "5";
        if (expression.equals("6*x")) return "6";
        if (expression.equals("7*x")) return "7";
        if (expression.equals("8*x")) return "8";
        if (expression.equals("9*x")) return "9";
        if (expression.equals("10*x")) return "10";
        
        // Try to extract coefficient
        if (expression.matches("\\d+\\*x")) {
            String coefficient = expression.substring(0, expression.indexOf("*"));
            return coefficient;
        }
        
        return "numerical_derivative(" + expression + ")";
    }
    
    /**
     * Generate a color for the derivative function
     */
    private Color generateDerivativeColor(Color originalColor) {
        // Create a darker, more muted version of the original color
        float[] hsb = Color.RGBtoHSB(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * 0.8f, hsb[2] * 0.7f);
    }
    
    /**
     * Show dialog to select a function for extrema finding
     */
    private void showExtremaFunctionDialog(List<Function> functions) {
        // Only show unique expressions
        java.util.LinkedHashSet<String> uniqueExpressions = new java.util.LinkedHashSet<>();
        for (Function func : functions) {
            uniqueExpressions.add(func.getExpression());
        }
        String[] functionExpressions = uniqueExpressions.toArray(new String[0]);

        JComboBox<String> functionCombo = new JComboBox<>(functionExpressions);

        // Custom undecorated dialog
        JDialog dialog = new JDialog(this);
        dialog.setUndecorated(true);
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panel.setBackground(Theme.SURFACE);

        JPanel funcRow = new JPanel();
        funcRow.setLayout(new BoxLayout(funcRow, BoxLayout.X_AXIS));
        funcRow.setOpaque(false);
        JLabel funcLabel = new JLabel("Select a function to find extrema:");
        funcLabel.setFont(Theme.font(12));
        funcLabel.setForeground(Theme.TEXT);
        funcRow.add(funcLabel);
        funcRow.add(Box.createHorizontalStrut(10));
        functionCombo.setFont(Theme.font(12));
        funcRow.add(functionCombo);
        panel.add(funcRow);
        panel.add(Box.createVerticalStrut(12));

        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(12));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.setMargin(new Insets(2, 18, 2, 18));
        okButton.setBackground(Theme.ACCENT_SOFT);
        okButton.setForeground(Theme.TEXT);
        okButton.addActionListener(e -> {
            String selectedExpression = (String) functionCombo.getSelectedItem();
            if (selectedExpression != null) {
                // Find the first function with this expression
                Function selectedFunc = null;
                for (Function func : functions) {
                    if (func.getExpression().equals(selectedExpression)) {
                        selectedFunc = func;
                        break;
                    }
                }
                if (selectedFunc != null) {
                    dialog.dispose();
                    findAndDisplayExtrema(selectedFunc);
                }
            }
        });
        panel.add(okButton);

        dialog.getContentPane().add(panel);
        dialog.pack();
        // Center dialog on parent
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    /**
     * Find and display extrema for the selected function
     */
    private void findAndDisplayExtrema(Function function) {
        try {
            // Get the current view bounds
            double[] bounds = graphPanel.getViewportBounds();
            double minX = bounds[0];
            double maxX = bounds[1];
            
            // Find extrema using the CriticalPointFinder
            List<Point2D> localMaxima = findLocalMaxima(function.getExpression(), minX, maxX);
            List<Point2D> localMinima = findLocalMinima(function.getExpression(), minX, maxX);
            
            // Store extrema points for plotting
            graphPanel.getGraphRenderer().setExtremaPoints(localMaxima, localMinima);
            
            // Show results dialog
            showExtremaResultsDialog(function, localMaxima, localMinima);
            
            // Redraw the graph to show the extrema points
            graphPanel.repaint();
            
        } catch (Exception e) {
            showMinimalDialog(this,
                "Error finding extrema: " + e.getMessage());
        }
    }
    
    /**
     * Find local maxima of a function
     */
    private List<Point2D> findLocalMaxima(String expression, double minX, double maxX) {
        List<Point2D> maxima = new ArrayList<>();
        double step = (maxX - minX) / 1000; // Fine sampling
        
        for (double x = minX; x <= maxX; x += step) {
            try {
                double y = evaluateFunction(expression, x);
                double yLeft = evaluateFunction(expression, x - step);
                double yRight = evaluateFunction(expression, x + step);
                
                // Check if this is a local maximum
                if (y > yLeft && y > yRight && !Double.isNaN(y) && !Double.isInfinite(y)) {
                    // Refine the maximum point
                    double refinedX = refineExtremum(expression, x, step, true);
                    double refinedY = evaluateFunction(expression, refinedX);
                    maxima.add(new Point2D(refinedX, refinedY));
                }
            } catch (Exception e) {
                // Skip points where evaluation fails
            }
        }
        
        return maxima;
    }
    
    /**
     * Find local minima of a function
     */
    private List<Point2D> findLocalMinima(String expression, double minX, double maxX) {
        List<Point2D> minima = new ArrayList<>();
        double step = (maxX - minX) / 1000; // Fine sampling
        
        for (double x = minX; x <= maxX; x += step) {
            try {
                double y = evaluateFunction(expression, x);
                double yLeft = evaluateFunction(expression, x - step);
                double yRight = evaluateFunction(expression, x + step);
                
                // Check if this is a local minimum
                if (y < yLeft && y < yRight && !Double.isNaN(y) && !Double.isInfinite(y)) {
                    // Refine the minimum point
                    double refinedX = refineExtremum(expression, x, step, false);
                    double refinedY = evaluateFunction(expression, refinedX);
                    minima.add(new Point2D(refinedX, refinedY));
                }
            } catch (Exception e) {
                // Skip points where evaluation fails
            }
        }
        
        return minima;
    }
    
    /**
     * Refine the position of an extremum using binary search
     */
    private double refineExtremum(String expression, double x, double step, boolean isMaximum) {
        double left = x - step;
        double right = x + step;
        double tolerance = 1e-6;
        
        for (int i = 0; i < 20; i++) { // Max 20 iterations
            double mid = (left + right) / 2;
            double yMid = evaluateFunction(expression, mid);
            double yLeft = evaluateFunction(expression, left);
            double yRight = evaluateFunction(expression, right);
            
            if (isMaximum) {
                if (yMid > yLeft && yMid > yRight) {
                    return mid;
                } else if (yLeft > yRight) {
                    right = mid;
                } else {
                    left = mid;
                }
            } else {
                if (yMid < yLeft && yMid < yRight) {
                    return mid;
                } else if (yLeft < yRight) {
                    right = mid;
                } else {
                    left = mid;
                }
            }
            
            if (Math.abs(right - left) < tolerance) {
                break;
            }
        }
        
        return x;
    }
    
    /**
     * Evaluate a function at a given x value
     */
    private double evaluateFunction(String expression, double x) {
        // Use the existing ExpressionEvaluator
        return com.graphing.math.parser.ExpressionEvaluator.evaluate(expression, x);
    }
    
    /**
     * Show dialog with extrema results
     */
    private void showExtremaResultsDialog(Function function, List<Point2D> localMaxima, List<Point2D> localMinima) {
        // Create custom panel for the dialog
        StringBuilder sb = new StringBuilder();
        sb.append("Extrema for " + function.getExpression() + ":\n");
        if (!localMaxima.isEmpty()) {
            sb.append("Local Maxima (" + localMaxima.size() + "):\n");
            for (int i = 0; i < localMaxima.size(); i++) {
                Point2D point = localMaxima.get(i);
                sb.append(String.format("  Point %d: (%.4f, %.4f)\n", i + 1, point.getX(), point.getY()));
            }
        }
        if (!localMinima.isEmpty()) {
            sb.append("Local Minima (" + localMinima.size() + "):\n");
            for (int i = 0; i < localMinima.size(); i++) {
                Point2D point = localMinima.get(i);
                sb.append(String.format("  Point %d: (%.4f, %.4f)\n", i + 1, point.getX(), point.getY()));
            }
        }
        if (localMaxima.isEmpty() && localMinima.isEmpty()) {
            sb.append("No local extrema found in the current view.\n");
        }
        showMinimalDialog(this, sb.toString());
    }
    
    /**
     * Show dialog to select a function and bounds for definite integral
     */
    private void showDefiniteIntegralDialog(List<Function> functions) {
        // Only show unique expressions
        java.util.LinkedHashSet<String> uniqueExpressions = new java.util.LinkedHashSet<>();
        for (Function func : functions) {
            uniqueExpressions.add(func.getExpression());
        }
        String[] functionExpressions = uniqueExpressions.toArray(new String[0]);

        JComboBox<String> functionCombo = new JComboBox<>(functionExpressions);
        functionCombo.setFont(Theme.font(12));
        JTextField lowerField = new JTextField(8);
        JTextField upperField = new JTextField(8);
        lowerField.setFont(Theme.font(12));
        upperField.setFont(Theme.font(12));

        // Set default bounds to current graph view
        double[] bounds = graphPanel.getViewportBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        lowerField.setText(String.format("%.2f", minX));
        upperField.setText(String.format("%.2f", maxX));

        // Custom undecorated dialog
        JDialog dialog = new JDialog(this);
        dialog.setUndecorated(true);
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panel.setBackground(Theme.SURFACE);

        JLabel funcLabel = new JLabel("Select a function:");
        funcLabel.setFont(Theme.font(12));
        funcLabel.setForeground(Theme.TEXT);
        funcLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(funcLabel);
        panel.add(Box.createVerticalStrut(6));
        functionCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(functionCombo);
        panel.add(Box.createVerticalStrut(10));

        JLabel lowerLabel = new JLabel("Enter lower bound (x₁):");
        lowerLabel.setFont(Theme.font(12));
        lowerLabel.setForeground(Theme.TEXT);
        lowerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lowerLabel);
        lowerField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lowerField);
        panel.add(Box.createVerticalStrut(6));

        JLabel upperLabel = new JLabel("Enter upper bound (x₂):");
        upperLabel.setFont(Theme.font(12));
        upperLabel.setForeground(Theme.TEXT);
        upperLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(upperLabel);
        upperField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(upperField);
        panel.add(Box.createVerticalStrut(14));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(12));
        okButton.setMargin(new Insets(2, 18, 2, 18));
        okButton.setBackground(Theme.ACCENT_SOFT);
        okButton.setForeground(Theme.TEXT);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(Theme.font(12));
        cancelButton.setMargin(new Insets(2, 18, 2, 18));
        cancelButton.setBackground(Theme.SURFACE);
        cancelButton.setForeground(Theme.TEXT);
        okButton.addActionListener(e -> {
            String selectedExpression = (String) functionCombo.getSelectedItem();
            Function selectedFunc = null;
            for (Function func : functions) {
                if (func.getExpression().equals(selectedExpression)) {
                    selectedFunc = func;
                    break;
                }
            }
            String lowerText = lowerField.getText().trim();
            String upperText = upperField.getText().trim();
            double a, b;
            try {
                a = Double.parseDouble(lowerText);
                b = Double.parseDouble(upperText);
                if (a == b) {
                    showMinimalDialog(this, "Bounds must be different.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showMinimalDialog(this, "Please enter valid numbers for bounds.");
                return;
            }
            // Compute definite integral
            try {
                double value = com.graphing.math.calculus.IntegralCalculator.definiteIntegral(selectedFunc.getExpression(), a, b);
                dialog.dispose();
                showDefiniteIntegralResultDialog(selectedFunc, a, b, value);
                // Highlight area under curve
                graphPanel.setAreaHighlight(selectedFunc, a, b);
            } catch (Exception ex) {
                showMinimalDialog(this, "Error calculating integral: " + ex.getMessage());
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Show dialog with definite integral result
     */
    private void showDefiniteIntegralResultDialog(Function function, double a, double b, double value) {
        StringBuilder sb = new StringBuilder();
        sb.append(function.getExpression() + "\n");
        sb.append(String.format("x₁ = %.4f, x₂ = %.4f\n", a, b));
        sb.append(String.format("∫[x₁,x₂] f(x) dx = %.6f", value));
        showMinimalDialog(this, sb.toString());
    }
    
    private void updateControlPanelFromGraph() {
        GraphSettings settings = graphPanel.getSettings();
        double[] bounds = graphPanel.getViewportBounds();
        
        controlPanel.updateAxisLimits(bounds[0], bounds[1], bounds[2], bounds[3]);
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        // File Menu
        fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        // Workspace Menu
        JMenu workspaceMenu = new JMenu("Workspace");
        JMenuItem saveWorkspaceItem = new JMenuItem("Save Workspace");
        JMenuItem loadWorkspaceItem = new JMenuItem("Load Workspace");
        saveWorkspaceItem.addActionListener(e -> saveWorkspace());
        loadWorkspaceItem.addActionListener(e -> loadWorkspace());
        workspaceMenu.add(saveWorkspaceItem);
        workspaceMenu.add(loadWorkspaceItem);
        // Export Menu
        exportMenu = new JMenu("Export");
        JMenuItem exportSVGItem = new JMenuItem("Export as SVG");
        exportSVGItem.addActionListener(e -> exportAsSVG());
        exportMenu.add(exportSVGItem);
        // Help Menu
        helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem helpItem = new JMenuItem("Help");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(workspaceMenu);
        menuBar.add(exportMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Left panel for predefined functions and function input
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        leftPanel.setBackground(Theme.BG);
        
        // Add predefined functions panel at the top
        leftPanel.add(predefinedFunctionsPanel, BorderLayout.NORTH);
        
        // Add function input panel below
        leftPanel.add(functionInputPanel, BorderLayout.CENTER);
        
        // Right panel for graph and controls
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        rightPanel.setBackground(Theme.BG);
        JPanel graphPanelWrapper = new JPanel(new BorderLayout());
        graphPanelWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0)); // 16px padding at bottom
        graphPanelWrapper.setBackground(Theme.BG);
        graphPanelWrapper.add(graphPanel, BorderLayout.CENTER);
        rightPanel.add(graphPanelWrapper, BorderLayout.CENTER);
        rightPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Ask user if they want to save workspace before closing
                int choice = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Do you want to save your current workspace before closing?",
                    "Save Workspace",
                    JOptionPane.YES_NO_CANCEL_OPTION
                );
                
                if (choice == JOptionPane.YES_OPTION) {
                    // exportWorkspace(); // Removed workspace export
                } else if (choice == JOptionPane.CANCEL_OPTION) {
                    return; // Don't close the window
                }
                
                // Continue with closing
                dispose();
            }
        });
    }
    
    // Remove all workspace management methods (loadWorkspace, exportWorkspace, etc.)
    
    private void exportAsSVG() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export as SVG");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SVG files", "svg"));
        if (userSelectedDirectory != null) {
            fileChooser.setCurrentDirectory(userSelectedDirectory);
        }
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filepath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filepath.endsWith(".svg")) {
                filepath += ".svg";
            }
            List<Function> functions = getCurrentFunctions();
            GraphSettings settings = getCurrentGraphSettings();
            int width = graphPanel.getWidth();
            int height = graphPanel.getHeight();
            // Get intersection points and extrema from GraphRenderer
            List<com.graphing.math.IntersectionPoint> intersections = graphPanel.getGraphRenderer().getIntersectionPoints();
            List<com.graphing.math.Point2D> maxima = new ArrayList<>();
            List<com.graphing.math.Point2D> minima = new ArrayList<>();
            try {
                java.lang.reflect.Field maxField = graphPanel.getGraphRenderer().getClass().getDeclaredField("localMaxima");
                java.lang.reflect.Field minField = graphPanel.getGraphRenderer().getClass().getDeclaredField("localMinima");
                maxField.setAccessible(true);
                minField.setAccessible(true);
                maxima = (List<com.graphing.math.Point2D>) maxField.get(graphPanel.getGraphRenderer());
                minima = (List<com.graphing.math.Point2D>) minField.get(graphPanel.getGraphRenderer());
            } catch (Exception e) {
                // fallback: leave maxima/minima empty
            }
            // Get area highlight info from graphPanel
            Function areaFunction = null;
            Double areaStart = null, areaEnd = null;
            Color areaColor = null;
            try {
                java.lang.reflect.Field areaFuncField = graphPanel.getClass().getDeclaredField("areaFunction");
                java.lang.reflect.Field areaStartField = graphPanel.getClass().getDeclaredField("areaStart");
                java.lang.reflect.Field areaEndField = graphPanel.getClass().getDeclaredField("areaEnd");
                areaFuncField.setAccessible(true);
                areaStartField.setAccessible(true);
                areaEndField.setAccessible(true);
                areaFunction = (Function) areaFuncField.get(graphPanel);
                areaStart = (Double) areaStartField.get(graphPanel);
                areaEnd = (Double) areaEndField.get(graphPanel);
                if (areaFunction != null) {
                    areaColor = areaFunction.getColor();
                }
            } catch (Exception e) {
                // fallback: no area highlight
            }
            boolean exported = com.graphing.io.SVGExporter.exportToSVG(
                functions, settings, width, height, filepath, intersections, maxima, minima,
                areaFunction, areaStart, areaEnd, areaColor
            );
            if (exported) {
                JOptionPane.showMessageDialog(this, "Graph exported as SVG successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export SVG.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "WolframBeta - Advanced Graphing Calculator\n\n" +
            "Version 1.0\n" +
            "A powerful graphing calculator with calculus support\n\n" +
            "Features:\n" +
            "• Plot multiple functions\n" +
            "• Calculus operations (derivatives, integrals)\n" +
            "• Workspace management\n" +
            "• SVG export\n" +
            "• Interactive graph manipulation",
            "About WolframBeta",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showHelpDialog() {
        JOptionPane.showMessageDialog(this,
            "WolframBeta Help\n\n" +
            "Function Input:\n" +
            "• Enter mathematical expressions like 'x^2', 'sin(x)', 'exp(x)'\n" +
            "• Use standard mathematical notation\n\n" +
            "Graph Interaction:\n" +
            "• Mouse wheel to zoom\n" +
            "• Drag to pan\n" +
            "• Click on graph for coordinates\n\n" +
            "Workspace Management:\n" +
            "• Save/Load workspaces to preserve your functions and settings\n" +
            "• Export graphs as SVG for use in documents\n\n" +
            "Keyboard Shortcuts:\n" +
            "• Ctrl+S: Save workspace\n" +
            "• Ctrl+O: Load workspace\n" +
            "• Ctrl+E: Export as SVG",
            "Help",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Helper methods to get current state from components
    private List<Function> getCurrentFunctions() {
        return functionInputPanel.getFunctions();
    }
    
    private GraphSettings getCurrentGraphSettings() {
        return graphPanel.getSettings();
    }
    
    private Viewport getCurrentViewport() {
        return graphPanel.getViewport();
    }
    
    private void applyWorkspaceData(JSONSerializer.WorkspaceData data) {
        // Ensure all imported functions are visible
        List<Function> importedFunctions = data.getFunctions();
        if (importedFunctions != null) {
            for (Function f : importedFunctions) {
                f.setVisible(true);
            }
        }
        // Apply functions to function input panel
        functionInputPanel.setFunctions(data.getFunctions());
        // Apply functions to graph panel
        graphPanel.setFunctions(data.getFunctions());
        // Apply settings to graph panel
        graphPanel.setSettings(data.getSettings());
        // Apply viewport to graph panel
        graphPanel.setViewport(data.getViewport());
        // Update control panel
        updateControlPanelFromGraph();
        // Repaint the graph
        graphPanel.repaint();
    }

    private void showMinimalDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    private void requestFilePermissionsOnStartup() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("Select a folder for saving and loading files");
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setAcceptAllFileFilterUsed(false);
        int result = dirChooser.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            userSelectedDirectory = dirChooser.getSelectedFile();
        } else {
            JOptionPane.showMessageDialog(this,
                "File access permission is required to save and load workspaces.\nYou can grant access later when saving or loading.",
                "Permission Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static final String WORKSPACE_DIR = "/Users/stellarb0rg/Downloads/WolframBeta-main/workspaces";

    private void saveWorkspace() {
        java.io.File dir = new java.io.File(WORKSPACE_DIR);
        if (!dir.exists()) dir.mkdirs();
        // Prompt the user for a filename
        String filename = JOptionPane.showInputDialog(this, "Enter a name for your workspace file:", "Save Workspace", JOptionPane.PLAIN_MESSAGE);
        if (filename == null) return; // User cancelled
        filename = filename.trim();
        if (filename.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Filename cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!filename.toLowerCase().endsWith(".json")) filename += ".json";
        String filepath = WORKSPACE_DIR + "/" + filename;
        try {
            List<Function> functions = getCurrentFunctions();
            String json = serializeFunctionsToJson(functions);
            try (java.io.FileWriter writer = new java.io.FileWriter(filepath)) {
                writer.write(json);
            }
            JOptionPane.showMessageDialog(this, "Workspace saved to: " + filepath, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save workspace:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        // After saving, clear intersection and extrema points
        graphPanel.getGraphRenderer().clearIntersections();
        graphPanel.getGraphRenderer().clearExtremaPoints();
        graphPanel.repaint();
    }

    private void loadWorkspace() {
        java.io.File dir = new java.io.File(WORKSPACE_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "No workspace directory found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] files = dir.list((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "No workspace files found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Select a workspace to load:",
            "Load Workspace",
            JOptionPane.PLAIN_MESSAGE,
            null,
            files,
            files[0]
        );
        if (selected == null) return;
        String filepath = WORKSPACE_DIR + "/" + selected;
        try {
            StringBuilder content = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filepath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }
            System.out.println("[DEBUG] Raw JSON loaded:\n" + content.toString());
            String functionsSection = extractSection(content.toString(), "functions");
            System.out.println("[DEBUG] Extracted functions section:\n" + functionsSection);
            if (functionsSection != null) {
                String[] functionBlocks = functionsSection.split("},");
                for (String block : functionBlocks) {
                    System.out.println("[DEBUG] Function block:\n" + block);
                }
            }
            List<Function> functions = deserializeFunctionsFromJson(content.toString());
            System.out.println("[DEBUG] Loaded workspace: " + functions.size() + " functions");
            for (Function f : functions) {
                System.out.println("[DEBUG]   Function: " + f.getExpression());
            }
            // Clear all current functions before loading
            functionInputPanel.setFunctions(new java.util.ArrayList<>());
            graphPanel.clearFunctions(); // <-- Ensure graph is cleared too
            // For each loaded function, add it using functionInputPanel.addFunction (which will also plot it)
            for (Function f : functions) {
                System.out.println("[DEBUG] Adding function: " + f.getExpression() + ", color: " + f.getColor() + ", type: " + f.getType());
                functionInputPanel.addFunction(f);
            }
            // --- Ensure all functions are visible after loading ---
            for (int i = 0; i < functionInputPanel.getFunctions().size(); i++) {
                functionInputPanel.setFunctionVisibility(i, true);
            }
            functionInputPanel.repaint();
            graphPanel.repaint();
            System.out.println("[DEBUG] Final function visibilities in input panel:");
            for (int i = 0; i < functionInputPanel.getFunctions().size(); i++) {
                System.out.println("  " + i + ": " + functionInputPanel.getFunctions().get(i).getExpression() + " visible=" + functionInputPanel.getFunctions().get(i).isVisible());
            }
            // --- Recompute and display intersection points and extrema after loading ---
            java.util.List<Function> visibleFunctions = new java.util.ArrayList<>();
            for (Function f : graphPanel.getFunctions()) {
                if (f.isVisible()) visibleFunctions.add(f);
            }
            // Recompute intersections
            graphPanel.getGraphRenderer().findIntersections(visibleFunctions);
            // Recompute extrema for each function
            double[] bounds = graphPanel.getViewportBounds();
            double minX = bounds[0];
            double maxX = bounds[1];
            for (Function f : visibleFunctions) {
                java.util.List<Point2D> maxima = findLocalMaxima(f.getExpression(), minX, maxX);
                java.util.List<Point2D> minima = findLocalMinima(f.getExpression(), minX, maxX);
                graphPanel.getGraphRenderer().setExtremaPoints(maxima, minima);
            }
            graphPanel.repaint();
            javax.swing.SwingUtilities.invokeLater(() -> {
                functionInputPanel.repaint();
                functionInputPanel.revalidate();
                graphPanel.repaint();
                graphPanel.revalidate();
            });
            // Re-set the listener to ensure wiring is correct
            functionInputPanel.setFunctionInputListener(new FunctionInputPanel.FunctionInputListener() {
                @Override
                public void onFunctionAdded(Function function) {
                    graphPanel.addFunction(function);
                }
                @Override
                public void onFunctionRemoved(Function function) {
                    graphPanel.removeFunction(function);
                }
                @Override
                public void onFunctionUpdated(Function function) {
                    List<Function> functions = graphPanel.getFunctions();
                    for (int i = 0; i < functions.size(); i++) {
                        if (functions.get(i).getName().equals(function.getName())) {
                            graphPanel.updateFunction(i, function);
                            break;
                        }
                    }
                }
                @Override
                public void onFunctionsCleared() {
                    graphPanel.clearFunctions();
                }
            });
            JOptionPane.showMessageDialog(this, "Workspace loaded from: " + filepath, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load workspace:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        // After loading, clear intersection and extrema points
        graphPanel.getGraphRenderer().clearIntersections();
        graphPanel.getGraphRenderer().clearExtremaPoints();
        graphPanel.repaint();
        javax.swing.SwingUtilities.invokeLater(() -> {
            functionInputPanel.repaint();
        });
    }

    // Minimal JSON serialization for a list of functions
    private String serializeFunctionsToJson(List<Function> functions) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"functions\": [\n");
        for (int i = 0; i < functions.size(); i++) {
            Function f = functions.get(i);
            json.append("    {\n");
            json.append("      \"name\": \"").append(escapeJson(f.getName())).append("\",\n");
            json.append("      \"expression\": \"").append(escapeJson(f.getExpression())).append("\",\n");
            json.append("      \"type\": \"").append(f.getType() != null ? f.getType().toString() : "UNKNOWN").append("\",\n");
            json.append("      \"color\": \"").append(colorToHex(f.getColor())).append("\",\n");
            json.append("      \"visible\": ").append(f.isVisible()).append("\n");
            json.append("    }");
            if (i < functions.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");
        // --- Serialize intersections ---
        List<Function> visibleFunctions = new java.util.ArrayList<>();
        for (Function f : functions) if (f.isVisible()) visibleFunctions.add(f);
        java.util.List<com.graphing.math.IntersectionPoint> intersections = graphPanel.getGraphRenderer().findIntersections(visibleFunctions);
        json.append("  \"intersections\": [\n");
        for (int i = 0; i < intersections.size(); i++) {
            com.graphing.math.IntersectionPoint ip = intersections.get(i);
            json.append("    {\n");
            json.append("      \"x\": ").append(ip.getX()).append(",\n");
            json.append("      \"y\": ").append(ip.getY()).append(",\n");
            json.append("      \"functions\": [");
            java.util.List<String> exprs = ip.getFunctionExprs();
            for (int j = 0; j < exprs.size(); j++) {
                json.append("\"").append(escapeJson(exprs.get(j))).append("\"");
                if (j < exprs.size() - 1) json.append(", ");
            }
            json.append("]\n");
            json.append("    }");
            if (i < intersections.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");
        // --- Serialize shaded area (definite integral) ---
        try {
            java.lang.reflect.Field areaFuncField = graphPanel.getClass().getDeclaredField("areaFunction");
            java.lang.reflect.Field areaStartField = graphPanel.getClass().getDeclaredField("areaStart");
            java.lang.reflect.Field areaEndField = graphPanel.getClass().getDeclaredField("areaEnd");
            areaFuncField.setAccessible(true);
            areaStartField.setAccessible(true);
            areaEndField.setAccessible(true);
            Function areaFunction = (Function) areaFuncField.get(graphPanel);
            Double areaStart = (Double) areaStartField.get(graphPanel);
            Double areaEnd = (Double) areaEndField.get(graphPanel);
            if (areaFunction != null && areaStart != null && areaEnd != null) {
                json.append("  \"shadedArea\": {\n");
                json.append("    \"function\": \"").append(escapeJson(areaFunction.getName())).append("\",\n");
                json.append("    \"start\": ").append(areaStart).append(",\n");
                json.append("    \"end\": ").append(areaEnd).append("\n");
                json.append("  }\n");
            } else {
                json.append("  \"shadedArea\": null\n");
            }
        } catch (Exception e) {
            json.append("  \"shadedArea\": null\n");
        }
        json.append("}\n");
        return json.toString();
    }

    private List<Function> deserializeFunctionsFromJson(String json) {
        List<Function> functions = new java.util.ArrayList<>();
        String functionsSection = extractSection(json, "functions");
        if (functionsSection != null) {
            String[] functionBlocks = functionsSection.split("\\},");
            for (String block : functionBlocks) {
                block = block.trim();
                if (!block.endsWith("}")) block += "}";
                String name = extractJsonValue(block, "name");
                String expr = extractJsonValue(block, "expression");
                String typeStr = extractJsonValue(block, "type");
                String colorHex = extractJsonValue(block, "color");
                boolean visible = Boolean.parseBoolean(extractJsonValue(block, "visible"));
                java.awt.Color color = hexToColor(colorHex);
                com.graphing.math.FunctionType type = com.graphing.math.FunctionType.ALGEBRAIC;
                try {
                    if (typeStr != null && !typeStr.equals("UNKNOWN")) {
                        type = com.graphing.math.FunctionType.valueOf(typeStr);
                    }
                } catch (Exception ignored) {}
                com.graphing.math.Function f = new com.graphing.math.Function(expr, color, type, name);
                f.setVisible(visible);
                functions.add(f);
            }
        }
        // --- Restore shaded area (definite integral) ---
        int shadedIdx = json.indexOf("\"shadedArea\"");
        if (shadedIdx != -1) {
            int startObj = json.indexOf("{", shadedIdx);
            int endObj = json.indexOf("}", startObj);
            if (startObj != -1 && endObj != -1) {
                String areaBlock = json.substring(startObj + 1, endObj);
                String funcName = extractJsonValue(areaBlock, "function");
                String startStr = extractJsonValue(areaBlock, "start");
                String endStr = extractJsonValue(areaBlock, "end");
                if (funcName != null && startStr != null && endStr != null) {
                    for (Function f : functions) {
                        if (f.getName().equals(funcName)) {
                            try {
                                double a = Double.parseDouble(startStr);
                                double b = Double.parseDouble(endStr);
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    graphPanel.setAreaHighlight(f, a, b);
                                });
                            } catch (Exception ignored) {}
                            break;
                        }
                    }
                }
            }
        }
        // --- Restore intersection points (optional: for display, not plotting) ---
        // (You can add code here to display intersection points if desired)
        return functions;
    }

    // Helpers for minimal JSON parsing/encoding
    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
    private String extractSection(String json, String sectionName) {
        String key = "\"" + sectionName + "\"";
        int keyIdx = json.indexOf(key);
        if (keyIdx == -1) return null;
        int arrayStart = json.indexOf('[', keyIdx);
        if (arrayStart == -1) return null;
        int arrayEnd = json.indexOf(']', arrayStart);
        if (arrayEnd == -1) return null;
        return json.substring(arrayStart + 1, arrayEnd).trim();
    }
    private String extractJsonValue(String block, String key) {
        String pattern = "\"" + key + "\":";
        int idx = block.indexOf(pattern);
        if (idx == -1) return null;
        idx += pattern.length();
        // Skip whitespace
        while (idx < block.length() && Character.isWhitespace(block.charAt(idx))) idx++;
        if (block.charAt(idx) == '"') {
            int end = block.indexOf('"', idx + 1);
            return block.substring(idx + 1, end);
        } else {
            int end = block.indexOf('\n', idx);
            if (end == -1) end = block.length();
            return block.substring(idx, end).replace(",", "").trim();
        }
    }
    private String colorToHex(java.awt.Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    private java.awt.Color hexToColor(String hex) {
        try {
            return java.awt.Color.decode(hex);
        } catch (Exception e) {
            return java.awt.Color.BLACK;
        }
    }
} 
