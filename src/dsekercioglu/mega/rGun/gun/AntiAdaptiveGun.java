package dsekercioglu.mega.rGun.gun;

import dsekercioglu.mega.core.CapacityKNNView;
import dsekercioglu.mega.core.DistancedGuessFactor;
import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rGun.BattleInfo;

import java.util.ArrayList;
import java.util.List;

public class AntiAdaptiveGun extends Predictor {

    final double[] WEIGHTS = {5, 2, 5, 5, 1, 5, 5, 1};

    final int REAL_CAPACITY = 100;
    final int VIRTUAL_CAPACITY = 1000;
    final int REAL_K = 5;
    final int VIRTUAL_K = 50;
    final int DIVISOR = 10;
    final double VIRTUAL_WAVE_WEIGHT = 0.1;

    CapacityKNNView realKnnView;
    CapacityKNNView virtualKnnView;

    public AntiAdaptiveGun() {
        realKnnView = new CapacityKNNView(REAL_CAPACITY, REAL_K, DIVISOR, WEIGHTS);
        virtualKnnView = new CapacityKNNView(VIRTUAL_CAPACITY, VIRTUAL_K, DIVISOR, WEIGHTS);
        realKnnView.addDataPoint(new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GuessFactor(0, 0.0001, 0));
        virtualKnnView.addDataPoint(new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5}, new GuessFactor(0, 0.0001, 0));
    }

    @Override
    public ArrayList<GuessFactor> getGuessFactors(BattleInfo battleInfo) {
        double[] searchPoint = getDataPoint(battleInfo);
        List<GuessFactor> realGuessFactors = probabilityGuessFactors(weightGuessFactors(realKnnView.nearestNeighbours(searchPoint)));
        List<GuessFactor> unrealGuessFactors = probabilityGuessFactors(weightGuessFactors(virtualKnnView.nearestNeighbours(searchPoint)));
        ArrayList<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (int i = 0; i < realGuessFactors.size(); i++) {
            GuessFactor guessFactor = realGuessFactors.get(i);
            weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight() * (1D / REAL_K), guessFactor.SCAN));
        }
        for (int i = 0; i < unrealGuessFactors.size(); i++) {
            GuessFactor guessFactor = unrealGuessFactors.get(i);
            weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight() * VIRTUAL_WAVE_WEIGHT * (1D / VIRTUAL_K), guessFactor.SCAN));
        }
        return weightedGuessFactors;
    }

    private ArrayList<GuessFactor> weightGuessFactors(List<DistancedGuessFactor> distancedGuessFactors) {
        ArrayList<GuessFactor> weightedGuessFactors = new ArrayList<>();
        for (int i = 0; i < distancedGuessFactors.size(); i++) {
            DistancedGuessFactor distancedGuessFactor = distancedGuessFactors.get(i);
            GuessFactor guessFactor = distancedGuessFactor.getGuessFactor();
            GuessFactor weightedGuessFactor = new GuessFactor(guessFactor.GUESS_FACTOR,
                    guessFactor.getWeight() / (1 + distancedGuessFactor.getDistance()),
                    guessFactor.SCAN);
            weightedGuessFactors.add(weightedGuessFactor);
        }
        return weightedGuessFactors;
    }

    private ArrayList<GuessFactor> probabilityGuessFactors(List<GuessFactor> guessFactors) {
        ArrayList<GuessFactor> weightedGuessFactors = new ArrayList<>();
        double weightSum = 0;
        for (int j = 0; j < guessFactors.size(); j++) {
            GuessFactor guessFactor = guessFactors.get(j);
            weightSum += guessFactor.getWeight();
        }
        for (int j = 0; j < guessFactors.size(); j++) {
            GuessFactor guessFactor = guessFactors.get(j);
            weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight() / weightSum, guessFactor.SCAN));
        }
        return weightedGuessFactors;
    }


    @Override
    public void addData(BattleInfo battleInfo, GuessFactor guessFactor, boolean real) {
        if (real) {
            GuessFactor realWave = new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight(), guessFactor.SCAN);
            realKnnView.addDataPoint(getDataPoint(battleInfo), realWave);
        }
        GuessFactor virtual = new GuessFactor(guessFactor.GUESS_FACTOR, guessFactor.getWeight(), guessFactor.SCAN);
        virtualKnnView.addDataPoint(getDataPoint(battleInfo), virtual);

    }

    public double[] getDataPoint(BattleInfo battleInfo) {
        double bulletVelocity = (20 - 3 * battleInfo.getLastFirePower());
        double mea = FastMath.asin(8 / bulletVelocity);
        double latVel = Math.abs(battleInfo.getEnemyLateralVelocity()) / 8;
        double advVel = battleInfo.getEnemyAdvancingVelocity() / 16 + 0.5;
        double bft = (battleInfo.getBotDistance() / bulletVelocity) / 91;
        double forwardWallMEA = Math.min(battleInfo.getMEA(1) / mea, 1);
        double backwardWallMEA = Math.min(battleInfo.getMEA(-1) / mea, 1);
        double latAcc = (battleInfo.getEnemyLateralAcceleration() + 2) / 3;
        double firePower = (battleInfo.getLastFirePower() - 0.1) / 2.9;
        double timeSinceDecel = 1 / (battleInfo.getEnemyTimeSinceDeceleration() * 0.5 + 1);
        double shotsTaken = battleInfo.getFired();
        return new double[]{
                latVel,
                advVel,
                bft,
                forwardWallMEA,
                backwardWallMEA,
                latAcc,
                firePower,
                timeSinceDecel};
    }

}
