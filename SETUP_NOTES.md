# Compressed Blocks - Setup Notes

## Before Building

### Required Files to Add

1. **icon.png** - Add a 128x128 icon to `src/main/resources/icon.png`
   - This is displayed in ModMenu and mod loaders

### Building the Mod

```bash
# Build everything (mod jar + standalone datapack)
./gradlew build

# Output locations:
# - Mod JAR: build/libs/compressed-blocks-1.0.0.jar
# - Datapack ZIP: build/datapacks/compressed-blocks-datapack-1.0.0.zip
```

### Installation Options

#### Option 1: Fabric Mod (Recommended)
- Install Fabric Loader 0.18.2+
- Install Fabric API
- Place the .jar in your mods folder

#### Option 2: Standalone Datapack
- Use the datapack .zip from build/datapacks/
- Place in your world's datapacks folder
- Note: Some mod features (like /cblocks commands) won't work without the mod

## Key Features

- **Compression Wand**: Right-click while holding blocks in offhand
- **Compressor Block**: Alternative method using placed block
- **32 Compression Levels**: From 9 blocks to astronomical amounts
- **No Resource Pack Needed**: Uses vanilla block textures
- **Tag-Based Detection**: Automatically supports blocks via Minecraft tags

## Commands

- `/cblocks help` - Show help
- `/cblocks give wand` - Get Compression Wand
- `/cblocks give compressor` - Get Compressor Block
- `/cblocks give all` - Get all items
- `/cblocks info` - Info about held compressed block
- `/cblocks admin debug` - Debug info (OP only)
- `/cblocks admin reload` - Reload config (OP only)

## Crafting Recipes

### Compression Wand
```
    A D
    S A
  S    
```
- A = Amethyst Shard
- D = Diamond  
- S = Stick

### Compressor Block
```
  I P I
  P L P
  I P I
```
- I = Iron Ingot
- P = Piston
- L = Lodestone

