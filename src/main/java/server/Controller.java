package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Controller {
    private static Controller _instance = null;
    private List<Command<?>> commandHistory;

    private Controller() {

    }

    public static Controller getInstance() {
        if (Objects.isNull(_instance)) {
            _instance = new Controller();
            _instance.commandHistory = new ArrayList<>();
        }
        return _instance;
    }

    public void executeCommand(Command<?> command) {
        command.execute();
        commandHistory.add(command);
    }

    public void printCommandHistory() {
        commandHistory.forEach(Command::print);
    }
}
