# Compressy - Setup Notes

## Before Building

### Required Files to Add

1. **icon.png** - Add a 128x128 icon to `src/main/resources/icon.png`
   - This is displayed in ModMenu and mod loaders

### Building the Mod

```bash
# Build everything (FULL + LITE versions)
./gradlew build

# Output locations (in build/libs/):
# - compressy-1.21.11-v{version}.jar        (FULL version, remapped)
# - compressy-1.21.11-lite-v{version}.jar   (LITE version, remapped)
# - compressy-1.21.11-v{version}-sources.jar (Source code)

# Build for a specific Minecraft version (multi-version support):
./gradlew build -Pmc_version=1.21.11
```

### Installation

1. Install [Fabric Loader](https://fabricmc.net/) 0.18.2+
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Choose your version:
   - **FULL** (`compressy-1.21.11-v{version}.jar`) - Place compressed blocks in world
   - **LITE** (`compressy-1.21.11-lite-v{version}.jar`) - Inventory-only, no placement
4. Place the JAR in your `mods` folder

### Optional Dependencies

- [ModMenu](https://modrinth.com/mod/modmenu) - In-game configuration screen
- [Cloth Config](https://modrinth.com/mod/cloth-config) - Enhanced config UI

## Key Features

- **Standard Crafting**: 3x3 grid with same block = compressed block
- **32 Compression Levels**: From 9 blocks to astronomical amounts
- **No Resource Pack Needed**: Uses vanilla block textures
- **Automatic Block Support**: Works with ALL blocks (vanilla + modded)
- **Automation Compatible**: Works with Autocrafter, Create, AE2, etc.

## Build Variants

| Version | Description |
|---------|-------------|
| **FULL** | Place compressed blocks in world with visual overlays |
| **LITE** | Inventory-only, no placement (servers, performance) |

## Commands

| Command | Description |
|---------|-------------|
| `/cblocks help` | Show help |
| `/cblocks info` | Info about held compressed block |

## Configuration

Config file: `config/compressy.toml` (TOML format - supports comments!)

Options available via ModMenu or config file:
- **showRomanNumerals** - Toggle tier display on placed blocks
- **showDarkeningOverlay** - Toggle visual darkening effect
- **useDefaultExclusions** - Auto-exclude non-solid blocks
- **excludedBlocks** - List of block IDs to exclude from compression

## How Compression Works

1. Place 9 identical blocks in a 3x3 crafting grid
2. Get 1 compressed block (Level 1 = 9 blocks)
3. Compress 9 compressed blocks to get Level 2 (81 blocks)
4. Continue up to Level 32 (9^32 blocks!)

### Decompression

Place any compressed block alone in a crafting grid to get 9 back:
- Level 1 → 9 regular blocks
- Level N → 9 blocks of Level N-1

## Runtime LITE Mode

Force LITE mode at runtime (useful for servers):
```bash
java -Dcompressedblocks.lite=true -jar minecraft_server.jar
```

## Troubleshooting

### Compressed blocks not loading?
- Make sure you're using the **remapped** JAR (not the `-dev` version)
- Check that Fabric API is installed

### ModMenu config not showing?
- Install [ModMenu](https://modrinth.com/mod/modmenu)
- Optionally install [Cloth Config](https://modrinth.com/mod/cloth-config) for enhanced UI

### Block not compressing?
- Check if the block is in the exclusion list
- Some non-solid blocks (flowers, torches) are excluded by default
