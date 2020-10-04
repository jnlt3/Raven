package dsekercioglu.mega.rMove.ws.path.aacalc;

import dsekercioglu.mega.rMove.sim.Wave;

import java.awt.geom.Point2D;

import static robocode.util.Utils.normalRelativeAngle;

public class TwoWaySmooth extends AttackAngleCalculator {

    private final AttackAngleCalculator ATTACK_ANGLE_CALCULATOR;

    public TwoWaySmooth(AttackAngleCalculator attackAngleCalculator) {
        ATTACK_ANGLE_CALCULATOR = attackAngleCalculator;
    }

    @Override
    public double getAttackAngle(Wave wave, Point2D.Double location, Point2D.Double enemyLocation, double currentAngle, double targetVelocity) {
        double angle0 = ATTACK_ANGLE_CALCULATOR.getAttackAngle(wave, location, enemyLocation, currentAngle, targetVelocity);
        double angle1 = ATTACK_ANGLE_CALCULATOR.getAttackAngle(wave, location, enemyLocation, currentAngle, -targetVelocity);

        return Math.abs(normalRelativeAngle(currentAngle - angle0)) < Math.abs(normalRelativeAngle(currentAngle - angle1)) ? angle0 : angle1;
    }
}
