package com.owl.cocoa.ship;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import common.SpacePosition;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

public class ShipActor extends UntypedActor {

    public static final String START = "start";
    public static final String TICK = "tick";

    private SpacePosition position = new SpacePosition("sector1").withRadius(5);

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
            }
        }
    }
    private Cancellable cancellable;
    private ActorRef mediator;

    private void start() {
        mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
        cancellable = this.context().system().scheduler().schedule(Duration.Zero(),
                                                                   Duration.create(20, TimeUnit.MILLISECONDS), this.getSelf(), TICK,
                                                                   this.context().system().dispatcher(), null);
    }

    private void tick() {
        double x = position.x + 1;
        double y = position.y + 1;
        double z = position.z + 1;
        position = position.withPosition(x, y, z);
        mediator.tell(new DistributedPubSubMediator.Publish("scene", position), getSelf());
    }

}
