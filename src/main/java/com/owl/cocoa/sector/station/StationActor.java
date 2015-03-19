package com.owl.cocoa.sector.station;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import common.SpacePosition;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

public class StationActor extends UntypedActor {

    public static final String START = "start";
    public static final String TICK = "tick";
    public static final String GET_POSITION = "getPos";

    private final SpacePosition position = new SpacePosition("sector1").withRadius(20).withPosition(Math.random() * 200, Math.random() * 200, Math.random() * 200);

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
    private ActorRef mediator;

    private void start() {
        mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Publish("scene", position), getSelf());

        cancellable = this.context().system().scheduler().schedule(Duration.Zero(),
                                                                   Duration.create(20, TimeUnit.MILLISECONDS), this.getSelf(), TICK,
                                                                   this.context().system().dispatcher(), null);
    }

    private void tick() {

    }

}
