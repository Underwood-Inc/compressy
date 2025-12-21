package com.compressedblocks.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;

/**
 * Serializer for the decompression recipe.
 * Since this is a special recipe that doesn't need JSON configuration,
 * the serializer is mostly empty.
 */
public class DecompressionRecipeSerializer implements RecipeSerializer<DecompressionRecipe> {
    
    public static final DecompressionRecipeSerializer INSTANCE = new DecompressionRecipeSerializer();
    
    private static final MapCodec<DecompressionRecipe> CODEC = MapCodec.unit(
        () -> new DecompressionRecipe(CraftingRecipeCategory.MISC)
    );
    
    private static final PacketCodec<RegistryByteBuf, DecompressionRecipe> PACKET_CODEC = PacketCodec.unit(
        new DecompressionRecipe(CraftingRecipeCategory.MISC)
    );

    @Override
    public MapCodec<DecompressionRecipe> codec() {
        return CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, DecompressionRecipe> packetCodec() {
        return PACKET_CODEC;
    }
}
