package dsekercioglu.mega.rGun;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import dsekercioglu.mega.core.wiki.FastMath;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class BattleInfo implements Cloneable {

    final AdvancedRobot BOT;

    final double BATTLE_FIELD_WIDTH;
    final double BATTLE_FIELD_HEIGHT;
    final double GUN_COOLING_RATE;

    Point2D.Double botLocation = new Point2D.Double();
    Point2D.Double enemyLocation = new Point2D.Double();

    double botVelocity;
    double botLateralVelocity;
    double botEnergy;
    double botBearing;
    double botHeading;
    double botDistance;
    double absoluteBearing;

    double enemyEnergy;
    double enemyLastFirePower;
    double enemyVel;
    double enemyLatVel;
    double enemyHeading;
    double enemyRelativeHeading;
    int enemyLateralDirection = 1;
    double enemyLateralAcceleration;
    double enemyAdvVel;
    double enemyTimeSinceDeceleration;

    boolean enemyFired = false;
    double lastFirePower;

    ArrayList<Double> enemyLateralVelocityList;

    int fired;

    public BattleInfo(AdvancedRobot advBot) {
        BOT = advBot;
        BATTLE_FIELD_WIDTH = advBot.getBattleFieldWidth();
        BATTLE_FIELD_HEIGHT = advBot.getBattleFieldHeight();
        GUN_COOLING_RATE = advBot.getGunCoolingRate();
    }

    public void run() {
        enemyLateralVelocityList = new ArrayList<>();
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        botLocation.setLocation(BOT.getX(), BOT.getY());

        botEnergy = BOT.getEnergy();

        double oldEnemyEnergy = enemyEnergy;
        enemyEnergy = e.getEnergy();
        double deltaEnergy = oldEnemyEnergy - enemyEnergy;

        if (deltaEnergy > 0.099 && deltaEnergy < 3.01) {
            enemyFired = true;
            enemyLastFirePower = deltaEnergy;
        } else {
            enemyFired = false;
        }

        enemyVel = e.getVelocity();
        botHeading = BOT.getHeadingRadians();
        botBearing = e.getBearingRadians();

        botVelocity = BOT.getVelocity();
        botLateralVelocity = FastMath.sin(botBearing) * botVelocity;

        absoluteBearing = botHeading + botBearing;
        enemyHeading = e.getHeadingRadians();
        enemyRelativeHeading = enemyHeading - absoluteBearing;
        enemyLateralAcceleration = Math.abs(enemyLatVel);
        enemyLatVel = FastMath.sin(enemyRelativeHeading) * enemyVel;
        enemyLateralAcceleration -= Math.abs(enemyLatVel);
        enemyLateralAcceleration = -enemyLateralAcceleration;
        enemyLateralVelocityList.add(enemyLatVel);
        enemyLateralDirection = enemyLatVel > 0 ? 1 : (enemyLatVel == 0 ? enemyLateralDirection : -1);
        enemyAdvVel = FastMath.cos(enemyRelativeHeading) * -enemyVel;
        botDistance = e.getDistance();

        enemyLocation.setLocation(GunUtils.project(botLocation, absoluteBearing, botDistance));

        enemyTimeSinceDeceleration = enemyLateralAcceleration < 0 ? 0 : enemyTimeSinceDeceleration + 1;

    }

    public void onFire() {
        fired++;
    }

    public double getFired() {
        return fired;
    }

    @Override
    public BattleInfo clone() {
        try {
            BattleInfo clone = (BattleInfo) super.clone();
            clone.enemyLocation = (Point2D.Double) enemyLocation.clone();
            clone.botLocation = (Point2D.Double) botLocation.clone();
            return clone;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BattleInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public double getBotEnergy() {
        return botEnergy;
    }

    public double getBotLateralVelocity() {
        return botLateralVelocity;
    }

    public double getEnemyEnergy() {
        return enemyEnergy;
    }

    public double getEnemyVelocity() {
        return enemyVel;
    }

    public double getEnemyLateralVelocity() {
        return enemyLatVel;
    }

    public double getEnemyAdvancingVelocity() {
        return enemyAdvVel;
    }

    public int getEnemyLateralDirection() {
        return enemyLateralDirection;
    }

    public double getBotDistance() {
        return botDistance;
    }

    public double getEnemyHeading() {
        return enemyHeading;
    }

    public double getEnemyLateralAcceleration() {
        return enemyLateralAcceleration;
    }

    public double getEnemyTimeSinceDeceleration() {
        return enemyTimeSinceDeceleration;
    }

    public double getLastFirePower() {
        return lastFirePower;
    }

    public double getEnemyLastFirePower() {
        return enemyLastFirePower;
    }

    public boolean enemyFired() {
        return enemyFired;
    }

    public void setLastFirePower(double firePower) {
        lastFirePower = firePower;
    }

    public double getEnemyMeanLateralVelocityLastX(int x) {
        double sumVelocity = 0;
        int clippedX = Math.min(enemyLateralVelocityList.size(), x);
        int startIndex = Math.max(enemyLateralVelocityList.size() - clippedX, 0);
        for (int i = startIndex; i < enemyLateralVelocityList.size(); i++) {
            sumVelocity += enemyLateralVelocityList.get(i);
        }
        return clippedX == 0 ? 0 : sumVelocity / clippedX;
    }

    public double getMEA(int direction) {
        double turnPerTick = 8 / botDistance;
        double maxIteration = Math.PI * 2 / turnPerTick;
        double angle = absoluteBearing;
        double mea = 0;
        Point2D.Double predictedPosition = GunUtils.project(botLocation, angle, botDistance);
        int iteration = 0;
        while (GunUtils.distanceToWall(predictedPosition.x,
                predictedPosition.y,
                BATTLE_FIELD_WIDTH,
                BATTLE_FIELD_HEIGHT) >= 18) {
            angle += turnPerTick * enemyLateralDirection * direction;
            mea += turnPerTick;
            predictedPosition = GunUtils.project(botLocation, angle, botDistance);
            if ((++iteration) > maxIteration) {
                break;
            }
        }
        return mea;
    }
}
