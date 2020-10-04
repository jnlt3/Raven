package dsekercioglu.mega.rGun;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rMove.MoveUtils;
import robocode.Rules;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import static robocode.util.Utils.normalRelativeAngle;

public class Wave {

    final Point2D.Double SOURCE;
    final Point2D.Double TARGET;

    final double WAVE_VELOCITY;
    final double WAVE_DAMAGE;
    final double ABS_BEARING;

    final int LATERAL_DIRECTION;

    final double MEA;
    final double MEA_CLOCKWISE;
    final double MEA_COUNTER_CLOCKWISE;

    final double CLOCKWISE_MEA_RATIO;
    final double COUNTER_CLOCKWISE_MEA_RATIO;

    double distanceTraveled;

    ArrayList<GuessFactor> guessFactors = new ArrayList<>();

    ArrayList<Double> virtualGunGuessFactors = new ArrayList<>();

    final BattleInfo INFO;

    boolean real = false;

    public Wave(double power, BattleInfo battleInfo) {
        this.INFO = battleInfo.clone();
        distanceTraveled = this.WAVE_VELOCITY = 20 - 3 * power;
        WAVE_DAMAGE = Rules.getBulletDamage(power);
        this.ABS_BEARING = battleInfo.absoluteBearing;
        this.LATERAL_DIRECTION = battleInfo.getEnemyLateralDirection();

        SOURCE = (Point2D.Double) battleInfo.botLocation.clone();
        TARGET = (Point2D.Double) battleInfo.enemyLocation.clone();

        PPMEA ppmea = new PPMEA(battleInfo.BATTLE_FIELD_WIDTH, battleInfo.BATTLE_FIELD_HEIGHT, 0.01);
        ppmea.calculateEscapeAngle(TARGET, SOURCE, WAVE_VELOCITY);
        MEA_CLOCKWISE = Math.abs(normalRelativeAngle(ppmea.getEscapeAngle(1) - ABS_BEARING));
        MEA_COUNTER_CLOCKWISE = Math.abs(normalRelativeAngle(ppmea.getEscapeAngle(-1) - ABS_BEARING));
        MEA = FastMath.asin(8 / WAVE_VELOCITY);

        CLOCKWISE_MEA_RATIO = MEA_CLOCKWISE / MEA;
        COUNTER_CLOCKWISE_MEA_RATIO = MEA_COUNTER_CLOCKWISE / MEA;
    }

    public void setReal(boolean real) {
        this.real = real;
    }

    public boolean isReal() {
        return real;
    }

    public void setGuessFactors(ArrayList<GuessFactor> guessFactors) {
        this.guessFactors = new ArrayList<>();
        for (GuessFactor guessFactor : guessFactors) {
            GuessFactor scaledGuessFactor = new GuessFactor(scaleGuessFactor(guessFactor.GUESS_FACTOR * LATERAL_DIRECTION) * LATERAL_DIRECTION, guessFactor.getWeight(), guessFactor.SCAN);
            this.guessFactors.add(scaledGuessFactor);
        }
    }

    public void setVirtualGunGuessFactors(ArrayList<Double> guessFactors) {
        this.virtualGunGuessFactors = guessFactors;
    }

    public ArrayList<Double> getVirtualGunGuessFactors() {
        return virtualGunGuessFactors;
    }

    public boolean update(Point2D.Double targetLocation) {
        distanceTraveled += WAVE_VELOCITY;
        return distanceTraveled > SOURCE.distance(targetLocation);
    }

    public double getFireAngle(double guessFactor) {
        return ABS_BEARING + guessFactor * MEA * LATERAL_DIRECTION;
    }

    public double getGuessFactor(double x, double y) {
        double deltaAngle = normalRelativeAngle(MoveUtils.absoluteBearing(SOURCE, new Point2D.Double(x, y)) - ABS_BEARING) * LATERAL_DIRECTION;
        return deltaAngle / MEA;
    }

    public double getPreciseGuessFactor(double x, double y) {
        double deltaAngle = normalRelativeAngle(MoveUtils.absoluteBearing(SOURCE, new Point2D.Double(x, y)) - ABS_BEARING) * LATERAL_DIRECTION;
        return descaleGuessFactor(deltaAngle / MEA);
    }

    public double getDanger(double startGuessFactor, double endGuessFactor, double bandWidth) {
        double totalDanger = 0;
        for (GuessFactor guessFactor : guessFactors) {
            totalDanger += getDangerForOneGuessFactor(startGuessFactor, endGuessFactor, guessFactor, bandWidth);
        }
        return totalDanger;
    }

    private double getDangerForOneGuessFactor(double startGuessFactor, double endGuessFactor, GuessFactor guessFactor, double bandWidth) {
        double checkGuessFactor = guessFactor.GUESS_FACTOR;
        return integrateDanger(endGuessFactor - checkGuessFactor, startGuessFactor - checkGuessFactor, guessFactor.getWeight(), bandWidth);
    }

    private double integrateDanger(double value0, double value1, double weight, double bandWidth) {
        return value0 > 0 && value1 < 0 ? weight : 0;
    }

    private double scaleGuessFactor(double guessFactor) {
        return guessFactor * (guessFactor < 0 ? COUNTER_CLOCKWISE_MEA_RATIO : CLOCKWISE_MEA_RATIO);
    }

    private double descaleGuessFactor(double guessFactor) {
        return guessFactor * (guessFactor < 0 ? 1 / COUNTER_CLOCKWISE_MEA_RATIO : 1 / CLOCKWISE_MEA_RATIO);
    }


    public BattleInfo getInfo() {
        return INFO;
    }
}
