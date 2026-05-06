package com.modmc.anticheat.check.movement;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.entity.Player;

/**
 * Detects ElytraFly exploits where players exceed maximum elytra speed
 * or use elytra flight in impossible situations.
 */
public class ElytraCheck extends Check {

    private final double maxSpeed;

    public ElytraCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.ELYTRA);
        this.maxSpeed = plugin.getConfig().getDouble("checks.elytra.max-speed", 3.5);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;

        // Only check when gliding
        if (!PlayerUtil.isGliding(player)) return;
        if (data.isVelocityPending()) return;

        double deltaX = data.getDeltaX();
        double deltaY = data.getDeltaY();
        double deltaZ = data.getDeltaZ();

        // Calculate 3D speed
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        // Check for excessive speed
        if (speed > maxSpeed) {
            flag(data, String.format("elytra speed=%.3f max=%.3f", speed, maxSpeed));
            return;
        }

        // Check for upward elytra movement without firework (sustained ascent)
        // Normal elytra can gain altitude briefly but not sustain upward flight
        if (deltaY > 0.5 && data.getLastDeltaY() > 0.5) {
            flag(data, String.format("elytra sustained ascent deltaY=%.3f lastDY=%.3f",
                    deltaY, data.getLastDeltaY()));
            return;
        }

        reward(data);
    }
}
