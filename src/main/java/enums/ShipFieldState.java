package enums;

public enum ShipFieldState {
    NO_SHIP_FIELD('_'),
    UNDAMAGED_SHIP_FIELD('*'),
    DAMAGED_SHIP_FIELD('X'),
    MISSED_SHIP_FIELD('O');

    private char token;

    ShipFieldState(char token) {
        this.token = token;
    }

    public char getToken() {
        return token;
    }
}
