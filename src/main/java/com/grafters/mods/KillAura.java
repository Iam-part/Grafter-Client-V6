package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

/** Attacks the nearest player in range as soon as your attack is charged. */
public final class KillAura {
    private KillAura() {}

    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (p == null || GraftersState.screen(mc) != null || Freecam.isActive()) return;

        AbstractClientPlayer t = CombatUtil.target(mc, c.kaRange);
        if (t == null) return;
        if (c.kaLos && !p.hasLineOfSight(t)) return;

        if (c.kaRotate) CombatUtil.aimAt(p, t.getBoundingBox().getCenter(), 60f);
        if (p.getAttackStrengthScale(0f) >= c.kaCooldown) CombatUtil.attack(mc, t);
    }
}
