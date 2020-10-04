package dsekercioglu.mega.rMove.movetree.nodes;

import dsekercioglu.mega.core.GuessFactor;

import java.util.List;

public abstract class DiscreteRawDataNode extends Node {

    public abstract List<GuessFactor> getGuessFactorsRaw(int[] dataPoint);

    public abstract void addDataRaw(int[] dataPoint, GuessFactor guessFactor);
}
