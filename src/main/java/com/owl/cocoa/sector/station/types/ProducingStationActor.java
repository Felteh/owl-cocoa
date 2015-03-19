package com.owl.cocoa.sector.station.types;

import akka.contrib.pattern.DistributedPubSubMediator;
import com.owl.cocoa.scene.SceneActor;
import com.owl.cocoa.sector.station.StationActor;

public class ProducingStationActor extends StationActor {

    private static final String PRODUCTION_ITEM = "wheat";
    private static final Integer TICK_INV_AMOUNT = 10;

    @Override
    protected void tick() {
        Integer quantity = inventory.inventory.get(PRODUCTION_ITEM);
        if (quantity == null) {
            quantity = 0;
        }
        if ((inventory.totalInventory + TICK_INV_AMOUNT) <= inventory.maxInventory) {
            quantity = quantity + TICK_INV_AMOUNT;
            inventory = inventory.withItem(PRODUCTION_ITEM, quantity);
            mediator.tell(new DistributedPubSubMediator.Publish(SceneActor.SCENE_EVENTS, inventory), getSelf());
        }
    }

}
