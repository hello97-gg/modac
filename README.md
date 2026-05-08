# ModMC AntiCheat 🛡️

> *Finally, an anti-cheat that doesn't make you want to pull your hair out.*

Look, I get it. You've probably tried like five different anti-cheats by now. One was too heavy and tanked your TPS. Another had more false positives than a budget smoke detector. The "premium" one required seventeen dependencies and a PhD to configure.

So yeah, I built this instead.

**ModMC AntiCheat** is a lightweight, stupid-fast anti-cheat that runs natively on [PacketEvents](https://github.com/retrooper/packetevents). No bloat, no nonsense. It uses strict math and packet analysis to catch modern hack clients without punishing your legit players for having the audacity to play on WiFi instead of ethernet.

**Works on:** 1.8 → 1.21.3 (yeah, it covers basically everything)

---

## So... why should you care?

I'm not gonna give you the corporate pitch. Here's the real deal:

**Actually plug-and-play** — PacketEvents is shaded in. No hunting down dependencies, no version conflicts, none of that mess. Drop the jar, restart, done. You could literally set this up during a bathroom break.

**Discord alerts that don't suck** — Get real-time cheat alerts and punishment logs piped straight to your Discord. No more alt-tabbing to check logs every 5 minutes.

**Live monitoring built in** — Type `/modac monitor <player>` and watch their violation flags roll in real-time. It's like having a spectator mode for cheaters.

**Database that won't lag you** — SQLite runs async so you can pull up a player's history from two weeks ago without tanking your main thread.

**Config that makes sense** — Want to ban instead of kick? Change one line. Want to tweak tolerance on specific checks? Go for it. It's your server, not mine.

---

## What does it actually catch?

16 checks covering the stuff you actually care about. Not gonna list every edge case, but here's the rundown:

### Movement
- **Speed** — Horizontal math checks. Bunny hop, ice sprint, speed pots, the works.
- **Fly** — Tracks airtime and vertical movement. Catches packet flight and impossible jumps.
- **NoFall** — Stops the whole "fake ground state" exploit to avoid fall damage.
- **Jesus** — No walking on water unless you're actually supposed to.
- **ElytraFly** — Caps infinite flight and insane firework boost abuse.
- **NoSlow** — Full sprint while eating? Yeah no.

### Combat
- **Reach** — 3D raytracing. No hitting from 6 blocks away.
- **KillAura** — Attack angle analysis + consistency checks.
- **AutoClicker** — Standard deviation analysis. Robotic clicks get flagged.
- **Velocity** — Anti-KB. Take your knockback like everyone else.
- **Criticals** — Catches those weird micro-jumps people use to force crits.

### Player/World
- **Timer** — Game speed manipulation. No running at 2x speed.
- **BadPackets** — Impossible packet orders, crash exploits, that kind of thing.
- **Inventory** — No moving items while sprinting or doing sketchy stuff with open GUIs.
- **Scaffold** — Block placement analysis for bridging hacks.
- **FastBreak** — Math-based check using block hardness + tool tier. Speed mine gets caught.

---

## � Installation

Seriously, it's three steps:

1. **Download** — Grab the `.jar` from [Releases](https://github.com/hello97-gg/modac/releases) (or build from source, your call)
2. **Drop** — Put it in your `plugins/` folder
3. **Restart** — That's literally it

### After installing:

The config lives at `plugins/ModMC-AntiCheat/config.yml`. Open it up if you want to:
- Change punishment actions (kick vs ban)
- Set up Discord webhooks
- Adjust VL thresholds per check
- Tweak tolerance values

Run `/modac reload` after making changes and you're golden.

---

## 🎮 Commands & Permissions

Here's your cheat sheet:

| Command | Permission | What it does |
|--------|------------|--------------|
| `/modac alerts` | `modac.alerts` | Toggle your violation notifications |
| `/modac verbose` | `modac.verbose` | Dev mode — raw data for debugging |
| `/modac notify <player>` | `modac.alerts` | Quick stats: ping, health, violations |
| `/modac monitor <player>` | `modac.alerts` | Live flag feed for specific player |
| `/modac history <player>` | `modac.info` | Pull up their database record |
| `/modac top` | `modac.alerts` | Top 10 suspicious players online |
| `/modac status` | `modac.alerts` | Plugin health check |
| `/modac kick <player>` | `modac.punish` | Kick + Discord alert |
| `/modac ban <player>` | `modac.punish` | Ban + Discord alert |
| `/modac reload` | `modac.reload` | Reload config |
| `/modac testwebhook` | `modac.reload` | Test your Discord setup |

**Pro tip:** Give `modac.alerts` to your mods. They'll see flags pop up in chat automatically.

---

## 📚 Full Documentation

Need more details? Check out the [Wiki](https://github.com/hello97-gg/modac/.wiki) for:
- Detailed check explanations
- Configuration deep-dive
- False positive troubleshooting
- Discord webhook setup guide
- API for developers

---

## 🔨 Building from Source

If you're the type who likes to compile things yourself:

```bash
# Clone it
git clone https://github.com/hello97-gg/modac.git

# Build it
# Windows:
.\gradlew.bat build

# Mac/Linux:
./gradlew build
```

Your compiled jar will be in `build/libs/ModMC-AntiCheat-1.0.0.jar`

---

## 🤝 Contributing

Found a bug? Have an idea? Open an issue or submit a PR. I actually read them.

This is open source for a reason — if you can make it better, go for it.

---

## 📜 License

GPL v3.0. Use it, mod it, learn from it. Just keep it open source. Check [LICENSE](LICENSE) for the details.

---

## 💬 Questions? Issues?

- **Bugs/Features:** [Open an issue](https://github.com/hello97-gg/modac/issues)
- **Discord:** *Coming soon*
- **SpigotMC:** *Coming soon*

---

**If this saved your server from cheaters, consider leaving a ⭐ — it helps others find it too.**
