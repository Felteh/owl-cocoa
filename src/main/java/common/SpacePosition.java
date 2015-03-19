package common;

import java.util.UUID;

public class SpacePosition {

    public final String objectName;
    public final String sector;
    public final double x;
    public final double y;
    public final double z;
    public final double radius;

    public SpacePosition(String sector) {
        objectName = UUID.randomUUID().toString();
        this.sector = sector;
        x = 0;
        y = 0;
        z = 0;
        radius = 0;
    }

    private SpacePosition(String objectName, String sector, double x, double y, double z, double radius) {
        this.objectName = objectName;
        this.sector = sector;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    public SpacePosition withPosition(double x, double y, double z) {
        return new SpacePosition(objectName, sector, x, y, z, radius);
    }

    public SpacePosition withRadius(double radius) {
        return new SpacePosition(objectName, sector, x, y, z, radius);
    }

    public SpacePosition withSector(String sector) {
        return new SpacePosition(objectName, sector, x, y, z, radius);
    }
}
