package dsekercioglu.mega.rGun;

import dsekercioglu.mega.core.wiki.FastMath;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GunUtils {

    public static double absoluteBearing(Point2D.Double p1, Point2D.Double p2) {
        return FastMath.atan2(p2.x - p1.x, p2.y - p1.y);
    }

    public static Point2D.Double project(Point2D.Double source, double angle, double distance) {
        return new Point2D.Double(source.x + FastMath.sin(angle) * distance, source.y + FastMath.cos(angle) * distance);
    }

    public static double distanceToWall(double x, double y, double battleFieldWidth, double battleFieldHeight) {
        return Math.min(Math.min(x, battleFieldWidth - x), Math.min(y, battleFieldHeight - y));
    }

    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double sq(double x) {
        return x * x;
    }

    public static double cb(double x) {
        return x * x * x;
    }

    public static double calculateMEA(double bulletSpeed) {
        return FastMath.asin(8 / bulletSpeed);
    }

    public static double average(ArrayList<Double> array) {
        double a = 0;
        for (int i = 0; i < array.size(); i++) {
            a += array.get(i);
        }
        a /= array.size();
        return a;
    }

    public static double stdDev(ArrayList<Double> array) {
        double avg = average(array);
        double v = 0;
        for (int i = 0; i < array.size(); i++) {
            v += sq(array.get(i) - avg);
        }
        v /= array.size();
        return Math.sqrt(v);
    }

    public static ArrayList<Double> normalize(ArrayList<Double> array) {
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < array.size(); i++) {
            max = Math.max(max, array.get(i));
            min = Math.min(min, array.get(i));
        }
        ArrayList<Double> normalizedArray = new ArrayList<>();
        max -= min;
        for (int i = 0; i < array.size(); i++) {
            normalizedArray.add((array.get(i) - min) / max);
        }
        return normalizedArray;
    }

    public static ArrayList<Double> probability(ArrayList<Double> array) {
        double sum = 0;
        for (int i = 0; i < array.size(); i++) {
            sum += array.get(i);
        }
        ArrayList<Double> probabilityArray = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            probabilityArray.add(array.get(i) / sum);
        }
        return probabilityArray;
    }

    public static ArrayList<Double> probabilityDistribution(ArrayList<Double> array) {
        double sum = 0;
        for (int i = 0; i < array.size(); i++) {
            sum += array.get(i);
        }
        ArrayList<Double> probabilityArray = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            probabilityArray.add(array.get(i) / sum);
        }
        return probabilityArray;
    }

    public static int highestValue(ArrayList<Double> array) {
        double highestValue = Double.NEGATIVE_INFINITY;
        int highestValueIndex = -1;
        for (int i = 0; i < array.size(); i++) {
            double currentValue = array.get(i);
            if (currentValue > highestValue) {
                highestValue = currentValue;
                highestValueIndex = i;
            }
        }
        return highestValueIndex;
    }
}
