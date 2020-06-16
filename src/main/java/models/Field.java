package models;

import enums.ShipFieldState;

import static enums.ShipFieldState.*;

public class Field {
    private Ship ship;
    private int xCoordinate;
    private int yCoordinate;
    private ShipFieldState shipFieldState;

    Field(int xCoordinate, int yCoordinate) {
        this.ship = null;
        this.shipFieldState = NO_SHIP_FIELD;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }


    void setShip(Ship ship) {
        this.ship = ship;
        this.shipFieldState = UNDAMAGED_SHIP_FIELD;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    ShipFieldState getShipFieldState() {
        return shipFieldState;
    }

    boolean getHit() {
        if (UNDAMAGED_SHIP_FIELD.equals(shipFieldState)) {
            ship.getHit();
            shipFieldState = DAMAGED_SHIP_FIELD;
            return true;
        }
        if (!DAMAGED_SHIP_FIELD.equals(shipFieldState)) {
            shipFieldState = MISSED_SHIP_FIELD;
        }
        return false;
    }
}
