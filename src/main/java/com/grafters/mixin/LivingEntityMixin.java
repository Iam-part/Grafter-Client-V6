package com.grafters.mixin;

import com.grafters.mods.SlowSwing;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SlowSwing hook: the renderer asks the player for its swing progress every frame; for the local
 * player we answer with SlowSwing's smooth, real-time value. Lives in the non-required visual
 * config, so if the target ever changes the game still starts (it just logs a warning).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getAttackAnim", at = @At("RETURN"), cancellable = true)
    private void grafters$slowSwing(CallbackInfoReturnable<Float> cir) {
        if ((Object) this != Minecraft.getInstance().player) return;
        Float v = SlowSwing.animation();
        if (v != null) cir.setReturnValue(v);
    }
}
