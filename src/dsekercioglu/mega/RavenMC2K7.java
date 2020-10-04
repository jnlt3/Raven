/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dsekercioglu.mega;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.raikoGun.RaikoGun;
import dsekercioglu.mega.rLock.RavenEyes;
import dsekercioglu.mega.rGun.Claws;
import dsekercioglu.mega.rMove.RavenFlight;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.*;

/**
 * @author doruksekercioglu
 */
public class RavenMC2K7 extends AdvancedRobot {

    static RavenFlight move;
    static RaikoGun gun;

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
            gun = new RaikoGun(this);
        }

        move.run();
        gun.run();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        move.onScannedRobot(e);
        gun.onScannedRobot(e);
    }


    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        move.onHitByBullet(e);
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        move.onBulletHitBullet(e);
    }

    public void onBulletHit(BulletHitEvent e) {
        move.onBulletHit(e);
    }

    public void onHitRobot(HitRobotEvent e) {
        move.onHitRobot(e);
    }

    public void onRoundEnded(RoundEndedEvent e) {
        move.onRoundEnded(e);
    }

    @Override
    public void onPaint(Graphics2D g) {
        move.onPaint(g);
    }


}
