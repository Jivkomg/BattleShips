package models;

import enums.ShipType;

import java.util.Objects;

public class Ship {
    private ShipType shipType;
    private int health;

    public Ship(ShipType shipType) {
        this.shipType = shipType;
        this.health = shipType.length;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public void getHit() {
        health--;
    }

    public boolean isSunk() {
        return health == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return shipType == ship.shipType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shipType);
    }
}
