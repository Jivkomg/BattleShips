package input.commands;

import interfaces.Command;
import server.Game;

import java.nio.channels.SocketChannel;
import java.util.Map;

import static enums.Message.*;
import static enums.Message.HAVE_GAME;

public class CreateGameCommand implements Command {
    private static final String CREATED_GAME = "Created game ";
    private static final String HALF_GAME_ROOM = ", PLAYERS: 1/2\n";
    private SocketChannel socketChannel;
    private Map<String, Game> games;
    private Map<SocketChannel, Game> gameByChannels;
    private int tokenIndex;
    private String[] tokens;
    private String username;


    public CreateGameCommand(SocketChannel socketChannel, Map<String, Game> games,
                             Map<SocketChannel, Game> gameByChannels, int tokenIndex, String[] tokens, String username) {
        this.socketChannel = socketChannel;
        this.games = games;
        this.gameByChannels = gameByChannels;
        this.tokenIndex = tokenIndex;
        this.tokens = tokens;
        this.username = username;
    }

    @Override
    public StringBuilder execute() {
        return createGame(socketChannel, username, tokenIndex, tokens);
    }

    private StringBuilder createGame(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            message = new StringBuilder(HAVE_GAME.getValue()).append(game.getName()).append("\n");
        } else {
            if (tokens.length < 2) {
                message = new StringBuilder(NO_NAME.getValue());
            } else {
                String gameName = tokens[tokenIndex++];
                message = games.containsKey(gameName) ? new StringBuilder(DUPLICATING_NAME.getValue()) : successfulCreation(socketChannel, username, gameName);
            }
        }
        return message;
    }

    private StringBuilder successfulCreation(SocketChannel socketChannel, String username, String gameName) {
        StringBuilder message;
        Game newGame = new Game();
        newGame.addUserToGame(username);
        newGame.setName(gameName);
        games.put(gameName, newGame);
        gameByChannels.put(socketChannel, newGame);
        message = new StringBuilder(CREATED_GAME).append(gameName).append(HALF_GAME_ROOM).append(ENTER_START.getValue());
        return message;
    }
}
