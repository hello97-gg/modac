package com.modmc.anticheat.manager;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Manages punishments when violation thresholds are exceeded.
 */
public class PunishmentManager {

    private final ModMCAntiCheat plugin;
    private String kickMessage;
    private boolean broadcastPunishments;
    private String broadcastFormat;

    public PunishmentManager(ModMCAntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.kickMessage = plugin.getConfig().getString("punishment.kick-message",
                "&c&lModMC AntiCheat\n&7You have been removed for unfair advantage.");
        this.broadcastPunishments = plugin.getConfig().getBoolean("punishment.broadcast-punishments", true);
        this.broadcastFormat = plugin.getConfig().getString("punishment.broadcast-format",
                "&8[&c&lModMC&8] &e%player% &7was punished for &c%check%&7.");
    }

    /**
     * Execute punishment for a player.
     */
    public void punish(PlayerData data, CheckType checkType, String command) {
        Player player = data.getPlayer();
        if (player == null || !player.isOnline()) return;

        // Reset VL after punishment
        data.resetViolations(checkType);

        // Execute the punishment command on the main thread
        String finalCommand = command
                .replace("%player%", player.getName())
                .replace("%check%", checkType.getDisplayName());

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        });

        // Broadcast punishment
        if (broadcastPunishments) {
            String broadcast = broadcastFormat
                    .replace("%player%", player.getName())
                    .replace("%check%", checkType.getDisplayName());
            String colorized = ColorUtil.colorize(broadcast);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("modac.alerts")) {
                    staff.sendMessage(colorized);
                }
            }
            Bukkit.getConsoleSender().sendMessage(colorized);
        }

        plugin.getLogger().warning("[PUNISHMENT] " + player.getName()
                + " punished for " + checkType.getDisplayName()
                + " | Command: " + finalCommand);
    }
}
