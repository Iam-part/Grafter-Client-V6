package com.grafters.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Helpers shared by the combat modules. */
public final class CombatUtil {
    private CombatUtil() {}

    /** Nearest other player within range (alive, not a spectator), or null. */
    public static AbstractClientPlayer target(Minecraft mc, double range) {
        LocalPlayer me = mc.player;
        AbstractClientPlayer best = null;
        double bestD = range * range;
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == me || !p.isAlive() || p.isSpectator()) continue;
            double d = me.distanceToSqr(p);
            if (d <= bestD) { bestD = d; best = p; }
        }
        return best;
    }

    public static boolean isCrystal(Entity e) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals("end_crystal");
    }

    /** First hotbar slot holding the item with exactly this id (e.g. "totem_of_undying"), or -1. */
    public static int hotbar(LocalPlayer p, String id) {
        for (int i = 0; i < 9; i++) {
            var s = p.getInventory().getItem(i);
            if (!s.isEmpty() && Util.itemId(s).equals(id)) return i;
        }
        return -1;
    }

    public static void select(LocalPlayer p, int slot) {
        p.getInventory().setSelectedSlot(slot);
    }

    /** Hit an entity the normal way (also runs the Criticals / Mace Swap hooks). */
    public static void attack(Minecraft mc, Entity e) {
        mc.gameMode.attack(mc.player, e);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    /** Right-click a block face with whatever is selected. */
    public static void use(Minecraft mc, BlockPos pos, Direction face, Vec3 hit) {
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, pos, false));
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    public static Vec3 topCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
    }

    public static float yawTo(Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        return (float) Math.toDegrees(Math.atan2(-d.x, d.z));
    }

    public static float pitchTo(Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        return (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
    }

    /** Turns toward a point by at most maxStep degrees. Returns true once (nearly) on target. */
    public static boolean aimAt(LocalPlayer p, Vec3 target, float maxStep) {
        Vec3 eye = p.getEyePosition();
        float dy = Mth.wrapDegrees(yawTo(eye, target) - p.getYRot());
        float dp = pitchTo(eye, target) - p.getXRot();
        float sy = Mth.clamp(dy, -maxStep, maxStep), sp = Mth.clamp(dp, -maxStep, maxStep);
        p.setYRot(p.getYRot() + sy);
        p.setXRot(Mth.clamp(p.getXRot() + sp, -90f, 90f));
        return Math.abs(dy - sy) < 1f && Math.abs(dp - sp) < 1f;
    }
}
