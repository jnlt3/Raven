package dsekercioglu.mega.rMove.movetree.formula.arrayformula;

import dsekercioglu.mega.rMove.info.WaveData;

public abstract class DiscreteFormula {

    public abstract int[] getDimensions();

    public abstract int[] getDataPoint(WaveData battleInfo);

}
