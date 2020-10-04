package dsekercioglu.mega.rGun;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.wiki.FastMath;
import dsekercioglu.mega.rGun.gun.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import dsekercioglu.mega.rMove.MoveUtils;
import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;

import static robocode.util.Utils.normalRelativeAngle;

public class Claws {

    private boolean TC;

    AdvancedRobot BOT;

    BattleInfo currentBattleInfo;

    ArrayList<Wave> waves;

    ArrayList<Predictor> predictors;
    ArrayList<Double> scores;
    ArrayList<Double> coefficients;

    FirePowerManager FIRE_POWER_MANAGER;

    int currentGun = 0;

    double bulletsShot = 0;
    double bulletsHit = 0;
    double bulletHitBullet = 0;

    double enemyBulletsShot = 0;
    double enemyBulletsHit = 0;

    double damageReceived = 0;
    double enemyDamageReceived = 0;

    double healthGained = 0;
    double enemyHealthGained = 0;

    double enemyHealthLost = 0;

    int scans = 0;


    public void init(AdvancedRobot advBot, boolean TC) {
        this.TC = TC;
        BOT = advBot;
        currentBattleInfo = new BattleInfo(advBot);
        predictors = new ArrayList<>();
        scores = new ArrayList<>();
        coefficients = new ArrayList<>();


        predictors.add(new MainGun());
        scores.add(16D);
        coefficients.add(1.05);

        predictors.add(new ASGun());
        scores.add(0D);
        coefficients.add(1D);

        //gun = new ScanWeightedPredictor(0.9, new MainGun());

        FIRE_POWER_MANAGER = new FirePowerManager(this);
    }

    public void run() {
        waves = new ArrayList<>();
        currentBattleInfo.run();
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        currentBattleInfo.onScannedRobot(e);
        if (currentBattleInfo.enemyFired()) {
            enemyHealthLost += currentBattleInfo.getLastFirePower();
            enemyBulletsShot++;
        }
        updateWaves();
        double firePower = TC ? Math.min(3, BOT.getEnergy()) : FIRE_POWER_MANAGER.firePower(currentBattleInfo);
        currentBattleInfo.setLastFirePower(firePower);
        Wave wave = new Wave(firePower, currentBattleInfo);
        waves.add(wave);
        if (BOT.getGunHeat() <= 0.3) {
            ArrayList<Double> guessFactors = getGuessFactors(firePower);
            int index = GunUtils.highestValue(scores);
            double guessFactor = guessFactors.get(index);
            wave.setGuessFactors(predictors.get(index).getGuessFactors(currentBattleInfo));
            double fireAngle = wave.getFireAngle(guessFactor);

            if (bulletHitBullet / bulletsShot > 0.3) {
                fireAngle += ((BOT.getTime() % 2) * 2 - 1) * 9 / currentBattleInfo.botDistance; // Be deterministic just to be so
            }

            double gunHeading = BOT.getGunHeadingRadians();
            BOT.setTurnGunRightRadians(normalRelativeAngle(fireAngle - gunHeading));
            if (BOT.getGunHeat() == 0 && BOT.getEnergy() > 0 && BOT.getGunTurnRemainingRadians() < FastMath.atan(18 / (currentBattleInfo.botDistance - 18))) {
                wave.setVirtualGunGuessFactors(guessFactors);
                currentBattleInfo.onFire();
                BOT.setFire(firePower);
                wave.setReal(true);
                FIRE_POWER_MANAGER.onFire(firePower, currentBattleInfo);
                bulletsShot++;
            }
        } else {
            BOT.setTurnGunRightRadians(normalRelativeAngle(currentBattleInfo.absoluteBearing - BOT.getGunHeadingRadians()));
        }


        if (scores.get(0) > scores.get(1)) {
            if (currentGun == 1) {
                System.out.println("Switching to Main Gun");
                currentGun = 0;
            }
        } else if (currentGun == 0) {
            System.out.println("Switching to AS Gun");
            currentGun = 1;
        }

        scans++;
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        bulletHitBullet++;
    }

    public void onBulletHit(BulletHitEvent e) {
        bulletsHit++;
        double bulletPower = e.getBullet().getPower();
        enemyDamageReceived += Rules.getBulletDamage(bulletPower);
        healthGained += bulletPower * 3;
    }

    public void onHitByBullet(HitByBulletEvent e) {
        double bulletPower = e.getBullet().getPower();
        damageReceived += Rules.getBulletDamage(bulletPower);
        enemyHealthGained += bulletPower * 3;
        enemyBulletsHit++;
    }

    public ArrayList<Double> getGuessFactors(double firePower) {
        ArrayList<Double> guessFactors = new ArrayList<>();
        for (Predictor predictor : predictors) {
            Wave wave = new Wave(firePower, currentBattleInfo);
            wave.setGuessFactors(predictor.getGuessFactors(currentBattleInfo));
            guessFactors.add(chooseBestGuessFactor(wave));
        }
        return guessFactors;
    }

    public double chooseBestGuessFactor(Wave wave) {
        //TODO: somehow make this more precise
        final int SENSITIVITY = 25;
        double highestScore = Double.NEGATIVE_INFINITY;
        double bestGuessFactor = 0;
        double botWidth = FastMath.atan(25 / (currentBattleInfo.botDistance)) / wave.MEA;
        int extraIndex = (int) Math.ceil(botWidth * SENSITIVITY);
        double[] BINS = new double[SENSITIVITY * 2 + 1];
        for (int i = -SENSITIVITY; i <= SENSITIVITY; i++) {
            double guessFactor = i * 1D / SENSITIVITY;
            double score = wave.getDanger(guessFactor - botWidth, guessFactor + botWidth, 1e-5);
            for (int j = Math.max(i - extraIndex, -SENSITIVITY); j <= Math.min(i + extraIndex, SENSITIVITY); j++) {
                BINS[j + SENSITIVITY] += score / (1 + MoveUtils.sq(i - j) * 10);
            }
        }
        for (int i = -SENSITIVITY; i <= SENSITIVITY; i++) {
            double guessFactor = 1D * i / SENSITIVITY;
            double score = BINS[i + SENSITIVITY];
            if (score > highestScore) {
                highestScore = score;
                bestGuessFactor = guessFactor;
            }
        }
        return bestGuessFactor;
    }

    public void updateWaves() {
        Point2D.Double botLocation = currentBattleInfo.enemyLocation;
        for (int i = 0; i < waves.size(); i++) {
            Wave wave = waves.get(i);
            double botWidth = FastMath.atan(18 / (currentBattleInfo.botDistance - 18)) / wave.MEA;
            if (wave.update(botLocation)) {
                GuessFactor preciseGuessFactor = new GuessFactor(wave.getPreciseGuessFactor(botLocation.x, botLocation.y), 1, (int) bulletsHit);
                for (Predictor predictor : predictors) {
                    predictor.addData(wave.getInfo(), preciseGuessFactor, wave.isReal());
                }

                double guessFactor = wave.getGuessFactor(botLocation.x, botLocation.y);
                ArrayList<Double> guessFactors = wave.getVirtualGunGuessFactors();
                for (int j = 0; j < guessFactors.size(); j++) {
                    double currentGuessFactor = guessFactors.get(j);
                    if (Math.abs(currentGuessFactor - guessFactor) < botWidth) {
                        scores.set(j, scores.get(j) + wave.WAVE_DAMAGE * coefficients.get(j));
                    }
                }
                waves.remove(i);
                i--;
            }
        }
    }

    public void onPaint(Graphics2D g) {

        if (!waves.isEmpty() && BOT.getGunHeat() < 0.4) {
            ArrayList<Double> waveScores = new ArrayList<>();
            Wave wave = waves.get(waves.size() - 1);
            final int SENSITIVITY = 20;
            final int INDEX_FACTOR = 10;
            double botWidth = Math.atan(18 / (currentBattleInfo.botDistance - 18)) / wave.MEA;
            for (int i = -SENSITIVITY; i <= SENSITIVITY; i++) {
                double guessFactor = i * 1D / SENSITIVITY;
                double score = wave.getDanger(guessFactor - botWidth, guessFactor + botWidth, 1e-5);
                waveScores.add(score);
            }
            ArrayList<Double> probabilities = GunUtils.normalize(waveScores);
            double avg = GunUtils.average(probabilities);
            double stdDev = GunUtils.stdDev(probabilities);
            Point2D.Double source = wave.SOURCE;

            final int SCORE_FACTOR = 100;
            for (int i = -SENSITIVITY * INDEX_FACTOR; i <= SENSITIVITY * INDEX_FACTOR; i++) {
                double guessFactor = i * 1D / (SENSITIVITY * INDEX_FACTOR);
                double score = (probabilities.get((i + SENSITIVITY * INDEX_FACTOR) / INDEX_FACTOR) * SCORE_FACTOR);

                double angle = wave.getFireAngle(guessFactor);
                final int COLOR_SENSITIVITY = 10;

                for (int j = COLOR_SENSITIVITY; j < score; j += COLOR_SENSITIVITY) {
                    Point2D.Double scorePosition0 = GunUtils.project(source, angle, j - COLOR_SENSITIVITY);
                    Point2D.Double scorePosition1 = GunUtils.project(source, angle, j);
                    g.setColor(heatMap(j * 1D / (SENSITIVITY * INDEX_FACTOR), avg, stdDev));
                    g.drawLine((int) scorePosition0.x, (int) scorePosition0.y, (int) scorePosition1.x, (int) scorePosition1.y);
                }
            }
        }
    }

    private static Color heatMap(double normalizedValue, double average, double stdDev) {
        if (normalizedValue == 0) {
            return Color.GREEN;
        }
        double min = average - stdDev;
        double max = average + stdDev;
        double range = max - min;
        double value = MoveUtils.limit(0, (normalizedValue - min) / range, 1);
        return new Color((int) Math.min((value * 510), 255), 0, (int) Math.min((510 - value * 510), 255));
    }
}
