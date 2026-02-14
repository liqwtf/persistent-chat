package me.liqw.persistentchat;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryState {
    private static final String FILE_NAME = "chat_history.dat";

    public static void save(IntegratedServer server, List<GuiMessage> messages) {
        ListTag chatHistory = new ListTag();

        for (GuiMessage message : messages) {
            ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, message.content())
                    .resultOrPartial(err -> PersistentChat.LOGGER.error("Failed to encode message: {}", err))
                    .ifPresent(chatHistory::add);
        }

        CompoundTag root = new CompoundTag();
        root.put("history", chatHistory);

        try {
            NbtIo.writeCompressed(root, getPath(server));
            PersistentChat.LOGGER.info("Saved {} chat messages", chatHistory.size());
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to save chat history", error);
        }
    }

    public static List<GuiMessage> load(IntegratedServer server) {
        Path path = getPath(server);
        List<GuiMessage> messages = new ArrayList<>();

        if (!Files.exists(path)) return messages;

        try {
            CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            ListTag chatHistory = root.getListOrEmpty("history");

            for (Tag tag : chatHistory) {
                ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, tag)
                        .resultOrPartial(err -> PersistentChat.LOGGER.error("Failed to parse message: {}", err))
                        .ifPresent(component -> messages.add(new GuiMessage(0, component, null, null)));
            }
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to load chat history", error);
        }

        return messages;
    }

    private static Path getPath(IntegratedServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
    }
}