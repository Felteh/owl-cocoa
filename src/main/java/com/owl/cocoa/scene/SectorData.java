package com.owl.cocoa.scene;

import com.owl.cocoa.common.SpacePosition;
import java.util.HashMap;
import java.util.Map;

public class SectorData {

    public final Map<String, SpacePosition> spacePositions;

    public SectorData() {
        spacePositions = new HashMap<>();
    }

    private SectorData(Map<String, SpacePosition> spacePositions) {
        this.spacePositions = spacePositions;
    }

    public SectorData withSpacePosition(SpacePosition pos) {
        spacePositions.put(pos.objectName, pos);
        return new SectorData(spacePositions);
    }
}
