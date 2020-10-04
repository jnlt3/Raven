package dsekercioglu.mega;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rGun.Claws;
import dsekercioglu.mega.rLock.RavenEyes;
import dsekercioglu.mega.rMove.RavenFlight;
import robocode.*;

import java.awt.*;

public class RavenTC extends AdvancedRobot {

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
            gun = new Claws();
            radar = new RavenEyes(this);
            gun.init(this, true);
        }

        gun.run();
        radar.run();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        gun.onScannedRobot(e);
        radar.onScannedRobot(e);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        gun.onHitByBullet(e);
    }

    public void onBulletHit(BulletHitEvent e) {
        gun.onBulletHit(e);
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        gun.onBulletHitBullet(e);
    }


    @Override
    public void onPaint(Graphics2D g) {
        gun.onPaint(g);
    }

}
