package dev.liqw.persistentchat.utils;

import net.minecraft.network.chat.Component;

public record TimestampedMessage(Component content, long timestamp) {}