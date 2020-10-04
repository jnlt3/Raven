package dsekercioglu.mega.rMove.ws.path.aacalc;

import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.sim.Wave;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class MEAMaximizer extends AttackAngleCalculator {

    private static final double WALL_MARGIN = 18.1;
    private static final double PREFERRED_DISTANCE = 450;
    private static final double AGGRESSION = 1;
    private static final double WALL_STICK_LENGTH = 140;
    private final double BATTLE_FIELD_WIDTH;
    private final double BATTLE_FIELD_HEIGHT;

    public MEAMaximizer(double battleFieldWidth, double battleFieldHeight) {
        BATTLE_FIELD_WIDTH = battleFieldWidth;
        BATTLE_FIELD_HEIGHT = battleFieldHeight;
    }


    public double getAttackAngle(Wave wave, Point2D.Double location, Point2D.Double enemyLocation, double currentAngle, double targetVelocity) {
        double battleFieldWidth = BATTLE_FIELD_WIDTH;
        double battleFieldHeight = BATTLE_FIELD_HEIGHT;
        Rectangle2D.Double battleField = new Rectangle2D.Double(WALL_MARGIN, WALL_MARGIN, battleFieldWidth - WALL_MARGIN * 2, battleFieldHeight - WALL_MARGIN * 2);
        double targetAngle = wave.getAbsoluteBearing() + Math.PI / 2;
        double dir = Math.signum(targetVelocity);
        if (dir != 0) {
            double distanceDifference = location.distance(enemyLocation) - PREFERRED_DISTANCE;
            double extraAngle = distanceDifference / PREFERRED_DISTANCE * AGGRESSION;
            double wallStick = WALL_STICK_LENGTH;
            targetAngle += (dir - 1) * Math.PI / 2 + dir * extraAngle;
            Point2D.Double p = MoveUtils.project(location, targetAngle, wallStick);
            for (int i = 0; !battleField.contains(p) && i < 4; i++) {
                if (p.x < WALL_MARGIN) {
                    p.x = WALL_MARGIN;
                    double a = location.x - WALL_MARGIN;
                    p.y = location.y + dir * Math.sqrt(wallStick * wallStick - a * a);
                } else if (p.y > battleFieldHeight - WALL_MARGIN) {
                    p.y = battleFieldHeight - WALL_MARGIN;
                    double a = battleFieldHeight - WALL_MARGIN - location.y;
                    p.x = location.x + dir * Math.sqrt(wallStick * wallStick - a * a);
                } else if (p.x > battleFieldWidth - WALL_MARGIN) {
                    p.x = battleFieldWidth - WALL_MARGIN;
                    double a = battleFieldWidth - WALL_MARGIN - location.x;
                    p.y = location.y - dir * Math.sqrt(wallStick * wallStick - a * a);
                } else if (p.y < WALL_MARGIN) {
                    p.y = WALL_MARGIN;
                    double a = location.y - WALL_MARGIN;
                    p.x = location.x - dir * Math.sqrt(wallStick * wallStick - a * a);
                }
            }
            return MoveUtils.absoluteBearing(location, p);
        }
        return targetAngle;
    }
}
