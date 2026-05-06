package com.modmc.anticheat.command;

import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.check.Check;
import com.modmc.anticheat.check.CheckType;
import com.modmc.anticheat.data.PlayerData;
import com.modmc.anticheat.database.ViolationLog;
import com.modmc.anticheat.util.ColorUtil;
import com.modmc.anticheat.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class AntiCheatCommand implements CommandExecutor, TabCompleter {

    private final ModMCAntiCheat plugin;

    // Track monitored players (staff UUID -> target player name)
    private final Map<UUID, String> monitoredPlayers = new HashMap<>();

    public AntiCheatCommand(ModMCAntiCheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "alerts":
                handleAlerts(sender);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "toggle":
                handleToggle(sender, args);
                break;
            case "verbose":
                handleVerbose(sender);
                break;
            case "history":
                handleHistory(sender, args);
                break;
            case "reset":
                handleReset(sender, args);
                break;
            case "notify":
                handleNotify(sender, args);
                break;
            case "monitor":
                handleMonitor(sender, args);
                break;
            case "kick":
                handleKick(sender, args);
                break;
            case "ban":
                handleBan(sender, args);
                break;
            case "testwebhook":
                handleTestWebhook(sender);
                break;
            case "top":
                handleTop(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &c&lModMC &fAntiCheat &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &e/modac alerts &8- &7Toggle alert messages"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac info <player> &8- &7View player VLs"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac history <player> &8- &7View DB logs"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac toggle <check> &8- &7Toggle a check"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac verbose &8- &7Toggle verbose mode"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac reload &8- &7Reload configuration"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac reset <player> &8- &7Reset player VLs"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac notify <player> &8- &7Get notified about a player"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac monitor <player> &8- &7Live monitor a player"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac kick <player> [reason] &8- &7Kick a player"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac ban <player> [reason] &8- &7Ban a player"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac top &8- &7View most flagged players"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac status &8- &7View anticheat status"));
        sender.sendMessage(ColorUtil.colorize("  &e/modac testwebhook &8- &7Test Discord webhook"));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
    }

    private void handleAlerts(CommandSender sender) {
        if (!sender.hasPermission("modac.alerts")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData data = plugin.getCheckManager().getOrCreatePlayerData(player);
            boolean newState = !data.isAlertsEnabled();
            data.setAlertsEnabled(newState);
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix()
                    + "Alerts " + (newState ? "&aenabled" : "&cdisabled") + "&7."));
        } else {
            // Console always has alerts via config
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&7Console alerts are always active via config."));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.info")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac info <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cPlayer not found."));
            return;
        }

        PlayerData data = plugin.getCheckManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo data for that player."));
            return;
        }

        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &c&lModMC &fAntiCheat &8| &e" + target.getName()));
        sender.sendMessage(ColorUtil.colorize("  &7Ping: &b" + PlayerUtil.getPing(target) + "ms"));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));

        Map<CheckType, Integer> violations = data.getAllViolations();
        for (CheckType type : CheckType.values()) {
            int vl = violations.getOrDefault(type, 0);
            if (vl > 0) {
                int maxVL = plugin.getConfig().getInt("checks." + type.getConfigKey() + ".max-vl", 20);
                sender.sendMessage(ColorUtil.colorize("  &7" + type.getDisplayName()
                        + " &8» " + ColorUtil.formatVL(vl, maxVL)
                        + " &8/ &f" + maxVL
                        + " " + ColorUtil.progressBar(vl, maxVL, 10)));
            }
        }

        boolean hasViolation = violations.values().stream().anyMatch(v -> v > 0);
        if (!hasViolation) {
            sender.sendMessage(ColorUtil.colorize("  &aNo active violations."));
        }
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("modac.reload")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }

        plugin.reloadConfig();
        plugin.getAlertManager().reload();
        plugin.getPunishmentManager().reload();
        plugin.getDiscordWebhook().reload();
        plugin.getCheckManager().init();

        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&aConfiguration reloaded."));
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.toggle")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac toggle <check>"));
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&7Checks: speed, fly, nofall, jesus, elytra, reach, killaura, autoclicker, velocity, timer, badpackets, inventory, scaffold"));
            return;
        }

        String checkName = args[1].toLowerCase();
        CheckType type = CheckType.fromConfigKey(checkName);
        if (type == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUnknown check: " + checkName));
            return;
        }

        String path = "checks." + type.getConfigKey() + ".enabled";
        boolean current = plugin.getConfig().getBoolean(path, true);
        plugin.getConfig().set(path, !current);
        plugin.saveConfig();
        plugin.getCheckManager().init();

        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&f" + type.getDisplayName()
                + " " + (!current ? "&aenabled" : "&cdisabled") + "&7."));
    }

    private void handleVerbose(CommandSender sender) {
        if (!sender.hasPermission("modac.verbose")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData data = plugin.getCheckManager().getOrCreatePlayerData(player);
            boolean newState = !data.isVerboseEnabled();
            data.setVerboseEnabled(newState);
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix()
                    + "Verbose mode " + (newState ? "&aenabled" : "&cdisabled") + "&7."));
        } else {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&7Console always receives verbose output."));
        }
    }

    private void handleHistory(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.info")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac history <player>"));
            return;
        }

        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || !target.hasPlayedBefore()) {
            Player onlineTarget = Bukkit.getPlayer(args[1]);
            if (onlineTarget == null) {
                sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cPlayer not found in database."));
                return;
            }
            target = onlineTarget;
        }

        List<ViolationLog> logs = plugin.getDatabaseManager().getViolations(target.getUniqueId(), 10);
        if (logs.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&aNo violation history."));
            return;
        }

        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &c&lModMC &fHistory &8| &e" + target.getName()));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        for (ViolationLog log : logs) {
            sender.sendMessage(ColorUtil.colorize("  &7" + log.getTimestamp()
                    + " &8| &c" + log.getCheckType()
                    + " &8VL:&f" + log.getVl()));
        }
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.toggle")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac reset <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cPlayer not online."));
            return;
        }

        PlayerData data = plugin.getCheckManager().getPlayerData(target.getUniqueId());
        if (data != null) {
            data.resetAllViolations();
        }
        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&aReset VLs for &e" + target.getName() + "&a."));
    }

    // ========================
    // NEW COMMANDS
    // ========================

    /**
     * /modac notify <player> — Get Discord-style notifications about a specific player.
     * Sends an immediate summary of their current violations.
     */
    private void handleNotify(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.alerts")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac notify <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cPlayer not online."));
            return;
        }

        PlayerData data = plugin.getCheckManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo data for that player."));
            return;
        }

        // Build summary
        Map<CheckType, Integer> violations = data.getAllViolations();
        int totalVL = violations.values().stream().mapToInt(Integer::intValue).sum();

        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &c&lModMC &fNotification &8| &e" + target.getName()));
        sender.sendMessage(ColorUtil.colorize("  &7Ping: &b" + PlayerUtil.getPing(target) + "ms &8| &7Total VL: &c" + totalVL));
        sender.sendMessage(ColorUtil.colorize("  &7Location: &f" + String.format("%.1f, %.1f, %.1f",
                target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ())));
        sender.sendMessage(ColorUtil.colorize("  &7World: &f" + target.getWorld().getName()));
        sender.sendMessage(ColorUtil.colorize("  &7Health: &c" + String.format("%.1f", target.getHealth()) + " &8| &7Food: &a" + target.getFoodLevel()));
        sender.sendMessage(ColorUtil.colorize("  &7Gamemode: &f" + target.getGameMode().name()));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));

        for (CheckType type : CheckType.values()) {
            int vl = violations.getOrDefault(type, 0);
            if (vl > 0) {
                int maxVL = plugin.getConfig().getInt("checks." + type.getConfigKey() + ".max-vl", 20);
                sender.sendMessage(ColorUtil.colorize("  &7" + type.getDisplayName()
                        + " &8» " + ColorUtil.formatVL(vl, maxVL)
                        + " &8/ &f" + maxVL
                        + " " + ColorUtil.progressBar(vl, maxVL, 10)));
            }
        }

        boolean hasViolation = violations.values().stream().anyMatch(v -> v > 0);
        if (!hasViolation) {
            sender.sendMessage(ColorUtil.colorize("  &aClean player — no active violations."));
        }
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
    }

    /**
     * /modac monitor <player> — Start live-monitoring a player's checks.
     * All their violations will be forwarded to you exclusively, even without verbose mode.
     */
    private void handleMonitor(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.alerts")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }

        // Use a special UUID for console
        UUID senderUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId()
                : UUID.nameUUIDFromBytes("CONSOLE".getBytes());

        if (args.length < 2) {
            if (monitoredPlayers.containsKey(senderUUID)) {
                String was = monitoredPlayers.remove(senderUUID);
                sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&7Stopped monitoring &e" + was + "&7."));
            } else {
                sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac monitor <player>"));
                sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&7Type /modac monitor without a player to stop."));
            }
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cPlayer not online."));
            return;
        }

        monitoredPlayers.put(senderUUID, target.getName());

        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&aNow monitoring &e" + target.getName()
                + "&a. All their violations will be sent to you."));
        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&7Type &e/modac monitor &7to stop."));
    }

    /**
     * Check if a staff member is monitoring a specific player.
     */
    public boolean isMonitoring(UUID staffUUID, String targetName) {
        String monitored = monitoredPlayers.get(staffUUID);
        return monitored != null && monitored.equalsIgnoreCase(targetName);
    }

    /**
     * Get all staff UUIDs monitoring a specific player.
     */
    public Set<UUID> getMonitoringStaff(String playerName) {
        Set<UUID> result = new HashSet<>();
        for (Map.Entry<UUID, String> entry : monitoredPlayers.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(playerName)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * /modac kick <player> [reason] — Kick a player with a reason.
     */
    private void handleKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.punish")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac kick <player> [reason]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cPlayer not online."));
            return;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                : "Kicked by staff";
        target.kickPlayer(ColorUtil.colorize("&c&lModMC AntiCheat\n&7" + reason));

        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&aKicked &e" + target.getName() + " &7(&f" + reason + "&7)"));

        // Send to Discord
        plugin.getDiscordWebhook().sendPunishment(target.getName(), "Staff Kick", "Kicked by " + sender.getName() + ": " + reason);

        // Broadcast to staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("modac.alerts") && !staff.equals(sender)) {
                staff.sendMessage(ColorUtil.colorize(plugin.getPrefix()
                        + "&e" + sender.getName() + " &7kicked &c" + target.getName() + " &7(&f" + reason + "&7)"));
            }
        }
    }

    /**
     * /modac ban <player> [reason] — Ban a player with a reason.
     */
    private void handleBan(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modac.punish")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cUsage: /modac ban <player> [reason]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cPlayer not online."));
            return;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                : "Banned by staff";

        // Ban the player using Bukkit's ban list
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(),
                ColorUtil.colorize("&c&lModMC AntiCheat\n&7" + reason), null, sender.getName());
        target.kickPlayer(ColorUtil.colorize("&c&lModMC AntiCheat\n&7" + reason));

        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&4Banned &e" + target.getName() + " &7(&f" + reason + "&7)"));

        // Send to Discord
        plugin.getDiscordWebhook().sendPunishment(target.getName(), "Staff Ban", "Banned by " + sender.getName() + ": " + reason);

        // Broadcast to staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("modac.alerts") && !staff.equals(sender)) {
                staff.sendMessage(ColorUtil.colorize(plugin.getPrefix()
                        + "&e" + sender.getName() + " &4banned &c" + target.getName() + " &7(&f" + reason + "&7)"));
            }
        }
    }

    /**
     * /modac testwebhook — Send a test message to the Discord webhook.
     */
    private void handleTestWebhook(CommandSender sender) {
        if (!sender.hasPermission("modac.reload")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }

        sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&7Sending test message to Discord..."));
        plugin.getDiscordWebhook().sendTest();
    }

    /**
     * /modac top — Show the most flagged online players sorted by total VL.
     */
    private void handleTop(CommandSender sender) {
        if (!sender.hasPermission("modac.alerts")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }

        // Collect all online players' data
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (PlayerData data : plugin.getCheckManager().getAllPlayerData()) {
            if (data.getPlayer() == null || !data.getPlayer().isOnline()) continue;
            int totalVL = data.getAllViolations().values().stream().mapToInt(Integer::intValue).sum();
            if (totalVL > 0) {
                entries.add(new AbstractMap.SimpleEntry<>(data.getName(), totalVL));
            }
        }

        if (entries.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&aNo active violations on any player."));
            return;
        }

        // Sort by VL descending
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &c&lModMC &fTop Flagged Players"));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));

        int rank = 1;
        for (Map.Entry<String, Integer> entry : entries) {
            if (rank > 10) break;
            String color = rank <= 3 ? "&c" : "&e";
            sender.sendMessage(ColorUtil.colorize("  " + color + "#" + rank + " &f" + entry.getKey() + " &8- &c" + entry.getValue() + " VL"));
            rank++;
        }
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
    }

    /**
     * /modac status — Show anticheat status: enabled checks, online players, etc.
     */
    private void handleStatus(CommandSender sender) {
        if (!sender.hasPermission("modac.alerts")) {
            sender.sendMessage(ColorUtil.colorize(plugin.getPrefix() + "&cNo permission."));
            return;
        }

        int totalPlayers = Bukkit.getOnlinePlayers().size();
        int trackedPlayers = plugin.getCheckManager().getAllPlayerData().size();

        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &c&lModMC &fAntiCheat Status"));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
        sender.sendMessage(ColorUtil.colorize("  &7Version: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(ColorUtil.colorize("  &7Players Online: &f" + totalPlayers));
        sender.sendMessage(ColorUtil.colorize("  &7Players Tracked: &f" + trackedPlayers));
        sender.sendMessage(ColorUtil.colorize("  &7Discord Webhook: " +
                (plugin.getConfig().getBoolean("discord.enabled", false) ? "&aEnabled" : "&cDisabled")));
        sender.sendMessage(ColorUtil.colorize("  &7Database: " +
                (plugin.getConfig().getBoolean("database.enabled", true) ? "&aEnabled" : "&cDisabled")));
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));

        // Show enabled/disabled checks
        sender.sendMessage(ColorUtil.colorize("  &7Checks:"));
        for (CheckType type : CheckType.values()) {
            boolean enabled = plugin.getConfig().getBoolean("checks." + type.getConfigKey() + ".enabled", true);
            sender.sendMessage(ColorUtil.colorize("    " + (enabled ? "&a✓" : "&c✗") + " &f" + type.getDisplayName()
                    + " &8[" + type.getCategory() + "]"));
        }
        sender.sendMessage(ColorUtil.colorize("&8&m                                        "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = Arrays.asList("alerts", "info", "reload", "toggle", "verbose",
                    "history", "reset", "notify", "monitor", "kick", "ban", "testwebhook", "top", "status");
            for (String sub : subs) {
                if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("info") || sub.equals("history") || sub.equals("reset")
                    || sub.equals("notify") || sub.equals("monitor")
                    || sub.equals("kick") || sub.equals("ban")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
            } else if (sub.equals("toggle")) {
                for (CheckType type : CheckType.values()) {
                    if (type.getConfigKey().startsWith(args[1].toLowerCase())) {
                        completions.add(type.getConfigKey());
                    }
                }
            }
        }

        return completions;
    }
}
