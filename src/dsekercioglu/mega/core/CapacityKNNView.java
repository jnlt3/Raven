package dsekercioglu.mega.core;

import java.util.ArrayList;
import java.util.List;

public class CapacityKNNView {


    private final int CAPACITY;
    final double[] WEIGHTS;
    final int K;
    final int DIVISOR;

    List<double[]> locations = new ArrayList<>();
    List<GuessFactor> guessFactors = new ArrayList<>();

    public CapacityKNNView(int capacity, int k, int divisor, double[] weights) {
        CAPACITY = capacity;
        K = k;
        DIVISOR = divisor;
        WEIGHTS = weights;
    }

    public List<DistancedGuessFactor> nearestNeighbours(double[] searchPoint) {
        ArrayList<DistancedGuessFactor> distancedGuessFactors = new ArrayList<>();
        double[] weightedSearchPoint = weightDataPoints(searchPoint);
        ArrayList<Double> distances = new ArrayList<>();
        for (int i = 0; i < locations.size(); i++) {
            double[] dataPoint = locations.get(i);
            double distance = 0;
            for (int j = 0; j < dataPoint.length; j++) {
                double difference = dataPoint[j] - weightedSearchPoint[j];
                distance += Math.abs(difference);
            }
            distances.add(distance);
            distancedGuessFactors.add(new DistancedGuessFactor(guessFactors.get(i), distance));
        }
        int k = Math.max(Math.min(K / DIVISOR, locations.size()), 1);
        List<DistancedGuessFactor> sortedGuessFactors = sortFirstX(k, distances, distancedGuessFactors);
        return sortedGuessFactors;
    }

    private static <T> List sortFirstX(int x, List<Double> sortBy, List<T> toSort) {
        List<T> sorted = new ArrayList<>();
        List<Double> sortedValues = new ArrayList<>();
        for (int i = 0; i < sortBy.size(); i++) {
            double value = sortBy.get(i);
            if (sorted.size() < x) {
                sorted.add(toSort.get(i));
                sortedValues.add(value);
            } else {
                int lowestValueIndex = -1;
                double highestValue = Double.NEGATIVE_INFINITY;
                for (int j = 0; j < sorted.size(); j++) {
                    double currentValue = sortedValues.get(j);
                    if (currentValue > highestValue) {
                        highestValue = currentValue;
                        lowestValueIndex = j;
                    }
                }
                if (highestValue > sortBy.get(i)) {
                    sorted.set(lowestValueIndex, toSort.get(i));
                    sortedValues.set(lowestValueIndex, sortBy.get(i));
                }
            }
        }
        return sorted;
    }

    public void addDataPoint(double[] searchPoint, GuessFactor guessFactor) {
        double[] weightedPoints = weightDataPoints(searchPoint);
        locations.add(weightedPoints);
        guessFactors.add(guessFactor);
        if (locations.size() >= CAPACITY) {
            locations.remove(0);
            guessFactors.remove(0);
        }
    }

    private double[] weightDataPoints(double[] dataPoint) {
        double[] weightedDataPoint = new double[WEIGHTS.length];
        for (int i = 0; i < WEIGHTS.length; i++) {
            weightedDataPoint[i] = dataPoint[i] * WEIGHTS[i];
        }
        return weightedDataPoint;
    }
}
