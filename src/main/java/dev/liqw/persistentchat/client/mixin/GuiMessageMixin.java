package dev.liqw.persistentchat.client.mixin;

import dev.liqw.persistentchat.utils.GuiMessageAccessor;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements GuiMessageAccessor {
    @Unique private long timestamp = 0L;
    @Unique private boolean isFromPayload = false;

    @Override public long getTimestamp() { return timestamp; }
    @Override public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    @Override public boolean isFromPayload() { return isFromPayload; }
    @Override public void setFromPayload(boolean fromPayload) { this.isFromPayload = fromPayload; }
}