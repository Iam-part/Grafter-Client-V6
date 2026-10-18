package com.grafters.mods;

import com.grafters.GraftersConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Switches to a mace from your hotbar for the hit (when falling, if you like), then swaps back. */
public final class MaceSwap {
    private MaceSwap() {}

    private static int prev = -1;

    /** From MultiPlayerGameModeMixin, right before an attack goes out. */
    public static void before(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (!c.maceSwap || p == null) return;
        if (c.maceFalling && p.fallDistance < 1.5) return;

        int mace = CombatUtil.hotbar(p, "mace");
        int cur = p.getInventory().getSelectedSlot();
        if (mace < 0 || mace == cur) return;
        prev = cur;
        CombatUtil.select(p, mace);
    }

    public static void tick(Minecraft mc) {
        if (prev >= 0 && mc.player != null && GraftersConfig.get().maceBack) CombatUtil.select(mc.player, prev);
        prev = -1;
    }
}
