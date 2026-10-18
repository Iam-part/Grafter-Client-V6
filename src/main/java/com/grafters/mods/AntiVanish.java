package com.grafters.mods;

import com.grafters.GraftersConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * When another player comes within range, flashes their skin's face in the middle of the screen
 * (a bit like the mining-fatigue guardian effect), with red text and then yellow text under it.
 * Purely a warning: it does not log you out.
 */
public final class AntiVanish {
    private AntiVanish() {}

    private record Alert(String name, PlayerSkin skin) {}

    private static final Map<UUID, Long> LAST = new HashMap<>();
    private static final Set<UUID> IN_RANGE = new HashSet<>();
    private static final ArrayDeque<Alert> QUEUE = new ArrayDeque<>();
    private static Alert current;
    private static long startedAt;

    public static void reset() {
        LAST.clear(); IN_RANGE.clear(); QUEUE.clear(); current = null;
    }

    // ------------------------------------------------------------------
    //  Detection (every tick)
    // ------------------------------------------------------------------
    public static void tick(Minecraft mc) {
        GraftersConfig c = GraftersConfig.get();
        if (mc.player == null || mc.level == null) return;

        double near = (double) c.vanishRange * c.vanishRange;
        double far = (double) (c.vanishRange + 8) * (c.vanishRange + 8);
        long now = System.currentTimeMillis();
        Set<UUID> present = new HashSet<>();

        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player) continue;
            UUID id = p.getUUID();
            present.add(id);
            double d2 = mc.player.distanceToSqr(p);

            if (d2 <= near) {
                if (IN_RANGE.add(id)) {                                  // just came into range
                    long last = LAST.getOrDefault(id, 0L);
                    if (now - last >= c.vanishCooldown * 1000L && QUEUE.size() < 3) {
                        LAST.put(id, now);
                        QUEUE.add(new Alert(p.getName().getString(), p.getSkin()));
                    }
                }
            } else if (d2 > far) {
                IN_RANGE.remove(id);                                     // left: can alert again later
            }
        }
        IN_RANGE.retainAll(present);
    }

    /** Shows the alert with your own face so you can see what it looks like. */
    public static void preview() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        QUEUE.add(new Alert(mc.player.getName().getString(), mc.player.getSkin()));
    }

    // ------------------------------------------------------------------
    //  Drawing (called from the HUD every frame)
    // ------------------------------------------------------------------
    public static void render(GuiGraphicsExtractor g) {
        GraftersConfig c = GraftersConfig.get();
        Minecraft mc = Minecraft.getInstance();
        if (!c.antiVanish || mc.player == null) { current = null; return; }

        long now = System.currentTimeMillis();
        if (current == null) {
            current = QUEUE.poll();
            if (current == null) return;
            startedAt = now;
            if (c.vanishSound)
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ELDER_GUARDIAN_CURSE, 1.0f));
        }

        long dur = Math.max(1000L, c.vanishSeconds * 1000L);
        long age = now - startedAt;
        if (age >= dur) { current = null; return; }

        Font font = mc.font;
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        int cx = w / 2;

        // red flash that fades quickly
        float flash = Math.max(0f, 1f - age / 700f);
        int fa = (int) (flash * 80);
        if (fa > 3) g.fill(0, 0, w, h, (fa << 24) | 0x600000);

        // the face: pops in, trembles a little, disappears before the text does
        long faceTime = Math.min(dur, 2600L);
        int base = (int) (Math.min(w, h) * 0.34f);
        int faceY = (int) (h * 0.14f);
        if (age < faceTime) {
            float pop = age < 300 ? 0.5f + 0.5f * (age / 300f) : 1f;
            double shake = age < 700 ? Math.sin(age / 22.0) * 2.5 : Math.sin(age / 55.0) * 0.8;
            int size = Math.max(8, (int) (base * pop));
            int fx = cx - size / 2 + (int) shake;
            int fy = faceY + (base - size) / 2;
            g.outline(fx - 3, fy - 3, size + 6, size + 6, 0xFFFFD84A);
            PlayerFaceExtractor.extractRenderState(g, current.skin(), fx, fy, size);
        }

        // red line, then yellow line
        float textFade = age > dur - 600 ? Math.max(0f, (dur - age) / 600f) : 1f;
        int ta = Math.max(8, (int) (textFade * 255));
        String red = withName(c.vanishText1, current.name());
        String yellow = withName(c.vanishText2, current.name());
        int ty = faceY + base + 14;
        drawScaled(g, font, red, cx, ty, 2.0f, (ta << 24) | 0xFF3030);
        if (age > 450)
            drawScaled(g, font, yellow, cx, ty + 26, 1.5f, (ta << 24) | 0xFFFF55);
    }

    /** Replaces {player} / {Player} (any capitalisation) with the player's name. */
    private static String withName(String text, String name) {
        return text.replaceAll("(?i)\\{player\\}", java.util.regex.Matcher.quoteReplacement(name));
    }

    private static void drawScaled(GuiGraphicsExtractor g, Font font, String text, int cx, int y, float scale, int color) {
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(cx, y);
        pose.scale(scale, scale);
        g.centeredText(font, Component.literal(text), 0, 0, color);
        pose.popMatrix();
    }
}
