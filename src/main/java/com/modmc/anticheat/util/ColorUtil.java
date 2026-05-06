package com.modmc.anticheat.util;

import org.bukkit.ChatColor;

/**
 * Color and text formatting utilities.
 */
public final class ColorUtil {

    private ColorUtil() {}

    /**
     * Translate color codes using '&' as the color char.
     */
    public static String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Strip all color codes from a string.
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(colorize(text));
    }

    /**
     * Format a violation level with color based on severity.
     */
    public static String formatVL(int vl, int maxVl) {
        double ratio = (double) vl / maxVl;
        String color;
        if (ratio >= 0.75) {
            color = "&4";
        } else if (ratio >= 0.5) {
            color = "&c";
        } else if (ratio >= 0.25) {
            color = "&e";
        } else {
            color = "&a";
        }
        return colorize(color + vl);
    }

    /**
     * Create a progress bar visualization.
     */
    public static String progressBar(int current, int max, int barLength) {
        double ratio = Math.min(1.0, (double) current / max);
        int filled = (int) (ratio * barLength);
        int empty = barLength - filled;

        StringBuilder bar = new StringBuilder();
        bar.append("&c");
        for (int i = 0; i < filled; i++) bar.append("┃");
        bar.append("&7");
        for (int i = 0; i < empty; i++) bar.append("┃");

        return colorize(bar.toString());
    }
}
