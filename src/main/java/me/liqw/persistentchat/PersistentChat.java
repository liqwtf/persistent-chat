package me.liqw.persistentchat;

import me.liqw.persistentchat.network.ChatHistoryPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PersistentChat implements ModInitializer {
    public static final String MOD_ID = "persistent-chat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(ChatHistoryManager::load);

        ServerLifecycleEvents.SERVER_STOPPING.register(ChatHistoryManager::save);

        ServerMessageEvents.GAME_MESSAGE.register((server, message, overlay) -> {
            if (!overlay) {
                ChatHistoryManager.addMessage(server, message);
            }
        });

        PayloadTypeRegistry.playS2C().register(ChatHistoryPayload.ID, ChatHistoryPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            List<Component> history = ChatHistoryManager.getMessages();

            if (!history.isEmpty()) {
                sender.sendPacket(new ChatHistoryPayload(history));
            }
        });
    }
}