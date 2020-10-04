package dsekercioglu.mega.rMove;

import dsekercioglu.mega.core.wiki.FastMath;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MoveUtils {

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

    public static double limitMinMax(double minMax1, double value, double minMax2) {
        return Math.max(Math.min(minMax1, minMax2), Math.min(value, Math.max(minMax1, minMax2)));
    }

    public static double sq(double x) {
        return x * x;
    }

    public static double cb(double x) {
        return x * x * x;
    }

    public static double signedSq(double x) {
        return Math.abs(x) * x;
    }

    public static double calculateMEA(double bulletSpeed) {
        return Math.asin(8 / bulletSpeed);
    }

    public static boolean waveIntersectsRobot(Point2D.Double botLocation, Point2D.Double waveSource, double waveVelocity, double waveDistanceTraveled) {
        double previousDistance = waveDistanceTraveled - waveVelocity;
        double maxDistance = Double.NEGATIVE_INFINITY;
        double minDistance = Double.POSITIVE_INFINITY;
        for (int x = -18; x <= 18; x += 36) {
            for (int y = -18; y <= 18; y += 36) {
                double cornerX = botLocation.x + x;
                double cornerY = botLocation.y + y;
                double distance = waveSource.distance(cornerX, cornerY);
                maxDistance = Math.max(maxDistance, distance);
                minDistance = Math.min(minDistance, distance);
            }
        }
        return (minDistance < waveDistanceTraveled && maxDistance > waveDistanceTraveled)
                || (minDistance < previousDistance && maxDistance > previousDistance);

    }

    public static boolean waveIntersectsPoint(Point2D.Double point, Point2D.Double waveSource, double waveVelocity, double waveDistanceTraveled) {
        double previousDistance = waveDistanceTraveled - waveVelocity;
        double distance = waveSource.distance(point.x, point.y);
        return (distance <= waveDistanceTraveled && distance >= previousDistance);

    }

    public static List<Point2D.Double> rectangleCircleIntersection(Ellipse2D.Double circle, Rectangle2D.Double rectangle) {
        double x0 = rectangle.x;
        double y0 = rectangle.y;
        double x1 = x0 + rectangle.width;
        double y1 = y0 + rectangle.height;
        Line2D.Double verticalLine0 = new Line2D.Double(x0, y0, x0, y1);
        Line2D.Double verticalLine1 = new Line2D.Double(x1, y0, x1, y1);
        Line2D.Double horizontalLine0 = new Line2D.Double(x0, y0, x1, y0);
        Line2D.Double horizontalLine1 = new Line2D.Double(x0, y1, x1, y1);
        List<Point2D.Double> intersections = new ArrayList<>();
        intersections.addAll(circleVerticalLineSegmentIntersection(circle, verticalLine0));
        intersections.addAll(circleVerticalLineSegmentIntersection(circle, verticalLine1));
        intersections.addAll(circleHorizontalLineSegmentIntersection(circle, horizontalLine0));
        intersections.addAll(circleHorizontalLineSegmentIntersection(circle, horizontalLine1));
        return intersections;
    }

    public static List<Point2D.Double> circleHorizontalLineSegmentIntersection(Ellipse2D.Double circle, Line2D.Double line) {
        //x = a + sqrt(-b^2 + 2 b c - c^2 + r^2) and y = c
        assert line.x2 > line.x1;
        assert circle.width == circle.height;
        double radius = circle.width / 2;
        double centerX = circle.x + radius;
        double centerY = circle.y + radius;
        double addThis = Math.sqrt(-sq(centerY) + 2 * centerY * line.y1 - sq(line.y1) + sq(radius));
        double x0 = centerX + addThis;
        double x1 = centerX - addThis;
        boolean x0InBounds = x0 >= line.x1 && x0 <= line.x2;
        boolean x1InBounds = x1 >= line.x1 && x1 <= line.x2;
        List<Point2D.Double> intersectionPoints = new ArrayList<>();
        if (x0InBounds) {
            intersectionPoints.add(new Point2D.Double(x0, line.y1));
        }
        if (x1InBounds) {
            intersectionPoints.add(new Point2D.Double(x1, line.y1));
        }
        return intersectionPoints;
    }

    public static List<Point2D.Double> circleVerticalLineSegmentIntersection(Ellipse2D.Double circle, Line2D.Double line) {
        //x = c and y = b - sqrt(-a^2 + 2 a c - c^2 + r^2)
        assert line.y2 > line.y1;
        assert circle.width == circle.height;
        double radius = circle.width / 2;
        double centerX = circle.x + radius;
        double centerY = circle.y + radius;
        double addThis = Math.sqrt(-sq(centerX) + 2 * centerX * line.x1 - sq(line.x1) + sq(radius));
        double y0 = centerY + addThis;
        double y1 = centerY - addThis;
        boolean y0InBounds = y0 >= line.y1 && y0 <= line.y2;
        boolean y1InBounds = y1 >= line.y1 && y1 <= line.y2;
        List<Point2D.Double> intersectionPoints = new ArrayList<>();
        if (y0InBounds) {
            intersectionPoints.add(new Point2D.Double(line.x1, y0));
        }
        if (y1InBounds) {
            intersectionPoints.add(new Point2D.Double(line.x1, y1));
        }
        return intersectionPoints;
    }

    public static double mean(List<Double> array) {
        double a = 0;
        for (Double aDouble : array) {
            a += aDouble;
        }
        a /= array.size();
        return a;
    }

    public static double stdDev(List<Double> array) {
        double avg = mean(array);
        double v = 0;
        for (Double aDouble : array) {
            v += sq(aDouble - avg);
        }
        v /= array.size();
        return Math.sqrt(v);
    }

    public static ArrayList<Double> normalize(ArrayList<Double> array) {
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        for (Double aDouble : array) {
            max = Math.max(max, aDouble);
            min = Math.min(min, aDouble);
        }
        ArrayList<Double> normalizedArray = new ArrayList<>();
        max -= min;
        for (Double aDouble : array) {
            normalizedArray.add((aDouble - min) / max);
        }
        return normalizedArray;
    }

    public static ArrayList<Double> probability(ArrayList<Double> array) {
        double sum = 0;
        for (Double value : array) {
            sum += value;
        }
        ArrayList<Double> probabilityArray = new ArrayList<>();
        for (Double aDouble : array) {
            probabilityArray.add(aDouble / sum);
        }
        return probabilityArray;
    }
}
