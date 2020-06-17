package input.commands;

import com.google.gson.Gson;
import enums.GameState;
import interfaces.Command;
import server.Game;

import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static enums.Message.*;

public class LoadGameCommand implements Command {
    private static final String FILE_PATH = "src\\main\\resources\\games\\";
    private static final String FILE_FORMAT = ".json";
    private SocketChannel socketChannel;
    private Map<String, Game> games;
    private Map<SocketChannel, Game> gameByChannels;
    private Map<String, SocketChannel> channelsByUsernames;
    private int tokenIndex;
    private String[] tokens;
    private String username;

    public LoadGameCommand(SocketChannel socketChannel, Map<String, Game> games, Map<SocketChannel, Game> gameByChannels, Map<String, SocketChannel> channelsByUsernames, int tokenIndex, String[] tokens, String username) {
        this.socketChannel = socketChannel;
        this.games = games;
        this.gameByChannels = gameByChannels;
        this.channelsByUsernames = channelsByUsernames;
        this.tokenIndex = tokenIndex;
        this.tokens = tokens;
        this.username = username;
    }

    @Override
    public StringBuilder execute() {
        return loadGame(socketChannel, username, tokenIndex, tokens);
    }

    private StringBuilder loadGame(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            message = new StringBuilder(NO_GAME.getValue());
        } else {
            if (game.getGameState() != GameState.PLACING) {
                message = new StringBuilder(CANNOT_LOAD.getValue());
            } else {
                String loadedGameName = tokens[tokenIndex++];
                try (FileReader fileReader = new FileReader(FILE_PATH + loadedGameName + FILE_FORMAT)) {
                    Gson gson = new Gson();
                    Game loadedGame = gson.fromJson(fileReader, Game.class);
                    String otherUsername = game.getPlayers()
                            .stream().filter(n -> !username.equals(n)).findFirst().get();

                    message = loadedGame.getPlayers().contains(username)
                            && loadedGame.getPlayers().contains(otherUsername) ? successfulLoadingOfGame(socketChannel, loadedGameName, loadedGame, otherUsername) : new StringBuilder(CANNOT_LOAD.getValue());
                } catch (IOException e) {
                    message = new StringBuilder(e.getMessage());
                }

            }
        }
        return message;
    }

    private StringBuilder successfulLoadingOfGame(SocketChannel socketChannel, String loadedGameName, Game loadedGame, String otherUsername) throws IOException {
        StringBuilder message;
        gameByChannels.put(socketChannel, loadedGame);
        gameByChannels.put(channelsByUsernames.get(otherUsername), loadedGame);

        games.put(loadedGameName, loadedGame);

        ByteBuffer byteBuffer = ByteBuffer.wrap(LOAD_GAME.getValue().getBytes());
        channelsByUsernames.get(otherUsername).write(byteBuffer);

        message = new StringBuilder(LOAD_GAME.getValue());
        return message;
    }
}
