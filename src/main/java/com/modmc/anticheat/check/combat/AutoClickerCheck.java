package com.modmc.anticheat.check.combat;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.MathUtil;

/**
 * Detects auto-clickers by analyzing CPS and click pattern consistency.
 * Human clicking has natural variation; bots produce uniform intervals.
 */
public class AutoClickerCheck extends Check {

    private final int maxCPS;
    private final double minDeviation;

    public AutoClickerCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.AUTOCLICKER);
        this.maxCPS = plugin.getConfig().getInt("checks.autoclicker.max-cps", 20);
        this.minDeviation = plugin.getConfig().getDouble("checks.autoclicker.min-deviation", 15.0);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        // Check CPS over the last second
        double cps = data.getCPS(1000);

        // Check 1: CPS exceeds maximum
        if (cps > maxCPS) {
            flag(data, String.format("cps=%.1f max=%d", cps, maxCPS));
            return;
        }

        // Check 2: Click pattern consistency (only with enough data)
        double[] intervals = data.getClickIntervals(20);
        if (intervals.length >= 10) {
            double stdDev = MathUtil.standardDeviation(intervals);
            double mean = MathUtil.mean(intervals);

            // Very low standard deviation means robotic clicking
            // Humans typically have 30-80ms deviation
            if (stdDev < minDeviation && mean > 0 && mean < 200) {
                flag(data, String.format("robotic clicking stdDev=%.2fms mean=%.2fms cps=%.1f",
                        stdDev, mean, cps));
                return;
            }
        }

        reward(data);
    }
}
