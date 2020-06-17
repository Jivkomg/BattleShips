package input.commands;

import interfaces.Command;
import server.Game;

import java.util.Map;

import static enums.Message.AVAILABLE_GAMES;
import static enums.Message.NO_GAME;

public class ListGamesCommand implements Command {
    private static final String MAX_NUMBER_OF_PLAYERS = "/2)";
    private static final String DELIMITER_NEW_LINE = "\n";
    private static final String DELIMITER = "(";

    private Map<String, Game> games;

    public ListGamesCommand(Map<String, Game> games) {
        this.games = games;
    }

    @Override
    public StringBuilder execute() {
        return listGames();
    }

    private StringBuilder listGames() {
        StringBuilder message;
        message = new StringBuilder();
        if (games.isEmpty()) {
            message.append(NO_GAME.getValue());
        } else {
            message.append(AVAILABLE_GAMES.getValue());
            games.keySet().forEach(gameName -> message.append(gameName).append(DELIMITER).append(games.get(gameName).getPlayers().size()).append(MAX_NUMBER_OF_PLAYERS));
            message.append(DELIMITER_NEW_LINE);
        }
        return message;
    }
}
