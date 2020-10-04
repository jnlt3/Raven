package dsekercioglu.mega.rMove.movetree.nodes.modify.optimize;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.List;

public class CacheNode extends Node {


    private final Node NODE;
    WaveData control;
    List<GuessFactor> buffer;

    public CacheNode(Node node) {
        NODE = node;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        if (control != battleInfo) {
            control = battleInfo;
            buffer = NODE.getGuessFactors(battleInfo);
        }
        return buffer;
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
        NODE.addData(battleInfo, guessFactor, real);
    }

    @Override
    public String toString() {
        return getName() + ":(" + NODE.toString() + ")";
    }

}
