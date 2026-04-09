package dev.liqw.persistentchat.mixin;

import dev.liqw.persistentchat.utils.Storage;
import net.minecraft.client.gui.components.ChatComponent;
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
        Storage.save(storeState());
    }
}
