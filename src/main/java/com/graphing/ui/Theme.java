package com.graphing.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public final class Theme {
    public static final Color BG = new Color(246, 247, 251);
    public static final Color SURFACE = new Color(255, 255, 255);
    public static final Color SURFACE_ALT = new Color(249, 250, 252);
    public static final Color BORDER = new Color(225, 229, 236);
    public static final Color ACCENT = new Color(59, 130, 246);
    public static final Color ACCENT_DARK = new Color(37, 99, 235);
    public static final Color ACCENT_SOFT = new Color(219, 234, 254);
    public static final Color TEXT = new Color(28, 30, 34);
    public static final Color MUTED_TEXT = new Color(99, 105, 116);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color DANGER_SOFT = new Color(254, 226, 226);

    private static Font baseFont;

    private Theme() {}

    public static void apply() {
        Font uiFont = getUiFont();
        UIManager.put("defaultFont", new FontUIResource(uiFont));

        UIManager.put("Panel.background", BG);
        UIManager.put("Separator.foreground", BORDER);
        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusedBorderColor", ACCENT);

        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.hoverBackground", ACCENT_SOFT);
        UIManager.put("Button.pressedBackground", ACCENT_SOFT.darker());

        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextArea.foreground", TEXT);

        UIManager.put("List.background", SURFACE);
        UIManager.put("List.selectionBackground", ACCENT_SOFT);
        UIManager.put("List.selectionForeground", TEXT);

        UIManager.put("MenuBar.background", SURFACE);
        UIManager.put("MenuBar.borderColor", BORDER);
        UIManager.put("Menu.foreground", TEXT);
        UIManager.put("MenuItem.foreground", TEXT);
    }

    public static Font getUiFont() {
        if (baseFont != null) {
            return baseFont;
        }
        String[] candidates = {
            "SF Pro Text",
            "SF Pro Display",
            "Segoe UI Variable",
            "Segoe UI",
            "Inter",
            "Roboto",
            "Helvetica Neue",
            "Helvetica",
            "Arial"
        };
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String candidate : candidates) {
            for (String name : available) {
                if (name.equalsIgnoreCase(candidate)) {
                    baseFont = new Font(name, Font.PLAIN, 13);
                    return baseFont;
                }
            }
        }
        baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        return baseFont;
    }

    public static Font font(int style, int size) {
        return getUiFont().deriveFont(style, (float) size);
    }

    public static Font font(int size) {
        return getUiFont().deriveFont(Font.PLAIN, (float) size);
    }
}
