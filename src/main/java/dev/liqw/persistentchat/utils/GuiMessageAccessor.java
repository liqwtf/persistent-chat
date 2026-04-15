package dev.liqw.persistentchat.utils;

//? >=26 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
 //? } else {
/*import net.minecraft.client.GuiMessage;
*///? }
import org.spongepowered.asm.mixin.Unique;

public interface GuiMessageAccessor {
    @Unique long getTimestamp();
    @Unique void setTimestamp(long timestamp);
    @Unique boolean isFromPayload();
    @Unique void setFromPayload(boolean fromPayload);

    static GuiMessageAccessor of(GuiMessage message) {
        return (GuiMessageAccessor) (Object) message;
    }
}
