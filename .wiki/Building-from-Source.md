# 🔨 Building from Source

Want to compile ModMC AntiCheat yourself? Maybe contribute? Here's how.

---

## Prerequisites

You'll need:

- **Java 8 or higher** (Java 17 recommended)
- **Git** (to clone the repo)
- **Internet connection** (Gradle needs to download dependencies)

---

## Step 1: Clone the Repository

```bash
git clone https://github.com/hello97-gg/modac.git
cd modac
```

This creates a local copy of the project.

---

## Step 2: Build with Gradle

### Windows

```powershell
.\gradlew.bat build
```

### Mac/Linux

```bash
./gradlew build
```

**First run takes a while** — Gradle downloads dependencies and sets up the build environment. Subsequent builds are faster.

---

## Step 3: Find Your Jar

After a successful build, you'll find the compiled plugin at:

```
build/libs/ModMC-AntiCheat-1.0.0.jar
```

Copy this to your server's `plugins/` folder.

---

## Build Variants

### Standard Build

```bash
./gradlew build
```

Produces the normal plugin jar with all dependencies shaded in.

### Clean Build

```bash
./gradlew clean build
```

Cleans previous build artifacts first. Useful if you're having weird issues.

### Skip Tests

```bash
./gradlew build -x test
```

Skips running tests. Faster, but not recommended.

---

## Project Structure

```
modac/
├── build.gradle.kts      # Build configuration
├── settings.gradle.kts   # Project settings
├── gradle.properties     # Gradle properties
├── src/
│   └── main/
│       ├── java/         # Source code
│       │   └── com/modmc/anticheat/
│       │       ├── check/         # Check implementations
│       │       ├── command/       # Command handlers
│       │       ├── data/          # Player data
│       │       ├── database/      # SQLite handling
│       │       ├── listener/      # Event listeners
│       │       ├── manager/       # Managers (alerts, checks, punishments)
│       │       ├── util/          # Utility classes
│       │       ├── webhook/       # Discord webhook
│       │       └── ModMCAntiCheat.java  # Main class
│       └── resources/
│           ├── config.yml         # Default config
│           └── plugin.yml         # Plugin metadata
└── build/                # Build output
```

---

## Dependencies

The project uses Gradle for dependency management. Key dependencies:

| Dependency | Purpose |
|------------|---------|
| PacketEvents | Packet handling (shaded in) |
| Spigot API | Bukkit/Spigot interfaces |
| SQLite JDBC | Database connectivity |

All dependencies are handled automatically by Gradle. PacketEvents is **shaded** into the final jar, so servers don't need it installed separately.

---

## Development Setup

### IDE Import

#### IntelliJ IDEA

1. File → Open
2. Select the project folder
3. Choose "Import Gradle project"
4. Wait for indexing

#### Eclipse

1. File → Import
2. Gradle → Existing Gradle Project
3. Select project folder
4. Finish

#### VS Code

1. Install "Gradle for Java" extension
2. Open project folder
3. Gradle tasks appear in sidebar

---

## Making Changes

### After modifying code:

1. Build: `./gradlew build`
2. Copy jar to test server
3. Restart server
4. Test your changes

### Recommended workflow:

1. Make small, focused changes
2. Build and test frequently
3. Keep backups of working jars

---

## Common Build Issues

### "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"

**Cause:** Gradle wrapper files missing or corrupted.

**Fix:**
```bash
gradle wrapper
```

Or download the wrapper files from the repo.

### "Permission denied" (Mac/Linux)

**Cause:** gradlew isn't executable.

**Fix:**
```bash
chmod +x gradlew
```

### "Could not resolve dependencies"

**Cause:** No internet connection, or Maven repos are down.

**Fix:**
1. Check your internet
2. Try again later
3. Check if repositories are accessible

### "Invalid source release: 17"

**Cause:** Your Java version is too old for the project's target.

**Fix:** Install a newer Java version.

To check your Java version:
```bash
java -version
```

---

## Contributing

### Before submitting a PR:

1. **Test your changes** — Don't submit untested code
2. **Follow the code style** — Match existing formatting
3. **Document changes** — Update comments/docs as needed
4. **One feature per PR** — Keep it focused

### PR process:

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make changes and commit: `git commit -m "Add my feature"`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request on GitHub

---

## Versioning

The version is defined in `build.gradle.kts`:

```kotlin
version = "1.0.0"
```

Update this for releases. Follow [Semantic Versioning](https://semver.org/):
- **Major:** Breaking changes
- **Minor:** New features, backward compatible
- **Patch:** Bug fixes

---

## Continuous Integration

GitHub Actions can automatically build on push/PR. The workflow file is at `.github/workflows/build.yml` (if configured).

Benefits:
- Automatic builds on every push
- Downloadable artifacts
- Build status badges

---

## Need Help?

- **Build issues:** Open an issue with full error output
- **Development questions:** Check existing issues first
- **Feature ideas:** Open a discussion or issue

---

*That's it! You now know everything to build and modify ModMC AntiCheat.*
