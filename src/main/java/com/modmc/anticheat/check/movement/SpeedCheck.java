package com.modmc.anticheat.check.movement;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.entity.Player;

/**
 * Detects speed/movement hacks by comparing horizontal movement
 * against the maximum possible vanilla speed considering all modifiers.
 */
public class SpeedCheck extends Check {

    private final double tolerance;

    public SpeedCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.SPEED);
        this.tolerance = plugin.getConfig().getDouble("checks.speed.tolerance", 1.05);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;

        // Skip if in liquid, climbing, or on partial blocks
        if (PlayerUtil.isInLiquid(player)) return;
        if (PlayerUtil.isClimbing(player)) return;
        if (PlayerUtil.isGliding(player)) return;
        if (PlayerUtil.isSwimming(player)) return;
        if (PlayerUtil.isInCobweb(player)) return;
        if (data.isVelocityPending()) return;

        double deltaX = data.getDeltaX();
        double deltaZ = data.getDeltaZ();
        double horizontalSpeed = MathUtil.horizontalSpeed(deltaX, deltaZ);

        // Calculate maximum allowed speed
        double maxSpeed = calculateMaxSpeed(player, data);

        if (horizontalSpeed > maxSpeed * tolerance) {
            double excess = horizontalSpeed - maxSpeed;

            // If the player moved a MASSIVE amount (e.g. Tap Teleport / Click TP)
            if (excess > 10.0) {
                // Instantly max out VL to kick them
                for (int i = 0; i < 20; i++) {
                    flag(data, String.format("instant-tp distance=%.1f", horizontalSpeed));
                }
                return;
            }

            // Scale VL increment based on how egregious the speed hack is
            int multiplier = (int) Math.max(1, Math.ceil(excess / 0.5));
            
            for (int i = 0; i < Math.min(5, multiplier); i++) {
                flag(data, String.format("speed=%.3f max=%.3f excess=%.3f", horizontalSpeed, maxSpeed, excess));
            }
        } else {
            reward(data);
        }
    }

    private double calculateMaxSpeed(Player player, PlayerData data) {
        // Base walking speed: ~0.2873 blocks/tick
        double base = 0.2873;

        // Sprinting: 1.3x multiplier
        if (player.isSprinting()) {
            base *= 1.3;
        }

        // Jump sprinting gives a significant speed boost
        if (!data.isClientOnGround() && player.isSprinting()) {
            base *= 1.6;
        }

        // Speed potion: +20% per level
        int speedLevel = PlayerUtil.getSpeedAmplifier(player);
        if (speedLevel > 0) {
            base += 0.2 * speedLevel * base;
        }

        // Ice: movement is faster on ice, up to 2.8x (blue ice)
        if (PlayerUtil.isOnIce(player)) {
            base *= 2.8;
        }

        // Soul Sand / Soul Soil with Soul Speed
        if (player.getLocation().getBlock().getType().name().contains("SOUL")) {
            base *= 1.5;
        }

        // Walking speed attribute (can be modified by plugins)
        float walkSpeed = player.getWalkSpeed();
        if (walkSpeed > 0.2f) {
            base *= (walkSpeed / 0.2f);
        }

        // Sneaking is slower
        if (player.isSneaking()) {
            base *= 0.3;
        }

        // Allow buffer for lag, diagonal movement, and edge cases
        base += 0.15;

        return base;
    }
}
