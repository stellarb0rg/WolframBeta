package com.graphing.io;

import com.graphing.math.Function;
import com.graphing.math.FunctionType;
import com.graphing.graph.GraphSettings;
import com.graphing.graph.Viewport;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Handles JSON serialization/deserialization for workspace data
 * Implements a simple JSON parser without external dependencies
 */
public class JSONSerializer {
    
    /**
     * Serialize workspace data to JSON string
     */
    public static String serializeWorkspace(List<Function> functions, GraphSettings settings, Viewport viewport) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        // Serialize functions
        json.append("  \"functions\": [\n");
        for (int i = 0; i < functions.size(); i++) {
            Function func = functions.get(i);
            json.append("    {\n");
            json.append("      \"name\": \"").append(escapeString(func.getName())).append("\",\n");
            json.append("      \"expression\": \"").append(escapeString(func.getExpression())).append("\",\n");
            json.append("      \"type\": \"");
            if (func.getType() != null) {
                json.append(func.getType().toString());
            } else {
                json.append("UNKNOWN");
            }
            json.append("\",\n");
            json.append("      \"color\": \"").append(colorToHex(func.getColor())).append("\",\n");
            json.append("      \"visible\": ").append(func.isVisible()).append("\n");
            json.append("    }");
            if (i < functions.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");
        
        // Serialize graph settings
        json.append("  \"graphSettings\": {\n");
        json.append("    \"minX\": ").append(settings.getMinX()).append(",\n");
        json.append("    \"maxX\": ").append(settings.getMaxX()).append(",\n");
        json.append("    \"minY\": ").append(settings.getMinY()).append(",\n");
        json.append("    \"maxY\": ").append(settings.getMaxY()).append(",\n");
        json.append("    \"showGrid\": ").append(settings.isShowGrid()).append(",\n");
        json.append("    \"showAxes\": ").append(settings.isShowAxes()).append(",\n");
        json.append("    \"gridSpacing\": ").append(settings.getGridSpacing()).append(",\n");
        json.append("    \"backgroundColor\": \"").append(colorToHex(settings.getBackgroundColor())).append("\",\n");
        json.append("    \"gridColor\": \"").append(colorToHex(settings.getGridColor())).append("\",\n");
        json.append("    \"axesColor\": \"").append(colorToHex(settings.getAxesColor())).append("\",\n");
        json.append("    \"showCoordinates\": ").append(settings.isShowCoordinates()).append(",\n");
        json.append("    \"showFunctionLabels\": ").append(settings.isShowFunctionLabels()).append(",\n");
        json.append("    \"antiAliasing\": ").append(settings.isAntiAliasing()).append("\n");
        json.append("  },\n");
        
        // Serialize viewport
        json.append("  \"viewport\": {\n");
        json.append("    \"centerX\": ").append(viewport.getCenterX()).append(",\n");
        json.append("    \"centerY\": ").append(viewport.getCenterY()).append(",\n");
        json.append("    \"zoomLevel\": ").append(viewport.getZoomLevel()).append(",\n");
        json.append("    \"screenWidth\": ").append(viewport.getScreenWidth()).append(",\n");
        json.append("    \"screenHeight\": ").append(viewport.getScreenHeight()).append("\n");
        json.append("  }\n");
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Deserialize workspace data from JSON string
     */
    public static WorkspaceData deserializeWorkspace(String json) {
        try {
            // Parse functions
            List<Function> functions = new ArrayList<>();
            String functionsSection = extractSection(json, "functions");
            if (functionsSection != null) {
                String[] functionBlocks = splitArray(functionsSection);
                for (String block : functionBlocks) {
                    if (block.trim().isEmpty()) continue;
                    Function func = parseFunction(block);
                    if (func != null) {
                        functions.add(func);
                    }
                }
            }
            
            // Parse graph settings
            GraphSettings settings = new GraphSettings();
            String settingsSection = extractSection(json, "graphSettings");
            if (settingsSection != null) {
                parseGraphSettings(settings, settingsSection);
            }
            
            // Parse viewport
            Viewport viewport = new Viewport(800, 600); // Default size
            String viewportSection = extractSection(json, "viewport");
            if (viewportSection != null) {
                parseViewport(viewport, viewportSection);
            }
            
            return new WorkspaceData(functions, settings, viewport);
        } catch (Exception e) {
            System.err.println("Error deserializing workspace: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse a function from JSON block
     */
    private static Function parseFunction(String jsonBlock) {
        try {
            String name = extractStringValue(jsonBlock, "name");
            String expression = extractStringValue(jsonBlock, "expression");
            String typeStr = extractStringValue(jsonBlock, "type");
            String colorHex = extractStringValue(jsonBlock, "color");
            boolean visible = extractBooleanValue(jsonBlock, "visible");
            
            FunctionType type = FunctionType.valueOf(typeStr);
            Color color = hexToColor(colorHex);
            
            Function func = new Function(expression, color, type, name);
            func.setVisible(visible);
            
            return func;
        } catch (Exception e) {
            System.err.println("Error parsing function: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse graph settings from JSON section
     */
    private static void parseGraphSettings(GraphSettings settings, String jsonSection) {
        try {
            settings.setMinX(extractDoubleValue(jsonSection, "minX"));
            settings.setMaxX(extractDoubleValue(jsonSection, "maxX"));
            settings.setMinY(extractDoubleValue(jsonSection, "minY"));
            settings.setMaxY(extractDoubleValue(jsonSection, "maxY"));
            settings.setShowGrid(extractBooleanValue(jsonSection, "showGrid"));
            settings.setShowAxes(extractBooleanValue(jsonSection, "showAxes"));
            settings.setGridSpacing(extractDoubleValue(jsonSection, "gridSpacing"));
            settings.setBackgroundColor(hexToColor(extractStringValue(jsonSection, "backgroundColor")));
            settings.setGridColor(hexToColor(extractStringValue(jsonSection, "gridColor")));
            settings.setAxesColor(hexToColor(extractStringValue(jsonSection, "axesColor")));
            settings.setShowCoordinates(extractBooleanValue(jsonSection, "showCoordinates"));
            settings.setShowFunctionLabels(extractBooleanValue(jsonSection, "showFunctionLabels"));
            settings.setAntiAliasing(extractBooleanValue(jsonSection, "antiAliasing"));
        } catch (Exception e) {
            System.err.println("Error parsing graph settings: " + e.getMessage());
        }
    }
    
    /**
     * Parse viewport from JSON section
     */
    private static void parseViewport(Viewport viewport, String jsonSection) {
        try {
            viewport.setCenterX(extractDoubleValue(jsonSection, "centerX"));
            viewport.setCenterY(extractDoubleValue(jsonSection, "centerY"));
            viewport.setZoomLevel(extractDoubleValue(jsonSection, "zoomLevel"));
            int width = (int) extractDoubleValue(jsonSection, "screenWidth");
            int height = (int) extractDoubleValue(jsonSection, "screenHeight");
            viewport.setScreenDimensions(width, height);
        } catch (Exception e) {
            System.err.println("Error parsing viewport: " + e.getMessage());
        }
    }
    
    // Helper methods for JSON parsing
    
    private static String extractSection(String json, String sectionName) {
        String pattern = "\"" + sectionName + "\"\\s*:\\s*";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        int braceCount = 0;
        boolean inArray = false;
        int end = start;
        
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                inArray = true;
                braceCount++;
            } else if (c == ']' && inArray) {
                braceCount--;
                if (braceCount == 0) {
                    end = i + 1;
                    break;
                }
            } else if (c == '{' && !inArray) {
                braceCount++;
            } else if (c == '}' && !inArray) {
                braceCount--;
                if (braceCount == 0) {
                    end = i + 1;
                    break;
                }
            }
        }
        
        return json.substring(start, end);
    }
    
    private static String[] splitArray(String arrayJson) {
        List<String> blocks = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        boolean inString = false;
        
        for (int i = 0; i < arrayJson.length(); i++) {
            char c = arrayJson.charAt(i);
            if (c == '"' && (i == 0 || arrayJson.charAt(i-1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') {
                    if (braceCount == 0) start = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        blocks.add(arrayJson.substring(start, i + 1));
                    }
                }
            }
        }
        
        return blocks.toArray(new String[0]);
    }
    
    private static String extractStringValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        
        return unescapeString(json.substring(start, end));
    }
    
    private static double extractDoubleValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*";
        int start = json.indexOf(pattern);
        if (start == -1) return 0.0;
        
        start += pattern.length();
        int end = start;
        while (end < json.length() && 
               (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || 
                json.charAt(end) == '-' || json.charAt(end) == 'e' || json.charAt(end) == 'E')) {
            end++;
        }
        
        return Double.parseDouble(json.substring(start, end));
    }
    
    private static boolean extractBooleanValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*";
        int start = json.indexOf(pattern);
        if (start == -1) return false;
        
        start += pattern.length();
        String value = json.substring(start, start + 4);
        return value.equals("true");
    }
    
    private static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    private static String unescapeString(String str) {
        return str.replace("\\\"", "\"")
                 .replace("\\\\", "\\")
                 .replace("\\n", "\n")
                 .replace("\\r", "\r")
                 .replace("\\t", "\t");
    }
    
    private static String colorToHex(Color color) {
                        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    private static Color hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return Color.BLACK;
        }
        
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        
        try {
            int rgb = Integer.parseInt(hex, 16);
            return new Color(rgb);
        } catch (NumberFormatException e) {
            System.err.println("Invalid color hex: " + hex);
            return Color.BLACK;
        }
    }
    
    /**
     * Data class to hold workspace information
     */
    public static class WorkspaceData {
        private final List<Function> functions;
        private final GraphSettings settings;
        private final Viewport viewport;
        
        public WorkspaceData(List<Function> functions, GraphSettings settings, Viewport viewport) {
            this.functions = functions;
            this.settings = settings;
            this.viewport = viewport;
        }
        
        public List<Function> getFunctions() { return functions; }
        public GraphSettings getSettings() { return settings; }
        public Viewport getViewport() { return viewport; }
    }
} 