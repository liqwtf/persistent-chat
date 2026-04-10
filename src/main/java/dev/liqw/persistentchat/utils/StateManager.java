package dev.liqw.persistentchat.utils;

import dev.liqw.persistentchat.PersistentChat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StateManager {
    private static Path activePath = null; // temp fix, server is not found on exit

    private static String parseFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9.()-]", "_").toLowerCase();
    }

    private static Path getPath() {
        Minecraft client = Minecraft.getInstance();
        String fileName = "fallback.dat";

        if (client.level != null && !PersistentChat.getConfig().shareHistory) {
            if (client.getSingleplayerServer() != null) {
                String worldName = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
                fileName = "singleplayer/" + parseFileName(worldName) + ".dat";
            } else {
                ServerData serverData = client.getCurrentServer();
                if (serverData != null) {
                    fileName = "server/" + parseFileName(serverData.ip) + ".dat";
                }
            }
        }

        return FabricLoader.getInstance().getGameDir()
                .resolve(".persistent-chat")
                .resolve(fileName);
    }

    public static void save(ChatComponent.State state) {
        Minecraft client = Minecraft.getInstance();
        Path path = (activePath != null) ? activePath : getPath();

        if (client.level == null) return;

        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, client.level.registryAccess());

        ChatComponentState.CODEC.encodeStart(ops, state).ifSuccess(nbt -> {
            try {
                Files.createDirectories(path.getParent());
                if (nbt instanceof CompoundTag compound) {
                    NbtIo.writeCompressed(compound, path);
                    PersistentChat.LOGGER.info("Saved {} chat state entries", compound.size());
                }
            } catch (IOException error) {
                PersistentChat.LOGGER.error("Failed to save chat state", error);
            }
        });

        activePath = null;
    }

    public static void load(ChatComponent chat) {
        if (!PersistentChat.getConfig().enabled) return;

        activePath = getPath();
        if (!Files.exists(activePath)) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        try {
            CompoundTag root = NbtIo.readCompressed(activePath, NbtAccounter.unlimitedHeap());
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, client.level.registryAccess());

            ChatComponentState.CODEC.parse(ops, root).ifSuccess(chat::restoreState);
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to load chat state", error);
        }
    }
}