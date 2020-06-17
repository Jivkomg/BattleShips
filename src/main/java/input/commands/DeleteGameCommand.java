package input.commands;

import interfaces.Command;

import java.io.File;


import static enums.Message.CANNOT_DELETE;
import static enums.Message.DELETE_GAME;

public class DeleteGameCommand implements Command {
    private static final String FILE_PATH = "src\\main\\resources\\games\\";
    private static final String FILE_FORMAT = ".json";
    private int tokenIndex;
    private String[] tokens;

    public DeleteGameCommand(int tokenIndex, String[] tokens) {
        this.tokenIndex = tokenIndex;
        this.tokens = tokens;
    }

    @Override
    public StringBuilder execute() {
        return deleteGame(tokenIndex, tokens);
    }

    private StringBuilder deleteGame(int tokenIndex, String[] tokens) {
        StringBuilder message;
        String gameName = tokens[tokenIndex++];
        File file = new File(FILE_PATH + gameName + FILE_FORMAT);
        if (file.delete()) {
            message = new StringBuilder(DELETE_GAME.getValue());
        } else {
            message = new StringBuilder(CANNOT_DELETE.getValue());
        }
        return message;
    }
}
