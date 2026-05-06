# 💬 Discord Webhook Setup

Get real-time cheat alerts piped straight to your Discord server. Super useful for moderation.

---

## What You Get

When enabled, the plugin sends:
- Violation alerts (when players hit configured VL threshold)
- Kick notifications
- Ban notifications
- Customizable embed colors and formatting

---

## Step 1: Create a Webhook

1. Open your Discord server
2. Go to the channel where you want alerts (create a dedicated one like `#anticheat-logs`)
3. Click the **gear icon** (Edit Channel)
4. Go to **Integrations** → **Webhooks**
5. Click **New Webhook**
6. Name it something like "ModMC AntiCheat"
7. Copy the **Webhook URL** (looks like `https://discord.com/api/webhooks/123456789/abcdefg...`)

**Important:** Keep this URL secret. Anyone with it can send messages to your channel.

---

## Step 2: Configure the Plugin

Open `plugins/ModMC-AntiCheat/config.yml` and find the discord section:

```yaml
discord:
  enabled: true  # <-- Set to true
  webhook-url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_HERE"  # <-- Paste your URL
  min-vl: 10
  color: 16711680
  rate-limit: 30
```

### Understanding the options:

| Option | What it does |
|--------|--------------|
| `enabled` | Turn Discord integration on/off |
| `webhook-url` | Your webhook URL from step 1 |
| `min-vl` | Minimum VL before sending to Discord. Set higher to avoid spamming every small flag. |
| `color` | Embed color (decimal). Red = 16711680, Green = 65280, Blue = 255 |
| `rate-limit` | Seconds between messages per player. Prevents hitting Discord's rate limits. |

### Color picker:

Use a decimal color converter like [this one](https://www.shodor.org/stella2java/rgbint.html).

Common colors:
- Red: `16711680`
- Orange: `16744192`
- Yellow: `16776960`
- Green: `65280`
- Blue: `255`
- Purple: `8388736`

---

## Step 3: Apply Changes

Run in-game or from console:

```
/modac reload
```

---

## Step 4: Test It

Run:

```
/modac testwebhook
```

You should see a test message appear in your Discord channel.

**If it works:**
```
[ModMC] Test webhook sent successfully!
```

**If it fails:**
```
[ModMC] ✗ Webhook test failed: [error message]
```

Common failures:
- `Invalid webhook URL` — Double-check you copied the whole URL
- `Could not connect` — Check your server's internet connection
- `Unknown webhook` — Webhook was deleted, recreate it

---

## What Discord Alerts Look Like

### Violation Alert
```
[Embed]
Title: ⚠️ Cheat Detection
Color: Red
Fields:
  - Player: Player123
  - Check: Speed
  - VL: 15/30
  - Server: YourServer
  - Time: 2026-05-06 14:32:00
```

### Punishment Alert
```
[Embed]
Title: 🔨 Player Punished
Color: Red
Fields:
  - Player: Player123
  - Action: KICK
  - Reason: Unfair Advantage - Speed
  - Staff: Console
  - Time: 2026-05-06 14:35:00
```

---

## Rate Limiting

Discord has strict rate limits. The plugin handles this by:

1. **Per-player rate limiting** — Set via `rate-limit` config. If a player flags multiple times, only one message goes through per `rate-limit` seconds.

2. **Min VL threshold** — Set via `min-vl` config. Only flags reaching this VL go to Discord. Prevents spamming minor flags.

**Recommended settings:**
- **Active monitoring:** `min-vl: 5`, `rate-limit: 15`
- **Minimal spam:** `min-vl: 15`, `rate-limit: 60`
- **Everything:** `min-vl: 1`, `rate-limit: 10` (will be spammy)

---

## Multiple Channels

Want different alerts in different channels? You'd need to:
1. Create multiple webhooks (one per channel)
2. Modify the plugin or use a Discord bot to route messages

The plugin currently supports one webhook URL. For advanced routing, consider:
- Setting up a Discord bot that listens to the webhook
- Using Discord's built-in channel following features

---

## Security Notes

**Keep your webhook URL private:**
- Don't commit it to public repos
- Don't share it in public chats
- Regenerate it if compromised (via Discord settings)

**If webhook is leaked:**
1. Go to your Discord channel settings
2. Integrations → Webhooks
3. Delete the compromised webhook
4. Create a new one
5. Update config and reload

---

## Troubleshooting

### Webhook not sending

**Check list:**
- [ ] `enabled: true` in config
- [ ] Webhook URL is complete (starts with `https://discord.com/api/webhooks/`)
- [ ] `/modac reload` was run after changes
- [ ] Server has internet access
- [ ] Webhook wasn't deleted in Discord

### Rate limited by Discord

If you see rate limit errors:
1. Increase `rate-limit` in config
2. Increase `min-vl` threshold
3. Space out your alerts

### Webhook URL "invalid"

Make sure you copied the ENTIRE URL. It should be long and end with random characters.

---

## Example Configurations

### Minimal Spam (Recommended)
```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/..."
  min-vl: 15
  color: 16711680
  rate-limit: 60
```

### Active Monitoring
```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/..."
  min-vl: 5
  color: 16711680
  rate-limit: 15
```

### Everything (Debug/Testing)
```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/..."
  min-vl: 1
  color: 16711680
  rate-limit: 5
```

---

*Next up: [False Positive Troubleshooting](False-Positive-Troubleshooting)*
