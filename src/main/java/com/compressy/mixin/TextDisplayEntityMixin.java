package com.compressy.mixin;

import com.compressy.CompressyBlockHandler;
import com.compressy.config.CompressyConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side mixin to hide TEXT_DISPLAY entities with compressy.label tag
 * when the client config has showRomanNumerals disabled.
 * 
 * This allows per-player control over roman numeral visibility.
 */
@Mixin(DisplayEntityRenderer.TextDisplayEntityRenderer.class)
public class TextDisplayEntityMixin {
    
    @Inject(
        method = "render(Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hideCompressyLabelsIfDisabled(
        DisplayEntity.TextDisplayEntity entity,
        float yaw,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        // Check if this is a compressy label entity
        if (entity.getCommandTags().contains(CompressyBlockHandler.LABEL_TAG)) {
            // Check client config - if disabled, don't render
            if (!CompressyConfig.get().showRomanNumerals) {
                ci.cancel(); // Skip rendering
            }
        }
    }
}

