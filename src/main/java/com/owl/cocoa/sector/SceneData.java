package com.owl.cocoa.sector;

import com.owl.cocoa.ship.ShipPosition;
import java.util.HashMap;
import java.util.Map;

public class SceneData {

    public final Map<String, ShipPosition> shipPositions;

    public SceneData() {
        shipPositions = new HashMap<>();
    }

    public SceneData(Map<String, ShipPosition> shipPositions) {
        this.shipPositions = shipPositions;
    }

    public SceneData withShipPosition(String key, ShipPosition pos) {
        shipPositions.put(key, pos);
        return new SceneData(shipPositions);
    }
}
