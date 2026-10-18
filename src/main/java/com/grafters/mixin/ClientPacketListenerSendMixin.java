package com.grafters.mixin;

import com.grafters.mods.Fakelag;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fakelag: lets Fakelag hold back your movement packets instead of sending them right away. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerSendMixin {
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void grafters$fakelag(Packet<?> packet, CallbackInfo ci) {
        if (Fakelag.intercept(packet)) ci.cancel();
    }
}
