package dsekercioglu.mega.rLock;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class RavenEyes {

    AdvancedRobot a;

    public RavenEyes(AdvancedRobot a) {
        this.a = a;
    }

    public void run() {
        for (;;) {
            a.turnRadarRightRadians(Double.POSITIVE_INFINITY);
            a.scan();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + a.getHeadingRadians();
        double turnAmount = Utils.normalRelativeAngle(absBearing - a.getRadarHeadingRadians());
        a.setTurnRadarRightRadians(turnAmount + Math.signum(turnAmount) * Math.PI / 16);
    }
}