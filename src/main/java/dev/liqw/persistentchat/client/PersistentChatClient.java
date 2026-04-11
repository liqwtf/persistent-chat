package dev.liqw.persistentchat.client;

import dev.liqw.persistentchat.client.config.PersistentChatConfig;
import dev.liqw.persistentchat.client.utils.MessageTimestampRegistry;
import dev.liqw.persistentchat.client.utils.StateManager;
import dev.liqw.persistentchat.network.MessageBufferPayload;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class PersistentChatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MessageBufferPayload.TYPE, (payload, context) -> {
            Minecraft client = context.client();

            for (int i = 0; i < payload.messages().size(); i++) {
                MessageTimestampRegistry.register(payload.messages().get(i), payload.timestamps().get(i));
            }

            client.execute(() -> StateManager.load(client.gui.getChat(), payload));
        });

        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> {
            client.execute(() ->  StateManager.load(client.gui.getChat()));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> {
            StateManager.reset();
        });

        AutoConfig.register(PersistentChatConfig.class, GsonConfigSerializer::new);
    }

    public static PersistentChatConfig getConfig() {
        return AutoConfig.getConfigHolder(PersistentChatConfig.class).getConfig();
    }
}