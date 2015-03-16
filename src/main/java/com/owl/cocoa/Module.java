package com.owl.cocoa;

import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.owl.cocoa.akka.ActorSystemProvider;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActorSystem.class).toProvider(ActorSystemProvider.class).asEagerSingleton();
    }

}
