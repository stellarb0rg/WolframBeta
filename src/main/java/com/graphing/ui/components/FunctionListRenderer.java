package com.graphing.ui.components;

import com.graphing.math.Function;
import com.graphing.ui.Theme;
import javax.swing.*;
import java.awt.*;

/**
 * Custom renderer for function list with color indicators
 */
public class FunctionListRenderer extends DefaultListCellRenderer {
    
    private static final int COLOR_SQUARE_SIZE = 12; // smaller circle
    private static final int PADDING = 4;
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw separator line at the bottom except for the last item
                if (index < list.getModel().getSize() - 1) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Theme.BORDER);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(12, getHeight() - 1, getWidth() - 12, getHeight() - 1); // Indent line
                    g2.dispose();
                }
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        // Use a rounded border for a modern look
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18)); // More padding

        // Background color with rounded corners
        Color bg = isSelected ? Theme.ACCENT_SOFT : Theme.SURFACE;
        JComponent background = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        background.setOpaque(false);
        panel.add(background, BorderLayout.CENTER);
        background.setLayout(new BorderLayout());

        // Prepare the label (formula)
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setFont(Theme.font(13));
        setOpaque(false);
        setBorder(null);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        // Set text color to function color, or selection color if selected
        if (value instanceof Function) {
            Function function = (Function) value;
            System.out.println("[DEBUG] Renderer for row " + index + ": " + function.getExpression() + " visible=" + function.isVisible());
            setText(function.getName());
            setToolTipText(function.getExpression());
            setIcon(new VisibilityIcon(function.isVisible())); // Restore visibility icon
            setForeground(isSelected ? Theme.TEXT : function.getColor());
        } else {
            setForeground(isSelected ? Theme.TEXT : Theme.MUTED_TEXT);
        }
        background.add(this, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Create a color icon for the function (circle)
     */
    private Icon createColorIcon(Color color, boolean visible) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Enable anti-aliasing
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                int diameter = COLOR_SQUARE_SIZE;
                if (visible) {
                    // Draw filled color circle
                    g2d.setColor(color);
                    g2d.fillOval(x, y, diameter, diameter);
                    
                    // No outline
                } else {
                    // Draw grayed out circle for invisible functions
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillOval(x, y, diameter, diameter);
                    
                    // No outline
                    
                    // Draw diagonal line to indicate disabled state
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setStroke(new BasicStroke(2.0f));
                    g2d.drawLine(x + 2, y + 2, x + diameter - 2, y + diameter - 2);
                }
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return COLOR_SQUARE_SIZE + PADDING;
            }
            
            @Override
            public int getIconHeight() {
                return COLOR_SQUARE_SIZE;
            }
        };
    }
    
    /**
     * Create a custom icon with different styles
     */
    public static Icon createCustomColorIcon(Color color, boolean visible, String style) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                switch (style.toLowerCase()) {
                    case "circle":
                        paintCircleIcon(g2d, x, y, color, visible);
                        break;
                    case "triangle":
                        paintTriangleIcon(g2d, x, y, color, visible);
                        break;
                    case "diamond":
                        paintDiamondIcon(g2d, x, y, color, visible);
                        break;
                    default:
                        paintSquareIcon(g2d, x, y, color, visible);
                        break;
                }
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return COLOR_SQUARE_SIZE + PADDING;
            }
            
            @Override
            public int getIconHeight() {
                return COLOR_SQUARE_SIZE;
            }
        };
    }
    
    private static void paintSquareIcon(Graphics2D g2d, int x, int y, Color color, boolean visible) {
        if (visible) {
            g2d.setColor(color);
            g2d.fillRect(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
            g2d.setColor(Color.GRAY);
            g2d.drawRect(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(x + 2, y + 2, x + COLOR_SQUARE_SIZE - 2, y + COLOR_SQUARE_SIZE - 2);
        }
    }
    
    private static void paintCircleIcon(Graphics2D g2d, int x, int y, Color color, boolean visible) {
        if (visible) {
            g2d.setColor(color);
            g2d.fillOval(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
            g2d.setColor(Color.GRAY);
            g2d.drawOval(x, y, COLOR_SQUARE_SIZE, COLOR_SQUARE_SIZE);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(x + 2, y + 2, x + COLOR_SQUARE_SIZE - 2, y + COLOR_SQUARE_SIZE - 2);
        }
    }
    
    private static void paintTriangleIcon(Graphics2D g2d, int x, int y, Color color, boolean visible) {
        int[] xPoints = {x + COLOR_SQUARE_SIZE/2, x, x + COLOR_SQUARE_SIZE};
        int[] yPoints = {y, y + COLOR_SQUARE_SIZE, y + COLOR_SQUARE_SIZE};
        
        if (visible) {
            g2d.setColor(color);
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(xPoints, yPoints, 3);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.GRAY);
            g2d.drawPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(x + 2, y + 2, x + COLOR_SQUARE_SIZE - 2, y + COLOR_SQUARE_SIZE - 2);
        }
    }
    
    private static void paintDiamondIcon(Graphics2D g2d, int x, int y, Color color, boolean visible) {
        int[] xPoints = {x + COLOR_SQUARE_SIZE/2, x, x + COLOR_SQUARE_SIZE/2, x + COLOR_SQUARE_SIZE};
        int[] yPoints = {y, y + COLOR_SQUARE_SIZE/2, y + COLOR_SQUARE_SIZE, y + COLOR_SQUARE_SIZE/2};
        
        if (visible) {
            g2d.setColor(color);
            g2d.fillPolygon(xPoints, yPoints, 4);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(xPoints, yPoints, 4);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(xPoints, yPoints, 4);
            g2d.setColor(Color.GRAY);
            g2d.drawPolygon(xPoints, yPoints, 4);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(x + 2, y + 2, x + COLOR_SQUARE_SIZE - 2, y + COLOR_SQUARE_SIZE - 2);
        }
    }
    
    /**
     * Set the icon style for the renderer
     */
    public void setIconStyle(String style) {
        // This method can be used to change the icon style dynamically
        // Implementation would depend on how you want to handle style changes
    }
    
    /**
     * Get the preferred size for the renderer
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width += COLOR_SQUARE_SIZE + PADDING;
        return size;
    }

    // Composite icon: visibility toggle + color circle
    private static class CompositeIcon implements Icon {
        private final Color color;
        private final boolean visible;
        private static final int DIAM = 12;
        private static final int GAP = 6;
        public CompositeIcon(Color color, boolean visible) {
            this.color = color;
            this.visible = visible;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Draw visibility toggle (checkbox style)
            int boxX = x;
            int boxY = y + 1;
            int boxSize = DIAM;
            if (visible) {
                // Filled rounded square
                g2d.setColor(Theme.ACCENT_SOFT);
                g2d.fillRoundRect(boxX, boxY, boxSize, boxSize, 5, 5);
                g2d.setColor(Theme.ACCENT_DARK);
                g2d.drawRoundRect(boxX, boxY, boxSize, boxSize, 5, 5);
                // Draw blue checkmark
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(Theme.ACCENT_DARK);
                int cx = boxX + 3;
                int cy = boxY + boxSize / 2;
                g2d.drawLine(cx, cy, cx + 2, cy + 3);
                g2d.drawLine(cx + 2, cy + 3, cx + 7, cy - 2);
            } else {
                // Outlined rounded square
                g2d.setColor(Theme.BORDER);
                g2d.drawRoundRect(boxX, boxY, boxSize, boxSize, 5, 5);
            }
            // Draw color circle
            int colorX = x + DIAM + GAP;
            int colorY = y + 1;
            g2d.setColor(color);
            g2d.fillOval(colorX, colorY, DIAM, DIAM);
            g2d.dispose();
        }
        @Override
        public int getIconWidth() { return DIAM * 2 + GAP + PADDING; }
        @Override
        public int getIconHeight() { return DIAM + 2; }
    }

    // Only the visibility toggle icon
    private static class VisibilityIcon implements Icon {
        private final boolean visible;
        private static final int DIAM = 12;
        public VisibilityIcon(boolean visible) {
            this.visible = visible;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int boxX = x;
            int boxY = y + 1;
            int boxSize = DIAM;
            if (visible) {
                g2d.setColor(Theme.ACCENT_SOFT);
                g2d.fillRoundRect(boxX, boxY, boxSize, boxSize, 5, 5);
                g2d.setColor(Theme.ACCENT_DARK);
                g2d.drawRoundRect(boxX, boxY, boxSize, boxSize, 5, 5);
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(Theme.ACCENT_DARK);
                int cx = boxX + 3;
                int cy = boxY + boxSize / 2;
                g2d.drawLine(cx, cy, cx + 2, cy + 3);
                g2d.drawLine(cx + 2, cy + 3, cx + 7, cy - 2);
            } else {
                g2d.setColor(Theme.BORDER);
                g2d.drawRoundRect(boxX, boxY, boxSize, boxSize, 5, 5);
            }
            g2d.dispose();
        }
        @Override
        public int getIconWidth() { return DIAM + PADDING; }
        @Override
        public int getIconHeight() { return DIAM + 2; }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Paint the color circle at the far right if present
        Color color = (Color) getClientProperty("functionColor");
        if (color != null) {
            int diameter = 12;
            int x = getWidth() - diameter - 8;
            int y = (getHeight() - diameter) / 2;
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.fillOval(x, y, diameter, diameter);
            g2d.dispose();
        }
    }
} 
