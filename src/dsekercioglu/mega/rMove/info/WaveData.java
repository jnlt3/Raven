package dsekercioglu.mega.rMove.info;

import dsekercioglu.mega.rMove.info.battle.BattleSummary;
import robocode.Rules;

public class WaveData {

    BattleSummary BATTLE_SUMMARY;

    private final double DISTANCE;
    private final double FIRE_POWER;
    private final double BULLET_SPEED;
    private final double BULLET_FLOAT_TIME;


    public WaveData(BattleSummary battleSummary, double firePower) {
        BATTLE_SUMMARY = battleSummary;
        DISTANCE = battleSummary.getDistance();
        FIRE_POWER = firePower;
        BULLET_SPEED = Rules.getBulletSpeed(FIRE_POWER);
        BULLET_FLOAT_TIME = DISTANCE / BULLET_SPEED;
    }

    public double getBotVelocity() {
        return BATTLE_SUMMARY.getBotVelocity();
    }

    public double getBotRelativeHeading() {
        return BATTLE_SUMMARY.getBotBearing();
    }

    public double getBotLateralVelocity() {
        return BATTLE_SUMMARY.getBotLateralVelocity();
    }

    public double getBotLateralAcceleration() {
        return BATTLE_SUMMARY.getBotLateralAcceleration();
    }

    public double getBotAdvancingVelocity() {
        return BATTLE_SUMMARY.getBotAdvancingVelocity();
    }

    public double getDistance() {
        return DISTANCE;
    }

    public double getFirePower() {
        return FIRE_POWER;
    }

    public double getBulletVelocity() {
        return BULLET_SPEED;
    }

    public double getBulletFloatTime() {
        return BULLET_FLOAT_TIME;
    }

    public double getForwardWallMEA() {
        return BATTLE_SUMMARY.getForwardWallMEA();
    }

    public double getBackwardWallMEA() {
        return BATTLE_SUMMARY.getBackwardWallMEA();
    }

    public double getBotTimeSinceDeceleration() {
        return BATTLE_SUMMARY.getBotTimeSinceDeceleration();
    }

    public double getBotTimeSinceDirectionChange() {
        return BATTLE_SUMMARY.getBotTimeSinceDirectionChange();
    }

    public double getEnemyWeightedHitrate() {
        return BATTLE_SUMMARY.getEnemyWeightedHitRate();
    }
}
