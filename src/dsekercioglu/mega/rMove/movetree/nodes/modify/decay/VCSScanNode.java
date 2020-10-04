package dsekercioglu.mega.rMove.movetree.nodes.modify.decay;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.Pair;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//CREDIT: Voidious
public class VCSScanNode extends Node {

    final Node NODE;
    final double DECAY_FACTOR;

    public VCSScanNode(double decayFactor, Node node) {
        DECAY_FACTOR = decayFactor;
        NODE = node;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        ArrayList<Pair<GuessFactor, Integer>> scanOrderedGuessFactors = new ArrayList<>();
        List<GuessFactor> ithGuessFactors = NODE.getGuessFactors(battleInfo);
        for (GuessFactor guessFactor : ithGuessFactors) {
            scanOrderedGuessFactors.add(new Pair<>(guessFactor, guessFactor.SCAN));
        }
        Collections.sort(scanOrderedGuessFactors);
        List<GuessFactor> weightedGuessFactors = new ArrayList<>();
        double scanWeight = 1;
        for (int i = scanOrderedGuessFactors.size() - 1; i >= 0; i--) {
            Pair<GuessFactor, Integer> pair = scanOrderedGuessFactors.get(i);
            GuessFactor guessFactor = pair.getObject();
            weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, scanWeight, guessFactor.SCAN));
            scanWeight /= DECAY_FACTOR;
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
