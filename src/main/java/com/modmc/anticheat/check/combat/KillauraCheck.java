package com.modmc.anticheat.check.combat;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Detects KillAura/Aimbot by analyzing rotation patterns,
 * attack consistency, and multi-target behavior.
 */
public class KillauraCheck extends Check {

    public KillauraCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.KILLAURA);
    }

    public void handle(PlayerData data, Player player, Entity target) {
        if (isExempt(data)) return;

        // Check 1: Rotation snap — unnaturally fast rotation to target
        checkRotationSnap(data, player, target);

        // Check 2: Multi-target — attacking multiple targets in quick succession
        checkMultiTarget(data, player, target);

        // Check 3: Angle check — attacking entity outside field of view
        checkAngle(data, player, target);
    }

    private void checkRotationSnap(PlayerData data, Player player, Entity target) {
        float deltaYaw = Math.abs(data.getDeltaYaw());
        float deltaPitch = Math.abs(data.getDeltaPitch());

        // Very large, instant rotation changes can indicate aimbot
        // Normal human rotation is gradual, but fast PvP players can flick quickly
        if (deltaYaw > 90 && deltaPitch > 45) {
            // Verify this isn't just a normal fast turn by checking consistency
            float lastDeltaYaw = Math.abs(data.getLastDeltaYaw());
            float lastDeltaPitch = Math.abs(data.getLastDeltaPitch());

            // If the previous tick had minimal rotation and this tick has huge rotation,
            // it's likely a snap
            if (lastDeltaYaw < 10 && lastDeltaPitch < 10) {
                flag(data, String.format("rotation snap dyaw=%.1f dpitch=%.1f", deltaYaw, deltaPitch));
            }
        }
    }

    private void checkMultiTarget(PlayerData data, Player player, Entity target) {
        if (data.getLastTarget() == null) return;
        if (data.getLastTarget().equals(target.getUniqueId())) return;

        long timeSinceLastAttack = System.currentTimeMillis() - data.getLastAttackTime();

        // Attacking different targets within 50ms is very suspicious
        if (timeSinceLastAttack < 50) {
            flag(data, String.format("multi-target dt=%dms", timeSinceLastAttack));
        }
    }

    private void checkAngle(PlayerData data, Player player, Entity target) {
        Location playerLoc = player.getLocation();
        Location targetLoc = target.getLocation();

        // Calculate angle between look direction and target direction
        Vector lookDir = playerLoc.getDirection();
        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector()).normalize();

        double angle = MathUtil.angleBetween(lookDir, toTarget);

        // Attacking something more than 90° away from look direction
        // is impossible in vanilla (field of view is ~110° max)
        if (angle > 90) {
            flag(data, String.format("behind-attack angle=%.1f°", angle));
        }
    }
}
