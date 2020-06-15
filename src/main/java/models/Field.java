package models;

import enums.State;

import static enums.State.*;

public class Field {
    private Ship ship;
    private int xCoordinate;
    private int yCoordinate;
    private State state;

    public Field(int xCoordinate, int yCoordinate) {
        this.ship = null;
        this.state = NO_SHIP;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }


    public void setShip(Ship ship) {
        this.ship = ship;
        this.state = UNDAMAGED_SHIP;
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

    public State getState() {
        return state;
    }

    public boolean getHit() {
        if (UNDAMAGED_SHIP.equals(state)) {
            ship.getHit();
            state = DAMAGED_SHIP;
            return true;
        }
        if (!DAMAGED_SHIP.equals(state)) {
            state = MISSED_SHIP;
        }
        return false;
    }
}
