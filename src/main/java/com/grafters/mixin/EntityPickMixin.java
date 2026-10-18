package com.grafters.mixin;

import com.grafters.mods.Hitboxes;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** HitBox / Static HitBoxes: grows the box the crosshair targets for other players. */
@Mixin(Entity.class)
public abstract class EntityPickMixin {
    @Inject(method = "getPickRadius", at = @At("RETURN"), cancellable = true)
    private void grafters$pick(CallbackInfoReturnable<Float> cir) {
        float extra = Hitboxes.extra((Entity) (Object) this);
        if (extra > 0f) cir.setReturnValue(cir.getReturnValue() + extra);
    }
}
