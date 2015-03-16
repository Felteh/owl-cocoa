package com.owl.coacoa;

import com.googlecode.guicebehave.Modules;
import com.googlecode.guicebehave.Story;
import com.googlecode.guicebehave.StoryRunner;
import org.junit.runner.RunWith;

@RunWith(StoryRunner.class)
@Modules(ModuleForTesting.class)
public class MainTest {

    @Story
    public void testStartup() {

    }

}
