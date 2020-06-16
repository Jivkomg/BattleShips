package models;

import enums.ShipFieldState;

import static enums.ShipFieldState.*;

class Field {
    private Ship ship;
    private ShipFieldState shipFieldState;

    Field() {
        this.ship = null;
        this.shipFieldState = NO_SHIP_FIELD;
    }

    void setShip(Ship ship) {
        this.ship = ship;
        this.shipFieldState = UNDAMAGED_SHIP_FIELD;
    }

    ShipFieldState getShipFieldState() {
        return shipFieldState;
    }

    void getHit() {
        if (UNDAMAGED_SHIP_FIELD.equals(shipFieldState)) {
            ship.getHit();
            shipFieldState = DAMAGED_SHIP_FIELD;
            return;
        }
        if (!DAMAGED_SHIP_FIELD.equals(shipFieldState)) {
            shipFieldState = MISSED_SHIP_FIELD;
        }
    }
}
