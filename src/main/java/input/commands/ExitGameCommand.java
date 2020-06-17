package input.commands;

import interfaces.Command;
import server.Game;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static enums.Message.*;

public class ExitGameCommand implements Command {
    private static final String FILE_PATH = "src\\main\\resources\\games\\";
    private static final String FILE_FORMAT = ".json";
    private Map<SocketChannel, Game> gameByChannels;
    private SocketChannel socketChannel;
    private Map<String, SocketChannel> channelsByUsernames = new HashMap<>();
    private String username;

    public ExitGameCommand(Map<SocketChannel, Game> gameByChannels, SocketChannel socketChannel, Map<String, SocketChannel> channelsByUsernames, String username) {
        this.gameByChannels = gameByChannels;
        this.socketChannel = socketChannel;
        this.channelsByUsernames = channelsByUsernames;
        this.username = username;
    }

    @Override
    public StringBuilder execute() {
        try {
            return exitGame(socketChannel, username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringBuilder();
    }

    private StringBuilder exitGame(SocketChannel socketChannel, String username) throws IOException {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            message = new StringBuilder(CANNOT_EXIT_GAME.getValue());
        } else {
            message = successfulExitGame(socketChannel, username, game);
        }
        return message;
    }

    private StringBuilder successfulExitGame(SocketChannel socketChannel, String username, Game game) throws IOException {
        StringBuilder message;
        gameByChannels.put(socketChannel, null);

        message = new StringBuilder(EXIT_GAME.getValue()).append(username).append("\n");
        if (game.getPlayers().size() > 1) {
            String otherUsername = game.getPlayers()
                    .stream().filter(n -> !username.equals(n)).findFirst().get();
            game.getPlayers().remove(username);
            game.getPlayers().remove(otherUsername);
            ByteBuffer byteBuffer = ByteBuffer.wrap(message.toString().getBytes());
            channelsByUsernames.get(otherUsername).write(byteBuffer);
        }
        return message;
    }
}
