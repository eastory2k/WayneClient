package com.swillway.wayne.modules;

import com.swillway.wayne.Module;
import com.swillway.wayne.WayneClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class TriggerBot extends Module {
    
    private int cd;
    
    public TriggerBot() { super("TriggerBot"); }
    
    @Override
    public void tick() {
        var p = WayneClient.mc.player;
        var im = WayneClient.mc.interactionManager;
        if (p == null || im == null || p.isDead()) return;
        
        if (cd > 0) { cd--; return; }
        
        var target = getTarget();
        if (target == null) return;
        
        im.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        cd = 2;
    }
    
    private LivingEntity getTarget() {
        var hit = WayneClient.mc.crosshairTarget;
        if (hit instanceof EntityHitResult e) {
            if (e.getEntity() instanceof LivingEntity t && t.isAlive() && t != WayneClient.mc.player) {
                return WayneClient.mc.player.distanceTo(t) <= 3.2f ? t : null;
            }
        }
        return null;
    }
}
