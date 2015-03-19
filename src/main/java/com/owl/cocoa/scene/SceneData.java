package com.owl.cocoa.scene;

import com.owl.cocoa.common.Inventory;
import java.util.HashMap;
import java.util.Map;

public class SceneData {

    public final Map<String, SectorData> sectorData;
    public final Map<String, Inventory> inventory;

    public SceneData() {
        sectorData = new HashMap<>();
        inventory = new HashMap<>();
    }

    public SceneData(Map<String, SectorData> sectorData, Map<String, Inventory> inventory) {
        this.sectorData = sectorData;
        this.inventory = inventory;
    }

    public SceneData withSectorData(String key, SectorData pos) {
        Map<String, SectorData> sectorD = new HashMap<>(sectorData);
        sectorD.put(key, pos);
        return new SceneData(sectorD, inventory);
    }

    public SceneData withInventoryData(String key, Inventory iv) {
        Map<String, Inventory> ivD = new HashMap<>(inventory);
        ivD.put(key, iv);
        return new SceneData(sectorData, ivD);
    }
}
