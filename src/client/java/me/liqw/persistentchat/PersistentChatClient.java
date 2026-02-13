package me.liqw.persistentchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.GuiMessage;

import java.util.List;

public class PersistentChatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            if (client.isSingleplayer()) {
                List<GuiMessage> messages = ChatSerializer.load();

                for (GuiMessage message : messages) {
                    client.gui.getChat().addMessage(message.content());
                }

            }
        });
    }
}