package com.swillway.wayne;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public abstract class Module {
    public String name;
    public boolean on = true;

    public Module(String name) {
        this.name = name;
    }

    public void tick() {}
    public void render(WorldRenderContext ctx) {}
}
