package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Gently pulls your aim toward the nearest player inside your field of view. */
public final class AimAssist {
    private AimAssist() {}

    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (p == null || GraftersState.screen(mc) != null || Freecam.isActive()) return;
        if (c.aaClick && !GraftersState.physical(mc, mc.options.keyAttack)) return;

        AbstractClientPlayer t = CombatUtil.target(mc, c.aaRange);
        if (t == null) return;
        Vec3 aim = t.getBoundingBox().getCenter();
        float off = Math.abs(Mth.wrapDegrees(CombatUtil.yawTo(p.getEyePosition(), aim) - p.getYRot()));
        if (off > c.aaFov / 2f) return;
        CombatUtil.aimAt(p, aim, c.aaSpeed);
    }
}
