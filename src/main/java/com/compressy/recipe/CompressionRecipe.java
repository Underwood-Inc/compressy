package com.compressy.recipe;

import com.compressy.CompressyMod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.text.TextColor;

import java.util.List;

/**
 * Custom crafting recipe for compressing blocks.
 * 
 * This recipe:
 * - Accepts 9 of the same compressible block in a 3x3 grid
 * - Outputs a compressed version of that block
 * - Works with ANY block that has an item form (no giant config needed!)
 * - Integrates with JEI/REI and automation mods automatically
 */
public class CompressionRecipe extends SpecialCraftingRecipe {
    
    public CompressionRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    /**
     * Check if the crafting grid contains a valid compression pattern:
     * - All 9 slots filled with the same item
     * - Item must be a placeable block (has Block form)
     * - If already compressed, level must be < 32
     */
    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        // Must be a 3x3 grid with all 9 slots filled
        if (input.getWidth() != 3 || input.getHeight() != 3) {
            return false;
        }
        
        // Get the first item to compare against
        ItemStack firstStack = input.getStackInSlot(0);
        if (firstStack.isEmpty()) {
            return false;
        }
        
        // CRITICAL: Check exclusion list FIRST before any other processing
        // This prevents flicker where the result briefly shows before being removed
        var item = firstStack.getItem();
        var block = net.minecraft.block.Block.getBlockFromItem(item);
        
        // If it's a block item (not already compressed), check exclusions immediately
        if (block != null && block != net.minecraft.block.Blocks.AIR && item != net.minecraft.item.Items.AIR) {
            String blockId = net.minecraft.registry.Registries.BLOCK.getId(block).toString();
            if (com.compressy.config.CompressyConfig.get().isBlockExcluded(blockId)) {
                return false; // Blocked by exclusion list - reject immediately
            }
        }
        // If already compressed, we'll check exclusions in isCompressibleItem() using the stored block ID
        
        // Check if it's a block item (can be placed)
        if (!isCompressibleItem(firstStack)) {
            return false;
        }
        
        // Check compression level if already compressed
        int currentLevel = getCompressionLevel(firstStack);
        if (currentLevel >= 32) {
            return false; // Max level reached
        }
        
        // All 9 slots must contain the same item
        for (int i = 0; i < 9; i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                return false;
            }
            if (!ItemStack.areItemsEqual(stack, firstStack)) {
                return false;
            }
            // If items are compressed, they must have the same compression data
            if (currentLevel > 0) {
                int otherLevel = getCompressionLevel(stack);
                String otherBlock = getCompressedBlockId(stack);
                if (otherLevel != currentLevel || !getCompressedBlockId(firstStack).equals(otherBlock)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Create the compressed output item
     */
    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack firstStack = input.getStackInSlot(0);
        
        int currentLevel = getCompressionLevel(firstStack);
        int newLevel = currentLevel + 1;
        
        // Get the base block ID
        String blockId;
        if (currentLevel > 0) {
            // Already compressed - get the original block ID
            blockId = getCompressedBlockId(firstStack);
        } else {
            // Regular item - get the BLOCK ID (not item ID) for consistency
            // This ensures we always store block IDs, which works for ALL blocks
            var item = firstStack.getItem();
            var block = net.minecraft.block.Block.getBlockFromItem(item);
            if (block != null && block != net.minecraft.block.Blocks.AIR) {
                blockId = net.minecraft.registry.Registries.BLOCK.getId(block).toString();
            } else {
                // Fallback to item ID if somehow no block exists (shouldn't happen for compressible items)
                blockId = net.minecraft.registry.Registries.ITEM.getId(item).toString();
            }
        }
        
        // Create output item (same item type as input)
        ItemStack output = new ItemStack(firstStack.getItem(), 1);
        
        // Get tier-based styling
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
        
        // Set custom data with compression info
        NbtCompound customData = new NbtCompound();
        customData.putInt("compressed_level", newLevel);
        customData.putString("compressed_block", blockId);
        output.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
        
        // Log compression for debugging
        // var outputItemId = net.minecraft.registry.Registries.ITEM.getId(output.getItem()).toString();
        // CompressyMod.LOGGER.info("CompressionRecipe.craft(): Created compressed item: {}, level: {}, blockId: {}", outputItemId, newLevel, blockId);
        
        // Verify the data was saved
        // var savedData = output.get(DataComponentTypes.CUSTOM_DATA);
        // if (savedData != null) {
        //     var savedNbt = savedData.copyNbt();
        //     if (savedNbt != null) {
        //         int savedLevel = com.compressy.util.NbtHelper.getInt(savedNbt, "compressed_level", 0);
        //         String savedBlockId = com.compressy.util.NbtHelper.getString(savedNbt, "compressed_block", "");
        //         CompressyMod.LOGGER.info("  Verified saved data: level={}, blockId={}", savedLevel, savedBlockId);
        //     } else {
        //         CompressyMod.LOGGER.error("  ERROR: CUSTOM_DATA exists but copyNbt() returned null for {}", outputItemId);
        //     }
        // } else {
        //     CompressyMod.LOGGER.error("  ERROR: CUSTOM_DATA was not saved for {}", outputItemId);
        // }
        
        // Add enchantment glint for higher levels (starts at level 5)
        if (newLevel >= 5) {
            output.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        
        return output;
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
        // Gradient from bright cyan → purple → dark red → gold for max
        if (level <= 3) {
            return TextColor.parse("#55FFFF").result().orElse(TextColor.fromRgb(0x55FFFF)); // Bright cyan
        } else if (level <= 6) {
            return TextColor.parse("#55FF55").result().orElse(TextColor.fromRgb(0x55FF55)); // Bright green
        } else if (level <= 10) {
            return TextColor.parse("#FFFF55").result().orElse(TextColor.fromRgb(0xFFFF55)); // Yellow
        } else if (level <= 15) {
            return TextColor.parse("#FFAA00").result().orElse(TextColor.fromRgb(0xFFAA00)); // Orange
        } else if (level <= 20) {
            return TextColor.parse("#FF5555").result().orElse(TextColor.fromRgb(0xFF5555)); // Red
        } else if (level <= 25) {
            return TextColor.parse("#FF55FF").result().orElse(TextColor.fromRgb(0xFF55FF)); // Magenta
        } else if (level <= 30) {
            return TextColor.parse("#AA00AA").result().orElse(TextColor.fromRgb(0xAA00AA)); // Dark magenta
        } else {
            return TextColor.parse("#FFD700").result().orElse(TextColor.fromRgb(0xFFD700)); // Gold for max tiers
        }
    }
    
    /**
     * Get contrasting color for roman numeral - ensures readability
     */
    private TextColor getContrastColor(int level) {
        // Light colors for dark backgrounds, dark for light
        if (level <= 10) {
            return TextColor.parse("#1A1A1A").result().orElse(TextColor.fromRgb(0x1A1A1A)); // Dark for light tiers
        } else if (level <= 20) {
            return TextColor.parse("#FFFFFF").result().orElse(TextColor.fromRgb(0xFFFFFF)); // White for mid tiers
        } else if (level <= 30) {
            return TextColor.parse("#FFFF55").result().orElse(TextColor.fromRgb(0xFFFF55)); // Yellow for dark tiers
        } else {
            return TextColor.parse("#000000").result().orElse(TextColor.fromRgb(0x000000)); // Black on gold
        }
    }
    
    /**
     * Get tier symbol
     */
    private String getTierSymbol(int level) {
        if (level <= 3) return "◇";      // Diamond outline - basic
        if (level <= 6) return "◆";      // Filled diamond - uncommon
        if (level <= 10) return "★";     // Star - rare
        if (level <= 15) return "✦";     // 4-pointed star - epic
        if (level <= 20) return "✧";     // Sparkle - legendary
        if (level <= 25) return "❖";     // Diamond with dot - mythic
        if (level <= 30) return "✴";     // 8-pointed star - cosmic
        return "☆";                       // Outlined star - OMEGA
    }
    
    /**
     * Get tier progress bar
     */
    private String getTierBar(int level) {
        int filled = Math.min(level, 32);
        int segments = 16; // 16 segments for the bar
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
     * Convert number to Roman numerals (supports up to 32)
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
    public RecipeSerializer<CompressionRecipe> getSerializer() {
        return CompressyMod.COMPRESSION_RECIPE_SERIALIZER;
    }
    
    /**
     * Check if an item can be compressed (is a block item).
     * 
     * THIS IS THE KEY TO AUTOMATIC MOD SUPPORT!
     * 
     * How it works:
     * - Block.getBlockFromItem() queries Minecraft's registry
     * - When ANY mod (vanilla, Fabric, Forge, etc.) registers a block,
     *   they also register a BlockItem that links back to the block
     * - This registry lookup automatically finds ALL blocks from ALL mods
     * - No config files, no block lists, no tags needed!
     * 
     * What CAN be compressed:
     * - Any item that has a corresponding Block (stone, dirt, mod blocks, etc.)
     * - Already-compressed blocks (for re-compression)
     * 
     * What CANNOT be compressed:
     * - Items without block form (swords, food, tools, etc.)
     * - Air (obviously)
     * - Blocks in the exclusion list (config)
     */
    private boolean isCompressibleItem(ItemStack stack) {
        var item = stack.getItem();
        
        // Query Minecraft's block registry - works for ALL mods automatically!
        var block = net.minecraft.block.Block.getBlockFromItem(item);
        
        // If getBlockFromItem returns AIR, this item has no block form
        if (block == net.minecraft.block.Blocks.AIR && item != net.minecraft.item.Items.AIR) {
            // Not a block item - but allow if it's already compressed
            int level = getCompressionLevel(stack);
            if (level > 0) {
                // Already compressed - check exclusion using the stored block ID
                String storedBlockId = getCompressedBlockId(stack);
                if (!storedBlockId.isEmpty() && com.compressy.config.CompressyConfig.get().isBlockExcluded(storedBlockId)) {
                    return false; // The original block is excluded
                }
                return true; // Already compressed, allow re-compression
            }
            return false; // Not a block and not compressed
        }
        
        // For regular block items, exclusion check already happened in matches() method
        // But we check again here for safety (in case this method is called elsewhere)
        String blockId = net.minecraft.registry.Registries.BLOCK.getId(block).toString();
        if (com.compressy.config.CompressyConfig.get().isBlockExcluded(blockId)) {
            return false;
        }
        
        // It's a block item and not excluded - allow compression!
        return true;
    }
    
    /**
     * Get the compression level of an item (0 if not compressed)
     */
    private int getCompressionLevel(ItemStack stack) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        var nbt = customData.copyNbt();
        return com.compressy.util.NbtHelper.getInt(nbt, "compressed_level", 0);
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
     * e.g., "minecraft:cobblestone" -> "Cobblestone"
     */
    private String formatBlockName(String blockId) {
        String name = blockId;
        if (name.contains(":")) {
            name = name.substring(name.indexOf(':') + 1);
        }
        // Convert underscores to spaces and capitalize
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
     * Calculate human-readable block count for a compression level
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
            case 11 -> "31.4B";
            case 12 -> "282B";
            case 13 -> "2.54T";
            case 14 -> "22.9T";
            case 15 -> "206T";
            default -> level <= 20 ? "Quadrillions+" :
                       level <= 25 ? "Sextillions+" :
                       level <= 30 ? "Octillions+" :
                       "∞ (Astronomical)";
        };
    }
}


