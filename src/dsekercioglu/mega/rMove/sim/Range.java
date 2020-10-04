package dsekercioglu.mega.rMove.sim;

import java.util.ArrayList;
import java.util.List;

public class Range {

    final double MIN;
    final double MAX;

    public Range(double num1, double num2) {
        MIN = Math.min(num1, num2);
        MAX = Math.max(num1, num2);
    }

    public double getMin() {
        return MIN;
    }

    public double getMax() {
        return MAX;
    }

    public static Range[] merge(Range r1, Range r2) {
        if (r2.MAX >= r1.MAX) {
            if (r2.MIN > r1.MAX) {
                return new Range[]{r1, r2};
            } else {
                return new Range[]{new Range(Math.min(r1.MIN, r2.MIN), r2.MAX)};
            }
        } else {
            return merge(r2, r1);
        }
    }

    public static List<Range> getIntersection(Range range, List<Range> ranges) {
        ArrayList<Range> intersectionRanges = new ArrayList<>();
        for (Range currentRange : ranges) {
            if (currentRange.MAX < range.MIN || range.MAX < currentRange.MIN) {
                continue;
            }
            Range intersection = new Range(Math.max(currentRange.MIN, range.MIN), Math.min(currentRange.MAX, range.MAX));
            intersectionRanges.add(intersection);
        }
        return intersectionRanges;
    }
}
