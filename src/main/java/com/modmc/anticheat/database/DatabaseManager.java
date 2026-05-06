package com.modmc.anticheat.database;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.CheckType;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * SQLite database manager for violation logging.
 */
public class DatabaseManager {

    private final ModMCAntiCheat plugin;
    private Connection connection;
    private boolean enabled;

    public DatabaseManager(ModMCAntiCheat plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the database connection and create tables.
     */
    public void init() {
        this.enabled = plugin.getConfig().getBoolean("database.enabled", true);
        if (!enabled) {
            plugin.getLogger().info("Database logging is disabled.");
            return;
        }

        String fileName = plugin.getConfig().getString("database.file", "violations.db");
        File dbFile = new File(plugin.getDataFolder(), fileName);

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            // Create tables
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS violations (" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  uuid TEXT NOT NULL," +
                    "  player_name TEXT NOT NULL," +
                    "  check_type TEXT NOT NULL," +
                    "  vl INTEGER NOT NULL," +
                    "  details TEXT," +
                    "  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_violations_uuid ON violations(uuid)"
                );

                stmt.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_violations_timestamp ON violations(timestamp)"
                );
            }

            // Run auto-purge
            int purgeDays = plugin.getConfig().getInt("database.purge-days", 30);
            if (purgeDays > 0) {
                purgeOld(purgeDays);
            }

            plugin.getLogger().info("SQLite database initialized: " + dbFile.getName());

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite database!", e);
            this.enabled = false;
        }
    }

    /**
     * Log a violation to the database (async).
     */
    public void logViolation(UUID uuid, String playerName, CheckType checkType, int vl, String details) {
        if (!enabled || connection == null) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO violations (uuid, player_name, check_type, vl, details) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, playerName);
                ps.setString(3, checkType.getConfigKey());
                ps.setInt(4, vl);
                ps.setString(5, details);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to log violation to database", e);
            }
        });
    }

    /**
     * Get violation logs for a player.
     */
    public List<ViolationLog> getViolations(UUID uuid, int limit) {
        List<ViolationLog> logs = new ArrayList<>();
        if (!enabled || connection == null) return logs;

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM violations WHERE uuid = ? ORDER BY timestamp DESC LIMIT ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ViolationLog(
                        rs.getInt("id"),
                        rs.getString("uuid"),
                        rs.getString("player_name"),
                        rs.getString("check_type"),
                        rs.getInt("vl"),
                        rs.getString("details"),
                        rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to query violations", e);
        }

        return logs;
    }

    /**
     * Get the total violation count for a player.
     */
    public int getTotalViolations(UUID uuid) {
        if (!enabled || connection == null) return 0;

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM violations WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to count violations", e);
        }
        return 0;
    }

    /**
     * Purge violations older than the specified number of days.
     */
    public void purgeOld(int days) {
        if (!enabled || connection == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM violations WHERE timestamp < datetime('now', '-' || ? || ' days')")) {
            ps.setInt(1, days);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                plugin.getLogger().info("Purged " + deleted + " old violation records (>" + days + " days).");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to purge old violations", e);
        }
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close database connection", e);
            }
        }
    }
}
