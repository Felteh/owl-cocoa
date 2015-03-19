package com.owl.cocoa.sector.station.types;

import akka.contrib.pattern.DistributedPubSubMediator;
import com.owl.cocoa.scene.SceneActor;
import com.owl.cocoa.sector.station.StationActor;

public class FactoryStationActor extends StationActor {

    private static final String CONSUMPTION_ITEM = "wheat";
    private static final String PRODUCTION_ITEM = "gold";
    private static final Integer TICK_INV_PRODUCTION_AMOUNT = 10;
    private static final Integer TICK_INV_CONSUMPTION_AMOUNT = 200;

    @Override
    protected void tick() {
        Integer consumptionQ = inventory.inventory.get(CONSUMPTION_ITEM);
        if (consumptionQ != null && consumptionQ >= TICK_INV_CONSUMPTION_AMOUNT) {
            if ((inventory.totalInventory + TICK_INV_PRODUCTION_AMOUNT - TICK_INV_CONSUMPTION_AMOUNT) <= inventory.maxInventory) {
                consumptionQ = consumptionQ - TICK_INV_CONSUMPTION_AMOUNT;
                Integer productionQ = inventory.inventory.get(PRODUCTION_ITEM);
                if (productionQ == null) {
                    productionQ = 0;
                }
                productionQ = productionQ + TICK_INV_PRODUCTION_AMOUNT;

                inventory = inventory.withItem(CONSUMPTION_ITEM, consumptionQ);
                inventory = inventory.withItem(PRODUCTION_ITEM, productionQ);

                mediator.tell(new DistributedPubSubMediator.Publish(SceneActor.SCENE_EVENTS, inventory), getSelf());
            }
        }
    }

}
