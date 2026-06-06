package com.swillway.wayne;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;

public class WayneClient implements ClientModInitializer {

    public static WayneClient INSTANCE;
    public static MinecraftClient mc;
    public ModuleManager modules;
    public CelestialHUD hud;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        mc = MinecraftClient.getInstance();
        modules = new ModuleManager();
        hud = new CelestialHUD();
        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register(hud::render);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(modules::onWorldRender);
        System.out.println("[Wayne] Готов");
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        modules.tick();
    }
}
