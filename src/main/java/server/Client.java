package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client extends Thread {
    private static final String DISCONNECTED_FROM_SERVER_MESSAGE = "The user has disconnected from the server";
    private static final String UNABLE_TO_INITIALIZE_CLIENT_MESSAGE = "Unable to initialize client.";
    private static final String WRITING_EXCEPTION_HAS_OCCURRED_MESSAGE = "An error when writing to server has occurred.";

    private static final int HOST_PORT = 8080;
    private static final String HOST_NAME = "localhost";

    private static final String DISCONNECT_COMMAND = "disconnect";

    static final int DEFAULT_BUFFER_CAPACITY = 1000;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in);
             SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(HOST_NAME, HOST_PORT));
            Thread clientWriterThread = new Thread(new ClientWriter(socketChannel));
            clientWriterThread.setDaemon(true);
            clientWriterThread.start();

            System.out.println(getAvailableCommands());

            while (true) {
                String input = scanner.nextLine().trim();
                StringTokenizer tokenizer = new StringTokenizer(input);
                writeToServer(socketChannel, input);

                if (input.isEmpty()) {
                    continue;
                }
                if (tokenizer.nextToken().equalsIgnoreCase(DISCONNECT_COMMAND)) {
                    System.out.println(DISCONNECTED_FROM_SERVER_MESSAGE);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println(UNABLE_TO_INITIALIZE_CLIENT_MESSAGE);
        }
    }

    private void writeToServer(SocketChannel socketChannel, String input) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(input.getBytes());
            socketChannel.write(buffer);
        } catch (IOException e) {
            System.err.println(WRITING_EXCEPTION_HAS_OCCURRED_MESSAGE);
        }
    }

    private String getAvailableCommands() {
        return "Available commands:\n" +
                "\tusername <new-username> //to change your username\n" +
                "\tlist-games //lists all available games\n " +
                "\tplace [<letter><number>}-<letter><number>] // example:  place A1-A5\n" +
                "\thit [<letter><number>] // example: hit D9\n" +
                "\tcreate-game [<game-name>] // creates a new game \n" +
                "\tjoin-game [<game-name>] // if a name is missing you will be connected to a random game\n" +
                "\tsave-game // to save your game\n" +
                "\tload-game [<game-name>] // when you are in ready state with player you can load your previous game\n" +
                "\tdelete-game [<game-name>] // deletes a previous save by name";
    }

}
