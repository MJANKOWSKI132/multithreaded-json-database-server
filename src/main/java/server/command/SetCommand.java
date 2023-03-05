package server.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import server.Command;

@AllArgsConstructor(staticName = "newInstance")
public class SetCommand extends Command<JsonObject> {
    private JsonObject currentDatabaseJson;
    private List<String> keyList;
    private JsonElement valueJson;
    private String value;

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
                    if (!Objects.isNull(valueJson)) {
                        keyObject.add(key, valueJson);
                    } else {
                        keyObject.addProperty(key, value);
                    }
                    this.setResult(currentDatabaseJson);
                    return currentDatabaseJson;
                } else {
                    throw new IllegalArgumentException("Does not have key");
                }
            }
            JsonElement keyElement = keyObject.get(key);
            if (keyElement.isJsonObject()) {
                if (keyQueue.isEmpty()) {
                    if (!Objects.isNull(valueJson)) {
                        keyObject.add(key, valueJson);
                    } else {
                        keyObject.addProperty(key, value);
                    }
                    this.setResult(currentDatabaseJson);
                    return currentDatabaseJson;
                }
                keyObject = keyObject.getAsJsonObject(key);
            } else if (keyElement.isJsonArray()) {
                JsonArray keyArray = keyElement.getAsJsonArray();
                if (!Objects.isNull(valueJson)) {
                    keyArray.add(valueJson);
                } else {
                    keyArray.add(value);
                }
                this.setResult(currentDatabaseJson);
                return currentDatabaseJson;
            } else if (keyElement.isJsonNull() || keyElement.isJsonPrimitive()) {
                if (!Objects.isNull(valueJson)) {
                    keyObject.add(key, valueJson);
                } else {
                    keyObject.addProperty(key, value);
                }
                this.setResult(currentDatabaseJson);
                return currentDatabaseJson;
            }
        }
        this.setResult(currentDatabaseJson);
        return currentDatabaseJson;
    }

    @Override
    public void print() {

    }
}
