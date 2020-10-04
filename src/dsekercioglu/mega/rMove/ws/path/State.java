package dsekercioglu.mega.rMove.ws.path;


import dsekercioglu.mega.rMove.MoveUtils;

import java.awt.geom.Point2D;
import java.util.List;

import static robocode.util.Utils.normalRelativeAngle;

public class State {

    private static final double ROBOT_MAX_VELOCITY = 8;

    private static final double PI = Math.PI;
    private static final double HALF_PI = Math.PI / 2;

    private final Point2D.Double LOCATION;
    private final double VELOCITY;
    private final double HEADING;
    private final int DELTA_TIME;
    private boolean intersecting;
    private double maxVelocity;

    List<Point2D.Double> intersections;

    public State(Point2D.Double location, double velocity, double heading, int deltaTime) {
        LOCATION = location;
        VELOCITY = velocity;
        HEADING = heading;
        DELTA_TIME = deltaTime;
        maxVelocity = 8;
    }

    public void setIntersecting(boolean intersecting) {
        this.intersecting = intersecting;
    }

    public boolean isIntersecting() {
        return intersecting;
    }

    public void setIntersection(List<Point2D.Double> intersections) {
        this.intersections = intersections;
    }

    public List<Point2D.Double> getIntersection() {
        return intersections;
    }

    public Point2D.Double getLocation() {
        return LOCATION;
    }

    public double getVelocity() {
        return VELOCITY;
    }

    public double getHeading() {
        return HEADING;
    }

    public int getDeltaTime() {
        return DELTA_TIME;
    }

    public void setMaxVelocity(double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public State moveTowards(double angleTo) {
        double turnAmount = normalRelativeAngle(angleTo - HEADING);

        boolean backAsForward = Math.abs(turnAmount) >= HALF_PI;
        double targetVelocity = backAsForward ? -maxVelocity : maxVelocity;
        double targetHeading = backAsForward ? angleTo + PI : angleTo;
        double nextHeading = getNextHeading(VELOCITY, HEADING, targetHeading);
        double nextVelocity = getNextVelocity(VELOCITY, targetVelocity);
        Point2D.Double nextLocation = MoveUtils.project(LOCATION, nextHeading, nextVelocity);
        State nextState = new State((Point2D.Double) nextLocation.clone(), nextVelocity, nextHeading, DELTA_TIME + 1);
        nextState.setMaxVelocity(maxVelocity);
        return nextState;
    }

    public State moveTo(Point2D.Double targetLocation) {
        double angleTo = MoveUtils.absoluteBearing(LOCATION, targetLocation);
        return moveTowards(angleTo);
    }

    public State brake() {
        double nextVelocity = decelerate(VELOCITY);
        Point2D.Double nextLocation = MoveUtils.project(LOCATION, HEADING, nextVelocity);
        return new State((Point2D.Double) nextLocation.clone(), nextVelocity, HEADING, DELTA_TIME + 1);
    }

    public int stopTime() {
        return (int) Math.ceil(Math.abs(VELOCITY) / 2);
    }

    private static double accelerate(double velocity, double preferredDirection) {
        return MoveUtils.limit(-ROBOT_MAX_VELOCITY, velocity + (velocity > 0 ? 1 : (velocity < 0 ? -1 : preferredDirection)), ROBOT_MAX_VELOCITY);
    }

    private static double decelerate(double velocity) {
        return Math.abs(velocity) <= 2 ? 0 : velocity + (velocity > 0 ? -2 : (velocity < 0 ? 2 : 0));
    }

    private static double getNextVelocity(double velocity, double target) {
        double velocitySign = Math.signum(velocity);
        double targetSign = Math.signum(target);
        boolean sameSign = targetSign == velocitySign;
        if (target == 0 || (!sameSign && velocitySign != 0)) {
            return decelerate(velocity);
        }
        double difference = target - velocity;
        boolean possiblyInRange = Math.abs(difference) <= 2;
        double differenceSign = Math.signum(difference);
        if (possiblyInRange) {
            double signedDifference = difference * differenceSign;
            if (signedDifference <= 1 && signedDifference >= -2) {
                return target;
            }
        }
        return accelerate(velocity, differenceSign);
    }

    private static double getNextHeading(double velocity, double heading, double target) {
        double maxTurn = Math.toRadians(10 - 0.75 * Math.abs(velocity));
        return heading + MoveUtils.limit(-maxTurn, normalRelativeAngle(target - heading), maxTurn);
    }

    @Override
    public String toString() {
        return "State{" +
                "LOCATION=" + LOCATION +
                ", VELOCITY=" + VELOCITY +
                ", HEADING=" + HEADING +
                ", intersecting=" + intersecting +
                '}';
    }
}

