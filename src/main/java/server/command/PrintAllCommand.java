package server.command;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import server.Command;

@Slf4j
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
        log.info("Printed all commands in the command history");
    }
}
