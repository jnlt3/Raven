package dsekercioglu.mega.rMove.movetree.nodes;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;

import java.util.List;

public abstract class Node {

    protected String name = getClass().getSimpleName();

    public abstract List<GuessFactor> getGuessFactors(WaveData battleInfo);

    public abstract void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real);

    @Override
    public abstract String toString();

    public void setName(String name) {
        this.name = getClass().getSimpleName() + " :(" + name + ")";
    }

    public String getName() {
        return name;
    }
}
