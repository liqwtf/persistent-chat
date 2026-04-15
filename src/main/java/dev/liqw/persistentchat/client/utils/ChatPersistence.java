package dev.liqw.persistentchat.client.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.liqw.persistentchat.client.mixin.ChatComponentStateAccessor;
import dev.liqw.persistentchat.utils.GuiMessageAccessor;
import net.minecraft.client.gui.components.ChatComponent;
//? >=26 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
//? } else {
/*import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
*///? }
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MessageSignature;

import java.util.ArrayList;
import java.util.Optional;

public class ChatPersistence {
    public static final GuiMessageTag GUI_MESSAGE_TAG_SERVER = new GuiMessageTag(13616525, null, Component.translatable("persistent-chat.chat.tag.payload"), "Retrieved from server");
    public static final GuiMessageTag GUI_MESSAGE_TAG_LOCAL = new GuiMessageTag(9418383, null, Component.translatable("persistent-chat.chat.tag.local"), "Restored message");

    private static final Codec<GuiMessage> GUI_MESSAGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("content").forGetter(GuiMessage::content),
            //? >=26
            Codec.INT.xmap(i -> GuiMessageSource.values()[i], Enum::ordinal).fieldOf("source").forGetter(GuiMessage::source),
            Codec.LONG.fieldOf("timestamp").forGetter(message -> GuiMessageAccessor.of(message).getTimestamp())
    ).apply(instance, (content, /*? >=26 {*/ source, /*? }*/ timestamp) -> {
        GuiMessage message = new GuiMessage(-999, content, null, /*? >=26 {*/ source, /*? }*/ GUI_MESSAGE_TAG_LOCAL);
        GuiMessageAccessor.of(message).setTimestamp(timestamp);
        return message;
    }));

    public static final Codec<ChatComponent.State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GUI_MESSAGE_CODEC.listOf().fieldOf("messages").forGetter(state -> ((ChatComponentStateAccessor) state).getMessages()),
            Codec.STRING.listOf().fieldOf("history").forGetter(state -> ((ChatComponentStateAccessor) state).getHistory())
    ).apply(instance, (messages, history) ->
            new ChatComponent.State(new ArrayList<>(messages), new ArrayList<>(history), new ArrayList<>())
    ));
}
