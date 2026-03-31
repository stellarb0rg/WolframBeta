package com.graphing.ui;

import com.graphing.math.Function;
import com.graphing.math.Point2D;
import com.graphing.graph.*;
import com.graphing.ui.components.CoordinateTooltip;
import com.graphing.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles graph rendering, panning, zooming, mouse events
 */
public class GraphPanel extends JPanel {
    
    private final Viewport viewport;
    private final GraphSettings settings;
    private final GraphRenderer graphRenderer;
    private final CoordinateTooltip tooltip;
    
    private List<Function> functions;
    private Point lastMousePoint;
    private boolean isDragging = false;
    private boolean isZooming = false;
    
    // Overlay zoom button bounds
    private Rectangle plusButtonBounds = new Rectangle(0, 0, 32, 32);
    private Rectangle minusButtonBounds = new Rectangle(0, 36, 32, 32);
    
    // Store the last highlighted point
    private Point2D highlightedPoint = null;
    private Color highlightedColor = null;

    // Area highlight for definite integral
    private Function areaFunction = null;
    private Double areaStart = null, areaEnd = null;

    public GraphPanel() {
        this.functions = new ArrayList<>();
        this.settings = new GraphSettings();
        this.viewport = new Viewport(800, 600);
        this.graphRenderer = new GraphRenderer(viewport, settings);
        this.tooltip = new CoordinateTooltip();
        
        // Initialize viewport with the default range from GraphSettings
        viewport.setView(settings.getMinX(), settings.getMaxX(), settings.getMinY(), settings.getMaxY());
        
        initializePanel();
        setupEventHandlers();
    }
    

    
    private void initializePanel() {
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(400, 300));
        setBackground(settings.getBackgroundColor());
        setFocusable(true);

    }
    
    private void setupEventHandlers() {
        // Mouse listeners for panning and zooming
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
                requestFocusInWindow();
                // Check zoom overlay buttons
                if (plusButtonBounds.contains(e.getPoint())) {
                    zoomIn();
                } else if (minusButtonBounds.contains(e.getPoint())) {
                    zoomOut();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                isZooming = false;
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Only reset view if double-click is NOT on a zoom button
                    if (!plusButtonBounds.contains(e.getPoint()) && !minusButtonBounds.contains(e.getPoint())) {
                        resetView();
                    }
                } else if (e.getClickCount() == 1) {
                    // First, check if an intersection dot was clicked
                    if (handleIntersectionDotClick(e)) {
                        return;
                    }
                    // Otherwise, single click to find interactive point
                    handleInteractivePointClick(e);
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    int dx = e.getX() - lastMousePoint.x;
                    int dy = e.getY() - lastMousePoint.y;
                    
                    if (e.isControlDown()) {
                        // Zoom with Ctrl+drag
                        double zoomFactor = 1.0 + (dy * 0.01);
                        viewport.zoomAtScreenPoint(zoomFactor, e.getX(), e.getY());
                        isZooming = true;
                    } else {
                        // Pan
                        viewport.panByScreen(-dx, -dy);
                        isDragging = true;
                    }
                    
                    lastMousePoint = e.getPoint();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                if (settings.isShowCoordinates()) {
                    Point2D mathPoint = viewport.screenToMath(e.getX(), e.getY());
                    tooltip.showTooltip(e.getX(), e.getY(), mathPoint);
                }
            }
        });
        
        addMouseWheelListener(e -> {
            // Zoom with mouse wheel
            double zoomFactor = e.getWheelRotation() > 0 ? 0.98 : 1.02;
            viewport.zoomAtScreenPoint(zoomFactor, e.getX(), e.getY());
            repaint();
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        
        // Component listener for resize events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                viewport.setScreenDimensions(getWidth(), getHeight());
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Update viewport dimensions
        viewport.setScreenDimensions(getWidth(), getHeight());
        
        // Render the graph
        graphRenderer.render(g2d, functions);
        
        // Draw zoom overlay buttons (top right)
        int x = getWidth() - 40;
        int y = 10;
        plusButtonBounds.setLocation(x, y);
        minusButtonBounds.setLocation(x, y + 36);
        // Draw + button
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(plusButtonBounds.x, plusButtonBounds.y, plusButtonBounds.width, plusButtonBounds.height, 12, 12);
        g2d.setColor(Theme.BORDER);
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(plusButtonBounds.x, plusButtonBounds.y, plusButtonBounds.width, plusButtonBounds.height, 10, 10);
        g2d.setStroke(oldStroke);
        g2d.setFont(Theme.font(Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String plus = "+";
        int plusWidth = fm.stringWidth(plus);
        int plusHeight = fm.getAscent();
        int plusX = plusButtonBounds.x + (plusButtonBounds.width - plusWidth) / 2;
        int plusY = plusButtonBounds.y + (plusButtonBounds.height + plusHeight) / 2 - 3;
        g2d.setColor(Theme.TEXT);
        g2d.drawString(plus, plusX, plusY);
        // Draw - button
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(minusButtonBounds.x, minusButtonBounds.y, minusButtonBounds.width, minusButtonBounds.height, 12, 12);
        g2d.setColor(Theme.BORDER);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(minusButtonBounds.x, minusButtonBounds.y, minusButtonBounds.width, minusButtonBounds.height, 10, 10);
        g2d.setStroke(oldStroke);
        g2d.setFont(Theme.font(Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        String minus = "-";
        int minusWidth = fm.stringWidth(minus);
        int minusHeight = fm.getAscent();
        int minusX = minusButtonBounds.x + (minusButtonBounds.width - minusWidth) / 2;
        int minusY = minusButtonBounds.y + (minusButtonBounds.height + minusHeight) / 2 - 3;
        g2d.setColor(Theme.TEXT);
        g2d.drawString(minus, minusX, minusY);
        // Draw highlighted point if present
        if (highlightedPoint != null) {
            Point2D screenPt = getScreenCoordinates(highlightedPoint.getX(), highlightedPoint.getY());
            int dotSize = 4;
            int dotX = (int)screenPt.getX() - dotSize / 2;
            int dotY = (int)screenPt.getY() - dotSize / 2;
            g2d.setColor(Color.WHITE);
            g2d.fillOval(dotX, dotY, dotSize, dotSize);
            g2d.setColor(Theme.ACCENT_DARK);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(dotX, dotY, dotSize, dotSize);
        }
        // Draw area highlight if present
        if (areaFunction != null && areaStart != null && areaEnd != null) {
            getGraphRenderer().renderFunctionWithArea(g2d, areaFunction, areaStart, areaEnd);
        }
        g2d.dispose();
    }
    
    /**
     * Add a function to the graph
     */
    public void addFunction(Function function) {
        functions.add(function);
        repaint();
    }
    
    /**
     * Remove a function from the graph
     */
    public void removeFunction(Function function) {
        functions.remove(function);
        graphRenderer.clearIntersections();
        repaint();
    }
    
    /**
     * Remove a function by index
     */
    public void removeFunction(int index) {
        if (index >= 0 && index < functions.size()) {
            functions.remove(index);
            repaint();
        }
    }
    
    /**
     * Clear all functions
     */
    public void clearFunctions() {
        functions.clear();
        graphRenderer.clearIntersections();
        clearAreaHighlight(); // Also clear the area highlight when clearing all functions
        graphRenderer.clearExtremaPoints(); // Also clear extrema points
        repaint();
    }
    
    /**
     * Get all functions
     */
    public List<Function> getFunctions() {
        return new ArrayList<>(functions);
    }
    
    /**
     * Set functions
     */
    public void setFunctions(List<Function> functions) {
        this.functions = new ArrayList<>(functions);
        repaint();
    }
    
    /**
     * Update a function
     */
    public void updateFunction(int index, Function function) {
        if (index >= 0 && index < functions.size()) {
            functions.set(index, function);
            repaint();
        }
    }
    
    /**
     * Get the graph settings
     */
    public GraphSettings getSettings() {
        return settings;
    }
    
    /**
     * Set the graph settings
     */
    public void setSettings(GraphSettings settings) {
        this.settings.setRange(settings.getMinX(), settings.getMaxX(), 
                              settings.getMinY(), settings.getMaxY());
        this.settings.setShowGrid(settings.isShowGrid());
        this.settings.setShowAxes(settings.isShowAxes());
        this.settings.setGridSpacing(settings.getGridSpacing());
        this.settings.setPlotPoints(settings.getPlotPoints());
        this.settings.setLineThickness(settings.getLineThickness());
        this.settings.setAntiAliasing(settings.isAntiAliasing());
        this.settings.setShowCoordinates(settings.isShowCoordinates());
        this.settings.setShowFunctionLabels(settings.isShowFunctionLabels());
        this.settings.setShowCriticalPoints(settings.isShowCriticalPoints());
        this.settings.setShowIntersections(settings.isShowIntersections());
        this.settings.setBackgroundColor(settings.getBackgroundColor());
        this.settings.setGridColor(settings.getGridColor());
        this.settings.setAxesColor(settings.getAxesColor());
        
        repaint();
    }
    
    /**
     * Reset the view to default
     */
    public void resetView() {
        // Reset to the default range from GraphSettings
        viewport.setView(settings.getMinX(), settings.getMaxX(), settings.getMinY(), settings.getMaxY());
        repaint();
    }
    
    /**
     * Zoom in
     */
    public void zoomIn() {
        viewport.zoom(1.2);
        repaint();
    }
    
    /**
     * Zoom out
     */
    public void zoomOut() {
        viewport.zoom(0.8);
        repaint();
    }
    
    /**
     * Set the view to show a specific range
     */
    public void setView(double minX, double maxX, double minY, double maxY) {
        viewport.setView(minX, maxX, minY, maxY);
        repaint();
    }
    
    /**
     * Auto-scale the view to fit all functions
     */
    public void autoScale() {
        if (functions.isEmpty()) return;
        
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        // Sample points from all functions to find bounds
        for (Function function : functions) {
            if (!function.isVisible()) continue;
            
            double[] bounds = viewport.getVisibleBounds();
            double step = (bounds[1] - bounds[0]) / settings.getPlotPoints();
            
            for (int i = 0; i < settings.getPlotPoints(); i++) {
                double x = bounds[0] + i * step;
                try {
                    double y = com.graphing.math.parser.ExpressionEvaluator.evaluate(function.getExpression(), x);
                    if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                } catch (Exception e) {
                    // Skip invalid points
                }
            }
        }
        
        if (minX != Double.POSITIVE_INFINITY && maxX != Double.NEGATIVE_INFINITY) {
            double margin = (maxX - minX) * 0.1;
            setView(minX - margin, maxX + margin, minY - margin, maxY + margin);
        }
    }
    
    /**
     * Handle keyboard shortcuts
     */
    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_R:
                if (e.isControlDown()) {
                    resetView();
                }
                break;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                if (e.isControlDown()) {
                    zoomIn();
                }
                break;
            case KeyEvent.VK_MINUS:
                if (e.isControlDown()) {
                    zoomOut();
                }
                break;
            case KeyEvent.VK_A:
                if (e.isControlDown()) {
                    autoScale();
                }
                break;
        }
    }
    
    /**
     * Get the mathematical coordinates at a screen point
     */
    public Point2D getMathCoordinates(int screenX, int screenY) {
        return viewport.screenToMath(screenX, screenY);
    }
    
    /**
     * Get the screen coordinates for a mathematical point
     */
    public Point2D getScreenCoordinates(double mathX, double mathY) {
        return viewport.mathToScreen(mathX, mathY);
    }
    
    /**
     * Check if a mathematical point is visible
     */
    public boolean isPointVisible(double mathX, double mathY) {
        return viewport.isPointVisible(mathX, mathY);
    }
    
    /**
     * Get the current viewport bounds
     */
    public double[] getViewportBounds() {
        return viewport.getVisibleBounds();
    }
    
    /**
     * Set the coordinate tooltip visibility
     */
    public void setShowCoordinates(boolean show) {
        settings.setShowCoordinates(show);
        if (!show) {
            tooltip.hideTooltip();
        }
        repaint();
    }
    
    /**
     * Get the coordinate tooltip
     */
    public CoordinateTooltip getTooltip() {
        return tooltip;
    }
    
    /**
     * Get the viewport
     */
    public Viewport getViewport() {
        return viewport;
    }
    
    /**
     * Get the graph renderer
     */
    public GraphRenderer getGraphRenderer() {
        return graphRenderer;
    }
    
    public void setAreaHighlight(Function function, double start, double end) {
        this.areaFunction = function;
        this.areaStart = start;
        this.areaEnd = end;
        repaint();
    }

    public void clearAreaHighlight() {
        this.areaFunction = null;
        this.areaStart = null;
        this.areaEnd = null;
        repaint();
    }

    /**
     * Set the viewport
     */
    public void setViewport(Viewport newViewport) {
        // Update the viewport with new settings
        viewport.setCenterX(newViewport.getCenterX());
        viewport.setCenterY(newViewport.getCenterY());
        viewport.setZoomLevel(newViewport.getZoomLevel());
        viewport.setScreenDimensions(newViewport.getScreenWidth(), newViewport.getScreenHeight());
        repaint();
    }
    
    /**
     * Handle interactive point click to find exact function coordinates
     */
    private void handleInteractivePointClick(MouseEvent e) {
        if (functions.isEmpty()) {
            return;
        }
        
        int clickX = e.getX();
        int clickY = e.getY();
        double tolerance = 5.0; // Pixel tolerance for curve detection
        
        // Convert screen coordinates to math coordinates
        Point2D mathPoint = viewport.screenToMath(clickX, clickY);
        double clickMathX = mathPoint.getX();
        
        Function closestFunction = null;
        Point2D closestPoint = null;
        double minDistance = Double.MAX_VALUE;
        
        // Check each function for proximity to the click point
        for (Function function : functions) {
            if (!function.isVisible()) {
                continue;
            }
            
            try {
                // Evaluate the function at the clicked x-coordinate
                double functionY = com.graphing.math.parser.ExpressionEvaluator.evaluate(
                    function.getExpression(), clickMathX);
                
                if (!Double.isNaN(functionY) && !Double.isInfinite(functionY)) {
                    // Convert function point to screen coordinates
                    Point2D functionScreenPoint = viewport.mathToScreen(clickMathX, functionY);
                    
                    // Calculate distance from click to function point
                    double distance = Math.sqrt(
                        Math.pow(clickX - functionScreenPoint.getX(), 2) + 
                        Math.pow(clickY - functionScreenPoint.getY(), 2)
                    );
                    
                    // If within tolerance and closer than previous best
                    if (distance <= tolerance && distance < minDistance) {
                        minDistance = distance;
                        closestFunction = function;
                        closestPoint = new Point2D(clickMathX, functionY);
                    }
                }
            } catch (Exception ex) {
                // Skip functions that can't be evaluated at this point
            }
        }
        
        // If we found a function close to the click point, show the coordinates
        if (closestFunction != null && closestPoint != null) {
            showInteractivePointDialog(closestFunction, closestPoint);
        }
    }
    
    /**
     * Show dialog with interactive point coordinates
     */
    private void showInteractivePointDialog(Function function, Point2D point) {
        // Highlight the clicked point
        highlightedPoint = point;
        highlightedColor = function.getColor();
        repaint();

        // Create custom dialog for the interactive point
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setUndecorated(true);
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // No black border

        // Colored circle for the function
        JPanel colorCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(function.getColor());
                g2d.fillOval(0, 0, 12, 12);
                g2d.dispose();
            }
        };
        colorCircle.setPreferredSize(new Dimension(12, 12));
        colorCircle.setMaximumSize(new Dimension(12, 12));
        colorCircle.setOpaque(false);
        panel.add(colorCircle);
        panel.add(Box.createHorizontalStrut(8));

        // Function equation label (smaller, not bold)
        JLabel eqLabel = new JLabel(function.getExpression());
        eqLabel.setFont(Theme.font(11));
        panel.add(eqLabel);
        panel.add(Box.createHorizontalStrut(10));

        // Coordinates label (smaller, not bold)
        JLabel coordLabel = new JLabel(String.format("(%.4f, %.4f)", point.getX(), point.getY()));
        coordLabel.setFont(Theme.font(11));
        panel.add(coordLabel);
        panel.add(Box.createHorizontalStrut(10));

        // OK button
        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(11));
        okButton.setMargin(new Insets(2, 8, 2, 8));
        okButton.addActionListener(e -> {
            dialog.dispose();
            highlightedPoint = null;
            highlightedColor = null;
            repaint();
        });
        panel.add(okButton);

        dialog.getContentPane().add(panel);
        dialog.pack();

        // Offset dialog from clicked point (30px right, 30px up)
        Point screenPoint = MouseInfo.getPointerInfo().getLocation();
        int dialogX = screenPoint.x + 30;
        int dialogY = screenPoint.y - 30;

        // Clamp dialog position within the main window borders
        Window mainWindow = SwingUtilities.getWindowAncestor(this);
        if (mainWindow != null) {
            Rectangle appBounds = mainWindow.getBounds();
            Dimension dialogSize = dialog.getSize();
            // Clamp X
            if (dialogX + dialogSize.width > appBounds.x + appBounds.width) {
                dialogX = appBounds.x + appBounds.width - dialogSize.width - 10;
            }
            if (dialogX < appBounds.x) {
                dialogX = appBounds.x + 10;
            }
            // Clamp Y
            if (dialogY + dialogSize.height > appBounds.y + appBounds.height) {
                dialogY = appBounds.y + appBounds.height - dialogSize.height - 10;
            }
            if (dialogY < appBounds.y) {
                dialogY = appBounds.y + 10;
            }
        }
        dialog.setLocation(dialogX, dialogY);

        dialog.setVisible(true);
    }

    /**
     * Check if an intersection dot was clicked, and show dialog if so
     */
    private boolean handleIntersectionDotClick(MouseEvent e) {
        List<com.graphing.math.IntersectionPoint> intersectionPoints = graphRenderer.getIntersectionPoints();
        int clickX = e.getX();
        int clickY = e.getY();
        int dotRadius = 6; // Should match the drawn dot size in GraphRenderer
        for (com.graphing.math.IntersectionPoint ip : intersectionPoints) {
            Point2D mathPt = ip.getPoint();
            Point2D screenPt = getScreenCoordinates(mathPt.getX(), mathPt.getY());
            int sx = (int) screenPt.getX();
            int sy = (int) screenPt.getY();
            double dist = Math.sqrt(Math.pow(clickX - sx, 2) + Math.pow(clickY - sy, 2));
            if (dist <= dotRadius + 2) { // Allow a little tolerance
                showIntersectionDialog(ip);
                return true;
            }
        }
        return false;
    }

    /**
     * Show a dialog with intersection info
     */
    private void showIntersectionDialog(com.graphing.math.IntersectionPoint ip) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setUndecorated(true);
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Theme.SURFACE);

        // Colored circle for the intersection point
        JPanel colorCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ip.getColor());
                g2d.fillOval(0, 0, 12, 12);
                g2d.dispose();
            }
        };
        colorCircle.setPreferredSize(new Dimension(12, 12));
        colorCircle.setMaximumSize(new Dimension(12, 12));
        colorCircle.setOpaque(false);
        panel.add(colorCircle);
        panel.add(Box.createHorizontalStrut(8));

        // Build the text in the requested format
        java.util.List<String> exprs = ip.getFunctionExprs();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exprs.size(); i++) {
            sb.append(exprs.get(i));
            if (i < exprs.size() - 1) sb.append(", ");
        }
        sb.append(" intersect at ");
        sb.append(String.format("(%.4f, %.4f)", ip.getX(), ip.getY()));
        JLabel infoLabel = new JLabel(sb.toString());
        infoLabel.setFont(Theme.font(11));
        infoLabel.setForeground(Theme.TEXT);
        panel.add(infoLabel);
        panel.add(Box.createHorizontalStrut(10));

        // OK button in the same line
        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(11));
        okButton.setMargin(new Insets(2, 8, 2, 8));
        okButton.setBackground(Theme.ACCENT_SOFT);
        okButton.setForeground(Theme.TEXT);
        okButton.addActionListener(e -> dialog.dispose());
        panel.add(okButton);

        dialog.getContentPane().add(panel);
        dialog.pack();

        // Offset dialog from clicked point (30px right, 30px up)
        Point screenPoint = MouseInfo.getPointerInfo().getLocation();
        int dialogX = screenPoint.x + 30;
        int dialogY = screenPoint.y - 30;

        // Clamp dialog position within the main window borders
        Window mainWindow = SwingUtilities.getWindowAncestor(this);
        if (mainWindow != null) {
            Rectangle appBounds = mainWindow.getBounds();
            Dimension dialogSize = dialog.getSize();
            // Clamp X
            if (dialogX + dialogSize.width > appBounds.x + appBounds.width) {
                dialogX = appBounds.x + appBounds.width - dialogSize.width - 10;
            }
            if (dialogX < appBounds.x) {
                dialogX = appBounds.x + 10;
            }
            // Clamp Y
            if (dialogY + dialogSize.height > appBounds.y + appBounds.height) {
                dialogY = appBounds.y + appBounds.height - dialogSize.height - 10;
            }
            if (dialogY < appBounds.y) {
                dialogY = appBounds.y + 10;
            }
        }
        dialog.setLocation(dialogX, dialogY);

        dialog.setVisible(true);
    }
} 
