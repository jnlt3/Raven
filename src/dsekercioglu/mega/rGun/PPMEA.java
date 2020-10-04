package dsekercioglu.mega.rGun;

import dsekercioglu.mega.core.wiki.FastMath;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PPMEA {

    final double BF_WIDTH;
    final double BF_HEIGHT;
    final double ORIENTATION;
    final int MAX_ITERATIONS;
    final Rectangle2D.Double B_FIELD;

    private Point2D.Double[] escapePositions = new Point2D.Double[2];
    private double[] escapeAngles = new double[2];

    public PPMEA(double battleFieldWidth, double battleFieldHeight, double orientation) {
        BF_WIDTH = battleFieldWidth;
        BF_HEIGHT = battleFieldHeight;
        ORIENTATION = orientation;
        MAX_ITERATIONS = (int) Math.ceil(Math.PI / ORIENTATION);
        B_FIELD = new Rectangle2D.Double(17.9, 17.9, battleFieldWidth - 35.8, battleFieldHeight - 35.8);
    }

    public void calculateEscapeAngle(Point2D.Double robotLocation, Point2D.Double orbitLocation, double bulletSpeed) {
        for (int i = -1; i <= 1; i += 2) {
            double absoluteBearing = GunUtils.absoluteBearing(orbitLocation, robotLocation);
            double moveAngle = absoluteBearing + Math.PI / 2;
            Point2D.Double interception = intercept(orbitLocation, bulletSpeed, robotLocation, moveAngle, 8 * i);
            int iterations = 0;
            while (!B_FIELD.contains(interception) && iterations <= MAX_ITERATIONS) {
                moveAngle += ORIENTATION * i;
                interception = intercept(orbitLocation, bulletSpeed, robotLocation, moveAngle, 8 * i);
                iterations++;
            }
            int index = (i + 1) / 2;
            escapePositions[index] = interception;
            escapeAngles[index] = GunUtils.absoluteBearing(orbitLocation, interception);
        }
    }

    public void calculateEscapeAngle(Point2D.Double robotLocation, Point2D.Double orbitLocation, double bulletDistanceTraveled, double bulletSpeed) {
        double distance = orbitLocation.distance(robotLocation);
        calculateEscapeAngle(robotLocation, orbitLocation, distance / ((distance - bulletDistanceTraveled) / bulletSpeed));
    }

    public Point2D.Double getEscapePosition(int direction) {
        return escapePositions[(direction + 1) / 2];
    }

    public double getEscapeAngle(int direction) {
        return escapeAngles[(direction + 1) / 2];
    }

    //CREDIT: Chase-san
    static Point2D.Double intercept(Point2D pos, double vel, Point2D tPos, double tHeading, double tVel) {
        double tVelX = FastMath.sin(tHeading) * tVel;
        double tVelY = FastMath.cos(tHeading) * tVel;
        double relX = tPos.getX() - pos.getX();
        double relY = tPos.getY() - pos.getY();
        double b = relX * tVelX + relY * tVelY;
        double a = vel * vel - tVel * tVel;
        b = (b + Math.sqrt(b * b + a * (relX * relX + relY * relY))) / a;
        return new Point2D.Double(tVelX * b + tPos.getX(), tVelY * b + tPos.getY());
    }

}
