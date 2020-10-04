package dsekercioglu.mega.rMove.movetree.nodes;

import dsekercioglu.mega.core.GuessFactor;

import java.util.List;

public abstract class ContinuousRawDataNode extends Node {

    public abstract List<GuessFactor> getGuessFactorsRaw(double[] dataPoint);

    public abstract void addDataRaw(double[] dataPoint, GuessFactor guessFactor);
}
