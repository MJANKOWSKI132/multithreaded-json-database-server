package server.command;

import com.beust.jcommander.internal.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import org.apache.commons.lang3.tuple.Pair;
import server.Command;

public class DeleteCommand extends Command<Void> {
    private final List<String> keyList;
    private final Map<JsonObject, Pair<String, JsonElement>> affectedKeyObjects = new WeakHashMap<>();

    public DeleteCommand(JsonObject currentDatabaseJson, List<String> keyList) {
        super(currentDatabaseJson);
        this.keyList = keyList;
    }

    @Override
    public Void execute() {
        Queue<String> keyQueue = new LinkedList<>();
        keyList.forEach(keyQueue::offer);
        JsonObject keyObject = currentDatabaseJson;
        while (!keyQueue.isEmpty()) {
            String key = keyQueue.poll();
            if (keyQueue.isEmpty()) {
                keyObject.remove(key);
                affectedKeyObjects.put(keyObject, Pair.of(key, keyObject.get(key)));
                break;
            }
            JsonElement keyElement = keyObject.get(key);
            if (keyElement.isJsonObject()) {
                keyObject = keyObject.getAsJsonObject(key);
            } else if (keyElement.isJsonArray()) {
                throw new IllegalArgumentException("Cannot progress into json array");
            } else if (keyElement.isJsonNull() || keyElement.isJsonPrimitive()) {
                throw new IllegalArgumentException("Cannot progress into json array");
            }
        }
        return null;
    }

    @Override
    public void print() {

    }

    @Override
    protected void rollback() {
        affectedKeyObjects.forEach((keyObject, pair) -> {
            String key = pair.getKey();
            JsonElement element = pair.getValue();
            keyObject.add(key, element);
        });
    }
}
