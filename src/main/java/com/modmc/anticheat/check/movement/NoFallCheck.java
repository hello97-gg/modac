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
 * Detects NoFall hacks where the client sends ground=true
 * while actually being in the air to avoid fall damage.
 */
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

public class NoFallCheck extends Check {

    public NoFallCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.NOFALL);
    }

    public void handle(PlayerData data, PacketReceiveEvent event) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;

        if (PlayerUtil.isGliding(player)) return;
        if (PlayerUtil.isInLiquid(player)) return;
        if (PlayerUtil.isClimbing(player)) return;
        if (PlayerUtil.hasSlowFalling(player)) return;
        if (data.isVelocityPending()) return;

        if (player.isInsideVehicle()) return;

        boolean clientOnGround = data.isClientOnGround();
        double deltaY = data.getDeltaY();

        // The client says they're on the ground, but they're falling fast
        if (clientOnGround && deltaY < -0.5) {
            // Verify they're not actually on a block
            Location loc = data.getLastLocation();
            if (loc != null && !isNearGround(loc)) {
                // Packet NoFall: sends one ground packet right before hitting the ground
                // Instantly scale VL based on the fall speed
                int multiplier = (int) Math.max(2, Math.abs(deltaY) * 5);
                for (int i = 0; i < Math.min(20, multiplier); i++) {
                    flag(data, String.format("clientGround=true deltaY=%.4f", deltaY));
                }
                event.setCancelled(true); // Cancel packet so fall distance is not reset
                return;
            }
        }

        // Client says on ground, but there's no block below for multiple ticks
        if (clientOnGround && data.getAirTicks() == 0) {
            Location loc = data.getLastLocation();
            if (loc != null && !isNearGround(loc) && deltaY < -0.1) {
                // Regular NoFall: constantly sends ground packets mid-air
                flag(data, String.format("spoofed ground deltaY=%.4f", deltaY));
                flag(data, String.format("spoofed ground deltaY=%.4f", deltaY)); // Double VL for NoFall
                event.setCancelled(true); // Cancel packet so fall distance is not reset
                return;
            }
        }

        reward(data);
    }

    private boolean isNearGround(Location loc) {
        // Check blocks below and around the player (including partial blocks)
        for (double xOff = -0.3; xOff <= 0.3; xOff += 0.3) {
            for (double zOff = -0.3; zOff <= 0.3; zOff += 0.3) {
                for (double yOff = 0; yOff <= 1.0; yOff += 0.5) {
                    Block block = loc.clone().add(xOff, -yOff, zOff).getBlock();
                    if (block.getType() != Material.AIR) {
                        String name = block.getType().name();
                        if (!name.equals("WATER") && !name.equals("LAVA")
                                && !name.equals("STATIONARY_WATER") && !name.equals("STATIONARY_LAVA")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
