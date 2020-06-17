package input.commands;

import interfaces.Command;

import java.nio.channels.SocketChannel;
import java.util.Map;

import static enums.Message.USERNAME_CREATED;

public class UsernameCommand implements Command {

    private SocketChannel socketChannel;
    private Map<SocketChannel, String> usernamesByChannels;
    private Map<String, SocketChannel> channelsByUsernames;
    private int tokenIndex;
    private String[] tokens;

    public UsernameCommand(SocketChannel socketChannel, Map<SocketChannel, String> usernamesByChannels, Map<String, SocketChannel> channelsByUsernames, int tokenIndex, String[] tokens) {
        this.socketChannel = socketChannel;
        this.usernamesByChannels = usernamesByChannels;
        this.channelsByUsernames = channelsByUsernames;
        this.tokenIndex = tokenIndex;
        this.tokens = tokens;
    }

    @Override
    public StringBuilder execute() {
        return createUsername(socketChannel, usernamesByChannels, channelsByUsernames, tokenIndex, tokens);
    }

    private StringBuilder createUsername(SocketChannel socketChannel, Map<SocketChannel, String> usernamesByChannels, Map<String, SocketChannel> channelsByUsernames, int tokenIndex, String[] tokens) {
        StringBuilder message;
        String newUsername = tokens[tokenIndex++];
        channelsByUsernames.remove(usernamesByChannels.get(socketChannel));
        usernamesByChannels.put(socketChannel, newUsername);
        channelsByUsernames.put(newUsername, socketChannel);
        message = new StringBuilder(USERNAME_CREATED.getValue());
        return message;
    }
}
