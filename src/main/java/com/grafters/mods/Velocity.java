package com.grafters.mods;

import com.grafters.GraftersConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Reduces knockback. If your motion jumps between the end of one tick and the start of the next,
 * a packet (a hit, an explosion) set it, so that push is scaled down by your percentages.
 */
public final class Velocity {
    private Velocity() {}

    private static Vec3 prev = Vec3.ZERO;
    private static boolean primed;

    /** Start of every client tick (called from GraftersState). */
    public static void start(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        LocalPlayer p = mc.player;
        if (!c.velocity || p == null || !primed) return;
        Vec3 now = p.getDeltaMovement();
        if (now.subtract(prev).length() > 0.25) {
            p.setDeltaMovement(now.x * c.velH / 100.0, now.y * c.velV / 100.0, now.z * c.velH / 100.0);
        }
    }

    /** End of every client tick (module ticker). */
    public static void end(Minecraft mc) {
        if (mc.player == null) { primed = false; return; }
        prev = mc.player.getDeltaMovement();
        primed = true;
    }
}
