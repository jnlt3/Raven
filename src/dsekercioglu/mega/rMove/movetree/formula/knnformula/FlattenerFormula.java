package dsekercioglu.mega.rMove.movetree.formula.knnformula;

import dsekercioglu.mega.rMove.info.WaveData;

public class FlattenerFormula extends ContinuousFormula {


    private final static double[] WEIGHTS = new double[]{5, 1, 3, 4, 1, 5, 3, 1, 1};
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
        double mea = 8 / (20 - 3 * battleInfo.getFirePower());
        double accel = battleInfo.getBotLateralAcceleration();
        double[] normalized = {Math.abs(battleInfo.getBotLateralVelocity()) / 8,
                (battleInfo.getBotAdvancingVelocity() + 8) / 16,
                battleInfo.getBulletFloatTime() / 91,
                Math.min(battleInfo.getForwardWallMEA() / mea, 1),
                Math.min(battleInfo.getBackwardWallMEA() / mea, 1),
                (Math.max(accel * 2, accel) + 2) / 4,
                battleInfo.getFirePower() / 3,
                1 / (1 + battleInfo.getBotTimeSinceDeceleration() * 0.5D),
                1 / (1 + battleInfo.getBotTimeSinceDirectionChange() * 0.5D)};
        for (int i = 0; i < normalized.length; i++)
            normalized[i] = normalized[i] * WEIGHTS[i];
        return normalized;
    }
}
