package com.phasetranscrystal.blast.player;

import com.phasetranscrystal.blast.Blast;
import com.phasetranscrystal.blast.skill.SkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;


@EventBusSubscriber(modid = Blast.MODID, value = Dist.CLIENT)
public class KeyInput {
    @SubscribeEvent
    public static void mouseInput(InputEvent.MouseButton.Pre event) {
        clientClickCheck(event.getButton(), event.getModifiers(), event.getAction(), () -> event.setCanceled(true));
    }

    public static void clientClickCheck(int key, int modifiers, int action, Runnable canceller) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().screen != null || key < 0 || action > 1)
            return;
        //TODO 检查技能输入模式

        int compoundKey = (modifiers & 0x3F) << 9 | key;
        Minecraft.getInstance().player.getExistingData(Blast.SKILL_ATTACHMENT)
                .flatMap(group -> Optional.ofNullable(group.getSkillCache()).map(skill -> skill.behaviors.get(group.getStageCache())))
                .map(behavior -> behavior.keys)
                .ifPresent(list -> {
                    if (!list.contains(compoundKey)) return;
                    canceller.run();
                    PacketDistributor.sendToServer(new KeyInputPacket(key, modifiers, action));
                });
    }

    //SERVER SIDE
    @FunctionalInterface
    public interface Consumer<T extends Entity> {
        void accept(SkillData<T> data, KeyInputPacket packet);
    }
}
