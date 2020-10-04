package dsekercioglu.mega.rMove.movetree.nodes.modify.select;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.List;

public class StatSelectNode extends Node {

    private final double[] WEIGHTS;
    private final double SMOOTH_FACTOR;
    private final double HIT_WEIGHT;
    private final double MISS_WEIGHT;
    private final double DECAY;
    private final int NUM_NODES;
    private final Node[] NODES;

    public StatSelectNode(double[] initialWeights, double smoothFactor, double hitWeight, double missWeight, double decay, Node... nodes) {
        WEIGHTS = initialWeights;
        SMOOTH_FACTOR = smoothFactor;
        HIT_WEIGHT = hitWeight;
        MISS_WEIGHT = missWeight;
        DECAY = decay;
        NODES = nodes;
        NUM_NODES = NODES.length;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        int highestIndex = 0;
        for (int i = 1; i < NUM_NODES; i++) {
            if (WEIGHTS[i] > WEIGHTS[highestIndex]) {
                highestIndex = i;
            }
        }
        return NODES[highestIndex].getGuessFactors(battleInfo);
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {

        List<GuessFactor>[] guessFactors = new List[NUM_NODES];
        double weight = real ? HIT_WEIGHT : -MISS_WEIGHT;
        for (int i = 0; i < NUM_NODES; i++) {
            guessFactors[i] = NODES[i].getGuessFactors(battleInfo);
            double danger = 0;
            for (GuessFactor prediction : guessFactors[i]) {
                danger += prediction.getWeight() / (1 + MoveUtils.sq((prediction.GUESS_FACTOR - guessFactor.GUESS_FACTOR) / SMOOTH_FACTOR));
            }
            WEIGHTS[i] = WEIGHTS[i] * DECAY + danger * weight * (1 - DECAY);
            NODES[i].addData(battleInfo, guessFactor, real);
        }
    }

    @Override
    public String toString() {
        int bestIndex = 0;
        for (int i = 1; i < WEIGHTS.length; i++) {
            if (WEIGHTS[i] > WEIGHTS[bestIndex]) {
                bestIndex = i;
            }
        }
        StringBuilder out = new StringBuilder("\n");
        for (int i = 0; i < WEIGHTS.length; i++) {
            String end = i == bestIndex ? "*" : "";
            out.append(NODES[i].toString()).append("(").append((int)(WEIGHTS[i] * 100) / 100D).append(")").append(end).append("\n");
        }
        return out.toString();
    }
}
