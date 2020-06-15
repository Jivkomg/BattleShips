package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static enums.Message.READING_EXCEPTION_HAS_OCCURRED;
import static server.Client.DEFAULT_BUFFER_CAPACITY;

public class ClientWriter implements Runnable {
    private static final String DISCONNECT_COMMAND = "disconnect";

    private SocketChannel socketChannel;
    private ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_CAPACITY);

    public ClientWriter(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        while (true) {
            try {
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
                String serverMessage = new String(buffer.array(), 0, buffer.limit());
                if (DISCONNECT_COMMAND.equalsIgnoreCase(serverMessage)) {
                    break;
                }
                System.out.print(serverMessage);
            } catch (IOException e) {
                System.err.println(READING_EXCEPTION_HAS_OCCURRED.getValue());
                e.printStackTrace();
            }
        }

    }
}
