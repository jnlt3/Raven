package dsekercioglu.mega;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rLock.RavenEyes;
import dsekercioglu.mega.rGun.Claws;
import dsekercioglu.mega.rMove.RavenFlight;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.*;

public class RavenDev extends AdvancedRobot {

    static RavenFlight move;
    static Claws gun;
    static RavenEyes radar;

    static {
        FastMath.init();
    }

    static long timeOnMovement = 0;
    static long timeOnTargeting = 0;

    static int ticks = 0;

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
        long timeAll = System.nanoTime();
        long time = System.nanoTime();
        move.onScannedRobot(e);
        long timeElapsed = System.nanoTime() - time;
        timeOnMovement += timeElapsed;
        if(timeElapsed > 2000000) {
            System.out.println("WARNING, MOVEMENT TAKING TOO MUCH TIME: " + timeElapsed / 1000000D);
        }
        time = System.nanoTime();
        gun.onScannedRobot(e);
        timeElapsed = System.nanoTime() - time;
        timeOnTargeting += timeElapsed;
        if(timeElapsed > 2000000) {
            System.out.println("WARNING, TARGETING TAKING TOO MUCH TIME: " + timeElapsed / 1000000D);
        }
        radar.onScannedRobot(e);
        //System.out.println((System.nanoTime() - timeAll) / 1000000D);
        ticks++;
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
        System.out.println("Time on Movement");
        System.out.println((timeOnMovement * 1D / ticks) / 1000000D);
        System.out.println("Time on Targeting");
        System.out.println((timeOnTargeting * 1D / ticks) / 1000000D);
    }

    @Override
    public void onPaint(Graphics2D g) {
        move.onPaint(g);
        gun.onPaint(g);
    }

}
