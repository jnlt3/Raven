package dsekercioglu.mega.rMove.ws.path.aacalc;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.sim.Wave;


import java.awt.geom.Point2D;
import java.util.function.Function;

public class FancyDistancer extends AttackAngleCalculator {

    private static final double WALL_MARGIN = 20;
    private static final double AGGRESSION = 1.2;
    private static final double PREFERRED_DISTANCE = 450;
    private static final double WALL_STICK_RADIUS = 120;//Larger Turns allow more mobility near corners
    //private static final double WALL_STICK_RADIUS = 114.5450131316624;//Turn Circle Radius
    private static final double HALF_PI = Math.PI * 0.5;
    private final double BATTLE_FIELD_WIDTH;
    private final double BATTLE_FIELD_HEIGHT;

    public FancyDistancer(double battleFieldWidth, double battleFieldHeight) {
        BATTLE_FIELD_WIDTH = battleFieldWidth;
        BATTLE_FIELD_HEIGHT = battleFieldHeight;
    }

    public double getAttackAngle(Wave wave, Point2D.Double location, Point2D.Double enemyLocation, double currentAngle, double targetVelocity) {
        Point2D.Double midPoint = new Point2D.Double((wave.getSource().x + enemyLocation.x) / 2, (wave.getSource().y + enemyLocation.y) / 2);
        double targetAngle = MoveUtils.absoluteBearing(midPoint, location) + Math.PI / 2;
        double dir = Math.signum(targetVelocity);
        if (dir != 0) {
            double targetFix = (dir - 1) * HALF_PI;
            targetAngle = targetAngle + targetFix;
            double distance = location.distance(enemyLocation);
            double extraAngle = (distance - PREFERRED_DISTANCE) / PREFERRED_DISTANCE * AGGRESSION;
            double distancingAngle = targetAngle + dir * extraAngle;
            if (MoveUtils.distanceToWall(
                    location.x,
                    location.y,
                    BATTLE_FIELD_WIDTH,
                    BATTLE_FIELD_HEIGHT) <= WALL_STICK_RADIUS + WALL_MARGIN) {

                Point2D.Double nextLocation = MoveUtils.project(location, distancingAngle, 8);
                double position = BATTLE_FIELD_HEIGHT - nextLocation.y - WALL_MARGIN;
                if (dir == 1) {
                    //North
                    if (smoothNorth(position, distancingAngle, dir)) {
                        double eastPosition = BATTLE_FIELD_WIDTH - nextLocation.x - WALL_MARGIN;
                        if (smoothEast(eastPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothEast(eastPosition, dir);
                        }
                        return targetFix + exactSmoothNorth(position, dir);
                    }
                    //East
                    position = BATTLE_FIELD_WIDTH - nextLocation.x - WALL_MARGIN;
                    if (smoothEast(position, distancingAngle, dir)) {
                        double southPosition = nextLocation.y - WALL_MARGIN;
                        if (smoothSouth(southPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothSouth(southPosition, dir);
                        }
                        return targetFix + exactSmoothEast(position, dir);
                    }
                    //South
                    position = nextLocation.y - WALL_MARGIN;
                    if (smoothSouth(position, distancingAngle, dir)) {
                        double westPosition = nextLocation.x - WALL_MARGIN;
                        if (smoothWest(westPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothWest(westPosition, dir);
                        }
                        return targetFix + exactSmoothSouth(position, dir);
                    }
                    //West
                    position = nextLocation.x - WALL_MARGIN;
                    if (smoothWest(position, distancingAngle, dir)) {
                        double northPosition = BATTLE_FIELD_HEIGHT - nextLocation.y - WALL_MARGIN;
                        if (smoothNorth(northPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothNorth(northPosition, dir);
                        }
                        return targetFix + exactSmoothWest(position, dir);
                    }
                } else {
                    //North
                    if (smoothNorth(position, distancingAngle, dir)) {
                        double westPosition = nextLocation.x - WALL_MARGIN;
                        if (smoothWest(westPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothWest(westPosition, dir);
                        }
                        return targetFix + exactSmoothNorth(position, dir);
                    }
                    //West
                    position = nextLocation.x - WALL_MARGIN;
                    if (smoothWest(position, distancingAngle, dir)) {
                        double southPosition = nextLocation.y - WALL_MARGIN;
                        if (smoothSouth(southPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothSouth(southPosition, dir);
                        }
                        return targetFix + exactSmoothWest(position, dir);
                    }
                    //South
                    position = nextLocation.y - WALL_MARGIN;
                    if (smoothSouth(position, distancingAngle, dir)) {
                        double eastPosition = BATTLE_FIELD_WIDTH - nextLocation.x - WALL_MARGIN;
                        if (smoothEast(eastPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothEast(eastPosition, dir);
                        }
                        return targetFix + exactSmoothSouth(position, dir);
                    }
                    //East
                    position = BATTLE_FIELD_WIDTH - nextLocation.x - WALL_MARGIN;
                    if (smoothEast(position, distancingAngle, dir)) {
                        double northPosition = BATTLE_FIELD_HEIGHT - nextLocation.y - WALL_MARGIN;
                        if (smoothNorth(northPosition, distancingAngle, dir)) {
                            return targetFix + exactSmoothNorth(northPosition, dir);
                        }
                        return targetFix + exactSmoothEast(position, dir);
                    }
                }
            }
            return distancingAngle;
        }
        return targetAngle;
    }

    private double exactSmoothNorth(double y, double dir) {
        return dir * doSmoothSouth(y, dir) + Math.PI;
    }

    private double exactSmoothWest(double y, double dir) {
        return dir * doSmoothSouth(y, dir) + HALF_PI;
    }

    private double exactSmoothSouth(double y, double dir) {
        return dir * doSmoothSouth(y, dir);
    }

    private double exactSmoothEast(double y, double dir) {
        return dir * doSmoothSouth(y, dir) - HALF_PI;
    }

    private boolean smoothNorth(double position, double angle, double dir) {
        return smooth(position, angle - Math.PI, dir);
    }

    private boolean smoothWest(double position, double angle, double dir) {
        return smooth(position, angle - HALF_PI, dir);
    }

    private boolean smoothSouth(double position, double angle, double dir) {
        return smooth(position, angle, dir);
    }

    private boolean smoothEast(double position, double angle, double dir) {
        return smooth(position, angle + HALF_PI, dir);
    }

    private boolean smooth(double position, double angle, double dir) {
        double moveY = position + FastMath.cos(angle) * WALL_STICK_RADIUS;
        if (moveY <= 0) {
            angle += HALF_PI * dir;
            double rotateY = position + FastMath.cos(angle) * WALL_STICK_RADIUS;
            return rotateY <= WALL_STICK_RADIUS;
        }
        return false;
    }

    private double doSmoothSouth(double position, double dir) {
        position = MoveUtils.limit(0, position, WALL_STICK_RADIUS);
        return -dir * HALF_PI - FastMath.acos(1 - position / WALL_STICK_RADIUS);
    }
}
