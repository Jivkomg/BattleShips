package input;

import input.commands.*;
import interfaces.Command;
import server.Game;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandDistributor {
    private static final String UNEXPECTED_VALUE = "Unexpected value: ";

    private static final String USERNAME_COMMAND = "username";
    private static final String CREATE_GAME_COMMAND = "create-game";
    private static final String JOIN_GAME_COMMAND = "join-game";
    private static final String LIST_GAMES_COMMAND = "list-games";
    private static final String START_COMMAND = "start";
    private static final String SAVE_GAMES_COMMAND = "save-game";
    private static final String LOAD_GAME_COMMAND = "load-game";
    private static final String DELETE_GAME_COMMAND = "delete-game";
    private static final String HIT_COMMAND = "hit";
    private static final String PLACE_COMMAND = "place";
    private static final String EXIT_GAME_COMMAND = "exit";

    public CommandDistributor() {
    }

    public StringBuilder executeCommand(String inputCommand, SocketChannel socketChannel, Map<SocketChannel, Game> gameByChannels,
                                        Map<String, Game> games, Map<SocketChannel, String> usernamesByChannels,
                                        Map<String, SocketChannel> channelsByUsernames, int tokenIndex, String[] tokens, String username) {
        Command command;
        switch (inputCommand) {
            case USERNAME_COMMAND:
                command = new UsernameCommand(socketChannel, usernamesByChannels, channelsByUsernames, tokenIndex, tokens);
                break;
            case CREATE_GAME_COMMAND:
                command = new CreateGameCommand(socketChannel, games, gameByChannels, tokenIndex, tokens, username);
                break;
            case JOIN_GAME_COMMAND:
                command = new JoinGameCommand(socketChannel, games, gameByChannels, tokenIndex, tokens, username);
                break;
            case LIST_GAMES_COMMAND:
                command = new ListGamesCommand(games);
                break;
            case PLACE_COMMAND:
                command = new PlaceCommand(socketChannel, gameByChannels, tokenIndex, tokens, username);
                break;
            case HIT_COMMAND:
                command = new HitCommand(socketChannel, gameByChannels, channelsByUsernames, tokenIndex, tokens, username);
                break;
            case START_COMMAND:
                command = new StartCommand(socketChannel, gameByChannels, channelsByUsernames, username);
                break;
            case SAVE_GAMES_COMMAND:
                command = new SaveGameCommand(socketChannel, gameByChannels);
                break;
            case LOAD_GAME_COMMAND:
                command = new LoadGameCommand(socketChannel, games, gameByChannels, channelsByUsernames, tokenIndex, tokens, username);
                break;
            case DELETE_GAME_COMMAND:
                command = new DeleteGameCommand(tokenIndex, tokens);
                break;
            case EXIT_GAME_COMMAND:
                command = new ExitGameCommand(gameByChannels, socketChannel, channelsByUsernames, username);
                break;
            default:
                throw new IllegalStateException(UNEXPECTED_VALUE + inputCommand);
        }

        StringBuilder messageForTheUser = command.execute();
        return messageForTheUser;
    }

    public List<String> getAllCommands(){
        return Arrays.asList(USERNAME_COMMAND, CREATE_GAME_COMMAND, JOIN_GAME_COMMAND,
                LIST_GAMES_COMMAND, PLACE_COMMAND, HIT_COMMAND, START_COMMAND, SAVE_GAMES_COMMAND,
                LOAD_GAME_COMMAND, DELETE_GAME_COMMAND, EXIT_GAME_COMMAND);
    }
}
