package com.modmc.anticheat.check.combat;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Detects Criticals hacks — getting critical hits without properly jumping.
 *
 * In vanilla, a critical hit requires:
 *   1. Player is falling (deltaY < 0)
 *   2. Player is NOT on the ground
 *   3. Player is NOT in water/lava/on ladder/in cobweb
 *   4. Player is NOT riding a vehicle
 *   5. Player does NOT have blindness
 *
 * Criticals hacks do tiny micro-jumps (e.g. 0.0625 blocks) to trigger
 * the critical flag every single hit. We detect this by tracking
 * if the player's jump height is suspiciously consistent and tiny.
 */
public class CriticalsCheck extends Check {

    private int suspiciousCritCount = 0;

    public CriticalsCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.CRITICALS);
    }

    /**
     * Called when the player attacks an entity.
     */
    public void handle(PlayerData data, Player player, org.bukkit.entity.Entity target) {
        if (isExempt(data)) return;
        if (player == null) return;

        // Only check if the attack would be a critical hit
        // Critical hit conditions: falling, not on ground, not in special states
        boolean isOnGround = data.isClientOnGround();
        double deltaY = data.getDeltaY();
        double lastDeltaY = data.getLastDeltaY();

        // Check if this looks like a critical hit attempt
        boolean falling = deltaY < 0;
        boolean wasRising = lastDeltaY > 0;

        if (!isOnGround && falling) {
            // The player IS meeting critical hit conditions.
            // Now check if it's a legitimate jump or a micro-jump.

            // Track the jump height: how high did they go before falling?
            // Legitimate jumps reach ~1.25 blocks height.
            // Criticals hacks do micro-jumps of 0.0625 to 0.1 blocks.

            // Calculate approximate jump height from fall velocity
            // In vanilla, after jumping: first tick deltaY ≈ 0.42
            // Micro-jump: deltaY ≈ 0.05 to 0.1

            // Check the last upward velocity before falling
            if (wasRising && lastDeltaY < 0.1 && lastDeltaY > 0) {
                // Micro-jump detected: went up by < 0.1 then immediately fell
                suspiciousCritCount++;

                if (suspiciousCritCount >= 3) {
                    flag(data, String.format("criticals microjump lastDY=%.4f curDY=%.4f count=%d",
                            lastDeltaY, deltaY, suspiciousCritCount));
                }
            } else if (!wasRising && Math.abs(lastDeltaY) < 0.08) {
                // Another pattern: almost no vertical movement but claims not on ground
                // This happens when criticals hack spoofs ground state
                suspiciousCritCount++;

                if (suspiciousCritCount >= 4) {
                    flag(data, String.format("criticals ground-spoof lastDY=%.4f curDY=%.4f count=%d",
                            lastDeltaY, deltaY, suspiciousCritCount));
                }
            } else {
                // Legitimate jump (high deltaY before falling)
                if (suspiciousCritCount > 0) suspiciousCritCount--;
                reward(data);
            }
        } else if (isOnGround) {
            // Player is on ground — can't crit. This is normal.
            // Decay the suspicious counter
            if (suspiciousCritCount > 0) suspiciousCritCount--;
            reward(data);
        }
    }
}
