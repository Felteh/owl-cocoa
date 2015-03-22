package com.owl.cocoa.sector.station;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.owl.cocoa.common.Inventory;
import com.owl.cocoa.common.SpacePosition;
import com.owl.cocoa.scene.SceneActor;
import com.owl.cocoa.sector.station.messages.InventoryRequest;
import com.owl.cocoa.sector.station.messages.InventoryRequestType;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

public abstract class StationActor extends UntypedActor {

    public static final String TICK = "tick";
    public static final String GET_POSITION = "getPos";
    public static final String GET_INVENTORY = "getInv";
    protected final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);
    protected final String objectName;

    private final SpacePosition position;
    protected Inventory inventory;

    private Cancellable cancellable;
    protected ActorRef mediator;

    public StationActor(SpacePosition position) {
        this.objectName = position.objectName;
        this.position = position;
        this.inventory = new Inventory(objectName, 5000000000d, 1000);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String) {
            switch ((String) o) {
                case TICK:
                    tick();
                    break;
                case GET_POSITION:
                    getSender().tell(position, this.getSelf());
                    break;
                case GET_INVENTORY:
                    getSender().tell(inventory, this.getSelf());
                    break;
            }
        } else if (o instanceof InventoryRequest) {
            getSender().tell(processInventoryRequest((InventoryRequest) o), this.getSelf());
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Publish(SceneActor.SCENE_EVENTS, position), getSelf());
        mediator.tell(new DistributedPubSubMediator.Subscribe(position.sector, getSelf()), getSelf());
        mediator.tell(new DistributedPubSubMediator.Subscribe(objectName, getSelf()), getSelf());

        cancellable = this.context().system().scheduler().schedule(Duration.Zero(),
                Duration.create(100, TimeUnit.MILLISECONDS), this.getSelf(), TICK,
                this.context().system().dispatcher(), null);
    }

    protected abstract void tick();

    private Object processInventoryRequest(InventoryRequest inventoryRequest) {
        switch (inventoryRequest.requestType) {
            case BUY: {
                int quantity = inventoryRequest.quantity;
                double price = inventoryRequest.price;

                Integer existingQuantity = inventory.inventory.get(inventoryRequest.item);
                if (existingQuantity == null) {
                    existingQuantity = 0;
                }
                if (quantity > existingQuantity) {
                    quantity = existingQuantity;
                }
                Double cash = inventory.cash + (quantity * price);

                inventory = inventory.withItem(inventoryRequest.item, existingQuantity - quantity);
                inventory = inventory.withCash(cash);

                return new InventoryRequest(InventoryRequestType.SELL, inventoryRequest.item, quantity, price);
            }
            case SELL: {
                int quantity = inventoryRequest.quantity;
                double price = inventoryRequest.price;

                Double cash = inventory.cash;
                Integer spareInventory = inventory.maxInventory - inventory.totalInventory;
                if (quantity > spareInventory) {
                    quantity = spareInventory;
                }
                if ((quantity * price) > cash) {
                    quantity = (int) Math.floor(cash / price);
                }
                cash = cash - (quantity * price);
                Integer existingQuantity = inventory.inventory.get(inventoryRequest.item);
                if (existingQuantity == null) {
                    existingQuantity = 0;
                }
                inventory = inventory.withItem(inventoryRequest.item, existingQuantity + quantity);
                inventory = inventory.withCash(cash);
                return new InventoryRequest(InventoryRequestType.BUY, inventoryRequest.item, quantity, price);
            }
            default:
                throw new RuntimeException("Got request of unknown type:" + inventoryRequest);
        }
    }

}
