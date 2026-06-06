package com.swillway.wayne.modules;

import com.swillway.wayne.Module;
import com.swillway.wayne.WayneClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

public class Visuals extends Module {
    
    public boolean esp = true, tracers = true, nametags = true;
    
    public Visuals() { super("Visuals"); }
    
    @Override
    public void render(WorldRenderContext ctx) {
        var w = WayneClient.mc.world;
        var p = WayneClient.mc.player;
        if (w == null || p == null) return;
        
        for (var e : w.getEntities()) {
            if (!(e instanceof PlayerEntity pl) || pl == p) continue;
            
            if (esp) drawBox(pl, ctx);
            if (tracers) drawTracer(pl, ctx);
        }
    }
    
    private void drawBox(PlayerEntity pl, WorldRenderContext ctx) {
        double x = lerp(pl.prevX, pl.getX(), ctx.tickDelta());
        double y = lerp(pl.prevY, pl.getY(), ctx.tickDelta());
        double z = lerp(pl.prevZ, pl.getZ(), ctx.tickDelta());
        
        float hp = (pl.getHealth() + pl.getAbsorptionAmount()) / pl.getMaxHealth();
        float r = hp < 0.5f ? 1f : 2f * (1f - hp);
        float g = hp > 0.5f ? 1f : 2f * hp;
        
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2f);
        
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(r, g, 0, 1);
        
        double[] xs = {x - 0.3, x + 0.3}, ys = {y, y + 1.8}, zs = {z - 0.3, z + 0.3};
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++)
                for (int k = 0; k < 2; k++) {
                    if (i == 0) { GL11.glVertex3d(xs[0], ys[j], zs[k]); GL11.glVertex3d(xs[1], ys[j], zs[k]); }
                    if (j == 0) { GL11.glVertex3d(xs[i], ys[0], zs[k]); GL11.glVertex3d(xs[i], ys[1], zs[k]); }
                    if (k == 0) { GL11.glVertex3d(xs[i], ys[j], zs[0]); GL11.glVertex3d(xs[i], ys[j], zs[1]); }
                }
        
        GL11.glEnd();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    
    private void drawTracer(PlayerEntity pl, WorldRenderContext ctx) {
        double x = lerp(pl.prevX, pl.getX(), ctx.tickDelta());
        double y = lerp(pl.prevY, pl.getY(), ctx.tickDelta()) + pl.getHeight() / 2;
        double z = lerp(pl.prevZ, pl.getZ(), ctx.tickDelta());
        
        var pos = worldToScreen(x, y, z);
        if (pos == null) return;
        
        int sw = WayneClient.mc.getWindow().getScaledWidth();
        
        RenderSystem.enableBlend();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(1, 1, 1, 0.8f);
        GL11.glVertex2d(sw / 2.0, 0);
        GL11.glVertex2d(pos.x, pos.y);
        GL11.glEnd();
        RenderSystem.disableBlend();
    }
    
    public void renderHUD(DrawContext ctx) {
        if (!nametags) return;
        var w = WayneClient.mc.world;
        var p = WayneClient.mc.player;
        if (w == null || p == null) return;
        
        int y = 50;
        var font = WayneClient.mc.textRenderer;
        
        for (var e : w.getEntities()) {
            if (!(e instanceof PlayerEntity pl) || pl == p) continue;
            String t = pl.getName().getString() + " " + (int)(pl.getHealth() + pl.getAbsorptionAmount()) + "HP " + (int)p.distanceTo(pl) + "m";
            ctx.drawTextWithShadow(font, t, 5, y, 0xFFFFFF);
            y += 12;
        }
    }
    
    private double lerp(double a, double b, float t) { return a + (b - a) * t; }
    
    private Vec3d worldToScreen(double x, double y, double z) { return null; } // Упрощено
}
