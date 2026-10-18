package com.grafters.mods;

import com.grafters.GraftersConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * Packet criticals: right before an attack, tells the server you hopped up and came back down,
 * so the hit counts as a falling (critical) hit. Called from MultiPlayerGameModeMixin.
 */
public final class Criticals {
    private Criticals() {}

    public static void before(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (!GraftersConfig.get().criticals || p == null) return;
        if (!p.onGround() || p.isInWater() || p.onClimbable()) return;

        double x = p.getX(), y = p.getY(), z = p.getZ();
        p.connection.send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
        p.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
    }
}
