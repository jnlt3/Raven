package dsekercioglu.mega.rMove.info;

import robocode.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class BotInfo {

    double energy;
    double oldEnergy;
    double deltaEnergy;
    ArrayList<Double> gunBearings = new ArrayList<>();
    ArrayList<Point2D.Double> locations = new ArrayList<>();

    final AdvancedRobot BOT;

    public BotInfo(AdvancedRobot advBot) {
        this.BOT = advBot;
    }

    public void run() {
        energy = 100;
        oldEnergy = 100;
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        gunBearings.add(BOT.getGunHeadingRadians());
        locations.add(new Point2D.Double(BOT.getX(), BOT.getY()));
        energy = BOT.getEnergy();
        deltaEnergy = oldEnergy - energy;
        oldEnergy = energy;
    }

    public void onHitRobot(HitRobotEvent e) {
        oldEnergy -= 0.6;
    }

    public void onHitByBullet(HitByBulletEvent e) {
        oldEnergy -= Rules.getBulletDamage(e.getPower());
    }

    public void onBulletHit(BulletHitEvent e) {
        oldEnergy += e.getBullet().getPower() * 3;
    }

    public boolean botFired() {
        return deltaEnergy > 0.099 && deltaEnergy < 3.001;
    }

    public double getGunHeading() {
        return gunBearings.get(gunBearings.size() - 2);
    }

    public double getFirePower() {
        return deltaEnergy;
    }

    public Point2D.Double getFireLocation() {
        return locations.get(locations.size() - 2);
    }
}
