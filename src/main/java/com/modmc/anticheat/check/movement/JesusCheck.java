package com.modmc.anticheat.check.movement;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Detects Jesus/WaterWalk hacks where players walk on water or lava.
 */
public class JesusCheck extends Check {

    public JesusCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.JESUS);
    }

    public void handle(PlayerData data) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;
        if (PlayerUtil.isGliding(player)) return;
        if (data.isVelocityPending()) return;

        Location loc = data.getLastLocation();
        if (loc == null) return;

        // Check if standing on water/lava surface
        Block below = loc.clone().subtract(0, 0.3, 0).getBlock();
        Block at = loc.getBlock();
        String belowName = below.getType().name();
        String atName = at.getType().name();

        boolean onLiquidSurface = (belowName.contains("WATER") || belowName.contains("LAVA"))
                && at.getType() == Material.AIR;

        if (!onLiquidSurface) {
            reward(data);
            return;
        }

        // Check Frost Walker enchantment (turns water to ice)
        // Frost Walker creates frosted ice, not applicable for lava
        if (belowName.contains("WATER")) {
            try {
                if (player.getInventory().getBoots() != null) {
                    if (player.getInventory().getBoots().getEnchantments().keySet().stream()
                            .anyMatch(e -> e.getName().contains("FROST_WALKER"))) {
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Check if there are any solid blocks or lilypads adjacent to the player's bounding box
        if (isNearSolidBlock(loc)) {
            reward(data);
            return;
        }

        double deltaY = data.getDeltaY();

        // Walking on liquid: very small deltaY and on ground claim
        if (Math.abs(deltaY) < 0.05 && data.isClientOnGround()) {
            flag(data, String.format("walking on %s deltaY=%.4f",
                    belowName.contains("LAVA") ? "lava" : "water", deltaY));
        }
    }

    private boolean isNearSolidBlock(Location loc) {
        // Check a 3x3 grid below the player's feet
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // Check blocks exactly below and slightly above to account for lilypads
                for (int y = -1; y <= 0; y++) {
                    Block b = loc.clone().add(x * 0.4, y, z * 0.4).getBlock();
                    String name = b.getType().name();
                    if (name.contains("LILY_PAD") || name.contains("WATERLILY") ||
                            name.contains("BOAT") ||
                            (b.getType().isSolid() && !name.contains("SIGN") && !name.contains("STRING"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
