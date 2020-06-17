package server;

import enums.GameState;
import models.Board;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Game {
    private Map<String, Board> boardByUsername = new HashMap<>();
    private String name;
    private Map<String, Boolean> readyStateByUsername = new HashMap<>();
    private GameState gameState = GameState.NOT_READY;
    private Map<String, Boolean> playerTurn = new HashMap<>();

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
        if (getPlayers().size() < 2) {
            boardByUsername.put(username, new Board());
            readyStateByUsername.put(username, false);
            return true;
        }
        return false;
    }

    public String getReadyMessage(String username) {
        readyStateByUsername.put(username, true);
        if (getPlayers().size() == 2
                && readyStateByUsername.values().stream().reduce((left, right) -> left && right).get()) {
            gameState = GameState.PLACING;

            return "Game is ready, you are all set to go! Start placing ships(limit 10 ships)." +
                    " One Aircraft carrier(5 fields), two battleships(4 fields), three submarines(3 fields), four patrol boats(2 fields))\n";
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

        boardsViewPlayerOne.append("\n");
        boardsViewPlayerOne.append(boardByUsername.get(username).getBoardView()).append("\n\n");
        boardsViewPlayerOne.append(enemyBoard.getHiddenBoardView());

        boardsViewPlayerTwo.append("\n");
        boardsViewPlayerTwo.append(enemyBoard.getBoardView()).append("\n\n");
        boardsViewPlayerTwo.append(boardByUsername.get(username).getHiddenBoardView());

        String enemyUsername = boardByUsername.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(username)).findFirst().get().getKey();

        if (enemyBoard.gameOver()) {
            String message = "Game Over. Winner is: " + username + "\n";
            boardViewByUsername.put(username, boardsViewPlayerOne.toString() + message);
            boardViewByUsername.put(enemyUsername, boardsViewPlayerTwo.toString() + message);
            playerTurn.put(username, false);
            playerTurn.put(enemyUsername, false);
        } else {
            boardViewByUsername.put(username, boardsViewPlayerOne.toString());
            boardViewByUsername.put(enemyUsername, boardsViewPlayerTwo.toString());
            switchTurn(username);
        }
        return boardViewByUsername;
    }

    public boolean placeShip(String username, String positions) {
        if (boardByUsername.get(username).isSizeOfPlacedShipsExceedingLimit()) {
            return false;
        }
        String[] positionTokens = positions.toUpperCase().split("-");

        int xCoordinate1 = positionTokens[0].charAt(0) - 'A';
        int yCoordinate1 = Integer.parseInt(positionTokens[0].substring(1)) - 1;

        int xCoordinate2 = positionTokens[1].charAt(0) - 'A';
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
