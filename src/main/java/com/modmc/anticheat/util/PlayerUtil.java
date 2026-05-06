package com.modmc.anticheat.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Player state utility methods.
 */
public final class PlayerUtil {

    private PlayerUtil() {}

    /**
     * Get the amplifier of a potion effect on a player.
     * Returns -1 if the player does not have the effect.
     */
    public static int getPotionLevel(Player player, PotionEffectType type) {
        PotionEffect effect = player.getPotionEffect(type);
        return effect != null ? effect.getAmplifier() : -1;
    }

    /**
     * Check if a player has a specific potion effect.
     */
    public static boolean hasPotionEffect(Player player, PotionEffectType type) {
        return player.hasPotionEffect(type);
    }

    /**
     * Get the player's speed potion amplifier (0-based).
     * Returns 0 if no speed effect.
     */
    public static int getSpeedAmplifier(Player player) {
        int level = getPotionLevel(player, PotionEffectType.SPEED);
        return level >= 0 ? level + 1 : 0;
    }

    /**
     * Get the player's jump boost amplifier (0-based).
     * Returns 0 if no jump effect.
     */
    public static int getJumpAmplifier(Player player) {
        int level = getPotionLevel(player, PotionEffectType.JUMP);
        return level >= 0 ? level + 1 : 0;
    }

    /**
     * Check if the player has slow falling effect.
     */
    public static boolean hasSlowFalling(Player player) {
        try {
            PotionEffectType slowFalling = PotionEffectType.getByName("SLOW_FALLING");
            return slowFalling != null && player.hasPotionEffect(slowFalling);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the player has levitation effect.
     */
    public static boolean hasLevitation(Player player) {
        try {
            PotionEffectType levitation = PotionEffectType.getByName("LEVITATION");
            return levitation != null && player.hasPotionEffect(levitation);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the player is in creative or spectator mode.
     */
    public static boolean isExempt(Player player) {
        switch (player.getGameMode()) {
            case CREATIVE:
            case SPECTATOR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the player is gliding with an elytra.
     */
    public static boolean isGliding(Player player) {
        return player.isGliding();
    }

    /**
     * Check if the player is swimming (1.13+ mechanic).
     */
    public static boolean isSwimming(Player player) {
        try {
            return player.isSwimming();
        } catch (NoSuchMethodError e) {
            // Pre-1.13 doesn't have swimming
            return false;
        }
    }

    /**
     * Check if the player is climbing (on a ladder or vine).
     */
    public static boolean isClimbing(Player player) {
        org.bukkit.Material blockType = player.getLocation().getBlock().getType();
        String name = blockType.name();
        return name.equals("LADDER") || name.equals("VINE")
                || name.contains("TWISTING_VINES") || name.contains("WEEPING_VINES")
                || name.contains("CAVE_VINES");
    }

    /**
     * Check if the player is in water.
     */
    public static boolean isInWater(Player player) {
        org.bukkit.Material blockType = player.getLocation().getBlock().getType();
        String name = blockType.name();
        return name.equals("WATER") || name.equals("STATIONARY_WATER");
    }

    /**
     * Check if the player is in lava.
     */
    public static boolean isInLava(Player player) {
        org.bukkit.Material blockType = player.getLocation().getBlock().getType();
        String name = blockType.name();
        return name.equals("LAVA") || name.equals("STATIONARY_LAVA");
    }

    /**
     * Check if the player is in a liquid (water or lava).
     */
    public static boolean isInLiquid(Player player) {
        return isInWater(player) || isInLava(player);
    }

    /**
     * Check if the player is on ice.
     */
    public static boolean isOnIce(Player player) {
        org.bukkit.Material below = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        String name = below.name();
        return name.contains("ICE");
    }

    /**
     * Check if the player is on a slab or stair (partial block).
     */
    public static boolean isOnPartialBlock(Player player) {
        org.bukkit.Material below = player.getLocation().subtract(0, 0.5, 0).getBlock().getType();
        String name = below.name();
        return name.contains("SLAB") || name.contains("STAIR") || name.contains("STEP")
                || name.contains("FENCE") || name.contains("WALL") || name.contains("BED")
                || name.contains("CARPET") || name.contains("SNOW") || name.contains("SKULL")
                || name.contains("HEAD") || name.contains("FLOWER_POT")
                || name.contains("CANDLE") || name.contains("CHAIN");
    }

    /**
     * Check if the player is in a cobweb.
     */
    public static boolean isInCobweb(Player player) {
        org.bukkit.Material blockType = player.getLocation().getBlock().getType();
        String name = blockType.name();
        return name.equals("COBWEB") || name.equals("WEB");
    }

    /**
     * Get the player's ping in milliseconds.
     */
    public static int getPing(Player player) {
        try {
            // Paper API
            return player.getPing();
        } catch (NoSuchMethodError e) {
            // Fallback: reflection for Spigot
            try {
                Object handle = player.getClass().getMethod("getHandle").invoke(player);
                return (int) handle.getClass().getField("ping").get(handle);
            } catch (Exception ex) {
                return 0;
            }
        }
    }
}
