package dev.liqw.persistentchat.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageBuffer {
    private static final int MAX_BUFFER_SIZE = 100;
    private static final List<TimestampedMessage> buffer = Collections.synchronizedList(new ArrayList<>());

    public static void save(TimestampedMessage message) {
        buffer.add(message);
        if (buffer.size() > MAX_BUFFER_SIZE) {
            buffer.removeFirst();
        }
    }

    public static List<TimestampedMessage> getBuffer() {
        return new ArrayList<>(buffer);
    }
}
