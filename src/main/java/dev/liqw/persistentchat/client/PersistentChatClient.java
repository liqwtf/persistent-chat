package dev.liqw.persistentchat.client;

import dev.liqw.persistentchat.PersistentChat;
import dev.liqw.persistentchat.client.config.PersistentChatClientConfig;
import dev.liqw.persistentchat.client.utils.MessageTimestampRegistry;
import dev.liqw.persistentchat.client.utils.StateManager;
import dev.liqw.persistentchat.network.MessageBufferPayload;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PersistentChatClient implements ClientModInitializer {
    private static ScheduledFuture<?> pendingLoad = null;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(PersistentChatClientConfig.class, GsonConfigSerializer::new);

        ClientPlayNetworking.registerGlobalReceiver(MessageBufferPayload.TYPE, (payload, context) -> {
            PersistentChat.LOGGER.info("Payload received, cancelling pending load: {}", pendingLoad != null);
            if (pendingLoad != null) {
                pendingLoad.cancel(false);
                pendingLoad = null;
            }

            Minecraft client = context.client();

            for (int i = 0; i < payload.messages().size(); i++) {
                MessageTimestampRegistry.register(payload.messages().get(i), payload.timestamps().get(i));
            }

            client.execute(() -> StateManager.load(client.gui.getChat(), payload));
        });

        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> {
            PersistentChat.LOGGER.info("JOIN fired, scheduling file load");
            pendingLoad = scheduler.schedule(() -> {
                client.execute(() -> StateManager.load(client.gui.getChat()));
            }, 500, TimeUnit.MILLISECONDS);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> {
            if (pendingLoad != null) {
                pendingLoad.cancel(false);
                pendingLoad = null;
            }
            StateManager.reset();
        });
    }

    public static PersistentChatClientConfig getConfig() {
        return AutoConfig.getConfigHolder(PersistentChatClientConfig.class).getConfig();
    }
}