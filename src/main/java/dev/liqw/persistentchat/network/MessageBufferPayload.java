package dev.liqw.persistentchat.network;

import dev.liqw.persistentchat.PersistentChat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record MessageBufferPayload(List<Component> messages, List<Long> timestamps) implements CustomPacketPayload {
    public static final Type<MessageBufferPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PersistentChat.MOD_ID, "message-buffer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageBufferPayload> CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),
            MessageBufferPayload::messages,
            ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()),
            MessageBufferPayload::timestamps,
            MessageBufferPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}