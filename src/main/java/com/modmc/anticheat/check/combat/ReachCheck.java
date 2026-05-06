package com.modmc.anticheat.check.combat;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Detects reach hacks by measuring the distance between attacker
 * and target at the time of attack, with latency compensation.
 */
public class ReachCheck extends Check {

    private final double maxReach;
    private final double pingCompensation;

    public ReachCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.REACH);
        this.maxReach = plugin.getConfig().getDouble("checks.reach.max-reach", 3.1);
        this.pingCompensation = plugin.getConfig().getDouble("checks.reach.ping-compensation", 0.05);
    }

    public void handle(PlayerData data, Player player, Entity target) {
        if (isExempt(data)) return;

        Location playerLoc = player.getLocation();
        Location targetLoc = target.getLocation();

        // Calculate distance (eye position to target hitbox center)
        Location eyeLoc = playerLoc.clone().add(0, player.getEyeHeight(), 0);
        double distance = MathUtil.distance3D(eyeLoc, targetLoc);

        // Apply latency compensation
        int ping = PlayerUtil.getPing(player);
        double compensated = maxReach + (ping / 50.0) * pingCompensation;

        // Cap compensation to prevent abuse at very high ping
        compensated = Math.min(compensated, maxReach + 0.5);

        if (distance > compensated) {
            flag(data, String.format("dist=%.3f max=%.3f ping=%dms", distance, compensated, ping));
        } else {
            reward(data);
        }
    }
}
