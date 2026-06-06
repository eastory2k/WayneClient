package com.swillway.wayne.modules;

import com.swillway.wayne.Module;
import com.swillway.wayne.WayneClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AppleFarm extends Module {
    
    private long last;
    private int prevSlot = -1;
    private boolean eating;
    
    public AppleFarm() { super("AppleFarm"); }
    
    @Override
    public void tick() {
        var p = WayneClient.mc.player;
        var im = WayneClient.mc.interactionManager;
        if (p == null || im == null || p.isDead()) return;
        
        if (p.isUsingItem()) { eating = true; return; }
        if (eating) { stop(); return; }
        if (p.getHealth() >= p.getMaxHealth() && p.getAbsorptionAmount() >= 16) return;
        
        int slot = findApple();
        if (slot < 0) return;
        if (System.currentTimeMillis() - last < 350) return;
        
        prevSlot = p.getInventory().selectedSlot;
        p.getInventory().selectedSlot = slot;
        im.interactItem(p, Hand.MAIN_HAND);
        eating = true;
        last = System.currentTimeMillis();
    }
    
    private int findApple() {
        var inv = WayneClient.mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            var item = inv.getStack(i).getItem();
            if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) return i;
        }
        return -1;
    }
    
    private void stop() {
        if (prevSlot >= 0) {
            WayneClient.mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
        eating = false;
    }
}
