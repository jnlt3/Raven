package dsekercioglu.mega.rMove.info.battle;

import java.awt.geom.Point2D;

public class BattleSummary {


    Point2D.Double botLocation;

    double distance;

    double botVelocity;
    double botBearing;

    double botLateralVelocity;
    double botLateralDirection;
    double botLateralAcceleration;
    double botAdvancingVelocity;

    double forwardWallMEA;
    double backwardWallMEA;

    double botTimeSinceDeceleration;
    double botTimeSinceDirectionChange;
    double enemyWeightedHitRate;

    double battleFieldWidth;
    double battleFieldHeight;

    public Point2D.Double getBotLocation() {
        return botLocation;
    }

    public double getDistance() {
        return distance;
    }

    public double getBotVelocity() {
        return botVelocity;
    }

    public double getBotBearing() {
        return botBearing;
    }

    public double getBotLateralVelocity() {
        return botLateralVelocity;
    }

    public double getBotLateralDirection() {
        return botLateralDirection;
    }

    public double getBotLateralAcceleration() {
        return botLateralAcceleration;
    }

    public double getBotAdvancingVelocity() {
        return botAdvancingVelocity;
    }

    public double getBotTimeSinceDeceleration() {
        return botTimeSinceDeceleration;
    }

    public double getBotTimeSinceDirectionChange() {
        return botTimeSinceDirectionChange;
    }

    public double getForwardWallMEA() {
        return forwardWallMEA;
    }

    public double getBackwardWallMEA() {
        return backwardWallMEA;
    }

    public double getEnemyWeightedHitRate() {
        return enemyWeightedHitRate;
    }

    public double getBattleFieldWidth() {
        return battleFieldWidth;
    }

    public double getBattleFieldHeight() {
        return battleFieldHeight;
    }
}
