package com.owl.cocoa.scene;

import javafx.scene.shape.Sphere;

public class Entity extends Sphere {

    public final String objectName;

    public Entity(String objectName, double radius) {
        super(radius, 25);
        this.objectName = objectName;
    }

}
