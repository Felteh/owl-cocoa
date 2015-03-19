package com.owl.cocoa.scene;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import common.SpacePosition;

public class SceneActor extends UntypedActor {

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
            System.out.println("Subscribed");
        } else if (o instanceof SpacePosition) {
            SpacePosition p = (SpacePosition) o;
            SectorData sD = sceneData.sectorData.get(p.sector);
            if (sD == null) {
                sD = new SectorData();
                sceneData = sceneData.withSectorData(p.sector, sD);
            }
            sD = sD.withSpacePosition(p);

            sceneData = sceneData.withSectorData(p.sector, sD);
        }
    }

    private void start() {
        ActorRef mediator
                 = DistributedPubSubExtension.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe("scene", getSelf()),
                      getSelf());
    }

}
