package me.liqw.persistentchat.network;

import me.liqw.persistentchat.PersistentChat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ChatHistoryPayload(List<Component> messages) implements CustomPacketPayload {
    public static final Identifier CHAT_HISTORY_PAYLOAD_ID = Identifier.fromNamespaceAndPath(PersistentChat.MOD_ID, "chat_history");
    public static final CustomPacketPayload.Type<ChatHistoryPayload> ID = new CustomPacketPayload.Type<>(CHAT_HISTORY_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatHistoryPayload> CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.apply(ByteBufCodecs.list()), ChatHistoryPayload::messages, ChatHistoryPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}