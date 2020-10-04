package dsekercioglu.mega.rMove.movetree.nodes.modify.condition;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.List;

public class NoInNode extends Node {


    private final Node NODE;

    public NoInNode(Node node) {
        NODE = node;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        return NODE.getGuessFactors(battleInfo);
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
    }

    @Override
    public String toString() {
        return getName() + ":(" + NODE.toString() + ")";
    }

}
