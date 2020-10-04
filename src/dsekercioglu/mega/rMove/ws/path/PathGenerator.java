package dsekercioglu.mega.rMove.ws.path;

import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.sim.Wave;
import dsekercioglu.mega.rMove.ws.path.aacalc.AttackAngleCalculator;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class PathGenerator {

    private static final double[] VELOCITIES = {-8, 0, 8};
    private static final int MAX_STOP_TIME = 5;

    private final double BATTLE_FIELD_WIDTH;
    private final double BATTLE_FIELD_HEIGHT;

    final double WALL_HIT_MARGIN = 18;

    private AttackAngleCalculator attackAngleCalculator;

    public PathGenerator(double battleFieldWidth, double battleFieldHeight) {
        BATTLE_FIELD_WIDTH = battleFieldWidth;
        BATTLE_FIELD_HEIGHT = battleFieldHeight;
    }

    public void setPathGenerator(AttackAngleCalculator attackAngleCalculator) {
        this.attackAngleCalculator = attackAngleCalculator;
    }

    public Path[] getExtremePaths(State currentState, State enemyState, Wave wave, int deltaTime) {
        int numberOfSimulations = getMaximumTimeLeft(wave, currentState.getLocation()) + MAX_STOP_TIME;

        Path[] extremePaths = new Path[2];
        extremePaths[0] = generatePath(attackAngleCalculator, currentState, enemyState, wave, deltaTime, -8, numberOfSimulations, -8);
        extremePaths[1] = generatePath(attackAngleCalculator, currentState, enemyState, wave, deltaTime, 8, numberOfSimulations, 8);
        return extremePaths;
    }

    public List<Path> generateRunAwayPaths(State currentState, State enemyState, Wave wave, int deltaTime) {
        List<Path> paths = new ArrayList<>(2);
        paths.add(generatePath(attackAngleCalculator, currentState, enemyState, wave, deltaTime, -8, 91, -8));
        paths.add(generatePath(attackAngleCalculator, currentState, enemyState, wave, deltaTime, 8, 91, 8));
        return filter(paths);
    }

    public List<Path> generateSimpleDistancingPaths(State currentState, State enemyState, Wave wave, int skippedCalculations) {
        return filter(generatePaths(attackAngleCalculator, currentState, enemyState, wave, 1, 0, skippedCalculations));
    }


    public List<Path> generateReverseDistancingPaths(State currentState, State enemyState, Wave wave, int skippedCalculations) {
        return filter(generatePaths(attackAngleCalculator, currentState, enemyState, wave, 1, -1, skippedCalculations));
    }

    public List<Path> generateAcceleratingDistancingPaths(State currentState, State enemyState, Wave wave, int skippedCalculations) {
        return filter(generatePaths(attackAngleCalculator, currentState, enemyState, wave, 0, 1, skippedCalculations));
    }

    private List<Path> generatePaths(AttackAngleCalculator attackAngleCalculator, State currentState, State enemyState, Wave wave, double startDirection, double finalTargetDirection, int skippedCalculations) {
        List<Path> paths = new ArrayList<>();
        Path[] extremePaths = getExtremePaths(currentState, enemyState, wave, currentState.getDeltaTime());
        int skipFactor = skippedCalculations + 1;
        int numberOfBackwardSimulations = extremePaths[0].numberOfStates() / skipFactor;
        int numberOfForwardSimulations = extremePaths[1].numberOfStates() / skipFactor;

        for (int i = -numberOfBackwardSimulations; i <= numberOfForwardSimulations; i++) {
            int iteration = Math.abs(i);
            int velocityIndex = Integer.compare(i, 0);
            int actualTime = iteration * skipFactor;
            double velocity = VELOCITIES[velocityIndex + 1];
            Path path = generatePath(attackAngleCalculator, currentState, enemyState, wave, currentState.getDeltaTime(), velocity * startDirection, actualTime, velocity * finalTargetDirection);
            paths.add(path);
        }
        paths.add(extremePaths[0]);
        paths.add(extremePaths[1]);
        return filter(paths);
    }

    private List<Path> filter(List<Path> paths) {
        int stopPoint = paths.size() / 2;
        List<Path> toRemove = new ArrayList<>();
        List<Point2D.Double> endLocations = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            Path currentPath = paths.get(i);
            Point2D.Double endLocation = currentPath.getState(currentPath.numberOfStates() - 1).getLocation();
            //double distance = getFirstIntersection(paths.get(i)).getLocation().distance(enemyState.getLocation());
            if (MoveUtils.distanceToWall(endLocation.x, endLocation.y, BATTLE_FIELD_WIDTH, BATTLE_FIELD_HEIGHT) < WALL_HIT_MARGIN) {
                toRemove.add(paths.get(i));
                if (i <= stopPoint) {
                    stopPoint--;
                }
            } else {
                endLocations.add(endLocation);
            }
        }
        paths.removeAll(toRemove);
        if (paths.size() > 7 && paths.size() > stopPoint + 1 && stopPoint > 0) {
            toRemove.clear();
            for (int i = stopPoint + 1; i < paths.size() - 1; i++) {
                Point2D.Double endLocation = endLocations.get(i);
                if (endLocations.get(i - 1).distance(endLocation) < 7) {
                    toRemove.add(paths.get(i));
                    i++;
                }
            }
            for (int i = stopPoint - 1; i > 0; i--) {
                Point2D.Double endLocation = endLocations.get(i);
                if (endLocations.get(i + 1).distance(endLocation) < 7) {
                    toRemove.add(paths.get(i));
                    i--;
                }
            }
            paths.removeAll(toRemove);
        }
        return paths;
    }

    private Path generatePath(AttackAngleCalculator attackAngleCalculator, State startState, State enemyState, Wave wave, int deltaTime, double targetVelocity, int directionChangeTime, double finalTargetVelocity) {
        Path path = new Path(startState);
        int moveTime = 0;
        boolean changingMovementPlan = false;
        double absoluteTargetVelocity = Math.abs(targetVelocity);
        path.setMaxVelocity(absoluteTargetVelocity);
        boolean intersected = false;

        State predictedEnemyState = new State((Point2D.Double) enemyState.getLocation().clone(), enemyState.getVelocity(), enemyState.getHeading(), enemyState.getDeltaTime());
        predictedEnemyState.setMaxVelocity(Math.abs(predictedEnemyState.getVelocity()));

        while (moveTime <= 91) {
            List<Point2D.Double> waveIntersection = calculateIntersection(path.getLastState().getLocation(), wave, moveTime + deltaTime);
            waveIntersection.addAll(calculateCornerIntersection(path.getLastState().getLocation(), wave, moveTime + deltaTime));
            if (!waveIntersection.isEmpty()) {
                path.getLastState().setIntersecting(true);
                path.getLastState().setIntersection(waveIntersection);
                intersected = true;
            } else if (intersected) {
                return path;
            }
            double moveAngle = attackAngleCalculator.getAttackAngle(wave, path.getLastState().getLocation(), predictedEnemyState.getLocation(), predictedEnemyState.getHeading(), targetVelocity);
            path.moveTowards(moveAngle);
            if (!changingMovementPlan && moveTime >= directionChangeTime) {
                targetVelocity = finalTargetVelocity;
                path.setMaxVelocity(Math.abs(targetVelocity));
                changingMovementPlan = true;
            }
            if (changingMovementPlan && (finalTargetVelocity == 0) && path.getLastState().getVelocity() == 0) {
                if (!intersected) {
                    path.getLastState().setIntersecting(true);
                    path.getLastState().setIntersection(getDefaultIntersection(path.getLastState().getLocation()));
                    return path;
                }
            }

            if (MoveUtils.distanceToWall(path.getLastState().getLocation().x, path.getLastState().getLocation().y, BATTLE_FIELD_WIDTH, BATTLE_FIELD_HEIGHT) < WALL_HIT_MARGIN
                    || path.getLastState().getLocation().distance(enemyState.getLocation()) < 18) {
                if (!intersected) {
                    path.getLastState().setIntersecting(true);
                    path.getLastState().setIntersection(getDefaultIntersection(path.getLastState().getLocation()));
                    return path;
                }
            }
            //predictedEnemyState = predictedEnemyState.moveTowards(predictedEnemyState.getHeading());
            moveTime++;
        }
        if (path.numberOfStates() <= 1) {
            System.out.println("ERROR IN PATH GENERATION");
        }
        return path;
    }

    private List<Point2D.Double> calculateIntersection(Point2D.Double location, Wave wave, int deltaTime) {
        double waveRadius = wave.getDistanceTraveled() + deltaTime * wave.getWaveVelocity();
        double previousWaveRadius = wave.getDistanceTraveled() + (deltaTime - 1) * wave.getWaveVelocity();
        Point2D.Double waveSource = wave.getSource();
        Ellipse2D.Double waveCircle = new Ellipse2D.Double(
                waveSource.x - waveRadius,
                waveSource.y - waveRadius,
                waveRadius * 2,
                waveRadius * 2
        );
        Ellipse2D.Double previousWaveCircle = new Ellipse2D.Double(
                waveSource.x - previousWaveRadius,
                waveSource.y - previousWaveRadius,
                previousWaveRadius * 2,
                previousWaveRadius * 2
        );

        double cornerX = location.x - 18;
        double cornerY = location.y - 18;
        Rectangle2D.Double botBoundingBox = new Rectangle2D.Double(cornerX, cornerY, 36, 36);
        List<Point2D.Double> intersectionPoints = MoveUtils.rectangleCircleIntersection(waveCircle, botBoundingBox);
        intersectionPoints.addAll(MoveUtils.rectangleCircleIntersection(previousWaveCircle, botBoundingBox));
        return intersectionPoints;
    }

    private List<Point2D.Double> calculateCornerIntersection(Point2D.Double location, Wave wave, int deltaTime) {
        List<Point2D.Double> intersectionPoints = new ArrayList<>();
        for (int x = -18; x <= 18; x += 36) {
            for (int y = -18; y <= 18; y += 36) {
                Point2D.Double cornerPosition = new Point2D.Double(location.x + x, location.y + y);
                if (MoveUtils.waveIntersectsPoint(
                        cornerPosition,
                        wave.getSource(),
                        wave.getWaveVelocity(),
                        wave.getDistanceTraveled() + deltaTime * wave.getWaveVelocity())) {
                    intersectionPoints.add(cornerPosition);
                }
            }
        }
        return intersectionPoints;
    }

    private List<Point2D.Double> getDefaultIntersection(Point2D.Double location) {
        List<Point2D.Double> intersectionPoints = new ArrayList<>();
        for (int x = -18; x <= 18; x += 36) {
            for (int y = -18; y <= 18; y += 36) {
                Point2D.Double cornerPosition = new Point2D.Double(location.x + x, location.y + y);
                intersectionPoints.add(cornerPosition);
            }
        }
        return intersectionPoints;
    }

    public int getTimeLeft(Wave wave, Point2D.Double location) {
        return (int) Math.ceil((wave.getSource().distance(location) - wave.getDistanceTraveled()) / wave.getWaveVelocity());
    }

    public int getMaximumTimeLeft(Wave wave, Point2D.Double location) {
        return (int) Math.min(Math.ceil((wave.getSource().distance(location) - wave.getDistanceTraveled()) / (wave.getWaveVelocity() - 8)), 91);
    }
}
