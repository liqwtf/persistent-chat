package dev.liqw.persistentchat.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.liqw.persistentchat.mixin.ChatComponentStateAccessor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MessageSignature;

import java.util.ArrayList;
import java.util.Optional;

public class ChatComponentState {
    private static final Codec<GuiMessage> GUI_MESSAGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MapCodec.unit(0).fieldOf("addedTime").forGetter(m -> -999),
            ComponentSerialization.CODEC.fieldOf("content").forGetter(GuiMessage::content),
            MapCodec.unit(Optional.<MessageSignature>empty()).fieldOf("signature").forGetter(m -> Optional.ofNullable(m.signature())),
            Codec.INT.xmap(i -> GuiMessageSource.values()[i], Enum::ordinal).fieldOf("source").forGetter(GuiMessage::source)
    ).apply(instance, (time, content, signature, source) ->
            new GuiMessage(time, content, signature.orElse(null), source, null)));

    public static final Codec<ChatComponent.State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GUI_MESSAGE_CODEC.listOf().fieldOf("messages").forGetter(state -> ((ChatComponentStateAccessor) state).getMessages()),
            Codec.STRING.listOf().fieldOf("history").forGetter(state -> ((ChatComponentStateAccessor) state).getHistory())
    ).apply(instance, (messages, history) ->
            new ChatComponent.State(messages, history, new ArrayList<>())));
}
