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
import com.owl.cocoa.common.SpacePosition;
import com.owl.cocoa.scene.Entity;
import com.owl.cocoa.scene.SceneActor;
import com.owl.cocoa.scene.SceneData;
import com.owl.cocoa.scene.SectorData;
import com.owl.cocoa.sector.SectorActor;
import com.owl.cocoa.sector.station.types.FactoryStationActor;
import com.owl.cocoa.sector.station.types.ProducingStationActor;
import com.owl.cocoa.ship.ShipActor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import scala.concurrent.Future;

public class Main extends Application {

    private static final ArrayBlockingQueue<SceneData> queue = new ArrayBlockingQueue<>(50);

    public static void main(String[] args) {
        launch(args);
    }

    private final Group root = new Group();
    private Injector injector;
    private ActorSystem akka;
    private ActorRef sceneRef;

    private final Map<String, Group> entityGroups = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        createInjector();
        createActors();
        pollSceneActor();
        createSectors();
        createStations();
        createShips();

        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SceneData lastRender = null;
                while (queue.peek() != null) {
                    lastRender = queue.poll();
                }

                if (lastRender != null) {
                    final SceneData toRender = lastRender;
                    for (Entry<String, SectorData> sector : toRender.sectorData.entrySet()) {
                        for (Entry<String, SpacePosition> entity : sector.getValue().spacePositions.entrySet()) {
                            Group g = entityGroups.get(entity.getKey());
                            if (g == null) {
                                Entity sphere = new Entity(entity.getValue().objectName, entity.getValue().radius);
                                g = new Group(sphere);
                                sphere.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                    @Override
                                    public void handle(MouseEvent event) {
                                        String objectName = ((Entity) event.getPickResult().getIntersectedNode()).objectName;
                                        System.out.println(toRender.inventory.get(objectName));
                                    }
                                });
                                root.getChildren().add(g);
                                entityGroups.put(entity.getKey(), g);
                            }

                            g.setTranslateX((scene.getWidth() / 2) + entity.getValue().x);
                            g.setTranslateY((scene.getHeight() / 2) + entity.getValue().y);
                            g.setTranslateZ(entity.getValue().z);
                        }
                    }
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
    }

    private void createSectors() {
        List<ActorRef> sectors = new ArrayList<>();
        sectors.add(akka.actorOf(Props.create(SectorActor.class), "sector1"));
    }

    private void createStations() {
        for (int i = 0; i < 8; i++) {
            createProducingStation(i);
        }
        for (int i = 0; i < 5; i++) {
            createFactoryStation(i);
        }
    }

    private void createShips() {
        for (int i = 0; i < 350; i++) {
            createShip(i);
        }
    }

    private void pollSceneActor() {
        Thread t = new Thread() {

            @Override
            public void run() {
                while (true) {
                    Future<Object> ask = Patterns.ask(sceneRef, SceneActor.GET_SCENE_DATA, Timeout.apply(1, TimeUnit.SECONDS));
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

    private void createProducingStation(int i) {
        ActorRef r = akka.actorOf(Props.create(ProducingStationActor.class, new SpacePosition(UUID.randomUUID().toString(), "sector1").withPosition(400 - (Math.random() * 800), 400 - (Math.random() * 800), 400 - (Math.random() * 800)).withRadius(25)), "producerStation" + i);
    }

    private void createFactoryStation(int i) {
        ActorRef r = akka.actorOf(Props.create(FactoryStationActor.class, new SpacePosition(UUID.randomUUID().toString(), "sector1").withPosition(400 - (Math.random() * 800), 400 - (Math.random() * 800), 400 - (Math.random() * 800)).withRadius(15)), "factoryStation" + i);
    }

    private void createShip(int i) {
        akka.actorOf(Props.create(ShipActor.class), "ship" + i);
    }

}
