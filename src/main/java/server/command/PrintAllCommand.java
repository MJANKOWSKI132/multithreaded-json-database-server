package server.command;

import com.google.gson.JsonObject;
import server.Command;

public class PrintAllCommand<K, V> extends Command<Void> {
    protected PrintAllCommand(JsonObject currentDatabaseJson) {
        super(currentDatabaseJson);
    }

    @Override
    public Void execute() {
        return null;
    }

    @Override
    public void print() {
        System.out.printf("Printed all items in the map");
    }
}
