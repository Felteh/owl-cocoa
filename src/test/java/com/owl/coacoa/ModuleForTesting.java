package com.owl.coacoa;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;

public class ModuleForTesting extends AbstractModule {

    @Override
    protected void configure() {
        install(Modules.override(new Module()).with());
    }

}
