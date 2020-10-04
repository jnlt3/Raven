package dsekercioglu.mega.rGun.gun;

import dsekercioglu.mega.core.DistancedGuessFactor;
import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.KNNView;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rGun.BattleInfo;

import java.util.ArrayList;

public class MainGun extends Predictor {

    final double[] WEIGHTS = {6.47, 3.64, 8.151, 6.32, 0.98, 9.02, 2.74};
    //final double[] WEIGHTS = {6.47, 3.64, 8.151, 6.32, 0.98, 9.02};
    final int K = 125;
    final int DIVISOR = 1;
    KNNView knnView;

    public MainGun() {
        knnView = new KNNView(K, DIVISOR, WEIGHTS);
        knnView.addDataPoint(new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GuessFactor(0, 0.0001, 0));
        //knnView.addDataPoint(new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GuessFactor(0, 0.0001, 0));
    }

    @Override
    public ArrayList<GuessFactor> getGuessFactors(BattleInfo battleInfo) {
        ArrayList<DistancedGuessFactor> guessFactors = knnView.nearestNeighbours(getDataPoint(battleInfo));
        ArrayList<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (DistancedGuessFactor distancedGuessFactor : guessFactors) {
            GuessFactor guessFactor = distancedGuessFactor.getGuessFactor();
            GuessFactor weightedGuessFactor = new GuessFactor(guessFactor.GUESS_FACTOR,
                    guessFactor.getWeight() / (1 + distancedGuessFactor.getDistance()),
                    guessFactor.SCAN);
            weightedGuessFactors.add(weightedGuessFactor);
        }
        return weightedGuessFactors;
    }

    @Override
    public void addData(BattleInfo battleInfo, GuessFactor guessFactor, boolean real) {
        knnView.addDataPoint(getDataPoint(battleInfo), guessFactor);
    }

    public double[] getDataPoint(BattleInfo battleInfo) {
        /*
        return new double[]{Math.abs(battleInfo.getEnemyLateralVelocity()) / 8,
                (battleInfo.getEnemyAdvancingVelocity() + 8) / 16,
                (battleInfo.getBotDistance() / (20 - 3 * battleInfo.getLastFirePower())) / 91,
                Math.min(battleInfo.getMEA(1) / Math.PI * 2, 1),
                Math.min(battleInfo.getMEA(-1) / Math.PI * 2, 1),
                (battleInfo.getEnemyLateralAcceleration() + 2) / 3};

         */
        double mea = FastMath.asin(8 / (20 - 3 * battleInfo.getLastFirePower()));
        return new double[]{Math.abs(battleInfo.getEnemyLateralVelocity()) / 8,
                (battleInfo.getEnemyAdvancingVelocity() + 8) / 16,
                (battleInfo.getBotDistance() / (20 - 3 * battleInfo.getLastFirePower())) / 91,
                Math.min(battleInfo.getMEA(1) / mea, 1),
                Math.min(battleInfo.getMEA(-1) / mea, 1),
                (battleInfo.getEnemyLateralAcceleration() + 2) / 3,
                1 / (1 + battleInfo.getEnemyTimeSinceDeceleration() * 2)};
    }

}
