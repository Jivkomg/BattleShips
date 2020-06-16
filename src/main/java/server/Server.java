package server;

import com.google.gson.Gson;
import enums.GameState;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static enums.Message.*;

public class Server {
    private static final int HOST_PORT = 8080;
    private static final String HOST_NAME = "localhost";
    private static final int MESSAGE_TOKENS_COUNT = 3;
    private static final String MESSAGE_ELEMENTS_REGEX = " ";
    private static final int BUFFER_CAPACITY = 1000;

    private static final String USERNAME_COMMAND = "username";
    private static final String CREATE_GAME_COMMAND = "create-game";
    private static final String JOIN_GAME_COMMAND = "join-game";
    private static final String LIST_GAMES_COMMAND = "list-games";
    private static final String START_COMMAND = "start";
    private static final String SAVE_GAMES_COMMAND = "save-game";
    private static final String LOAD_GAME_COMMAND = "load-game";
    private static final String DELETE_GAME_COMMAND = "delete-game";
    private static final String DISCONNECT_COMMAND = "disconnect";
    private static final String HIT_COMMAND = "hit";
    private static final String PLACE_COMMAND = "place";
    private static final String EXIT_GAME_COMMAND = "exit";


    private Map<SocketChannel, String> usernamesByChannels = new HashMap<>();
    private Map<String, SocketChannel> channelsByUsernames = new HashMap<>();
    private Map<String, Game> games = new HashMap<>();
    private Map<SocketChannel, Game> gameByChannels = new HashMap<>();

    private static final String FILE_PATH = "src\\main\\resources\\games";

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(HOST_NAME, HOST_PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isAcceptable()) {
                            registerChannel(selector, serverSocketChannel);
                        } else if (key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            readFromClient(socketChannel);
                        }

                        updateChannels();

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    System.err.println(SELECTOR_UNABLE_TO_SELECT_KEYS.getValue());
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println(UNABLE_TO_INITIALIZE_APPLICATION.getValue());
            e.printStackTrace();
        }

    }

    private void registerChannel(Selector selector, ServerSocketChannel serverSocketChannel) {
        try {
            SocketChannel clientSocketChannel = serverSocketChannel.accept();
            clientSocketChannel.configureBlocking(false);
            clientSocketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            String username = UUID.randomUUID().toString();
            usernamesByChannels.put(clientSocketChannel, username);
            channelsByUsernames.put(username, clientSocketChannel);
        } catch (IOException e) {
            System.err.println(ERROR_HAS_OCCURRED.getValue());
            e.printStackTrace();
        }
    }

    private void readFromClient(SocketChannel socketChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
            StringBuilder clientMessageBuilder = new StringBuilder();
            while (socketChannel.read(buffer) > 0) {
                buffer.flip();
                clientMessageBuilder.append(new String(buffer.array(), 0, buffer.limit()));
                buffer.clear();
            }
            String clientMessage = clientMessageBuilder.toString();

            String username = usernamesByChannels.get(socketChannel);

            int tokenIndex = 0;
            String[] tokens = clientMessage.split(MESSAGE_ELEMENTS_REGEX, MESSAGE_TOKENS_COUNT);

            String command = tokens[tokenIndex++];
            StringBuilder message;
            if (USERNAME_COMMAND.equals(command)) {
                message = createUsername(socketChannel, tokenIndex, tokens);
            } else if (CREATE_GAME_COMMAND.equals(command)) {
                message = createGame(socketChannel, username, tokenIndex, tokens);
            } else if (JOIN_GAME_COMMAND.equals(command)) {
                message = joinGame(socketChannel, username, tokenIndex, tokens);
            } else if (LIST_GAMES_COMMAND.equals(command)) {
                message = listGames();
            } else if (PLACE_COMMAND.equals(command)) {
                message = placeShip(socketChannel, username, tokenIndex, tokens);
            } else if (HIT_COMMAND.equals(command)) {
                message = hitShip(socketChannel, username, tokenIndex, tokens);
            } else if (START_COMMAND.equals(command)) {
                message = startGame(socketChannel, username);
            } else if (DISCONNECT_COMMAND.equals(command)) {
                channelsByUsernames.remove(usernamesByChannels.remove(socketChannel));

                ByteBuffer writeBuffer = ByteBuffer.wrap(DISCONNECT_COMMAND.getBytes());
                socketChannel.write(writeBuffer);
                socketChannel.close();
                return;
            } else if (SAVE_GAMES_COMMAND.equals(command)) {
                message = saveGame(socketChannel);
            } else if (LOAD_GAME_COMMAND.equals(command)) {
                message = loadGame(socketChannel, username, tokenIndex, tokens);
            } else if (DELETE_GAME_COMMAND.equals(command)) {
                message = deleteGame(tokenIndex, tokens);
            } else if (EXIT_GAME_COMMAND.equals(command)) {
                message = exitGame(socketChannel, username);
            } else {
                message = new StringBuilder(WRONG_COMMAND.getValue());
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(message.toString().getBytes());
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            System.err.println(ERROR_HAS_OCCURRED.getValue());
            e.printStackTrace();
        }
    }

    private StringBuilder exitGame(SocketChannel socketChannel, String username) throws IOException {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            message = exitGame(socketChannel, username, game);
        } else {
            message = new StringBuilder(CANNOT_EXIT_GAME.getValue());
        }
        return message;
    }

    private StringBuilder deleteGame(int tokenIndex, String[] tokens) {
        StringBuilder message;
        String gameName = tokens[tokenIndex++];
        File file = new File(FILE_PATH + "\\" + gameName + ".json");
        if (file.delete()) {
            message = new StringBuilder(DELETE_GAME.getValue());
        } else {
            message = new StringBuilder(CANNOT_DELETE.getValue());
        }
        return message;
    }

    private StringBuilder loadGame(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            if (game.getGameState() == GameState.PLACING) {
                String loadedGameName = tokens[tokenIndex++];
                try (FileReader fileReader = new FileReader(FILE_PATH + "\\" + loadedGameName + ".json")) {
                    Gson gson = new Gson();
                    Game loadedGame = gson.fromJson(fileReader, Game.class);
                    String otherUsername = game.getPlayers()
                            .stream().filter(n -> !username.equals(n)).findFirst().get();

                    if (loadedGame.getPlayers().contains(username)
                            && loadedGame.getPlayers().contains(otherUsername)) {

                        gameByChannels.put(socketChannel, loadedGame);
                        gameByChannels.put(channelsByUsernames.get(otherUsername), loadedGame);

                        games.put(loadedGameName, loadedGame);

                        ByteBuffer byteBuffer = ByteBuffer.wrap(LOAD_GAME.getValue().getBytes());
                        channelsByUsernames.get(otherUsername).write(byteBuffer);

                        message = new StringBuilder(LOAD_GAME.getValue());
                    } else {
                        message = new StringBuilder(CANNOT_LOAD.getValue());
                    }
                } catch (IOException e) {
                    message = new StringBuilder(e.getMessage());
                }

            } else {
                message = new StringBuilder(CANNOT_LOAD.getValue());
            }
        } else {
            message = new StringBuilder(NO_GAME.getValue());
        }
        return message;
    }

    private StringBuilder saveGame(SocketChannel socketChannel) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            try (FileWriter fileWriter = new FileWriter(FILE_PATH + "\\" + game.getName() + ".json")) {
                Gson gson = new Gson();
                gson.toJson(game, fileWriter);
                message = new StringBuilder(SAVE_GAME.getValue());
            } catch (IOException e) {
                message = new StringBuilder(e.getMessage());
            }
        } else {
            message = new StringBuilder(NO_GAME.getValue());
        }
        return message;
    }

    private StringBuilder exitGame(SocketChannel socketChannel, String username, Game game) throws IOException {
        StringBuilder message;
        gameByChannels.put(socketChannel, null);

        message = new StringBuilder(EXIT_GAME.getValue()).append(username).append("\n");
        if (game.getPlayers().size() > 1) {
            String otherUsername = game.getPlayers()
                    .stream().filter(n -> !username.equals(n)).findFirst().get();
            game.getPlayers().remove(username);
            game.getPlayers().remove(otherUsername);
            ByteBuffer byteBuffer = ByteBuffer.wrap(message.toString().getBytes());
            channelsByUsernames.get(otherUsername).write(byteBuffer);
        }
        return message;
    }

    private StringBuilder startGame(SocketChannel socketChannel, String username) throws IOException {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            if (GameState.NOT_READY.equals(game.getGameState())) {
                message = new StringBuilder(READY.getValue());
                String messageToBegin = game.ready(username);
                message.append(game.ready(username));

                if (game.getPlayers().size() > 1) {
                    String otherUsername = game.getPlayers()
                            .stream().filter(n -> !username.equals(n)).findFirst().get();
                    ByteBuffer byteBuffer = ByteBuffer.wrap(messageToBegin.getBytes());
                    channelsByUsernames.get(otherUsername).write(byteBuffer);
                }
            } else {
                message = new StringBuilder(HAVE_GAME.getValue());
            }
        } else {
            message = new StringBuilder(NOT_JOINED.getValue());
        }
        return message;
    }

    public StringBuilder hitShip(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) throws IOException {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            String position = tokens[tokenIndex++];
            if (checkValidPosition(position)) {
                if (GameState.PLAYING.equals(game.getGameState())) {
                    if (game.isPlayerTurn(username)) {
                        Map<String, String> messages = game.play(username, position);
                        message = new StringBuilder(messages.get(username));
                        String lastTurnMessage = "Last turn: " + username + ", " + position + "\n";

                        message.append(lastTurnMessage);

                        String otherUsername = game.getPlayers()
                                .stream().filter(n -> !username.equals(n)).findFirst().get();

                        String messageToEnemy = messages.get(otherUsername) + lastTurnMessage;
                        ByteBuffer byteBuffer = ByteBuffer.wrap(messageToEnemy.getBytes());
                        channelsByUsernames.get(otherUsername).write(byteBuffer);
                    } else {
                        message = new StringBuilder(OTHER_PLAYER_TURN.getValue());
                    }
                } else {
                    message = new StringBuilder(NOT_READY.getValue());
                }
            } else {
                message = new StringBuilder(INVALID_POSITION.getValue());
            }

        } else {
            message = new StringBuilder(NOT_JOINED.getValue());
        }
        return message;
    }

    public StringBuilder placeShip(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game != null) {
            if (game.getGameState().equals(GameState.PLACING)) {
                if (game.placeShips(username, tokens[tokenIndex++])) {
                    message = new StringBuilder(PLACE_SHIP.getValue());
                    if (GameState.PLAYING.equals(game.getGameState())) {
                        message.append(START_HITTING.getValue());
                    }
                } else {
                    message = new StringBuilder(INVALID_POSITION.getValue());
                }
            } else {
                message = new StringBuilder(NOT_READY.getValue());
            }
        } else {
            message = new StringBuilder(NOT_JOINED.getValue());
        }
        return message;
    }

    public StringBuilder listGames() {
        StringBuilder message;
        message = new StringBuilder();
        if (games.isEmpty()) {
            message.append(NO_GAME.getValue());
        } else {
            message.append(AVAILABLE_GAMES.getValue());
            games.keySet().forEach(gameName -> message.append(gameName).append("(").append(games.get(gameName).getPlayers().size()).append("/2)"));
            message.append("\n");
        }
        return message;
    }

    public StringBuilder joinGame(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            Game joinedGame;
            if (tokens.length > 1) {
                String gameName = tokens[tokenIndex++];

                joinedGame = games.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(gameName)).map(Map.Entry::getValue)
                        .findFirst().orElse(null);
            } else {
                if (!games.isEmpty()) {
                    joinedGame = getRandomGame();
                } else {
                    joinedGame = null;
                }
            }
            if (joinedGame == null) {
                message = new StringBuilder(CANNOT_JOIN_GAME.getValue());
            } else {
                if (joinedGame.addUser(username)) {
                    message = new StringBuilder(JOINED_GAME.getValue());
                    message.append(joinedGame.getName()).append("\n");
                    message.append("PLAYERS: ").append(joinedGame.getPlayers()
                            .size()).append("/2").append("\n");
                    message.append(ENTER_START.getValue());
                    gameByChannels.put(socketChannel, joinedGame);
                } else {
                    message = new StringBuilder(FULL_GAME.getValue());
                }
            }
        } else {
            message = new StringBuilder(HAVE_GAME.getValue()).append(game.getName()).append("\n");
        }
        return message;
    }

    public StringBuilder createGame(SocketChannel socketChannel, String username, int tokenIndex, String[] tokens) {
        StringBuilder message;
        Game game = gameByChannels.get(socketChannel);
        if (game == null) {
            if (tokens.length < 2) {
                message = new StringBuilder(NO_NAME.getValue());
            } else {
                String gameName = tokens[tokenIndex++];
                if (games.containsKey(gameName)) {
                    message = new StringBuilder(DUPLICATING_NAME.getValue());
                } else {
                    Game newGame = new Game();
                    newGame.addUser(username);
                    newGame.setName(gameName);
                    games.put(gameName, newGame);
                    gameByChannels.put(socketChannel, newGame);
                    message = new StringBuilder("Created game ").append(gameName).append(", PLAYERS: 1/2\n").append(ENTER_START.getValue());
                }
            }
        } else {
            message = new StringBuilder(HAVE_GAME.getValue()).append(game.getName()).append("\n");
        }
        return message;
    }

    public StringBuilder createUsername(SocketChannel socketChannel, int tokenIndex, String[] tokens) {
        StringBuilder message;
        String newUsername = tokens[tokenIndex++];
        channelsByUsernames.remove(usernamesByChannels.get(socketChannel));
        usernamesByChannels.put(socketChannel, newUsername);
        channelsByUsernames.put(newUsername, socketChannel);
        message = new StringBuilder(USERNAME_CREATED.getValue());
        return message;
    }

    private Game getRandomGame() {
        Random randomIndex = new Random();
        int index = randomIndex.nextInt(games.values().size());
        return (Game) games.values().stream().filter(game -> game.getPlayers().size() == 1).toArray()[index];
    }

    private void updateChannels() {
        usernamesByChannels.keySet().stream()
                .filter(channel -> !channel.isConnected())
                .forEach(channel -> channelsByUsernames.remove(usernamesByChannels.remove(channel)));
    }

    private boolean checkValidPosition(String position) {
        if (position.length() > 3 || position.length() < 2) {
            return false;
        }

        int yCoordinate1 = Integer.parseInt(position.substring(1)) - 1;
        return Character.isLetter(position.charAt(0)) && yCoordinate1 >= 0 && yCoordinate1 <= 9;
    }
}
