package com.owl.cocoa.ship;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.owl.cocoa.common.Inventory;
import com.owl.cocoa.common.Item;
import com.owl.cocoa.common.SpacePosition;
import com.owl.cocoa.scene.SceneActor;
import com.owl.cocoa.sector.station.StationActor;
import com.owl.cocoa.sector.station.messages.InventoryRequest;
import com.owl.cocoa.sector.station.messages.InventoryRequestType;
import com.owl.cocoa.ship.goals.ShipGoal;
import com.owl.cocoa.ship.goals.TradeRouteGoal;
import com.owl.cocoa.ship.goals.TradeRouteGoalStage;
import com.owl.cocoa.ship.goals.TradeScanAwaitGoal;
import com.owl.cocoa.ship.goals.TradeScanStartGoal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ShipActor extends UntypedActor {

    private final String objectName = UUID.randomUUID().toString();
    public static final String TICK = "tick";
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);
    private SpacePosition position = new SpacePosition(objectName, "sector1").withRadius(5);

    private final ArrayBlockingQueue<ShipGoal> goals = new ArrayBlockingQueue<>(50);

    private final Map<String, Inventory> localInventories = new HashMap<>();
    private Inventory shipInventory = new Inventory(objectName, 1000d, 300);

    @Override
    public void preStart() throws Exception {
        super.preStart();

        mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
        cancellable = this.context().system().scheduler().schedule(Duration.Zero(),
                Duration.create(20, TimeUnit.MILLISECONDS), this.getSelf(), TICK,
                this.context().system().dispatcher(), null);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String) {
            switch ((String) o) {
                case TICK:
                    tick();
                    break;
            }
        } else if (o instanceof Inventory) {
            Inventory i = (Inventory) o;
            localInventories.put(i.objectName, i);
        }
    }
    private Cancellable cancellable;
    private ActorRef mediator;

    private void tick() {
        if (goals.isEmpty()) {
            //If more than half full - try to sell things
            if (shipInventory.totalInventory.doubleValue() / shipInventory.maxInventory.doubleValue() < 0.5) {
                goals.add(new TradeScanStartGoal());
            } else {
                List<Entry<Item, Integer>> items = new ArrayList<>(shipInventory.inventory.entrySet());
                Collections.sort(items,
                        (Entry<Item, Integer> o1, Entry<Item, Integer> o2) -> Integer.compare(o2.getValue(), o1.getValue()));
                goals.add(new TradeScanStartGoal(items.get(0).getKey()));
            }
        } else {
            ShipGoal goal = goals.poll();
            if (goal instanceof TradeScanStartGoal) {
                tradeScan((TradeScanStartGoal) goal);
            } else if (goal instanceof TradeScanAwaitGoal) {
                tradeScanAwait((TradeScanAwaitGoal) goal);
            } else if (goal instanceof TradeRouteGoal) {
                tradeRoute((TradeRouteGoal) goal);
            }
        }
    }

    private void tradeScan(TradeScanStartGoal tradeScanGoal) {
        localInventories.clear();
        mediator.tell(new DistributedPubSubMediator.Publish("sector1", StationActor.GET_INVENTORY), getSelf());

        goals.add(new TradeScanAwaitGoal(tradeScanGoal.item, ((System.nanoTime() / 1000000000) + (long) (Math.random() * 20d))));
    }

    private void tradeScanAwait(TradeScanAwaitGoal tradeScanAwaitGoal) {
        if ((System.nanoTime() / 1000000000) < tradeScanAwaitGoal.expiryNano) {
            goals.add(tradeScanAwaitGoal);
        } else {
            if (tradeScanAwaitGoal.item.isPresent()) {
                findSellRoute(tradeScanAwaitGoal.item.get());
            } else {
                findTradeRoute();
            }
        }
    }

    private void findSellRoute(Item i) {
        List<TradeableItem> tradeables = new ArrayList<>();
        for (Entry<String, Inventory> e1 : localInventories.entrySet()) {
            Integer buyQuantity = this.shipInventory.inventory.get(i);
            Double buyPrice = i.minPrice;

            Integer sellQuantity = e1.getValue().inventory.get(i);
            Double sellPrice = e1.getValue().price.get(i);

            TradeableItem t = new TradeableItem(i);
            if (sellQuantity != null && sellQuantity > 0) {
                t = t.withBuy(this.objectName, buyQuantity, buyPrice);
                t = t.withSell(e1.getKey(), sellQuantity, sellPrice);

                if (t.roi > 1) {
                    tradeables.add(t);
                }
            }
        }

        if (!tradeables.isEmpty()) {
            Collections.sort(tradeables, (TradeableItem o1, TradeableItem o2) -> Double.compare(o2.roi, o1.roi));
            List<TradeableItem> subTradeables = tradeables.subList(0, Math.min(tradeables.size(), 5));
            Collections.shuffle(subTradeables);
            TradeableItem potentialRoute = subTradeables.get(0);
            try {
                Future<Object> sellPos = Patterns.ask(mediator, new DistributedPubSubMediator.Publish(potentialRoute.sellFrom, StationActor.GET_POSITION), Timeout.apply(1, TimeUnit.SECONDS));
                SpacePosition sellP = (SpacePosition) Await.result(sellPos, Duration.apply(1, TimeUnit.SECONDS));
                goals.add(new TradeRouteGoal(potentialRoute, sellP));
            } catch (Exception ex) {
                LOG.error(ex, "Fail");
            }
        }
    }

    private void findTradeRoute() {
        List<TradeableItem> tradeables = new ArrayList<>();
        for (Item i : Item.values()) {
            for (Entry<String, Inventory> e1 : localInventories.entrySet()) {
                for (Entry<String, Inventory> e2 : localInventories.entrySet()) {
                    if (!e1.getKey().equals(e2.getKey())) {
                        Integer e1Inventory = e1.getValue().inventory.get(i);
                        if (e1Inventory == null) {
                            e1Inventory = 0;
                        }
                        Double e1Price = e1.getValue().price.get(i);
                        if (e1Price == null) {
                            e1Price = 0d;
                        }

                        Integer e2Inventory = e2.getValue().inventory.get(i);
                        if (e2Inventory == null) {
                            e2Inventory = 0;
                        }
                        Double e2Price = e2.getValue().price.get(i);
                        if (e2Price == null) {
                            e2Price = 0d;
                        }

                        if (e1Inventory > 0 || e2Inventory > 0) {
                            TradeableItem t = new TradeableItem(i);
                            if (e1Inventory == 0) {
                                Integer buyQuantity = maxBuyableQuantity(e2Price);

                                t = t.withBuy(e2.getKey(), Math.min(buyQuantity, e2Inventory), e2Price);
                                t = t.withSell(e1.getKey(), e1Inventory, e1Price);
                            } else if (e2Inventory == 0) {
                                Integer buyQuantity = maxBuyableQuantity(e1Price);
                                t = t.withBuy(e1.getKey(), Math.min(buyQuantity, e1Inventory), e1Price);
                                t = t.withSell(e2.getKey(), e2Inventory, e2Price);
                            } else if (e1Price < e2Price) {
                                Integer buyQuantity = maxBuyableQuantity(e1Price);
                                t = t.withBuy(e1.getKey(), Math.min(buyQuantity, e1Inventory), e1Price);
                                t = t.withSell(e2.getKey(), e2Inventory, e2Price);
                            } else {
                                Integer buyQuantity = maxBuyableQuantity(e2Price);
                                t = t.withBuy(e2.getKey(), Math.min(buyQuantity, e2Inventory), e2Price);
                                t = t.withSell(e1.getKey(), e1Inventory, e1Price);
                            }

                            if (t.roi > 1) {
                                tradeables.add(t);
                            }
                        }
                    }
                }
            }
        }

        if (!tradeables.isEmpty()) {
            Collections.sort(tradeables, (TradeableItem o1, TradeableItem o2) -> Double.compare(o2.roi, o1.roi));
            List<TradeableItem> subTradeables = tradeables.subList(0, Math.min(tradeables.size(), 5));
            Collections.shuffle(subTradeables);
            TradeableItem potentialRoute = subTradeables.get(0);
            try {
                Future<Object> buyPos = Patterns.ask(mediator, new DistributedPubSubMediator.Publish(potentialRoute.buyFrom, StationActor.GET_POSITION), Timeout.apply(1, TimeUnit.SECONDS));
                Future<Object> sellPos = Patterns.ask(mediator, new DistributedPubSubMediator.Publish(potentialRoute.sellFrom, StationActor.GET_POSITION), Timeout.apply(1, TimeUnit.SECONDS));
                SpacePosition buyP = (SpacePosition) Await.result(buyPos, Duration.apply(1, TimeUnit.SECONDS));
                SpacePosition sellP = (SpacePosition) Await.result(sellPos, Duration.apply(1, TimeUnit.SECONDS));
                goals.add(new TradeRouteGoal(potentialRoute, buyP, sellP));
            } catch (Exception ex) {
                LOG.error(ex, "Fail");
            }
        }
    }

    private void tradeRoute(TradeRouteGoal goal) {
        switch (goal.stage) {
            case MOVE_TO_BUY:
                moveTo(goal.buyPosition);
                if (!withinRangeOfPosition(10, goal.buyPosition)) {
                    goals.add(goal);
                } else {
                    goals.add(goal.withStage(TradeRouteGoalStage.BUY));
                }
                break;
            case BUY:
                if (buyMax(goal.potentialRoute.buyFrom, goal.potentialRoute.item, goal.potentialRoute.buyPrice)) {
                    goals.add(goal.withStage(TradeRouteGoalStage.MOVE_TO_SELL));
                }
                break;
            case MOVE_TO_SELL:
                moveTo(goal.sellPosition);
                if (!withinRangeOfPosition(10, goal.sellPosition)) {
                    goals.add(goal);
                } else {
                    goals.add(goal.withStage(TradeRouteGoalStage.SELL));
                }
                break;
            case SELL:
                sellMax(goal.potentialRoute.sellFrom, goal.potentialRoute.item, goal.potentialRoute.sellPrice);
                break;
        }
    }

    private void moveTo(SpacePosition pos) {
        double x = position.x;
        double y = position.y;
        double z = position.z;
        if (x != pos.x) {
            if (x < pos.x) {
                x = x + 1;
            } else {
                x = x - 1;
            }
        }
        if (y != pos.y) {
            if (y < pos.y) {
                y = y + 1;
            } else {
                y = y - 1;
            }
        }
        if (z != pos.z) {
            if (z < pos.z) {
                z = z + 1;
            } else {
                z = z - 1;
            }
        }
        position = position.withPosition(x, y, z);
        mediator.tell(new DistributedPubSubMediator.Publish(SceneActor.SCENE_EVENTS, position), getSelf());
    }

    private boolean withinRangeOfPosition(int i, SpacePosition pos) {
        return Math.abs(pos.x - position.x) < i && Math.abs(pos.y - position.y) < i && Math.abs(pos.z - position.z) < i;
    }

    private boolean buyMax(String buyFrom, Item item, double buyPrice) {
        try {
            Integer buyQuantity = maxBuyableQuantity(buyPrice);

            InventoryRequest buy = new InventoryRequest(InventoryRequestType.BUY, item, buyQuantity, buyPrice);
            LOG.debug("BUY Request: " + buy);
            Future<Object> buyPos = Patterns.ask(mediator, new DistributedPubSubMediator.Publish(buyFrom, buy), Timeout.apply(1, TimeUnit.SECONDS));
            InventoryRequest buyResponse = (InventoryRequest) Await.result(buyPos, Duration.apply(1, TimeUnit.SECONDS));
            if (buyResponse.quantity == 0) {
                return false;
            }

            Double cash = shipInventory.cash - (buyResponse.quantity * buyResponse.price);

            Integer existingQuantity = shipInventory.inventory.get(item);
            if (existingQuantity == null) {
                existingQuantity = 0;
            }
            shipInventory = shipInventory.withItem(item, existingQuantity + buyResponse.quantity);
            shipInventory = shipInventory.withCash(cash);
            return true;
        } catch (Exception ex) {
            LOG.error(ex, "Error");
        }
        return false;
    }

    private void sellMax(String sellFrom, Item item, double sellPrice) {
        try {
            Integer existingQuantity = shipInventory.inventory.get(item);
            if (existingQuantity == null) {
                existingQuantity = 0;
            }
            Integer sellQuantity = existingQuantity;

            InventoryRequest sell = new InventoryRequest(InventoryRequestType.SELL, item, sellQuantity, sellPrice);
            LOG.debug("SELL Request: " + sell);
            Future<Object> sellPos = Patterns.ask(mediator, new DistributedPubSubMediator.Publish(sellFrom, sell), Timeout.apply(1, TimeUnit.SECONDS));
            InventoryRequest sellResponse = (InventoryRequest) Await.result(sellPos, Duration.apply(1, TimeUnit.SECONDS));
            LOG.debug("SELL Response: " + sellResponse);

            Double cash = shipInventory.cash + (sellResponse.quantity * sellResponse.price);

            shipInventory = shipInventory.withItem(item, existingQuantity - sellResponse.quantity);
            shipInventory = shipInventory.withCash(cash);
        } catch (Exception ex) {
            LOG.error(ex, "Error");
        }
    }

    private Integer maxBuyableQuantity(double buyPrice) {
        Double cash = shipInventory.cash;
        Integer spareInventory = shipInventory.maxInventory - shipInventory.totalInventory;
        Integer buyQuantity = spareInventory;
        if ((buyQuantity * buyPrice) > cash) {
            buyQuantity = (int) Math.floor(cash / buyPrice);
        }
        return buyQuantity;
    }
}
