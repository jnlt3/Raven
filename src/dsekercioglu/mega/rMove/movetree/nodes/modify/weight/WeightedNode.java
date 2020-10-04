package dsekercioglu.mega.rMove.movetree.nodes.modify.weight;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class WeightedNode extends Node {


    final double[] WEIGHTS;
    final Node[] NODES;

    public WeightedNode(double[] weights, Node... nodes) {
        WEIGHTS = weights;
        NODES = nodes;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        List<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (int i = 0; i < WEIGHTS.length; i++) {
            Node node = NODES[i];
            List<GuessFactor> ithGuessFactors = node.getGuessFactors(battleInfo);
            for (GuessFactor guessFactor : ithGuessFactors) {
                weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, WEIGHTS[i] * guessFactor.getWeight(), guessFactor.SCAN));
            }
        }
        return weightedGuessFactors;
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
        for (Node node : NODES) {
            node.addData(battleInfo, guessFactor, real);
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("\n");
        for (int i = 0; i < WEIGHTS.length; i++) {
            out.append(NODES[i].toString()).append(": ").append(WEIGHTS[i]).append("\n");
        }
        return out.toString();
    }

}
