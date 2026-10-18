package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * While you hold right-click on a respawn anchor: charges it with glowstone, then switches to
 * something else and detonates it. Needs glowstone in your hotbar.
 */
public final class AnchorMacro {
    private AnchorMacro() {}

    private static int delay;

    static boolean isAnchor(BlockState st) {
        return BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath().equals("respawn_anchor");
    }

    /** A hotbar slot that is not glowstone (a totem if you have one), so right-click detonates. */
    static int explodeSlot(LocalPlayer p) {
        int totem = CombatUtil.hotbar(p, "totem_of_undying");
        if (totem >= 0) return totem;
        int cur = p.getInventory().getSelectedSlot();
        if (!Util.itemId(p.getInventory().getItem(cur)).equals("glowstone")) return cur;
        return (cur + 1) % 9;
    }

    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || GraftersState.screen(mc) != null || Freecam.isActive()) return;
        if (!GraftersState.physical(mc, mc.options.keyUse)) return;
        if (--delay > 0) return;

        HitResult hr = mc.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || hr.getType() != HitResult.Type.BLOCK) return;
        BlockState st = mc.level.getBlockState(bhr.getBlockPos());
        if (!isAnchor(st)) return;

        int charge = st.getValue(BlockStateProperties.RESPAWN_ANCHOR_CHARGES);
        if (charge == 0) {
            int glow = CombatUtil.hotbar(p, "glowstone");
            if (glow < 0) return;
            CombatUtil.select(p, glow);
        } else {
            CombatUtil.select(p, explodeSlot(p));
        }
        CombatUtil.use(mc, bhr.getBlockPos(), bhr.getDirection(), bhr.getLocation());
        delay = c.anchorDelay;
    }
}
