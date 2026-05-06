package com.modmc.anticheat.check.player;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Multi-signal scaffold detection. Combines 3 independent signals:
 *   1. Rotation snap — pitch snaps down to place, then snaps back up
 *   2. Direction mismatch — placing blocks BEHIND while moving FORWARD
 *   3. Timing consistency — robotic placement intervals (low std deviation)
 *
 * Only flags when 2+ signals fire together to minimize false positives.
 */
public class ScaffoldCheck extends Check {

    public ScaffoldCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.SCAFFOLD);
    }

    public void handle(PlayerData data, PacketReceiveEvent event) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;

        long now = System.currentTimeMillis();
        long timeSinceLastPlace = now - data.getLastBlockPlace();

        // Reset signals and counters after a gap in placement
        if (timeSinceLastPlace > 2000) {
            data.setScaffoldSignalCount(0);
            data.setBlocksPlaced(0);
            data.getPlacementIntervals().clear();
        }

        // Parse the block placement packet
        WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
        Vector3i blockPos = packet.getBlockPosition();
        BlockFace face = packet.getFace();

        // Get player position from the last packet-level location (not Bukkit)
        org.bukkit.Location packetLoc = data.getLastLocation();
        if (packetLoc == null) packetLoc = player.getLocation();

        double playerX = packetLoc.getX();
        double playerY = packetLoc.getY();
        double playerZ = packetLoc.getZ();
        float packetPitch = packetLoc.getPitch();
        float packetYaw = packetLoc.getYaw();

        // Check if the block is being placed directly below the player's feet
        // We do NOT check face here because Scaffold hacks can place on the side (EAST/WEST/NORTH/SOUTH) of an adjacent block!
        boolean placingBelow = (blockPos.getY() <= (int) Math.ceil(playerY))
                && (Math.abs(blockPos.getX() - (int) Math.floor(playerX)) <= 1)
                && (Math.abs(blockPos.getZ() - (int) Math.floor(playerZ)) <= 1);

        data.setPlacingBelow(placingBelow);

        if (!placingBelow) {
            // Not placing below themselves — normal building
            reward(data);
            data.setLastPlacePitch(packetPitch);
            return;
        }

        // =========================================
        // The player IS placing blocks under themselves.
        // Run all 3 signal checks.
        // =========================================
        int signals = 0;
        StringBuilder details = new StringBuilder();

        // --- Signal 1: Rotation Snap ---
        // Scaffold hacks snap pitch down by 80°+ in one tick to place,
        // then snap back up. Real players rotate gradually.
        float lastPitch = data.getLastPlacePitch();
        float pitchDelta = Math.abs(packetPitch - lastPitch);
        if (pitchDelta > 50 && data.getBlocksPlaced() >= 2) {
            signals++;
            details.append(String.format("RotSnap=%.1f ", pitchDelta));
        }

        // --- Signal 2: Direction Mismatch ---
        // Real players face where they place. Scaffold hacks place
        // BEHIND themselves while running FORWARD.
        double facingX = -Math.sin(Math.toRadians(packetYaw));
        double facingZ = Math.cos(Math.toRadians(packetYaw));
        double toBlockX = (blockPos.getX() + 0.5) - playerX;
        double toBlockZ = (blockPos.getZ() + 0.5) - playerZ;
        double dot = (facingX * toBlockX) + (facingZ * toBlockZ);

        if (dot < -0.3) {
            signals++;
            details.append(String.format("DirMis=%.2f ", dot));
        }

        // --- Signal 3: Timing Consistency ---
        // Humans have variable timing. Bots are perfectly consistent.
        if (timeSinceLastPlace > 0 && timeSinceLastPlace < 1000) {
            List<Long> intervals = data.getPlacementIntervals();
            intervals.add(timeSinceLastPlace);

            // Sliding window of 10 placements
            while (intervals.size() > 10) {
                intervals.remove(0);
            }

            if (intervals.size() >= 6) {
                double stdDev = calculateStdDev(intervals);
                double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0);

                // Std deviation < 20ms over 6+ placements with fast placement = robotic
                if (stdDev < 20.0 && mean < 200) {
                    signals++;
                    details.append(String.format("Robotic=%.1fms±%.1f ", mean, stdDev));
                }
            }
        }

        // Update state for next check
        data.setLastPlacePitch(packetPitch);
        data.setLastBlockPlace(now);
        data.incrementBlocksPlaced();

        // --- Decision: Only flag when 2+ signals fire together ---
        if (signals >= 2) {
            data.addScaffoldSignal();

            // Flag and mark the placement to be cancelled by BukkitListener
            flag(data, String.format("scaffold [%d signals] %s blocks=%d",
                    signals, details.toString().trim(), data.getBlocksPlaced()));
            event.setCancelled(true); // Attempt packet-level cancel
            data.setLastScaffoldCancelTime(now); // Reliable server-side cancel

            // If they've been caught multiple times, escalate VL faster
            if (data.getScaffoldSignalCount() >= 3) {
                flag(data, String.format("persistent scaffold signals=%d",
                        data.getScaffoldSignalCount()));
            }
        } else {
            reward(data);
        }
    }

    private double calculateStdDev(List<Long> values) {
        double mean = values.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }
}
