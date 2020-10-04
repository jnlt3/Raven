package dsekercioglu.mega.rMove;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.core.Pair;
import dsekercioglu.mega.rMove.info.BotInfo;
import dsekercioglu.mega.rMove.info.EnemyTargetingInfo;
import dsekercioglu.mega.rMove.info.WaveData;
import dsekercioglu.mega.rMove.info.battle.BattleInfo;
import dsekercioglu.mega.rMove.info.battle.BattleSummary;
import dsekercioglu.mega.rMove.movetree.formula.knnformula.*;
import dsekercioglu.mega.rMove.movetree.nodes.Node;
import dsekercioglu.mega.rMove.movetree.nodes.learn.ETP;
import dsekercioglu.mega.rMove.movetree.nodes.learn.MP;
import dsekercioglu.mega.rMove.movetree.nodes.modify.condition.ConditionalInNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.condition.ConditionalOutNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.condition.NoInNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.decay.KNNScanNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.optimize.CacheNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.optimize.CombineNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.weight.AreaNormNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.weight.SmartAreaNormNode;
import dsekercioglu.mega.rMove.movetree.nodes.modify.weight.WeightedNode;
import dsekercioglu.mega.rMove.sim.ShadowBullet;
import dsekercioglu.mega.rMove.sim.Wave;
import dsekercioglu.mega.rMove.ws.WaveSurfingGT;
import robocode.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static robocode.util.Utils.normalRelativeAngle;

public class RavenFlight {

	private final double SMOOTH_FACTOR = 0.16;

	private final BattleInfo BATTLE_INFO;
	private final BotInfo BOT_INFO;

	private final List<BattleSummary> PAST_SUMMARY = new ArrayList<>();
	private final List<EnemyTargetingInfo> PAST_ENEMY_TARGETING_INFO = new ArrayList<>();
	private final List<Wave> WAVES = new ArrayList<>();
	private final List<ShadowBullet> BULLETS = new ArrayList<>();

	private final Node PREDICTOR;

	private final double ROLL_FACTOR = 0.9;

	private final WaveSurfingGT WS_GT;

	public RavenFlight(AdvancedRobot robot) {
		BOT_INFO = new BotInfo(robot);
		BATTLE_INFO = new BattleInfo(robot);


		ContinuousFormula simpleFormula = new SimpleFormula();
		ContinuousFormula simpleFormula2 = new SimpleFormula2();
		ContinuousFormula normalFormula = new NormalFormula();
		ContinuousFormula flattenerFormula = new FlattenerFormula();

		Node simple = new ConditionalOutNode(new SmartAreaNormNode(new ETP(simpleFormula, 15, 1, 24), ROLL_FACTOR, SMOOTH_FACTOR), 0, 0.09);
		Node simple2 = new ConditionalOutNode(new SmartAreaNormNode(new ETP(simpleFormula2, 15, 1, 24), ROLL_FACTOR, SMOOTH_FACTOR), 0, 0.09);

		Node normal = new CacheNode(new SmartAreaNormNode(new ETP(normalFormula, 30, 2, 24), ROLL_FACTOR, SMOOTH_FACTOR));
		Node decay = new NoInNode(new ConditionalOutNode(new SmartAreaNormNode(new KNNScanNode(2.2, normal), ROLL_FACTOR, SMOOTH_FACTOR), 0.06, 1));

		Node flattener = new ConditionalInNode(new ConditionalOutNode(new AreaNormNode(new KNNScanNode(2.2, new MP(flattenerFormula, 30, 5, 24)), SMOOTH_FACTOR), 0.09, 1), 0.09, 1);

		Node learn = new AreaNormNode(
				new CombineNode(
						31,
						new WeightedNode(
								new double[]{0.5, 0.5, 1, 1},
								simple,
								simple2,
								normal,
								decay
						)
				),
				SMOOTH_FACTOR
		);

		PREDICTOR = new WeightedNode(
				new double[]{0.5, 0.5},
				learn,
				flattener
		);

		WS_GT = new WaveSurfingGT(robot, SMOOTH_FACTOR);
	}

	public void run() {
		PAST_SUMMARY.clear();
		PAST_ENEMY_TARGETING_INFO.clear();
		WAVES.clear();
		BULLETS.clear();
		BATTLE_INFO.run();
		BOT_INFO.run();
		WS_GT.run();
	}


	public void onScannedRobot(ScannedRobotEvent e) {
		BATTLE_INFO.onScannedRobot(e);
		BOT_INFO.onScannedRobot(e);
		updateWaves();
		if (BATTLE_INFO.enemyFired()) {
			double firePower = BATTLE_INFO.getEnemyFirePower();
			int size = PAST_SUMMARY.size();
			BattleSummary battleSummary = PAST_SUMMARY.get(size - 2);
			WaveData waveData = new WaveData(battleSummary, firePower);
			EnemyTargetingInfo enemyTargetingInfo = PAST_ENEMY_TARGETING_INFO.get(size - 1);

			Wave w = new Wave(enemyTargetingInfo, waveData, battleSummary, 2);
			List<GuessFactor> predictions = PREDICTOR.getGuessFactors(new WaveData(battleSummary, firePower));
			w.setGuessFactors(predictions);
			WAVES.add(w);
			WS_GT.onWaveUpdate();
		}
		if (this.BOT_INFO.botFired()) {
			this.BULLETS.add(new ShadowBullet(this.BOT_INFO.getFireLocation(), this.BOT_INFO.getGunHeading(), Rules.getBulletSpeed(this.BOT_INFO.getFirePower())));
		}
		BattleSummary summary = BATTLE_INFO.getSummary();
		PAST_SUMMARY.add(summary);
		PAST_ENEMY_TARGETING_INFO.add(BATTLE_INFO.getEnemyTargetingInfo());

		WS_GT.surf(WAVES, BATTLE_INFO, summary);
	}

	private void updateWaves() {
		for (int i = 0; i < this.BULLETS.size(); i++) {
			ShadowBullet b = this.BULLETS.get(i);
			boolean remove = b.update();
			if (remove) {
				this.BULLETS.remove(i);
				i--;
			}
		}
		Point2D.Double targetLocation = BATTLE_INFO.getBotLocation();
		for (int i = 0; i < WAVES.size(); i++) {
			Wave wave = WAVES.get(i);
			if (wave.update(targetLocation)) {
				WAVES.remove(i);
				i--;
			}
			if (wave.botVisited()) {
				GuessFactor guessFactor = new GuessFactor(wave.getGuessFactor(BATTLE_INFO.getBotLocation().getX(), BATTLE_INFO.getBotLocation().getY()), 1, BATTLE_INFO.getEnemyHitNum());
				PREDICTOR.addData(wave.getWaveData(), guessFactor, false);
			}

			for (ShadowBullet b : this.BULLETS) {
				wave.addShadow(b.getLocation(), b.getVelocity(), b.getAbsBearing());
			}
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		BATTLE_INFO.onHitByBullet(e);
		BOT_INFO.onHitByBullet(e);
		Bullet hitBullet = e.getBullet();
		Wave w = bulletDetectedRealWave(hitBullet.getX(), hitBullet.getY(), hitBullet.getVelocity());
		if (w != null) {
			logHit(w, w.getPreciseGuessFactor(hitBullet.getX(), hitBullet.getY()));
		} else {
			System.out.println("HitByBulletEvent: Wave couldn't be identified");
		}
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		Bullet hitBullet = e.getHitBullet();
		Wave w = bulletDetectedRealWave(hitBullet.getX(), hitBullet.getY(), hitBullet.getVelocity());
		if (w != null) {
			logHit(w, w.getPreciseGuessFactor(hitBullet.getX(), hitBullet.getY()));
		} else {
			System.out.println("BulletHitBulletEvent: Wave couldn't be identified");
		}
		removeShadows(e.getBullet());
	}

	public void onBulletHit(BulletHitEvent e) {
		BATTLE_INFO.onBulletHit(e);
		BOT_INFO.onBulletHit(e);
	}

	public void onHitRobot(HitRobotEvent e) {
		WS_GT.onWaveUpdate();
		BATTLE_INFO.onHitRobot(e);
		BOT_INFO.onHitRobot(e);
	}

	public void logHit(Wave w, double guessFactor) {
		GuessFactor hitGuessFactor = new GuessFactor(guessFactor, 1, BATTLE_INFO.getEnemyHitNum());
		WaveData data = w.getWaveData();
		PREDICTOR.addData(data, hitGuessFactor, true);
		updateWaveDanger(w);
	}

	public void updateWaveDanger(Wave w) {
		WS_GT.onWaveUpdate();
		for (Wave wave : WAVES) {
			wave.setGuessFactors(PREDICTOR.getGuessFactors(w.getWaveData()));
		}
	}

	public Wave bulletDetectedRealWave(double bulletX, double bulletY, double bulletVelocity) {
		for (int i = 0; i < WAVES.size(); i++) {
			Wave wave = WAVES.get(i);
			Point2D.Double source = wave.getSource();
			if (Math.abs(source.distance(bulletX, bulletY) - wave.getDistanceTraveled()) < wave.getWaveVelocity() * 3) {
				if (Math.abs(wave.getWaveVelocity() - bulletVelocity) < 0.2) {
					WAVES.remove(i);
					return wave;
				}
			}
		}
		return null;
	}

	public void removeShadows(Bullet bullet) {
		for (int i = 0; i < BULLETS.size(); i++) {
			ShadowBullet sb = BULLETS.get(i);
			if (Math.abs(normalRelativeAngle(sb.getAbsBearing() - bullet.getHeadingRadians())) < 0.003 && sb.getLocation().distance(new Point2D.Double(bullet.getX(), bullet.getY())) < sb.getVelocity() + 1) {
				BULLETS.remove(sb);
				break;
			}
		}
	}

	public void onRoundEnded(RoundEndedEvent e) {
		System.out.println("Enemy weighted hit rate: " + (int) (BATTLE_INFO.getEnemyWeightedHitRate() * 1000) / 10D + "%");
	}

	public void onPaint(Graphics2D g) {

        /*
        if (lastFirePower != -1) {
            AccelMEA ppmea = new AccelMEA(currentBattleInfo.getBattleFieldWidth(), currentBattleInfo.getBattleFieldHeight());
            ppmea.calculateEscapeAngle(currentBattleInfo.getBotLocation(), currentBattleInfo.getEnemyLocation(), (20 - 3 * lastFirePower), currentBattleInfo.getBotLateralVelocity());
            Point2D.Double clockwisePPMEA = ppmea.getEscapePosition(1);
            Point2D.Double counterClockwisePPMEA = ppmea.getEscapePosition(-1);
            g.setColor(Color.WHITE);
            g.drawRect((int) (clockwisePPMEA.x - 17.5), (int) (clockwisePPMEA.y - 17.5), 36, 36);
            g.drawRect((int) (counterClockwisePPMEA.x - 17.5), (int) (counterClockwisePPMEA.y - 17.5), 36, 36);
        }
        */

		g.setColor(Color.WHITE);
		g.drawRect((int) (BATTLE_INFO.getEnemyLocation().getX() - 18), (int) (BATTLE_INFO.getEnemyLocation().getY() - 18), 36, 36);

		final int ARTIFICIAL_BINS = 153;
		final int MID_BIN = ARTIFICIAL_BINS / 2;
		final double BIN_WIDTH = 2D / ARTIFICIAL_BINS;

		for (Wave wave : WAVES) {
			g.setStroke(new BasicStroke(2));
			ArrayList<Line2D.Double> segments = new ArrayList<>();
			ArrayList<Double> dangers = new ArrayList<>();

			for (int j = (int) (MID_BIN - MID_BIN * (wave.getMEACounterClowise() / wave.getMEA()) + 0.5); j < MID_BIN; j++) {
				double guessFactor = (j - MID_BIN) * 1D / MID_BIN;
				double angle = wave.getAbsoluteBearing() + wave.getMEA() * guessFactor;
				Point2D.Double p1 = MoveUtils.project(wave.getSource(), angle, wave.getDistanceTraveled());
				Point2D.Double p2 = MoveUtils.project(wave.getSource(), angle, wave.getDistanceTraveled() - wave.getWaveVelocity());
				segments.add(new Line2D.Double(p1, p2));
				guessFactor *= wave.getLateralDirection();
				dangers.add(wave.getDanger(guessFactor - BIN_WIDTH / 2, guessFactor + BIN_WIDTH / 2, SMOOTH_FACTOR));
			}
			for (int j = MID_BIN; j < (int) (MID_BIN + MID_BIN * (wave.getMEAClockwise() / wave.getMEA()) + 0.5); j++) {
				double guessFactor = (j - MID_BIN) * 1D / MID_BIN;
				double angle = wave.getAbsoluteBearing() + wave.getMEA() * guessFactor;
				Point2D.Double p1 = MoveUtils.project(wave.getSource(), angle, wave.getDistanceTraveled());
				Point2D.Double p2 = MoveUtils.project(wave.getSource(), angle, wave.getDistanceTraveled() - wave.getWaveVelocity());
				segments.add(new Line2D.Double(p1, p2));
				guessFactor *= wave.getLateralDirection();
				dangers.add(wave.getDanger(guessFactor - BIN_WIDTH / 2, guessFactor + BIN_WIDTH / 2, SMOOTH_FACTOR));
			}

			List<Pair<Integer, Double>> dangerIndices = new ArrayList<>();
			for (int i = 0; i < dangers.size(); i++) {
				dangerIndices.add(new Pair<>(i, dangers.get(i)));
			}
			dangerIndices.sort(Collections.reverseOrder());


			List<Double> updatedProbabilities = new ArrayList<>();
			List<Line2D.Double> updatedSegments = new ArrayList<>();
			double probability = 0;
			for (Pair<Integer, Double> currentDangerIndex : dangerIndices) {
				double currentProbability = currentDangerIndex.getComparable();
				if (probability + currentProbability <= 0.5) {
					probability += currentProbability;
					int index = currentDangerIndex.getObject();
					updatedProbabilities.add(dangers.get(index));
					updatedSegments.add(segments.get(index));
				}
			}


			double average = MoveUtils.mean(updatedProbabilities);
			double stdDev = MoveUtils.stdDev(updatedProbabilities);
			for (int j = 0; j < updatedSegments.size(); j++) {
				Line2D.Double line = updatedSegments.get(j);
				Point2D previous = line.getP1();
				Point2D current = line.getP2();
				g.setColor(heatMap(updatedProbabilities.get(j), average, stdDev));
				g.drawLine((int) previous.getX(), (int) previous.getY(), (int) current.getX(), (int) current.getY());
			}
            /*
            List<GuessFactor> guessFactors = wave.getGuessFactors();
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.WHITE);
            for (GuessFactor guessFactor : guessFactors) {
                double angle = wave.getAbsoluteBearing() + wave.getMEA() * wave.getLateralDirection() * guessFactor.GUESS_FACTOR;
                Point2D.Double position = MoveUtils.project(wave.getSource(), angle, wave.getDistanceTraveled());
                Point2D.Double previousPosition = MoveUtils.project(wave.getSource(), angle, wave.getDistanceTraveled() - wave.getWaveVelocity());
                g.drawLine((int) previousPosition.x, (int) previousPosition.y, (int) position.x, (int) position.y);
            }
            */
		}

		g.setStroke(new BasicStroke(1));
		g.setColor(Color.GREEN);
		for (ShadowBullet shadowBullet : BULLETS) {
			Point2D.Double currentPos = shadowBullet.getLocation();
			Point2D.Double oldPos = MoveUtils.project(currentPos, shadowBullet.getAbsBearing() + Math.PI, shadowBullet.getVelocity());
			g.drawLine((int) (currentPos.x + 0.5),
					(int) (currentPos.y + 0.5),
					(int) (oldPos.x + 0.5),
					(int) (oldPos.y + 0.5));
		}
		WS_GT.onPaint(g);
	}

	private static Color heatMap(double normalizedValue, double average, double stdDev) {
		if (normalizedValue == 0) {
			return Color.GREEN;
		}
		double min = average - 2 * stdDev;
		double max = average + 2 * stdDev;
		double range = max - min;
		double value = MoveUtils.limit(0, (normalizedValue - min) / range, 1);
		return new Color((int) Math.min((value * 510), 255), 0, (int) Math.min((510 - value * 510), 255));
	}
}
