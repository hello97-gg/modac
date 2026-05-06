# 📥 Installation Guide

This should take you about 2 minutes. Let's go.

## Requirements

Before you start, make sure you have:

- **Server version:** 1.8–1.21.3 (Spigot, Paper, or any fork)
- **Java:** 8 or higher
- **Nothing else** — seriously, no dependencies needed

## Step 1: Download

Head over to the [Releases page](https://github.com/hello97-gg/modac/releases) and grab the latest `.jar` file.

Alternatively, if you want the bleeding edge version:
1. Clone the repo
2. Run `./gradlew build` (or `.\gradlew.bat build` on Windows)
3. Grab the jar from `build/libs/`

## Step 2: Install

Drop the `.jar` file into your server's `plugins/` folder.

```
your-server/
├── server.jar
├── plugins/
│   └── ModMC-AntiCheat-1.0.0.jar   <-- put it here
├── world/
└── ...
```

## Step 3: Restart

Restart your server. Don't use a plugin manager to load it — just do a proper restart.

On startup, you should see something like:

```
[ModMC-AntiCheat] Loading ModMC AntiCheat v1.0.0...
[ModMC-AntiCheat] Detected server version: 1.20.4
[ModMC-AntiCheat] Loaded 16 checks
[ModMC-AntiCheat] Database initialized successfully
[ModMC-AntiCheat] Enabled! Ready to catch some cheaters.
```

If you see errors, check the [Troubleshooting](#troubleshooting) section below.

## Step 4: Verify It's Working

Join your server and run:

```
/modac status
```

You should see something like:

```
[ModMC] Plugin Status: ✓ Running
[ModMC] Database: ✓ Connected
[ModMC] Checks Loaded: 16
[ModMC] Discord Webhook: ✗ Not configured
```

If everything shows green, you're good to go!

## Step 5: Configure (Optional)

The default config works fine for most servers, but you might want to:

1. Set up [Discord webhooks](Discord-Setup)
2. Adjust [punishment settings](Configuration-Guide#punishment-settings)
3. Tweak [individual checks](Checks-Overview)

Config location: `plugins/ModMC-AntiCheat/config.yml`

After editing, run `/modac reload` to apply changes.

## Post-Install Checklist

- [ ] Plugin loads without errors
- [ ] `/modac status` shows all systems green
- [ ] You have `modac.alerts` permission to see flags
- [ ] Test it by having a friend (or alt) try a hack
- [ ] (Optional) Discord webhook set up
- [ ] (Optional) Config tweaked to your liking

## Troubleshooting

### Plugin doesn't load

**Check your Java version:**
```
java -version
```

You need Java 8 or higher. If you're on 7, update it.

**Check server version:**
The plugin supports 1.8–1.21.3. If you're running something older or a weird fork, that might be the issue.

### "Unsupported class version" error

Your Java is too old. Update to at least Java 8.

### Plugin loads but nothing happens

Make sure:
1. You have the `modac.alerts` permission
2. Checks are enabled in config (`checks.<name>.enabled: true`)
3. You're actually testing with a hack, not just playing normally

### Still stuck?

Open an issue on [GitHub](https://github.com/hello97-gg/modac/issues) with:
- Server version
- Java version
- Full startup log
- What you were trying to do

---

*Next up: [Configuration Guide](Configuration-Guide)*
