package dev.liqw.persistentchat.client.utils;

import net.minecraft.network.chat.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageTimestampRegistry {
    private static final Map<String, Long> pending = new ConcurrentHashMap<>();

    public static void register(Component content, long timestamp) {
        pending.put(content.getString(), timestamp);
    }

    public static long consume(Component content) {
        Long timestamp = pending.remove(content.getString());
        return timestamp != null ? timestamp : System.currentTimeMillis();
    }
}