package server;

import enums.GameState;
import enums.Message;
import models.Board;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Game {
    private static final String NEW_LINE_DELIMITER = "\n";
    private static final String REGEX = "-";
    private static final int MAX_NUMBER_OF_PLAYERS = 2;
    private Map<String, Board> boardByUsername = new HashMap<>();
    private String name;
    private Map<String, Boolean> readyStateByUsername = new HashMap<>();
    private GameState gameState = GameState.NOT_READY;
    private Map<String, Boolean> playerTurn = new HashMap<>();
    private static final char FIRST_BOARD_INDEX = 'A';

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getReadyStateByUsername() {
        return readyStateByUsername;
    }

    public Map<String, Board> getBoardByUsername() {
        return boardByUsername;
    }

    public Set<String> getPlayers() {
        return boardByUsername.keySet();
    }

    public Map<String, Boolean> getPlayerTurn() {
        return playerTurn;
    }

    public boolean addUserToGame(String username) {
        if (getPlayers().size() < MAX_NUMBER_OF_PLAYERS) {
            boardByUsername.put(username, new Board());
            readyStateByUsername.put(username, false);
            return true;
        }
        return false;
    }

    public String getReadyMessage(String username) {
        readyStateByUsername.put(username, true);
        if (getPlayers().size() == MAX_NUMBER_OF_PLAYERS
                && readyStateByUsername.values().stream().reduce((left, right) -> left && right).get()) {
            gameState = GameState.PLACING;

            return Message.READY_GAME_MESSAGE.getValue();
        }
        return "";
    }

    public Map<String, String> getBoardViewByPerformingMove(String username, String move) {
        Board enemyBoard = boardByUsername.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(username)).findFirst().get().getValue();

        enemyBoard.hitEnemyBoard(move);

        Map<String, String> boardViewByUsername = new HashMap<>();

        StringBuilder boardsViewPlayerOne = new StringBuilder();
        StringBuilder boardsViewPlayerTwo = new StringBuilder();

        getInitialBoardsView(username, enemyBoard, boardsViewPlayerOne, boardsViewPlayerTwo);

        String enemyUsername = boardByUsername.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(username)).findFirst().get().getKey();

        if (enemyBoard.isGameOver()) {
            getGameOverView(username, boardViewByUsername, boardsViewPlayerOne, boardsViewPlayerTwo, enemyUsername);
        } else {
            boardViewByUsername.put(username, boardsViewPlayerOne.toString());
            boardViewByUsername.put(enemyUsername, boardsViewPlayerTwo.toString());
            switchTurn(username);
        }
        return boardViewByUsername;
    }

    private void getGameOverView(String username, Map<String, String> boardViewByUsername, StringBuilder boardsViewPlayerOne, StringBuilder boardsViewPlayerTwo, String enemyUsername) {
        String message = Message.GAME_OVER_MESSAGE.getValue() + username + NEW_LINE_DELIMITER;
        boardViewByUsername.put(username, boardsViewPlayerOne.toString() + message);
        boardViewByUsername.put(enemyUsername, boardsViewPlayerTwo.toString() + message);
        playerTurn.put(username, false);
        playerTurn.put(enemyUsername, false);
    }

    private void getInitialBoardsView(String username, Board enemyBoard, StringBuilder boardsViewPlayerOne, StringBuilder boardsViewPlayerTwo) {
        boardsViewPlayerOne.append(NEW_LINE_DELIMITER);
        boardsViewPlayerOne.append(boardByUsername.get(username).getBoardView()).append(NEW_LINE_DELIMITER).append(NEW_LINE_DELIMITER);
        boardsViewPlayerOne.append(enemyBoard.getHiddenBoardView());

        boardsViewPlayerTwo.append(NEW_LINE_DELIMITER);
        boardsViewPlayerTwo.append(enemyBoard.getBoardView()).append(NEW_LINE_DELIMITER).append(NEW_LINE_DELIMITER);
        boardsViewPlayerTwo.append(boardByUsername.get(username).getHiddenBoardView());
    }

    public boolean placeShip(String username, String positions) {
        if (boardByUsername.get(username).isSizeOfPlacedShipsExceedingLimit()) {
            return false;
        }
        String[] positionTokens = positions.toUpperCase().split(REGEX);

        int xCoordinate1 = positionTokens[0].charAt(0) - FIRST_BOARD_INDEX;
        int yCoordinate1 = Integer.parseInt(positionTokens[0].substring(1)) - 1;

        int xCoordinate2 = positionTokens[1].charAt(0) - FIRST_BOARD_INDEX;
        int yCoordinate2 = Integer.parseInt(positionTokens[1].substring(1)) - 1;

        if (boardByUsername.get(username).placeShip(xCoordinate1, yCoordinate1, xCoordinate2, yCoordinate2)) {
            if (boardByUsername.entrySet().stream().allMatch(entry -> entry.getValue().isSizeOfPlacedShipsExceedingLimit())) {
                gameState = GameState.PLAYING;
                playerTurn.put(username, true);
            }
            return true;
        }
        return false;
    }

    private void switchTurn(String username) {
        playerTurn.put(username, false);
        String enemyUsername = boardByUsername.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(username)).findFirst().get().getKey();
        playerTurn.put(enemyUsername, true);
    }

    public boolean isPlayerTurn(String username) {
        return playerTurn.get(username);
    }
}
