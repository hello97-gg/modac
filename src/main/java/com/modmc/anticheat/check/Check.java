package com.modmc.anticheat.check;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Abstract base class for all anticheat checks.
 * Each check is responsible for a single type of cheat detection.
 */
public abstract class Check {

    protected final ModMCAntiCheat plugin;
    protected final CheckType checkType;
    protected final boolean enabled;
    protected final int maxVL;
    protected final int alertVL;
    protected final String punishCommand;

    public Check(ModMCAntiCheat plugin, CheckType checkType) {
        this.plugin = plugin;
        this.checkType = checkType;

        String path = "checks." + checkType.getConfigKey();
        this.enabled = plugin.getConfig().getBoolean(path + ".enabled", true);
        this.maxVL = plugin.getConfig().getInt(path + ".max-vl", 20);
        this.alertVL = plugin.getConfig().getInt(path + ".alert-vl", 5);
        this.punishCommand = plugin.getConfig().getString(path + ".punish-command", "kick %player% Unfair Advantage");
    }

    /**
     * Flag a player for a violation.
     *
     * @param data    The player's data
     * @param details Human-readable details about the violation
     */
    protected void flag(PlayerData data, String details) {
        if (!enabled) return;

        Player player = data.getPlayer();
        if (player == null || !player.isOnline()) return;

        // Check bypass permission
        if (player.hasPermission("modac.bypass")) return;

        // Increment violation level
        int vl = data.incrementViolation(checkType);

        // Alert staff if VL meets threshold
        if (vl >= alertVL) {
            plugin.getAlertManager().sendAlert(data, checkType, vl, details);
        }

        // Log to database
        plugin.getDatabaseManager().logViolation(data.getUuid(), data.getName(), checkType, vl, details);

        // Send to Discord if configured
        if (vl >= plugin.getConfig().getInt("discord.min-vl", 10)) {
            plugin.getDiscordWebhook().sendViolation(data, checkType, vl, details);
        }

        // Check if punishment threshold reached
        if (vl >= maxVL) {
            plugin.getPunishmentManager().punish(data, checkType, punishCommand);
        }
    }

    /**
     * Reward a player for clean play by decaying their VL.
     */
    protected void reward(PlayerData data) {
        data.decrementViolation(checkType, 1);
    }

    /**
     * Check if a player should be exempt from this check.
     */
    protected boolean isExempt(PlayerData data) {
        Player player = data.getPlayer();
        if (player == null) return true;
        if (!player.isOnline()) return true;
        if (!enabled) return true;
        if (player.hasPermission("modac.bypass")) return true;
        if (data.isTeleporting()) return true;

        // Grace period: don't check players who just joined
        if (System.currentTimeMillis() - data.getJoinTime() < 3000) return true;

        return com.modmc.anticheat.util.PlayerUtil.isExempt(player);
    }

    public CheckType getCheckType() {
        return checkType;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
