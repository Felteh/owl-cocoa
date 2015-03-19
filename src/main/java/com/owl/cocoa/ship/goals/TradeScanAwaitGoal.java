package com.owl.cocoa.ship.goals;

public class TradeScanAwaitGoal implements ShipGoal {

    public final long expiryNano;

    public TradeScanAwaitGoal(long expiryNano) {
        this.expiryNano = expiryNano;
    }

}
