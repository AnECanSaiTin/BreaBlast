package com.phasetranscrystal.blast;


import com.phasetranscrystal.blast.skill.SkillData;
import com.phasetranscrystal.blast.player.SkillGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Blast.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillGroup>> SKILL_ATTACHMENT =
            ATTACHMENT.register("skill", () -> AttachmentType.builder(() -> new SkillGroup()).serialize(SkillGroup.CODEC).copyOnDeath().build());


    public static void onDeath(LivingDeathEvent event) {
        event.getEntity().getExistingData(SKILL_ATTACHMENT).flatMap(SkillGroup::getCurrentSkillData).ifPresent(SkillData::requestDisable);
    }

    public static void init(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(SKILL_ATTACHMENT).bindEntity(serverPlayer);
            serverPlayer.getData(SKILL_ATTACHMENT).getCurrentSkillData().ifPresent(SkillData::requestEnable);
        }
    }
}
