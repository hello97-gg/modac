package com.modmc.anticheat;

import com.modmc.anticheat.command.AntiCheatCommand;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.database.DatabaseManager;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.listener.BukkitListener;
import com.modmc.anticheat.listener.PacketListener;
import com.modmc.anticheat.manager.AlertManager;
import com.modmc.anticheat.manager.CheckManager;
import com.modmc.anticheat.manager.PunishmentManager;
import com.modmc.anticheat.util.ColorUtil;
import com.modmc.anticheat.util.VersionUtil;
import com.modmc.anticheat.webhook.DiscordWebhook;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ModMC AntiCheat — Main plugin class.
 * Advanced server-side anticheat for Minecraft 1.16+.
 */
public class ModMCAntiCheat extends JavaPlugin {

    private CheckManager checkManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    private DatabaseManager databaseManager;
    private DiscordWebhook discordWebhook;
    private AntiCheatCommand commandHandler;
    private String prefix;

    @Override
    public void onLoad() {
        // Initialize PacketEvents BEFORE the server enables plugins
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(false)
                .checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        // Save default config
        saveDefaultConfig();
        this.prefix = getConfig().getString("prefix", "&8[&c&lModMC&8] &7");

        // Initialize PacketEvents
        PacketEvents.getAPI().init();

        // Initialize managers
        this.checkManager = new CheckManager(this);
        this.alertManager = new AlertManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.discordWebhook = new DiscordWebhook(this);

        // Initialize database
        databaseManager.init();

        // Initialize checks
        checkManager.init();

        // Register packet listener
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener(this));

        // Register Bukkit listener
        Bukkit.getPluginManager().registerEvents(new BukkitListener(this), this);

        // Register commands
        this.commandHandler = new AntiCheatCommand(this);
        getCommand("modac").setExecutor(commandHandler);
        getCommand("modac").setTabCompleter(commandHandler);

        // Initialize data for online players (in case of reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkManager.getOrCreatePlayerData(player);
        }

        // Schedule VL decay task
        int decayInterval = getConfig().getInt("vl-decay.interval", 30);
        int decayAmount = getConfig().getInt("vl-decay.amount", 1);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (PlayerData data : checkManager.getAllPlayerData()) {
                for (CheckType type : CheckType.values()) {
                    data.decrementViolation(type, decayAmount);
                }
            }
        }, decayInterval * 20L, decayInterval * 20L);

        long elapsed = System.currentTimeMillis() - startTime;

        // Startup banner
        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║     ModMC AntiCheat v" + getDescription().getVersion() + "           ║");
        getLogger().info("║     Server: " + VersionUtil.getVersionString() + "                    ║");
        getLogger().info("║     Loaded in " + elapsed + "ms                   ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        // Terminate PacketEvents
        PacketEvents.getAPI().terminate();

        // Close database
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("ModMC AntiCheat disabled.");
    }

    // ===========================
    // Getters
    // ===========================

    public CheckManager getCheckManager() { return checkManager; }
    public AlertManager getAlertManager() { return alertManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public DiscordWebhook getDiscordWebhook() { return discordWebhook; }
    public AntiCheatCommand getCommandHandler() { return commandHandler; }
    public String getPrefix() { return prefix; }
}
