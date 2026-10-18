package com.grafters.mods;

import com.grafters.GraftersConfig;
import com.grafters.GraftersState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;

/** Auto Totem, Auto Inv Totem and Hover Totem: keep a totem of undying in your off hand. */
public final class Totems {
    private Totems() {}

    private static int delayAuto, delayInv;
    private static Field hoveredField;
    private static boolean hoveredSearched;

    public static boolean isTotem(ItemStack s) {
        return !s.isEmpty() && Util.itemId(s).equals("totem_of_undying");
    }

    /** Swaps a totem from your inventory into the off hand. True if a click was sent. */
    public static boolean fill(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || p.containerMenu != p.inventoryMenu) return false;
        if (isTotem(p.getOffhandItem())) return false;
        for (int s = 9; s <= 44; s++) {                        // inventory menu: 9-35 main, 36-44 hotbar
            if (isTotem(p.inventoryMenu.getSlot(s).getItem())) {
                Util.click(p.inventoryMenu.containerId, s, 40, ContainerInput.SWAP);   // 40 = off hand
                return true;
            }
        }
        return false;
    }

    /** Auto Totem: works anywhere, no inventory screen needed. */
    public static void autoTick(Minecraft mc) {
        if (--delayAuto > 0) return;
        if (fill(mc)) delayAuto = GraftersConfig.get().totemDelay;
    }

    /** Auto Inv Totem: only while your inventory screen is open. */
    public static void invTick(Minecraft mc) {
        if (!(GraftersState.screen(mc) instanceof InventoryScreen)) return;
        if (--delayInv > 0) return;
        if (fill(mc)) delayInv = GraftersConfig.get().invTotemDelay;
    }

    /** Hover Totem: hover a totem in any container screen and it jumps to your off hand. */
    public static void hoverTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        Screen s = GraftersState.screen(mc);
        if (p == null || !(s instanceof AbstractContainerScreen<?>)) return;
        if (isTotem(p.getOffhandItem())) return;

        Slot hovered = hovered(s);
        if (hovered == null || !isTotem(hovered.getItem())) return;
        Util.click(p.containerMenu.containerId, hovered.index, 40, ContainerInput.SWAP);
    }

    private static Slot hovered(Screen s) {
        try {
            if (!hoveredSearched) {
                hoveredSearched = true;
                Field f = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
                f.setAccessible(true);
                hoveredField = f;
            }
            return hoveredField == null ? null : (Slot) hoveredField.get(s);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
