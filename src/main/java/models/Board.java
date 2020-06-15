package models;

import enums.ShipType;
import enums.State;

import java.util.ArrayList;
import java.util.List;

import static enums.ShipType.*;

public class Board {
    private final static int BOARD_SIZE = 10;
    private Field[][] fields;
    private List<Ship> ships;


    public Board() {
        fields = new Field[BOARD_SIZE][BOARD_SIZE];

        ships = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
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
            if (!fields[axisCoordinate][i].getState().equals(State.NO_SHIP)) {
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
            if (!fields[i][axisCoordinate].getState().equals(State.NO_SHIP)) {
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
        return x < 0 || y < 0 || x >= BOARD_SIZE || y >= BOARD_SIZE;
    }


    public boolean gameOver() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    public String getBoardView() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("       YOUR BOARD").append("\n  ");

        for (int j = 1; j <= BOARD_SIZE; j++) {
            stringBuilder.append(" ").append(j);
        }

        stringBuilder.append("\n\n  ");

        for (int j = 1; j <= BOARD_SIZE; j++) {
            stringBuilder.append(" ").append("_");
        }
        stringBuilder.append("\n");

        for (int i = 0; i < BOARD_SIZE; i++) {
            stringBuilder.append((char) ('A' + i)).append(" |");
            for (int j = 0; j < BOARD_SIZE; j++) {
                Field field = fields[i][j];
                State state = field.getState();

                char symbol = '_';

                if (state.equals(State.DAMAGED_SHIP)) {
                    symbol = 'X';
                } else if (state.equals(State.UNDAMAGED_SHIP)) {
                    symbol = '*';
                } else if (state.equals(State.MISSED_SHIP)) {
                    symbol = 'O';
                }
                stringBuilder.append(symbol).append("|");
            }
            stringBuilder.append('\n');
        }
        return String.valueOf(stringBuilder);
    }

    public String getHiddenBoardView() {
        return getBoardView().replaceAll("\\*", "_")
                .replace("YOUR BOARD", "ENEMY BOARD");
    }

    public void play(String move) {
        int xCoordinate = move.toUpperCase().charAt(0) - 'A';
        int yCoordinate = Integer.parseInt(move.substring(1)) - 1;
        fields[xCoordinate][yCoordinate].getHit();
    }

    public boolean isFull() {
        return ships.size() == 10;
    }
}
