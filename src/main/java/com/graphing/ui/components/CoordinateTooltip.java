package com.graphing.ui.components;

import com.graphing.math.Point2D;
import com.graphing.ui.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


/**
 * Tooltip for showing (x, y) values on hover
 */
public class CoordinateTooltip extends JWindow {
    
    private JLabel label;
    private Point2D currentPoint;
    private boolean isVisible = false;
    
    public CoordinateTooltip() {
        initializeComponents();
    }
    
    public CoordinateTooltip(Window owner) {
        super(owner);
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Create tooltip label
        label = new JLabel();
        label.setFont(Theme.font(12));
        label.setForeground(Color.WHITE);
        label.setBackground(new Color(17, 19, 24, 220));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        // Set up the window
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        
        // Make window non-focusable
        setFocusableWindowState(false);
    }
    
    /**
     * Show tooltip at the specified screen coordinates
     */
    public void showTooltip(int screenX, int screenY, Point2D mathPoint) {
        if (mathPoint == null) {
            hideTooltip();
            return;
        }
        
        currentPoint = mathPoint;
        updateTooltipText();
        
        // Position tooltip near mouse but not under it
        int tooltipX = screenX + 10;
        int tooltipY = screenY - 10;
        
        // Ensure tooltip stays within screen bounds
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension tooltipSize = getPreferredSize();
        
        if (tooltipX + tooltipSize.width > screenSize.width) {
            tooltipX = screenX - tooltipSize.width - 10;
        }
        if (tooltipY - tooltipSize.height < 0) {
            tooltipY = screenY + tooltipSize.height + 10;
        }
        
        setLocation(tooltipX, tooltipY);
        
        if (!isVisible) {
            setVisible(true);
            isVisible = true;
        }
    }
    
    /**
     * Hide the tooltip
     */
    public void hideTooltip() {
        if (isVisible) {
            setVisible(false);
            isVisible = false;
        }
        currentPoint = null;
    }
    
    /**
     * Update tooltip text with current coordinates
     */
    private void updateTooltipText() {
        if (currentPoint != null) {
            String text = String.format("(%.4f, %.4f)", currentPoint.getX(), currentPoint.getY());
            label.setText(text);
        }
    }
    
    /**
     * Set custom tooltip text
     */
    public void setTooltipText(String text) {
        label.setText(text);
    }
    
    /**
     * Set tooltip font
     */
    public void setTooltipFont(Font font) {
        label.setFont(font);
    }
    
    /**
     * Set tooltip colors
     */
    public void setTooltipColors(Color foreground, Color background) {
        label.setForeground(foreground);
        label.setBackground(background);
    }
    
    /**
     * Add mouse listener to a component for automatic tooltip display
     */
    public void addMouseListenerToComponent(Component component) {
        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // This will be overridden by the specific implementation
                // that knows how to convert screen coordinates to math coordinates
            }
        });
        
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hideTooltip();
            }
        });
    }
    
    /**
     * Show tooltip with custom formatting
     */
    public void showTooltipFormatted(int screenX, int screenY, Point2D mathPoint, String format) {
        if (mathPoint == null) {
            hideTooltip();
            return;
        }
        
        currentPoint = mathPoint;
        String text = String.format(format, mathPoint.getX(), mathPoint.getY());
        setTooltipText(text);
        
        // Position tooltip
        int tooltipX = screenX + 10;
        int tooltipY = screenY - 10;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension tooltipSize = getPreferredSize();
        
        if (tooltipX + tooltipSize.width > screenSize.width) {
            tooltipX = screenX - tooltipSize.width - 10;
        }
        if (tooltipY - tooltipSize.height < 0) {
            tooltipY = screenY + tooltipSize.height + 10;
        }
        
        setLocation(tooltipX, tooltipY);
        
        if (!isVisible) {
            setVisible(true);
            isVisible = true;
        }
    }
    
    /**
     * Show tooltip with additional information
     */
    public void showTooltipWithInfo(int screenX, int screenY, Point2D mathPoint, String additionalInfo) {
        if (mathPoint == null) {
            hideTooltip();
            return;
        }
        
        currentPoint = mathPoint;
        String text = String.format("(%.4f, %.4f) %s", mathPoint.getX(), mathPoint.getY(), additionalInfo);
        setTooltipText(text);
        
        // Position tooltip
        int tooltipX = screenX + 10;
        int tooltipY = screenY - 10;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension tooltipSize = getPreferredSize();
        
        if (tooltipX + tooltipSize.width > screenSize.width) {
            tooltipX = screenX - tooltipSize.width - 10;
        }
        if (tooltipY - tooltipSize.height < 0) {
            tooltipY = screenY + tooltipSize.height + 10;
        }
        
        setLocation(tooltipX, tooltipY);
        
        if (!isVisible) {
            setVisible(true);
            isVisible = true;
        }
    }
    
    /**
     * Check if tooltip is currently visible
     */
    public boolean isTooltipVisible() {
        return isVisible;
    }
    
    /**
     * Get current tooltip point
     */
    public Point2D getCurrentPoint() {
        return currentPoint;
    }
} 
