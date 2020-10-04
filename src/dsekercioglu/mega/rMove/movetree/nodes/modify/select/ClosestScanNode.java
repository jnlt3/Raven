package dsekercioglu.mega.rMove.movetree.nodes.modify.select;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class ClosestScanNode extends Node {

    final Node NODE;

    public ClosestScanNode(Node node) {
        NODE = node;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        int closestScan = -1;
        GuessFactor closestScanGuessFactor = null;
        List<GuessFactor> guessFactors = NODE.getGuessFactors(battleInfo);
        for (GuessFactor guessFactor : guessFactors) {
            if (guessFactor.SCAN > closestScan) {
                closestScanGuessFactor = guessFactor;
                closestScan = guessFactor.SCAN;
            }
        }
        List<GuessFactor> guessFactor = new ArrayList<>();
        guessFactor.add(closestScanGuessFactor);
        return guessFactor;
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
