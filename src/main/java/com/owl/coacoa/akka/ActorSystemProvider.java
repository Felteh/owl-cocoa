package com.owl.coacoa.akka;

import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import javax.inject.Provider;

public class ActorSystemProvider implements Provider<ActorSystem> {

    @Override
    public ActorSystem get() {
        ExtendedActorSystem system = (ExtendedActorSystem) ActorSystem.create("systemName");
        return system;
    }

}
