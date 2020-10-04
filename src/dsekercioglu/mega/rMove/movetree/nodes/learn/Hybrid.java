package dsekercioglu.mega.rMove.movetree.nodes.learn;

import dsekercioglu.mega.core.DistancedGuessFactor;
import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.KNNView;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.formula.knnformula.ContinuousFormula;
import dsekercioglu.mega.rMove.movetree.nodes.ContinuousRawDataNode;

import java.util.ArrayList;
import java.util.List;

public class Hybrid extends ContinuousRawDataNode {


    private final ContinuousFormula FORMULA;
    private final KNNView KNN_VIEW;

    private final double VISIT_WEIGHT;

    public Hybrid(ContinuousFormula formula, int k, int divisor, double visitWeight) {
        FORMULA = formula;
        VISIT_WEIGHT = visitWeight;
        KNN_VIEW = new KNNView(k, divisor, FORMULA.getWeights());
        double[] zeroPoint = new double[FORMULA.getWeights().length];
        KNN_VIEW.addDataPoint(zeroPoint, new GuessFactor(0, 0.0001, 0));
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        return getGuessFactorsRaw(FORMULA.getDataPoint(battleInfo));
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
        if (real) {
            KNN_VIEW.addDataPoint(FORMULA.getDataPoint(battleInfo), guessFactor);
        } else {
            GuessFactor weightedGuessFactor
                    = new GuessFactor(guessFactor.GUESS_FACTOR,
                    guessFactor.getWeight() * VISIT_WEIGHT,
                    guessFactor.SCAN);
            KNN_VIEW.addDataPoint(FORMULA.getDataPoint(battleInfo), weightedGuessFactor);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public List<GuessFactor> getGuessFactorsRaw(double[] dataPoint) {
        List<GuessFactor> weightedGuessFactors = new ArrayList<>();
        List<DistancedGuessFactor> guessFactors = KNN_VIEW.nearestNeighbours(dataPoint);
        for (DistancedGuessFactor distancedGuessFactor : guessFactors) {
            GuessFactor guessFactor = distancedGuessFactor.getGuessFactor();
            GuessFactor weightedGuessFactor = new GuessFactor(guessFactor.GUESS_FACTOR,
                    guessFactor.getWeight() / (1 + distancedGuessFactor.getDistance() / FORMULA.getMaxDistance()),
                    guessFactor.SCAN);
            weightedGuessFactors.add(weightedGuessFactor);
        }
        return weightedGuessFactors;
    }

    @Override
    public void addDataRaw(double[] dataPoint, GuessFactor guessFactor) {
        KNN_VIEW.addDataPoint(dataPoint, guessFactor);
    }
}
