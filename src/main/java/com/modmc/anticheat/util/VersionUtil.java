package com.modmc.anticheat.util;

import org.bukkit.Bukkit;

/**
 * Server version detection utilities.
 */
public final class VersionUtil {

    private static int majorVersion = -1;
    private static int minorVersion = -1;

    private VersionUtil() {}

    /**
     * Parse the server version on first call and cache it.
     */
    private static void parseVersion() {
        if (majorVersion != -1) return;

        String version = Bukkit.getBukkitVersion(); // e.g., "1.20.4-R0.1-SNAPSHOT"
        try {
            String[] parts = version.split("-")[0].split("\\.");
            majorVersion = Integer.parseInt(parts[1]); // 20
            minorVersion = parts.length > 2 ? Integer.parseInt(parts[2]) : 0; // 4
        } catch (Exception e) {
            majorVersion = 16;
            minorVersion = 5;
        }
    }

    /**
     * Get the major version number (e.g., 20 for 1.20.x).
     */
    public static int getMajorVersion() {
        parseVersion();
        return majorVersion;
    }

    /**
     * Get the minor version number (e.g., 4 for 1.20.4).
     */
    public static int getMinorVersion() {
        parseVersion();
        return minorVersion;
    }

    /**
     * Check if the server is running at least the specified version.
     * Example: isAtLeast(17, 0) returns true for 1.17+
     */
    public static boolean isAtLeast(int major, int minor) {
        parseVersion();
        if (majorVersion > major) return true;
        if (majorVersion == major) return minorVersion >= minor;
        return false;
    }

    /**
     * Check if the server is running at least the specified major version.
     */
    public static boolean isAtLeast(int major) {
        return isAtLeast(major, 0);
    }

    /**
     * Get a human-readable version string.
     */
    public static String getVersionString() {
        parseVersion();
        return "1." + majorVersion + "." + minorVersion;
    }
}
