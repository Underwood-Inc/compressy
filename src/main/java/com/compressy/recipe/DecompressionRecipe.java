package com.compressy.recipe;

import java.util.List;

import com.compressy.CompressyMod;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * Custom crafting recipe for decompressing blocks.
 * 
 * This recipe:
 * - Accepts a single compressed block anywhere in the grid
 * - Outputs 9 of the lower compression level (or base blocks if level 1)
 * - Works with automation mods automatically
 */
public class DecompressionRecipe extends SpecialCraftingRecipe {
    
    public DecompressionRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    /**
     * Check if the crafting grid contains exactly one compressed block
     */
    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        ItemStack compressedBlock = ItemStack.EMPTY;
        int itemCount = 0;
        
        // Iterate through all slots in the crafting grid
        int totalSlots = input.getWidth() * input.getHeight();
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (itemCount > 1) {
                    return false; // More than one item
                }
                compressedBlock = stack;
            }
        }
        
        if (compressedBlock.isEmpty()) {
            return false;
        }
        
        // var itemId = net.minecraft.registry.Registries.ITEM.getId(compressedBlock.getItem()).toString();
        // CompressyMod.LOGGER.info("DecompressionRecipe.matches() called for item: {}", itemId);
        
        // Debug: Check what data components exist
        // var customData = compressedBlock.get(DataComponentTypes.CUSTOM_DATA);
        // if (customData != null) {
        //     var nbt = customData.copyNbt();
        //     if (nbt != null) {
        //         CompressyMod.LOGGER.info("  NBT keys present: {}", nbt.getKeys());
        //         CompressyMod.LOGGER.info("  NBT contains compressed_level: {}", nbt.contains("compressed_level"));
        //         if (nbt.contains("compressed_level")) {
        //             try {
        //                 var levelOpt = nbt.getInt("compressed_level");
        //                 CompressyMod.LOGGER.info("  Direct NBT read - level present: {}, value: {}", levelOpt.isPresent(), levelOpt.orElse(-1));
        //             } catch (Exception e) {
        //                 CompressyMod.LOGGER.error("  Error reading compressed_level directly from NBT", e);
        //             }
        //         }
        //     } else {
        //         CompressyMod.LOGGER.warn("  CUSTOM_DATA exists but copyNbt() returned null for {}", itemId);
        //     }
        // } else {
        //     CompressyMod.LOGGER.warn("  No CUSTOM_DATA component found for {}", itemId);
        // }
        
        // CRITICAL: Check if it's a compressed block FIRST
        // This ensures our recipe takes priority over vanilla recipes
        int compressionLevel = getCompressionLevel(compressedBlock);
        String blockId = getCompressedBlockId(compressedBlock);
        // CompressyMod.LOGGER.info("  compressionLevel={}, blockId={}", compressionLevel, blockId);
        
        if (compressionLevel <= 0) {
            // CompressyMod.LOGGER.info("  Not compressed, returning false");
            return false; // Not compressed, don't match
        }
        
        // CompressyMod.LOGGER.info("  Compressed item detected! Returning true");
        return true;
    }

    /**
     * Create the decompressed output items (9x)
     */
    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        // Find the compressed block
        ItemStack compressedBlock = ItemStack.EMPTY;
        int totalSlots = input.getWidth() * input.getHeight();
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                compressedBlock = stack;
                break;
            }
        }
        
        int currentLevel = getCompressionLevel(compressedBlock);
        String blockId = getCompressedBlockId(compressedBlock);
        
        if (currentLevel == 1) {
            // Return 9 of the original block
            // CRITICAL: Use the stored blockId to get the correct block, then get its item
            // This ensures we get the correct item for ALL blocks, even if item/block IDs differ
            if (blockId.isEmpty()) {
                // Fallback: use item from compressed block if blockId is missing (shouldn't happen)
                // CompressyMod.LOGGER.warn("Decompressing level 1: blockId is empty, using item from compressed block");
                return new ItemStack(compressedBlock.getItem(), 9);
            }
            
            // Get the block from the stored block ID
            var block = net.minecraft.registry.Registries.BLOCK.get(net.minecraft.util.Identifier.of(blockId));
            if (block == null || block == net.minecraft.block.Blocks.AIR) {
                // Fallback: try to get item directly from blockId (might be item ID in old saves)
                var item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(blockId));
                if (item != null && item != net.minecraft.item.Items.AIR) {
                    // CompressyMod.LOGGER.info("Decompressing level 1: blockId was item ID, using item directly: {}", blockId);
                    return new ItemStack(item, 9);
                }
                // Last resort: use item from compressed block
                // CompressyMod.LOGGER.warn("Decompressing level 1: Could not resolve blockId {}, using item from compressed block", blockId);
                return new ItemStack(compressedBlock.getItem(), 9);
            }
            
            // Get the item from the block - this is the correct way for ALL blocks
            var item = block.asItem();
            if (item == null || item == net.minecraft.item.Items.AIR) {
                // Shouldn't happen, but fallback
                // CompressyMod.LOGGER.error("Decompressing level 1: Block {} has no item form, using item from compressed block", blockId);
                return new ItemStack(compressedBlock.getItem(), 9);
            }
            
            // Log for debugging
            // CompressyMod.LOGGER.info("Decompressing level 1: blockId={}, resolved to item={}", blockId, net.minecraft.registry.Registries.ITEM.getId(item).toString());
            
            // Create a new stack with 9 items - this is the decompressed result
            return new ItemStack(item, 9);
        } else {
            // Return 9 of the lower compression level
            int newLevel = currentLevel - 1;
            
            // CRITICAL: Use the stored blockId to get the correct item, not compressedBlock.getItem()
            // This ensures consistency across all compression levels
            ItemStack output;
            if (blockId.isEmpty()) {
                // Fallback: use item from compressed block if blockId is missing
                // CompressyMod.LOGGER.warn("Decompressing level {}: blockId is empty, using item from compressed block", newLevel);
                output = new ItemStack(compressedBlock.getItem(), 9);
            } else {
                // Get the block from the stored block ID
                var block = net.minecraft.registry.Registries.BLOCK.get(net.minecraft.util.Identifier.of(blockId));
                if (block == null || block == net.minecraft.block.Blocks.AIR) {
                    // Fallback: try to get item directly from blockId (might be item ID in old saves)
                    var item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(blockId));
                    if (item != null && item != net.minecraft.item.Items.AIR) {
                        // CompressyMod.LOGGER.info("Decompressing level {}: blockId was item ID, using item directly: {}", newLevel, blockId);
                        output = new ItemStack(item, 9);
                    } else {
                        // Last resort: use item from compressed block
                        // CompressyMod.LOGGER.warn("Decompressing level {}: Could not resolve blockId {}, using item from compressed block", newLevel, blockId);
                        output = new ItemStack(compressedBlock.getItem(), 9);
                    }
                } else {
                    // Get the item from the block - this is the correct way for ALL blocks
                    var item = block.asItem();
                    if (item == null || item == net.minecraft.item.Items.AIR) {
                        // Shouldn't happen, but fallback
                        // CompressyMod.LOGGER.error("Decompressing level {}: Block {} has no item form, using item from compressed block", newLevel, blockId);
                        output = new ItemStack(compressedBlock.getItem(), 9);
                    } else {
                        output = new ItemStack(item, 9);
                    }
                }
            }
            
            // Get tier-based styling (matching CompressionRecipe)
            String romanNumeral = toRoman(newLevel);
            TextColor nameColor = getTierColor(newLevel);
            String tierSymbol = getTierSymbol(newLevel);
            
            // Set custom name with tier symbol and roman numeral
            output.set(DataComponentTypes.CUSTOM_NAME, 
                Text.literal(tierSymbol + " ")
                    .styled(style -> style.withColor(nameColor).withItalic(false))
                    .append(Text.literal(formatBlockName(blockId))
                        .styled(style -> style.withColor(nameColor).withItalic(false).withBold(true)))
                    .append(Text.literal(" " + romanNumeral)
                        .styled(style -> style.withColor(getContrastColor(newLevel)).withItalic(false).withBold(true))));
            
            // Set fancy lore with tier-appropriate styling
            String blockCount = calculateBlockCountString(newLevel);
            List<Text> lore = createFancyLore(newLevel, blockCount, romanNumeral);
            output.set(DataComponentTypes.LORE, new LoreComponent(lore));
            
            // Set custom data
            NbtCompound customData = new NbtCompound();
            customData.putInt("compressed_level", newLevel);
            customData.putString("compressed_block", blockId);
            output.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
            
            // Add enchantment glint for higher levels (starts at level 5)
            if (newLevel >= 5) {
                output.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
            
            return output;
        }
    }
    
    /**
     * Create fancy lore with tier-appropriate styling
     */
    private List<Text> createFancyLore(int level, String blockCount, String roman) {
        List<Text> lore = new java.util.ArrayList<>();
        
        // Tier indicator bar
        String tierBar = getTierBar(level);
        lore.add(Text.literal(tierBar).styled(s -> s.withColor(getTierColor(level)).withItalic(false)));
        
        // Empty line
        lore.add(Text.empty());
        
        // Compression tier with roman numeral
        lore.add(Text.literal("⬥ Tier: ").styled(s -> s.withColor(Formatting.GRAY).withItalic(false))
            .append(Text.literal(roman).styled(s -> s.withColor(getTierColor(level)).withBold(true).withItalic(false))));
        
        // Block count
        lore.add(Text.literal("⬥ Contains: ").styled(s -> s.withColor(Formatting.GRAY).withItalic(false))
            .append(Text.literal(blockCount + " blocks").styled(s -> s.withColor(Formatting.WHITE).withItalic(false))));
        
        // Empty line
        lore.add(Text.empty());
        
        // Tier-specific flavor text
        lore.add(getTierFlavorText(level));
        
        // Empty line
        lore.add(Text.empty());
        
        // Instructions
        lore.add(Text.literal("▸ ").styled(s -> s.withColor(Formatting.DARK_GRAY).withItalic(false))
            .append(Text.literal("3×3 craft to compress more").styled(s -> s.withColor(Formatting.GREEN).withItalic(false))));
        lore.add(Text.literal("▸ ").styled(s -> s.withColor(Formatting.DARK_GRAY).withItalic(false))
            .append(Text.literal("Craft alone to decompress").styled(s -> s.withColor(Formatting.AQUA).withItalic(false))));
        
        return lore;
    }
    
    /**
     * Get tier-based color - gradually darker with good contrast
     */
    private TextColor getTierColor(int level) {
        if (level <= 3) {
            return TextColor.parse("#55FFFF").result().orElse(TextColor.fromRgb(0x55FFFF));
        } else if (level <= 6) {
            return TextColor.parse("#55FF55").result().orElse(TextColor.fromRgb(0x55FF55));
        } else if (level <= 10) {
            return TextColor.parse("#FFFF55").result().orElse(TextColor.fromRgb(0xFFFF55));
        } else if (level <= 15) {
            return TextColor.parse("#FFAA00").result().orElse(TextColor.fromRgb(0xFFAA00));
        } else if (level <= 20) {
            return TextColor.parse("#FF5555").result().orElse(TextColor.fromRgb(0xFF5555));
        } else if (level <= 25) {
            return TextColor.parse("#FF55FF").result().orElse(TextColor.fromRgb(0xFF55FF));
        } else if (level <= 30) {
            return TextColor.parse("#AA00AA").result().orElse(TextColor.fromRgb(0xAA00AA));
        } else {
            return TextColor.parse("#FFD700").result().orElse(TextColor.fromRgb(0xFFD700));
        }
    }
    
    /**
     * Get contrasting color for roman numeral - ensures readability
     */
    private TextColor getContrastColor(int level) {
        if (level <= 10) {
            return TextColor.parse("#1A1A1A").result().orElse(TextColor.fromRgb(0x1A1A1A));
        } else if (level <= 20) {
            return TextColor.parse("#FFFFFF").result().orElse(TextColor.fromRgb(0xFFFFFF));
        } else if (level <= 30) {
            return TextColor.parse("#FFFF55").result().orElse(TextColor.fromRgb(0xFFFF55));
        } else {
            return TextColor.parse("#000000").result().orElse(TextColor.fromRgb(0x000000));
        }
    }
    
    /**
     * Get tier symbol
     */
    private String getTierSymbol(int level) {
        if (level <= 3) return "◇";
        if (level <= 6) return "◆";
        if (level <= 10) return "★";
        if (level <= 15) return "✦";
        if (level <= 20) return "✧";
        if (level <= 25) return "❖";
        if (level <= 30) return "✴";
        return "☆";
    }
    
    /**
     * Get tier progress bar
     */
    private String getTierBar(int level) {
        int filled = Math.min(level, 32);
        int segments = 16;
        int filledSegments = (filled * segments) / 32;
        int emptySegments = segments - filledSegments;
        return "▓".repeat(filledSegments) + "░".repeat(emptySegments);
    }
    
    /**
     * Get tier-specific flavor text
     */
    private Text getTierFlavorText(int level) {
        String text;
        Formatting color;
        
        if (level <= 3) {
            text = "Lightly compressed";
            color = Formatting.GRAY;
        } else if (level <= 6) {
            text = "Notably dense";
            color = Formatting.GREEN;
        } else if (level <= 10) {
            text = "Remarkably compact";
            color = Formatting.YELLOW;
        } else if (level <= 15) {
            text = "Extraordinarily dense";
            color = Formatting.GOLD;
        } else if (level <= 20) {
            text = "Impossibly compressed";
            color = Formatting.RED;
        } else if (level <= 25) {
            text = "Reality-bending density";
            color = Formatting.LIGHT_PURPLE;
        } else if (level <= 30) {
            text = "Cosmic singularity";
            color = Formatting.DARK_PURPLE;
        } else {
            text = "☆ OMEGA COMPRESSION ☆";
            color = Formatting.GOLD;
        }
        
        return Text.literal(text).styled(s -> s.withColor(color).withItalic(true));
    }
    
    /**
     * Convert number to Roman numerals (supports up to 40)
     */
    private String toRoman(int num) {
        if (num <= 0 || num > 40) return String.valueOf(num);
        
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        
        return thousands[num / 1000] +
               hundreds[(num % 1000) / 100] +
               tens[(num % 100) / 10] +
               ones[num % 10];
    }

    @Override
    public RecipeSerializer<DecompressionRecipe> getSerializer() {
        return CompressyMod.DECOMPRESSION_RECIPE_SERIALIZER;
    }
    
    /**
     * Get the compression level of an item (0 if not compressed)
     */
    private int getCompressionLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        
        // var itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        
        if (customData == null) {
            // CompressyMod.LOGGER.debug("getCompressionLevel: {} has no CUSTOM_DATA", itemId);
            return 0;
        }
        
        var nbt = customData.copyNbt();
        if (nbt == null) {
            // CompressyMod.LOGGER.warn("getCompressionLevel: {} has CUSTOM_DATA but copyNbt() returned null!", itemId);
            return 0;
        }
        
        // Try multiple ways to read the data
        int level = 0;
        if (nbt.contains("compressed_level")) {
            try {
                level = nbt.getInt("compressed_level").orElse(0);
            } catch (Exception e) {
                // CompressyMod.LOGGER.warn("getCompressionLevel: Failed to read compressed_level for {}, trying alternative method", itemId, e);
                // Try direct access
                try {
                    var opt = nbt.getInt("compressed_level");
                    level = opt.isPresent() ? opt.get() : 0;
                } catch (Exception e2) {
                    // CompressyMod.LOGGER.error("getCompressionLevel: All methods failed for {}", itemId, e2);
                }
            }
        }
        
        // if (level > 0) {
        //     CompressyMod.LOGGER.info("getCompressionLevel: {} detected as compressed level {}", itemId, level);
        // } else {
        //     CompressyMod.LOGGER.debug("getCompressionLevel: {} not compressed (level=0), nbt keys: {}", itemId, nbt.getKeys());
        // }
        
        return level;
    }
    
    /**
     * Get the original block ID from a compressed item
     */
    private String getCompressedBlockId(ItemStack stack) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return "";
        }
        var nbt = customData.copyNbt();
        return com.compressy.util.NbtHelper.getString(nbt, "compressed_block", "");
    }
    
    /**
     * Format block ID into a nice display name
     */
    private String formatBlockName(String blockId) {
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
    
    /**
     * Calculate human-readable block count
     */
    private String calculateBlockCountString(int level) {
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


