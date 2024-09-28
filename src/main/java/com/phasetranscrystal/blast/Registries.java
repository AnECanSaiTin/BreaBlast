package com.phasetranscrystal.blast;

import com.phasetranscrystal.blast.skill.Skill;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Blast.MODID)
public class Registries {
    public static final Registry<Skill<?>> SKILL = new RegistryBuilder<>(Keys.SKILL).sync(true).create();

    public static class Keys {
        public static final ResourceKey<Registry<Skill<?>>> SKILL = ResourceKey.createRegistryKey(Skill.NAME);
    }

    @SubscribeEvent
    public static void regis(NewRegistryEvent event){
        event.register(SKILL);
    }
}
