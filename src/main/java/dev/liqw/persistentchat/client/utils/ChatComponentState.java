package dev.liqw.persistentchat.client.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.liqw.persistentchat.client.mixin.ChatComponentStateAccessor;
import dev.liqw.persistentchat.utils.GuiMessageAccessor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MessageSignature;

import java.util.ArrayList;
import java.util.Optional;

public class ChatComponentState {
    private static final Codec<GuiMessage> GUI_MESSAGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("addedTime").forGetter(_ -> -999),
            ComponentSerialization.CODEC.fieldOf("content").forGetter(GuiMessage::content),
            MapCodec.unit(Optional.<MessageSignature>empty()).fieldOf("signature").forGetter(m -> Optional.ofNullable(m.signature())),
            Codec.INT.xmap(i -> GuiMessageSource.values()[i], Enum::ordinal).fieldOf("source").forGetter(GuiMessage::source),
            Codec.LONG.fieldOf("timestamp").forGetter(message -> GuiMessageAccessor.of(message).getTimestamp())
    ).apply(instance, (addedTime, content, signature, source, timestamp) -> {
        GuiMessage message = new GuiMessage(addedTime, content, signature.orElse(null), source, null);

        GuiMessageAccessor.of(message).setTimestamp(timestamp);

        return message;
    }));

    public static final Codec<ChatComponent.State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GUI_MESSAGE_CODEC.listOf().fieldOf("messages").forGetter(state -> ((ChatComponentStateAccessor) state).getMessages()),
            Codec.STRING.listOf().fieldOf("history").forGetter(state -> ((ChatComponentStateAccessor) state).getHistory())
    ).apply(instance, (messages, history) ->
            new ChatComponent.State(new ArrayList<>(messages), new ArrayList<>(history), new ArrayList<>())));
}
