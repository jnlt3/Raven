package dsekercioglu.mega;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rLock.RavenEyes;
import dsekercioglu.mega.rGun.Claws;
import dsekercioglu.mega.rMove.RavenFlight;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.*;

/**
 * @author doruksekercioglu
 */
public class Raven extends AdvancedRobot {

    static RavenFlight move;
    static Claws gun;
    static RavenEyes radar;

    static {
        FastMath.init();
    }

    public void run() {
        setBodyColor(new Color(30, 28, 30));
        setGunColor(new Color(27, 27, 27));
        setRadarColor(new Color(0, 0, 0));
        setScanColor(new Color(0, 0, 0));
        setBulletColor(new Color(255, 255, 255));

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        if (getRoundNum() == 0) {
            move = new RavenFlight(this);
            gun = new Claws();
            radar = new RavenEyes(this);
            gun.init(this, false);
        }

        move.run();
        gun.run();
        radar.run();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        move.onScannedRobot(e);
        gun.onScannedRobot(e);
        radar.onScannedRobot(e);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        move.onHitByBullet(e);
        gun.onHitByBullet(e);
    }

    public void onBulletHit(BulletHitEvent e) {
        move.onBulletHit(e);
        gun.onBulletHit(e);
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        move.onBulletHitBullet(e);
        gun.onBulletHitBullet(e);
    }

    public void onHitRobot(HitRobotEvent e) {
        move.onHitRobot(e);
    }

    @Override
    public void onRoundEnded(RoundEndedEvent e) {
        move.onRoundEnded(e);
    }

    @Override
    public void onPaint(Graphics2D g) {
        move.onPaint(g);
        gun.onPaint(g);
    }

}
