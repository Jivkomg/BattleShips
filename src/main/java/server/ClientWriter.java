package server;

import enums.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static server.Client.DEFAULT_BUFFER_CAPACITY;

public class ClientWriter implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(ClientWriter.class);

    private static final String DISCONNECT_COMMAND = "disconnect";
    private static final int OFFSET = 0;

    private SocketChannel socketChannel;
    private ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_CAPACITY);

    ClientWriter(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        while (true) {
            try {
                LOGGER.info("Starting running of the client writer...");
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
                String serverMessage = new String(buffer.array(), OFFSET, buffer.limit());
                if (DISCONNECT_COMMAND.equalsIgnoreCase(serverMessage)) {
                    LOGGER.info("User has disconnected");
                    break;
                }
                System.out.print(serverMessage);
            } catch (IOException e) {
                System.err.println(Message.READING_EXCEPTION_HAS_OCCURRED.getValue());
                LOGGER.error("Error during reading from the server. Reason: ", e);
            }
        }

    }
}
