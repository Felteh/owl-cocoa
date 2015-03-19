package com.owl.cocoa.akka;

import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.inject.Provider;

public class ActorSystemProvider implements Provider<ActorSystem> {

    @Override
    public ActorSystem get() {
        Config akkaConfig = ConfigFactory.load("application.conf");
        ExtendedActorSystem system = (ExtendedActorSystem) ActorSystem.create("systemName", akkaConfig);
        return system;
    }

}
