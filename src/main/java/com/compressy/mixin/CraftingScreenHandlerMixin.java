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
    private void overrideCraftingResultBeforeVanilla(net.minecraft.inventory.Inventory inventory, CallbackInfo ci) {
        CompressyMod.LOGGER.debug("CraftingScreenHandlerMixin.onContentChanged: CALLED AT HEAD");
        
        // Get the world from the player
        if (player == null) {
            return;
        }
        
        World world = player.getEntityWorld();
        if (world == null || world.isClient()) {
            return;
        }
        
        // Check if there's exactly one item (decompression pattern) - CHECK FIRST BEFORE VANILLA
        // Need to cast to CraftingInventory to access the crafting grid
        if (!(inventory instanceof CraftingInventory craftingInventory)) {
            return;
        }
        
        ItemStack singleItem = ItemStack.EMPTY;
        int itemCount = 0;
        
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (itemCount > 1) {
                    return; // More than one item, let vanilla handle it
                }
                singleItem = stack;
            }
        }
        
        if (singleItem.isEmpty()) {
            return;
        }
        
        // Check if it's a compressed block - THIS MUST HAPPEN BEFORE VANILLA CALCULATES RESULT
        int compressionLevel = getCompressionLevel(singleItem);
        if (compressionLevel <= 0) {
            return; // Not compressed, let vanilla handle it
        }
        
        // IT'S A COMPRESSED BLOCK! Set result IMMEDIATELY before vanilla can interfere
        CompressyMod.LOGGER.info("*** COMPRESSED BLOCK DETECTED AT HEAD! Level {} - Setting result BEFORE vanilla ***", compressionLevel);
        
        String blockId = getCompressedBlockId(singleItem);
        if (blockId.isEmpty()) {
            blockId = net.minecraft.registry.Registries.ITEM.getId(singleItem.getItem()).toString();
        }
        
        // Create decompressed result
        ItemStack result = createDecompressedResult(singleItem, compressionLevel, blockId, world);
        
        if (!result.isEmpty()) {
            // Set result slot IMMEDIATELY - this happens BEFORE vanilla calculates
            CraftingScreenHandler handler = (CraftingScreenHandler)(Object)this;
            Slot resultSlot = handler.getSlot(0);
            if (resultSlot != null) {
                resultSlot.setStack(result);
                CompressyMod.LOGGER.info("*** Result set to {} x{} BEFORE vanilla calculation ***", 
                    net.minecraft.registry.Registries.ITEM.getId(result.getItem()).toString(), result.getCount());
            }
        }
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
        
        // Check for single compressed block again
        ItemStack singleItem = ItemStack.EMPTY;
        int itemCount = 0;
        
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (itemCount > 1) {
                    return;
                }
                singleItem = stack;
            }
        }
        
        if (singleItem.isEmpty()) {
            return;
        }
        
        int compressionLevel = getCompressionLevel(singleItem);
        if (compressionLevel <= 0) {
            return;
        }
        
        // Verify the result is correct - if vanilla overwrote it, fix it
        CraftingScreenHandler handler = (CraftingScreenHandler)(Object)this;
        Slot resultSlot = handler.getSlot(0);
        if (resultSlot != null) {
            ItemStack currentResult = resultSlot.getStack();
            
            // Check if vanilla overwrote our result (wrong item or wrong count)
            String blockId = getCompressedBlockId(singleItem);
            if (blockId.isEmpty()) {
                blockId = net.minecraft.registry.Registries.ITEM.getId(singleItem.getItem()).toString();
            }
            
            ItemStack correctResult = createDecompressedResult(singleItem, compressionLevel, blockId, world);
            
            // If result is wrong, fix it
            if (!correctResult.isEmpty() && 
                (currentResult.isEmpty() || 
                 !net.minecraft.registry.Registries.ITEM.getId(currentResult.getItem()).equals(
                     net.minecraft.registry.Registries.ITEM.getId(correctResult.getItem())) ||
                 currentResult.getCount() != correctResult.getCount())) {
                CompressyMod.LOGGER.warn("Vanilla overwrote result! Fixing: {} -> {}", 
                    currentResult.isEmpty() ? "EMPTY" : net.minecraft.registry.Registries.ITEM.getId(currentResult.getItem()).toString(),
                    net.minecraft.registry.Registries.ITEM.getId(correctResult.getItem()).toString());
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

