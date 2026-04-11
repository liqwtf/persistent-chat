package dev.liqw.persistentchat.client.config;

import dev.liqw.persistentchat.PersistentChat;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = PersistentChat.MOD_ID)
public class PersistentChatConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean enabled = true;

    @ConfigEntry.Gui.Tooltip
    public boolean shareHistory = false;
}
