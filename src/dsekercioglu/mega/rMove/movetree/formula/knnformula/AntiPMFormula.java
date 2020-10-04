package dsekercioglu.mega.rMove.movetree.formula.knnformula;

import dsekercioglu.mega.rMove.info.WaveData;

public class AntiPMFormula extends ContinuousFormula {

    private static final double[] WEIGHTS = {5, 1, 3, 4, 1, 1};
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

        double firePower = battleInfo.getFirePower();
        double circularMEA = 8 / (20 - 3 * firePower);

        double[] normalized = {Math.abs(battleInfo.getBotLateralVelocity()) / 8,
                (battleInfo.getBotAdvancingVelocity() + 8) / 16,
                battleInfo.getBulletFloatTime() / 91,
                Math.min(battleInfo.getForwardWallMEA() / circularMEA, 1),
                1 / (1 + battleInfo.getBotTimeSinceDeceleration() / battleInfo.getBulletFloatTime() * 10),
                1 / (1 + battleInfo.getBotTimeSinceDirectionChange() / battleInfo.getBulletFloatTime() * 4)};
        for (int i = 0; i < normalized.length; i++) {
            normalized[i] *= WEIGHTS[i];
        }
        return normalized;
    }
}
