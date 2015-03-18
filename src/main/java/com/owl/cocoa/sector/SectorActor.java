package com.owl.cocoa.sector;

import akka.actor.UntypedActor;

public class SectorActor extends UntypedActor {

    public static final String START = "start";

    @Override
    public void onReceive(Object o) throws Exception {
        System.out.println("Sector:" + o);
    }

}
