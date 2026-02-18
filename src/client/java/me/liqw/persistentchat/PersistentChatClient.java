package me.liqw.persistentchat;

import me.liqw.persistentchat.mixin.client.ChatComponentAccessor;
import me.liqw.persistentchat.network.ChatHistoryPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GuiMessage;

import java.util.List;

public class PersistentChatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChatHistoryPayload.ID, (payload, context) -> {
            ChatComponentAccessor chat = (ChatComponentAccessor) context.client().gui.getChat();
            List<GuiMessage> messages = payload.messages().stream()
                    .map(component -> new GuiMessage(0, component, null, null)).toList();

            context.client().execute(() -> {
                overwriteHistory(chat, messages);
            });
        });
    }

    private void overwriteHistory(ChatComponentAccessor chat, List<GuiMessage> messages) {
        List<GuiMessage> history = chat.getAllMessages();

        history.clear();
        history.addAll(messages);
        chat.invokeRefreshTrimmedMessages();
    }
}