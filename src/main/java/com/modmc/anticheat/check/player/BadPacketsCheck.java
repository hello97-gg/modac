package com.modmc.anticheat.check.player;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;

/**
 * Detects invalid/impossible packet data that no vanilla client would send.
 * Covers NaN positions, invalid pitch, and impossible states.
 */
public class BadPacketsCheck extends Check {

    public BadPacketsCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.BADPACKETS);
    }

    public void handle(PacketReceiveEvent event, PlayerData data) {
        if (isExempt(data)) return;

        // Check position packets for NaN/Infinity
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
            double x = packet.getLocation().getX();
            double y = packet.getLocation().getY();
            double z = packet.getLocation().getZ();

            if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)
                    || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
                flag(data, "NaN/Infinite position");
                event.setCancelled(true);
                return;
            }

            // Check for impossibly large coordinates
            if (Math.abs(x) > 3.0E7 || Math.abs(z) > 3.0E7 || Math.abs(y) > 1000) {
                flag(data, String.format("impossible coords x=%.0f y=%.0f z=%.0f", x, y, z));
                event.setCancelled(true);
                return;
            }
        }

        // Check position+rotation packets
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
            float pitch = packet.getPitch();

            // Vanilla pitch is clamped between -90 and 90
            if (Math.abs(pitch) > 90.0f) {
                flag(data, String.format("invalid pitch=%.2f", pitch));
                event.setCancelled(true);
                return;
            }

            // Check for NaN rotation
            if (Float.isNaN(packet.getYaw()) || Float.isNaN(pitch)) {
                flag(data, "NaN rotation");
                event.setCancelled(true);
                return;
            }
        }

        reward(data);
    }
}
