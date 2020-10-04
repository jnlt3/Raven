package dsekercioglu.mega.rMove.movetree.nodes.modify.optimize;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombineNode extends Node {


    private final int MAP_BINS;
    private final Node[] NODES;

    public CombineNode(int mapBins, Node... nodes) {
        MAP_BINS = mapBins;
        NODES = nodes;
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        List<GuessFactor> weightedGuessFactors = new ArrayList<>();
        List<GuessFactor>[] guessFactorMap = new List[MAP_BINS];

        Arrays.setAll(guessFactorMap, i -> new ArrayList<>());

        for (Node node : NODES) {
            List<GuessFactor> ithGuessFactors = node.getGuessFactors(battleInfo);
            for (GuessFactor guessFactor : ithGuessFactors) {
                double gfValue = guessFactor.GUESS_FACTOR;
                int bin = (int) MoveUtils.limit(0, ((gfValue + 1) * 0.5 * MAP_BINS), MAP_BINS - 1);
                List<GuessFactor> guessFactors = guessFactorMap[bin];
                if (guessFactorMap[bin].isEmpty()) {
                    guessFactorMap[bin].add(guessFactor);
                } else {
                    outer:
                    {
                        for (GuessFactor gf : guessFactors) {
                            if (gf.GUESS_FACTOR == gfValue) {
                                gf.setWeight(gf.getWeight() + guessFactor.getWeight());
                                break outer;
                            }
                        }
                        guessFactors.add(guessFactor);
                    }
                }
            }
        }
        for (List<GuessFactor> guessFactors : guessFactorMap) {
            weightedGuessFactors.addAll(guessFactors);
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
        return Arrays.toString(NODES);
    }


}
