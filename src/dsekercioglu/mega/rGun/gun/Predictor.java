package dsekercioglu.mega.rGun.gun;

import dsekercioglu.mega.core.GuessFactor;
import dsekercioglu.mega.rGun.BattleInfo;
import java.util.ArrayList;

public abstract class Predictor {
    
    public abstract ArrayList<GuessFactor> getGuessFactors(BattleInfo battleInfo);
    
    public abstract void addData(BattleInfo battleInfo, GuessFactor guessFactor, boolean real);
}
