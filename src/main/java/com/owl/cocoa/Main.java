package com.owl.cocoa;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.owl.cocoa.scene.SceneActor;
import com.owl.cocoa.sector.SceneData;
import com.owl.cocoa.ship.ShipPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import javafx.util.Duration;
import scala.concurrent.Future;

public class Main extends Application {

    private final Group root = new Group();
    private Injector injector;
    private ActorSystem akka;
    private ActorRef sceneRef;

    private final Map<String, Group> shipGroups = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        createInjector();
        createActors();
        pollSceneActor();

        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ev) -> {
            List<SceneData> toRender = new ArrayList<>();
            queue.drainTo(toRender);

            for (SceneData a : toRender) {
                for (Entry<String, ShipPosition> ship : a.shipPositions.entrySet()) {
                    Group g = shipGroups.get(ship.getKey());
                    if (g == null) {
                        Sphere sphere = new Sphere(50, 25);
                        g = new Group(sphere);
                        root.getChildren().add(g);
                        shipGroups.put(ship.getKey(), g);
                    }

                    g.setTranslateX((scene.getWidth() / 2) + ship.getValue().x);
                    g.setTranslateY((scene.getHeight() / 2) + ship.getValue().y);
                    g.setTranslateZ(ship.getValue().z);
                    System.out.println(ship.getKey() + " at " + ship.getValue().x + " " + ship.getValue().y + " " + ship.getValue().z);
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void createInjector() {
        injector = Guice.createInjector(new Module());
        akka = injector.getInstance(ActorSystem.class);
    }

    private void createActors() {
        sceneRef = akka.actorOf(Props.create(SceneActor.class), "Scene");
        sceneRef.tell(SceneActor.START, ActorRef.noSender());
    }
    private static final ArrayBlockingQueue<SceneData> queue = new ArrayBlockingQueue<>(50);

    private void pollSceneActor() {
        Thread t = new Thread() {

            @Override
            public void run() {
                while (true) {
                    Future<Object> ask = Patterns.ask(sceneRef, SceneActor.PING, Timeout.apply(1, TimeUnit.SECONDS));
                    ask.onSuccess(new OnSuccess<Object>() {

                        @Override
                        public void onSuccess(Object t) throws Throwable {
                            SceneData data = (SceneData) t;
                            queue.add(data);
                        }
                    }, ExecutionContexts.global());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        System.out.println("Error" + ex.getMessage());
                    }
                }
            }

        };
        t.setDaemon(true);
        t.start();
    }
}
