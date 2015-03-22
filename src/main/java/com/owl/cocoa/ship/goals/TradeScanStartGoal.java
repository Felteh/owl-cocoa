package com.owl.cocoa.ship.goals;

import com.owl.cocoa.common.Item;
import java.util.Optional;

public class TradeScanStartGoal implements ShipGoal {

    public final Optional<Item> item;

    public TradeScanStartGoal() {
        this.item = Optional.empty();
    }

    public TradeScanStartGoal(Item item) {
        this.item = Optional.of(item);
    }

}
