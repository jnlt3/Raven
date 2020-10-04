package dsekercioglu.mega.rMove.info.battle;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.info.EnemyTargetingInfo;
import dsekercioglu.mega.rMove.mea.MEA;

import robocode.*;

import java.awt.geom.Point2D;

public class BattleInfo {

    private final AdvancedRobot ROBOT;

    private final double BATTLE_FIELD_WIDTH;
    private final double BATTLE_FIELD_HEIGHT;
    private final double GUN_COOLING_RATE;

    private final Point2D.Double BOT_LOCATION = new Point2D.Double();
    private final Point2D.Double ENEMY_LOCATION = new Point2D.Double();

    private EnemyTargetingInfo enemyTargetingInfo;

    private double distance;
    private double absoluteBearing;

    private double botVelocity;
    private double botHeading;
    private double botBearing;

    private double botLateralVelocity;
    private double botLateralDirection = 1;
    private double botLateralAcceleration;
    private double botAdvancingVelocity;

    private double botTimeSinceDeceleration;
    private double botTimeSinceDirectionChange;

    private double botEnergy;

    private double enemyEnergy;
    private boolean enemyFired;
    private double enemyFirePower;
    private double enemyGunHeat;

    private double enemyVelocity;
    private double enemyHeading;

    private int enemyFireNum;
    private int enemyHitNum;
    private double enemyWeightedFireRate = 200;
    private double enemyWeightedHitRate;


    public BattleInfo(AdvancedRobot advancedRobot) {
        ROBOT = advancedRobot;
        BATTLE_FIELD_WIDTH = advancedRobot.getBattleFieldWidth();
        BATTLE_FIELD_HEIGHT = advancedRobot.getBattleFieldHeight();
        GUN_COOLING_RATE = advancedRobot.getGunCoolingRate();
    }

    public void run() {
        botTimeSinceDirectionChange = 0;
        botTimeSinceDeceleration = 0;
    }

    public BattleSummary getSummary() {
        BattleSummary battleSummary = new BattleSummary();

        battleSummary.botLocation = (Point2D.Double) BOT_LOCATION.clone();
        battleSummary.distance = distance;
        battleSummary.botBearing = botBearing;
        battleSummary.botVelocity = botVelocity;

        battleSummary.botLateralVelocity = botLateralVelocity;
        battleSummary.botLateralDirection = botLateralDirection;
        battleSummary.botAdvancingVelocity = botAdvancingVelocity;
        battleSummary.botLateralAcceleration = botLateralAcceleration;

        battleSummary.botTimeSinceDeceleration = botTimeSinceDeceleration;
        battleSummary.botTimeSinceDirectionChange = botTimeSinceDirectionChange;

        battleSummary.forwardWallMEA = getMEA(1);
        battleSummary.backwardWallMEA = getMEA(-1);

        battleSummary.enemyWeightedHitRate = getEnemyWeightedHitRate();
        battleSummary.battleFieldWidth = BATTLE_FIELD_WIDTH;
        battleSummary.battleFieldHeight = BATTLE_FIELD_HEIGHT;
        return battleSummary;
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        BOT_LOCATION.setLocation(ROBOT.getX(), ROBOT.getY());
        botEnergy = ROBOT.getEnergy();
        distance = e.getDistance();
        botHeading = ROBOT.getHeadingRadians();
        botBearing = e.getBearingRadians();

        double oldAbsoluteBearing = absoluteBearing;
        absoluteBearing = botHeading + botBearing;

        ENEMY_LOCATION.setLocation(
                BOT_LOCATION.getX() + FastMath.sin(absoluteBearing) * distance,
                BOT_LOCATION.getY() + FastMath.cos(absoluteBearing) * distance);


        enemyTargetingInfo = new EnemyTargetingInfo((Point2D.Double) ENEMY_LOCATION.clone(), oldAbsoluteBearing + Math.PI);

        double oldEnemyEnergy = enemyEnergy;
        enemyEnergy = e.getEnergy();
        double deltaEnergy = oldEnemyEnergy - enemyEnergy;

        enemyGunHeat = Math.max(0, enemyGunHeat - GUN_COOLING_RATE);
        if (deltaEnergy > 0.0999 && deltaEnergy < 3.001 && enemyGunHeat <= 0) {
            enemyFireNum++;
            enemyFired = true;
            enemyFirePower = deltaEnergy;
            enemyGunHeat = 1 + deltaEnergy / 5 - GUN_COOLING_RATE;
            double bulletVelocity = 20 - 3 * deltaEnergy;
            double botWidth = 2 * FastMath.atan(25 / distance);
            double hitChance = botWidth / Math.asin(8 / bulletVelocity);
            enemyWeightedFireRate += 1 / hitChance;
        } else {
            enemyFired = false;
        }

        botVelocity = ROBOT.getVelocity();

        double oldLateralVelocity = botLateralVelocity;

        botLateralVelocity = FastMath.sin(botBearing) * botVelocity;

        double oldLateralDirection = botLateralDirection;
        botLateralDirection = botLateralVelocity > 0 ? 1 : botLateralVelocity < 0 ? -1 : botLateralDirection;

        botAdvancingVelocity = -FastMath.cos(botBearing) * botVelocity;

        botLateralAcceleration = Math.abs(botLateralVelocity) - oldLateralVelocity;

        botTimeSinceDeceleration = botLateralAcceleration >= 0 ? botTimeSinceDirectionChange + 1 : 0;
        botTimeSinceDirectionChange = oldLateralDirection == botLateralDirection ? botTimeSinceDirectionChange + 1 : 0;

        enemyVelocity = e.getVelocity();
        enemyHeading = e.getHeadingRadians();
    }

    public void onHitByBullet(HitByBulletEvent e) {
        double bulletVelocity = e.getVelocity();
        enemyHitNum++;
        enemyEnergy += e.getBullet().getPower() * 3;
        if (distance > 200) {
            double botWidth = 2 * FastMath.atan(25 / distance);
            double hitChance = botWidth / Math.asin(8 / bulletVelocity);
            enemyWeightedHitRate += 1 / hitChance;
        }
    }

    public void onBulletHit(BulletHitEvent e) {
        enemyEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
    }

    public void onHitRobot(HitRobotEvent e) {
        enemyEnergy -= 0.6;
    }

    public Point2D.Double getBotLocation() {
        return BOT_LOCATION;
    }

    public Point2D.Double getEnemyLocation() {
        return ENEMY_LOCATION;
    }

    public double getDistance() {
        return distance;
    }

    public double getAbsoluteBearing() {
        return absoluteBearing;
    }

    public double getBotVelocity() {
        return botVelocity;
    }

    public double getBotHeading() {
        return botHeading;
    }

    public double getBotBearing() {
        return botBearing;
    }

    public double getBotLateralVelocity() {
        return botLateralVelocity;
    }

    public double getBotLateralAcceleration() {
        return botLateralAcceleration;
    }

    public double getBotAdvancingVelocity() {
        return botAdvancingVelocity;
    }

    public double getEnemyEnergy() {
        return enemyEnergy;
    }


    public double getBotEnergy() {
        return botEnergy;
    }

    public boolean enemyFired() {
        return enemyFired;
    }

    public double getEnemyFirePower() {
        return enemyFirePower;
    }

    public double getEnemyGunHeat() {
        return enemyGunHeat;
    }

    public double getEnemyVelocity() {
        return enemyVelocity;
    }

    public double getEnemyHeading() {
        return enemyHeading;
    }

    public int getEnemyFireNum() {
        return enemyFireNum;
    }

    public int getEnemyHitNum() {
        return enemyHitNum;
    }

    public double getEnemyWeightedHitRate() {
        return enemyWeightedHitRate / enemyWeightedFireRate;
    }

    public int enemyTimeUntilFire() {
        return (int) Math.ceil(enemyGunHeat / GUN_COOLING_RATE);
    }

    public EnemyTargetingInfo getEnemyTargetingInfo() {
        return enemyTargetingInfo;
    }

    public double getMEA(int direction) {
        double turnPerTick = 8 / distance;
        double maxIteration = Math.PI * 2 / turnPerTick;
        double angle = absoluteBearing + Math.PI;
        double mea = 0;
        Point2D.Double predictedPosition = MoveUtils.project(ENEMY_LOCATION, angle, distance);
        int iteration = 0;
        while (MoveUtils.distanceToWall(predictedPosition.x,
                predictedPosition.y,
                BATTLE_FIELD_WIDTH,
                BATTLE_FIELD_HEIGHT) >= 18) {
            angle += turnPerTick * botLateralDirection * direction;
            mea += turnPerTick;
            predictedPosition = MoveUtils.project(ENEMY_LOCATION, angle, distance);
            if ((++iteration) > maxIteration) {
                break;
            }
        }
        return mea;
    }
}
