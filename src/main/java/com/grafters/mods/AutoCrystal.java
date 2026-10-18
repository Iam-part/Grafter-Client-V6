package com.grafters.mods;

import com.grafters.Freecam;
import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Crystal PvP automation. Breaks end crystals within reach (never ones that are too close to you),
 * and places new ones on obsidian/bedrock as close to the nearest player as it can reach.
 * Needs end crystals in your hotbar.
 */
public final class AutoCrystal {
    private AutoCrystal() {}

    private static int placeCd, breakCd;

    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || GraftersState.screen(mc) != null || Freecam.isActive()) return;
        if (placeCd > 0) placeCd--;
        if (breakCd > 0) breakCd--;

        // 1) break crystals in reach, but not ones that would hurt you
        if (breakCd <= 0) {
            for (Entity e : mc.level.getEntities(p, p.getBoundingBox().inflate(5.0), CombatUtil::isCrystal)) {
                double d = p.distanceTo(e);
                if (d <= 4.5 && d >= c.crystalSafe) {
                    CombatUtil.attack(mc, e);
                    breakCd = c.crystalBreak;
                    return;
                }
            }
        }

        // 2) place a new one
        if (placeCd > 0) return;
        AbstractClientPlayer t = CombatUtil.target(mc, c.crystalRange);
        if (t == null) return;
        int slot = CombatUtil.hotbar(p, "end_crystal");
        if (slot < 0) return;

        BlockPos base = p.blockPosition();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -4; dy <= 4; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    BlockState st = mc.level.getBlockState(pos);
                    String id = BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath();
                    if (!id.equals("obsidian") && !id.equals("bedrock")) continue;
                    if (!mc.level.getBlockState(pos.above()).isAir() || !mc.level.getBlockState(pos.above(2)).isAir()) continue;

                    Vec3 top = CombatUtil.topCenter(pos);
                    if (p.getEyePosition().distanceTo(top) > 4.5) continue;
                    if (p.position().distanceTo(top) < c.crystalSafe) continue;
                    AABB box = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
                    if (!mc.level.getEntities((Entity) null, box, e -> true).isEmpty()) continue;

                    double d = t.position().distanceTo(top);       // closer to them = more damage
                    if (d < bestD) { bestD = d; best = pos.immutable(); }
                }
        if (best == null || bestD > 6.0) return;

        CombatUtil.select(p, slot);
        CombatUtil.use(mc, best, Direction.UP, CombatUtil.topCenter(best));
        placeCd = c.crystalPlace;
    }
}
