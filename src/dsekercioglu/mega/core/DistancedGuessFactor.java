/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dsekercioglu.mega.core;

/**
 *
 * @author doruksekercioglu
 */
public class DistancedGuessFactor {

    private final GuessFactor GUESS_FACTOR;
    private final double DISTANCE;

    public DistancedGuessFactor(GuessFactor guessFactor, double distance) {
        GUESS_FACTOR = guessFactor;
        DISTANCE = distance;
    }
    
    public GuessFactor getGuessFactor() {
        return GUESS_FACTOR;
    }
    
    public double getDistance() {
        return DISTANCE;
    }
}
