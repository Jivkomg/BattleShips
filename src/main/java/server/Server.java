package server;

import enums.Message;
import input.CommandDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server {
    private static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private static final int HOST_PORT = 8080;
    private static final String HOST_NAME = "localhost";
    private static final int MESSAGE_TOKENS_COUNT = 3;
    private static final String MESSAGE_ELEMENTS_REGEX = " ";
    private static final int BUFFER_CAPACITY = 1000;

    private static final String DISCONNECT_COMMAND = "disconnect";
    private static final int OFFSET = 0;

    private Map<SocketChannel, String> usernamesByChannels = new HashMap<>();
    private Map<String, SocketChannel> channelsByUsernames = new HashMap<>();
    private Map<String, Game> games = new HashMap<>();
    private Map<SocketChannel, Game> gameByChannels = new HashMap<>();

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            LOGGER.info("Starting configuring of server...");
            serverSocketChannel.bind(new InetSocketAddress(HOST_NAME, HOST_PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                try {

                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isAcceptable()) {
                            registerChannel(selector, serverSocketChannel);
                        } else if (key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            readFromClient(socketChannel);
                        }

                        updateChannels();

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    System.err.println(Message.SELECTOR_UNABLE_TO_SELECT_KEYS.getValue());
                    LOGGER.error("Error during selecting keys", e);
                }
            }

        } catch (IOException e) {
            System.err.println(Message.UNABLE_TO_INITIALIZE_APPLICATION.getValue());
            LOGGER.error("Error initializing application", e);
        }

    }

    private void registerChannel(Selector selector, ServerSocketChannel serverSocketChannel) {
        try {
            SocketChannel clientSocketChannel = serverSocketChannel.accept();
            clientSocketChannel.configureBlocking(false);
            clientSocketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            String username = UUID.randomUUID().toString();
            usernamesByChannels.put(clientSocketChannel, username);
            channelsByUsernames.put(username, clientSocketChannel);
        } catch (IOException e) {
            System.err.println(Message.ERROR_HAS_OCCURRED.getValue());
            e.printStackTrace();
        }
    }

    private void readFromClient(SocketChannel socketChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
            StringBuilder clientMessageBuilder = new StringBuilder();
            while (socketChannel.read(buffer) > 0) {
                buffer.flip();
                clientMessageBuilder.append(new String(buffer.array(), OFFSET, buffer.limit()));
                buffer.clear();
            }
            String clientMessage = clientMessageBuilder.toString();

            String username = usernamesByChannels.get(socketChannel);

            int tokenIndex = 0;
            String[] tokens = clientMessage.split(MESSAGE_ELEMENTS_REGEX, MESSAGE_TOKENS_COUNT);

            String command = tokens[tokenIndex++];
            StringBuilder message;


            if (DISCONNECT_COMMAND.equals(command)) {
                channelsByUsernames.remove(usernamesByChannels.remove(socketChannel));

                ByteBuffer writeBuffer = ByteBuffer.wrap(DISCONNECT_COMMAND.getBytes());
                socketChannel.write(writeBuffer);
                socketChannel.close();
                return;
            }

            CommandDistributor commandDistributor = new CommandDistributor();

            if (!commandDistributor.getAllCommands().contains(command)) {
                message = new StringBuilder(Message.WRONG_COMMAND.getValue());
            } else {
                message = commandDistributor.executeCommand(command, socketChannel, gameByChannels, games, usernamesByChannels, channelsByUsernames, tokenIndex, tokens, username);
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(message.toString().getBytes());
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            System.err.println(Message.ERROR_HAS_OCCURRED.getValue());
            e.printStackTrace();
        }
    }

    private void updateChannels() {
        usernamesByChannels.keySet().stream()
                .filter(channel -> !channel.isConnected())
                .forEach(channel -> channelsByUsernames.remove(usernamesByChannels.remove(channel)));
    }
}
