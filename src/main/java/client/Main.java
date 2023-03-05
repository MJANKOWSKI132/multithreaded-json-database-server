package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import dto.CommandDto;
import dto.CommandEnum;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    private static final int PORT = 34522;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final Path clientDataPath = Path.of(System.getProperty("user.dir") + "/src/client/data/");

    @Parameter(names = {"-t"})
    private String command;
    @Parameter(names = {"-k"})
    private String key;
    @Parameter(names = {"-v"})
    private String value;
    @Parameter(names = {"-in"})
    private String fileName;

    public static void main(String[] args) {
        Main main = new Main();
        JCommander.newBuilder()
            .addObject(main)
            .build()
            .parse(args);
        main.run();
    }

    private void run() {
        try (
            Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS),PORT);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            log.info("Client started!");

            Gson gson = new Gson();

            CommandDto outgoingCommand;
            if (!Objects.isNull(fileName)) {
                String fileContent = Files.readString(Path.of(clientDataPath.toAbsolutePath().toString(), fileName));
                outgoingCommand = gson.fromJson(fileContent, CommandDto.class);
            } else {
                outgoingCommand = CommandDto.builder()
                    .type(Objects.isNull(command) ? CommandEnum.INPUT_FILE.toString().toLowerCase() : CommandEnum.valueOf(command.toUpperCase()).toString().toLowerCase())
                    .key(Objects.isNull(key) ? null : gson.toJsonTree(key))
                    .value(Objects.isNull(value) ? null : gson.toJsonTree(value).getAsJsonPrimitive())
                    .fileName(fileName)
                    .build();
            }

            String outgoingMessage = gson.toJson(outgoingCommand);

            output.writeUTF(outgoingMessage);
            log.info("Sent: {}", outgoingMessage);

            String receivedMessage = input.readUTF();
            log.info("Received: {}", receivedMessage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
