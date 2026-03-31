package com.graphing.io;

import com.graphing.math.Function;
import com.graphing.math.parser.ExpressionEvaluator;
import com.graphing.graph.GraphSettings;
import com.graphing.math.IntersectionPoint;
import com.graphing.math.Point2D;

import java.awt.Color;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Exports current graph as SVG vector image
 */
public class SVGExporter {
    
    /**
     * Export graph as SVG to a file
     */
    public static boolean exportToSVG(List<Function> functions, GraphSettings settings, 
                                    int width, int height, String filename,
                                    List<IntersectionPoint> intersections,
                                    List<Point2D> maxima, List<Point2D> minima,
                                    Function areaFunction, Double areaStart, Double areaEnd, Color areaColor) {
        try {
            String svg = generateSVG(functions, settings, width, height, intersections, maxima, minima, areaFunction, areaStart, areaEnd, areaColor);
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(svg);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting SVG: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate SVG content for the graph
     */
    public static String generateSVG(List<Function> functions, GraphSettings settings, 
                                   int width, int height,
                                   List<IntersectionPoint> intersections,
                                   List<Point2D> maxima, List<Point2D> minima,
                                   Function areaFunction, Double areaStart, Double areaEnd, Color areaColor) {
        StringBuilder svg = new StringBuilder();
        
        // SVG header
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height)
           .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        
        // Background
        String bgColor = colorToHex(settings.getBackgroundColor());
        svg.append("  <rect width=\"").append(width).append("\" height=\"").append(height)
           .append("\" fill=\"").append(bgColor).append("\"/>\n");
        
        // Grid
        if (settings.isShowGrid()) {
            svg.append(generateGridSVG(settings, width, height));
        }
        
        // Axes (thinner)
        if (settings.isShowAxes()) {
            svg.append(generateAxesSVG(settings, width, height, 1));
        }
        
        // Area under curve (if specified)
        if (areaFunction != null && areaStart != null && areaEnd != null && areaColor != null) {
            svg.append(generateAreaUnderCurveSVG(areaFunction, settings, width, height, areaStart, areaEnd, areaColor));
        }
        
        // Functions (thinner)
        for (Function function : functions) {
            if (function.isVisible()) {
                svg.append(generateFunctionSVG(function, settings, width, height, 2));
            }
        }
        
        // Draw intersection points
        if (intersections != null) {
            for (IntersectionPoint ip : intersections) {
                svg.append(drawSVGPoint(ip.getX(), ip.getY(), 3, colorToHex(ip.getColor()), width, height, settings));
            }
        }
        // Draw local maxima
        if (maxima != null) {
            for (Point2D p : maxima) {
                svg.append(drawSVGPoint(p.getX(), p.getY(), 3, "#000000", width, height, settings));
            }
        }
        // Draw local minima
        if (minima != null) {
            for (Point2D p : minima) {
                svg.append(drawSVGPoint(p.getX(), p.getY(), 3, "#000000", width, height, settings));
            }
        }
        
        // SVG footer
        svg.append("</svg>");
        
        return svg.toString();
    }
    
    /**
     * Generate SVG for the grid
     */
    private static String generateGridSVG(GraphSettings settings, int width, int height) {
        StringBuilder grid = new StringBuilder();
        String gridColor = colorToHex(settings.getGridColor());
        
        double minX = settings.getMinX();
        double maxX = settings.getMaxX();
        double minY = settings.getMinY();
        double maxY = settings.getMaxY();
        double gridSpacing = settings.getGridSpacing();
        
        // Vertical grid lines
        double startX = Math.ceil(minX / gridSpacing) * gridSpacing;
        for (double x = startX; x <= maxX; x += gridSpacing) {
            int screenX = (int) ((x - minX) / (maxX - minX) * width);
            grid.append("  <line x1=\"").append(screenX).append("\" y1=\"0\"")
                .append(" x2=\"").append(screenX).append("\" y2=\"").append(height).append("\"")
                .append(" stroke=\"").append(gridColor).append("\" stroke-width=\"1\"/>\n");
        }
        
        // Horizontal grid lines
        double startY = Math.ceil(minY / gridSpacing) * gridSpacing;
        for (double y = startY; y <= maxY; y += gridSpacing) {
            int screenY = height - (int) ((y - minY) / (maxY - minY) * height);
            grid.append("  <line x1=\"0\" y1=\"").append(screenY).append("\"")
                .append(" x2=\"").append(width).append("\" y2=\"").append(screenY).append("\"")
                .append(" stroke=\"").append(gridColor).append("\" stroke-width=\"1\"/>\n");
        }
        
        return grid.toString();
    }
    
    /**
     * Generate SVG for the coordinate axes
     */
    private static String generateAxesSVG(GraphSettings settings, int width, int height, int thickness) {
        StringBuilder axes = new StringBuilder();
        String axesColor = colorToHex(settings.getAxesColor());
        double minX = settings.getMinX();
        double maxX = settings.getMaxX();
        double minY = settings.getMinY();
        double maxY = settings.getMaxY();
        double gridSpacing = settings.getGridSpacing();

        // X-axis (y = 0)
        if (minY <= 0 && maxY >= 0) {
            int yAxis = height - (int) ((0 - minY) / (maxY - minY) * height);
            axes.append("  <line x1=\"0\" y1=\"").append(yAxis).append("\" x2=\"").append(width).append("\" y2=\"").append(yAxis).append("\" stroke=\"").append(axesColor).append("\" stroke-width=\"").append(thickness).append("\"/>");
            axes.append("\n");
            // Arrow at positive end
            axes.append("  <polygon points=\"")
                .append(width - 10).append(",").append(yAxis - 5).append(" ")
                .append(width).append(",").append(yAxis).append(" ")
                .append(width - 10).append(",").append(yAxis + 5)
                .append("\" fill=\"").append(axesColor).append("\"/>");
            axes.append("\n");
            // Arrow at negative end
            axes.append("  <polygon points=\"")
                .append(10).append(",").append(yAxis - 5).append(" ")
                .append(0).append(",").append(yAxis).append(" ")
                .append(10).append(",").append(yAxis + 5)
                .append("\" fill=\"").append(axesColor).append("\"/>");
            axes.append("\n");
        }
        // Y-axis (x = 0)
        if (minX <= 0 && maxX >= 0) {
            int xAxis = (int) ((0 - minX) / (maxX - minX) * width);
            axes.append("  <line x1=\"").append(xAxis).append("\" y1=\"0\" x2=\"").append(xAxis).append("\" y2=\"").append(height).append("\" stroke=\"").append(axesColor).append("\" stroke-width=\"").append(thickness).append("\"/>");
            axes.append("\n");
            // Arrow at positive end
            axes.append("  <polygon points=\"")
                .append(xAxis - 5).append(",10 ")
                .append(xAxis).append(",0 ")
                .append(xAxis + 5).append(",10")
                .append("\" fill=\"").append(axesColor).append("\"/>");
            axes.append("\n");
            // Arrow at negative end
            axes.append("  <polygon points=\"")
                .append(xAxis - 5).append(",").append(height - 10).append(" ")
                .append(xAxis).append(",").append(height).append(" ")
                .append(xAxis + 5).append(",").append(height - 10)
                .append("\" fill=\"").append(axesColor).append("\"/>");
            axes.append("\n");
        }
        axes.append(generateAxisLabelsSVG(settings, width, height));
        return axes.toString();
    }
    
    /**
     * Generate SVG for axis labels
     */
    private static String generateAxisLabelsSVG(GraphSettings settings, int width, int height) {
        StringBuilder labels = new StringBuilder();
        String textColor = colorToHex(settings.getAxesColor());
        double minX = settings.getMinX();
        double maxX = settings.getMaxX();
        double minY = settings.getMinY();
        double maxY = settings.getMaxY();
        double gridSpacing = settings.getGridSpacing();
        // Find axis positions
        int xAxis = (minY <= 0 && maxY >= 0) ? (height - (int) ((0 - minY) / (maxY - minY) * height)) : -1;
        int yAxis = (minX <= 0 && maxX >= 0) ? ((int) ((0 - minX) / (maxX - minX) * width)) : -1;
        // X-axis labels (only if x-axis is visible)
        if (xAxis != -1) {
            double startX = Math.ceil(minX / gridSpacing) * gridSpacing;
            for (double x = startX; x <= maxX; x += gridSpacing) {
                if (Math.abs(x) < 1e-10) x = 0; // Handle zero
                int screenX = (int) ((x - minX) / (maxX - minX) * width);
                String label = formatNumber(x);
                labels.append("  <text x=\"").append(screenX).append("\" y=\"").append(xAxis + 18).append("\" text-anchor=\"middle\" font-family=\"Segoe UI\" font-size=\"12\" fill=\"").append(textColor).append("\">").append(escapeXml(label)).append("</text>\n");
            }
        }
        // Y-axis labels (only if y-axis is visible)
        if (yAxis != -1) {
            double startY = Math.ceil(minY / gridSpacing) * gridSpacing;
            for (double y = startY; y <= maxY; y += gridSpacing) {
                if (Math.abs(y) < 1e-10) y = 0; // Handle zero
                int screenY = height - (int) ((y - minY) / (maxY - minY) * height);
                String label = formatNumber(y);
                labels.append("  <text x=\"").append(yAxis - 8).append("\" y=\"").append(screenY + 4).append("\" text-anchor=\"end\" font-family=\"Segoe UI\" font-size=\"12\" fill=\"").append(textColor).append("\">").append(escapeXml(label)).append("</text>\n");
            }
        }
        return labels.toString();
    }
    
    /**
     * Format number for display
     */
    private static String formatNumber(double value) {
        if (Math.abs(value) < 1e-10) return "0";
        if (Math.abs(value - Math.round(value)) < 1e-10) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format("%.1f", value);
    }
    
    /**
     * Generate SVG for a function
     */
    private static String generateFunctionSVG(Function function, GraphSettings settings, int width, int height, int thickness) {
        StringBuilder functionSVG = new StringBuilder();
        String functionColor = colorToHex(function.getColor());
        double minX = settings.getMinX();
        double maxX = settings.getMaxX();
        double minY = settings.getMinY();
        double maxY = settings.getMaxY();
        // Skip y=f(x) plotting for IMPLICIT and INEQUALITY types
        if (function.getType() == com.graphing.math.FunctionType.IMPLICIT || function.getType() == com.graphing.math.FunctionType.INEQUALITY) {
            // Marching squares boundary for implicit/inequality
            int gridSize = 600;
            double dx = (maxX - minX) / (gridSize - 1);
            double dy = (maxY - minY) / (gridSize - 1);
            double[][] values = new double[gridSize][gridSize];
            for (int ix = 0; ix < gridSize; ix++) {
                double x = minX + ix * dx;
                for (int iy = 0; iy < gridSize; iy++) {
                    double y = minY + iy * dy;
                    try {
                        java.util.Map<String, Double> vars = new java.util.HashMap<>();
                        vars.put("x", x);
                        vars.put("y", y);
                        double left = function.getLeftExpression().evaluate(vars);
                        double right = function.getRightExpression().evaluate(vars);
                        values[ix][iy] = left - right;
                    } catch (Exception e) {
                        values[ix][iy] = Double.NaN;
                    }
                }
            }
            java.util.List<java.util.List<java.awt.geom.Point2D>> contours = computeMarchingSquaresContoursSVG(values, minX, minY, dx, dy, gridSize, 0.0, width, height, minX, maxX, minY, maxY);
            for (java.util.List<java.awt.geom.Point2D> polyline : contours) {
                if (polyline.size() < 2) continue;
                StringBuilder path = new StringBuilder();
                path.append("M ").append(polyline.get(0).getX()).append(" ").append(polyline.get(0).getY());
                for (int i = 1; i < polyline.size(); i++) {
                    path.append(" L ").append(polyline.get(i).getX()).append(" ").append(polyline.get(i).getY());
                }
                functionSVG.append("  <path d=\"").append(path.toString()).append("\" stroke=\"").append(functionColor).append("\" stroke-width=\"2.5\" fill=\"none\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>");
                functionSVG.append("\n");
            }
        } else {
            int numPoints = Math.max(3000, settings.getPlotPoints()); // Force high point count for smoothness
            double step = (maxX - minX) / (numPoints - 1);
            StringBuilder path = new StringBuilder();
            boolean firstPoint = true;
            double lastScreenX = 0, lastScreenY = 0;
            double lastValidX = minX, lastValidY = 0;
            Double prevY = null;
            Double prevScreenX = null, prevScreenY = null;
            for (int i = 0; i < numPoints; i++) {
                double x = minX + i * step;
                try {
                    double y;
                    if (function.getParsedExpression() != null) {
                        y = function.getParsedExpression().evaluate(java.util.Collections.singletonMap("x", x));
                    } else {
                        y = ExpressionEvaluator.evaluate(function.getExpression(), x);
                    }
                    if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                        double screenX = ((x - minX) / (maxX - minX)) * width;
                        double screenY = height - ((y - minY) / (maxY - minY)) * height;
                        if (firstPoint) {
                            path.append("M ").append(screenX).append(" ").append(screenY);
                            firstPoint = false;
                        } else {
                            // Skip vertical lines for discontinuities (large y-jump)
                            if (prevY != null && Math.abs(y - prevY) > 0.9) {
                                path.append(" M ").append(screenX).append(" ").append(screenY);
                            } else {
                                path.append(" L ").append(screenX).append(" ").append(screenY);
                            }
                        }
                        prevY = y;
                        prevScreenX = screenX;
                        prevScreenY = screenY;
                        lastScreenX = screenX;
                        lastScreenY = screenY;
                        lastValidX = x;
                        lastValidY = y;
                    }
                } catch (Exception e) {
                    // Skip points where evaluation fails
                }
            }
            if (path.length() > 0) {
                functionSVG.append("  <path d=\"").append(path.toString()).append("\" stroke=\"").append(functionColor).append("\" stroke-width=\"").append(thickness).append("\" fill=\"none\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>");
                functionSVG.append("\n");
                // Robust function label placement
                String label = function.getExpression();
                String name = function.getName();
                if (name != null && !name.isEmpty() && !name.matches("f\\d{1,5}")) {
                    label = name;
                }
                // Font size and margin
                int fontSize = 14;
                double margin = 20;
                // Estimate label width: ~0.6 * fontSize * label.length()
                double estLabelWidth = 0.6 * fontSize * label.length();
                double labelX = width - margin - estLabelWidth;
                if (labelX < margin) labelX = margin; // Clamp to left margin
                double labelY = height / 2.0; // fallback: vertical center
                double labelMathX = maxX - (maxX - minX) * 0.03;
                try {
                    double y;
                    if (function.getParsedExpression() != null) {
                        y = function.getParsedExpression().evaluate(java.util.Collections.singletonMap("x", labelMathX));
                    } else {
                        y = ExpressionEvaluator.evaluate(function.getExpression(), labelMathX);
                    }
                    if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                        labelY = height - ((y - minY) / (maxY - minY)) * height;
                        if (labelY < margin + fontSize) labelY = margin + fontSize;
                        if (labelY > height - margin) labelY = height - margin;
                    }
                } catch (Exception e) {
                    // fallback: keep labelY at center
                }
                // Draw white outline for contrast
                functionSVG.append("  <text x=\"").append(labelX).append("\" y=\"").append(labelY).append("\" fill=\"white\" font-size=\"").append(fontSize).append("\" font-family=\"Inter, Segoe UI, Arial, sans-serif\" font-weight=\"bold\" stroke=\"white\" stroke-width=\"4\" paint-order=\"stroke\" >");
                functionSVG.append(escapeXml(label));
                functionSVG.append("</text>\n");
                // Draw colored label on top
                functionSVG.append("  <text x=\"").append(labelX).append("\" y=\"").append(labelY).append("\" fill=\"").append(functionColor).append("\" font-size=\"").append(fontSize).append("\" font-family=\"Inter, Segoe UI, Arial, sans-serif\" font-weight=\"bold\" >");
                functionSVG.append(escapeXml(label));
                functionSVG.append("</text>\n");
            }
        }
        // Special handling for inequalities: fill region
        if (function.getType() == com.graphing.math.FunctionType.INEQUALITY && function.getLeftExpression() != null && function.getRightExpression() != null) {
            int gridSize = 300;
            // Use already defined minX, maxX, minY, maxY
            double dx = (maxX - minX) / (gridSize - 1);
            double dy = (maxY - minY) / (gridSize - 1);
            int fillAlpha = 80;
            String fillColor = colorToHex(function.getColor()) + String.format("%02x", fillAlpha);
            StringBuilder fillRects = new StringBuilder();
            for (int ix = 0; ix < gridSize; ix++) {
                double x = minX + ix * dx;
                boolean inRegion = false;
                int yStartPix = 0;
                for (int iy = 0; iy < gridSize; iy++) {
                    double y = minY + iy * dy;
                    try {
                        java.util.Map<String, Double> vars = new java.util.HashMap<>();
                        vars.put("x", x);
                        vars.put("y", y);
                        double left = function.getLeftExpression().evaluate(vars);
                        double right = function.getRightExpression().evaluate(vars);
                        boolean holds = false;
                        String op = function.getOperator();
                        switch (op) {
                            case ">": holds = left > right; break;
                            case "<": holds = left < right; break;
                            case ">=": holds = left >= right; break;
                            case "<=": holds = left <= right; break;
                        }
                        int screenX = (int)(((x - minX) / (maxX - minX)) * width);
                        int screenY = height - (int)(((y - minY) / (maxY - minY)) * height);
                        if (holds && !inRegion) {
                            inRegion = true;
                            yStartPix = screenY;
                        } else if (!holds && inRegion) {
                            inRegion = false;
                            int yEndPix = screenY;
                            int rectHeight = Math.abs(yEndPix - yStartPix);
                            fillRects.append(String.format("  <rect x=\"%d\" y=\"%d\" width=\"2\" height=\"%d\" fill=\"%s\" stroke=\"none\"/>", screenX, Math.min(yStartPix, yEndPix), rectHeight, fillColor));
                        }
                        if (iy == gridSize - 1 && inRegion) {
                            int yEndPix = screenY;
                            int rectHeight = Math.abs(yEndPix - yStartPix);
                            fillRects.append(String.format("  <rect x=\"%d\" y=\"%d\" width=\"2\" height=\"%d\" fill=\"%s\" stroke=\"none\"/>", screenX, Math.min(yStartPix, yEndPix), rectHeight, fillColor));
                        }
                    } catch (Exception e) {
                        // skip
                    }
                }
            }
            functionSVG.append(fillRects.toString());
        }
        return functionSVG.toString();
    }
    
    /**
     * Export graph with custom styling
     */
    public static boolean exportToSVGWithStyle(List<Function> functions, GraphSettings settings,
                                             int width, int height, String filename, 
                                             String customStyle) {
        try {
            String svg = generateSVGWithStyle(functions, settings, width, height, customStyle);
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(svg);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting styled SVG: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate SVG with custom CSS styling
     */
    public static String generateSVGWithStyle(List<Function> functions, GraphSettings settings,
                                            int width, int height, String customStyle) {
        StringBuilder svg = new StringBuilder();
        
        // SVG header with style
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height)
           .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        
        // Custom style
        if (customStyle != null && !customStyle.trim().isEmpty()) {
            svg.append("  <defs>\n");
            svg.append("    <style type=\"text/css\">\n");
            svg.append("      ").append(customStyle).append("\n");
            svg.append("    </style>\n");
            svg.append("  </defs>\n");
        }
        
        // Background
        String bgColor = colorToHex(settings.getBackgroundColor());
        svg.append("  <rect width=\"").append(width).append("\" height=\"").append(height)
           .append("\" fill=\"").append(bgColor).append("\"/>\n");
        
        // Grid
        if (settings.isShowGrid()) {
            svg.append(generateGridSVG(settings, width, height));
        }
        
        // Axes
        if (settings.isShowAxes()) {
            svg.append(generateAxesSVG(settings, width, height, 1));
        }
        
        // Functions
        for (Function function : functions) {
            if (function.isVisible()) {
                svg.append(generateFunctionSVG(function, settings, width, height, 2));
            }
        }
        
        // SVG footer
        svg.append("</svg>");
        
        return svg.toString();
    }
    
    /**
     * Convert Color to hex string
     */
    private static String colorToHex(Color color) {
        if (color == null) {
            return "#000000";
        }
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Escape XML special characters
     */
    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
    
    /**
     * Export graph as SVG with embedded metadata
     */
    public static boolean exportToSVGWithMetadata(List<Function> functions, GraphSettings settings,
                                                int width, int height, String filename,
                                                String title, String description) {
        try {
            String svg = generateSVGWithMetadata(functions, settings, width, height, title, description);
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                writer.write(svg);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting SVG with metadata: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate SVG with embedded metadata
     */
    public static String generateSVGWithMetadata(List<Function> functions, GraphSettings settings,
                                               int width, int height, String title, String description) {
        StringBuilder svg = new StringBuilder();
        
        // SVG header with metadata
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height)
           .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        
        // Metadata
        if (title != null && !title.trim().isEmpty()) {
            svg.append("  <title>").append(escapeXml(title)).append("</title>\n");
        }
        if (description != null && !description.trim().isEmpty()) {
            svg.append("  <desc>").append(escapeXml(description)).append("</desc>\n");
        }
        
        // Background
        String bgColor = colorToHex(settings.getBackgroundColor());
        svg.append("  <rect width=\"").append(width).append("\" height=\"").append(height)
           .append("\" fill=\"").append(bgColor).append("\"/>\n");
        
        // Grid
        if (settings.isShowGrid()) {
            svg.append(generateGridSVG(settings, width, height));
        }
        
        // Axes
        if (settings.isShowAxes()) {
            svg.append(generateAxesSVG(settings, width, height, 1));
        }
        
        // Functions
        for (Function function : functions) {
            if (function.isVisible()) {
                svg.append(generateFunctionSVG(function, settings, width, height, 2));
            }
        }
        
        // SVG footer
        svg.append("</svg>");
        
        return svg.toString();
    }

    // Helper to draw a point as a circle with black outline
    private static String drawSVGPoint(double mathX, double mathY, int radius, String fillColor, int width, int height, GraphSettings settings) {
        double minX = settings.getMinX();
        double maxX = settings.getMaxX();
        double minY = settings.getMinY();
        double maxY = settings.getMaxY();
        double screenX = ((mathX - minX) / (maxX - minX)) * width;
        double screenY = height - ((mathY - minY) / (maxY - minY)) * height;
        return String.format("  <circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" fill=\"%s\" stroke=\"#000000\" stroke-width=\"1\"/>\n", screenX, screenY, radius, fillColor);
    }

    // 4. Add generateAreaUnderCurveSVG
    private static String generateAreaUnderCurveSVG(Function function, GraphSettings settings, int width, int height, double areaStart, double areaEnd, Color areaColor) {
        StringBuilder areaSVG = new StringBuilder();
        String fillColor = colorToHex(areaColor);
        double minX = settings.getMinX();
        double maxX = settings.getMaxX();
        double minY = settings.getMinY();
        double maxY = settings.getMaxY();
        int numPoints = 100;
        double step = (areaEnd - areaStart) / (numPoints - 1);
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            double x = areaStart + i * step;
            try {
                double y;
                if (function.getParsedExpression() != null) {
                    y = function.getParsedExpression().evaluate(java.util.Collections.singletonMap("x", x));
                } else {
                    y = ExpressionEvaluator.evaluate(function.getExpression(), x);
                }
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    xs.add(x);
                    ys.add(y);
                } else {
                    xs.add(x);
                    ys.add(null);
                }
            } catch (Exception e) {
                xs.add(x);
                ys.add(null);
            }
        }
        // Find first and last valid points
        int firstValid = -1, lastValid = -1;
        for (int i = 0; i < ys.size(); i++) {
            if (ys.get(i) != null) { firstValid = i; break; }
        }
        for (int i = ys.size() - 1; i >= 0; i--) {
            if (ys.get(i) != null) { lastValid = i; break; }
        }
        if (firstValid == -1 || lastValid == -1 || firstValid == lastValid) {
            // No valid area to shade
            return "";
        }
        StringBuilder path = new StringBuilder();
        // Move to first valid point
        double fx = xs.get(firstValid);
        double fy = ys.get(firstValid);
        double screenX = ((fx - minX) / (maxX - minX)) * width;
        double screenY = height - ((fy - minY) / (maxY - minY)) * height;
        path.append("M ").append(screenX).append(" ").append(screenY);
        // Draw line through all valid points
        for (int i = firstValid + 1; i <= lastValid; i++) {
            if (ys.get(i) != null) {
                double px = xs.get(i);
                double py = ys.get(i);
                double pxScreen = ((px - minX) / (maxX - minX)) * width;
                double pyScreen = height - ((py - minY) / (maxY - minY)) * height;
                path.append(" L ").append(pxScreen).append(" ").append(pyScreen);
            }
        }
        // Close down to x-axis at right bound
        double rightX = xs.get(lastValid);
        double rightScreenX = ((rightX - minX) / (maxX - minX)) * width;
        double xAxisY = height - ((0 - minY) / (maxY - minY)) * height;
        path.append(" L ").append(rightScreenX).append(" ").append(xAxisY);
        // Line along x-axis to left bound
        double leftX = xs.get(firstValid);
        double leftScreenX = ((leftX - minX) / (maxX - minX)) * width;
        path.append(" L ").append(leftScreenX).append(" ").append(xAxisY);
        path.append(" Z");
        areaSVG.append("  <path d=\"").append(path.toString()).append("\" fill=\"").append(fillColor).append("\" fill-opacity=\"0.25\" style=\"fill-opacity:0.25;\" stroke=\"none\"/>");
        areaSVG.append("\n");
        return areaSVG.toString();
    }

    // Helper class for spatial hashing of points (copied from CurveRenderer)
    private static class PointKey {
        final double x, y;
        private static final double TOL = 1e-2;
        PointKey(java.awt.geom.Point2D pt) {
            this.x = Math.round(pt.getX() / TOL) * TOL;
            this.y = Math.round(pt.getY() / TOL) * TOL;
        }
        @Override public boolean equals(Object o) {
            if (!(o instanceof PointKey)) return false;
            PointKey p = (PointKey)o;
            return Math.abs(x - p.x) < TOL && Math.abs(y - p.y) < TOL;
        }
        @Override public int hashCode() {
            return Double.hashCode(x) * 31 + Double.hashCode(y);
        }
    }

    // Marching squares for SVG: returns a list of polylines (list of points)
    private static java.util.List<java.util.List<java.awt.geom.Point2D>> computeMarchingSquaresContoursSVG(double[][] values, double minX, double minY, double dx, double dy, int gridSize, double level, int width, int height, double plotMinX, double plotMaxX, double plotMinY, double plotMaxY) {
        class Segment {
            java.awt.geom.Point2D a, b;
            Segment(java.awt.geom.Point2D a, java.awt.geom.Point2D b) { this.a = a; this.b = b; }
        }
        java.util.Map<PointKey, java.util.List<Segment>> startMap = new java.util.HashMap<>();
        java.util.Map<PointKey, java.util.List<Segment>> endMap = new java.util.HashMap<>();
        java.util.List<Segment> allSegments = new java.util.ArrayList<>();
        for (int ix = 0; ix < gridSize - 1; ix++) {
            for (int iy = 0; iy < gridSize - 1; iy++) {
                double v00 = values[ix][iy];
                double v10 = values[ix+1][iy];
                double v01 = values[ix][iy+1];
                double v11 = values[ix+1][iy+1];
                int state = 0;
                if (v00 > level) state |= 1;
                if (v10 > level) state |= 2;
                if (v11 > level) state |= 4;
                if (v01 > level) state |= 8;
                if (state == 0 || state == 15) continue;
                double x0 = minX + ix * dx;
                double y0 = minY + iy * dy;
                double x1 = x0 + dx;
                double y1 = y0 + dy;
                java.awt.geom.Point2D[] pts = new java.awt.geom.Point2D[4];
                pts[0] = mathToSVGScreenPoint(interp(x0, y0, x1, y0, v00, v10, level), width, height, plotMinX, plotMaxX, plotMinY, plotMaxY);
                pts[1] = mathToSVGScreenPoint(interp(x1, y0, x1, y1, v10, v11, level), width, height, plotMinX, plotMaxX, plotMinY, plotMaxY);
                pts[2] = mathToSVGScreenPoint(interp(x1, y1, x0, y1, v11, v01, level), width, height, plotMinX, plotMaxX, plotMinY, plotMaxY);
                pts[3] = mathToSVGScreenPoint(interp(x0, y1, x0, y0, v01, v00, level), width, height, plotMinX, plotMaxX, plotMinY, plotMaxY);
                java.util.List<int[]> segCases = new java.util.ArrayList<>();
                switch (state) {
                    case 1: case 14: segCases.add(new int[]{0,3}); break;
                    case 2: case 13: segCases.add(new int[]{0,1}); break;
                    case 3: case 12: segCases.add(new int[]{1,3}); break;
                    case 4: case 11: segCases.add(new int[]{1,2}); break;
                    case 5: segCases.add(new int[]{0,1}); segCases.add(new int[]{2,3}); break;
                    case 6: case 9: segCases.add(new int[]{0,2}); break;
                    case 7: case 8: segCases.add(new int[]{2,3}); break;
                    case 10: segCases.add(new int[]{0,1}); segCases.add(new int[]{2,3}); break;
                }
                for (int[] seg : segCases) {
                    Segment s = new Segment(pts[seg[0]], pts[seg[1]]);
                    allSegments.add(s);
                    startMap.computeIfAbsent(new PointKey(s.a), k -> new java.util.ArrayList<>()).add(s);
                    endMap.computeIfAbsent(new PointKey(s.b), k -> new java.util.ArrayList<>()).add(s);
                }
            }
        }
        java.util.Set<Segment> used = new java.util.HashSet<>();
        java.util.List<java.util.List<java.awt.geom.Point2D>> polylines = new java.util.ArrayList<>();
        for (Segment seg : allSegments) {
            if (used.contains(seg)) continue;
            java.util.List<java.awt.geom.Point2D> poly = new java.util.ArrayList<>();
            poly.add(seg.a);
            poly.add(seg.b);
            used.add(seg);
            java.awt.geom.Point2D curr = seg.b;
            while (true) {
                java.util.List<Segment> nexts = startMap.get(new PointKey(curr));
                Segment next = null;
                if (nexts != null) {
                    for (Segment cand : nexts) {
                        if (!used.contains(cand) && (cand.a.distance(curr) < 1e-1)) {
                            next = cand; break;
                        }
                    }
                }
                if (next == null) break;
                poly.add(next.b);
                used.add(next);
                curr = next.b;
            }
            curr = seg.a;
            while (true) {
                java.util.List<Segment> prevs = endMap.get(new PointKey(curr));
                Segment prev = null;
                if (prevs != null) {
                    for (Segment cand : prevs) {
                        if (!used.contains(cand) && (cand.b.distance(curr) < 1e-1)) {
                            prev = cand; break;
                        }
                    }
                }
                if (prev == null) break;
                poly.add(0, prev.a);
                used.add(prev);
                curr = prev.a;
            }
            if (poly.size() > 1) polylines.add(poly);
        }
        return polylines;
    }
    // Interpolation and screen conversion helpers for SVG
    private static java.awt.geom.Point2D interp(double x0, double y0, double x1, double y1, double v0, double v1, double level) {
        double t = (level - v0) / (v1 - v0 + 1e-12);
        return new java.awt.geom.Point2D.Double(x0 + t * (x1 - x0), y0 + t * (y1 - y0));
    }
    private static java.awt.geom.Point2D mathToSVGScreenPoint(java.awt.geom.Point2D mathPt, int width, int height, double minX, double maxX, double minY, double maxY) {
        double screenX = ((mathPt.getX() - minX) / (maxX - minX)) * width;
        double screenY = height - ((mathPt.getY() - minY) / (maxY - minY)) * height;
        return new java.awt.geom.Point2D.Double(screenX, screenY);
    }
} 
