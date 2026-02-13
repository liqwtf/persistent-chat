package me.liqw.persistentchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PersistentChatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            if (client.isSingleplayer()) {
                List<Component> messages = ChatSerializer.load();

                for (Component message : messages) {
                    client.gui.getChat().addMessage(message);
                }

            }
        });
    }
}