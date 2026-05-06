# 🔌 API Documentation

Want to hook into ModMC AntiCheat from your own plugins? Here's how.

---

## API Overview

The API lets you:
- Listen for violation events
- Check player violation levels
- Get player data
- Hook into the punishment system

---

## Getting Started

### Maven/Gradle Dependency

Add ModMC AntiCheat as a dependency in your `plugin.yml`:

```yaml
depend: [ModMC-AntiCheat]
```

Or soft-depend if optional:

```yaml
softdepend: [ModMC-AntiCheat]
```

### Accessing the API

Get the API instance from the main plugin:

```java
import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.api.AntiCheatAPI;

public class MyPlugin extends JavaPlugin {
    
    private AntiCheatAPI api;
    
    @Override
    public void onEnable() {
        // Get the plugin instance
        ModMCAntiCheat antiCheat = (ModMCAntiCheat) Bukkit.getPluginManager().getPlugin("ModMC-AntiCheat");
        
        if (antiCheat != null) {
            this.api = antiCheat.getAPI();
            getLogger().info("Hooked into ModMC AntiCheat!");
        } else {
            getLogger().warning("ModMC AntiCheat not found!");
        }
    }
    
    public AntiCheatAPI getAntiCheatAPI() {
        return api;
    }
}
```

---

## API Methods

### Get Player Data

```java
PlayerData data = api.getPlayerData(player);
```

The `PlayerData` object contains:

| Method | Returns | Description |
|--------|---------|-------------|
| `getPing()` | `int` | Player's ping |
| `getViolationLevel(CheckType type)` | `int` | Current VL for specific check |
| `getTotalViolations()` | `int` | Total VL across all checks |
| `isFlagging()` | `boolean` | If player is currently flagging |
| `getLastFlagTime()` | `long` | Timestamp of last flag |

**Example: Check if player is suspicious:**

```java
PlayerData data = api.getPlayerData(player);
if (data.getTotalViolations() > 50) {
    player.sendMessage("§cYou're being watched closely...");
}
```

---

### Get Check Manager

```java
CheckManager checkManager = api.getCheckManager();
```

Available methods:

| Method | Description |
|--------|-------------|
| `getChecks()` | Get all registered checks |
| `getCheck(String name)` | Get specific check by name |
| `isEnabled(CheckType type)` | Check if a check is enabled |

---

### Get Alert Manager

```java
AlertManager alertManager = api.getAlertManager();
```

Available methods:

| Method | Description |
|--------|-------------|
| `hasAlertsEnabled(Player player)` | Check if player sees alerts |
| `toggleAlerts(Player player)` | Toggle alerts for player |
| `broadcastAlert(String message)` | Broadcast to all staff |

---

### Get Punishment Manager

```java
PunishmentManager punishmentManager = api.getPunishmentManager();
```

Available methods:

| Method | Description |
|--------|-------------|
| `kick(Player player, String reason)` | Kick with AC branding |
| `ban(Player player, String reason)` | Ban with AC branding |
| `isPunished(Player player)` | Check if recently punished |

---

## Events

Listen for AntiCheat events to react to violations.

### PlayerFlagEvent

Fired when a player flags a check.

```java
import com.modmc.anticheat.event.PlayerFlagEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FlagListener implements Listener {
    
    @EventHandler
    public void onFlag(PlayerFlagEvent event) {
        Player player = event.getPlayer();
        Check check = event.getCheck();
        int newVL = event.getNewVL();
        
        // Do something
        if (check.getType() == CheckType.SPEED && newVL > 20) {
            Bukkit.broadcast("§c" + player.getName() + " is speed hacking!", "myplugin.alerts");
        }
        
        // Cancel the flag (if you want to exempt someone)
        // event.setCancelled(true);
    }
}
```

**Event fields:**

| Field | Type | Description |
|-------|------|-------------|
| `getPlayer()` | `Player` | The player who flagged |
| `getCheck()` | `Check` | The check that flagged |
| `getNewVL()` | `int` | VL after this flag |
| `getOldVL()` | `int` | VL before this flag |
| `getData()` | `String` | Debug data from check |
| `isCancelled()` | `boolean` | If event was cancelled |
| `setCancelled(boolean)` | `void` | Cancel the flag |

---

### PlayerPunishEvent

Fired when a player is punished by AntiCheat.

```java
import com.modmc.anticheat.event.PlayerPunishEvent;

public class PunishListener implements Listener {
    
    @EventHandler
    public void onPunish(PlayerPunishEvent event) {
        Player player = event.getPlayer();
        String reason = event.getReason();
        PunishAction action = event.getAction(); // KICK or BAN
        
        // Log to your own system
        myPlugin.logPunishment(player, reason, action);
    }
}
```

**Event fields:**

| Field | Type | Description |
|-------|------|-------------|
| `getPlayer()` | `Player` | The punished player |
| `getReason()` | `String` | Punishment reason |
| `getAction()` | `PunishAction` | KICK or BAN |
| `isCancelled()` | `boolean` | If event was cancelled |
| `setCancelled(boolean)` | `void` | Cancel the punishment |

---

## Exempting Players

Currently, the API doesn't have a built-in exemption system. You can:

1. **Listen to events and cancel:**

```java
@EventHandler
public void onFlag(PlayerFlagEvent event) {
    if (event.getPlayer().hasPermission("myplugin.bypass")) {
        event.setCancelled(true);
    }
}
```

2. **Check before your plugin's actions:**

```java
PlayerData data = api.getPlayerData(player);
if (data.getTotalViolations() > 0) {
    // Don't let them participate in minigame, etc.
}
```

---

## Example: Custom Alert System

```java
import com.modmc.anticheat.ModMCAntiCheat;
import com.modmc.anticheat.event.PlayerFlagEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomAlertPlugin extends JavaPlugin implements Listener {
    
    private AntiCheatAPI api;
    
    @Override
    public void onEnable() {
        ModMCAntiCheat antiCheat = (ModMCAntiCheat) getServer().getPluginManager().getPlugin("ModMC-AntiCheat");
        if (antiCheat != null) {
            this.api = antiCheat.getAPI();
            getServer().getPluginManager().registerEvents(this, this);
            getLogger().info("Custom alert system enabled!");
        }
    }
    
    @EventHandler
    public void onFlag(PlayerFlagEvent event) {
        // Only alert for high VL
        if (event.getNewVL() >= 15) {
            String message = String.format(
                "§8[§c§lALERT§8] §e%s §7flagged §c%s §7(VL: %d)",
                event.getPlayer().getName(),
                event.getCheck().getName(),
                event.getNewVL()
            );
            
            // Send to your custom channel
            getServer().broadcast(message, "custom.alerts");
        }
    }
}
```

---

## Example: Stats Leaderboard

```java
public class LeaderboardCommand implements CommandExecutor {
    
    private AntiCheatAPI api;
    
    public LeaderboardCommand(AntiCheatAPI api) {
        this.api = api;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Get top violators
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        online.sort((p1, p2) -> {
            PlayerData d1 = api.getPlayerData(p1);
            PlayerData d2 = api.getPlayerData(p2);
            return Integer.compare(d2.getTotalViolations(), d1.getTotalViolations());
        });
        
        sender.sendMessage("§6=== Top Cheaters Online ===");
        for (int i = 0; i < Math.min(10, online.size()); i++) {
            Player p = online.get(i);
            int vl = api.getPlayerData(p).getTotalViolations();
            sender.sendMessage(String.format("§e%d. §f%s §7- §c%d VL", i + 1, p.getName(), vl));
        }
        
        return true;
    }
}
```

---

## API Stability

The API is currently in **version 1.0**. Breaking changes will be:
- Announced in releases
- Documented in changelog
- Ideally avoided in minor versions

If you need features not documented here, open a GitHub issue.

---

*Next up: [Building from Source](Building-from-Source)*
