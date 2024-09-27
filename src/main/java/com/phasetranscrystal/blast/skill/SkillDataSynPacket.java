package com.phasetranscrystal.blast.skill;

import com.phasetranscrystal.blast.Blast;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record SkillDataSynPacket(Optional<Boolean> enabled, Optional<Optional<Skill<? super Player>>> skill, Optional<Optional<String>> stage,
                                 Optional<Integer> inactiveEnergy, Optional<Integer> activeEnergy, Optional<Integer> activeTimes) implements CustomPacketPayload {
    public static final Type<SkillDataSynPacket> TYPE = new Type<>(Blast.location("player_skill_syn"));
    public static final StreamCodec<ByteBuf, SkillDataSynPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.BOOL), SkillDataSynPacket::enabled,
            ByteBufCodecs.optional(ByteBufCodecs.optional(ByteBufCodecs.fromCodec(Registries.SKILL.byNameCodec()))), pack -> pack.skill.map(opt -> opt.map(s -> s)),
            ByteBufCodecs.optional(ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)), SkillDataSynPacket::stage,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), SkillDataSynPacket::inactiveEnergy,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), SkillDataSynPacket::activeEnergy,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), SkillDataSynPacket::activeTimes,
            SkillDataSynPacket::decode
    );

    @SuppressWarnings("all")
    private static SkillDataSynPacket decode(Optional<Boolean> enabled, Optional<Optional<Skill<?>>> skill, Optional<Optional<String>> stage,
                                             Optional<Integer> inactiveEnergy, Optional<Integer> activeEnergy, Optional<Integer> activeTimes) {
        return new SkillDataSynPacket(enabled, skill.map(opt -> opt.map(s -> (Skill<? super Player>) s)), stage, inactiveEnergy, activeEnergy, activeTimes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    //Client Only
    public static void consume(SkillDataSynPacket packet, IPayloadContext context){
        Player player = context.
    }
}
