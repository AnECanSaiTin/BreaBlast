package com.phasetranscrystal.blast;


import com.phasetranscrystal.blast.registry.PlayerSkillRegistry;
import com.phasetranscrystal.blast.skill.Skill;
import com.phasetranscrystal.blast.skill.SkillData;
import com.phasetranscrystal.blast.player.SkillGroup;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Blast {
    public static final String MODID = "brea_blast";

    public Blast(IEventBus bus) {
        ATTACHMENT.register(bus);
        PlayerSkillRegistry.SKILL.register(bus);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    //PLAYER SKILLS
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Blast.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillGroup>> SKILL_ATTACHMENT =
            ATTACHMENT.register("skill", () -> AttachmentType.builder(SkillGroup::new).serialize(SkillGroup.CODEC).copyOnDeath().build());



}
