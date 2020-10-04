package dsekercioglu.mega.rGun.gun;

import dsekercioglu.mega.core.DistancedGuessFactor;
import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.KNNView;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rGun.BattleInfo;

import java.util.ArrayList;

public class ASGun extends Predictor {

    final double[] WEIGHTS = {1, 1, 1, 1, 1};

    final int K = 25;
    final int DIVISOR = 1;
    final double DECAY_FACTOR = 2.5;
    final double VIRTUAL_WAVE_WEIGHT = 0.5;

    KNNView knnView;

    public ASGun() {
        knnView = new KNNView(K, DIVISOR, WEIGHTS);
        knnView.addDataPoint(new double[]{0.5, 0.5, 0.5, 0.5, 0.5}, new GuessFactor(0, 0.0001, 0));
    }

    @Override
    public ArrayList<GuessFactor> getGuessFactors(BattleInfo battleInfo) {
        ArrayList<DistancedGuessFactor> distancedGuessFactors = knnView.nearestNeighbours(getDataPoint(battleInfo));
        ArrayList<GuessFactor> guessFactors = new ArrayList<>();
        int maxScan = 0;
        for (DistancedGuessFactor distancedGuessFactor : distancedGuessFactors) {
            GuessFactor guessFactor = distancedGuessFactor.getGuessFactor();
            GuessFactor weightedGuessFactor = new GuessFactor(guessFactor.GUESS_FACTOR,
                    guessFactor.getWeight() / (1 + distancedGuessFactor.getDistance()),
                    guessFactor.SCAN);
            guessFactors.add(weightedGuessFactor);
            maxScan = Math.max(guessFactor.SCAN, maxScan);
        }
        ArrayList<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (GuessFactor guessFactor : guessFactors) {
            weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight() * Math.pow(DECAY_FACTOR, (guessFactor.SCAN - maxScan) / guessFactor.getWeight()), guessFactor.SCAN));
        }
        return weightedGuessFactors;
    }

    @Override
    public void addData(BattleInfo battleInfo, GuessFactor guessFactor, boolean real) {
        if (real) {
            GuessFactor realWave = new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight(), guessFactor.SCAN);
            knnView.addDataPoint(getDataPoint(battleInfo), realWave);
        } else {
            GuessFactor virtual = new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight() * VIRTUAL_WAVE_WEIGHT, guessFactor.SCAN);
            knnView.addDataPoint(getDataPoint(battleInfo), virtual);
        }
    }

    public double[] getDataPoint(BattleInfo battleInfo) {
        double bulletVelocity = (20 - 3 * battleInfo.getLastFirePower());
        double mea = FastMath.asin(8 / bulletVelocity);
        return new double[]{Math.abs(battleInfo.getEnemyLateralVelocity()) / 8,
                (battleInfo.getBotDistance() / bulletVelocity) / 91,
                Math.min(battleInfo.getMEA(1) / mea, 1),
                (battleInfo.getEnemyLateralAcceleration() + 2) / 3,
                (battleInfo.getLastFirePower() - 0.1) / 2.9};
    }

}
