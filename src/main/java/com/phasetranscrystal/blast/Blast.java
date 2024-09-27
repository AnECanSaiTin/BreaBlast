package com.phasetranscrystal.blast;


import com.mojang.serialization.Codec;
import com.phasetranscrystal.blast.skill.SkillGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class Blast {
    public static final String MODID = "brea_blast";

    public Blast(IEventBus bus) {
        SkillTest.bootstrap(bus);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    //PLAYER SKILLS
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Blast.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillGroup>> SKILL_ATTACHMENT =
            ATTACHMENT.register("skill", () -> AttachmentType.builder(() -> new SkillGroup()).serialize((Codec) SkillData.CODEC).copyOnDeath().build());

    public static final DeferredRegister<Item> ITEM = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, Nonard.MOD_ID);

    public static final DeferredHolder<Item, SkillTest.Start> START = ITEM.register("skill_start", SkillTest.Start::new);


    public static void onDeath(LivingDeathEvent event) {
        event.getEntity().getExistingData(SKILL_ATTACHMENT).ifPresent(SkillData::requestDisable);
    }

    public static void init(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(SKILL_ATTACHMENT).bindEntity(serverPlayer);
        }
    }
}
