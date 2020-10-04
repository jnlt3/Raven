package dsekercioglu.mega.rMove.movetree.nodes.learn;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.VariableDimensionArray;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.formula.arrayformula.DiscreteFormula;
import dsekercioglu.mega.rMove.movetree.nodes.DiscreteRawDataNode;

import java.util.ArrayList;
import java.util.List;

public class ArrayETP extends DiscreteRawDataNode {


    private final DiscreteFormula FORMULA;
    private final VariableDimensionArray ARRAY_VIEW;

    public ArrayETP(DiscreteFormula formula, int capacityPerSegment) {
        FORMULA = formula;
        ARRAY_VIEW = new VariableDimensionArray(FORMULA.getDimensions(), capacityPerSegment);
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        return getGuessFactorsRaw(FORMULA.getDataPoint(battleInfo));
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
        if (real) {
            ARRAY_VIEW.addDataPoint(FORMULA.getDataPoint(battleInfo), guessFactor);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public List<GuessFactor> getGuessFactorsRaw(int[] dataPoint) {
        List<GuessFactor> guessFactors = ARRAY_VIEW.get(dataPoint);
        List<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (GuessFactor guessFactor : guessFactors) {
            GuessFactor copiedGuessFactor = new GuessFactor(guessFactor.GUESS_FACTOR,
                    guessFactor.getWeight(),
                    guessFactor.SCAN);
            weightedGuessFactors.add(copiedGuessFactor);
        }
        return weightedGuessFactors;
    }

    @Override
    public void addDataRaw(int[] dataPoint, GuessFactor guessFactor) {
        ARRAY_VIEW.addDataPoint(dataPoint, guessFactor);
    }
}
