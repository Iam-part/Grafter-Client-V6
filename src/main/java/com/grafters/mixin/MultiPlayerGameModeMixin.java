package com.grafters.mixin;

import com.grafters.mods.Criticals;
import com.grafters.mods.MaceSwap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs Mace Swap and Criticals right before every attack. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void grafters$beforeAttack(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        MaceSwap.before(mc);
        Criticals.before(mc);
    }
}
