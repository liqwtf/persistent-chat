package dev.liqw.persistentchat.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record TimestampedMessage(Component content, long timestamp) {
    public static final Codec<TimestampedMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("content").forGetter(TimestampedMessage::content),
            Codec.LONG.fieldOf("timestamp").forGetter(TimestampedMessage::timestamp)
    ).apply(instance, TimestampedMessage::new));
}