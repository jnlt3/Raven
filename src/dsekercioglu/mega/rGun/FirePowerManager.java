package dsekercioglu.mega.rGun;

import dsekercioglu.mega.core.wiki.FastMath;
import robocode.Rules;

import static robocode.util.Utils.normalRelativeAngle;

public class FirePowerManager {

    final static double STD_FIRE_POWER = 2;

    final static double AVERAGING_FACTOR = 0.5;

    private static final double INVERSE_SQRT_2_PI = 1 / Math.sqrt(Math.PI * 2);

    private double averageFirePower = 2;
    private double averageDistance = 450;


    private static final double FUTURE_TICKS = 50;

    private final Claws GUN;


    //private static final double[] EXPLOIT = {0.15, 0.25, 0.35, 0.45, 0.65, 0.85, 0.95, 1.15, 1.95, 2.95};

    public FirePowerManager(Claws gun) {
        GUN = gun;
    }


    public void onFire(double firePower, BattleInfo battleInfo) {
        averageFirePower = averageFirePower * AVERAGING_FACTOR + firePower * (1 - AVERAGING_FACTOR);
        averageDistance = averageDistance * AVERAGING_FACTOR + battleInfo.getBotDistance() * (1 - AVERAGING_FACTOR);
    }


    public double firePower(BattleInfo battleInfo) {
        double robotEnergy = battleInfo.getBotEnergy();
        double enemyEnergy = battleInfo.getEnemyEnergy();
        double neededAmountToKill;
        if (enemyEnergy <= 4) {
            neededAmountToKill = enemyEnergy / 4;
        } else {
            neededAmountToKill = (enemyEnergy + 2) / 6;
        }
        double hitRate = 1D * GUN.bulletsHit / GUN.bulletsShot;
        double farDistanceFirePower = 1200 / battleInfo.getBotDistance();
        //double divisor = GunUtils.limit(30, enemyEnergy - robotEnergy, 50);
        double lowEnergyFirePower = robotEnergy / 20;

        double firePower = hitRate >= 0.33 ? 3 : GunUtils.limit(0.1, Math.min(lowEnergyFirePower, Math.min(farDistanceFirePower, Math.min(neededAmountToKill, STD_FIRE_POWER))), 3);
        if (!Double.isFinite(firePower)) {
            firePower = battleInfo.getEnemyLastFirePower();
        }
        return firePower;
        //double finalPower = GunUtils.limit(0.1, Math.max(closeDistanceFirePower, Math.min(Math.min(neededAmountToKill, lowEnergyFirePower), Math.min(STD_FIRE_POWER, farDistanceFirePower))), 3);
    }
}
