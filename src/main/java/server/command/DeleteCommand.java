package server.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import server.Command;

@RequiredArgsConstructor(staticName = "newInstance")
public class DeleteCommand extends Command<Void> {
    private final JsonObject currentDatabaseJson;
    private final List<String> keyList;

    @Override
    public Void execute() {
        Queue<String> keyQueue = new LinkedList<>();
        keyList.forEach(keyQueue::offer);
        JsonObject keyObject = currentDatabaseJson;
        while (!keyQueue.isEmpty()) {
            String key = keyQueue.poll();
            if (keyQueue.isEmpty()) {
                keyObject.remove(key);
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
}
