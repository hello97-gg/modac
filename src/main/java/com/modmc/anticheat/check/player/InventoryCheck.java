package com.modmc.anticheat.check.player;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;

/**
 * Detects inventory manipulation hacks where players interact
 * with inventory while performing actions impossible in vanilla
 * (e.g., moving/attacking while in inventory).
 */
public class InventoryCheck extends Check {

    public InventoryCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.INVENTORY);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        // Check if inventory is open AND player is moving significantly
        if (!data.isInventoryOpen()) {
            reward(data);
            return;
        }

        double horizontalSpeed = MathUtil.horizontalSpeed(data.getDeltaX(), data.getDeltaZ());

        // In vanilla, you can't sprint/move fast with inventory open
        // Some slow movement is allowed (drift, lag, etc)
        if (horizontalSpeed > 0.25) {
            flag(data, String.format("inv-move speed=%.4f", horizontalSpeed));
            return;
        }

        // Check for rotation changes while inventory is open
        // Large rotation changes in inventory are suspicious
        // Allow up to 45 degrees of drift before flagging
        float yawChange = Math.abs(data.getDeltaYaw());
        float pitchChange = Math.abs(data.getDeltaPitch());

        if (yawChange > 45 || pitchChange > 45) {
            flag(data, String.format("inv-rotate yaw=%.1f pitch=%.1f", yawChange, pitchChange));
            return;
        }

        reward(data);
    }
}
