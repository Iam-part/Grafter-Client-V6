package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Attacks automatically when your crosshair is on a player and your attack is charged. */
public final class TriggerBot {
    private TriggerBot() {}

    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (p == null || GraftersState.screen(mc) != null || Freecam.isActive()) return;

        HitResult hr = mc.hitResult;
        if (!(hr instanceof EntityHitResult ehr)) return;
        Entity e = ehr.getEntity();
        if (!(e instanceof Player) || !e.isAlive() || e == p) return;
        if (p.getAttackStrengthScale(0f) >= c.tbCooldown) CombatUtil.attack(mc, e);
    }
}
