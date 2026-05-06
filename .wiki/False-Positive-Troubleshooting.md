# 🐛 False Positive Troubleshooting

So a legit player got flagged. Annoying, but usually fixable. Here's how to diagnose and fix false positives.

---

## First: Is It Actually a False Positive?

Before assuming it's a bug, verify:

1. **Check the player's history** — `/modac history <player>` — Are they consistently flagging or just once?
2. **Watch them in monitor mode** — `/modac monitor <player>` — See the actual flags in real-time
3. **Consider their situation** — High ping? Using a VPN? Playing on mobile?
4. **Check what they were doing** — Lag spikes can cause weird movement patterns

**Legit false positives usually:**
- Happen inconsistently
- Occur during lag spikes or high activity
- Affect players with high ping or poor connections

**Actual cheaters usually:**
- Flag consistently over time
- Hit multiple different checks
- Have patterns (always flagging the same checks)

---

## Common False Positive Causes

### High Ping

High latency causes position desync between client and server. The server thinks the player is somewhere they're not.

**Symptoms:**
- Speed flags during normal movement
- Fly flags when jumping
- Reach flags in combat

**Solutions:**
- Increase `tolerance` in speed config: `tolerance: 1.1` or higher
- Increase `ping-compensation` in reach config: `ping-compensation: 0.1`
- Raise `max-vl` thresholds

### Server Lag

When the server is lagging (TPS < 19), packet timing gets weird.

**Symptoms:**
- Timer flags (server thinks player is sending too fast)
- Movement desync flags

**Solutions:**
- Increase `max-speed-percentage` in timer config: `max-speed-percentage: 120.0`
- Fix your server lag (optimize plugins, increase resources)

### Very Competitive Players

Some legit players are just really good. Jitter clicking, breezily bridging, etc.

**Symptoms:**
- AutoClicker flags for competitive PvPers
- Scaffold flags for skilled bridgers

**Solutions:**
- Raise `max-cps` in autoclicker config: `max-cps: 22`
- Raise `min-deviation` to allow less variation: `min-deviation: 20.0`
- Increase `min-angle` in scaffold config: `min-angle: 80.0`

### Modded Clients

Some legitimate client mods (OptiFine, Lunar, Badlion) can cause edge-case flags.

**Symptoms:**
- BadPackets flags
- Inventory flags
- Weird movement flags

**Solutions:**
- These are usually client-specific. Check for known issues.
- If the client is approved/whitelisted, you may need to adjust tolerance.
- Consider exempting players using approved clients (manual process).

---

## Check-Specific Troubleshooting

### Speed False Positives

**Causes:**
- High ping
- Slime block bouncing
- Ice/soul sand combinations
- Speed potion + lag

**Fix:**
```yaml
speed:
  tolerance: 1.1  # Increase from 1.05
  alert-vl: 8     # Increase to reduce spam
```

### Fly False Positives

**Causes:**
- Lag spikes during jumps
- Elytra awkwardness
- Trident riptide
- Levitation effects

**Fix:**
```yaml
fly:
  max-airtime: 50  # Increase from 40
  alert-vl: 5      # Increase threshold
```

### Reach False Positives

**Causes:**
- High ping (the big one)
- Speed effects during combat
- 1.9+ combat hitbox weirdness

**Fix:**
```yaml
reach:
  max-reach: 3.2           # Slight increase
  ping-compensation: 0.1   # Double the compensation
  alert-vl: 8              # Higher threshold
```

### AutoClicker False Positives

**Causes:**
- Jitter clicking
- Butterfly clicking
- Very competitive PvP players

**Fix:**
```yaml
autoclicker:
  max-cps: 22              # Allow higher CPS
  min-deviation: 20.0      # Higher deviation required
  alert-vl: 15             # Don't alert until higher VL
```

### Velocity False Positives

**Causes:**
- Shield blocking (reduces KB)
- Sprint-hitting (reduces KB)
- Packet lag during combat

**Fix:**
```yaml
velocity:
  min-percentage: 65.0     # Lower threshold
  alert-vl: 8              # Higher alert threshold
```

### Scaffold False Positives

**Causes:**
- Breezily bridging
- God bridging
- Ninja bridging
- Very skilled players

**Fix:**
```yaml
scaffold:
  min-angle: 80.0          # Higher angle tolerance
  alert-vl: 8              # Higher alert threshold
  max-vl: 30               # More chances before punishment
```

### Timer False Positives

**Causes:**
- Server lag spikes
- Player rejoining (temporary spike)
- Network issues

**Fix:**
```yaml
timer:
  max-speed-percentage: 120.0  # More tolerance
  alert-vl: 15                 # Higher alert threshold
```

---

## General Solutions

If you're getting false positives across multiple checks:

### Increase Alert Thresholds

```yaml
alert-vl: 10  # For most checks
```

This doesn't reduce flags, but it reduces the spam you see.

### Increase Max VL

```yaml
max-vl: 40  # For most checks
```

Gives players more chances before punishment.

### Increase Decay

```yaml
vl-decay:
  interval: 20
  amount: 2
```

VL drops faster, so occasional flags don't stack up.

---

## Diagnosing the Root Cause

Use verbose mode to see what's actually happening:

```
/modac verbose
```

Then watch the chat for raw flag data. You'll see things like:

```
[Speed] Player moved 0.92 blocks (expected: 0.86) - VL+1
[Speed] Player moved 0.94 blocks (expected: 0.86) - VL+1
[Reach] Hit distance: 3.4 (max: 3.1) - VL+1
```

This tells you:
- What the actual values are
- How far off they are from expected
- Whether it's consistent or sporadic

---

## When Nothing Works

If a player is consistently flagged but you're sure they're legit:

1. **Whitelist them manually** — There's no built-in exempt system, but you could:
   - Give them a permission that bypasses checks (requires code modification)
   - Temporarily disable the problematic check for everyone

2. **Lower your standards** — Increase tolerance across the board

3. **Report the issue** — Open a GitHub issue with:
   - Server version
   - What was happening when flagged
   - Verbose logs
   - Player's ping and connection info

---

## Balancing Act

There's always a tradeoff between:
- **Catching cheaters** (strict settings)
- **Not annoying legit players** (lenient settings)

For most servers:
- Keep default settings
- Adjust based on your player base
- Competitive PvP servers = stricter
- Casual survival servers = more lenient

---

## Quick Checklist

When a player reports false positives:

- [ ] Check their history: `/modac history <player>`
- [ ] Monitor them: `/modac monitor <player>`
- [ ] Check their ping (high ping = more tolerance needed)
- [ ] Check server TPS (low TPS = adjust timer settings)
- [ ] Ask what they were doing when flagged
- [ ] Adjust specific check config if needed
- [ ] Run `/modac reload` after changes
- [ ] Test and monitor for improvement

---

*Next up: [API Documentation](API-Documentation)*
