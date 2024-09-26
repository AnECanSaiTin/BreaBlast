package com.phasetranscrystal.blast;


import net.neoforged.bus.api.IEventBus;

public class Blast {
    public static final String MODID = "brea_blast";

    public Blast(IEventBus bus) {
        SkillTest.bootstrap(bus);
    }
}
