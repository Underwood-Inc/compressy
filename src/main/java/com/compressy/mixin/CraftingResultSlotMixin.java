package com.compressy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.compressy.CompressyMod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;

/**
 * Mixin to fix the duplication bug when taking decompressed items from crafting.
 * 
 * THE PROBLEM:
 * When we override the crafting result slot directly in CraftingScreenHandlerMixin,
 * we bypass the normal recipe system. When the player takes the result, vanilla's
 * consumption logic doesn't know what to consume, leading to duplication.
 * 
 * THE FIX:
 * Intercept onTakeItem and manually consume the compressed block input when
 * the player is taking a decompression result.
 */
@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {
    
    @Shadow
    @Final
    private RecipeInputInventory input;
    
    @Shadow
    @Final
    private PlayerEntity player;
    
    @Inject(
        method = "onTakeItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void handleDecompressionConsumption(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player == null || input == null) {
            return;
        }
        
        // Find the compressed block in the input
        ItemStack compressedBlock = ItemStack.EMPTY;
        int compressedSlotIndex = -1;
        int itemCount = 0;
        
        for (int i = 0; i < input.size(); i++) {
            ItemStack inputStack = input.getStack(i);
            if (!inputStack.isEmpty()) {
                itemCount++;
                int compressionLevel = getCompressionLevel(inputStack);
                if (compressionLevel > 0 && compressedBlock.isEmpty()) {
                    compressedBlock = inputStack;
                    compressedSlotIndex = i;
                }
            }
        }
        
        // Only handle if there's exactly one item and it's compressed (decompression pattern)
        if (itemCount != 1 || compressedBlock.isEmpty() || compressedSlotIndex < 0) {
            return;
        }
        
        int compressionLevel = getCompressionLevel(compressedBlock);
        if (compressionLevel <= 0) {
            return;
        }
        
        // String gridType = input.size() == 4 ? "2x2 player inventory" : "3x3 crafting table";
        // CompressyMod.LOGGER.info("CraftingResultSlotMixin: Handling decompression in {} for level {} block (stack size: {})", 
        //     gridType, compressionLevel, compressedBlock.getCount());
        
        // Consume ONE compressed block from the input
        // IMPORTANT: Always use setStack to ensure inventory listeners are triggered!
        // This is critical for shift-click (sequential crafting) to work properly.
        if (compressedBlock.getCount() > 1) {
            ItemStack remaining = compressedBlock.copy();
            remaining.setCount(compressedBlock.getCount() - 1);
            input.setStack(compressedSlotIndex, remaining);
        } else {
            input.setStack(compressedSlotIndex, ItemStack.EMPTY);
        }
        
        // Mark input as dirty to trigger onContentChanged for result recalculation
        input.markDirty();
        
        // CompressyMod.LOGGER.info("CraftingResultSlotMixin: Consumed 1 compressed block from slot {} in {} (remaining: {})", 
        //     compressedSlotIndex, gridType, input.getStack(compressedSlotIndex).getCount());
        
        // CRITICAL: Cancel vanilla's onTakeItem to prevent double-consumption!
        // We've already consumed the input, don't let vanilla do it again.
        ci.cancel();
    }
    
    
    @Unique
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
}
