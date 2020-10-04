package dsekercioglu.mega.rMove.ws;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.Pair;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.MoveUtils;
import dsekercioglu.mega.rMove.info.EnemyTargetingInfo;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.info.battle.BattleInfo;
import dsekercioglu.mega.rMove.info.battle.BattleSummary;
import dsekercioglu.mega.rMove.sim.Wave;
import dsekercioglu.mega.rMove.ws.path.Path;
import dsekercioglu.mega.rMove.ws.path.PathGenerator;
import dsekercioglu.mega.rMove.ws.path.State;
import dsekercioglu.mega.rMove.ws.path.aacalc.FancyDistancer;
import robocode.AdvancedRobot;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WaveSurfingGT {

    private final double SMOOTH_FACTOR;

    private static final int SECOND_WAVE_BINS = 51;
    private static final double SECOND_WAVE_WEIGHT = 0.2;

    private final AdvancedRobot ROBOT;

    private Wave currentSurfWave = null;

    private Wave[] oldSurfWaves = new Wave[2];

    private double[] secondWaveDangers;
    private double minSecondWaveDanger;

    private final PathGenerator PATH_GENERATOR;
    private Path currentPath;

    private boolean waveUpdate;

    List<Path> allPaths = new ArrayList<>();

    public WaveSurfingGT(AdvancedRobot robot, double smoothFactor) {
        ROBOT = robot;
        SMOOTH_FACTOR = smoothFactor;
        double battleFieldWidth = robot.getBattleFieldWidth();
        double battleFieldHeight = robot.getBattleFieldHeight();
        PATH_GENERATOR = new PathGenerator(battleFieldWidth, battleFieldHeight);
        PATH_GENERATOR.setPathGenerator(new FancyDistancer(battleFieldWidth, battleFieldHeight));
    }

    public void run() {
        currentPath = null;
        waveUpdate = true;
    }

    public void surf(List<Wave> waves, BattleInfo battleInfo, BattleSummary battleSummary) {

        currentSurfWave = null;
        Wave surfWave2 = null;
        Wave[] surfWaves = getSurfWaves(waves, battleInfo.getBotLocation());
        if (!Arrays.equals(surfWaves, oldSurfWaves)) {
            waveUpdate = true;
        }
        oldSurfWaves = surfWaves;
        boolean wavesOnAir;
        if (surfWaves != null) {
            currentSurfWave = surfWaves[0];
            if (surfWaves.length > 1) {
                surfWave2 = surfWaves[1];
            }
            wavesOnAir = true;
        } else {
            wavesOnAir = false;
            EnemyTargetingInfo enemyTargetingInfo = new EnemyTargetingInfo(battleInfo.getEnemyLocation(), battleInfo.getAbsoluteBearing() + Math.PI);
            currentSurfWave = new Wave(enemyTargetingInfo, new WaveData(battleSummary, 3), battleSummary, 0);
            surfWave2 = new Wave(enemyTargetingInfo, new WaveData(battleSummary, 3), battleSummary, -16);
            ArrayList<GuessFactor> headOn = new ArrayList<>();
            headOn.add(new GuessFactor(0, 1, 0));
            currentSurfWave.setGuessFactors(headOn);
            surfWave2.setGuessFactors(headOn);
        }

        if (surfWave2 != null) {
            secondWaveDangers = getSecondWaveDangers(surfWave2, battleInfo.getBotLocation(), battleInfo);
        }

        if (!wavesOnAir || waveUpdate || !currentPath.hasNext()) {
            double startVelocity = battleInfo.getBotVelocity();
            double startHeading = battleInfo.getBotHeading();
            State currentState = new State((Point2D.Double) battleInfo.getBotLocation().clone(), startVelocity, startHeading, 0);
            State enemyState = new State((Point2D.Double) battleInfo.getEnemyLocation().clone(), battleInfo.getEnemyVelocity(), battleInfo.getEnemyHeading(), 0);

            allPaths.clear();
            List<Path> paths = new ArrayList<>();
            if (wavesOnAir) {
                paths.addAll(PATH_GENERATOR.generateSimpleDistancingPaths(currentState, enemyState, currentSurfWave, 2));
                if (paths.isEmpty()) {
                    paths.addAll(PATH_GENERATOR.generateRunAwayPaths(currentState, enemyState, currentSurfWave, 0));
                }
            } else {
                paths.addAll(PATH_GENERATOR.generateRunAwayPaths(currentState, enemyState, currentSurfWave, 0));
            }
            Path pathToTake = safestPath(paths, enemyState, currentSurfWave, surfWave2, battleInfo, true);
            if (pathToTake == null) {
                System.out.println("NO SOLUTIONS FOUND");
            } else {
                allPaths.addAll(paths);
                currentPath = pathToTake;
                currentPath.reset();
                waveUpdate = false;
            }
        }

        if (currentPath != null && currentPath.hasNext()) {
            double[] moveInstructions = currentPath.next();
            double velocity = moveInstructions[0];
            double direction = Math.signum(velocity);
            ROBOT.setMaxVelocity(direction * velocity);
            ROBOT.setAhead(direction * 36);
            ROBOT.setTurnRightRadians(moveInstructions[1]);
        } else {
            System.out.println("NO PATH GENERATED IN TIME");
            ROBOT.setMaxVelocity(0);
        }
    }

    public Path safestPath(List<Path> paths, State enemyState, Wave firstWave, Wave secondWave, BattleInfo battleInfo, boolean useIntersection) {
        List<Pair<PathIntersectionDanger, Double>> orderedPaths = new ArrayList<>();
        Path pathToTake = null;
        double lowestDanger = Double.POSITIVE_INFINITY;
        for (Path path : paths) {
            int index = path.numberOfStates() - 1;
            List<State> intersection = new ArrayList<>();
            if (useIntersection) {
                State state = path.getState(index);
                while (!state.isIntersecting() && index > 0) {
                    index--;
                    state = path.getState(index);
                }
                while (state.isIntersecting() && index >= 0) {
                    intersection.add(state);
                    state = path.getState(index);
                    index--;
                }
            } else {
                intersection.add(path.getState(index));
            }
            double danger = getDanger(firstWave, intersection, battleInfo);
            double positionalDanger = getPositionalDanger(intersection, battleInfo);
            if (secondWave == null) {
                danger *= positionalDanger;
                if (danger < lowestDanger) {
                    pathToTake = path;
                    lowestDanger = danger;
                }
            } else {
                orderedPaths.add(new Pair<>(new PathIntersectionDanger(path, intersection, positionalDanger), danger));
            }
        }
        if (secondWave == null) {
            return pathToTake;
        }
        Collections.sort(orderedPaths);
        for (Pair<PathIntersectionDanger, Double> pathIntersection : orderedPaths) {
            double danger = pathIntersection.getComparable();
            PathIntersectionDanger pid = pathIntersection.getObject();
            List<State> intersection = pid.INTERSECTION;
            if ((danger + minSecondWaveDanger) * pid.DANGER > lowestDanger) {
                break;
            }
            Path path = pathIntersection.getObject().PATH;
            danger += getCachedDanger(secondWave, intersection.get(intersection.size() - 1), battleInfo, secondWaveDangers);
            danger *= pid.DANGER;
            if (danger < lowestDanger) {
                lowestDanger = danger;
                pathToTake = path;
            }
        }
        return pathToTake;
    }


    public void onWaveUpdate() {
        waveUpdate = true;
    }

    private int getTimeLeft(Wave wave, Point2D.Double location) {
        return (int) Math.ceil((wave.getSource().distance(location) - 18 - wave.getDistanceTraveled()) / wave.getWaveVelocity());
    }

    /**
     * @param waves
     * @param location
     * @return Returns the first and second wave to surf if they exist
     */
    private Wave[] getSurfWaves(List<Wave> waves, Point2D.Double location) {
        ArrayList<Pair<Wave, Integer>> orderedWaves = new ArrayList<>();
        for (Wave wave : waves) {
            int timeLeft = getTimeLeft(wave, location);
            if (timeLeft >= 0) {
                Pair<Wave, Integer> pair = new Pair<>(wave, timeLeft);
                orderedWaves.add(pair);
            }
        }
        if (orderedWaves.isEmpty()) {
            return null;
        }
        if (orderedWaves.size() == 1) {
            return new Wave[]{orderedWaves.get(0).getObject()};
        }
        if (orderedWaves.size() == 2) {
            Wave wave0 = orderedWaves.get(0).getObject();
            Wave wave1 = orderedWaves.get(1).getObject();
            if (getTimeLeft(wave0, location) < getTimeLeft(wave1, location)) {
                return new Wave[]{wave0, wave1};
            }
            return new Wave[]{wave1, wave0};
        }

        Collections.sort(orderedWaves);
        return new Wave[]{orderedWaves.get(0).getObject(), orderedWaves.get(1).getObject()};
    }

    private Point2D.Double[] getSimulatedMEA(State currentState, State enemyState, Wave surfWave, int deltaTime) {
        Path[] paths = PATH_GENERATOR.getExtremePaths(currentState, enemyState, surfWave, deltaTime);
        return new Point2D.Double[]{
                paths[0].getLastState().getLocation(),
                paths[1].getLastState().getLocation()};
    }

    private double getPositionalDanger(List<State> intersection, BattleInfo battleInfo) {
        double distance = intersection.get(0).getLocation().distance(battleInfo.getEnemyLocation()) - 36;
        return 100 / (distance);
    }

    private double getEscapeSafety(State currentState, BattleInfo battleInfo, BattleSummary battleSummary) {
        EnemyTargetingInfo enemyTargetingInfo = new EnemyTargetingInfo(battleInfo.getEnemyLocation(), battleInfo.getAbsoluteBearing() + Math.PI);
        Wave meaWave = new Wave(enemyTargetingInfo, new WaveData(battleSummary, 3), battleSummary, 0);
        State enemyState = new State(battleInfo.getEnemyLocation(), 0, battleInfo.getEnemyHeading(), 0);
        Point2D.Double[] meaPoints = getSimulatedMEA(currentState, enemyState, meaWave, 0);
        double startGuessFactor = meaWave.getGuessFactor(meaPoints[0].x, meaPoints[0].y);
        double endGuessFactor = meaWave.getGuessFactor(meaPoints[1].x, meaPoints[1].y);
        return Math.abs(endGuessFactor - startGuessFactor) * meaWave.getMEA();
    }

    private double getDanger(Wave surfWave, List<State> intersection, BattleInfo battleInfo) {
        double hitProbability = calculateDanger(intersection, surfWave);
        return (hitProbability * (surfWave.getWaveDamage() + surfWave.getWavePower() * 3));
    }

    private double getCachedDanger(Wave wave, State state, BattleInfo battleInfo, double[] secondWaveDangers) {
        double secondWaveDanger = Double.POSITIVE_INFINITY;
        Point2D.Double[] pointsMEA = getSimulatedMEA(state,
                new State(battleInfo.getEnemyLocation(), battleInfo.getEnemyVelocity(), battleInfo.getEnemyHeading(), state.getDeltaTime()),
                wave,
                state.getDeltaTime()
        );
        double startGuessFactor = wave.getGuessFactor(pointsMEA[0].x, pointsMEA[0].y);
        double endGuessFactor = wave.getGuessFactor(pointsMEA[1].x, pointsMEA[1].y);
        double temp = startGuessFactor;
        startGuessFactor = Math.min(temp, endGuessFactor);
        endGuessFactor = Math.max(temp, endGuessFactor);
        int startBin = Math.max((int) ((startGuessFactor + 1) / 2 * (SECOND_WAVE_BINS - 1)), 0);
        int endBin = Math.min((int) ((endGuessFactor + 1) / 2 * (SECOND_WAVE_BINS - 1)), SECOND_WAVE_BINS - 1);
        for (int i = startBin; i <= endBin; i++) {
            secondWaveDanger = Math.min(secondWaveDanger, secondWaveDangers[i]);
        }
        return secondWaveDanger;
    }

    private double calculateDanger(List<State> botIntersection, Wave wave) {
        double[] guessFactors = calculateInterval(botIntersection, wave);
        return wave.getDanger(guessFactors[0], guessFactors[1], SMOOTH_FACTOR);
    }

    private double[] calculateInterval(List<State> botIntersection, Wave wave) {
        double maxGuessFactor = Double.NEGATIVE_INFINITY;
        double minGuessFactor = Double.POSITIVE_INFINITY;
        for (State state : botIntersection) {
            List<Point2D.Double> intersectingPoints = state.getIntersection();
            if (intersectingPoints == null) {
                intersectingPoints = getDefaultIntersection(state.getLocation());
            }
            for (Point2D.Double intersectingPoint : intersectingPoints) {
                double guessFactor = wave.getGuessFactor(intersectingPoint.x, intersectingPoint.y);
                maxGuessFactor = Math.max(maxGuessFactor, guessFactor);
                minGuessFactor = Math.min(minGuessFactor, guessFactor);
            }
        }
        return new double[]{minGuessFactor, maxGuessFactor};
    }

    private double[] calculateDefaultInterval(List<Point2D.Double> botIntersection, Wave wave) {
        double maxGuessFactor = Double.NEGATIVE_INFINITY;
        double minGuessFactor = Double.POSITIVE_INFINITY;
        for (Point2D.Double intersectingPoint : botIntersection) {
            double guessFactor = wave.getGuessFactor(intersectingPoint.x, intersectingPoint.y);
            maxGuessFactor = Math.max(maxGuessFactor, guessFactor);
            minGuessFactor = Math.min(minGuessFactor, guessFactor);
        }
        return new double[]{minGuessFactor, maxGuessFactor};
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

    private double[] getSecondWaveDangers(Wave wave, Point2D.Double botLocation, BattleInfo battleInfo) {
        minSecondWaveDanger = Double.POSITIVE_INFINITY;
        double botWidth = FastMath.atan(25 / wave.getSource().distance(botLocation)) / wave.getMEA();
        double[] dangers = new double[SECOND_WAVE_BINS];
        for (int i = 0; i < SECOND_WAVE_BINS; i++) {
            double currentGuessFactor = ((i * 2D) / (SECOND_WAVE_BINS - 1) - 1);
            double hitProbability = wave.getDanger(currentGuessFactor - botWidth, currentGuessFactor + botWidth, SMOOTH_FACTOR);
            double danger = hitProbability * (wave.getWaveDamage() + wave.getWavePower() * 3) * SECOND_WAVE_WEIGHT;
            dangers[i] = danger;
            minSecondWaveDanger = Math.min(danger, minSecondWaveDanger);
        }
        return dangers;
    }

    public void onPaint(Graphics2D g) {
        /*
        for (int c = 0; c < allPaths.size(); c++) {
            Path path = allPaths.get(c);
            Point2D.Double previousLocation = path.getState(0).getLocation();
            g.setStroke(new BasicStroke(1));
            if (path == currentPath) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.WHITE);
            }
            for (int i = 1; i < path.numberOfStates(); i++) {
                Point2D.Double location = path.getState(i).getLocation();
                g.setColor(Color.WHITE);
                g.drawLine((int) (previousLocation.x + 0.5), (int) (previousLocation.y + 0.5), (int) (location.x + 0.5), (int) (location.y + 0.5));
                previousLocation = location;
            }
            if (path == currentPath) {
                Point2D.Double targetLocation = path.getState(path.numberOfStates() - 1).getLocation();
                g.drawRect((int) (targetLocation.x - 17.5), (int) (targetLocation.y - 17.5), 36, 36);
            }
        }
         */

        /*
        for (Path path : allPaths) {
            State endState = path.getState(path.numberOfStates() - 1);
            Point2D.Double endLocation = endState.getLocation();
            double velocity = endState.getVelocity();
            double heading = endState.getHeading();
            Point2D.Double projection = MoveUtils.project(endLocation, heading, velocity * 2);
            double projectionAngle = MoveUtils.absoluteBearing(endLocation, projection);
            g.setColor(new Color(255, 0, 0));
            g.fillOval((int) (endLocation.x - 4), (int) (endLocation.y - 4), 8, 8);
            double arrowAngle0 = projectionAngle + -Math.PI * 0.2;
            double arrowAngle1 = projectionAngle + Math.PI * 0.2;
            Point2D.Double arrowPoint0 = MoveUtils.project(projection, arrowAngle0, -5);
            Point2D.Double arrowPoint1 = MoveUtils.project(projection, arrowAngle1, -5);
            g.drawLine((int) (endLocation.x + 0.5), (int) (endLocation.y + 0.5), (int) (projection.x + 0.5), (int) (projection.y + 0.5));
            g.drawLine((int) (arrowPoint0.x + 0.5), (int) (arrowPoint0.y + 0.5), (int) (projection.x + 0.5), (int) (projection.y + 0.5));
            g.drawLine((int) (arrowPoint1.x + 0.5), (int) (arrowPoint1.y + 0.5), (int) (projection.x + 0.5), (int) (projection.y + 0.5));
        }
        */

        if (currentPath != null) {
            List<State> intersectingStates = new ArrayList<>();
            double maxDistance = 0;
            for (int i = 0; i < currentPath.numberOfStates(); i++) {
                if (currentPath.getState(i).isIntersecting()) {
                    intersectingStates.add(currentPath.getState(i));
                    maxDistance = Math.max(maxDistance, currentPath.getState(i).getLocation().distance(currentSurfWave.getSource()));
                }
            }
            double[] guessFactors = calculateInterval(intersectingStates, currentSurfWave);
            g.setColor(Color.WHITE);
            for (double guessFactor : guessFactors) {
                double angle = currentSurfWave.getAbsoluteBearing() + currentSurfWave.getLateralDirection() * currentSurfWave.getMEA() * guessFactor;
                Point2D.Double estimatedIntersectionPoint = MoveUtils.project(currentSurfWave.getSource(), angle, maxDistance);
                g.drawLine((int) (currentSurfWave.getSource().x + 0.5),
                        (int) (currentSurfWave.getSource().y + 0.5),
                        (int) (estimatedIntersectionPoint.x + 0.5),
                        (int) (estimatedIntersectionPoint.y + 0.5));
            }
            g.setColor(Color.MAGENTA);
            for (int i = 1; i < currentPath.numberOfStates(); i++) {
                State currentState = currentPath.getState(i);
                State previousState = currentPath.getState(i - 1);
                Point2D.Double location = currentState.getLocation();
                Point2D.Double previousLocaton = previousState.getLocation();

                //double angle = currentState.getHeading() + (currentState.getVelocity() > 0 ? 0 : Math.PI);
                //double arrowAngle0 = angle - Math.PI * 0.4;
                //double arrowAngle1 = angle + Math.PI * 0.4;
                //Point2D.Double arrowPoint0 = MoveUtils.project(location, arrowAngle0, -5);
                //Point2D.Double arrowPoint1 = MoveUtils.project(location, arrowAngle1, -5);

                //g.drawLine((int) (arrowPoint0.x + 0.5), (int) (arrowPoint0.y + 0.5), (int) (location.x + 0.5), (int) (location.y + 0.5));
                //g.drawLine((int) (arrowPoint1.x + 0.5), (int) (arrowPoint1.y + 0.5), (int) (location.x + 0.5), (int) (location.y + 0.5));
                g.drawLine((int) (previousLocaton.x + 0.5), (int) (previousLocaton.y + 0.5), (int) (location.x + 0.5), (int) (location.y + 0.5));
                //g.drawOval((int) (location.x - 2), (int) (location.y - 2), 4, 4);
            }
            Point2D.Double targetLocation = currentPath.getState(currentPath.numberOfStates() - 1).getLocation();
            g.drawRect((int) (targetLocation.x - 17.5), (int) (targetLocation.y - 17.5), 36, 36);
        }
    }

    private static class PathIntersectionDanger {

        private final Path PATH;
        private final List<State> INTERSECTION;
        private final double DANGER;

        public PathIntersectionDanger(Path path, List<State> intersection, double danger) {
            PATH = path;
            INTERSECTION = intersection;
            DANGER = danger;
        }
    }
}
