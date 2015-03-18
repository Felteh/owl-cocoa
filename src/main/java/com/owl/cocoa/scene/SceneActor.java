package com.owl.cocoa.scene;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.owl.cocoa.sector.SceneData;
import com.owl.cocoa.sector.SectorActor;
import com.owl.cocoa.ship.ShipActor;
import com.owl.cocoa.ship.ShipPosition;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class SceneActor extends UntypedActor {

    public static final String START = "start";
    public static final String PING = "ping";

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String) {
            switch ((String) o) {
                case START:
                    start();
                    break;
                case PING:
                    Patterns.pipe(getSceneData(), this.context().dispatcher()).to(getSender());
                    break;
            }
        }
    }

    private final Map<String, ActorRef> sectors = new HashMap<>();
    private final Map<String, ActorRef> ships = new HashMap<>();

    private Future<SceneData> getSceneData() {
        Map<String, Future<Object>> shipPoss = new HashMap<>();
        for (Entry<String, ActorRef> entry : ships.entrySet()) {
            shipPoss.put(entry.getKey(), Patterns.ask(entry.getValue(), ShipActor.GET_POSITION, Timeout.apply(1, TimeUnit.SECONDS)));
        }

        return Futures.future(new Callable<SceneData>() {

            @Override
            public SceneData call() throws Exception {
                SceneData data = new SceneData();
                for (Entry<String, Future<Object>> e : shipPoss.entrySet()) {
                    try {
                        ShipPosition pos = (ShipPosition) Await.result(e.getValue(), Timeout.apply(5, TimeUnit.SECONDS).duration());
                        data = data.withShipPosition(e.getKey(), pos);
                    } catch (Exception ex) {
                        Logger.getLogger(SceneActor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                return data;
            }
        }, this.context().dispatcher());
    }

    private void start() {
        sectors.put("sector1", this.context().actorOf(Props.create(SectorActor.class), "sector1"));

        for (ActorRef r : sectors.values()) {
            r.tell(SectorActor.START, this.getSelf());
        }

        ships.put("ship1", this.context().actorOf(Props.create(ShipActor.class), "ship1"));

        for (ActorRef r : ships.values()) {
            r.tell(ShipActor.START, this.getSelf());
        }
    }

}
