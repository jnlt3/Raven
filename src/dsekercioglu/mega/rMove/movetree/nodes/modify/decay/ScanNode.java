package dsekercioglu.mega.rMove.movetree.nodes.modify.decay;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.List;

public class ScanNode extends Node {

    final Node NODE;

    public ScanNode(Node node) {
        NODE = node;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        List<GuessFactor> guessFactors = NODE.getGuessFactors(battleInfo);
        for (GuessFactor guessFactor : guessFactors) {
            guessFactor.setWeight(guessFactor.getWeight() * (guessFactor.SCAN + 1));
        }
        return guessFactors;
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
