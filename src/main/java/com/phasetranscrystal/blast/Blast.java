package com.phasetranscrystal.blast;


import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

public class Blast {
    public static final String MODID = "brea_blast";

    public Blast(IEventBus bus) {
        SkillTest.bootstrap(bus);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
