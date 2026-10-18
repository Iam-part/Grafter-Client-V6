package com.grafters.mods;

import com.grafters.GraftersConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Makes other players easier to hit by growing the box your crosshair targets (EntityPickMixin).
 * Static HitBoxes also keeps that box standing-height while they crouch or swim.
 * This only changes what YOU can target: the server still checks real reach.
 */
public final class Hitboxes {
    private Hitboxes() {}

    public static float extra(Entity e) {
        GraftersConfig c = GraftersConfig.get();
        if (!c.hitbox && !c.staticHitbox) return 0f;
        if (!(e instanceof Player) || e == Minecraft.getInstance().player) return 0f;

        float extra = 0f;
        if (c.hitbox) extra += c.hbExpand;
        if (c.staticHitbox) {
            float h = e.getBbHeight();
            if (h < 1.79f) extra += (1.8f - h) / 2f;
        }
        return extra;
    }
}
