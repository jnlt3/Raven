package dsekercioglu.mega.rMove.sim;



import dsekercioglu.mega.rMove.MoveUtils;

import java.awt.geom.Point2D;

public class ShadowBullet implements Cloneable {

    Point2D.Double fireLocation;
    Point2D.Double location;
    double absBearing;
    double speed;
    int time;

    public ShadowBullet(Point2D.Double fireLocation, double absBearing, double speed) {
        this.fireLocation = ((Point2D.Double) fireLocation.clone());
        this.location = ((Point2D.Double) fireLocation.clone());
        this.absBearing = absBearing;
        this.speed = speed;
        this.time = 3;
    }

    public boolean update() {
        this.time++;
        this.location = MoveUtils.project(this.fireLocation, this.absBearing, this.speed * this.time);
        return this.time > 91;
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Point2D.Double getFireLocation() {
        return fireLocation;
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public double getAbsBearing() {
        return absBearing;
    }

    public double getVelocity() {
        return speed;
    }

    public int getTime() {
        return time;
    }
}
