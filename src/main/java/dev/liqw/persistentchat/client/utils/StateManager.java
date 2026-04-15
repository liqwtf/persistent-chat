package dev.liqw.persistentchat.client.utils;

import dev.liqw.persistentchat.PersistentChat;
import dev.liqw.persistentchat.client.PersistentChatClient;
import dev.liqw.persistentchat.client.mixin.ChatComponentStateAccessor;
import dev.liqw.persistentchat.network.MessageBufferPayload;
import dev.liqw.persistentchat.utils.GuiMessageAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StateManager {
    private static Path activePath = null;
    private static boolean loadedUsingPayload = false;

    private static final Set<Long> recentlyRestored = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static long restoreTime = 0;
    private static final long RESTORE_WINDOW_MS = 3000;
    private static final long TIMESTAMP_MARGIN_MS = 150;

    private static boolean timestampMatches(long saved, long payload) {
        return Math.abs(saved - payload) <= TIMESTAMP_MARGIN_MS;
    }

    public static boolean shouldIgnore(long timestamp) {
        if (System.currentTimeMillis() - restoreTime > RESTORE_WINDOW_MS) return false;
        return recentlyRestored.contains(timestamp);
    }

    private static void markRestored(List<GuiMessage> messages) {
        recentlyRestored.clear();
        messages.forEach(msg -> recentlyRestored.add(GuiMessageAccessor.of(msg).getTimestamp()));
        restoreTime = System.currentTimeMillis();
    }

    private static String parseFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9.()-]", "_").toLowerCase();
    }

    private static Path getPath() {
        Minecraft client = Minecraft.getInstance();
        String fileName = "fallback.dat";

        if (client.level != null && !PersistentChatClient.getConfig().shareHistory) {
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

        List<GuiMessage> filtered = ((ChatComponentStateAccessor) state).getMessages().stream()
                .filter(message -> !GuiMessageAccessor.of(message).isFromPayload())
                .toList();

        ChatComponent.State filteredState = new ChatComponent.State(
                new ArrayList<>(filtered),
                new ArrayList<>(((ChatComponentStateAccessor) state).getHistory()),
                new ArrayList<>()
        );

        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, client.level.registryAccess());

        ChatComponentState.CODEC.encodeStart(ops, filteredState).ifSuccess(nbt -> {
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
        if (!PersistentChatClient.getConfig().enabled || loadedUsingPayload) return;

        activePath = getPath();
        if (!Files.exists(activePath)) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        try {
            CompoundTag root = NbtIo.readCompressed(activePath, NbtAccounter.unlimitedHeap());
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, client.level.registryAccess());

            ChatComponentState.CODEC.parse(ops, root).ifSuccess(state -> {
                List<GuiMessage> messages = ((ChatComponentStateAccessor) state).getMessages();
                sortByTimestamp(messages);
                markRestored(messages);
                chat.restoreState(state);
            });
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to load chat state", error);
        }
    }

    public static void load(ChatComponent chat, MessageBufferPayload payload) {
        loadedUsingPayload = true;

        if (!PersistentChatClient.getConfig().enabled) return;

        activePath = getPath();

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        if (!Files.exists(activePath)) {
            List<GuiMessage> messages = new ArrayList<>();
            for (int i = 0; i < payload.messages().size(); i++) {
                GuiMessage message = new GuiMessage(-999, payload.messages().get(i), null, GuiMessageSource.PLAYER, ChatComponentState.GUI_MESSAGE_TAG_SERVER);
                GuiMessageAccessor.of(message).setTimestamp(payload.timestamps().get(i));
                GuiMessageAccessor.of(message).setFromPayload(true);
                messages.add(message);
            }
            sortByTimestamp(messages);
            markRestored(messages);
            chat.restoreState(new ChatComponent.State(messages, new ArrayList<>(), new ArrayList<>()));
            return;
        }

        try {
            CompoundTag root = NbtIo.readCompressed(activePath, NbtAccounter.unlimitedHeap());
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, client.level.registryAccess());

            ChatComponentState.CODEC.parse(ops, root).ifSuccess(state -> {
                List<GuiMessage> messages = ((ChatComponentStateAccessor) state).getMessages();

                Set<Long> existingTimestamps = messages.stream()
                        .map(msg -> GuiMessageAccessor.of(msg).getTimestamp())
                        .collect(Collectors.toSet());

                for (int i = 0; i < payload.messages().size(); i++) {
                    Long timestamp = payload.timestamps().get(i);
                    boolean isDuplicate = existingTimestamps.stream()
                            .anyMatch(savedTs -> timestampMatches(savedTs, timestamp));
                    if (isDuplicate) continue;

                    GuiMessage message = new GuiMessage(-999, payload.messages().get(i), null, GuiMessageSource.PLAYER, ChatComponentState.GUI_MESSAGE_TAG_SERVER);
                    GuiMessageAccessor.of(message).setTimestamp(timestamp);
                    GuiMessageAccessor.of(message).setFromPayload(true);
                    messages.add(message);
                }

                sortByTimestamp(messages);
                markRestored(messages);
                chat.restoreState(state);
            });
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to load chat state", error);
        }
    }

    private static void sortByTimestamp(List<GuiMessage> messages) {
        messages.sort(Comparator.comparingLong(message -> GuiMessageAccessor.of(message).getTimestamp()));
        Collections.reverse(messages);
    }

    public static void reset() {
        loadedUsingPayload = false;
        recentlyRestored.clear();
        restoreTime = 0;
    }
}