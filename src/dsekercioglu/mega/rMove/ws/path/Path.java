package dsekercioglu.mega.rMove.ws.path;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static robocode.util.Utils.normalRelativeAngle;

public class Path {

    private final List<State> STATES;
    private final List<double[]> INSTRUCTIONS;

    private State currentState;

    private int instructionIndex = 0;

    public Path(State rootState) {
        currentState = rootState;
        STATES = new ArrayList<>();
        STATES.add(rootState);
        INSTRUCTIONS = new ArrayList<>();
    }

    public void setMaxVelocity(double maxVelocity) {
        currentState.setMaxVelocity(maxVelocity);
    }

    public State getLastState() {
        return STATES.get(numberOfStates() - 1);
    }

    public void moveTowards(double angle) {
        State nextState = currentState.moveTowards(angle);
        add(nextState);
    }

    public void moveTo(Point2D.Double targetLocation) {
        State nextState = currentState.moveTo(targetLocation);
        add(nextState);
    }

    public void brake() {
        State nextState = currentState.brake();
        add(nextState);
    }

    private void add(State nextState) {
        double[] instructions = {nextState.getVelocity(),
                normalRelativeAngle(nextState.getHeading() - currentState.getHeading())};
        INSTRUCTIONS.add(instructions);
        STATES.add(nextState);
        currentState = nextState;
    }

    public int stopTime() {
        return currentState.stopTime();
    }

    public void removeLast() {
        int removeIndex = STATES.size() - 1;
        STATES.remove(removeIndex);
        INSTRUCTIONS.remove(removeIndex - 1);
        currentState = STATES.get(STATES.size() - 1);
    }

    public State getState(int index) {
        return STATES.get(index);
    }

    public boolean hasNext() {
        return instructionIndex < numberOfInstructions();
    }

    public double[] next() {
        return INSTRUCTIONS.get(instructionIndex++);
    }

    public int numberOfStates() {
        return STATES.size();
    }

    public int numberOfInstructions() {
        return INSTRUCTIONS.size();
    }

    public void reset() {
        instructionIndex = 0;
    }

    public void mergePath(Path path) {
        STATES.addAll(path.STATES);
    }

    @Override
    public String toString() {
        return "Path{" +
                "STATES=" + STATES +
                '}';
    }
}
