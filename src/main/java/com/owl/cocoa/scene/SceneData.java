package com.owl.cocoa.scene;

import java.util.HashMap;
import java.util.Map;

public class SceneData {

    public final Map<String, SectorData> sectorData;

    public SceneData() {
        sectorData = new HashMap<>();
    }

    public SceneData(Map<String, SectorData> sectorData) {
        this.sectorData = sectorData;
    }

    public SceneData withSectorData(String key, SectorData pos) {
        sectorData.put(key, pos);
        return new SceneData(sectorData);
    }
}
