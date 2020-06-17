import enums.GameState;
import input.commands.*;
import interfaces.Command;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.Game;

import static enums.Message.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

    private Command command;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createGameTest_WhenCreated_ShouldReturnSuccessfulMessage() {
        when(gameByChannels.get(SocketChannel.class)).thenReturn(null);
        String username = "name";
        command = new CreateGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"create-game", "ala"}, username);

        Assert.assertEquals("Created game " + "ala, PLAYERS: 1/2\n" + ENTER_START.getValue(),
                command.execute().toString());

    }

    @Test
    public void createUsernameTest_ShouldReturnSuccessfulMessage() {
        String username = "name";
        channelsByUsername.put(username, socketChannel);

        when(usernamesByChannels.get(socketChannel)).thenReturn(username);
        command = new UsernameCommand(socketChannel, usernamesByChannels, channelsByUsername, 1, new String[]{"create-game", "ala"});
        Assert.assertEquals(USERNAME_CREATED.getValue(), command.execute().toString());
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
        command = new ListGamesCommand(games);
        Assert.assertEquals(AVAILABLE_GAMES.getValue() + "a(2/2)\n", command.execute().toString());
    }

    @Test
    public void listGamesTest_WhenThereAreNoGames_ShouldReturnNoGame() {
        when(games.isEmpty()).thenReturn(true);
        command = new ListGamesCommand(games);
        Assert.assertEquals(NO_GAME.getValue(), command.execute().toString());
    }

    @Test
    public void joinGamesTest_WhenYouHaveGame_ShouldReturnHaveGame() {
        when(gameByChannels.get(socketChannel)).thenReturn(new Game());
        command = new JoinGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"join-game", "game"}, "name");
        Assert.assertEquals(HAVE_GAME.getValue() + "null\n", command.execute().toString());
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

        command = new JoinGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"join-game", "game"}, "name");

        Assert.assertEquals(message, command.execute().toString());
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

        command = new JoinGameCommand(socketChannel, games, gameByChannels, 1, new String[]{"join-game"}, "name");

        Assert.assertEquals(message, command.execute().toString());
    }

    @Test
    public void placeShipTest_WhenNoGame_ShouldReturnNotJoinedMessage() {
        when(gameByChannels.get(socketChannel)).thenReturn(null);
        command = new PlaceCommand(socketChannel, gameByChannels, 1, new String[]{"place", "A1-A3"}, "name");
        Assert.assertEquals(NOT_JOINED.getValue(),
                command.execute().toString());
    }

    @Test
    public void placeShipTest_WhenNotPlacing_ShouldReturnNotReadyMessage() {
        Game game = new Game();
        game.setGameState(GameState.NOT_READY);
        when(gameByChannels.get(socketChannel)).thenReturn(game);

        command = new PlaceCommand(socketChannel, gameByChannels, 1, new String[]{"place", "A1-A3"}, "name");

        Assert.assertEquals(NOT_READY.getValue(),
                command.execute().toString());
    }

    @Test
    public void hitShipTest_WhenGameIsNull_ShouldReturnNotJoinedMessage() {
        when(gameByChannels.get(socketChannel)).thenReturn(null);
        command = new HitCommand(socketChannel, gameByChannels, channelsByUsername, 0,  new String[]{"hit", "A1"}, "name");

        Assert.assertEquals(NOT_JOINED.getValue(), command.execute().toString());
    }

    @Test
    public void hitShipTest_WhenInvalidPosition_ShouldReturnInvalidMessage() {
        Game game = new Game();

        when(gameByChannels.get(socketChannel)).thenReturn(game);

        command = new HitCommand(socketChannel, gameByChannels, channelsByUsername, 1, new String[]{"hit", "a"}, "name");

        Assert.assertEquals(INVALID_POSITION.getValue(), command.execute().toString());
    }

    @Test
    public void hitShipTest_WhenGameIsNotPlaying_ShouldReturnNotReady() {
        Game game = new Game();

        game.setGameState(GameState.NOT_READY);
        when(gameByChannels.get(socketChannel)).thenReturn(game);

        command = new HitCommand(socketChannel, gameByChannels, channelsByUsername, 1, new String[]{"hit", "A1"}, "name");

        Assert.assertEquals(NOT_READY.getValue(), command.execute().toString());
    }
}
