package com.owl.cocoa.ship.goals;

import com.owl.cocoa.common.Item;
import java.util.Optional;

public class TradeScanAwaitGoal implements ShipGoal {

    public final long expiryNano;
    public final Optional<Item> item;

    public TradeScanAwaitGoal(Optional<Item> item, long expiryNano) {
        this.item = item;
        this.expiryNano = expiryNano;
    }

}
