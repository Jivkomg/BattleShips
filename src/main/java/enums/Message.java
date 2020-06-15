package enums;

public enum Message {
    SELECTOR_UNABLE_TO_SELECT_KEYS("Sorry, selector unable to select the keys. Please try again\n"),
    UNABLE_TO_INITIALIZE_APPLICATION("Sorry, unable to start the application. Please try again\n"),
    ERROR_HAS_OCCURRED("Unexpected error has occurred\n"),
    CANNOT_JOIN_GAME("Cannot join game\n"),
    USERNAME_CREATED("Successfully created new username!\n"),
    JOINED_GAME("Successfully joined a game: "),
    WRONG_COMMAND("Wrong command!\n"),
    HAVE_GAME("You already have a game: "),
    FULL_GAME("Sorry, but the game is full. Try finding another one\n"),
    READING_EXCEPTION_HAS_OCCURRED("Sorry, a problem when reading from server has occurred\n"),
    NOT_JOINED("Sorry, but you haven't joined a game yet\n"),
    INVALID_POSITION("Sorry, but this is not a valid position or you are exceeding the limit.\n"),
    PLACE_SHIP("Successfully places a ship\n"),
    NO_NAME("Unsuccessful, you didn't type any name\n"),
    NO_GAME("Sorry, but there are no games. You can create one yourself\n"),
    AVAILABLE_GAMES("Available games are: "),
    NOT_READY("Wait, the game is not ready\n"),
    READY("You are ready. Waiting for the other player!\n"),
    OTHER_PLAYER_TURN("Sorry, it's the other's player turn"),
    ENTER_START("Enter <start> to state that you are ready to begin\n"),
    DUPLICATING_NAME("Sorry, but there is already a game with this name\n"),
    CANNOT_EXIT_GAME("Sorry, but you are not in a game\n"),
    EXIT_GAME("Player has exited game:\n"),
    SAVE_GAME("Successfully saved a game!\n"),
    CANNOT_LOAD("Sorry, you can only load a game during placing mode, and if you were in the game\n"),
    LOAD_GAME("Successfully loaded a game\n"),
    DELETE_GAME("Successfully deleted game\n"),
    CANNOT_DELETE("Cannot delete game\n"),
    START_HITTING("You can now start hitting the other player!\n");

    private String value;

    public String getValue() {
        return value;
    }

    Message(String value) {
        this.value = value;
    }
}