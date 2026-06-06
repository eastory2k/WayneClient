package com.swillway.wayne.modules;

import com.swillway.wayne.Module;
import com.swillway.wayne.WayneClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {
    
    private static final float SMOOTH = 0.3f;
    private static final float FOV = 60f;
    private static final float RANGE = 5f;
    
    public AimAssist() { super("AimAssist"); }
    
    @Override
    public void tick() {
        var p = WayneClient.mc.player;
        if (p == null || p.isDead()) return;
        
        var target = find();
        if (target == null) return;
        
        aim(target);
    }
    
    private LivingEntity find() {
        var p = WayneClient.mc.player;
        var best = (LivingEntity) null;
        var bestDist = Double.MAX_VALUE;
        
        for (var e : WayneClient.mc.world.getEntities()) {
            if (!(e instanceof LivingEntity t) || t == p || !t.isAlive()) continue;
            double d = p.distanceTo(t);
            if (d > RANGE) continue;
            if (!inFOV(t)) continue;
            if (d < bestDist) { bestDist = d; best = t; }
        }
        return best;
    }
    
    private boolean inFOV(LivingEntity t) {
        var p = WayneClient.mc.player;
        var a = p.getRotationVector();
        var b = t.getPos().add(0, t.getHeight() / 2, 0).subtract(p.getEyePos()).normalize();
        return Math.toDegrees(Math.acos(MathHelper.clamp(a.dotProduct(b), -1, 1))) < FOV;
    }
    
    private void aim(LivingEntity t) {
        var p = WayneClient.mc.player;
        var tp = t.getPos().add(0, t.getHeight() * 0.8, 0);
        var ep = p.getEyePos();
        
        double dx = tp.x - ep.x, dy = tp.y - ep.y, dz = tp.z - ep.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        
        p.setYaw(p.getYaw() + MathHelper.wrapDegrees(yaw - p.getYaw()) * SMOOTH);
        p.setPitch(MathHelper.clamp(p.getPitch() + (pitch - p.getPitch()) * SMOOTH, -90, 90));
    }
}
