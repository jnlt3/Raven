package dsekercioglu.mega.rMove.movetree.formula.arrayformula;

import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.info.WaveData;

public class NormalArrFormula extends DiscreteFormula {

    final int[] DIMENSIONS;

    public NormalArrFormula(int[] dimensions) {
        DIMENSIONS = dimensions;
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int[] getDataPoint(WaveData battleInfo) {

        double firePower = battleInfo.getFirePower();
        double mea = FastMath.asin(8 / (20 - 3 * firePower));
        double accel = battleInfo.getBotLateralAcceleration();

        double[] normalized = {
                Math.abs(battleInfo.getBotLateralVelocity()) / 8,
                battleInfo.getBotAdvancingVelocity() / 16 + 0.5,
                battleInfo.getBulletFloatTime() / 91,
                Math.min(battleInfo.getForwardWallMEA() / mea, 1),
                Math.min(battleInfo.getBackwardWallMEA() / mea, 1),
                (Math.max(accel * 2, accel) + 2) / 4
        };
        int[] indices = new int[DIMENSIONS.length];
        for (int i = 0; i < normalized.length; i++) {
            indices[i] = (int) MoveUtils.limit(0, normalized[i] * DIMENSIONS[i], DIMENSIONS[i] - 1);
        }
        return indices;
    }
}
