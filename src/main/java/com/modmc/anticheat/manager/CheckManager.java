package com.modmc.anticheat.manager;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.*;
import com.modmc.anticheat.check.combat.*;
import com.modmc.anticheat.check.movement.*;
import com.modmc.anticheat.check.player.*;
import com.modmc.anticheat.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all checks and player data.
 */
public class CheckManager {

    private final ModMCAntiCheat plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private final List<Check> movementChecks = new ArrayList<>();
    private final List<Check> combatChecks = new ArrayList<>();
    private final List<Check> playerChecks = new ArrayList<>();

    public CheckManager(ModMCAntiCheat plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize all checks.
     */
    public void init() {
        movementChecks.clear();
        combatChecks.clear();
        playerChecks.clear();

        // Movement checks
        movementChecks.add(new SpeedCheck(plugin));
        movementChecks.add(new FlyCheck(plugin));
        movementChecks.add(new NoFallCheck(plugin));
        movementChecks.add(new JesusCheck(plugin));
        movementChecks.add(new ElytraCheck(plugin));
        movementChecks.add(new NoSlowCheck(plugin));

        // Combat checks
        combatChecks.add(new ReachCheck(plugin));
        combatChecks.add(new KillauraCheck(plugin));
        combatChecks.add(new AutoClickerCheck(plugin));
        combatChecks.add(new VelocityCheck(plugin));
        combatChecks.add(new CriticalsCheck(plugin));

        // Player checks
        playerChecks.add(new TimerCheck(plugin));
        playerChecks.add(new BadPacketsCheck(plugin));
        playerChecks.add(new InventoryCheck(plugin));
        playerChecks.add(new ScaffoldCheck(plugin));
        playerChecks.add(new FastBreakCheck(plugin));

        int total = movementChecks.size() + combatChecks.size() + playerChecks.size();
        long enabled = getEnabledCount();
        plugin.getLogger().info("Loaded " + total + " checks (" + enabled + " enabled).");
    }

    private long getEnabledCount() {
        long count = 0;
        for (Check c : movementChecks) if (c.isEnabled()) count++;
        for (Check c : combatChecks) if (c.isEnabled()) count++;
        for (Check c : playerChecks) if (c.isEnabled()) count++;
        return count;
    }

    // ===========================
    // Player Data Management
    // ===========================

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public PlayerData getOrCreatePlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player));
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public Collection<PlayerData> getAllPlayerData() {
        return playerDataMap.values();
    }

    // ===========================
    // Check Access
    // ===========================

    public List<Check> getMovementChecks() { return movementChecks; }
    public List<Check> getCombatChecks() { return combatChecks; }
    public List<Check> getPlayerChecks() { return playerChecks; }

    public List<Check> getAllChecks() {
        List<Check> all = new ArrayList<>();
        all.addAll(movementChecks);
        all.addAll(combatChecks);
        all.addAll(playerChecks);
        return all;
    }

    /**
     * Find a check by its CheckType.
     */
    public Check getCheck(CheckType type) {
        for (Check check : getAllChecks()) {
            if (check.getCheckType() == type) {
                return check;
            }
        }
        return null;
    }
}
