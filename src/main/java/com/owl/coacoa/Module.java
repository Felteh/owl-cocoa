package com.owl.coacoa;

import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.owl.coacoa.akka.ActorSystemProvider;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActorSystem.class).toProvider(ActorSystemProvider.class).asEagerSingleton();
    }

}
