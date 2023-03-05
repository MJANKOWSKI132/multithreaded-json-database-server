package server.command;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import server.Command;

@RequiredArgsConstructor(staticName = "newInstance")
public class PrintAllCommand<K, V> extends Command<Void> {
    private final JsonObject currentDatabaseJson;

    @Override
    public Void execute() {
        return null;
    }

    @Override
    public void print() {
        System.out.printf("Printed all items in the map");
    }
}
