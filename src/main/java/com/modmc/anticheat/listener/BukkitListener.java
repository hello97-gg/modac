package com.modmc.anticheat.listener;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BukkitListener implements Listener {

    private final ModMCAntiCheat plugin;

    public BukkitListener(ModMCAntiCheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getCheckManager().getOrCreatePlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getCheckManager().removePlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        PlayerData data = plugin.getCheckManager().getPlayerData(event.getPlayer().getUniqueId());
        if (data != null) data.markTeleport();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        PlayerData data = plugin.getCheckManager().getPlayerData(event.getPlayer().getUniqueId());
        if (data != null) data.setInventoryOpen(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        PlayerData data = plugin.getCheckManager().getPlayerData(event.getPlayer().getUniqueId());
        if (data != null) {
            data.setInventoryOpen(false);
            data.setLastInventoryClose(System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        PlayerData data = plugin.getCheckManager().getPlayerData(event.getPlayer().getUniqueId());
        if (data != null) {
            // If flagged for scaffold in the last 200ms, cancel the placement server-side
            if (System.currentTimeMillis() - data.getLastScaffoldCancelTime() < 200) {
                event.setCancelled(true);
            }
        }
    }
}
