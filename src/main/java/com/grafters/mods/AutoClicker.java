package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Random;

/** While you hold attack, sends real left-clicks at your chosen clicks per second. */
public final class AutoClicker {
    private AutoClicker() {}

    private static final Random RNG = new Random();
    private static int timer;

    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (p == null || GraftersState.screen(mc) != null || Freecam.isActive()) return;
        if (!GraftersState.physical(mc, mc.options.keyAttack)) { timer = 0; return; }

        if (--timer > 0) return;
        if (c.acCooldown && p.getAttackStrengthScale(0f) < 0.9f) return;
        KeyMapping.click(KeyMappingHelper.getBoundKeyOf(mc.options.keyAttack));
        timer = Math.max(1, Math.round(20f / Math.max(1, c.acCps) * (0.8f + 0.4f * RNG.nextFloat())));
    }
}
