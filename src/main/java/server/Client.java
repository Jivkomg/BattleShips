package server;

import enums.Message;

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

    @Override
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
        return Message.TUTORIAL_MESSAGE.getValue();
    }

}
