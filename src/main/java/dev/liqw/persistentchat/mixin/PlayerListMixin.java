package dev.liqw.persistentchat.mixin;

import dev.liqw.persistentchat.PersistentChat;
import dev.liqw.persistentchat.utils.MessageBuffer;
import dev.liqw.persistentchat.utils.TimestampedMessage;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"))
    public void catchChatMessage(PlayerChatMessage message, Predicate<ServerPlayer> isFiltered, ServerPlayer senderPlayer, ChatType.Bound chatType, CallbackInfo ci) {
        Component decorated = chatType.decorate(message.decoratedContent());
        MessageBuffer.save(new TimestampedMessage(decorated, System.currentTimeMillis()));
    }

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"))
    public void catchSystemMessage(Component message, boolean overlay, CallbackInfo ci) {
        if (!overlay && PersistentChat.getConfig().saveSystemMessages) {
            MessageBuffer.save(new TimestampedMessage(message, System.currentTimeMillis()));
        }
    }
}
