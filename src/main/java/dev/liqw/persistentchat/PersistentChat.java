package dev.liqw.persistentchat;

import dev.liqw.persistentchat.config.PersistentChatConfig;
import dev.liqw.persistentchat.network.MessageBufferPayload;
import dev.liqw.persistentchat.utils.MessageBuffer;
import dev.liqw.persistentchat.utils.TimestampedMessage;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
        //~ if <26 'clientboundPlay()' -> 'playS2C()'
        PayloadTypeRegistry.clientboundPlay().register(MessageBufferPayload.TYPE, MessageBufferPayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(MessageBuffer::initialize);
        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> MessageBuffer.flush());

        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            server.execute(() -> {
                List<TimestampedMessage> buffer = MessageBuffer.getBuffer();
                if (!buffer.isEmpty()) {
                    List<Component> messages = buffer.stream().map(TimestampedMessage::content).toList();
                    List<Long> timestamps = buffer.stream().map(TimestampedMessage::timestamp).toList();

                    sender.sendPacket(new MessageBufferPayload(messages, timestamps));
                }
            });
        });

        AutoConfig.register(PersistentChatConfig.class, GsonConfigSerializer::new);
    }

    public static PersistentChatConfig getConfig() {
        return AutoConfig.getConfigHolder(PersistentChatConfig.class).getConfig();
    }
}