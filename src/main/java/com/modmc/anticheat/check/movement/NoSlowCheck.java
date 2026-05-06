package com.modmc.anticheat.check.movement;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.entity.Player;

/**
 * Detects NoSlow hacks — moving at sprint speed while performing
 * actions that should slow the player (eating, blocking, drawing bow).
 *
 * In vanilla, using an item reduces movement speed to ~0.06 blocks/tick
 * (roughly 1/5th of walking speed). NoSlow hacks bypass this.
 */
public class NoSlowCheck extends Check {

    public NoSlowCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.NOSLOW);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;

        // Skip if in liquid, climbing, gliding, etc.
        if (PlayerUtil.isInLiquid(player)) return;
        if (PlayerUtil.isClimbing(player)) return;
        if (PlayerUtil.isGliding(player)) return;
        if (data.isVelocityPending()) return;

        // Check if the player is using an item (eating, blocking, drawing bow)
        boolean usingItem = false;
        try {
            usingItem = player.isHandRaised();
        } catch (NoSuchMethodError e) {
            // Fallback for older versions
            try {
                usingItem = (boolean) player.getClass().getMethod("isBlocking").invoke(player);
            } catch (Exception ignored) {}
        }

        if (!usingItem) {
            reward(data);
            return;
        }

        // Player IS using an item — check their movement speed
        double hSpeed = MathUtil.horizontalSpeed(data.getDeltaX(), data.getDeltaZ());

        // While using an item, max horizontal speed should be ~0.06 blocks/tick
        // We give a generous buffer: 0.15 blocks/tick
        // Normal walking is ~0.2, sprinting is ~0.28
        double maxSlowSpeed = 0.15;

        // Speed potion affects even slowed speed
        int speedLevel = PlayerUtil.getSpeedAmplifier(player);
        if (speedLevel > 0) {
            maxSlowSpeed += 0.04 * speedLevel;
        }

        // If on ice, slippery movement applies
        if (PlayerUtil.isOnIce(player)) {
            maxSlowSpeed *= 2.0;
        }

        if (hSpeed > maxSlowSpeed) {
            flag(data, String.format("noslow speed=%.3f max=%.3f item=true",
                    hSpeed, maxSlowSpeed));
        } else {
            reward(data);
        }
    }
}
