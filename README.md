# ModMC AntiCheat 🛡️

Hey everyone! 👋 Welcome to ModMC AntiCheat. 

I was tired of heavy, bloated anti-cheats that lagged the server or required five different dependencies just to run. So, I built this. It's a lightweight, extremely fast anti-cheat built natively on top of [PacketEvents](https://github.com/retrooper/packetevents). It uses strict math and packet analysis to catch modern hack clients instantly, while making sure legitimate players don't get rubberbanded or false-flagged just for having high ping.

If you're running a server anywhere from 1.8 to 1.21.3, you can just drop this in and it works out of the box.

---

## 🌟 Why use this?

- **Zero setup nightmare:** No dependencies required (PacketEvents is shaded in). Drag, drop, restart. 
- **Discord Integration built-in:** It sends live cheat alerts and punishment logs straight to your Discord webhook.
- **In-game Live Monitoring:** Staff can type `/modac monitor <player>` and literally watch a live feed of that specific player's violation flags stream into their chat.
- **Database support:** Uses SQLite to log everything asynchronously, so you can look up a player's history days later without lagging the main thread.
- **Highly Configurable:** Want to ban instead of kick? Just change a line in the config.

---

## 🛠️ What does it actually catch?

Right now, we have 16 highly tuned checks covering the most common modern client bypasses:

### 🏃 Movement
* **Speed:** Checks your horizontal math. Tries to catch bunny-hopping, speed pots, ice-sprinting, etc.
* **Fly:** Tracks airtime and weird vertical hovering. Instantly catches packet-based flight and high jumps.
* **NoFall:** Stops people from spoofing their ground state to avoid taking fall damage.
* **Jesus:** Checks your bounding box against surrounding blocks to prevent water/lava walking.
* **ElytraFly:** Limits infinite flight and crazy firework boosting.
* **NoSlow:** Catches players moving at full sprint speed while eating, blocking, or drawing a bow.

### ⚔️ Combat
* **Reach:** Strict 3D raytracing to stop players hitting from impossible distances.
* **KillAura:** Looks at attack angles and inhuman consistency.
* **AutoClicker:** Uses standard deviation to find robotic clicking patterns.
* **Velocity:** Anti-KB check. Forces players to actually take the knockback the server gives them.
* **Criticals:** Catches those sneaky 0.05-block micro-jumps used to force critical hits.

### 👤 Player & World
* **Timer:** Stops clients from speeding up their game loop to send packets faster than 20 TPS.
* **BadPackets:** Blocks impossible packet orders and crash exploits.
* **Inventory:** Prevents moving items around while sprinting or doing things you shouldn't be able to do with an open GUI.
* **Scaffold:** Analyzes block placement angles and speeds to catch modern bridging hacks.
* **FastBreak:** Mathematical check based on block hardness, tool, and enchantments to stop SpeedMine/Nuker.

---

## 🚀 Setup Guide

1. Grab the latest `.jar` from the releases page (or build it yourself, see below).
2. Drop it into your server's `plugins/` folder.
3. Restart your server.
4. Open `plugins/ModMC-AntiCheat/config.yml` to customize your punishments, VL limits, or setup your Discord Webhook.
5. Do `/modac reload` in-game. Boom. Done.

---

## 💻 Commands & Permissions

If you're an admin, you'll mainly be using these:

| Command | Perm Node | What it does |
|---|---|---|
| `/modac alerts` | `modac.alerts` | Turns your live violation notifications on or off. |
| `/modac verbose` | `modac.verbose` | Turns on dev-level raw data spam (good for debugging false flags). |
| `/modac notify <player>` | `modac.alerts` | Pulls up a quick stat sheet: ping, health, gamemode, and active violations. |
| `/modac monitor <player>` | `modac.alerts` | Tap into a player. Their specific flags will show up in your chat with a `[⊕]` prefix. |
| `/modac history <player>` | `modac.info` | View a player's past hacking logs from the database. |
| `/modac top` | `modac.alerts` | See the top 10 most suspicious online players right now. |
| `/modac status` | `modac.alerts` | Check if the plugin/database/webhook are running healthy. |
| `/modac kick <player>` | `modac.punish` | Kicks them and alerts Discord + staff. |
| `/modac ban <player>` | `modac.punish` | Bans them and alerts Discord + staff. |
| `/modac reload` | `modac.reload` | Reloads the config. |
| `/modac testwebhook` | `modac.reload` | Sends a test ping to your Discord channel. |

---

## ⚙️ For Developers (How to build)

We use Gradle. If you want to fork this or build it from source yourself, it's super easy.

1. Clone the repo: `git clone https://github.com/hello97-gg/modac.git`
2. Run the Gradle wrapper:
   - Windows: `.\gradlew.bat build`
   - Mac/Linux: `./gradlew build`
3. Your fresh compiled plugin will be sitting in `build/libs/ModMC-AntiCheat-1.0.0.jar`.

---

## 📜 License

This project is totally open-source and licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for all the boring legal details, but basically: feel free to use it, modify it, and learn from it, just keep it open source!
