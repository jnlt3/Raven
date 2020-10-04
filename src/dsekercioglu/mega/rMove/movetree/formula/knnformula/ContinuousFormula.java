package dsekercioglu.mega.rMove.movetree.formula.knnformula;

import dsekercioglu.mega.rMove.info.WaveData;

public abstract class ContinuousFormula {

    public abstract double[] getWeights();

    public abstract double getMaxDistance();

    public abstract double[] getDataPoint(WaveData battleInfo);
}
