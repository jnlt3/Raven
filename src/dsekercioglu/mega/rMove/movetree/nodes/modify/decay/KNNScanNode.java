package dsekercioglu.mega.rMove.movetree.nodes.modify.decay;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class KNNScanNode extends Node {

    final Node NODE;
    final double DECAY_FACTOR;

    public KNNScanNode(double decayFactor, Node node) {
        DECAY_FACTOR = decayFactor;
        NODE = node;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        double maxScan = 0;
        List<GuessFactor> weightedGuessFactors = new ArrayList<>();
        List<GuessFactor> guessFactors = NODE.getGuessFactors(battleInfo);
        for (GuessFactor guessFactor : guessFactors) {
            if (guessFactor.SCAN > maxScan) {
                maxScan = guessFactor.SCAN;
            }
            weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight(), guessFactor.SCAN));
        }
        double ratio = 2 / maxScan;
        if (!Double.isFinite(ratio)) {
            ratio = 0;
        }
        for (GuessFactor guessFactor : weightedGuessFactors) {
            guessFactor.setWeight(guessFactor.getWeight() * Math.pow(DECAY_FACTOR, (guessFactor.SCAN - maxScan) * ratio));
        }
        return weightedGuessFactors;
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
