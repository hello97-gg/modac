package com.modmc.anticheat.check.movement;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.entity.Player;

/**
 * Detects fly hacks by tracking air time and vertical movement patterns.
 * Vanilla clients have predictable gravity behavior.
 */
public class FlyCheck extends Check {

    private final int maxAirtime;

    public FlyCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.FLY);
        this.maxAirtime = plugin.getConfig().getInt("checks.fly.max-airtime", 40);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;

        // Exempt flying-capable players
        if (player.getAllowFlight() || player.isFlying()) return;
        if (PlayerUtil.isGliding(player)) return;
        if (PlayerUtil.isClimbing(player)) return;
        if (PlayerUtil.isInLiquid(player)) return;
        if (PlayerUtil.isSwimming(player)) return;
        if (PlayerUtil.hasLevitation(player)) return;
        if (PlayerUtil.hasSlowFalling(player)) return;
        if (data.isVelocityPending()) return;

        double deltaY = data.getDeltaY();
        int airTicks = data.getAirTicks();
        boolean clientOnGround = data.isClientOnGround();

        // Check 1: Excessive air time without falling properly
        if (airTicks > maxAirtime && deltaY >= -0.05) {
            flag(data, String.format("airTicks=%d deltaY=%.4f", airTicks, deltaY));
            return;
        }

        // Check 1.5: Massive instant vertical movement (Vertical Tap TP / High Jump)
        // Vanilla max jump height in one tick is around ~0.42 (or slightly higher with jump boost)
        // Anything above 5 blocks in a single tick is an instant teleport hack
        if (deltaY > 5.0 && !data.isVelocityPending()) {
            for (int i = 0; i < 20; i++) {
                flag(data, String.format("instant-vertical-tp deltaY=%.2f", deltaY));
            }
            return;
        }

        // Check 2: Ascending while in air for too long (not a normal jump)
        // Normal jump: deltaY positive for ~6 ticks then falls
        // Allow up to 15 ticks for things like jumping up blocks/slabs
        if (airTicks > 15 && deltaY > 0.0 && !data.isVelocityPending()) {
            flag(data, String.format("ascending airTicks=%d deltaY=%.4f", airTicks, deltaY));
            return;
        }

        // Check 3: Hovering — staying at same Y level in air
        if (airTicks > 20 && Math.abs(deltaY) < 0.01) {
            flag(data, String.format("hovering airTicks=%d deltaY=%.4f", airTicks, deltaY));
            return;
        }

        // Check 4: Gravity check — after the jump apex, gravity should pull down
        // Very lenient to avoid flagging on ledges/fences
        if (airTicks > 15 && deltaY > data.getLastDeltaY() + 0.1 && data.getLastDeltaY() < 0) {
            // Gained speed upward while supposed to be falling
            flag(data, String.format("anti-gravity deltaY=%.4f lastDY=%.4f", deltaY, data.getLastDeltaY()));
            return;
        }

        if (airTicks <= 5) {
            reward(data);
        }
    }
}
