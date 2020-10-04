package dsekercioglu.mega.rMove.movetree.formula.knnformula;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.info.WaveData;

public class SimpleFormula extends ContinuousFormula {

    private static final double[] WEIGHTS = {5, 3, 4, 5};
    private final static double MAX_DISTANCE;

    static {
        double maxDistance = 0;
        for (double d : WEIGHTS) {
            maxDistance += Math.abs(d);
        }
        MAX_DISTANCE = maxDistance;
    }

    public double[] getWeights() {
        return WEIGHTS;
    }

    @Override
    public double getMaxDistance() {
        return MAX_DISTANCE;
    }

    public double[] getDataPoint(WaveData battleInfo) {
        double mea = 8 / battleInfo.getBulletVelocity();

        double accel = battleInfo.getBotLateralAcceleration();
        double[] normalized = {Math.abs(battleInfo.getBotLateralVelocity()) / 8,
                battleInfo.getBulletFloatTime() / 91,
                Math.min(battleInfo.getForwardWallMEA() / mea, 1),
                (Math.max(accel * 2, accel) + 2) / 4};
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] *= WEIGHTS[i];
        }
        return normalized;
    }
}
