package com.compressy;

import java.util.List;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * Handles compressed block placement and breaking.
 * 
 * APPROACH (like totem-rituals):
 * 1. Place the ACTUAL BLOCK (full collision, redstone, piston behavior, etc.)
 * 2. Summon an INTERACTION entity at the block to store compression data
 * 3. Add a TEXT_DISPLAY above for Roman numeral tier
 * 4. Add a BLOCK_DISPLAY overlay (tinted glass) for darkening effect
 * 5. On block break, intercept and drop the compressed item instead
 * 
 * This preserves all vanilla block behavior while storing compression data!
 */
public class CompressyBlockHandler {
    
    // Tags for our entities
    public static final String MARKER_TAG = "compressy.marker";
    public static final String LABEL_TAG = "compressy.label";
    public static final String OVERLAY_TAG = "compressy.overlay";
    
    /**
     * Register all event handlers
     */
    public static void register() {
        // Intercept block placement for compressed items
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack heldItem = player.getStackInHand(hand);
            
            // Check if it's a compressed block
            int level = getCompressionLevel(heldItem);
            if (level <= 0) {
                return ActionResult.PASS; // Not compressed, let vanilla handle it
            }
            
            // It's a compressed block!
            if (!world.isClient() && world instanceof ServerWorld serverWorld) {
                // Calculate placement position
                BlockPos placePos = hitResult.getBlockPos().offset(hitResult.getSide());
                
                // Check if space is available
                if (!world.getBlockState(placePos).isAir()) {
                    return ActionResult.FAIL;
                }
                
                // Get the block to place
                String blockId = getCompressedBlockId(heldItem);
                if (blockId.isEmpty()) {
                    blockId = net.minecraft.registry.Registries.ITEM.getId(heldItem.getItem()).toString();
                }
                
                Block block = net.minecraft.registry.Registries.BLOCK.get(Identifier.of(blockId));
                if (block == Blocks.AIR) {
                    return ActionResult.FAIL;
                }
                
                // Only allow placing FULL CUBE blocks (no flowers, torches, etc.)
                // These non-solid blocks cause issues with data preservation
                BlockState state = block.getDefaultState();
                if (!state.isFullCube(world, placePos)) {
                    player.sendMessage(
                        Text.literal("✗ Cannot place compressed non-solid blocks (flowers, torches, etc.)")
                            .formatted(Formatting.RED),
                        true
                    );
                    return ActionResult.FAIL;
                }
                
                // PLACE THE REAL BLOCK!
                world.setBlockState(placePos, state);
                
                // Now add our marker entities
                createCompressionMarker(serverWorld, placePos, blockId, level);
                
                // Consume the item (unless creative)
                if (!player.isCreative()) {
                    heldItem.decrement(1);
                }
                
                // Play sound
                world.playSound(null, placePos, 
                    block.getDefaultState().getSoundGroup().getPlaceSound(),
                    net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 0.8f);
                
                // Send feedback
                player.sendMessage(
                    Text.literal("Placed compressed " + formatBlockName(blockId) + " (Tier " + toRoman(level) + ")")
                        .formatted(Formatting.GOLD), 
                    true
                );
                
                return ActionResult.SUCCESS;
            }
            
            return ActionResult.PASS;
        });
        
        // Intercept block breaking - BEFORE the block is broken
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) {
                return true; // Continue breaking on client
            }
            
            // Check if there's a compression marker at this position
            List<InteractionEntity> markers = world.getEntitiesByClass(
                InteractionEntity.class,
                new Box(pos),
                e -> e.getCommandTags().contains(MARKER_TAG)
            );
            
            if (markers.isEmpty()) {
                return true; // No marker, normal block break
            }
            
            // Found a marker! This is a compressed block
            InteractionEntity marker = markers.get(0);
            
            // Get compression data from command tags
            int level = 1;
            String blockId = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock()).toString();
            
            // Parse level and block ID from command tags
            for (String tag : marker.getCommandTags()) {
                if (tag.startsWith("compressy.level.")) {
                try {
                        level = Integer.parseInt(tag.substring("compressy.level.".length()));
                } catch (NumberFormatException e) {
                        // Use default
                    }
                } else if (tag.startsWith("compressy.block.")) {
                    // Convert back from tag format (dots) to ID format (colon)
                    String tagBlockId = tag.substring("compressy.block.".length());
                    int firstDot = tagBlockId.indexOf('.');
                    if (firstDot > 0) {
                        blockId = tagBlockId.substring(0, firstDot) + ":" + tagBlockId.substring(firstDot + 1);
                    }
                }
            }
            
            // Create the compressed item to drop
            ItemStack dropItem = createCompressedItem(blockId, level);
            
            // Remove the marker and related display entities
            removeCompressionEntities(world, pos);
            
            // Remove the block without normal drops
            world.removeBlock(pos, false);
            
            // Drop the compressed item in world (player must pick it up)
            net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
                world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropItem
            );
            world.spawnEntity(itemEntity);
            
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(
                    Text.literal("Retrieved compressed block (Tier " + toRoman(level) + ")")
                        .formatted(Formatting.GREEN),
                    true
                );
            }
            
            // Play sound
            world.playSound(null, pos, state.getSoundGroup().getBreakSound(),
                net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
            
            return false; // Cancel normal block break (we handled it)
        });
        
        CompressyMod.LOGGER.info("Compressed block handler registered (ACTUAL BLOCKS + marker entities)");
    }
    
    /**
     * Create marker entities at a compressed block position
     */
    private static void createCompressionMarker(ServerWorld world, BlockPos pos, String blockId, int level) {
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        
        // 1. INTERACTION ENTITY - stores data, invisible, at block center
        var marker = EntityType.INTERACTION.create(world, SpawnReason.COMMAND);
        if (marker != null) {
            marker.setPosition(x, y, z);
            marker.setInteractionWidth(0.98f);
            marker.setInteractionHeight(0.98f);
            marker.addCommandTag(MARKER_TAG);
            marker.addCommandTag("compressy.pos." + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
            marker.addCommandTag("compressy.level." + level);
            marker.addCommandTag("compressy.block." + blockId.replace(":", "."));
            // No custom name - prevents flash on removal
            world.spawnEntity(marker);
        }
        
        // 2. TEXT_DISPLAY - shows Roman numeral tier above the block (if enabled in config)
        if (com.compressy.config.CompressyConfig.get().showRomanNumerals) {
            var textDisplay = EntityType.TEXT_DISPLAY.create(world, SpawnReason.COMMAND);
            if (textDisplay != null) {
                textDisplay.setPosition(x, y + 1.0, z);
                
                String roman = toRoman(level);
                int color = getTierColor(level);
                
                ((DisplayEntity.TextDisplayEntity) textDisplay).setText(
                    Text.literal(" " + roman + " ")
                        .styled(s -> s.withColor(color).withBold(true))
                );
                
                textDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
                textDisplay.addCommandTag(MARKER_TAG);
                textDisplay.addCommandTag(LABEL_TAG);
                textDisplay.addCommandTag("compressy.pos." + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
                
                world.spawnEntity(textDisplay);
            }
        }
        
        // 3. BLOCK_DISPLAY OVERLAY - darkening tint based on compression level (if enabled)
        // Scale LARGER (1.01x) so overlay is visible on top of the block
        if (level > 1 && com.compressy.config.CompressyConfig.get().showDarkeningOverlay) {
            var overlay = EntityType.BLOCK_DISPLAY.create(world, SpawnReason.COMMAND);
            if (overlay != null) {
                // Scale factor - slightly larger so overlay is visible on block faces
                float scale = 1.01f;
                // Offset to center the scaled block (-0.005 on each axis for 1.01 scale)
                double offset = (1.0 - scale) / 2.0;
                
                overlay.setPosition(
                    pos.getX() + offset,
                    pos.getY() + offset,
                    pos.getZ() + offset
                );
                
                // Use tinted glass - darker colors for higher levels
                BlockState overlayState = getOverlayBlock(level);
                ((DisplayEntity.BlockDisplayEntity) overlay).setBlockState(overlayState);
                
                // Set transformation for scale
                // The transformation is applied via the entity's properties
                org.joml.Matrix4f transform = new org.joml.Matrix4f();
                transform.scale(scale);
                ((DisplayEntity.BlockDisplayEntity) overlay).setTransformation(
                    new net.minecraft.util.math.AffineTransformation(
                        new org.joml.Vector3f(0, 0, 0),  // translation
                        null,  // left rotation
                        new org.joml.Vector3f(scale, scale, scale),  // scale
                        null   // right rotation
                    )
                );
                
                // Reduce brightness for darker effect at higher levels
                int brightness = Math.max(0, 15 - (level / 3));
                overlay.setBrightness(new net.minecraft.entity.decoration.Brightness(brightness, brightness));
                
                overlay.addCommandTag(MARKER_TAG);
                overlay.addCommandTag(OVERLAY_TAG);
                overlay.addCommandTag("compressy.pos." + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
                
                world.spawnEntity(overlay);
            }
        }
    }
    
    /**
     * Get the overlay block based on compression level (progressively darker)
     */
    private static BlockState getOverlayBlock(int level) {
        // Use progressively darker tinted glass
        if (level <= 5) {
            return Blocks.LIGHT_GRAY_STAINED_GLASS.getDefaultState();
        } else if (level <= 10) {
            return Blocks.GRAY_STAINED_GLASS.getDefaultState();
        } else if (level <= 20) {
            return Blocks.BLACK_STAINED_GLASS.getDefaultState();
        } else {
            // Very high levels - use tinted glass (even darker)
            return Blocks.TINTED_GLASS.getDefaultState();
        }
    }
    
    /**
     * Remove all compression-related entities at a position
     */
    private static void removeCompressionEntities(World world, BlockPos pos) {
        // Use position tag for EXACT matching - no ambiguity with neighbors
        String posTag = "compressy.pos." + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
        
        // Expand search to catch all nearby entities with our tags
        Box searchBox = new Box(pos).expand(1.5);
        
        List<Entity> entities = world.getEntitiesByClass(
            Entity.class,
            searchBox,
            e -> e.getCommandTags().contains(posTag)
        );
        
        // Remove all entities tagged with this exact position
        for (Entity entity : entities) {
            entity.discard();
        }
    }
    
    /**
     * Create a compressed item from stored data
     * blockId should be a BLOCK ID (not item ID) for consistency
     */
    private static ItemStack createCompressedItem(String blockId, int level) {
        // Try to get block first (blockId should be a block ID)
        var block = net.minecraft.registry.Registries.BLOCK.get(Identifier.of(blockId));
        net.minecraft.item.Item item;
        if (block != null && block != Blocks.AIR) {
            // Get item from block - this is the correct way
            item = block.asItem();
            if (item == null || item == net.minecraft.item.Items.AIR) {
                // Fallback: try to get item directly (might be item ID in old saves)
                item = net.minecraft.registry.Registries.ITEM.get(Identifier.of(blockId));
            }
        } else {
            // Fallback: try to get item directly (might be item ID in old saves)
            item = net.minecraft.registry.Registries.ITEM.get(Identifier.of(blockId));
        }
        
        if (item == null || item == net.minecraft.item.Items.AIR) {
            CompressyMod.LOGGER.error("createCompressedItem: Could not resolve blockId {} to any item", blockId);
            // Last resort: use stone
            item = net.minecraft.item.Items.STONE;
        }
        
        ItemStack stack = new ItemStack(item, 1);
        
        // Set the compression data
        NbtCompound customData = new NbtCompound();
        customData.putInt("compressed_level", level);
        customData.putString("compressed_block", blockId);
        stack.set(DataComponentTypes.CUSTOM_DATA, 
            net.minecraft.component.type.NbtComponent.of(customData));
        
        // Set name
        String roman = toRoman(level);
        String displayName = formatBlockName(blockId);
        int color = getTierColor(level);
        
        stack.set(DataComponentTypes.CUSTOM_NAME,
            Text.literal(getTierSymbol(level) + " ")
                .styled(s -> s.withColor(color).withItalic(false))
                .append(Text.literal(displayName)
                    .styled(s -> s.withColor(color).withItalic(false).withBold(true)))
                .append(Text.literal(" " + roman)
                    .styled(s -> s.withColor(getContrastColor(level)).withItalic(false).withBold(true))));
        
        // Set lore
        String blockCount = calculateBlockCountString(level);
        java.util.List<Text> lore = new java.util.ArrayList<>();
        lore.add(Text.literal(getTierBar(level)).styled(s -> s.withColor(color).withItalic(false)));
        lore.add(Text.empty());
        lore.add(Text.literal("⬥ Tier: ").styled(s -> s.withColor(Formatting.GRAY).withItalic(false))
            .append(Text.literal(roman).styled(s -> s.withColor(color).withBold(true).withItalic(false))));
        lore.add(Text.literal("⬥ Contains: ").styled(s -> s.withColor(Formatting.GRAY).withItalic(false))
            .append(Text.literal(blockCount + " blocks").styled(s -> s.withColor(Formatting.WHITE).withItalic(false))));
        lore.add(Text.empty());
        lore.add(Text.literal("▸ ").styled(s -> s.withColor(Formatting.DARK_GRAY).withItalic(false))
            .append(Text.literal("3×3 craft to compress more").styled(s -> s.withColor(Formatting.GREEN).withItalic(false))));
        lore.add(Text.literal("▸ ").styled(s -> s.withColor(Formatting.DARK_GRAY).withItalic(false))
            .append(Text.literal("Craft alone to decompress").styled(s -> s.withColor(Formatting.AQUA).withItalic(false))));
        
        stack.set(DataComponentTypes.LORE, 
            new net.minecraft.component.type.LoreComponent(lore));
        
        // Glint for high levels
        if (level >= 5) {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        
        return stack;
    }
    
    // === Helper Methods ===
    
    private static int getCompressionLevel(ItemStack stack) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return 0;
        var nbt = customData.copyNbt();
        return com.compressy.util.NbtHelper.getInt(nbt, "compressed_level", 0);
    }
    
    private static String getCompressedBlockId(ItemStack stack) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return "";
        var nbt = customData.copyNbt();
        return com.compressy.util.NbtHelper.getString(nbt, "compressed_block", "");
    }
    
    private static String toRoman(int num) {
        if (num <= 0 || num > 40) return String.valueOf(num);
        String[] tens = {"", "X", "XX", "XXX", "XL"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return tens[num / 10] + ones[num % 10];
    }
    
    private static int getTierColor(int level) {
        if (level <= 3) return 0x55FFFF;   // Aqua
        if (level <= 6) return 0x55FF55;   // Green
        if (level <= 10) return 0xFFFF55;  // Yellow
        if (level <= 15) return 0xFFAA00;  // Orange
        if (level <= 20) return 0xFF5555;  // Red
        if (level <= 25) return 0xFF55FF;  // Pink
        if (level <= 30) return 0xAA00AA;  // Purple
        return 0xFFD700;                    // Gold
    }
    
    private static int getContrastColor(int level) {
        // For readability of Roman numerals on darker blocks
        if (level <= 10) return 0x1A1A1A;   // Dark on light backgrounds
        if (level <= 20) return 0xFFFFFF;   // White on medium
        if (level <= 30) return 0xFFFF55;   // Yellow on dark
        return 0x000000;                     // Black on gold
    }
    
    private static int getContrastBackgroundColor(int level) {
        // Background color for text display
        if (level <= 10) return 0x40000000;  // Semi-transparent dark
        if (level <= 20) return 0x40FFFFFF;  // Semi-transparent light
        return 0x80000000;                    // More opaque dark
    }
    
    private static String getTierSymbol(int level) {
        if (level <= 3) return "◇";
        if (level <= 6) return "◆";
        if (level <= 10) return "★";
        if (level <= 15) return "✦";
        if (level <= 20) return "✧";
        if (level <= 25) return "❖";
        if (level <= 30) return "✴";
        return "☆";
    }
    
    private static String getTierBar(int level) {
        int filled = Math.min(level, 32);
        int segments = 16;
        int filledSegments = (filled * segments) / 32;
        int emptySegments = segments - filledSegments;
        return "▓".repeat(filledSegments) + "░".repeat(emptySegments);
    }
    
    private static String formatBlockName(String blockId) {
        String name = blockId;
        if (name.contains(":")) {
            name = name.substring(name.indexOf(':') + 1);
        }
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
    
    private static String calculateBlockCountString(int level) {
        return switch (level) {
            case 1 -> "9";
            case 2 -> "81";
            case 3 -> "729";
            case 4 -> "6,561";
            case 5 -> "59,049";
            case 6 -> "531,441";
            case 7 -> "4.78M";
            case 8 -> "43M";
            case 9 -> "387M";
            case 10 -> "3.49B";
            default -> level <= 15 ? "Trillions+" :
                       level <= 20 ? "Quadrillions+" :
                       "Astronomical";
        };
    }
}

