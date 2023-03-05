package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dto.CommandDto;
import dto.CommandEnum;
import dto.ResponseDto;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.command.DeleteCommand;
import server.command.GetCommand;
import server.command.SetCommand;

public class Main {
    private static final int PORT = 34522;
    private static final Path dirPath = Path.of(System.getProperty("user.dir") + "/src/server/data/");
    private static final Path dbFilePath = Path.of(System.getProperty("user.dir") + "/src/server/data/db.json");
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock writeLock = lock.writeLock();
    private static final Controller controller = Controller.getInstance();
    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setLenient()
        .create();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final String NO_SUCH_KEY = "No such key";
    private static final Path clientDataPath = Path.of(System.getProperty("user.dir") + "/src/client/data/");
    private static final Gson regularGson = new GsonBuilder()
        .setLenient()
        .create();
    
    public static void main(String[] args) {
        try {
            Files.createDirectories(dirPath);
            Files.deleteIfExists(dbFilePath);
            Files.createFile(dbFilePath);

            Files.createDirectories(clientDataPath);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started!");
            while (true) {
                Socket socket = server.accept();
                CompletableFuture.runAsync(() -> {
                    try (
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                    ) {
                        String receivedMessage = input.readUTF();
                        CommandDto receivedCommand = gson.fromJson(receivedMessage, CommandDto.class);
                        processCommand(receivedCommand, output, server, socket);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }, executorService);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void processCommand(CommandDto receivedCommand, DataOutputStream output, ServerSocket server, Socket socket) {
        try {
            switch (CommandEnum.valueOf(receivedCommand.getType().toUpperCase())) {
                case GET: {
                    JsonObject currentDatabaseJson = readFromDbFile();

                    JsonElement keyListElement = receivedCommand.getKey();
                    List<String> keyList = new ArrayList<>();
                    if (keyListElement.isJsonArray()) {
                        for (JsonElement jsonElement : keyListElement.getAsJsonArray()) {
                            keyList.add(jsonElement.getAsString());
                        }
                    } else {
                        keyList = Collections.singletonList(keyListElement.getAsString());
                    }

                    GetCommand getCommand = new GetCommand(currentDatabaseJson, keyList);
                    controller.executeCommand(getCommand);
                    JsonElement result = getCommand.getResult();
                    if (result.isJsonPrimitive()) {
                        output.writeUTF(regularGson.toJson(ResponseDto.ok(Optional.of(result.getAsString()))));
                    } else {
                        output.writeUTF(
                            ResponseDto.ok(
                                Optional.of(regularGson.toJson(result))
                            ).toString()
                        );
                    }
                    break;
                }
                case SET: {
                    JsonObject currentDatabaseJson = readFromDbFile();
                    JsonElement valueAsJson = null;
                    String value = null;
                    try {
                        valueAsJson = gson.toJsonTree(receivedCommand.getValue(), JsonElement.class);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        value = receivedCommand.getValue().getAsString();
                    }

                    JsonElement keyListElement = receivedCommand.getKey();
                    List<String> keyList = new ArrayList<>();
                    if (keyListElement.isJsonArray()) {
                        for (JsonElement jsonElement : keyListElement.getAsJsonArray()) {
                            keyList.add(jsonElement.getAsString());
                        }
                    } else {
                        keyList = Collections.singletonList(keyListElement.getAsString());
                    }
                    
                    SetCommand setCommand = new SetCommand(currentDatabaseJson, keyList, valueAsJson, value);
                    controller.executeCommand(setCommand);
                    output.writeUTF(regularGson.toJson(ResponseDto.ok(Optional.empty())));
                    writeJsonObjectToFile(setCommand.getResult());
                    break;
                }
                case DELETE: {
                    JsonObject currentDatabaseJson = readFromDbFile();

                    JsonElement keyListElement = receivedCommand.getKey();
                    List<String> keyList = new ArrayList<>();
                    if (keyListElement.isJsonArray()) {
                        for (JsonElement jsonElement : keyListElement.getAsJsonArray()) {
                            keyList.add(jsonElement.getAsString());
                        }
                    } else {
                        keyList = Collections.singletonList(keyListElement.getAsString());
                    }

                    DeleteCommand deleteCommand = new DeleteCommand(currentDatabaseJson, keyList);
                    controller.executeCommand(deleteCommand);
                    output.writeUTF(regularGson.toJson(ResponseDto.ok(Optional.empty())));
                    writeJsonObjectToFile(currentDatabaseJson);
                    break;
                }
                case ROLLBACK:
                    Optional<Command<?>> optionalLastCommand = controller.rollbackLastCommand();
                    if (optionalLastCommand.isEmpty()) {
                        output.writeUTF(regularGson.toJson(ResponseDto.error("No commands have been executed thus far")));
                        break;
                    }
                    // TODO: update response
                    output.writeUTF(regularGson.toJson(ResponseDto.ok(Optional.empty())));
                    Command<?> lastCommand = optionalLastCommand.get();
                    // For garbage collection
                    lastCommand = null;
                case EXIT:
                    output.writeUTF(regularGson.toJson(ResponseDto.ok(Optional.empty())));
                    server.close();
                    socket.close();
                    break;
                default: {
                    output.writeUTF(regularGson.toJson(ResponseDto.error("Invalid command")));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            try {
                output.writeUTF(regularGson.toJson(ResponseDto.error(NO_SUCH_KEY)));
            } catch (IOException ex2) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void writeJsonObjectToFile(JsonObject jsonObject) {
        writeLock.lock();
        try {
            Files.writeString(dbFilePath, gson.toJson(jsonObject));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    private static JsonObject readFromDbFile() throws IOException {
        readLock.lock();
        try {
            String jsonFileContent = Files.readString(dbFilePath);
            return gson.fromJson(jsonFileContent, JsonObject.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            readLock.unlock();
        }
    }
}
