package dev.liqw.persistentchat.client.mixin;

import dev.liqw.persistentchat.client.utils.StateManager;
import dev.liqw.persistentchat.client.utils.MessageTimestampRegistry;
import dev.liqw.persistentchat.utils.GuiMessageAccessor;
import net.minecraft.client.gui.components.ChatComponent;
//? >=26 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//? } else
//import net.minecraft.client.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow public abstract ChatComponent.State storeState();

    @Inject(method = "clearMessages", at = @At("HEAD"))
    public void catchMessages(boolean history, CallbackInfo ci) {
        StateManager.save(storeState());
    }

    @Inject(method = "addMessageToQueue", at = @At("HEAD"), cancellable = true)
    public void onAddMessageToQueue(GuiMessage message, CallbackInfo ci) {
        if (GuiMessageAccessor.of(message).getTimestamp() == 0L) {
            GuiMessageAccessor.of(message).setTimestamp(
                    MessageTimestampRegistry.consume(message.content())
            );
        }

        if (StateManager.shouldIgnore(GuiMessageAccessor.of(message).getTimestamp())) {
            ci.cancel();
        }
    }
}