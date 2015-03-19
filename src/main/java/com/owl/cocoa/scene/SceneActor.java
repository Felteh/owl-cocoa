package com.owl.cocoa.scene;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.owl.cocoa.common.Inventory;
import com.owl.cocoa.common.SpacePosition;

public class SceneActor extends UntypedActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);
    public static final String START = "start";
    public static final String GET_SCENE_DATA = "getSceneData";

    private SceneData sceneData = new SceneData();

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String) {
            switch ((String) o) {
                case START:
                    start();
                    break;
                case GET_SCENE_DATA:
                    this.getSender().tell(sceneData, getSelf());
                    break;
            }
        } else if (o instanceof DistributedPubSubMediator.SubscribeAck) {
            LOG.info("Subscribed");
        } else if (o instanceof SpacePosition) {
            SpacePosition p = (SpacePosition) o;
            SectorData sD = sceneData.sectorData.get(p.sector);
            if (sD == null) {
                sD = new SectorData();
                sceneData = sceneData.withSectorData(p.sector, sD);
            }
            sD = sD.withSpacePosition(p);

            sceneData = sceneData.withSectorData(p.sector, sD);
        } else if (o instanceof Inventory) {
            Inventory p = (Inventory) o;
            sceneData = sceneData.withInventoryData(p.objectName, p);
        } else {
            LOG.info("Unhandled:" + o);
        }
    }

    public static final String SCENE_EVENTS = "scene";

    private void start() {
        ActorRef mediator
                = DistributedPubSubExtension.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(SCENE_EVENTS, getSelf()),
                getSelf());
    }

}
