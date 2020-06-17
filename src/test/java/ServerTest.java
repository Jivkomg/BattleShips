import enums.GameState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.Game;
import server.Server;

import static enums.Message.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;

@RunWith(JUnit4.class)
public class ServerTest {
    @Mock
    SocketChannel socketChannel;

    @Mock(name = "gameByChannels")
    Map<SocketChannel, Game> gameByChannels = new HashMap<>();

    @Mock(name = "channelsByUsername")
    Map<String, SocketChannel> channelsByUsername = new HashMap<>();

    @Mock(name = "usernamesByChannels")
    Map<SocketChannel, String> usernamesByChannels = new HashMap<>();

    @Mock(name = "games")
    Map<String, Game> games = new HashMap<>();

    @InjectMocks
    Server server = new Server();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createGameTest_WhenCreated_ShouldReturnSuccessfulMessage() {
        when(gameByChannels.get(SocketChannel.class)).thenReturn(null);
        String username = "name";
        Assert.assertEquals("Created game " + "ala, PLAYERS: 1/2\n" + ENTER_START.getValue(),
                server.createGame(socketChannel, username, 1, new String[]{"create-game", "ala"}).toString());

    }

    @Test
    public void createUsernameTest_ShouldReturnSuccessfulMessage() {
        String username = "name";
        channelsByUsername.put(username, socketChannel);

        when(usernamesByChannels.get(socketChannel)).thenReturn(username);
        Server server = new Server();
        Assert.assertEquals(USERNAME_CREATED.getValue(), server.createUsername(socketChannel, 1, new String[]{"username", "name"}).toString());
    }

    @Test
    public void listGamesTest_WhenThereAreGames_ShouldReturnAvailableGames() {
        Set<String> gameSet = new HashSet<>();
        gameSet.add("a");
        Game game = new Game();
        game.addUserToGame("1");
        game.addUserToGame("2");
        when(games.isEmpty()).thenReturn(false);
        when(games.keySet()).thenReturn(gameSet);
        when(games.get(anyString())).thenReturn(game);
        Assert.assertEquals(AVAILABLE_GAMES.getValue() + "a(2/2)\n", server.listGames().toString());
    }

    @Test
    public void listGamesTest_WhenThereAreNoGames_ShouldReturnNoGame() {
        when(games.isEmpty()).thenReturn(true);
        Assert.assertEquals(NO_GAME.getValue(), server.listGames().toString());
    }

    @Test
    public void joinGamesTest_WhenYouHaveGame_ShouldReturnHaveGame() {
        when(gameByChannels.get(socketChannel)).thenReturn(new Game());

        Assert.assertEquals(HAVE_GAME.getValue() + "null\n", server.joinGame(socketChannel, "name", 1, new String[]{"join-game", "game"}).toString());
    }

    @Test
    public void joinGamesTest_WhenYouDontHaveGame_ShouldSuccessfulMessage() {
        when(gameByChannels.get(socketChannel)).thenReturn(null);
        Set<Map.Entry<String, Game>> gameSet = new HashSet<>();

        Game game = new Game();
        game.setName("game");
        gameSet.add(new AbstractMap.SimpleEntry<>("game", game));

        when(games.entrySet()).thenReturn(gameSet);
        String message = JOINED_GAME.getValue() + "game" + "\n" +
                "PLAYERS: " + "1/2" + "\n" +
                ENTER_START.getValue();
        Assert.assertEquals(message, server.joinGame(socketChannel, "name", 1, new String[]{"join-game", "game"}).toString());
    }

    @Test
    public void joinGamesTest_WhenRandomGame_ShouldSuccessfulMessage() {
        when(gameByChannels.get(socketChannel)).thenReturn(null);
        Map<String, Game> gameSet = new HashMap<>();

        Game game = new Game();
        game.setName("game");
        gameSet.put("game", game);
        game.addUserToGame("1");

        when(games.values()).thenReturn(Collections.singletonList(game));

        String message = JOINED_GAME.getValue() + "game" + "\n" +
                "PLAYERS: " + "2/2" + "\n" +
                ENTER_START.getValue();
        Assert.assertEquals(message, server.joinGame(socketChannel, "name", 1, new String[]{"join-game"}).toString());
    }

    @Test
    public void placeShipTest_WhenNoGame_ShouldReturnNotJoinedMessage() {
        when(gameByChannels.get(socketChannel)).thenReturn(null);
        Assert.assertEquals(NOT_JOINED.getValue(),
                server.placeShip(socketChannel, "name", 1, new String[]{"place", "A1-A3"}).toString());
    }

    @Test
    public void placeShipTest_WhenNotPlacing_ShouldReturnNotReadyMessage() {
        Game game = new Game();
        game.setGameState(GameState.NOT_READY);
        when(gameByChannels.get(socketChannel)).thenReturn(game);

        Assert.assertEquals(NOT_READY.getValue(),
                server.placeShip(socketChannel, "name", 1, new String[]{"place", "A1-A3"}).toString());
    }

    @Test
    public void hitShipTest_WhenGameIsNull_ShouldReturnNotJoinedMessage() throws IOException {
        when(gameByChannels.get(socketChannel)).thenReturn(null);
        Assert.assertEquals(NOT_JOINED.getValue(), server.hitShip(socketChannel, "name", 0, new String[]{"hit", "A1"}).toString());
    }

    @Test
    public void hitShipTest_WhenInvalidPosition_ShouldReturnInvalidMessage() throws IOException {
        Game game = new Game();

        when(gameByChannels.get(socketChannel)).thenReturn(game);

        Assert.assertEquals(INVALID_POSITION.getValue(), server.hitShip(socketChannel, "name", 1, new String[]{"hit", "a"}).toString());
    }

    @Test
    public void hitShipTest_WhenGameIsNotPlaying_ShouldReturnNotReady() throws IOException {
        Game game = new Game();

        game.setGameState(GameState.NOT_READY);
        when(gameByChannels.get(socketChannel)).thenReturn(game);

        Assert.assertEquals(NOT_READY.getValue(), server.hitShip(socketChannel, "name", 1, new String[]{"hit", "A1"}).toString());
    }
}
