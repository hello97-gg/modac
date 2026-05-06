package com.modmc.anticheat.listener;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.combat.*;
import com.modmc.anticheat.check.movement.*;
import com.modmc.anticheat.check.player.*;
import com.modmc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Main PacketEvents listener that routes packets to appropriate checks.
 */
public class PacketListener extends PacketListenerAbstract {

    private final ModMCAntiCheat plugin;

    public PacketListener(ModMCAntiCheat plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        User user = event.getUser();
        if (user == null) return;

        UUID uuid = user.getUUID();
        if (uuid == null) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        PlayerData data = plugin.getCheckManager().getOrCreatePlayerData(player);
        data.incrementPacketCount();

        // === MOVEMENT PACKETS ===
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION
                || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                || event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION
                || event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING) {

            // Run Timer check ONLY on movement packets
            for (Check check : plugin.getCheckManager().getPlayerChecks()) {
                if (check instanceof TimerCheck) {
                    ((TimerCheck) check).handle(data, event);
                }
            }

            if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION
                    || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
                handleMovement(event, player, data);
            }
        }

        // === PLAYER ROTATION ONLY ===
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
            handleRotation(event, player, data);
        }

        // === FLYING PACKET (ground state updates) ===
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING) {
            handleFlying(event, player, data);
        }

        // === ATTACK / USE ENTITY ===
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            handleAttack(event, player, data);
        }

        // === ARM ANIMATION (clicks) ===
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            handleAnimation(player, data);
        }

        // === PLAYER DIGGING (track mining state) ===
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            handleDigging(event, data);
        }

        // === BLOCK PLACEMENT ===
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            handleBlockPlace(event, player, data);
        }

        // === WINDOW CLICK (inventory) ===
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            handleInventoryClick(player, data);
        }

        // === BAD PACKETS (all packets) ===
        for (Check check : plugin.getCheckManager().getPlayerChecks()) {
            if (check instanceof BadPacketsCheck) {
                ((BadPacketsCheck) check).handle(event, data);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        User user = event.getUser();
        if (user == null) return;

        UUID uuid = user.getUUID();
        if (uuid == null) return;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        PlayerData data = plugin.getCheckManager().getPlayerData(uuid);
        if (data == null) return;

        // Track velocity sent by server (for VelocityCheck)
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event);
            if (velocity.getEntityId() == player.getEntityId()) {
                double vx = velocity.getVelocity().getX();
                double vy = velocity.getVelocity().getY();
                double vz = velocity.getVelocity().getZ();
                data.setVelocity(vx, vy, vz);
            }
        }

        // Track teleport (for exemption)
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            data.markTeleport();
        }
    }

    // ===========================
    // Packet Handlers
    // ===========================

    private void handleMovement(PacketReceiveEvent event, Player player, PlayerData data) {
        double x, y, z;
        boolean onGround;

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
            x = packet.getLocation().getX();
            y = packet.getLocation().getY();
            z = packet.getLocation().getZ();
            onGround = packet.isOnGround();
        } else {
            WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
            x = packet.getLocation().getX();
            y = packet.getLocation().getY();
            z = packet.getLocation().getZ();
            onGround = packet.isOnGround();
        }

        Location newLoc = new Location(player.getWorld(), x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
        data.updateLocation(newLoc, onGround);

        // Run movement checks
        for (Check check : plugin.getCheckManager().getMovementChecks()) {
            if (check instanceof SpeedCheck) {
                ((SpeedCheck) check).handle(data);
            } else if (check instanceof FlyCheck) {
                ((FlyCheck) check).handle(data);
            } else if (check instanceof NoFallCheck) {
                ((NoFallCheck) check).handle(data, event);
            } else if (check instanceof JesusCheck) {
                ((JesusCheck) check).handle(data);
            } else if (check instanceof ElytraCheck) {
                ((ElytraCheck) check).handle(data);
            } else if (check instanceof NoSlowCheck) {
                ((NoSlowCheck) check).handle(data);
            }
        }

        // Velocity tick
        data.tickVelocity();
    }

    private void handleRotation(PacketReceiveEvent event, Player player, PlayerData data) {
        WrapperPlayClientPlayerRotation packet = new WrapperPlayClientPlayerRotation(event);
        Location loc = player.getLocation().clone();
        loc.setYaw(packet.getYaw());
        loc.setPitch(packet.getPitch());
        data.updateLocation(loc, packet.isOnGround());
    }

    private void handleFlying(PacketReceiveEvent event, Player player, PlayerData data) {
        WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
        // Update ground state only
        Location loc = data.getLastLocation() != null ? data.getLastLocation().clone() : player.getLocation().clone();
        data.updateLocation(loc, packet.isOnGround());
    }

    private void handleAttack(PacketReceiveEvent event, Player player, PlayerData data) {
        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

        int entityId = packet.getEntityId();
        Entity target = null;

        // Find the target entity
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.getEntityId() == entityId) {
                target = entity;
                break;
            }
        }

        if (target == null) return;

        data.addClick();
        data.setAttackTick((int) (System.currentTimeMillis() / 50L));
        data.setLastTarget(target.getUniqueId());

        // Run combat checks
        for (Check check : plugin.getCheckManager().getCombatChecks()) {
            if (check instanceof ReachCheck) {
                ((ReachCheck) check).handle(data, player, target);
            } else if (check instanceof KillauraCheck) {
                ((KillauraCheck) check).handle(data, player, target);
            } else if (check instanceof VelocityCheck) {
                ((VelocityCheck) check).handle(data);
            } else if (check instanceof CriticalsCheck) {
                ((CriticalsCheck) check).handle(data, player, target);
            }
        }
    }

    private void handleAnimation(Player player, PlayerData data) {
        // Don't count arm swings as clicks while mining blocks
        // Mining naturally sends ~20 arm animations per second
        if (data.isDigging()) return;

        // Also skip if the player was digging recently (within 500ms)
        // to handle the gap between dig finish and digging=false
        long timeSinceDig = System.currentTimeMillis() - data.getLastDigStartTime();
        if (timeSinceDig < 500 && timeSinceDig >= 0) return;

        data.addClick();

        // Run autoclicker check
        for (Check check : plugin.getCheckManager().getCombatChecks()) {
            if (check instanceof AutoClickerCheck) {
                ((AutoClickerCheck) check).handle(data);
            }
        }
    }

    private void handleDigging(PacketReceiveEvent event, PlayerData data) {
        WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
        DiggingAction action = packet.getAction();

        if (action == DiggingAction.START_DIGGING) {
            data.setDigging(true);
            data.setLastDigStartTime(System.currentTimeMillis());
        } else if (action == DiggingAction.CANCELLED_DIGGING
                || action == DiggingAction.FINISHED_DIGGING) {
            data.setDigging(false);
        }

        // Run FastBreak check
        Player player = data.getPlayer();
        if (player != null) {
            for (Check check : plugin.getCheckManager().getPlayerChecks()) {
                if (check instanceof FastBreakCheck) {
                    ((FastBreakCheck) check).handle(data, event);
                }
            }
        }
    }

    private void handleBlockPlace(PacketReceiveEvent event, Player player, PlayerData data) {
        // Run scaffold check (it handles lastBlockPlace/blocksPlaced internally)
        for (Check check : plugin.getCheckManager().getPlayerChecks()) {
            if (check instanceof ScaffoldCheck) {
                ((ScaffoldCheck) check).handle(data, event);
            }
        }
    }

    private void handleInventoryClick(Player player, PlayerData data) {
        // Run inventory check
        for (Check check : plugin.getCheckManager().getPlayerChecks()) {
            if (check instanceof InventoryCheck) {
                ((InventoryCheck) check).handle(data);
            }
        }
    }
}
