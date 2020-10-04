package dsekercioglu.mega.core;

import dsekercioglu.mega.core.jk.KDTree;
import dsekercioglu.mega.core.jk.KDTree.SearchResult;

import java.util.ArrayList;

public class KNNView {

    KDTree<GuessFactor> kdTree;

    final double[] WEIGHTS;
    final int K;
    final int DIVISOR;

    public KNNView(int k, int divisor, double[] weights) {
        K = k;
        DIVISOR = divisor;
        WEIGHTS = weights;
        kdTree = new KDTree.Manhattan<>(WEIGHTS.length);
    }

    public ArrayList<DistancedGuessFactor> nearestNeighbours(double[] searchPoint) {
        ArrayList<DistancedGuessFactor> guessFactors = new ArrayList<>();
        double[] weightedSearchPoint = weightDataPoints(searchPoint);
        ArrayList<SearchResult<GuessFactor>> nearestNeighbours = kdTree.nearestNeighbours(weightedSearchPoint, Math.max(Math.min(K, kdTree.size() / DIVISOR), 1));
        for (int i = 0; i < nearestNeighbours.size(); i++) {
            SearchResult result = nearestNeighbours.get(i);
            GuessFactor guessFactor = (GuessFactor) result.payload;

            DistancedGuessFactor distancedGuessFactor = new DistancedGuessFactor(guessFactor, result.distance);
            guessFactors.add(distancedGuessFactor);
        }
        return guessFactors;
    }

    public void addDataPoint(double[] dataPoint, GuessFactor guessFactor) {
        kdTree.addPoint(weightDataPoints(dataPoint), guessFactor);
    }

    private double[] weightDataPoints(double[] dataPoint) {
        double[] weightedDataPoint = new double[WEIGHTS.length];
        for (int i = 0; i < WEIGHTS.length; i++) {
            weightedDataPoint[i] = dataPoint[i] * WEIGHTS[i];
        }
        return weightedDataPoint;
    }
}
