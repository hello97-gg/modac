package com.modmc.anticheat.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Math utilities for anticheat calculations.
 * Covers distance, angle, and statistical operations.
 */
public final class MathUtil {

    private MathUtil() {}

    /**
     * Calculate horizontal distance between two locations (ignoring Y).
     */
    public static double horizontalDistance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Calculate 3D distance between two locations.
     */
    public static double distance3D(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculate horizontal distance squared (avoids sqrt for comparisons).
     */
    public static double horizontalDistanceSq(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    /**
     * Calculate the horizontal speed (blocks/tick) from movement delta.
     */
    public static double horizontalSpeed(double deltaX, double deltaZ) {
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    /**
     * Get the angle between two vectors in degrees.
     */
    public static double angleBetween(Vector a, Vector b) {
        double dot = a.dot(b);
        double lenA = a.length();
        double lenB = b.length();
        if (lenA == 0 || lenB == 0) return 0;
        double cos = dot / (lenA * lenB);
        cos = clamp(cos, -1.0, 1.0);
        return Math.toDegrees(Math.acos(cos));
    }

    /**
     * Calculate the yaw angle difference, wrapping around 360 degrees.
     */
    public static float yawDifference(float yaw1, float yaw2) {
        float diff = Math.abs(yaw1 - yaw2) % 360;
        return diff > 180 ? 360 - diff : diff;
    }

    /**
     * Calculate the standard deviation of a set of values.
     */
    public static double standardDeviation(double[] values) {
        if (values.length < 2) return 0;
        double mean = mean(values);
        double sumSqDiff = 0;
        for (double v : values) {
            double diff = v - mean;
            sumSqDiff += diff * diff;
        }
        return Math.sqrt(sumSqDiff / (values.length - 1));
    }

    /**
     * Calculate the mean of a set of values.
     */
    public static double mean(double[] values) {
        if (values.length == 0) return 0;
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp an int value between min and max.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Get the direction vector from yaw and pitch.
     */
    public static Vector directionFromRotation(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vector(x, y, z);
    }

    /**
     * Calculate GCD (Greatest Common Divisor) for sensitivity detection.
     */
    public static double gcd(double a, double b) {
        if (Math.abs(b) < 1e-6) return a;
        return gcd(b, a % b);
    }
}
