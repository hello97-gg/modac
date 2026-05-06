# ModMC AntiCheat

A lightweight, zero-dependency, and highly accurate Minecraft anti-cheat plugin built on top of [PacketEvents](https://github.com/retrooper/packetevents). ModMC AntiCheat uses advanced mathematics and packet analysis to catch modern hack clients while maintaining zero false positives for legitimate players.

## Features

- **16 Highly Accurate Checks** across Movement, Combat, and Player categories.
- **Discord Webhook Integration:** Real-time violation alerts and punishment logs sent straight to your Discord server.
- **In-Game Staff Monitoring:** Tap into live player violation streams using `/modac monitor`.
- **Zero-Dependency:** Runs completely standalone (aside from PacketEvents API which is shaded/relocated).
- **SQLite Database:** Asynchronous violation logging and historical lookups.
- **1.8 to 1.21.3 Support:** Compatible across all modern versions thanks to PacketEvents.

## Checks Included

### 🏃 Movement
- **Speed** - Prevents horizontal movement faster than vanilla speed.
- **Fly** - Detects unnatural airtime and hovering.
- **NoFall** - Prevents spoofing ground state to avoid fall damage.
- **Jesus** - Prevents walking on liquids (water/lava).
- **ElytraFly** - Limits elytra boost speeds and prevents infinite flight.
- **NoSlow** - Prevents sprinting while eating, blocking, or drawing a bow.

### ⚔️ Combat
- **Reach** - Advanced raytracing to prevent attacking from impossible distances.
- **KillAura** - Detects impossible attack angles and robotic consistency.
- **AutoClicker** - Detects inhuman clicking consistency using standard deviation.
- **Velocity** - Prevents players from ignoring server knockback (Anti-KB).
- **Criticals** - Catches micro-jump critical hit hacks.

### 👤 Player
- **Timer** - Prevents speeding up the client tick rate.
- **BadPackets** - Protects against malformed or impossible packet sequences.
- **Inventory** - Prevents moving items while sprinting or clicking outside standard UI rules.
- **Scaffold** - Prevents impossible block placement patterns and bridging.
- **FastBreak** - Ensures blocks aren't broken faster than mathematically possible.

## Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/modac alerts` | `modac.alerts` | Toggle receiving violation alerts. |
| `/modac verbose` | `modac.verbose` | Toggle verbose development data. |
| `/modac notify <player>` | `modac.alerts` | Show a player's live stats, ping, health, and current violations. |
| `/modac monitor <player>` | `modac.alerts` | Live-stream a specific player's violations to your chat. |
| `/modac history <player>` | `modac.info` | View a player's past violation logs from the database. |
| `/modac top` | `modac.alerts` | Show the top 10 most flagged online players. |
| `/modac status` | `modac.alerts` | View the anti-cheat system status. |
| `/modac kick <player> [reason]` | `modac.punish` | Kick a player and alert Discord/Staff. |
| `/modac ban <player> [reason]` | `modac.punish` | Ban a player and alert Discord/Staff. |
| `/modac reload` | `modac.reload` | Reload the config file. |

## Building

This project uses Gradle. To build the plugin:
```bash
./gradlew build
```
The compiled jar will be in `build/libs/ModMC-AntiCheat-1.0.0.jar`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
