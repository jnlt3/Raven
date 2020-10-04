package dsekercioglu.mega.rMove.movetree.formula.arrayformula;

import dsekercioglu.mega.rMove.info.WaveData;

public class All extends DiscreteFormula {

    final int[] DIMENSIONS = {1};

    @Override
    public int[] getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public int[] getDataPoint(WaveData battleInfo) {
        return new int[1];
    }
}
