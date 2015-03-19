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
import com.owl.cocoa.sector.station.StationActor;
import com.owl.cocoa.sector.station.types.FactoryStationActor;
import com.owl.cocoa.sector.station.types.ProducingStationActor;
import com.owl.cocoa.ship.ShipActor;
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

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(5), (ev) -> {
            List<SceneData> toRender = new ArrayList<>();
            queue.drainTo(toRender);

            for (final SceneData a : toRender) {
                for (Entry<String, SectorData> sector : a.sectorData.entrySet()) {
                    for (Entry<String, SpacePosition> entity : sector.getValue().spacePositions.entrySet()) {
                        Group g = entityGroups.get(entity.getKey());
                        if (g == null) {
                            Entity sphere = new Entity(entity.getValue().objectName, entity.getValue().radius);
                            g = new Group(sphere);
                            sphere.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                @Override
                                public void handle(MouseEvent event) {
                                    String objectName = ((Entity) event.getPickResult().getIntersectedNode()).objectName;
                                    System.out.println(a.inventory.get(objectName));
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

    private void createSectors() {
        List<ActorRef> sectors = new ArrayList<>();
        sectors.add(akka.actorOf(Props.create(SectorActor.class), "sector1"));

        for (ActorRef r : sectors) {
            r.tell(SectorActor.START, ActorRef.noSender());
        }
    }

    private void createStations() {
        List<ActorRef> stations = new ArrayList<>();
        stations.add(akka.actorOf(Props.create(ProducingStationActor.class), "station1"));
        stations.add(akka.actorOf(Props.create(ProducingStationActor.class), "station2"));
        stations.add(akka.actorOf(Props.create(FactoryStationActor.class), "station3"));
        stations.add(akka.actorOf(Props.create(FactoryStationActor.class), "station4"));

        stations.get(0).tell(new SpacePosition(null, "sector1").withPosition(0, 0, 0), ActorRef.noSender());
        stations.get(1).tell(new SpacePosition(null, "sector1").withPosition(100, 100, 200), ActorRef.noSender());
        stations.get(2).tell(new SpacePosition(null, "sector1").withPosition(300, 300, 100), ActorRef.noSender());
        stations.get(3).tell(new SpacePosition(null, "sector1").withPosition(-150, 200, 75), ActorRef.noSender());

        for (ActorRef r : stations) {
            r.tell(StationActor.START, ActorRef.noSender());
        }
    }

    private void createShips() {
        List<ActorRef> ships = new ArrayList<>();
        ships.add(akka.actorOf(Props.create(ShipActor.class), "ship1"));

        for (ActorRef r : ships) {
            r.tell(ShipActor.START, ActorRef.noSender());
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

}
