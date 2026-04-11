package dev.liqw.persistentchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.liqw.persistentchat.PersistentChat;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PersistentChatConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("persistent-chat.json");
    }

    private static PersistentChatConfig instance = new PersistentChatConfig();

    public boolean saveSystemMessages = true;

    public static PersistentChatConfig get() {
        return instance;
    }

    public static void load() {
        Path path = getPath();

        if (!Files.exists(path)) {
            save();
            return;
        }

        try {
            String json = Files.readString(path);
            instance = GSON.fromJson(json, PersistentChatConfig.class);
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to load server config", error);
            instance = new PersistentChatConfig();
        }
    }

    private static void save() {
        try {
            Files.writeString(getPath(), GSON.toJson(instance));
        } catch (IOException error) {
            PersistentChat.LOGGER.error("Failed to save server config", error);
        }
    }
}
