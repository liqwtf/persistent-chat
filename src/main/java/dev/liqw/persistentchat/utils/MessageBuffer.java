package dev.liqw.persistentchat.utils;

import com.mojang.serialization.Codec;
import dev.liqw.persistentchat.PersistentChat;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageBuffer {
    private static final int MAX_BUFFER_SIZE = 100;
    private static final List<TimestampedMessage> buffer = Collections.synchronizedList(new ArrayList<>());
    private static final Codec<List<TimestampedMessage>> LIST_CODEC = TimestampedMessage.CODEC.listOf();
    private static final String NBT_KEY = "messages";

    private static MinecraftServer server = null;
    private static Path path = null;

    public static void initialize(MinecraftServer server) {
        MessageBuffer.server = server;
        path = server.getWorldPath(LevelResource.ROOT)
                .resolve(".persistent-chat")
                .resolve("message-buffer.dat");

        buffer.clear();

        if (!Files.exists(path)) return;

        try {
            CompoundTag root = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());

            LIST_CODEC.parse(ops, root.get(NBT_KEY)).ifSuccess(loaded -> {
                buffer.addAll(loaded);
                PersistentChat.LOGGER.info("Loaded {} messages from server buffer", buffer.size());
            });
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to load server chat buffer", error);
        }
    }

    public static void save(TimestampedMessage message) {
        buffer.add(message);
        if (buffer.size() > MAX_BUFFER_SIZE) {
            buffer.removeFirst();
        }

        if (path == null) {
            PersistentChat.LOGGER.warn("MessageBuffer not initialized, messages will not persist between server restarts");
            return;
        }

        flush();
    }

    public static void flush() {
        if (server == null || path == null) return;

        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());

        LIST_CODEC.encodeStart(ops, new ArrayList<>(buffer)).ifSuccess(tag -> {
            try {
                Files.createDirectories(path.getParent());
                CompoundTag root = new CompoundTag();
                root.put(NBT_KEY, tag);
                NbtIo.writeCompressed(root, path);
            } catch (IOException error) {
                PersistentChat.LOGGER.error("Failed to save server chat buffer", error);
            }
        });
    }

    public static List<TimestampedMessage> getBuffer() {
        return new ArrayList<>(buffer);
    }
}