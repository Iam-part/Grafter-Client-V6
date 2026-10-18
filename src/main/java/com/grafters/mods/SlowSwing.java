package com.grafters.mods;

import com.grafters.GraftersConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Smooth slow swing. Vanilla advances your swing once per game tick (20 times a second), so any
 * trick that slows it down per tick looks steppy. Instead this runs its OWN swing animation by
 * real time, one value per rendered frame, and LivingEntityMixin hands that value to the renderer.
 *
 *  - A swing (or a click while mining) asks for an animation; it then plays slowly from start to
 *    finish in one continuous arc.
 *  - While you keep mining, it loops straight into the next arc with no snap back.
 *  - Visual only: attack timing and damage are untouched.
 */
public final class SlowSwing {
    private SlowSwing() {}

    private static final float VANILLA_TICKS = 6f;        // how long a normal swing lasts

    private static boolean wasSwinging, swingingNow, requested, running;
    private static int lastSwingTime;
    private static float phase;
    private static long lastNanos;

    /** Every client tick: notice when a new swing starts. */
    public static void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) return;
        swingingNow = p.swinging;
        // swing() resets the counter, so a drop in swingTime (or swinging turning on) = a new swing
        if (p.swinging && (!wasSwinging || p.swingTime < lastSwingTime)) requested = true;
        wasSwinging = p.swinging;
        lastSwingTime = p.swingTime;
    }

    public static void reset() {
        running = false; requested = false; phase = 0; lastNanos = 0;
    }

    /**
     * Called from LivingEntityMixin each time the renderer asks for the swing progress.
     * Returns the value to use (0..1), or null to leave vanilla's value alone.
     */
    public static Float animation() {
        GraftersConfig c = GraftersConfig.get();
        if (!c.slowSwing) { running = false; requested = false; return null; }

        long now = System.nanoTime();
        float ticks = lastNanos == 0 ? 0f : Math.min(now - lastNanos, 100_000_000L) / 50_000_000f;
        lastNanos = now;

        if (!running && requested) { running = true; phase = 0f; requested = false; }
        if (!running) return null;

        phase += ticks / (VANILLA_TICKS * Math.max(1.0f, c.slowSwingFactor));
        if (phase >= 1f) {
            if (requested || swingingNow) { phase -= 1f; requested = false; }   // still mining/swinging: next arc
            else { running = false; return 0f; }
        }
        return phase;
    }
}
