package com.owl.cocoa.sector.station;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import com.owl.cocoa.common.Inventory;
import com.owl.cocoa.common.SpacePosition;
import com.owl.cocoa.scene.SceneActor;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

public abstract class StationActor extends UntypedActor {

    protected final String objectName = UUID.randomUUID().toString();
    public static final String START = "start";
    public static final String TICK = "tick";
    public static final String GET_POSITION = "getPos";

    private SpacePosition position = new SpacePosition(objectName, null).withRadius(20).withPosition(Math.random() * 200, Math.random() * 200, Math.random() * 200);
    protected Inventory inventory = new Inventory(objectName, 1000);

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
        } else if (o instanceof SpacePosition) {
            SpacePosition p = (SpacePosition) o;
            position = position.withPosition(p.x, p.y, p.z);
        }
    }
    private Cancellable cancellable;
    protected ActorRef mediator;

    private void start() {
        mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Publish(SceneActor.SCENE_EVENTS, position), getSelf());

        cancellable = this.context().system().scheduler().schedule(Duration.Zero(),
                Duration.create(100, TimeUnit.MILLISECONDS), this.getSelf(), TICK,
                this.context().system().dispatcher(), null);
    }

    protected abstract void tick();

}
