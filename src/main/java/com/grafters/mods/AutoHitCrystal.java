package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Hits any end crystal under your crosshair automatically (e.g. one you just placed). */
public final class AutoHitCrystal {
    private AutoHitCrystal() {}

    private static int delay;

    public static void tick(Minecraft mc) {
        if (mc.player == null || GraftersState.screen(mc) != null || Freecam.isActive()) return;
        if (--delay > 0) return;
        HitResult hr = mc.hitResult;
        if (hr instanceof EntityHitResult ehr && CombatUtil.isCrystal(ehr.getEntity())) {
            CombatUtil.attack(mc, ehr.getEntity());
            delay = GraftersConfig.get().hitCrystalDelay;
        }
    }
}
