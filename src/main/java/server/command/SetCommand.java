package server.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.WeakHashMap;
import org.apache.commons.lang3.tuple.Pair;
import server.Command;

public class SetCommand extends Command<JsonObject> {
    private final List<String> keyList;
    private final JsonElement valueJson;
    private final String value;
    private final Map<JsonObject, Pair<String, JsonElement>> affectedKeyObjects = new WeakHashMap<>();

    public SetCommand(JsonObject currentDatabaseJson, List<String> keyList, JsonElement valueJson, String value) {
        super(currentDatabaseJson);
        this.keyList = keyList;
        this.valueJson = valueJson;
        this.value = value;
    }

    @Override
    public JsonObject execute()  {
        if (Objects.isNull(currentDatabaseJson)) {
            currentDatabaseJson = new JsonObject();
        }
        Queue<String> keyQueue = new LinkedList<>();
        keyList.forEach(keyQueue::offer);
        JsonObject keyObject = currentDatabaseJson;
        while (!keyQueue.isEmpty()) {
            String key = keyQueue.poll();
            if (!keyObject.has(key)) {
                if (keyQueue.isEmpty()) {
                    return addToIndentedKeyObject(keyObject, key);
                } else {
                    throw new IllegalArgumentException("Does not have key");
                }
            }
            JsonElement keyElement = keyObject.get(key);
            if (keyElement.isJsonObject()) {
                if (keyQueue.isEmpty()) {
                    return addToIndentedKeyObject(keyObject, key);
                }
                keyObject = keyObject.getAsJsonObject(key);
            } else if (keyElement.isJsonArray()) {
                JsonArray keyArray = keyElement.getAsJsonArray();
                if (!Objects.isNull(valueJson)) {
                    addToAffectedKeyObjectMap(key, keyObject);
                    keyArray.add(valueJson);
                } else {
                    addToAffectedKeyObjectMap(key, keyObject);
                    keyArray.add(value);
                }
                this.setResult(currentDatabaseJson);
                return currentDatabaseJson;
            } else if (keyElement.isJsonNull() || keyElement.isJsonPrimitive()) {
                return addToIndentedKeyObject(keyObject, key);
            }
        }
        this.setResult(currentDatabaseJson);
        return currentDatabaseJson;
    }

    private void addToAffectedKeyObjectMap(String key, JsonObject keyObject) {
        affectedKeyObjects.put(
                keyObject,
                Pair.of(
                        key,
                        keyObject.has(key) ? keyObject.get(key) : null
                )
        );
    }

    private JsonObject addToIndentedKeyObject(JsonObject keyObject, String key) {
        if (!Objects.isNull(valueJson)) {
            addToAffectedKeyObjectMap(key, keyObject);
            keyObject.add(key, valueJson);
        } else {
            addToAffectedKeyObjectMap(key, keyObject);
            keyObject.addProperty(key, value);
        }
        this.setResult(currentDatabaseJson);
        return currentDatabaseJson;
    }

    @Override
    public void print() {

    }

    @Override
    protected void rollback() {
        affectedKeyObjects.forEach((keyObject, pair) -> {
            String key = pair.getKey();
            JsonElement element = pair.getValue();
            if (Objects.isNull(element)) {
                keyObject.remove(key);
            } else {
                keyObject.add(key, element);
            }
        });
    }
}
