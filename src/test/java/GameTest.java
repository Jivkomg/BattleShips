import enums.ShipType;
import models.Board;
import models.Ship;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import server.Game;

import java.util.*;

@RunWith(JUnit4.class)
public class GameTest {

    @Test
    public void addUserTest_WhenLessThanTwoPlayers_ShouldReturnTrue() {
        String username = "name";
        Game game = new Game();

        Assert.assertTrue(game.addUserToGame(username));
    }

    @Test
    public void addUserTest_WhenMoreThanTwoPlayers_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        String username3 = "name3";
        Assert.assertFalse(game.addUserToGame(username3));
    }

    @Test
    public void readyTest_WhenBothAreReady_ShouldReturnMessage() {
        String message = "Game is getReadyMessage, you are all set to go! Start placing ships(limit 10 ships)." +
                " One Aircraft carrier(5 fields), two battleships(4 fields), three submarines(3 fields), four patrol boats(2 fields))\n";

        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        game.getReadyStateByUsername().put(username1, true);
        game.getReadyStateByUsername().put(username2, true);

        Assert.assertEquals(message, game.getReadyMessage(username1));
    }

    @Test
    public void readyTest_WhenBothAreNotReady_ShouldReturnEmptyString() {
        String username1 = "name1";
        String username2 = "name2";

        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        game.getReadyStateByUsername().put(username1, false);
        game.getReadyStateByUsername().put(username2, false);

        Assert.assertEquals("", game.getReadyMessage(username1));
    }

    @Test
    public void readyTest_WhenOnlyOnePlayer_ShouldReturnEmptyString() {
        String username1 = "name1";

        Game game = new Game();
        game.addUserToGame(username1);

        Assert.assertEquals("", game.getReadyMessage(username1));
    }

    @Test
    public void playTest() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        Map<String, String> boardViewByUsername = new HashMap<>();

        boardViewByUsername.put(username1, "\n" +
                "       YOUR BOARD\n" +
                "   1 2 3 4 5 6 7 8 9 10" +
                "\n" +
                "   _ _ _ _ _ _ _ _ _ _\n" +
                "A |_|_|_|_|_|_|_|_|_|_|\n" +
                "B |_|_|_|_|_|_|_|_|_|_|\n" +
                "C |_|_|_|_|_|_|_|_|_|_|\n" +
                "D |_|_|_|_|_|_|_|_|_|_|\n" +
                "E |_|_|_|_|_|_|_|_|_|_|\n" +
                "F |_|_|_|_|_|_|_|_|_|_|\n" +
                "G |_|_|_|_|_|_|_|_|_|_|\n" +
                "H |_|_|_|_|_|_|_|_|_|_|\n" +
                "I |_|_|_|_|_|_|_|_|_|_|\n" +
                "J |_|_|_|_|_|_|_|_|_|_|\n" +
                "\n" +
                "\n" +
                "       ENEMY BOARD\n" +
                "   1 2 3 4 5 6 7 8 9 10" +
                "\n" +
                "   _ _ _ _ _ _ _ _ _ _\n" +
                "A |O|_|_|_|_|_|_|_|_|_|\n" +
                "B |_|_|_|_|_|_|_|_|_|_|\n" +
                "C |_|_|_|_|_|_|_|_|_|_|\n" +
                "D |_|_|_|_|_|_|_|_|_|_|\n" +
                "E |_|_|_|_|_|_|_|_|_|_|\n" +
                "F |_|_|_|_|_|_|_|_|_|_|\n" +
                "G |_|_|_|_|_|_|_|_|_|_|\n" +
                "H |_|_|_|_|_|_|_|_|_|_|\n" +
                "I |_|_|_|_|_|_|_|_|_|_|\n" +
                "J |_|_|_|_|_|_|_|_|_|_|\n" +
                "Game Over. Winner is: name1\n");
        boardViewByUsername.put(username2, "\n" +
                "       YOUR BOARD\n" +
                "   1 2 3 4 5 6 7 8 9 10" +
                "\n" +
                "   _ _ _ _ _ _ _ _ _ _\n" +
                "A |O|_|_|_|_|_|_|_|_|_|\n" +
                "B |_|_|_|_|_|_|_|_|_|_|\n" +
                "C |_|_|_|_|_|_|_|_|_|_|\n" +
                "D |_|_|_|_|_|_|_|_|_|_|\n" +
                "E |_|_|_|_|_|_|_|_|_|_|\n" +
                "F |_|_|_|_|_|_|_|_|_|_|\n" +
                "G |_|_|_|_|_|_|_|_|_|_|\n" +
                "H |_|_|_|_|_|_|_|_|_|_|\n" +
                "I |_|_|_|_|_|_|_|_|_|_|\n" +
                "J |_|_|_|_|_|_|_|_|_|_|\n" +
                "\n" +
                "\n" +
                "       ENEMY BOARD\n" +
                "   1 2 3 4 5 6 7 8 9 10" +
                "\n" +
                "   _ _ _ _ _ _ _ _ _ _\n" +
                "A |_|_|_|_|_|_|_|_|_|_|\n" +
                "B |_|_|_|_|_|_|_|_|_|_|\n" +
                "C |_|_|_|_|_|_|_|_|_|_|\n" +
                "D |_|_|_|_|_|_|_|_|_|_|\n" +
                "E |_|_|_|_|_|_|_|_|_|_|\n" +
                "F |_|_|_|_|_|_|_|_|_|_|\n" +
                "G |_|_|_|_|_|_|_|_|_|_|\n" +
                "H |_|_|_|_|_|_|_|_|_|_|\n" +
                "I |_|_|_|_|_|_|_|_|_|_|\n" +
                "J |_|_|_|_|_|_|_|_|_|_|\n" +
                "Game Over. Winner is: name1\n");

        Assert.assertEquals(game.getBoardViewByPerformingMove(username1, "A1"), boardViewByUsername);
    }

    @Test
    public void placeShipsTest_WhenBoardIsFull_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        Board board1 = new Board();
        List<Ship> ships = Arrays.asList(new Ship(ShipType.AIRCRAFT_CARRIER),
                new Ship(ShipType.PATROL_BOAT),
                new Ship(ShipType.PATROL_BOAT),
                new Ship(ShipType.PATROL_BOAT),
                new Ship(ShipType.PATROL_BOAT),
                new Ship(ShipType.BATTLESHIP),
                new Ship(ShipType.BATTLESHIP),
                new Ship(ShipType.SUBMARINE),
                new Ship(ShipType.SUBMARINE),
                new Ship(ShipType.SUBMARINE));

        board1.setShips(ships);
        game.getBoardByUsername().put(username1, board1);
        Assert.assertFalse(game.placeShip(username1, "A1-A3"));
    }

    @Test
    public void placeShipsTest_WhenShipOverlapXAxis_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);


        game.placeShip(username1, "A1-A2");
        Assert.assertFalse(game.placeShip(username1, "A1-A3"));
    }

    @Test
    public void placeShipsTest_WhenShipOverlapYAxis_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);


        game.placeShip(username1, "A1-B1");
        Assert.assertFalse(game.placeShip(username1, "A1-B3"));
    }

    @Test
    public void placeShipsTest_WhenShipDiagonal_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        Assert.assertFalse(game.placeShip(username1, "A1-B2"));
    }

    @Test
    public void placeShipsTest_WhenShipNull_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);


        Assert.assertFalse(game.placeShip(username1, "A1-A1"));
    }

    @Test
    public void placeShipsTest_WhenInvalidFirstField_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        Assert.assertFalse(game.placeShip(username1, "A0-B3"));
    }

    @Test
    public void placeShipsTest_WhenInvalidSecondField_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        Assert.assertFalse(game.placeShip(username1, "C2-A0"));
    }

    @Test
    public void placeShipsTest_WhenShipOverlap_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        game.placeShip(username1, "A1-A2");
        Assert.assertFalse(game.placeShip(username1, "A2-A5"));
    }

    @Test
    public void isPlayerTurn_WhenIsTurn_ShouldReturnTrue() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        game.getPlayerTurn().put(username1, true);
        Assert.assertTrue(game.isPlayerTurn(username1));
    }

    @Test
    public void isPlayerTurn_WhenIsNotTurn_ShouldReturnFalse() {
        String username1 = "name1";
        String username2 = "name2";
        Game game = new Game();
        game.addUserToGame(username1);
        game.addUserToGame(username2);

        game.getPlayerTurn().put(username1, false);
        Assert.assertFalse(game.isPlayerTurn(username1));
    }


}