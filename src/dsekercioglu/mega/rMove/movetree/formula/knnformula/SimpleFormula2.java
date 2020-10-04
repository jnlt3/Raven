package dsekercioglu.mega.rMove.movetree.formula.knnformula;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.info.WaveData;

public class SimpleFormula2 extends ContinuousFormula {

    private static final double[] WEIGHTS = {5, 2, 3, 5};
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

        double[] normalized = {Math.abs(battleInfo.getBotVelocity()) / 8,
                Math.abs(battleInfo.getBotRelativeHeading()) / Math.PI,
                battleInfo.getBulletFloatTime() / 91,
                Math.min(battleInfo.getForwardWallMEA() / mea, 1)};
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] *= WEIGHTS[i];
        }
        return normalized;
    }
}
