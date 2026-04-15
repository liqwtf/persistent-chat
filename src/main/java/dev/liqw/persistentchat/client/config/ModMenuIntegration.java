package dev.liqw.persistentchat.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
//~ if <=1.21.1 'AutoConfigClient' -> 'AutoConfig'
import me.shedaniel.autoconfig.AutoConfigClient;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        //~ if <=1.21.1 'AutoConfigClient' -> 'AutoConfig'
        return parent -> AutoConfigClient.getConfigScreen(PersistentChatClientConfig.class, parent).get();
    }
}