package dsekercioglu.mega.rMove.movetree.nodes.learn;

import dsekercioglu.mega.core.DistancedGuessFactor;
import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.KNNView;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.formula.knnformula.ContinuousFormula;
import dsekercioglu.mega.rMove.movetree.nodes.ContinuousRawDataNode;

import java.util.ArrayList;
import java.util.List;

public class MP extends ContinuousRawDataNode {

    private final ContinuousFormula FORMULA;
    private final KNNView KNN_VIEW;
    private final double DIST_COEFF;

    public MP(ContinuousFormula formula, int k, int divisor, double distCoeff) {
        FORMULA = formula;
        KNN_VIEW = new KNNView(k, divisor, FORMULA.getWeights());
        DIST_COEFF = distCoeff;
        double[] zeroPoint = new double[FORMULA.getWeights().length];
        KNN_VIEW.addDataPoint(zeroPoint, new GuessFactor(0, 0.0001, 0));
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        return getGuessFactorsRaw(FORMULA.getDataPoint(battleInfo));
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
        if (!real) {
            KNN_VIEW.addDataPoint(FORMULA.getDataPoint(battleInfo), guessFactor);
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
                    guessFactor.getWeight() / (1 + distancedGuessFactor.getDistance() * DIST_COEFF / FORMULA.getMaxDistance()),
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
