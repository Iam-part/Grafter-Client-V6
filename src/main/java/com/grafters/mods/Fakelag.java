package com.grafters.mods;

import com.grafters.GraftersConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.ArrayDeque;

/**
 * Holds your movement packets for a short time, then sends them all at once, so to the server
 * (and other players) you appear to stand still and then jump forward.
 */
public final class Fakelag {
    private Fakelag() {}

    private static final ArrayDeque<Packet<?>> HELD = new ArrayDeque<>();
    private static final ArrayDeque<Long> TIMES = new ArrayDeque<>();

    /** From ClientPacketListenerSendMixin: true = we are holding this packet, do not send it now. */
    public static boolean intercept(Packet<?> packet) {
        if (!GraftersConfig.get().fakelag) return false;
        if (!(packet instanceof ServerboundMovePlayerPacket)) return false;
        HELD.add(packet);
        TIMES.add(System.currentTimeMillis());
        return true;
    }

    public static void tick(Minecraft mc) {
        if (HELD.isEmpty()) return;
        if (System.currentTimeMillis() - TIMES.peek() >= GraftersConfig.get().flDelay) flush(mc);
    }

    public static void flush(Minecraft mc) {
        var listener = mc.getConnection();
        if (listener == null) { clear(); return; }
        while (!HELD.isEmpty()) {
            listener.getConnection().send(HELD.poll());
            TIMES.poll();
        }
    }

    public static void clear() { HELD.clear(); TIMES.clear(); }
}
