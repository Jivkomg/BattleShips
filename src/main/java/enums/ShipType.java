package enums;

import java.util.Arrays;

public enum ShipType {
    AIRCRAFT_CARRIER(5),
    BATTLESHIP(4),
    SUBMARINE(3),
    PATROL_BOAT(2);

    public int length;

    ShipType(int length) {
        this.length = length;
    }

    public static ShipType fromLength(int length) {
        return Arrays.stream(ShipType.values())
                .filter(shipType -> shipType.length == length).findFirst().orElse(null);
    }

}
