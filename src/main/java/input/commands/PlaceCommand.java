package input.commands;

import enums.GameState;
import interfaces.Command;
import server.Game;

import java.nio.channels.SocketChannel;
import java.util.Map;

import static enums.Message.*;

public class PlaceCommand implements Command {
    private SocketChannel socketChannel;
    private Map<SocketChannel, Game> gameByChannels;
    private int tokenIndex;
    private String[] tokens;
    private String username;

    public PlaceCommand(SocketChannel socketChannel, Map<SocketChannel, Game> gameByChannels, int tokenIndex, String[] tokens, String username) {
        this.socketChannel = socketChannel;
        this.gameByChannels = gameByChannels;
        this.tokenIndex = tokenIndex;
        this.tokens = tokens;
        this.username = username;
    }

    @Override
    public StringBuilder execute() {
        return placeShip(socketChannel, username, tokenIndex, tokens);
    }

    private StringBuilder placeShip(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            message = new StringBuilder(NOT_JOINED.getValue());
        } else {
            if (!game.getGameState().equals(GameState.PLACING)) {
                message = new StringBuilder(NOT_READY.getValue());
            } else {
                if (!game.placeShip(username, tokens[tokenIndex++])) {
                    message = new StringBuilder(INVALID_POSITION.getValue());
                } else {
                    message = new StringBuilder(PLACE_SHIP.getValue());
                    if (GameState.PLAYING.equals(game.getGameState())) {
                        message.append(START_HITTING.getValue());
                    }
                }
            }
        }
        return message;
    }
}
