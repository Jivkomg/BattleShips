package input.commands;

import enums.GameState;
import interfaces.Command;
import server.Game;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static enums.Message.*;
import static enums.Message.NOT_JOINED;

public class HitCommand implements Command {
    private static final String LAST_TURN = "Last turn: ";
    private static final String DELIMITER = ", ";
    private static final String NEW_LINE_DELIMITER = "\n";
    private SocketChannel socketChannel;
    private Map<SocketChannel, Game> gameByChannels;
    private Map<String, SocketChannel> channelsByUsernames;
    private int tokenIndex;
    private String[] tokens;
    private String username;

    public HitCommand(SocketChannel socketChannel, Map<SocketChannel, Game> gameByChannels, Map<String, SocketChannel> channelsByUsernames, int tokenIndex, String[] tokens, String username) {
        this.socketChannel = socketChannel;
        this.gameByChannels = gameByChannels;
        this.channelsByUsernames = channelsByUsernames;
        this.tokenIndex = tokenIndex;
        this.tokens = tokens;
        this.username = username;
    }

    @Override
    public StringBuilder execute() {
        try {
            return hitShip(socketChannel, username, tokenIndex, tokens);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringBuilder();
    }

    private StringBuilder hitShip(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) throws IOException {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            message = new StringBuilder(NOT_JOINED.getValue());
        } else {
            String position = tokens[tokenIndex++];
            if (checkValidPosition(position)) {
                if (GameState.PLAYING.equals(game.getGameState())) {
                    message = game.isPlayerTurn(username) ? performHitOnEnemyPlayer(username, game, position) : new StringBuilder(OTHER_PLAYER_TURN.getValue());
                } else {
                    message = new StringBuilder(NOT_READY.getValue());
                }
            } else {
                message = new StringBuilder(INVALID_POSITION.getValue());
            }

        }
        return message;
    }

    private StringBuilder performHitOnEnemyPlayer(String username, Game game, String position) throws IOException {
        StringBuilder message;
        Map<String, String> messages = game.getBoardViewByPerformingMove(username, position);
        message = new StringBuilder(messages.get(username));
        String lastTurnMessage = LAST_TURN + username + DELIMITER + position + NEW_LINE_DELIMITER;

        message.append(lastTurnMessage);

        String otherUsername = game.getPlayers()
                .stream().filter(n -> !username.equals(n)).findFirst().get();

        String messageToEnemy = messages.get(otherUsername) + lastTurnMessage;
        ByteBuffer byteBuffer = ByteBuffer.wrap(messageToEnemy.getBytes());
        channelsByUsernames.get(otherUsername).write(byteBuffer);
        return message;
    }

    private boolean checkValidPosition(String position) {
        if (position.length() > 3 || position.length() < 2) {
            return false;
        }

        int yCoordinate1 = Integer.parseInt(position.substring(1)) - 1;
        return Character.isLetter(position.charAt(0)) && yCoordinate1 >= 0 && yCoordinate1 <= 9;
    }
}
