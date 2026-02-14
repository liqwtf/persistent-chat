package me.liqw.persistentchat.mixin.client;

import me.liqw.persistentchat.ChatHistoryState;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "disconnect*", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        IntegratedServer server = client.getSingleplayerServer();

        if (server != null) {
            ChatComponentAccessor chat = (ChatComponentAccessor) client.gui.getChat();
            List<GuiMessage> messages = chat.getAllMessages();

            ChatHistoryState.save(server, messages);
        }

    }
}