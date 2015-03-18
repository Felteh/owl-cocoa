package com.owl.cocoa.ship;

import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

public class ShipActor extends UntypedActor {

    public static final String START = "start";
    public static final String TICK = "tick";
    public static final String GET_POSITION = "getPos";

    private ShipPosition position = new ShipPosition();

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String) {
            switch ((String) o) {
                case START:
                    start();
                    break;
                case TICK:
                    tick();
                    break;
                case GET_POSITION:
                    getSender().tell(position, this.getSelf());
                    break;
            }
        }
    }
    private Cancellable cancellable;

    private void start() {
        cancellable = this.context().system().scheduler().schedule(Duration.Zero(),
                                                                   Duration.create(50, TimeUnit.MILLISECONDS), this.getSelf(), TICK,
                                                                   this.context().system().dispatcher(), null);
    }

    private void tick() {
        double x = Math.random() * 100;
        double y = Math.random() * 100;
        double z = Math.random() * 100;
        position = new ShipPosition(x, y, z);
    }

}
