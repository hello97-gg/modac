package com.modmc.anticheat.manager;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.command.AntiCheatCommand;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.ColorUtil;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Manages alert broadcasting to staff members.
 */
public class AlertManager {

    private final ModMCAntiCheat plugin;
    private boolean alertsEnabled;
    private boolean consoleAlerts;
    private String alertPermission;

    public AlertManager(ModMCAntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.alertsEnabled = plugin.getConfig().getBoolean("alerts.enabled", true);
        this.consoleAlerts = plugin.getConfig().getBoolean("alerts.console", true);
        this.alertPermission = plugin.getConfig().getString("alerts.broadcast-permission", "modac.alerts");
    }

    /**
     * Send a violation alert to all staff with alerts enabled.
     */
    public void sendAlert(PlayerData data, CheckType checkType, int vl, String details) {
        if (!alertsEnabled) return;

        String prefix = plugin.getPrefix();
        int maxVL = plugin.getConfig().getInt("checks." + checkType.getConfigKey() + ".max-vl", 20);
        int ping = 0;
        try {
            if (data.getPlayer() != null) {
                ping = PlayerUtil.getPing(data.getPlayer());
            }
        } catch (Exception ignored) {}

        String message = prefix
                + "&e" + data.getName()
                + " &7failed &c" + checkType.getDisplayName()
                + " &7[&fVL: " + ColorUtil.formatVL(vl, maxVL)
                + "&7] [&fPing: &b" + ping + "ms&7]"
                + (details != null && !details.isEmpty() ? " &8(" + details + ")" : "");

        String colorized = ColorUtil.colorize(message);

        // Send to console
        if (consoleAlerts) {
            Bukkit.getConsoleSender().sendMessage(colorized);
        }

        // Send to online staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.hasPermission(alertPermission)) continue;

            // Check if the staff member has alerts enabled in their PlayerData
            PlayerData staffData = plugin.getCheckManager().getPlayerData(staff.getUniqueId());
            if (staffData != null && !staffData.isAlertsEnabled()) continue;

            staff.sendMessage(colorized);
        }

        // Send to monitoring staff (even if they don't have alert permission toggled)
        AntiCheatCommand cmdHandler = plugin.getCommandHandler();
        if (cmdHandler != null) {
            Set<UUID> monitors = cmdHandler.getMonitoringStaff(data.getName());
            for (UUID monitorUUID : monitors) {
                Player monitor = Bukkit.getPlayer(monitorUUID);
                if (monitor != null && monitor.isOnline()) {
                    // Send a special prefixed message so they know it's from monitoring
                    String monitorMsg = ColorUtil.colorize("&8[&d⊕&8] " + message);
                    monitor.sendMessage(monitorMsg);
                }
            }
        }
    }
}
