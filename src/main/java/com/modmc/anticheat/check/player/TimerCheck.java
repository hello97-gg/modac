package com.modmc.anticheat.check.player;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;

/**
 * Detects Timer hacks where the client sends movement packets faster
 * than normal (20 ticks per second). Timer speeds up the client's game loop.
 *
 * Uses per-player balance tracking: each movement packet adds 50ms of "debt",
 * and real elapsed time pays it off. If the balance grows too high, the player
 * is sending packets faster than the clock allows.
 */
public class TimerCheck extends Check {

    private final double maxSpeedPercentage;

    public TimerCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.TIMER);
        this.maxSpeedPercentage = plugin.getConfig().getDouble("checks.timer.max-speed-percentage", 110.0);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        long now = System.currentTimeMillis();
        long lastTime = data.getTimerLastCheck();

        // Initialize on first call
        if (lastTime == 0) {
            data.setTimerLastCheck(now);
            data.setTimerPacketCount(0);
            return;
        }

        data.setTimerPacketCount(data.getTimerPacketCount() + 1);

        long elapsed = now - lastTime;

        // Only evaluate every 2 seconds to smooth out bursts
        if (elapsed < 2000) return;

        int packets = data.getTimerPacketCount();

        // Expected: ~20 movement packets per second
        double expected = 20.0 * (elapsed / 1000.0);
        double percentage = (packets / expected) * 100.0;

        // Only flag if consistently above threshold
        // Allow burst margin — network jitter can cause packets to bunch up
        if (percentage > maxSpeedPercentage && packets > 50) {
            flag(data, String.format("timer=%.1f%% packets=%d expected=%.0f",
                    percentage, packets, expected));
        } else {
            reward(data);
        }

        // Reset per-player counters
        data.setTimerPacketCount(0);
        data.setTimerLastCheck(now);
    }
}
