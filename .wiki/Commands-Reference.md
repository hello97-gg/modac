# 📋 Commands Reference

Complete reference for every command in ModMC AntiCheat.

---

## Command Overview

All commands use `/modac` as the base. You can also use `/anticheat` as an alias.

---

## Alert Commands

### `/modac alerts`

**Permission:** `modac.alerts`

Toggle your personal violation alert notifications on/off.

**Usage:**
```
/modac alerts
```

**What happens:**
- Alerts ON → You'll see all violation flags from players in chat
- Alerts OFF → You won't see anything

**When to use:** 
- Turn on if you're actively monitoring the server
- Turn off if you're busy and don't want chat spam
- Mods should keep this on during active play sessions

**Example output:**
```
[ModMC] Alerts toggled: ON
[ModMC] You will now receive violation alerts.
```

---

### `/modac verbose`

**Permission:** `modac.verbose`

Enable developer-mode verbose logging for debugging.

**Usage:**
```
/modac verbose
```

**What happens:**
- Shows raw check data in chat
- Displays VL changes in real-time
- Includes debug information for each flag

**When to use:**
- Debugging false positives
- Investigating suspicious flags
- Testing check configurations

**Warning:** This is **spammy**. Only use when actively debugging.

**Example output:**
```
[ModMC] Verbose mode: ON
[ModMC] [Speed] Player123 moved 0.92 blocks (expected: 0.86) - VL+1
[ModMC] [Speed] Player123 moved 0.94 blocks (expected: 0.86) - VL+1
```

---

### `/modac notify <player>`

**Permission:** `modac.alerts`

Get a quick snapshot of a player's current status.

**Usage:**
```
/modac notify Player123
```

**What you see:**
- Player name
- Ping
- Health
- Game mode
- Current violation levels (top checks)
- Online time

**Example output:**
```
[ModMC] === Player123 ===
[ModMC] Ping: 45ms | Health: 18/20 | Gamemode: Survival
[ModMC] Violations: Speed(8), Reach(3), Timer(0)
[ModMC] Online: 24 minutes
```

---

### `/modac monitor <player>`

**Permission:** `modac.alerts`

Start live monitoring of a specific player's flags.

**Usage:**
```
/modac monitor Player123
```

**What happens:**
- Every flag from that player appears in your chat with `[⊕]` prefix
- Continues until you stop monitoring or disconnect
- Use `/modac monitor` again to stop

**When to use:**
- Investigating a suspected cheater
- Watching a player who's close to ban threshold
- Staff spectating without being obvious

**Example output:**
```
[⊕] Player123 flagged Speed (VL: 12)
[⊕] Player123 flagged Reach (VL: 5, distance: 3.4)
[⊕] Player123 flagged KillAura (VL: 8)
```

---

### `/modac top`

**Permission:** `modac.alerts`

Show the top 10 most suspicious online players.

**Usage:**
```
/modac top
```

**What you see:**
- Ranked list of players by total violations
- Current VL for each
- Top checks triggering

**Example output:**
```
[ModMC] === Top 10 Suspicious Players ===
[ModMC] 1. HackerDude - Total VL: 87 (Speed: 42, Fly: 28, Reach: 17)
[ModMC] 2. SusPlayer - Total VL: 45 (KillAura: 30, Reach: 15)
[ModMC] 3. MaybeHacker - Total VL: 23 (Timer: 23)
...
```

---

## Punishment Commands

### `/modac kick <player>`

**Permission:** `modac.punish`

Kick a player with AntiCheat branding.

**Usage:**
```
/modac kick Player123
```

**What happens:**
- Player is kicked with configured kick message
- Discord webhook notified (if enabled)
- Staff with `modac.alerts` see broadcast
- Logged to database

**Example output:**
```
[ModMC] Player123 has been kicked for unfair advantage.
```

---

### `/modac ban <player>`

**Permission:** `modac.punish`

Ban a player with AntiCheat branding.

**Usage:**
```
/modac ban Player123
```

**What happens:**
- Player is banned
- Discord webhook notified (if enabled)
- Staff with `modac.alerts` see broadcast
- Logged to database

**Note:** This uses your server's ban system. Make sure your ban plugin handles it properly.

---

## Information Commands

### `/modac history <player>`

**Permission:** `modac.info`

View a player's violation history from the database.

**Usage:**
```
/modac history Player123
```

**What you see:**
- Last 10 violations
- Check type, VL, and timestamp
- Total violation count

**Example output:**
```
[ModMC] === Player123 History ===
[ModMC] Total Violations: 23
[ModMC] Recent:
[ModMC] - Speed (VL: 30) - 2 hours ago [KICKED]
[ModMC] - Reach (VL: 18) - 5 hours ago
[ModMC] - KillAura (VL: 12) - yesterday
...
```

---

### `/modac status`

**Permission:** `modac.alerts`

Check plugin health status.

**Usage:**
```
/modac status
```

**What you see:**
- Plugin running state
- Database connection status
- Checks loaded count
- Discord webhook status

**Example output:**
```
[ModMC] === Plugin Status ===
[ModMC] Plugin: ✓ Running (v1.0.0)
[ModMC] Database: ✓ Connected (45,232 records)
[ModMC] Checks: 16 loaded
[ModMC] Discord: ✓ Connected
```

---

## Admin Commands

### `/modac reload`

**Permission:** `modac.reload`

Reload the configuration file.

**Usage:**
```
/modac reload
```

**What happens:**
- Config is reloaded from disk
- All changes applied immediately
- No server restart needed

**Example output:**
```
[ModMC] Configuration reloaded.
[ModMC] 16 checks loaded.
```

---

### `/modac testwebhook`

**Permission:** `modac.reload`

Send a test message to your Discord webhook.

**Usage:**
```
/modac testwebhook
```

**What happens:**
- Sends a test embed to configured webhook
- Confirms webhook is working
- Shows in your Discord channel

**Example output:**
```
[ModMC] Test webhook sent successfully!
[ModMC] Check your Discord channel.
```

If it fails:
```
[ModMC] ✗ Webhook test failed: Invalid webhook URL
```

---

## Permission Summary

| Permission | What it allows |
|------------|----------------|
| `modac.alerts` | See violation alerts, use notify/monitor/top/status |
| `modac.verbose` | Use verbose debug mode |
| `modac.info` | View player history |
| `modac.punish` | Kick and ban players |
| `modac.reload` | Reload config, test webhook |
| `modac.*` | All permissions (ops get this by default) |

---

## Quick Reference Card

```
/modac alerts           - Toggle alerts on/off
/modac verbose          - Debug mode
/modac notify <player>  - Quick player stats
/modac monitor <player> - Live flag feed
/modac history <player> - Violation history
/modac top              - Top 10 suspicious players
/modac status           - Plugin health check
/modac kick <player>    - Kick player
/modac ban <player>     - Ban player
/modac reload           - Reload config
/modac testwebhook      - Test Discord webhook
```

---

*Next up: [Discord Setup](Discord-Setup)*
