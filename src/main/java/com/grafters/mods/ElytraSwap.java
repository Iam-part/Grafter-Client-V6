package com.grafters.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/** One press: swaps your chestplate for an elytra, or back again. Works from anywhere in your inventory. */
public final class ElytraSwap {
    private ElytraSwap() {}

    private static final String[] ORDER = {"netherite", "diamond", "iron", "golden", "chainmail", "leather"};

    public static void run() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || p.containerMenu != p.inventoryMenu) return;

        boolean wearingElytra = Util.itemId(p.inventoryMenu.getSlot(6).getItem()).equals("elytra");
        int slot = wearingElytra ? bestChestplate(p) : findId(p, "elytra");
        if (slot < 0) { Util.chat(wearingElytra ? "No chestplate in your inventory" : "No elytra in your inventory"); return; }

        int id = p.inventoryMenu.containerId;
        Util.click(id, slot, 0, ContainerInput.PICKUP);   // pick it up
        Util.click(id, 6, 0, ContainerInput.PICKUP);      // swap with the chest slot
        Util.click(id, slot, 0, ContainerInput.PICKUP);   // put the old piece where it came from
    }

    private static int findId(LocalPlayer p, String id) {
        for (int s = 9; s <= 44; s++) {
            ItemStack st = p.inventoryMenu.getSlot(s).getItem();
            if (!st.isEmpty() && Util.itemId(st).equals(id)) return s;
        }
        return -1;
    }

    private static int bestChestplate(LocalPlayer p) {
        for (String tier : ORDER) {
            for (int s = 9; s <= 44; s++) {
                ItemStack st = p.inventoryMenu.getSlot(s).getItem();
                if (!st.isEmpty() && Util.itemId(st).equals(tier + "_chestplate")) return s;
            }
        }
        return -1;
    }
}
