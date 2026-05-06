package com.modmc.anticheat.check.player;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Detects FastBreak/SpeedMine/InstantBreak hacks by comparing
 * the time between START_DIGGING and FINISHED_DIGGING against
 * the vanilla break time for that block + tool + enchantment combo.
 */
public class FastBreakCheck extends Check {

    public FastBreakCheck(ModMCAntiCheat plugin) {
        super(plugin, CheckType.FASTBREAK);
    }

    /**
     * Called when we receive a PLAYER_DIGGING packet.
     */
    public void handle(PlayerData data, PacketReceiveEvent event) {
        if (isExempt(data)) return;

        Player player = data.getPlayer();
        if (player == null) return;

        WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
        DiggingAction action = packet.getAction();

        if (action == DiggingAction.START_DIGGING) {
            // Record when and where the player started digging
            Vector3i pos = packet.getBlockPosition();
            data.setDigStartTime(System.currentTimeMillis());
            data.setDigBlockX(pos.getX());
            data.setDigBlockY(pos.getY());
            data.setDigBlockZ(pos.getZ());

        } else if (action == DiggingAction.FINISHED_DIGGING) {
            long startTime = data.getDigStartTime();
            if (startTime <= 0) return; // No start recorded

            long elapsed = System.currentTimeMillis() - startTime;

            // Get the block that was broken
            Vector3i pos = packet.getBlockPosition();
            Block block;
            try {
                block = player.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            } catch (Exception e) {
                return;
            }

            Material blockType = block.getType();

            // Skip instant-break blocks (flowers, grass, etc.)
            if (isInstantBreak(blockType, player)) {
                return;
            }

            // Calculate expected break time in ms
            double expectedTicks = getBreakTicks(blockType, player);
            long expectedMs = (long) (expectedTicks * 50); // 1 tick = 50ms

            // Allow 20% tolerance for lag/timing
            long minExpectedMs = (long) (expectedMs * 0.8);

            // Flag if broken significantly faster than possible
            if (elapsed < minExpectedMs && expectedMs > 200) {
                flag(data, String.format("fastbreak block=%s time=%dms expected=%dms",
                        blockType.name(), elapsed, expectedMs));
            } else {
                reward(data);
            }

            data.setDigStartTime(0); // Reset
        }
    }

    /**
     * Check if a block can be broken instantly with the player's current tool.
     */
    private boolean isInstantBreak(Material material, Player player) {
        float hardness = material.getHardness();
        if (hardness <= 0) return true; // Grass, flowers, torches, etc.
        if (hardness < 0) return false; // Bedrock, barriers

        // Check if player has correct tool + high efficiency
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.containsEnchantment(Enchantment.DIG_SPEED)) {
            int effLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
            // With Efficiency V + Haste II, some blocks can be instant-broken
            if (hardness <= 0.5 && effLevel >= 5) {
                return true;
            }
        }

        return false;
    }

    /**
     * Estimate the number of ticks to break a block.
     * This is a simplified calculation — real calculation involves
     * tool type, tool material, enchantments, and potion effects.
     */
    private double getBreakTicks(Material material, Player player) {
        float hardness = material.getHardness();
        if (hardness <= 0) return 0;

        // Base break speed multiplier
        double speedMultiplier = 1.0;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.getType() != Material.AIR) {
            // Check if the tool is appropriate for this block
            if (isCorrectTool(tool.getType(), material)) {
                speedMultiplier = getToolSpeed(tool.getType());

                // Efficiency enchantment
                if (tool.containsEnchantment(Enchantment.DIG_SPEED)) {
                    int effLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
                    speedMultiplier += (effLevel * effLevel) + 1;
                }
            }
        }

        // Haste potion effect
        if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
            int hasteLevel = player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier() + 1;
            speedMultiplier *= (1 + 0.2 * hasteLevel);
        }

        // Mining fatigue
        if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
            int fatigueLevel = player.getPotionEffect(PotionEffectType.SLOW_DIGGING).getAmplifier() + 1;
            switch (fatigueLevel) {
                case 1: speedMultiplier *= 0.3; break;
                case 2: speedMultiplier *= 0.09; break;
                case 3: speedMultiplier *= 0.027; break;
                default: speedMultiplier *= 0.00081; break;
            }
        }

        // Not on ground = 5x slower
        if (!player.isOnGround()) {
            speedMultiplier /= 5.0;
        }

        // In water without Aqua Affinity = 5x slower
        if (player.isInWater()) {
            boolean hasAquaAffinity = false;
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet != null) {
                try {
                    hasAquaAffinity = helmet.containsEnchantment(Enchantment.WATER_WORKER);
                } catch (Exception ignored) {}
            }
            if (!hasAquaAffinity) {
                speedMultiplier /= 5.0;
            }
        }

        // Calculate ticks
        double damage = speedMultiplier / hardness;
        if (isCorrectTool(tool != null ? tool.getType() : Material.AIR, material)) {
            damage /= 30.0;
        } else {
            damage /= 100.0;
        }

        if (damage >= 1.0) return 0; // Instant break

        return Math.ceil(1.0 / damage);
    }

    private boolean isCorrectTool(Material toolType, Material blockType) {
        String tool = toolType.name();
        String block = blockType.name();

        if (tool.contains("PICKAXE")) {
            return block.contains("STONE") || block.contains("ORE") || block.contains("IRON")
                    || block.contains("GOLD") || block.contains("DIAMOND") || block.contains("NETHERITE")
                    || block.contains("OBSIDIAN") || block.contains("BRICK") || block.contains("CONCRETE")
                    || block.contains("TERRACOTTA") || block.contains("PRISMARINE")
                    || block.contains("QUARTZ") || block.contains("SANDSTONE")
                    || block.contains("DEEPSLATE") || block.contains("COPPER")
                    || block.contains("AMETHYST") || block.contains("BASALT");
        }
        if (tool.contains("AXE") && !tool.contains("PICKAXE")) {
            return block.contains("WOOD") || block.contains("LOG") || block.contains("PLANK")
                    || block.contains("FENCE") || block.contains("DOOR") || block.contains("SIGN")
                    || block.contains("MUSHROOM") || block.contains("PUMPKIN")
                    || block.contains("MELON") || block.contains("BAMBOO");
        }
        if (tool.contains("SHOVEL")) {
            return block.contains("DIRT") || block.contains("SAND") || block.contains("GRAVEL")
                    || block.contains("CLAY") || block.contains("SNOW") || block.contains("SOUL")
                    || block.contains("MUD") || block.contains("MYCELIUM")
                    || block.contains("FARMLAND") || block.contains("GRASS_BLOCK");
        }
        if (tool.contains("HOE")) {
            return block.contains("LEAVES") || block.contains("HAY")
                    || block.contains("SPONGE") || block.contains("SHROOMLIGHT")
                    || block.contains("NETHER_WART_BLOCK") || block.contains("WARPED_WART_BLOCK")
                    || block.contains("SCULK");
        }
        if (tool.contains("SHEARS")) {
            return block.contains("WOOL") || block.contains("COBWEB")
                    || block.contains("LEAVES") || block.contains("VINE");
        }
        return false;
    }

    private double getToolSpeed(Material toolType) {
        String name = toolType.name();
        if (name.contains("WOODEN") || name.contains("GOLD")) return 2.0;
        if (name.contains("STONE")) return 4.0;
        if (name.contains("IRON")) return 6.0;
        if (name.contains("DIAMOND")) return 8.0;
        if (name.contains("NETHERITE")) return 9.0;
        return 1.0;
    }
}
