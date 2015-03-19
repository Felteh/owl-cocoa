package com.owl.cocoa.sector;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SectorActor extends UntypedActor {

    public static final String START = "start";
    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object o) throws Exception {
        LOG.info("Sector:" + o);
    }

}
