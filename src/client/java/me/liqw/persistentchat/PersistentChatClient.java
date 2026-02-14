package me.liqw.persistentchat;

import me.liqw.persistentchat.mixin.client.ChatComponentAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.server.IntegratedServer;

import java.util.List;

public class PersistentChatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            IntegratedServer server = client.getSingleplayerServer();

            if (server != null) {
                List<GuiMessage> messages = ChatHistoryState.load(server);
                ChatComponentAccessor chat = (ChatComponentAccessor) client.gui.getChat();

                overwriteHistory(chat, messages);
            }
        });
    }

    private void overwriteHistory(ChatComponentAccessor chat, List<GuiMessage> messages) {
        List<GuiMessage> history = chat.getAllMessages();

        history.clear();
        history.addAll(messages);
        chat.invokeRefreshTrimmedMessages();
    }
}