package dsekercioglu.mega.rMove.movetree.nodes.modify.select;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class LimitedKNode extends Node {


    private final Node NODE;
    private final int MAX_K;

    public LimitedKNode(Node node, int maxK) {
        NODE = node;
        MAX_K = maxK;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        List<GuessFactor> guessFactors = NODE.getGuessFactors(battleInfo);
        List<GuessFactor> croppedGuessFactors = new ArrayList<>();
        for (int i = 0; i < Math.min(guessFactors.size(), MAX_K); i++) {
            croppedGuessFactors.add(guessFactors.get(i));
        }
        return croppedGuessFactors;
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
