package com.swillway.wayne;

import com.swillway.wayne.modules.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import java.util.*;

public class ModuleManager {
    public final List<Module> list = new ArrayList<>();

    public ModuleManager() {
        list.add(new AppleFarm());
        list.add(new TriggerBot());
        list.add(new AimAssist());
        list.add(new Visuals());
    }

    public void tick() {
        for (Module m : list) if (m.on) m.tick();
    }

    public void onWorldRender(WorldRenderContext ctx) {
        for (Module m : list) if (m.on) m.render(ctx);
    }

    public List<Module> all() { return list; }
    public int enabled() {
        int n = 0;
        for (Module m : list) if (m.on) n++;
        return n;
    }
}
