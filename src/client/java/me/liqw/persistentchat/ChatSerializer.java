package me.liqw.persistentchat;

import net.minecraft.client.GuiMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.mojang.serialization.DataResult;

public class ChatSerializer {
    private static final String FILE_NAME = "persistent_chat.dat";

    public static void save(List<GuiMessage> messages) {
        Minecraft client = Minecraft.getInstance();
        IntegratedServer server = client.getSingleplayerServer();

        if (server == null)
            return;

        File directory = server.getWorldPath(LevelResource.ROOT).toFile();
        File file = new File(directory, FILE_NAME);

        CompoundTag root = new CompoundTag();
        ListTag historyList = new ListTag();

        for (GuiMessage message : messages) {
            CompoundTag entry = new CompoundTag();

            DataResult<Tag> serializedMessage = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE,
                    message.content());

            entry.put("message", serializedMessage.getOrThrow());
            entry.putInt("created", message.addedTime());
            historyList.add(entry);
        }

        root.put("history", historyList);

        try {
            NbtIo.writeCompressed(root, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Component> load() {
        Minecraft client = Minecraft.getInstance();
        IntegratedServer server = client.getSingleplayerServer();

        List<Component> messages = new ArrayList<>();

        if (server == null)
            return messages;

        File directory = server.getWorldPath(LevelResource.ROOT).toFile();
        File file = new File(directory, FILE_NAME);

        if (!file.exists())
            return messages;

        try {
            CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            ListTag historyList = root.getListOrEmpty("history");

            for (int i = 0; i < historyList.size(); i++) {
                CompoundTag entry = historyList.getCompoundOrEmpty(i);

                DataResult<Component> result = ComponentSerialization.CODEC.parse(NbtOps.INSTANCE,
                        entry.get("message"));

                if (result != null)
                    messages.add(result.getOrThrow());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return messages;
    }
}