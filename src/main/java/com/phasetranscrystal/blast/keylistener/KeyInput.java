package com.phasetranscrystal.blast.keylistener;

import com.phasetranscrystal.blast.Blast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.phasetranscrystal.nonard.testobjs.SkillTest.SKILL_ATTACHMENT;

@EventBusSubscriber(modid = Blast.MODID, value = Dist.CLIENT)
public class KeyInput {
    @SubscribeEvent
    public static void keyboard(KeyInputEvent.Client event) {
        check(event.getKey(), event.getModifiers(), event.getAction(), event);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void mouse(InputEvent.MouseButton.Pre event) {
        check(event.getButton(), event.getModifiers(), event.getAction(), event);
    }

    private static void check(int key, int modifiers, int action, ICancellableEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;

        //在解决技能数据同步问题前，要测试按键监听，直接使用下面这行，其他的注释掉
//        PacketDistributor.sendToServer(new C2SKeyInputPacket(key, modifiers, action));

        if (Minecraft.getInstance().screen != null) {
            //开启了gui就不要监听了
            return;
        }

        //todo 这个技能数据附加还是测试，记得更改为正式的
        //todo 技能附加未同步到客户端，待解决
        Optional<SkillData<ServerPlayer>> optData = player.getExistingData(SKILL_ATTACHMENT);

        if (optData.isEmpty()) {
            //无技能，结束
            return;
        }

        SkillData<ServerPlayer> skill = optData.get();
        Optional<Behavior<ServerPlayer>> optBehavior = skill.getBehavior();

        if (optBehavior.isEmpty()) {
            //无行为，结束
            return;
        }

        Behavior<ServerPlayer> behavior = optBehavior.get();

        if (!behavior.keys.contains(key)) {
            //不是绑定的按键，结束
            return;
        }

        event.setCanceled(true);

        if (action > 1) {
            //1按下，0松开，其他情况不发包，但同样取消事件
            return;
        }

        //发包
        PacketDistributor.sendToServer(new KeyInputPacket(key, modifiers, action));
    }
}
