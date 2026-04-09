package dev.liqw.persistentchat.utils;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.liqw.persistentchat.mixin.ChatComponentStateAccessor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.network.chat.ComponentSerialization;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Codec {
    private final List<GuiMessage> messages;
    private final List<String> history;

    public static final com.mojang.serialization.Codec<GuiMessage> GUI_MESSAGE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            com.mojang.serialization.Codec.INT.fieldOf("addedTime").forGetter(_ -> -999),
            ComponentSerialization.CODEC.fieldOf("content").forGetter(GuiMessage::content),
            MapCodec.unit(Optional.<net.minecraft.network.chat.MessageSignature>empty())
                    .fieldOf("signature")
                    .forGetter(m -> Optional.ofNullable(m.signature())),
            com.mojang.serialization.Codec.INT.xmap(i -> GuiMessageSource.values()[i], Enum::ordinal).fieldOf("source").forGetter(GuiMessage::source)
    ).apply(inst, (addedTime, content, signature, source) ->
            new GuiMessage(addedTime, content, signature.orElse(null), source, null)
    ));

    public static final com.mojang.serialization.Codec<Codec> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GUI_MESSAGE_CODEC.listOf().fieldOf("messages").forGetter(s -> s.messages),
            com.mojang.serialization.Codec.STRING.listOf().fieldOf("history").forGetter(s -> s.history)
    ).apply(inst, Codec::new));

    public Codec(List<GuiMessage> messages, List<String> history) {
        this.messages = messages;
        this.history = history;
    }

    public static Codec fromVanilla(ChatComponent.State vanilla) {
        ChatComponentStateAccessor accessor = (ChatComponentStateAccessor) vanilla;
        return new Codec(accessor.getMessages(), accessor.getHistory());
    }

    public ChatComponent.State toVanilla() {
        return new ChatComponent.State(
                List.copyOf(this.messages),
                List.copyOf(this.history),
                new ArrayList<>()
        );
    }
}