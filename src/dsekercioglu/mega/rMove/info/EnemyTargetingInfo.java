package dsekercioglu.mega.rMove.info;

import java.awt.geom.Point2D;

public class EnemyTargetingInfo {

    private final Point2D.Double SOURCE;
    private final double ABSOLUTE_BEARING;

    public EnemyTargetingInfo(Point2D.Double source, double absoluteBearing) {
        SOURCE = source;
        ABSOLUTE_BEARING = absoluteBearing;
    }

    public Point2D.Double getSource() {
        return SOURCE;
    }

    public double getAbsoluteBearing() {
        return ABSOLUTE_BEARING;
    }
}
