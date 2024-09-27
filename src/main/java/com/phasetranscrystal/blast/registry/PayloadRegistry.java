package com.phasetranscrystal.blast.registry;

import com.phasetranscrystal.blast.Blast;
import com.phasetranscrystal.blast.keylistener.KeyInputPacket;
import com.phasetranscrystal.blast.skill.SkillDataSynPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Blast.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadRegistry {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                KeyInputPacket.TYPE,
                KeyInputPacket.STREAM_CODEC,
                KeyInputPacket::serverHandler
        );

        registrar.playToClient(
                SkillDataSynPacket.TYPE,
                SkillDataSynPacket.STREAM_CODEC,
                SkillDataSynPacket
        )
    }
}
