package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Hold right-click on a block with respawn anchors and glowstone in your hotbar: places an anchor,
 * charges it, stacks a second anchor on top, charges that, then detonates both in a row.
 * Runs once per press. Release right-click to reset.
 */
public final class DoubleAnchor {
    private DoubleAnchor() {}

    private enum S { IDLE, CHARGE1, PLACE2, CHARGE2, EXPLODE1, EXPLODE2, DONE }

    private static S state = S.IDLE;
    private static int delay, guard;
    private static BlockPos a1, a2;

    public static void reset() { state = S.IDLE; delay = 0; guard = 0; }

    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || GraftersState.screen(mc) != null || Freecam.isActive()) { reset(); return; }
        if (!GraftersState.physical(mc, mc.options.keyUse)) { reset(); return; }
        if (state == S.DONE) return;                         // wait for you to let go
        if (--delay > 0) return;

        int anchor = CombatUtil.hotbar(p, "respawn_anchor");
        int glow = CombatUtil.hotbar(p, "glowstone");

        switch (state) {
            case IDLE -> {
                HitResult hr = mc.hitResult;
                if (!(hr instanceof BlockHitResult bhr) || hr.getType() != HitResult.Type.BLOCK) return;
                if (anchor < 0 || glow < 0) return;
                a1 = bhr.getBlockPos().relative(bhr.getDirection());
                CombatUtil.select(p, anchor);
                CombatUtil.use(mc, bhr.getBlockPos(), bhr.getDirection(), bhr.getLocation());
                next(S.CHARGE1, c);
            }
            case CHARGE1 -> {
                if (!AnchorMacro.isAnchor(mc.level.getBlockState(a1))) { waitForBlock(); return; }
                CombatUtil.select(p, glow);
                CombatUtil.use(mc, a1, Direction.UP, CombatUtil.topCenter(a1));
                next(S.PLACE2, c);
            }
            case PLACE2 -> {
                CombatUtil.select(p, anchor);
                CombatUtil.use(mc, a1, Direction.UP, CombatUtil.topCenter(a1));
                a2 = a1.above();
                next(S.CHARGE2, c);
            }
            case CHARGE2 -> {
                if (!AnchorMacro.isAnchor(mc.level.getBlockState(a2))) { waitForBlock(); return; }
                CombatUtil.select(p, glow);
                CombatUtil.use(mc, a2, Direction.UP, CombatUtil.topCenter(a2));
                next(S.EXPLODE1, c);
            }
            case EXPLODE1 -> {
                CombatUtil.select(p, AnchorMacro.explodeSlot(p));
                Vec3 side = Vec3.atCenterOf(a1).add(0.0, 0.0, -0.5);
                CombatUtil.use(mc, a1, Direction.NORTH, side);
                next(S.EXPLODE2, c);
            }
            case EXPLODE2 -> {
                CombatUtil.use(mc, a2, Direction.UP, CombatUtil.topCenter(a2));
                state = S.DONE;
            }
            default -> { }
        }
    }

    private static void next(S s, GraftersConfig c) { state = s; delay = c.doubleAnchorDelay; guard = 0; }

    /** The block we just placed has not shown up yet: give it a moment, then give up. */
    private static void waitForBlock() { if (++guard > 20) state = S.DONE; }
}
