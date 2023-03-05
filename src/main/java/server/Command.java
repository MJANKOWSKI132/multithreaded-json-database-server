package server;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

public abstract class Command<T> {
    @Getter @Setter
    protected T result;
    @Getter
    protected JsonObject currentDatabaseJson;

    protected Command(JsonObject currentDatabaseJson) {
        this.currentDatabaseJson = currentDatabaseJson;
    }

    protected abstract T execute();
    protected abstract void print();
    protected void rollback() {

    }
}
