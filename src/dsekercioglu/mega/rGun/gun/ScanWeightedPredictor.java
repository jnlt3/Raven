package dsekercioglu.mega.rGun.gun;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.Pair;
import dsekercioglu.mega.rGun.BattleInfo;

import java.util.ArrayList;
import java.util.Collections;

public class ScanWeightedPredictor extends Predictor {

    final Predictor[] PREDICTORS;
    final double DECAY_FACTOR;

    public ScanWeightedPredictor(double decayFactor, Predictor... predictors) {
        DECAY_FACTOR = decayFactor;
        PREDICTORS = predictors;
    }

    @Override
    public ArrayList<GuessFactor> getGuessFactors(BattleInfo battleInfo) {
        ArrayList<Pair<GuessFactor, Integer>> scanOrderedGuessFactors = new ArrayList<>();
        for (int i = 0; i < PREDICTORS.length; i++) {
            Predictor predictor = PREDICTORS[i];
            ArrayList<GuessFactor> ithGuessFactors = predictor.getGuessFactors(battleInfo);
            for (int j = 0; j < ithGuessFactors.size(); j++) {
                GuessFactor guessFactor = ithGuessFactors.get(j);
                scanOrderedGuessFactors.add(new Pair<>(guessFactor, guessFactor.SCAN));
            }
        }
        Collections.sort(scanOrderedGuessFactors);
        ArrayList<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (int i = 0; i < scanOrderedGuessFactors.size(); i++) {
            Pair<GuessFactor, Integer> pair = scanOrderedGuessFactors.get(i);
            GuessFactor guessFactor = pair.getObject();
            weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, Math.pow(DECAY_FACTOR, i), 0));
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
