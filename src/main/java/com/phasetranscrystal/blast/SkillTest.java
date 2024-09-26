package com.phasetranscrystal.blast;

import com.mojang.serialization.Codec;
import com.phasetranscrystal.nonard.Nonard;
import com.phasetranscrystal.nonard.skill.Registries;
import com.phasetranscrystal.nonard.skill.Skill;
import com.phasetranscrystal.nonard.skill.SkillData;
import io.github.tt432.eyelib.network.SpawnParticlePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.lwjgl.glfw.GLFW;

public class SkillTest {

    public static void bootstrap(IEventBus bus) {
        SKILL.register(bus);
        ATTACHMENT.register(bus);
        ITEM.register(bus);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, LivingDeathEvent.class, SkillTest::onDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, EntityJoinLevelEvent.class, SkillTest::init);
    }

    public static final DeferredRegister<Skill<?>> SKILL = DeferredRegister.create(Registries.SKILL, Nonard.MOD_ID);

    public static final DeferredHolder<Skill<?>, Skill<ServerPlayer>> TEST_SKILL = SKILL.register("test",
            () -> Skill.Builder.<ServerPlayer>of(30, 4)
                    .start(data -> data.getEntity().displayClientMessage(Component.literal("TestSkillInit"), false))
//                    .flag(Skill.Flag.INSTANT_COMPLETE, true)
                    .onEvent(EntityTickEvent.Post.class, (event, data) -> {
                        ServerPlayer player = data.getEntity();
                        if (player.serverLevel().getGameTime() % 100 == 0 && player.getHealth() < player.getMaxHealth()) {
                            player.displayClientMessage(Component.literal("You are healed!"), false);
                            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 100, 2));
                        }
                    })
                    .inactive(builder -> builder
                            //按键监听测试
                            .setKeyInputListener(new int[]{GLFW.GLFW_KEY_H}, (event, data) -> {data.getEntity().sendSystemMessage(Component.literal("按键拦截成功"));})
                            .onHurt((event, data) -> data.addEnergy(-1))
                            .onAttack((event, data) -> data.addEnergy(2))
                            .onKillTarget((event, data) -> data.addEnergy(5))
                            .inactiveEnergyChanged((data, i) -> {
                                data.getEntity().displayClientMessage(Component.literal("Energy " + (i >= 0 ? "§a+" : "§c-") + i), true);
                            })
                            .chargeChanged((data, i) -> {
                                data.getEntity().displayClientMessage(Component.literal("Charge " + (i >= 0 ? "§a+" : "§c-") + i), true);
                                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Nonard.MOD_ID, "skill_test");
                                data.addAutoCleanAttribute(new AttributeModifier(location, 0.5 * data.getCharge(), AttributeModifier.Operation.ADD_VALUE), Attributes.MOVEMENT_SPEED);
                            })
                            .onChargeReady(data -> data.getEntity().displayClientMessage(Component.literal("ReachReady!"), false))
                            .onChargeFull(data -> data.getEntity().displayClientMessage(Component.literal("ReachStop!"), false))
                            .endWith(data -> {
                                data.putCacheData("charge_consume", data.getCharge() + 1 + "", true, true);
                                data.setCharge(0);
                            })
                    )
                    .judge((data, name) -> !"active".equals(name.orElse("")) || (data.getEntity().level().isNight() && data.getCharge() >= 1))
                    .active(builder -> builder
                            .startWith(data -> PacketDistributor.sendToPlayersTrackingChunk(
                                    (ServerLevel) data.getEntity().level(), new ChunkPos(data.getEntity().blockPosition()),
                                    new SpawnParticlePacket("baozi", ParticlesTest.LARGE_SPORE_RING_SPRAY, data.getEntity().getPosition(0).toVector3f())
                            ))
                            .onTick((event, data) -> {
                                data.getEntity().displayClientMessage(Component.literal("activeTick"), true);
                                data.modifyActiveEnergy(-1);
                            })
                            .endWith(data -> {
                                data.getEntity().jumpFromGround();
                                data.getEntity().addDeltaMovement(new Vec3(0, 0.1, 0));
                                data.getEntity().addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200 * data.getCacheDataAsInt("charge_consume", 0, true), 2));
                            })
                    )
                    .onBehaviorChange((data, behavior) -> {
                        if (data.getActiveTimes() == 5) data.requestDisable();
                        else {
                            data.getEntity().displayClientMessage(Component.literal("StateChanged: to " + behavior.map(s -> "\"" + behavior + "\"").orElse("null") + " time " + data.getActiveTimes()), false);
                            if ("active".equals(behavior.orElse(""))) data.consumeCharge();
                        }
                    })
                    .end(data -> data.getEntity().displayClientMessage(Component.literal("skill disabled"), false))
    );

    public static final DeferredHolder<Skill<?>, Skill<ServerPlayer>> OLD_MA = SKILL.register("old_ma", () -> {
        return Skill.Builder
                .<ServerPlayer>of(50, 3, 0, 0, 50)
                .start(data -> data.getEntity().displayClientMessage(Component.literal("OldMaInit"), false))
                .judge((data, name) -> data.getCharge() == 3)
                .addBehavior(builder -> {
                    builder.setKeyInputListener(new int[]{GLFW.GLFW_KEY_H}, (event, data) -> {data.getEntity().sendSystemMessage(Component.literal("按键拦截成功"));})
                            .endWith(data -> data.getEntity().displayClientMessage(Component.literal("OldMaEnd"), false));
                }, "key test")
                .end();
    });

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nonard.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillData<ServerPlayer>>> SKILL_ATTACHMENT =
            ATTACHMENT.register("skill", () -> AttachmentType.builder(() -> new SkillData<>(TEST_SKILL.get())).serialize((Codec) SkillData.CODEC).copyOnDeath().build());

    public static final DeferredRegister<Item> ITEM = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, Nonard.MOD_ID);

    public static final DeferredHolder<Item, Start> START = ITEM.register("skill_start", Start::new);


    public static void onDeath(LivingDeathEvent event) {
        event.getEntity().getExistingData(SKILL_ATTACHMENT).ifPresent(SkillData::requestDisable);
    }

    public static void init(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(SKILL_ATTACHMENT).bindEntity(serverPlayer);
        }
    }

    public static class Start extends Item {
        public Start() {
            super(new Properties());
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            if (level instanceof ServerLevel server) {
                if (usedHand == InteractionHand.OFF_HAND && !player.getData(SKILL_ATTACHMENT).isEnabled()) {
                    player.getData(SKILL_ATTACHMENT).requestEnable();
                } else {
                    player.getData(SKILL_ATTACHMENT).switchToIfNot("active");
                }
            }
            return InteractionResultHolder.success(player.getItemInHand(usedHand));
        }
    }
}
