package com.compressy.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;

/**
 * Serializer for the compression recipe.
 * Since this is a special recipe that doesn't need JSON configuration,
 * the serializer is mostly empty.
 */
public class CompressionRecipeSerializer implements RecipeSerializer<CompressionRecipe> {
    
    public static final CompressionRecipeSerializer INSTANCE = new CompressionRecipeSerializer();
    
    private static final MapCodec<CompressionRecipe> CODEC = MapCodec.unit(
        () -> new CompressionRecipe(CraftingRecipeCategory.MISC)
    );
    
    private static final PacketCodec<RegistryByteBuf, CompressionRecipe> PACKET_CODEC = PacketCodec.unit(
        new CompressionRecipe(CraftingRecipeCategory.MISC)
    );

    @Override
    public MapCodec<CompressionRecipe> codec() {
        return CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, CompressionRecipe> packetCodec() {
        return PACKET_CODEC;
    }
}


