import enums.GameState;
import input.commands.*;
import interfaces.Command;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import server.Game;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static enums.Message.*;

@RunWith(JUnit4.class)
public class ServerTest {
    @Mock
    SocketChannel socketChannel;

    private Map<SocketChannel, Game> gameByChannels;
    private Map<String, SocketChannel> channelsByUsername;
    private Map<SocketChannel, String> usernamesByChannels;
    private Map<String, Game> games;

    private Command command;

    @Before
    public void init() {
        gameByChannels = new HashMap<>();
        channelsByUsername = new HashMap<>();
        usernamesByChannels = new HashMap<>();
        games = new HashMap<>();
    }

    @Test
    public void createGameTest_WhenCreated_ShouldReturnSuccessfulMessage() {
        String username = "name";

        command = new CreateGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"create-game", "ala"}, username);

        Assert.assertEquals("Created game " + "ala, PLAYERS: 1/2\n" + ENTER_START.getValue(),
                command.execute().toString());

    }

    @Test
    public void createUsernameTest_ShouldReturnSuccessfulMessage() {
        String username = "name";
        channelsByUsername.put(username, socketChannel);

        command = new UsernameCommand(socketChannel, usernamesByChannels, channelsByUsername, 1, new String[]{"create-game", "ala"});

        Assert.assertEquals(USERNAME_CREATED.getValue(), command.execute().toString());
    }

    @Test
    public void listGamesTest_WhenThereAreGames_ShouldReturnAvailableGames() {
        Game game = new Game();
        game.addUserToGame("1");
        game.addUserToGame("2");
        games.put("a", game);

        command = new ListGamesCommand(games);

        Assert.assertEquals(AVAILABLE_GAMES.getValue() + "a(2/2)\n", command.execute().toString());
    }

    @Test
    public void listGamesTest_WhenThereAreNoGames_ShouldReturnNoGame() {
        command = new ListGamesCommand(games);

        Assert.assertEquals(NO_GAME.getValue(), command.execute().toString());
    }

    @Test
    public void joinGamesTest_WhenYouHaveGame_ShouldReturnHaveGame() {
        gameByChannels.put(socketChannel, new Game());

        command = new JoinGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"join-game", "game"}, "name");

        Assert.assertEquals(HAVE_GAME.getValue() + "null\n", command.execute().toString());
    }

    @Test
    public void joinGamesTest_WhenYouDontHaveGame_ShouldSuccessfulMessage() {
        Game game = new Game();
        game.setName("game");
        games.put("game", game);
        String message = JOINED_GAME.getValue() + "game" + "\n" +
                "PLAYERS: " + "1/2" + "\n" +
                ENTER_START.getValue();

        command = new JoinGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"join-game", "game"}, "name");

        Assert.assertEquals(message, command.execute().toString());
    }

    @Test
    public void joinGamesTest_WhenRandomGame_ShouldSuccessfulMessage() {
        Game game = new Game();
        game.setName("game");
        game.addUserToGame("1");
        games.put("game", game);
        String message = JOINED_GAME.getValue() + "game" + "\n" +
                "PLAYERS: " + "2/2" + "\n" +
                ENTER_START.getValue();

        command = new JoinGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"join-game"}, "name");

        Assert.assertEquals(message, command.execute().toString());
    }

    @Test
    public void placeShipTest_WhenNoGame_ShouldReturnNotJoinedMessage() {
        command = new PlaceCommand(socketChannel, gameByChannels, 1, new String[]{"place", "A1-A3"}, "name");

        Assert.assertEquals(NOT_JOINED.getValue(),
                command.execute().toString());
    }

    @Test
    public void placeShipTest_WhenNotPlacing_ShouldReturnNotReadyMessage() {
        Game game = new Game();
        game.setGameState(GameState.NOT_READY);
        gameByChannels.put(socketChannel, game);

        command = new PlaceCommand(socketChannel, gameByChannels, 1, new String[]{"place", "A1-A3"}, "name");

        Assert.assertEquals(NOT_READY.getValue(),
                command.execute().toString());
    }

    @Test
    public void hitShipTest_WhenGameIsNull_ShouldReturnNotJoinedMessage() {
        command = new HitCommand(socketChannel, gameByChannels, channelsByUsername, 0, new String[]{"hit", "A1"}, "name");

        Assert.assertEquals(NOT_JOINED.getValue(), command.execute().toString());
    }

    @Test
    public void hitShipTest_WhenInvalidPosition_ShouldReturnInvalidMessage() {
        Game game = new Game();
        gameByChannels.put(socketChannel, game);

        command = new HitCommand(socketChannel, gameByChannels, channelsByUsername, 1, new String[]{"hit", "a"}, "name");

        Assert.assertEquals(INVALID_POSITION.getValue(), command.execute().toString());
    }

    @Test
    public void hitShipTest_WhenGameIsNotPlaying_ShouldReturnNotReady() {
        Game game = new Game();
        game.setGameState(GameState.NOT_READY);
        gameByChannels.put(socketChannel, game);

        command = new HitCommand(socketChannel, gameByChannels, channelsByUsername, 1, new String[]{"hit", "A1"}, "name");

        Assert.assertEquals(NOT_READY.getValue(), command.execute().toString());
    }
}
