package dsekercioglu.mega.core;

public class GuessFactor implements Comparable {

    public final double GUESS_FACTOR;
    private double weight;
    public final int SCAN;

    public GuessFactor(double guessFactor, double weight, int scan) {
        GUESS_FACTOR = guessFactor;
        this.weight = weight;
        SCAN = scan;
    }

    @Override
    public int compareTo(Object o) {
        Double otherGuessFactor = ((GuessFactor) o).GUESS_FACTOR;
        return otherGuessFactor.compareTo(GUESS_FACTOR);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double newWeight) {
        weight = newWeight;
    }

}
