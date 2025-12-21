package com.compressy;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

/**
 * LITE version of compressed block handler.
 * 
 * Simply PREVENTS placement of compressed blocks entirely.
 * No marker entities, no break handlers, no overlays.
 * 
 * Benefits:
 * - Zero world overhead
 * - No entity spawning
 * - No block break interception
 * - Simpler, faster, lighter
 * 
 * Trade-off:
 * - Compressed blocks are inventory-only items
 * - Cannot be placed in world as decoration
 */
public class CompressyBlockHandlerLite {
    
    /**
     * Register the LITE handler - just blocks all placement
     */
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack heldItem = player.getStackInHand(hand);
            
            // Check if it's a compressed block
            int level = getCompressionLevel(heldItem);
            if (level <= 0) {
                return ActionResult.PASS; // Not compressed, let vanilla handle it
            }
            
            // It's a compressed block - BLOCK PLACEMENT!
            if (!world.isClient()) {
                player.sendMessage(
                    Text.literal("⚠ ")
                        .styled(s -> s.withColor(Formatting.GOLD))
                        .append(Text.literal("Compressed blocks cannot be placed!")
                            .styled(s -> s.withColor(Formatting.YELLOW)))
                        .append(Text.literal(" (LITE mode)")
                            .styled(s -> s.withColor(Formatting.GRAY))),
                    true
                );
            }
            
            // Return FAIL to prevent placement
            return ActionResult.FAIL;
        });
        
        CompressyMod.LOGGER.info("Compressed block handler (LITE) registered - placement disabled");
    }
    
    private static int getCompressionLevel(ItemStack stack) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return 0;
        var nbt = customData.copyNbt();
        return com.compressy.util.NbtHelper.getInt(nbt, "compressed_level", 0);
    }
}


