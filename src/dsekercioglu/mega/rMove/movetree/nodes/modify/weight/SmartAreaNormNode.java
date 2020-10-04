package dsekercioglu.mega.rMove.movetree.nodes.modify.weight;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.movetree.nodes.Node;

import java.util.List;

public class SmartAreaNormNode extends Node {

	private final Node NODE;
	private final double ROLL_FACTOR;
	private final double SMOOTH_FACTOR;

	private final double MAX_VALUE;

	private double average = 1;


	public SmartAreaNormNode(Node node, double rollFactor, double bandwidth) {
		NODE = node;
		ROLL_FACTOR = rollFactor;
		SMOOTH_FACTOR = bandwidth;
		MAX_VALUE = 1 / (integrateDanger(1, 1, SMOOTH_FACTOR) - integrateDanger(-1, 1, SMOOTH_FACTOR));

	}

	@Override
	public List<GuessFactor> getGuessFactors(WaveData battleInfo) {
		List<GuessFactor> guessFactors = NODE.getGuessFactors(battleInfo);
		double area = 0;
		for (GuessFactor guessFactor : guessFactors) {
			area += integrateDanger(1 - guessFactor.GUESS_FACTOR, guessFactor.getWeight(), SMOOTH_FACTOR)
					- integrateDanger(-1 - guessFactor.GUESS_FACTOR, guessFactor.getWeight(), SMOOTH_FACTOR);
		}
		double newAverage = average * ROLL_FACTOR + average * (1 - ROLL_FACTOR);
		area /= MAX_VALUE * average;
		average = newAverage;
		for (GuessFactor guessFactor : guessFactors) {
			guessFactor.setWeight(guessFactor.getWeight() / area);
		}
		return guessFactors;
	}

	@Override
	public void addData(WaveData battleInfo, GuessFactor guessFactor, boolean real) {
		NODE.addData(battleInfo, guessFactor, real);
	}

	@Override
	public String toString() {
		return getName() + ":(" + NODE.toString() + ")";
	}

	private double integrateDanger(double value, double weight, double smoothFactor) {
		return smoothFactor * weight * FastMath.atan(value / smoothFactor);
	}

}
