package models;

import enums.ShipType;
import enums.ShipFieldState;

import java.util.ArrayList;
import java.util.List;

import static enums.ShipType.*;

public class Board {
    private final static int BOARD_MAX_SIZE = 10;
    private final static int BOARD_MIN_SIZE = 0;

    private static final String FIRST_PLAYER_BOARD_MESSAGE = "YOUR BOARD";
    private static final String SECOND_PLAYER_BOARD_MESSAGE = "ENEMY BOARD";

    private static final String REGEX = "\\*";
    private static final char EMPTY_FIELD_TOKEN = '_';
    private static final char EMPTY_FIELD_SIDE_TOKEN = '|';
    private static final char FIRST_BOARD_INDEX = 'A';

    private Field[][] fields;
    private List<Ship> ships;


    public Board() {
        fields = new Field[BOARD_MAX_SIZE][BOARD_MAX_SIZE];

        ships = new ArrayList<>();

        for (int i = 0; i < BOARD_MAX_SIZE; i++) {
            for (int j = 0; j < BOARD_MAX_SIZE; j++) {
                fields[i][j] = new Field(i, j);
            }
        }
    }

    public void setShips(List<Ship> ships) {
        this.ships = ships;
    }

    public boolean placeShip(int x1, int y1, int x2, int y2) {
        if (isInvalidField(x1, y1)) {
            return false;
        }

        if (isInvalidField(x2, y2)) {
            return false;
        }

        if (x1 == x2) {
            return setupShipOnXAxis(x1, y1, y2);
        }
        if (y1 == y2) {
            return setupShipOnYAxis(y1, x1, x2);
        } else {
            return false;
        }

    }

    private boolean setupShipOnXAxis(int axisCoordinate, int firstCoordinate, int lastCoordinate) {
        int shipLength = Math.abs(firstCoordinate - lastCoordinate) + 1;
        ShipType shipType = ShipType.fromLength(shipLength);

        if (shipType == null) {
            return false;
        }

        Ship ship = new Ship(shipType);

        if (ifShipTypeExceedsLimit(shipType, ship)) {
            return false;
        }

        for (int i = firstCoordinate; i <= lastCoordinate; i++) {
            if (!fields[axisCoordinate][i].getShipFieldState().equals(ShipFieldState.NO_SHIP_FIELD)) {
                return false;
            }
        }

        for (int i = firstCoordinate; i <= lastCoordinate; i++) {
            fields[axisCoordinate][i].setShip(ship);
        }

        ships.add(ship);
        return true;
    }

    private boolean setupShipOnYAxis(int axisCoordinate, int firstCoordinate, int lastCoordinate) {
        int shipLength = Math.abs(firstCoordinate - lastCoordinate) + 1;
        ShipType shipType = ShipType.fromLength(shipLength);

        if (shipType == null) {
            return false;
        }

        Ship ship = new Ship(shipType);

        if (ifShipTypeExceedsLimit(shipType, ship)) {
            return false;
        }

        for (int i = firstCoordinate; i <= lastCoordinate; i++) {
            if (!fields[i][axisCoordinate].getShipFieldState().equals(ShipFieldState.NO_SHIP_FIELD)) {
                return false;
            }
        }

        for (int i = firstCoordinate; i <= lastCoordinate; i++) {
            fields[i][axisCoordinate].setShip(ship);
        }

        ships.add(ship);
        return true;
    }

    private boolean ifShipTypeExceedsLimit(ShipType shipType, Ship ship) {
        if (shipType.equals(AIRCRAFT_CARRIER)) {
            return ships.contains(ship);
        } else if (shipType.equals(BATTLESHIP)) {
            int cnt = (int) ships.stream().filter(s -> s.getShipType().equals(BATTLESHIP)).count();
            return cnt >= 2;
        } else if (shipType.equals(SUBMARINE)) {
            int cnt = (int) ships.stream().filter(s -> s.getShipType().equals(SUBMARINE)).count();
            return cnt >= 3;
        } else if (shipType.equals(PATROL_BOAT)) {
            int cnt = (int) ships.stream().filter(s -> s.getShipType().equals(PATROL_BOAT)).count();
            return cnt >= 4;
        }
        return false;
    }

    private boolean isInvalidField(int x, int y) {
        return x < BOARD_MIN_SIZE || y < BOARD_MIN_SIZE || x >= BOARD_MAX_SIZE || y >= BOARD_MAX_SIZE;
    }

    public boolean gameOver() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    public String getBoardView() {
        StringBuilder stringBuilder = getEmptyBoardView();

        for (int i = 0; i < BOARD_MAX_SIZE; i++) {
            stringBuilder.append((char) (FIRST_BOARD_INDEX + i)).append(" ").append(EMPTY_FIELD_SIDE_TOKEN);
            for (int j = 0; j < BOARD_MAX_SIZE; j++) {
                Field field = fields[i][j];
                ShipFieldState shipFieldState = field.getShipFieldState();

                char token = shipFieldState.getToken();

                stringBuilder.append(token).append(EMPTY_FIELD_SIDE_TOKEN);
            }
            stringBuilder.append('\n');
        }
        return String.valueOf(stringBuilder);
    }

    private StringBuilder getEmptyBoardView() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("       ").append(FIRST_PLAYER_BOARD_MESSAGE).append("\n  ");

        for (int j = 1; j <= BOARD_MAX_SIZE; j++) {
            stringBuilder.append(" ").append(j);
        }

        stringBuilder.append("\n  ");

        for (int j = 1; j <= BOARD_MAX_SIZE; j++) {
            stringBuilder.append(" ").append(EMPTY_FIELD_TOKEN);
        }
        stringBuilder.append("\n");
        return stringBuilder;
    }

    public String getHiddenBoardView() {
        return getBoardView().replaceAll(REGEX, String.valueOf(EMPTY_FIELD_TOKEN))
                .replace(FIRST_PLAYER_BOARD_MESSAGE, SECOND_PLAYER_BOARD_MESSAGE);
    }

    public void play(String move) {
        int xCoordinate = move.toUpperCase().charAt(0) - FIRST_BOARD_INDEX;
        int yCoordinate = Integer.parseInt(move.substring(1)) - 1;
        fields[xCoordinate][yCoordinate].getHit();
    }

    public boolean isFull() {
        return ships.size() == 10;
    }
}
