package server;

public abstract class Command<T> {
    private T result;

    protected void setResult(T value) {
        this.result = value;
    }

    protected T getResult() {
        return this.result;
    }

    protected abstract T execute();
    protected abstract void print();
}
