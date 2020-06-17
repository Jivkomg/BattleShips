package input.commands;

import enums.GameState;
import interfaces.Command;
import server.Game;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static enums.Message.*;

public class StartCommand implements Command {
    private SocketChannel socketChannel;
    private Map<SocketChannel, Game> gameByChannels;
    private Map<String, SocketChannel> channelsByUsernames;
    private String username;

    public StartCommand(SocketChannel socketChannel, Map<SocketChannel, Game> gameByChannels, Map<String, SocketChannel> channelsByUsernames, String username) {
        this.socketChannel = socketChannel;
        this.gameByChannels = gameByChannels;
        this.channelsByUsernames = channelsByUsernames;
        this.username = username;
    }

    @Override
    public StringBuilder execute() {
        try {
            return startGame(socketChannel, username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringBuilder();
    }

    private StringBuilder startGame(SocketChannel socketChannel, String username) throws IOException {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            message = new StringBuilder(NOT_JOINED.getValue());
        } else {
            message = GameState.NOT_READY.equals(game.getGameState()) ? startGameWhenTateIsNotReady(username, game) : new StringBuilder(HAVE_GAME.getValue());
        }
        return message;
    }

    private StringBuilder startGameWhenTateIsNotReady(String username, Game game) throws IOException {
        StringBuilder message;
        message = new StringBuilder(READY.getValue());
        String messageToBegin = game.getReadyMessage(username);
        message.append(game.getReadyMessage(username));

        if (game.getPlayers().size() > 1) {
            String otherUsername = game.getPlayers()
                    .stream().filter(n -> !username.equals(n)).findFirst().get();
            ByteBuffer byteBuffer = ByteBuffer.wrap(messageToBegin.getBytes());
            channelsByUsernames.get(otherUsername).write(byteBuffer);
        }
        return message;
    }
}
