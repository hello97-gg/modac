# 🔍 Checks Overview

Here's a deep dive into every check — what it catches, how it works, and how to configure it.

---

## Movement Checks

### Speed

**What it catches:** Speed hacks, bunny hop abuse, ice sprint exploits, and any weird horizontal movement.

**How it works:**
- Tracks player velocity and position every tick
- Compares actual movement distance against predicted maximum
- Accounts for speed potions, ice, slime blocks, and other legitimate speed boosters
- Uses configurable tolerance to avoid false positives from lag spikes

**Config options:**
```yaml
speed:
  enabled: true
  max-vl: 30
  punish-command: "kick %player% &cUnfair Advantage - Speed"
  alert-vl: 5
  tolerance: 1.05  # 1.0 = strict, higher = lenient
```

**Common false positive causes:**
- Very high ping (increase tolerance)
- Slime block bouncing at weird angles
- Trident riptide (may need tolerance bump)

---

### Fly

**What it catches:** Flight hacks, high jump, air walking, and gravity manipulation.

**How it works:**
- Monitors airtime and vertical movement patterns
- Tracks expected fall speed vs actual fall speed
- Detects players hovering or moving upward without valid cause
- Accounts for elytra, levitation effects, and shulker bullets

**Config options:**
```yaml
fly:
  enabled: true
  max-vl: 25
  punish-command: "kick %player% &cUnfair Advantage - Flight"
  alert-vl: 3
  max-airtime: 40  # Ticks in air before flag
```

**Common false positive causes:**
- Lag spikes causing rubberbanding
- Elytra landing awkwardness
- Tridents with riptide

---

### NoFall

**What it catches:** Players spoofing ground state to avoid fall damage.

**How it works:**
- Compares client-reported ground state with server-calculated position
- Detects when players claim to be on ground while actually falling
- Flags impossible damage mitigation patterns

**Config options:**
```yaml
nofall:
  enabled: true
  max-vl: 20
  punish-command: "kick %player% &cUnfair Advantage - NoFall"
  alert-vl: 5
```

**Common false positive causes:**
- Slime block bounces (should be fine, but watch for edge cases)
- Falling into water at weird angles

---

### Jesus

**What it catches:** Walking on water or lava.

**How it works:**
- Analyzes player bounding box against liquid blocks
- Detects standing/rowing on water without a boat
- Accounts for frost walker enchantment

**Config options:**
```yaml
jesus:
  enabled: true
  max-vl: 20
  punish-command: "kick %player% &cUnfair Advantage - Jesus"
  alert-vl: 5
```

**Common false positive causes:**
- Frost walker boots (legit)
- Lag causing position desync

---

### ElytraFly

**What it catches:** Infinite elytra flight, firework boost abuse.

**How it works:**
- Monitors elytra speed and flight duration
- Detects speeds exceeding what's possible with firework rockets
- Flags hovering patterns that shouldn't exist

**Config options:**
```yaml
elytra:
  enabled: true
  max-vl: 20
  punish-command: "kick %player% &cUnfair Advantage - ElytraFly"
  alert-vl: 5
  max-speed: 3.5  # Blocks per tick limit
```

**Common false positive causes:**
- High-altitude firework chaining
- Trident riptide + elytra combo

---

### NoSlow

**What it catches:** Moving at full speed while eating, blocking, or using a bow.

**How it works:**
- Checks player speed during item use animations
- Compares against expected slowed movement
- Flags when walking/running speed is maintained during actions that should slow you

**Config options:**
```yaml
noslow:
  enabled: true
  max-vl: 15
  punish-command: "kick %player% &cUnfair Advantage - NoSlow"
  alert-vl: 5
```

**Common false positive causes:**
- Very specific tick-perfect actions (rare)
- Packet lag causing state desync

---

## Combat Checks

### Reach

**What it catches:** Players hitting from impossible distances.

**How it works:**
- 3D raytracing from attacker to victim
- Calculates exact hit distance considering hitbox expansion
- Adds ping compensation for legitimate latency
- Flags distances exceeding configured maximum

**Config options:**
```yaml
reach:
  enabled: true
  max-vl: 20
  punish-command: "kick %player% &cUnfair Advantage - Reach"
  alert-vl: 5
  max-reach: 3.1  # Vanilla is 3.0, we allow slight tolerance
  ping-compensation: 0.05  # Per 50ms of ping
```

**Common false positive causes:**
- Very high ping (increase ping-compensation)
- Players with speed effect hitting while moving fast
- Hitbox desync in 1.9+ combat

---

### KillAura

**What it catches:** Auto-attack hacks that target multiple entities or hit through walls.

**How it works:**
- Analyzes attack angle vs player look direction
- Detects inhuman consistency in attack timing
- Flags when players hit entities they aren't looking at
- Monitors for switch patterns (rapid target switching)

**Config options:**
```yaml
killaura:
  enabled: true
  max-vl: 25
  punish-command: "kick %player% &cUnfair Advantage - KillAura"
  alert-vl: 5
```

**Common false positive causes:**
- F5 mode players (looking behind while hitting)
- Packet lag causing look desync

---

### AutoClicker

**What it catches:** Automated clicking with robotic patterns.

**How it works:**
- Tracks click timestamps and calculates intervals
- Uses standard deviation analysis to find non-human patterns
- Humans have natural variation; bots don't
- Flags when deviation falls below threshold

**Config options:**
```yaml
autoclicker:
  enabled: true
  max-vl: 20
  punish-command: "kick %player% &cUnfair Advantage - AutoClicker"
  alert-vl: 10
  max-cps: 20  # Maximum clicks per second
  min-deviation: 15.0  # Lower = more robotic
```

**Common false positive causes:**
- Players with jitter clicking (high CPS, but still human variation)
- Butterfly clicking (still has natural variation)
- Consider raising max-cps if your players are competitive PvPers

---

### Velocity

**What it catches:** Anti-knockback hacks.

**How it works:**
- Compares expected knockback vs actual player movement
- Tracks percentage of knockback taken
- Flags when player consistently takes less than expected

**Config options:**
```yaml
velocity:
  enabled: true
  max-vl: 15
  punish-command: "kick %player% &cUnfair Advantage - Velocity"
  alert-vl: 5
  min-percentage: 75.0  # Must take at least 75% of KB
```

**Common false positive causes:**
- Players blocking with shields
- Players sprint-hitting (reduces KB taken)
- Ping causing movement desync

---

### Criticals

**What it catches:** Players forcing critical hits through micro-movements.

**How it works:**
- Detects impossible jump patterns used to force crits
- Flags when players "jump" without actually jumping
- Catches packet-based crit forcing

**Config options:**
```yaml
criticals:
  enabled: true
  max-vl: 15
  punish-command: "kick %player% &cUnfair Advantage - Criticals"
  alert-vl: 5
```

**Common false positive causes:**
- Slime block bouncing
- Piston-launched players
- Falling from heights while attacking

---

## Player/World Checks

### Timer

**What it catches:** Game speed manipulation (running faster than 20 TPS).

**How it works:**
- Counts packets received per second
- Compares against expected 20 TPS
- Flags when player is sending packets faster than possible

**Config options:**
```yaml
timer:
  enabled: true
  max-vl: 20
  punish-command: "kick %player% &cUnfair Advantage - Timer"
  alert-vl: 10
  max-speed-percentage: 110.0  # Allow 10% over 20 TPS
```

**Common false positive causes:**
- Server lag spikes causing packet bunching
- Players rejoining (temporary spike)

---

### BadPackets

**What it catches:** Impossible packet sequences and crash exploits.

**How it works:**
- Validates packet order and structure
- Detects packets that shouldn't exist
- Blocks known crash packet exploits
- Flags impossible state combinations

**Config options:**
```yaml
badpackets:
  enabled: true
  max-vl: 10
  punish-command: "kick %player% &cUnfair Advantage - BadPackets"
  alert-vl: 1
```

**Common false positive causes:**
- Very rarely false positives
- If you see them, check client mods

---

### Inventory

**What it catches:** Inventory manipulation while doing impossible actions.

**How it works:**
- Detects item movement while sprinting/flying
- Flags inventory interaction during combat
- Catches auto-armor and instant-eat hacks

**Config options:**
```yaml
inventory:
  enabled: true
  max-vl: 15
  punish-command: "kick %player% &cUnfair Advantage - Inventory"
  alert-vl: 5
```

**Common false positive causes:**
- Lag causing state desync
- Very fast human actions (rare)

---

### Scaffold

**What it catches:** Auto-bridging and tower hacks.

**How it works:**
- Analyzes block placement angles
- Detects placements player isn't looking at
- Flags impossible placement speeds
- Monitors crouch/uncrouch patterns

**Config options:**
```yaml
scaffold:
  enabled: true
  max-vl: 20
  punish-command: "kick %player% &cUnfair Advantage - Scaffold"
  alert-vl: 5
  min-angle: 70.0  # Degrees between look direction and placed block
```

**Common false positive causes:**
- Very skilled bridgers (breezily, god bridging)
- Consider raising min-angle if false positives occur

---

### FastBreak

**What it catches:** Speed mine, nuker, and fast breaking exploits.

**How it works:**
- Calculates expected break time based on block hardness
- Factors in tool tier and efficiency enchantments
- Flags when blocks break faster than mathematically possible
- Accounts for haste effects

**Config options:**
```yaml
fastbreak:
  enabled: true
  max-vl: 15
  punish-command: "kick %player% &cUnfair Advantage - FastBreak"
  alert-vl: 5
```

**Common false positive causes:**
- Haste II beacon + efficiency V pickaxe
- Very low latency connections
- Consider adding tolerance for dedicated mining servers

---

## Tuning Tips

**If you're getting too many false positives:**
1. Increase `tolerance` values
2. Raise `max-vl` thresholds
3. Increase `alert-vl` to reduce spam
4. Add ping compensation where available

**If cheaters aren't getting caught:**
1. Lower `tolerance` values
2. Decrease `max-vl` for faster punishment
3. Lower `alert-vl` to catch early flags
4. Check if the specific check is even enabled

---

*Next up: [Commands Reference](Commands-Reference)*
