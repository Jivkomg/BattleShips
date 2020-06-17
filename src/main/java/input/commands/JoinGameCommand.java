package input.commands;

import interfaces.Command;
import server.Game;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Random;

import static enums.Message.*;

public class JoinGameCommand implements Command {
    private static final String PLAYERS = "PLAYERS: ";
    private static final String MAX_NUMBER_OF_PLAYERS = "/2";
    private static final String DELIMITER = "\n";
    private SocketChannel socketChannel;
    private Map<String, Game> games;
    private Map<SocketChannel, Game> gameByChannels;
    private int tokenIndex;
    private String[] tokens;
    private String username;


    public JoinGameCommand(SocketChannel socketChannel, Map<String, Game> games, Map<SocketChannel, Game> gameByChannels, int tokenIndex, String[] tokens, String username) {
        this.socketChannel = socketChannel;
        this.games = games;
        this.gameByChannels = gameByChannels;
        this.tokenIndex = tokenIndex;
        this.tokens = tokens;
        this.username = username;
    }

    @Override
    public StringBuilder execute() {
        return joinGame(socketChannel, username, tokenIndex, tokens);
    }

    private StringBuilder joinGame(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            message = new StringBuilder(HAVE_GAME.getValue()).append(game.getName()).append(DELIMITER);
        } else {
            Game joinedGame;
            if (tokens.length > 1) {
                String gameName = tokens[tokenIndex++];

                joinedGame = games.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(gameName)).map(Map.Entry::getValue)
                        .findFirst().orElse(null);
            } else {
                joinedGame = !games.isEmpty() ? getRandomGame() : null;
            }
            if (joinedGame == null) {
                message = new StringBuilder(CANNOT_JOIN_GAME.getValue());
            } else {
                message = joinedGame.addUserToGame(username) ? successfulJoinInSpecificGame(socketChannel, joinedGame) : new StringBuilder(FULL_GAME.getValue());
            }
        }
        return message;
    }

    private StringBuilder successfulJoinInSpecificGame(SocketChannel socketChannel, Game joinedGame) {
        StringBuilder message;
        message = new StringBuilder(JOINED_GAME.getValue());
        message.append(joinedGame.getName()).append(DELIMITER);
        message.append(PLAYERS).append(joinedGame.getPlayers()
                .size()).append(MAX_NUMBER_OF_PLAYERS).append(DELIMITER);
        message.append(ENTER_START.getValue());
        gameByChannels.put(socketChannel, joinedGame);
        return message;
    }

    private Game getRandomGame() {
        Random randomIndex = new Random();
        int index = randomIndex.nextInt(games.values().size());
        Game randomGame = (Game) games.values().stream().filter(game -> game.getPlayers().size() == 1).toArray()[index];
        return randomGame;
    }
}
