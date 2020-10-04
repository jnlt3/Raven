package dsekercioglu.mega.rMove.ws.path.aacalc;

import dsekercioglu.mega.rMove.sim.Wave;

import java.awt.geom.Point2D;

public abstract class AttackAngleCalculator {

    public abstract double getAttackAngle(Wave wave, Point2D.Double location, Point2D.Double enemyLocation, double currentAngle, double targetVelocity);
}
