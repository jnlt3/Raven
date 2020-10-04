package dsekercioglu.mega.rMove.movetree.formula.arrayformula;

import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.info.WaveData;

public class SimpleArrFormula extends DiscreteFormula {

    final int[] DIMENSIONS;

    public SimpleArrFormula(int[] dimensions) {
        DIMENSIONS = dimensions;
    }

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int[] getDataPoint(WaveData battleInfo) {
        double[] normalized = {
                Math.abs(battleInfo.getBotLateralVelocity()) / 8,
                battleInfo.getBotAdvancingVelocity() / 16 + 0.5,
                battleInfo.getBulletFloatTime() / 91
        };
        int[] indices = new int[DIMENSIONS.length];
        for (int i = 0; i < normalized.length; i++) {
            indices[i] = (int) MoveUtils.limit(0, normalized[i] * DIMENSIONS[i], DIMENSIONS[i] - 1);
        }
        return indices;
    }
}
