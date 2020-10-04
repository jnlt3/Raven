package dsekercioglu.mega.rMove.movetree.nodes.modify.weight;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class VirtualNode extends Node {

    private final double SMOOTH_FACTOR;
    private final double HIT_LEARNING_RATE;
    private final double MISS_LEARNING_RATE;
    private final double MOMENTUM;
    private final double BETA;
    private final double WEIGHT_DECAY;
    private final Node[] NODES;
    private final int NUM_PREDICTORS;


    private final double[] WEIGHTS;

    private final double[] STEPS;
    private final double[] RMS;

    public VirtualNode(double[] initialWeights, double smoothFactor, double weightDecay, double hitLearningRate, double missLearningRate, double momentum, double beta, Node... nodes) {
        SMOOTH_FACTOR = smoothFactor;
        WEIGHT_DECAY = weightDecay;
        HIT_LEARNING_RATE = hitLearningRate;
        MISS_LEARNING_RATE = missLearningRate;
        MOMENTUM = momentum;
        BETA = beta;
        NODES = nodes;
        NUM_PREDICTORS = NODES.length;
        WEIGHTS = initialWeights;
        STEPS = new double[NUM_PREDICTORS];
        RMS = new double[NUM_PREDICTORS];
    }

    @Override
    public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
        List<GuessFactor>[] guessFactors = new List[NUM_PREDICTORS];
        for (int i = 0; i < NUM_PREDICTORS; i++) {
            guessFactors[i] = NODES[i].getGuessFactors(battleInfo);
        }
        List<GuessFactor> weightedGuessFactors = new ArrayList<>();
        double[] softmaxWeights = softmax(WEIGHTS);
        for (int i = 0; i < NUM_PREDICTORS; i++) {
            for (int j = 0; j < guessFactors[i].size(); j++) {
                GuessFactor guessFactor = guessFactors[i].get(j);
                weightedGuessFactors.add(new GuessFactor(guessFactor.GUESS_FACTOR, softmaxWeights[i] * guessFactor.getWeight(), guessFactor.SCAN));
            }
        }
        return weightedGuessFactors;
    }

    @Override
    public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
        double learningRate = real ? HIT_LEARNING_RATE : MISS_LEARNING_RATE;

        List<GuessFactor>[] guessFactors = new List[NUM_PREDICTORS];

        for (int i = 0; i < NUM_PREDICTORS; i++) {
            guessFactors[i] = NODES[i].getGuessFactors(battleInfo);
            probabilityGuessFactors(guessFactors[i]);
        }
        double[] softmaxWeights = softmax(WEIGHTS);

        double[] error = real ? getErrorForHitGuessFactors(guessFactors, softmaxWeights, guessFactor) : getErrorForMissedGuessFactors(guessFactors, softmaxWeights, guessFactor);
        double[] steps = new double[NUM_PREDICTORS];

        double[] derivatives = softmaxDerivative(WEIGHTS);
        for (int i = 0; i < NUM_PREDICTORS; i++) {
            steps[i] = error[i] * derivatives[i];
        }
        for (int i = 0; i < NUM_PREDICTORS; i++) {
            WEIGHTS[i] *= WEIGHT_DECAY;
            STEPS[i] = MOMENTUM * STEPS[i] + (1 - MOMENTUM) * steps[i] * learningRate;
            RMS[i] = BETA * RMS[i] + (1 - BETA) * MoveUtils.sq(steps[i]);
            WEIGHTS[i] -= STEPS[i] / (Math.sqrt(RMS[i]) + 1e-8);
        }

        for (Node node : NODES) {
            node.addData(battleInfo, guessFactor, real);
        }
    }

    @Override
    public String toString() {
        double[] virtualLearnWeights = getWeights();
        StringBuilder out = new StringBuilder("\n");
        for (int i = 0; i < virtualLearnWeights.length; i++) {
            out.append(NODES[i].toString()).append(": ").append(((int) (virtualLearnWeights[i] * 1000)) / 10D).append("%").append("\n");
        }
        return out.toString();
    }

    private void probabilityGuessFactors(List<GuessFactor> guessFactors) {
        double max = 0;
        double min = Double.POSITIVE_INFINITY;
        for (GuessFactor guessFactor0 : guessFactors) {
            double current = 0;
            for (GuessFactor guessFactor1 : guessFactors) {
                current += guessFactor1.getWeight() / (1 + MoveUtils.sq((guessFactor0.GUESS_FACTOR - guessFactor1.GUESS_FACTOR) / SMOOTH_FACTOR));
            }
            max = Math.max(current, max);
            min = Math.min(guessFactor0.getWeight(), min);
        }
        double diff = max - min + 1e-8;
        for (GuessFactor guessFactor : guessFactors) {
            guessFactor.setWeight((guessFactor.getWeight() - min) / diff);
        }
    }


    private double[] getErrorForHitGuessFactors(List<GuessFactor>[] guessFactors, double[] weights, GuessFactor compareGuessFactor) {
        return hitDerivative(guessFactors, weights, compareGuessFactor.GUESS_FACTOR);
    }

    private double[] getErrorForMissedGuessFactors(List<GuessFactor>[] guessFactors, double[] weights, GuessFactor compareGuessFactor) {
        return missDerivative(guessFactors, weights, compareGuessFactor.GUESS_FACTOR);
    }


    private double[] hitDerivative(List<GuessFactor>[] predictorGuessFactors, double[] weights, double value) {
        double dangerValue = 0;
        double[] derivatives = new double[predictorGuessFactors.length];
        for (int i = 0; i < predictorGuessFactors.length; i++) {
            List<GuessFactor> guessFactors = predictorGuessFactors[i];
            for (GuessFactor guessFactor : guessFactors) {
                double difference = guessFactor.GUESS_FACTOR - value;
                double currentDangerValue = guessFactor.getWeight() / (MoveUtils.sq(difference / SMOOTH_FACTOR) + 1);
                dangerValue += weights[i] * currentDangerValue;
                derivatives[i] += currentDangerValue;
            }
        }
        dangerValue = 2 * (dangerValue - 1);
        for (int i = 0; i < derivatives.length; i++) {
            derivatives[i] *= dangerValue;
        }
        return derivatives;
    }

    private double[] missDerivative(List<GuessFactor>[] predictorGuessFactors, double[] weights, double value) {
        double dangerValue = 0;
        double[] derivatives = new double[predictorGuessFactors.length];
        for (int i = 0; i < predictorGuessFactors.length; i++) {
            List<GuessFactor> guessFactors = predictorGuessFactors[i];
            for (int j = 0; j < guessFactors.size(); j++) {
                GuessFactor guessFactor = guessFactors.get(j);
                double difference = guessFactor.GUESS_FACTOR - value;
                double currentDangerValue = guessFactor.getWeight() / (MoveUtils.sq(difference / SMOOTH_FACTOR) + 1);
                dangerValue += weights[i] * currentDangerValue;
                derivatives[i] += currentDangerValue;
            }
        }
        dangerValue *= 2;
        for (int i = 0; i < derivatives.length; i++) {
            derivatives[i] *= dangerValue;
        }
        return derivatives;
    }

    public double[] getWeights() {
        return softmax(WEIGHTS);
    }

    private double[] softmax(double[] values) {
        double sum = 0;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            max = Math.max(max, values[i]);
        }
        double[] outValue = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            double expValue = Math.exp(values[i] - max);
            outValue[i] = expValue;
            sum += expValue;
        }
        for (int i = 0; i < values.length; i++) {
            outValue[i] /= sum;
        }
        return outValue;
    }

    private double[] softmaxDerivative(double[] values) {
        double sum = 0;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            max = Math.max(max, values[i]);
        }
        double[] expValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            double expValue = Math.exp(values[i] - max);
            expValues[i] = expValue;
            sum += expValue;
        }
        double[] derivatives = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            derivatives[i] = expValues[i] * (sum - expValues[i]) / (sum * sum);
        }
        return derivatives;
    }
}
