package dev.liqw.persistentchat;

import dev.liqw.persistentchat.utils.Storage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentChat implements ClientModInitializer {
    public static final String MOD_ID = "persistent-chat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> {
            Storage.load(client.gui.getChat());
        });
    }
}