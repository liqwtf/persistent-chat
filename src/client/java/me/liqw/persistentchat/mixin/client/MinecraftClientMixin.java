package me.liqw.persistentchat.mixin.client;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.liqw.persistentchat.ChatSerializer;

import java.util.List;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "disconnect*", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        if (client.isSingleplayer()) {
            try {
                List<GuiMessage> messages = ((ChatComponentAccessor) client.gui.getChat()).getAllMessages();

                ChatSerializer.save(messages);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}