package server.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import server.Command;

@Slf4j
public class DeleteCommand extends Command<Boolean> {
    private final List<String> keyList;
    private final Map<JsonObject, Pair<String, JsonElement>> affectedKeyObjects = new WeakHashMap<>();
    private static final String COMMAND_TYPE = "Delete";

    public DeleteCommand(JsonObject currentDatabaseJson, List<String> keyList) {
        super(currentDatabaseJson);
        this.keyList = keyList;
    }

    @Override
    public Boolean execute() {
        Queue<String> keyQueue = new LinkedList<>();
        keyList.forEach(keyQueue::offer);
        JsonObject keyObject = currentDatabaseJson;
        while (!keyQueue.isEmpty()) {
            String key = keyQueue.poll();
            if (keyQueue.isEmpty()) {
                keyObject.remove(key);
                affectedKeyObjects.put(keyObject, Pair.of(key, keyObject.get(key)));
                this.setResult(Boolean.TRUE);
                break;
            }
            JsonElement keyElement = keyObject.get(key);
            if (keyElement.isJsonObject()) {
                keyObject = keyObject.getAsJsonObject(key);
            } else if (keyElement.isJsonArray()) {
                this.setResult(Boolean.FALSE);
                throw new IllegalArgumentException("Cannot progress into json array");
            } else if (keyElement.isJsonNull() || keyElement.isJsonPrimitive()) {
                this.setResult(Boolean.FALSE);
                throw new IllegalArgumentException("Cannot progress into json array");
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public void print() {
        if (Boolean.TRUE.equals(this.result)) {
            log.info("Successfully deleted the key at: {}", String.join(".", keyList));
        } else {
            log.error("Unsuccessfully deleted the key at: {}", String.join(".", keyList));
        }
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
