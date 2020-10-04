package dsekercioglu.mega.rMove.sim;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.*;
import dsekercioglu.mega.rMove.info.EnemyTargetingInfo;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.info.battle.BattleSummary;
import dsekercioglu.mega.rMove.mea.MEA;
import robocode.Rules;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static robocode.util.Utils.normalRelativeAngle;

public class Wave {

    final private double MAX_BOT_WIDTH = 18 * Math.sqrt(2);

    private final WaveData WAVE_DATA;

    final Point2D.Double SOURCE;

    final double WAVE_POWER;
    final double WAVE_DAMAGE;
    final double WAVE_VELOCITY;
    final double ABS_BEARING;

    final double LATERAL_DIRECTION;

    final double MEA;

    final double MEA_CLOCKWISE;
    final Point2D.Double POS_CLOCKWISE;
    final double MEA_COUNTER_CLOCKWISE;
    final Point2D.Double POS_COUNTER_CLOCKWISE;

    final double CLOCKWISE_MEA_RATIO;
    final double COUNTER_CLOCKWISE_MEA_RATIO;

    double distanceTraveled;

    List<GuessFactor> guessFactors = new ArrayList<>();

    List<Range> bulletShadows = new ArrayList<>();

    private boolean botVisited = false;


    public Wave(EnemyTargetingInfo enemyTargetingInfo, WaveData waveData, BattleSummary battleSummary, int deltaTime) {
        WAVE_DATA = waveData;
        WAVE_POWER = WAVE_DATA.getFirePower();
        WAVE_DAMAGE = Rules.getBulletDamage(WAVE_POWER);
        WAVE_VELOCITY = Rules.getBulletSpeed(WAVE_POWER);
        ABS_BEARING = enemyTargetingInfo.getAbsoluteBearing();
        LATERAL_DIRECTION = battleSummary.getBotLateralDirection();

        SOURCE = (Point2D.Double) enemyTargetingInfo.getSource().clone();

        distanceTraveled = WAVE_VELOCITY * deltaTime;
        MEA mea = new MEA(battleSummary.getBattleFieldWidth(), battleSummary.getBattleFieldHeight());
        mea.calculateEscapeAngle(battleSummary.getBotLocation(), enemyTargetingInfo.getSource(), WAVE_VELOCITY);
        MEA_CLOCKWISE = Math.abs(normalRelativeAngle(mea.getEscapeAngle(1) - ABS_BEARING));
        POS_CLOCKWISE = mea.getEscapePosition(1);
        MEA_COUNTER_CLOCKWISE = Math.abs(normalRelativeAngle(mea.getEscapeAngle(-1) - ABS_BEARING));
        POS_COUNTER_CLOCKWISE = mea.getEscapePosition(-1);
        MEA = FastMath.asin(8 / WAVE_VELOCITY);

        CLOCKWISE_MEA_RATIO = MEA_CLOCKWISE / MEA;
        COUNTER_CLOCKWISE_MEA_RATIO = MEA_COUNTER_CLOCKWISE / MEA;
    }

    public void setGuessFactors(List<GuessFactor> guessFactors) {
        this.guessFactors = new ArrayList<>();
        for (GuessFactor guessFactor : guessFactors) {
            GuessFactor scaledGuessFactor = new GuessFactor(scaleGuessFactor(guessFactor.GUESS_FACTOR * LATERAL_DIRECTION) * LATERAL_DIRECTION, guessFactor.getWeight(), guessFactor.SCAN);
            this.guessFactors.add(scaledGuessFactor);
        }
    }

    public List<GuessFactor> getGuessFactors() {
        return guessFactors;
    }

    public boolean update(Point2D.Double targetLocation) {
        distanceTraveled += WAVE_VELOCITY;
        botVisited = distanceTraveled > SOURCE.distance(targetLocation);
        return distanceTraveled > SOURCE.distance(targetLocation) + MAX_BOT_WIDTH * 3;
    }

    public boolean botVisited() {
        return botVisited;
    }

    public double getGuessFactor(double x, double y) {
        double deltaAngle = normalRelativeAngle(MoveUtils.absoluteBearing(SOURCE, new Point2D.Double(x, y)) - ABS_BEARING);
        return deltaAngle / MEA * LATERAL_DIRECTION;
    }

    public double getPreciseGuessFactor(double x, double y) {
        double deltaAngle = normalRelativeAngle(MoveUtils.absoluteBearing(SOURCE, new Point2D.Double(x, y)) - ABS_BEARING);
        return descaleGuessFactor(deltaAngle / MEA) * LATERAL_DIRECTION;
    }

    public double getDanger(double startGuessFactor, double endGuessFactor, double smoothFactor) {
        return (getGuessFactorDanger(startGuessFactor, endGuessFactor, smoothFactor) - getShadowedDanger(startGuessFactor, endGuessFactor, smoothFactor));
    }

    private double getGuessFactorDanger(double startGuessFactor, double endGuessFactor, double smoothFactor) {
        double totalDanger = 0;
        for (GuessFactor guessFactor : guessFactors) {
            totalDanger += getDangerForOneGuessFactor(startGuessFactor, endGuessFactor, guessFactor, smoothFactor);
        }
        return totalDanger;
    }

    private double getDangerForOneGuessFactor(double startGuessFactor, double endGuessFactor, GuessFactor guessFactor, double smoothFactor) {
        double checkGuessFactor = guessFactor.GUESS_FACTOR;
        return (integrateDanger(endGuessFactor - checkGuessFactor, guessFactor.getWeight(), smoothFactor)
                - integrateDanger(startGuessFactor - checkGuessFactor, guessFactor.getWeight(), smoothFactor));
    }

    private double scaleGuessFactor(double guessFactor) {
        return guessFactor * (guessFactor < 0 ? COUNTER_CLOCKWISE_MEA_RATIO : CLOCKWISE_MEA_RATIO);
    }

    private double descaleGuessFactor(double guessFactor) {
        return guessFactor * (guessFactor < 0 ? 1 / COUNTER_CLOCKWISE_MEA_RATIO : 1 / CLOCKWISE_MEA_RATIO);
    }


    private double getShadowedDanger(double startGuessFactor, double endGuessFactor, double smoothFactor) {
        Range range = new Range(startGuessFactor, endGuessFactor);
        List<Range> intersections = Range.getIntersection(range, bulletShadows);
        double shadowedDanger = 0;
        for (Range currentRange : intersections) {
            for (GuessFactor guessFactor : guessFactors) {
                shadowedDanger += getDangerForOneGuessFactor(currentRange.getMin(), currentRange.getMax(), guessFactor, smoothFactor);
            }
        }
        return shadowedDanger;
    }

    private double integrateDanger(double value, double weight, double smoothFactor) {
        return smoothFactor * weight * FastMath.atan(value / smoothFactor)/* + MoveUtils.limit(0, value + smoothFactor, smoothFactor * 2)*/;
    }

    public void addShadow(Point2D.Double bulletLocation, double bulletSpeed, double bulletAngle) {
        Point2D.Double previousBulletLocation = MoveUtils.project(bulletLocation, bulletAngle, -bulletSpeed);
        Line2D.Double bulletSegment = new Line2D.Double(previousBulletLocation, bulletLocation);

        double r0 = distanceTraveled;
        double r1 = distanceTraveled + WAVE_VELOCITY;

        double newR0 = Math.max(r0, SOURCE.distance(bulletSegment.x1, bulletSegment.y1));
        double newR1 = Math.min(r1, SOURCE.distance(bulletSegment.x2, bulletSegment.y2));

        Point2D.Double intersection0;
        Point2D.Double intersection1;
        if (r0 != newR0) {
            intersection0 = previousBulletLocation;
        } else {
            intersection0 = getIntersection(SOURCE, bulletSegment, r0);
        }
        if (r1 != newR1) {
            intersection1 = bulletLocation;
        } else {
            intersection1 = getIntersection(SOURCE, bulletSegment, r1);
        }
        if (intersection0 != null && intersection1 != null) {
            double gf0 = getGuessFactor(intersection0.x, intersection0.y);
            double gf1 = getGuessFactor(intersection1.x, intersection1.y);
            Range range = new Range(gf0, gf1);
            for (int i = 0; i < bulletShadows.size(); i++) {
                Range currentRange = bulletShadows.get(i);
                Range[] ranges = Range.merge(range, currentRange);
                if (ranges.length == 1) {
                    bulletShadows.remove(i);
                    i--;
                    range = ranges[0];
                }
            }
            bulletShadows.add(range);
        }
    }

    public Point2D.Double getIntersection(Point2D.Double center, Line2D.Double segment, double radius) {
        Line2D.Double tranlatedSegment = new Line2D.Double(
                segment.x1 - center.x,
                segment.y1 - center.y,
                segment.x2 - center.x,
                segment.y2 - center.y);
        double m = (tranlatedSegment.y1 - tranlatedSegment.y2) / (tranlatedSegment.x1 - tranlatedSegment.x2);
        double n = tranlatedSegment.y1 - (m * tranlatedSegment.x1);

        double mSqP1 = m * m + 1;
        double radSq = radius * radius;
        double nSq = n * n;
        double mn = m * n;
        double squareRoot = Math.sqrt(mSqP1 * radSq - nSq);

        double x1 = -(squareRoot + mn) / mSqP1;
        double y1 = (n - (m * squareRoot)) / mSqP1;
        if (MoveUtils.limitMinMax(tranlatedSegment.x1, x1, tranlatedSegment.x2) == x1 &&
                MoveUtils.limitMinMax(tranlatedSegment.y1, y1, tranlatedSegment.y2) == y1) {
            return new Point2D.Double(center.x + x1, center.y + y1);
        } else {
            double x2 = (squareRoot - mn) / mSqP1;
            double y2 = (m * squareRoot + n) / mSqP1;
            if (MoveUtils.limitMinMax(tranlatedSegment.x1, x2, tranlatedSegment.x2) == x2 &&
                    MoveUtils.limitMinMax(tranlatedSegment.y1, y2, tranlatedSegment.y2) == y2) {
                return new Point2D.Double(center.x + x2, center.y + y2);
            }
        }
        return null;
    }

    public WaveData getWaveData() {
        return WAVE_DATA;
    }

    public Point2D.Double getSource() {
        return SOURCE;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public double getWaveVelocity() {
        return WAVE_VELOCITY;
    }

    public double getAbsoluteBearing() {
        return ABS_BEARING;
    }

    public double getLateralDirection() {
        return LATERAL_DIRECTION;
    }

    public double getMEAClockwise() {
        return MEA_CLOCKWISE;
    }

    public double getMEACounterClowise() {
        return MEA_COUNTER_CLOCKWISE;
    }

    public double getMEA() {
        return MEA;
    }

    public double getWavePower() {
        return WAVE_POWER;
    }

    public double getWaveDamage() {
        return WAVE_DAMAGE;
    }

    public Point2D.Double getPositionClockwise() {
        return POS_CLOCKWISE;
    }

    public Point2D.Double getPositionCounterClockwise() {
        return POS_COUNTER_CLOCKWISE;
    }
}
