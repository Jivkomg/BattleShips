package input.commands;

import com.google.gson.Gson;
import interfaces.Command;
import server.Game;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static enums.Message.NO_GAME;
import static enums.Message.SAVE_GAME;

public class SaveGameCommand implements Command {
    private static final String FILE_PATH = "src\\main\\resources\\games\\";
    private static final String FILE_FORMAT = ".json";
    private SocketChannel socketChannel;
    private Map<SocketChannel, Game> gameByChannels;

    public SaveGameCommand(SocketChannel socketChannel, Map<SocketChannel, Game> gameByChannels) {
        this.socketChannel = socketChannel;
        this.gameByChannels = gameByChannels;
    }

    @Override
    public StringBuilder execute() {
        return saveGame(socketChannel);
    }

    private StringBuilder saveGame(SocketChannel socketChannel) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            message = new StringBuilder(NO_GAME.getValue());
        } else {
            try (FileWriter fileWriter = new FileWriter(FILE_PATH + game.getName() + FILE_FORMAT)) {
                Gson gson = new Gson();
                gson.toJson(game, fileWriter);
                message = new StringBuilder(SAVE_GAME.getValue());
            } catch (IOException e) {
                message = new StringBuilder(e.getMessage());
            }
        }
        return message;
    }
}
