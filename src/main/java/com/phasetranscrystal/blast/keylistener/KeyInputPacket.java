package com.phasetranscrystal.blast.keylistener;

import com.phasetranscrystal.blast.Blast;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record KeyInputPacket(short var) implements CustomPacketPayload {
    public static final Type<KeyInputPacket> TYPE = new Type<>(Blast.location("key_input"));
    public static final StreamCodec<ByteBuf, KeyInputPacket> STREAM_CODEC = ByteBufCodecs.SHORT.map(KeyInputPacket::new, KeyInputPacket::var);

    public KeyInputPacket(int key, int modifier, int action) {
        this((short) (((action & 0b1) << 15) | ((modifier & 0x7F) << 9) | (key & 0x1FF)));
    }

    public int key() {
        return var & 0x1FF;
    }

    public int modifier() {
        return (var >>> 9) & 0x7F;
    }

    public int action() {
        return (var >>> 15) & 0b1;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void serverHandler(KeyInputPacket pack, IPayloadContext context) {
        NeoForge.EVENT_BUS.post(new KeyInputEvent.Server((ServerPlayer) context.player(), pack.key(), pack.action(), pack.modifier()));
    }
}
