package com.compressy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compressy.CompressyMod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

/**
 * COMPLETELY NEW APPROACH: Directly intercept onContentChanged to override results
 * This bypasses the broken recipe matching system entirely.
 * 
 * HOW IT WORKS:
 * - Intercepts when crafting grid changes
 * - Checks if there's exactly one compressed block
 * - Directly overrides the result slot with decompressed output
 * - Works BEFORE vanilla recipes can interfere
 */
@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    
    @Shadow
    private PlayerEntity player;
    
    @Inject(
        method = "onContentChanged",
        at = @At("HEAD")
    )
    private void checkForCompressedBlock(net.minecraft.inventory.Inventory inventory, CallbackInfo ci) {
        // CompressyMod.LOGGER.info("CraftingScreenHandlerMixin.onContentChanged: CALLED AT HEAD");
        
        // Get the world from the player
        if (player == null) {
            // CompressyMod.LOGGER.debug("  Player is null");
            return;
        }
        
        World world = player.getEntityWorld();
        if (world == null || world.isClient()) {
            // CompressyMod.LOGGER.debug("  World is null or client");
            return;
        }
        
        // Check if there's exactly one item (decompression pattern) - CHECK FIRST BEFORE VANILLA
        // Need to cast to CraftingInventory to access the crafting grid
        if (!(inventory instanceof CraftingInventory craftingInventory)) {
            // CompressyMod.LOGGER.debug("  Not a CraftingInventory");
            return;
        }
        
        // Count how many SLOTS contain compressed blocks (not total items)
        ItemStack singleCompressedItem = ItemStack.EMPTY;
        int compressedSlotCount = 0;
        int totalItemSlots = 0;
        
        // CompressyMod.LOGGER.info("  Checking {} slots", craftingInventory.size());
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (!stack.isEmpty()) {
                totalItemSlots++;
                int compressionLevel = getCompressionLevel(stack);
                // CompressyMod.LOGGER.info("  Slot {}: {} x{} (compressed: {})", i, 
                //     net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString(), 
                //     stack.getCount(),
                //     compressionLevel > 0 ? "YES" : "NO");
                
                if (compressionLevel > 0) {
                    compressedSlotCount++;
                    if (compressedSlotCount == 1) {
                        singleCompressedItem = stack;
                    }
                }
            }
        }
        
        // CRITICAL: If no compressed blocks, let vanilla handle it - DO NOT INTERFERE AT ALL
        if (compressedSlotCount == 0) {
            // CompressyMod.LOGGER.debug("  No compressed blocks found ({} item slots), letting vanilla handle it - NOT BLOCKING", totalItemSlots);
            return; // Early return - do not touch result slot at all
        }
        
        // If multiple SLOTS have compressed blocks, check if it's a valid compression recipe
        if (compressedSlotCount > 1) {
            if (isValidCompressionRecipe(craftingInventory)) {
                // Valid compression recipe (9 identical compressed blocks) - allow it
                // CompressyMod.LOGGER.info("  Multiple compressed blocks form valid compression recipe - allowing");
                return; // Let CompressionRecipe handle it
            } else {
                // Invalid pattern - block to prevent vanilla recipes
                // CompressyMod.LOGGER.warn("  Multiple slots ({}) contain compressed blocks but NOT a valid compression recipe - BLOCKING", compressedSlotCount);
                CraftingScreenHandler handler = (CraftingScreenHandler)(Object)this;
                Slot resultSlot = handler.getSlot(0);
                if (resultSlot != null) {
                    resultSlot.setStack(ItemStack.EMPTY);
                    // CompressyMod.LOGGER.info("  Result slot cleared - crafting blocked");
                }
                return; // Block crafting completely
            }
        }
        
        // Exactly one slot has compressed blocks - proceed with decompression
        if (singleCompressedItem.isEmpty()) {
            // CompressyMod.LOGGER.debug("  Grid is empty");
            return;
        }
        
        // CompressyMod.LOGGER.info("  Found single compressed slot: {} x{}", 
        //     net.minecraft.registry.Registries.ITEM.getId(singleCompressedItem.getItem()).toString(), 
        //     singleCompressedItem.getCount());
        
        // Check compression level
        int compressionLevel = getCompressionLevel(singleCompressedItem);
        // CompressyMod.LOGGER.info("  Compression level: {}", compressionLevel);
        
        if (compressionLevel <= 0) {
            // CompressyMod.LOGGER.warn("  Compression level is 0 but slot was marked as compressed - this shouldn't happen");
            return;
        }
        
        // IT'S A COMPRESSED BLOCK! Set result IMMEDIATELY before vanilla can interfere
        // CompressyMod.LOGGER.info("*** COMPRESSED BLOCK DETECTED! Level {} - Setting result BEFORE vanilla ***", compressionLevel);
        
        String blockId = getCompressedBlockId(singleCompressedItem);
        // CompressyMod.LOGGER.info("  Block ID: '{}'", blockId);
        if (blockId.isEmpty()) {
            blockId = net.minecraft.registry.Registries.ITEM.getId(singleCompressedItem.getItem()).toString();
            // CompressyMod.LOGGER.warn("  Block ID was empty, using item ID: {}", blockId);
        }
        
        // Create decompressed result
        ItemStack result = createDecompressedResult(singleCompressedItem, compressionLevel, blockId, world);
        
        if (!result.isEmpty()) {
            // Set result slot IMMEDIATELY - this happens BEFORE vanilla calculates
            CraftingScreenHandler handler = (CraftingScreenHandler)(Object)this;
            Slot resultSlot = handler.getSlot(0);
            if (resultSlot != null) {
                resultSlot.setStack(result);
                // CompressyMod.LOGGER.info("*** SUCCESS! Result set to {} x{} BEFORE vanilla calculation ***", 
                //     net.minecraft.registry.Registries.ITEM.getId(result.getItem()).toString(), result.getCount());
            }
            // else {
            //     CompressyMod.LOGGER.error("  ERROR: Result slot is null!");
            // }
        }
        // else {
        //     CompressyMod.LOGGER.error("  ERROR: createDecompressedResult returned EMPTY!");
        // }
    }
    
    @Inject(
        method = "onContentChanged",
        at = @At("TAIL")
    )
    private void overrideCraftingResultAfterVanilla(net.minecraft.inventory.Inventory inventory, CallbackInfo ci) {
        // Double-check after vanilla - if vanilla overwrote our result, fix it
        if (player == null) {
            return;
        }
        
        World world = player.getEntityWorld();
        if (world == null || world.isClient()) {
            return;
        }
        
        // Need to cast to CraftingInventory
        if (!(inventory instanceof CraftingInventory craftingInventory)) {
            return;
        }
        
        // Check for compressed blocks again - count SLOTS with compressed blocks
        ItemStack singleCompressedItem = ItemStack.EMPTY;
        int compressedSlotCount = 0;
        
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (!stack.isEmpty()) {
                int compressionLevel = getCompressionLevel(stack);
                if (compressionLevel > 0) {
                    compressedSlotCount++;
                    if (compressedSlotCount == 1) {
                        singleCompressedItem = stack;
                    }
                }
            }
        }
        
        // CRITICAL: If no compressed blocks, let vanilla handle it - DO NOT INTERFERE AT ALL
        if (compressedSlotCount == 0) {
            return; // Early return - do not touch result slot at all
        }
        
        // If multiple slots have compressed blocks, check if it's a valid compression recipe
        if (compressedSlotCount > 1) {
            if (isValidCompressionRecipe(craftingInventory)) {
                // Valid compression recipe - allow it
                return; // Let CompressionRecipe handle it
            } else {
                // Invalid pattern - block to prevent vanilla recipes
                // CompressyMod.LOGGER.warn("TAIL: Multiple slots ({}) contain compressed blocks but NOT a valid compression recipe - BLOCKING", compressedSlotCount);
                CraftingScreenHandler handler = (CraftingScreenHandler)(Object)this;
                Slot resultSlot = handler.getSlot(0);
                if (resultSlot != null) {
                    resultSlot.setStack(ItemStack.EMPTY);
                }
                return;
            }
        }
        
        // If not exactly one compressed block, let vanilla handle it
        if (singleCompressedItem.isEmpty()) {
            return;
        }
        
        int compressionLevel = getCompressionLevel(singleCompressedItem);
        if (compressionLevel <= 0) {
            return;
        }
        
        // Verify the result is correct - if vanilla overwrote it, fix it
        CraftingScreenHandler handler = (CraftingScreenHandler)(Object)this;
        Slot resultSlot = handler.getSlot(0);
        if (resultSlot != null) {
            ItemStack currentResult = resultSlot.getStack();
            
            // Check if vanilla overwrote our result (wrong item or wrong count)
            String blockId = getCompressedBlockId(singleCompressedItem);
            if (blockId.isEmpty()) {
                blockId = net.minecraft.registry.Registries.ITEM.getId(singleCompressedItem.getItem()).toString();
            }
            
            ItemStack correctResult = createDecompressedResult(singleCompressedItem, compressionLevel, blockId, world);
            
            // If result is wrong, fix it
            if (!correctResult.isEmpty() && 
                (currentResult.isEmpty() || 
                 !net.minecraft.registry.Registries.ITEM.getId(currentResult.getItem()).equals(
                     net.minecraft.registry.Registries.ITEM.getId(correctResult.getItem())) ||
                 currentResult.getCount() != correctResult.getCount())) {
                // CompressyMod.LOGGER.warn("Vanilla overwrote result! Fixing: {} -> {}", 
                //     currentResult.isEmpty() ? "EMPTY" : net.minecraft.registry.Registries.ITEM.getId(currentResult.getItem()).toString(),
                //     net.minecraft.registry.Registries.ITEM.getId(correctResult.getItem()).toString());
                resultSlot.setStack(correctResult);
            }
        }
    }
    
    private int getCompressionLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        var customData = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        var nbt = customData.copyNbt();
        if (nbt == null) {
            return 0;
        }
        return com.compressy.util.NbtHelper.getInt(nbt, "compressed_level", 0);
    }
    
    private String getCompressedBlockId(ItemStack stack) {
        var customData = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return "";
        }
        var nbt = customData.copyNbt();
        return com.compressy.util.NbtHelper.getString(nbt, "compressed_block", "");
    }
    
    /**
     * Check if the crafting grid forms a valid compression recipe.
     * A valid compression recipe requires:
     * - All 9 slots filled
     * - All items are the same (ItemStack.areItemsEqual)
     * - If compressed, all have the same compression level and block ID
     * - Compression level < 32 (if compressed)
     */
    private boolean isValidCompressionRecipe(CraftingInventory craftingInventory) {
        // Must be a 3x3 grid (9 slots)
        if (craftingInventory.size() != 9) {
            return false;
        }
        
        // Get the first item to compare against
        ItemStack firstStack = craftingInventory.getStack(0);
        if (firstStack.isEmpty()) {
            return false;
        }
        
        // Check compression level if already compressed
        int firstLevel = getCompressionLevel(firstStack);
        if (firstLevel >= 32) {
            return false; // Max level reached
        }
        
        String firstBlockId = getCompressedBlockId(firstStack);
        
        // All 9 slots must contain the same item
        for (int i = 0; i < 9; i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (stack.isEmpty()) {
                return false; // All slots must be filled
            }
            
            // Items must be equal (same item type)
            if (!ItemStack.areItemsEqual(stack, firstStack)) {
                return false;
            }
            
            // If items are compressed, they must have the same compression data
            if (firstLevel > 0) {
                int otherLevel = getCompressionLevel(stack);
                String otherBlockId = getCompressedBlockId(stack);
                if (otherLevel != firstLevel || !firstBlockId.equals(otherBlockId)) {
                    return false;
                }
            }
        }
        
        return true; // Valid compression recipe!
    }
    
    private ItemStack createDecompressedResult(ItemStack compressedBlock, int level, String blockId, World world) {
        if (level == 1) {
            // Return 9 of the original block
            var block = net.minecraft.registry.Registries.BLOCK.get(net.minecraft.util.Identifier.of(blockId));
            if (block == null || block == net.minecraft.block.Blocks.AIR) {
                var item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(blockId));
                if (item != null && item != net.minecraft.item.Items.AIR) {
                    return new ItemStack(item, 9);
                }
                return new ItemStack(compressedBlock.getItem(), 9);
            }
            var item = block.asItem();
            if (item == null || item == net.minecraft.item.Items.AIR) {
                return new ItemStack(compressedBlock.getItem(), 9);
            }
            return new ItemStack(item, 9);
        } else {
            // Return 9 of the lower compression level - use DecompressionRecipe's logic
            com.compressy.recipe.DecompressionRecipe recipe = new com.compressy.recipe.DecompressionRecipe(
                net.minecraft.recipe.book.CraftingRecipeCategory.MISC
            );
            net.minecraft.recipe.input.CraftingRecipeInput fakeInput = net.minecraft.recipe.input.CraftingRecipeInput.create(1, 1, java.util.List.of(compressedBlock));
            net.minecraft.registry.RegistryWrapper.WrapperLookup registryWrapper = world.getRegistryManager();
            return recipe.craft(fakeInput, registryWrapper);
        }
    }
}

