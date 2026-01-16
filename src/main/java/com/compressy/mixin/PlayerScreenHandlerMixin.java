package com.compressy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compressy.CompressyMod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

/**
 * Mixin for the 2x2 player inventory crafting grid.
 * 
 * This handles decompression in the player's inventory crafting grid,
 * which is separate from the 3x3 crafting table (CraftingScreenHandler).
 * 
 * Without this mixin, placing a compressed block in the 2x2 grid would
 * match vanilla recipes (like logs → planks) instead of decompressing.
 */
@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {
    
    @Shadow
    @Final
    private PlayerEntity owner;
    
    @Inject(
        method = "onContentChanged",
        at = @At("HEAD")
    )
    private void checkForCompressedBlockHead(Inventory inventory, CallbackInfo ci) {
        if (owner == null) {
            return;
        }
        
        World world = owner.getEntityWorld();
        if (world == null || world.isClient()) {
            return;
        }
        
        // Only handle RecipeInputInventory (crafting grid)
        if (!(inventory instanceof RecipeInputInventory craftingInput)) {
            return;
        }
        
        // 2x2 grid has 4 slots
        if (craftingInput.size() != 4) {
            return;
        }
        
        // Count compressed blocks in the 2x2 grid
        ItemStack singleCompressedItem = ItemStack.EMPTY;
        int compressedSlotCount = 0;
        int totalItemSlots = 0;
        int compressedSlotIndex = -1;
        
        for (int i = 0; i < craftingInput.size(); i++) {
            ItemStack stack = craftingInput.getStack(i);
            if (!stack.isEmpty()) {
                totalItemSlots++;
                int compressionLevel = getCompressionLevel(stack);
                
                if (compressionLevel > 0) {
                    compressedSlotCount++;
                    if (compressedSlotCount == 1) {
                        singleCompressedItem = stack;
                        compressedSlotIndex = i;
                    }
                }
            }
        }
        
        // If no compressed blocks, let vanilla handle it
        if (compressedSlotCount == 0) {
            return;
        }
        
        // Get the result slot (slot 0 in PlayerScreenHandler is the crafting result)
        PlayerScreenHandler handler = (PlayerScreenHandler)(Object)this;
        Slot resultSlot = handler.getSlot(0);
        
        // Multiple compressed blocks in 2x2 - can't compress (need 3x3), block crafting
        if (compressedSlotCount > 1) {
            if (resultSlot != null) {
                resultSlot.setStack(ItemStack.EMPTY);
            }
            return;
        }
        
        // Exactly one compressed block - decompression!
        if (singleCompressedItem.isEmpty()) {
            return;
        }
        
        // Only allow decompression if it's the ONLY item in the grid
        if (totalItemSlots > 1) {
            // Multiple items but only one compressed - block to prevent weird recipes
            if (resultSlot != null) {
                resultSlot.setStack(ItemStack.EMPTY);
            }
            return;
        }
        
        int compressionLevel = getCompressionLevel(singleCompressedItem);
        if (compressionLevel <= 0) {
            return;
        }
        
        CompressyMod.LOGGER.info("PlayerScreenHandlerMixin: Compressed block detected in 2x2 grid, level {}", compressionLevel);
        
        String blockId = getCompressedBlockId(singleCompressedItem);
        if (blockId.isEmpty()) {
            blockId = net.minecraft.registry.Registries.ITEM.getId(singleCompressedItem.getItem()).toString();
        }
        
        // Create decompressed result
        ItemStack result = createDecompressedResult(singleCompressedItem, compressionLevel, blockId, world);
        
        if (!result.isEmpty() && resultSlot != null) {
            resultSlot.setStack(result);
            CompressyMod.LOGGER.info("PlayerScreenHandlerMixin: Set result to {} x{}", 
                net.minecraft.registry.Registries.ITEM.getId(result.getItem()).toString(), result.getCount());
        }
    }
    
    @Inject(
        method = "onContentChanged",
        at = @At("TAIL")
    )
    private void overrideResultAfterVanilla(Inventory inventory, CallbackInfo ci) {
        if (owner == null) {
            return;
        }
        
        World world = owner.getEntityWorld();
        if (world == null || world.isClient()) {
            return;
        }
        
        if (!(inventory instanceof RecipeInputInventory craftingInput)) {
            return;
        }
        
        if (craftingInput.size() != 4) {
            return;
        }
        
        // Check for compressed blocks again
        ItemStack singleCompressedItem = ItemStack.EMPTY;
        int compressedSlotCount = 0;
        int totalItemSlots = 0;
        
        for (int i = 0; i < craftingInput.size(); i++) {
            ItemStack stack = craftingInput.getStack(i);
            if (!stack.isEmpty()) {
                totalItemSlots++;
                int compressionLevel = getCompressionLevel(stack);
                if (compressionLevel > 0) {
                    compressedSlotCount++;
                    if (compressedSlotCount == 1) {
                        singleCompressedItem = stack;
                    }
                }
            }
        }
        
        // If no compressed blocks, don't interfere
        if (compressedSlotCount == 0) {
            return;
        }
        
        PlayerScreenHandler handler = (PlayerScreenHandler)(Object)this;
        Slot resultSlot = handler.getSlot(0);
        
        // Block crafting for invalid patterns
        if (compressedSlotCount > 1 || totalItemSlots > 1) {
            if (resultSlot != null) {
                resultSlot.setStack(ItemStack.EMPTY);
            }
            return;
        }
        
        if (singleCompressedItem.isEmpty()) {
            return;
        }
        
        int compressionLevel = getCompressionLevel(singleCompressedItem);
        if (compressionLevel <= 0) {
            return;
        }
        
        // Verify and fix the result if vanilla overwrote it
        String blockId = getCompressedBlockId(singleCompressedItem);
        if (blockId.isEmpty()) {
            blockId = net.minecraft.registry.Registries.ITEM.getId(singleCompressedItem.getItem()).toString();
        }
        
        ItemStack correctResult = createDecompressedResult(singleCompressedItem, compressionLevel, blockId, world);
        
        if (resultSlot != null) {
            ItemStack currentResult = resultSlot.getStack();
            
            if (!correctResult.isEmpty() && 
                (currentResult.isEmpty() || 
                 !net.minecraft.registry.Registries.ITEM.getId(currentResult.getItem()).equals(
                     net.minecraft.registry.Registries.ITEM.getId(correctResult.getItem())) ||
                 currentResult.getCount() != correctResult.getCount())) {
                CompressyMod.LOGGER.warn("PlayerScreenHandlerMixin TAIL: Vanilla overwrote result, fixing");
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
