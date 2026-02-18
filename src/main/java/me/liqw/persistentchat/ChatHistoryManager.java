package me.liqw.persistentchat;

import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryManager {
    private static final String FILE_NAME = "chat_history.dat";
    private static final List<Component> messages = new ArrayList<>();
    private static final int MAX_MESSAGES = 100;

    public static void addMessage(MinecraftServer server, Component message) {
        if (message == null) return;

        messages.add(message);

        if (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }

        save(server);
    }

    public static List<Component> getMessages() {
        return messages;
    }

    public static void save(MinecraftServer server) {
        ListTag chatHistory = new ListTag();

        for (Component message : messages) {
            ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, message)
                    .resultOrPartial(error -> PersistentChat.LOGGER.error("Failed to encode message: {}", error))
                    .ifPresent(chatHistory::add);
        }

        CompoundTag root = new CompoundTag();
        root.put("history", chatHistory);

        try {
            NbtIo.writeCompressed(root, getPath(server));
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to save chat history", error);
        }
    }

    public static void load(MinecraftServer server) {
        Path path = getPath(server);
        messages.clear();

        if (!Files.exists(path)) return;

        try {
            CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            ListTag chatHistory = root.getListOrEmpty("history");

            for (Tag tag : chatHistory) {
                ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, tag)
                        .resultOrPartial(error -> PersistentChat.LOGGER.error("Failed to parse message: {}", error))
                        .ifPresent(messages::add);
            }
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to load chat history", error);
        }
    }

    private static Path getPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve(FILE_NAME);
    }
}