package server.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import server.Command;

public class GetCommand extends Command<JsonElement> {
    private final List<String> keyList;

    public GetCommand(JsonObject currentDatabaseJson, List<String> keyList) {
        super(currentDatabaseJson);
        this.keyList = keyList;
    }

    @Override
    public JsonElement execute() {
        Queue<String> keyQueue = new LinkedList<>();
        keyList.forEach(keyQueue::offer);
        JsonObject keyObject = currentDatabaseJson;
        while (!keyQueue.isEmpty()) {
            String key = keyQueue.poll();
            if (keyQueue.isEmpty()) {
                JsonElement value = keyObject.get(key);
                this.setResult(value);
                return value;
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
        throw new IllegalArgumentException("Could not find value at key provided");
    }

    @Override
    public void print() {

    }
}
