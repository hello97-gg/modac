package com.modmc.anticheat.check.combat;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;

/**
 * Detects anti-knockback (Velocity) hacks where players ignore
 * or reduce knockback sent by the server.
 */
public class VelocityCheck extends Check {

    private final double minPercentage;

    public VelocityCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.VELOCITY);
        this.minPercentage = plugin.getConfig().getDouble("checks.velocity.min-percentage", 75.0);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;
        if (!data.isVelocityPending()) return;

        // Wait a few ticks for the client to respond to velocity
        if (data.getVelocityTicks() < 3) return;

        double expectedX = data.getLastVelocityX();
        double expectedZ = data.getLastVelocityZ();
        double expectedHorizontal = MathUtil.horizontalSpeed(expectedX, expectedZ);

        // Skip very small velocities
        if (expectedHorizontal < 0.1) {
            data.setVelocityPending(false);
            return;
        }

        double actualX = data.getDeltaX();
        double actualZ = data.getDeltaZ();
        double actualHorizontal = MathUtil.horizontalSpeed(actualX, actualZ);

        // Calculate percentage of expected velocity taken
        double percentage = (actualHorizontal / expectedHorizontal) * 100.0;

        // Exempt if player is jumping or taking fall damage (changes Y velocity and affects horizontal)
        if (data.getAirTicks() > 0 && Math.abs(data.getDeltaY() - data.getLastDeltaY()) > 0.1) {
            data.setVelocityPending(false);
            return;
        }

        // Lower minimum percentage slightly to account for friction and wall collisions
        if (percentage < 60.0) {
            flag(data, String.format("velocity=%.1f%% expected=%.3f actual=%.3f",
                    percentage, expectedHorizontal, actualHorizontal));
        } else {
            reward(data);
        }

        // Mark velocity as handled after check
        if (data.getVelocityTicks() >= 5) {
            data.setVelocityPending(false);
        }
    }
}
