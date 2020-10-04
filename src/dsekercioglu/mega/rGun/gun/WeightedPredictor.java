package dsekercioglu.mega.rGun.gun;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.Pair;
import dsekercioglu.mega.rGun.BattleInfo;
import java.util.ArrayList;
import java.util.Collections;

public class WeightedPredictor extends Predictor {

    
    final double[] WEIGHTS;
    final Predictor[] PREDICTORS;

    public WeightedPredictor(double[] weights, Predictor[] predictors) {
        WEIGHTS = weights;
        PREDICTORS = predictors;
    }

    @Override
    public ArrayList<GuessFactor> getGuessFactors(BattleInfo battleInfo) {
        ArrayList<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (int i = 0; i < PREDICTORS.length; i++) {
            Predictor predictor = PREDICTORS[i];
            ArrayList<GuessFactor> ithGuessFactors = predictor.getGuessFactors(battleInfo);
            double weightSum = 0;
            for (int j = 0; j < ithGuessFactors.size(); j++) {
                GuessFactor guessFactor = ithGuessFactors.get(j);
                weightSum += guessFactor.getWeight();
            }
            for (int j = 0; j < ithGuessFactors.size(); j++) {
                GuessFactor guessFactor = ithGuessFactors.get(j);
                weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, WEIGHTS[i] * guessFactor.getWeight() / weightSum, guessFactor.SCAN));
            }
        }
        return weightedGuessFactors;
    }

    @Override
    public void addData(BattleInfo battleInfo, GuessFactor guessFactor, boolean real) {
        for (int i = 0; i < PREDICTORS.length; i++) {
            PREDICTORS[i].addData(battleInfo, guessFactor, real);
        }
    }

}
