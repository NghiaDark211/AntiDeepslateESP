# AntiDeepslateESP

A Paper 1.21.11 plugin that prevents ESP hacks by hiding deepslate blocks from players on the surface.

## How it works

When a player stands above Y=6 (surface), all blocks in chunk sections at Y≤0 are hidden from that player using packet manipulation via ProtocolLib. When the player digs down to Y≤6, the deepslate sections become visible again.

```
Surface (Y > 6)   → Deepslate hidden (Y ≤ 0 = air)
Mining (Y ≤ 6)    → Deepslate visible (normal)
```

This prevents ESP/X-Ray cheats from seeing ores and caves through deepslate while the player is on the surface.

## Features

- **Per-player packet manipulation** — no world data is modified, only the packets sent to surface players
- **Configurable thresholds** — customize surface/deepslate Y levels
- **Auto-commands** — run console commands when players cross the Y threshold (e.g., title notifications)
- **Update checker** — automatically checks GitHub for new releases
- **Reload command** — `/adsesp reload` to reload configuration

## Requirements

- **Paper** 1.21.11
- **ProtocolLib** 5.4.0+
- Java 21

## Installation

1. Download the latest release from GitHub
2. Place `AntiDeepslateESP.jar` in your server's `plugins/` folder
3. Ensure `ProtocolLib.jar` is also in `plugins/`
4. Restart the server

## Commands

| Command | Alias | Permission | Default | Description |
|---------|-------|-----------|---------|-------------|
| `/antideepslateesp reload` | `/adsesp reload` | `antideepslateesp.reload` | OP | Reload config |

## Configuration

```yaml
# Threshold Y level: when player Y > this value, deepslate is hidden
threshold-y: 6

# Y level below which sections are hidden
hide-below-y: 0

# Auto-commands when crossing threshold (%player%, %world%, %x%, %y%, %z%)
auto-commands:
  surface:
    - "title %player% title {\"text\":\"§cAnti-ESP Active\",\"bold\":true}"
    - "title %player% subtitle {\"text\":\"§7Deepslate sections hidden\"}"
  mining:
    - "title %player% title {\"text\":\"§aMining Mode\",\"bold\":true}"
    - "title %player% subtitle {\"text\":\"§7Deepslate sections visible\"}"

update-checker: true
github-repo: NghiaDark211/AntiDeepslateESP
```

## Building from source

```bash
mvn clean package
```

The output JAR will be in `target/AntiDeepslateESP-1.0.0.jar`.

## Dependencies

- **[Paper API](https://papermc.io/)** 1.21.11-R0.1-SNAPSHOT
- **[ProtocolLib](https://github.com/dmulloy2/ProtocolLib)** 5.4.0

## Author

**nghiadark**
