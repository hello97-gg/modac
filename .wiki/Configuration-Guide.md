# ⚙️ Configuration Guide

The config file lives at `plugins/ModMC-AntiCheat/config.yml`. This guide breaks down every option so you know exactly what you're tweaking.

After making changes, **always run `/modac reload`** to apply them.

---

## General Settings

```yaml
prefix: "&8[&c&lModMC&8] &7"
```

This is what appears before chat messages from the plugin. Uses standard Minecraft color codes (`&` + color code).

**Example changes:**
- `"&6[AntiCheat]&f "` — Gold brackets, white text
- `"&c&lAC &7» "` — Bold red "AC" with gray arrow

---

## Alert Settings

```yaml
alerts:
  enabled: true
  console: true
  broadcast-permission: "modac.alerts"
```

| Option | What it does |
|--------|---------------|
| `enabled` | Master switch for alerts. Turn off to disable all notifications. |
| `console` | If `true`, logs alerts to console. Useful for server logs. |
| `broadcast-permission` | Players with this permission see in-game alerts. |

---

## Discord Integration

```yaml
discord:
  enabled: false
  webhook-url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL"
  min-vl: 10
  color: 16711680
  rate-limit: 30
```

| Option | What it does |
|--------|---------------|
| `enabled` | Master switch for Discord alerts. |
| `webhook-url` | Your Discord webhook URL. Get this from channel settings → Integrations → Webhooks. |
| `min-vl` | Minimum violation level before sending to Discord. Prevents spamming every tiny flag. |
| `color` | Embed color in decimal. Red = 16711680, Green = 65280, Blue = 255. Use a [color converter](https://www.shodor.org/stella2java/rgbint.html). |
| `rate-limit` | Seconds between Discord messages per player. Prevents webhook rate limits. |

**See [Discord Setup](Discord-Setup) for a full guide.**

---

## Database Settings

```yaml
database:
  enabled: true
  file: "violations.db"
  purge-days: 30
```

| Option | What it does |
|--------|---------------|
| `enabled` | Master switch for database logging. |
| `file` | Database filename. Stored in plugin folder. |
| `purge-days` | Auto-delete records older than X days. Set to `0` to keep everything forever. |

**Note:** Database runs asynchronously — it won't lag your server.

---

## Violation Level Decay

```yaml
vl-decay:
  interval: 30
  amount: 1
```

Violation levels decay over time. This prevents old flags from stacking up permanently.

| Option | What it does |
|--------|---------------|
| `interval` | How often (seconds) to decay VL. |
| `amount` | How much VL to remove per interval. |

**Example:** With `interval: 30` and `amount: 1`, a player with VL 10 will drop to VL 9 after 30 seconds of not flagging.

---

## Punishment Settings

```yaml
punishment:
  kick-message: "&c&lModMC AntiCheat\n&7You have been removed for unfair advantage.\n&8Contact staff if you believe this is an error."
  broadcast-punishments: true
  broadcast-format: "&8[&c&lModMC&8] &e%player% &7was punished for &c%check%&7."
```

| Option | What it does |
|--------|---------------|
| `kick-message` | Message shown to kicked players. Use `\n` for newlines. |
| `broadcast-punishments` | If `true`, announces punishments to staff. |
| `broadcast-format` | Format for broadcast. Use `%player%` and `%check%` placeholders. |

---

## Check Configuration

Every check follows this pattern:

```yaml
checks:
  <check-name>:
    enabled: true
    max-vl: 20
    punish-command: "kick %player% &cUnfair Advantage - <Check>"
    alert-vl: 5
    # ... check-specific options
```

### Universal Check Options

| Option | What it does |
|--------|---------------|
| `enabled` | Master switch for this check. |
| `max-vl` | Violations needed before punishment triggers. |
| `punish-command` | Command executed when `max-vl` is reached. `%player%` gets replaced with username. |
| `alert-vl` | VL threshold before alerts start showing. Prevents spam from minor flags. |

### Custom Punishment Commands

You're not limited to kicks. Here are some ideas:

```yaml
# Temp ban with Essentials
punish-command: "tempban %player% 1h Suspected cheating - %check%"

# Mute instead (if you want to let them appeal)
punish-command: "mute %player% 24h Auto-detected cheat: %check%"

# Ban with custom message
punish-command: "ban %player% &c&lCHEAT DETECTED\n&7Appeal at: yourserver.com/appeal"

# Just kick (default)
punish-command: "kick %player% &cUnfair Advantage detected"
```

### Check-Specific Options

Some checks have extra tuning knobs:

#### Speed
```yaml
tolerance: 1.05  # Higher = more lenient. 1.0 = strict.
```

#### Fly
```yaml
max-airtime: 40  # Max ticks in air before flagging
```

#### Elytra
```yaml
max-speed: 3.5  # Blocks per tick limit
```

#### Reach
```yaml
max-reach: 3.1  # Distance limit (vanilla is 3.0, we add tolerance)
ping-compensation: 0.05  # Added per 50ms of ping
```

#### AutoClicker
```yaml
max-cps: 20  # Maximum clicks per second
min-deviation: 15.0  # Lower = more robotic = flagged
```

#### Velocity
```yaml
min-percentage: 75.0  # Minimum knockback % player must take
```

#### Timer
```yaml
max-speed-percentage: 110.0  # Max deviation from 20 TPS
```

#### Scaffold
```yaml
min-angle: 70.0  # Minimum angle between look direction and block
```

---

## Best Practices

### For Competitive/Minigame Servers
- Lower `max-vl` values (faster punishment)
- Set `tolerance` closer to 1.0 (stricter)
- Enable Discord webhooks for moderation

### For Survival/SMP Servers
- Higher `max-vl` values (more chances)
- Increase `tolerance` slightly (reduce false positives)
- Consider longer `vl-decay` intervals

### For High-Ping Players
- Increase `ping-compensation` in reach check
- Raise `tolerance` on speed check
- Consider higher `max-vl` across the board

---

*Next up: [Checks Overview](Checks-Overview)*
