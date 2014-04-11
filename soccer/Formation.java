/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soccer;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Alexander
 */
public class Formation {

    void setSide(char field_side) {
        if (this.field_side != field_side) {
            y = -y;
            double tmpMin = thresholds.get(0).min;
            double tmpMax = thresholds.get(0).max;
            thresholds.get(0).min = thresholds.get(3).min;
            thresholds.get(0).max = thresholds.get(3).max;
            thresholds.get(3).min = tmpMin;
            thresholds.get(3).max = tmpMax;
            this.field_side = field_side;
        }
    }

    enum Pos {

        GOALIE,
        FBB,
        FBCB,
        FBCT,
        FBT,
        HBB,
        HBT,
        HBC,
        FT,
        FC,
        FB;
    }
    int x, y;
    char field_side;
    boolean goalie;
    int away_from_ball;
    List<Threshold> thresholds;

    Formation(Pos p) {
        goalie = false;
        field_side = 'l';
        thresholds = new LinkedList<>();
        String[] flags = {"l r", "l t", "l b", "l l"};//new String[4];
        double[] min = new double[4];
        double[] max = new double[4];
        switch (p) {
            case GOALIE:
                x = -50;
                y = 0;
                goalie = true;
                away_from_ball = 80;
                //flags[0] = "l r";
                min[0] = 90;
                max[0] = 105;
                //flags[1] = "l t";
                min[1] = 25;
                max[1] = 40;
                //flags[2] = "l b";
                min[2] = 25;
                max[2] = 40;
                //flags[3] = "l l";
                min[3] = 1;
                max[3] = 15;

                break;
            case FBB:
                x = -35;
                y = 20;
                away_from_ball = 30;
                //flags[0] = "l r";
                min[0] = 75;
                max[0] = 90;
                //flags[1] = "l t";
                min[1] = 50;
                max[1] = 60;
                //flags[2] = "l b";
                min[2] = 5;
                max[2] = 15;
                //flags[3] = "l l";
                min[3] = 15;
                max[3] = 30;
                break;
            case FBCB:
                //flags[0] = "l r";
                away_from_ball = 30;
                min[0] = 75;
                max[0] = 90;
                //flags[1] = "l t";
                min[1] = 35;
                max[1] = 55;
                //flags[2] = "l b";
                min[2] = 15;
                max[2] = 35;
                //flags[3] = "l l";
                min[3] = 15;
                max[3] = 30;
                x = -35;
                y = 7;
                break;
            case FBCT:
                //flags[0] = "l r";
                away_from_ball = 30;
                min[0] = 75;
                max[0] = 90;
                //flags[1] = "l t";
                min[1] = 15;
                max[1] = 35;
                //flags[2] = "l b";
                min[2] = 35;
                max[2] = 55;
                //flags[3] = "l l";
                min[3] = 15;
                max[3] = 30;
                x = -35;
                y = -7;
                break;
            case FBT:
                //flags[0] = "l r";
                away_from_ball = 30;
                min[0] = 75;
                max[0] = 90;
                //flags[1] = "l t";
                min[1] = 5;
                max[1] = 15;
                //flags[2] = "l b";
                min[2] = 50;
                max[2] = 60;
                //flags[3] = "l l";
                min[3] = 15;
                max[3] = 30;
                x = -35;
                y = -20;
                break;
            case HBB:
                //flags[0] = "l r";
                away_from_ball = 15;
                min[0] = 65;
                max[0] = 75;
                //flags[1] = "l t";
                min[1] = 50;
                max[1] = 60;
                //flags[2] = "l b";
                min[2] = 5;
                max[2] = 15;
                //flags[3] = "l l";
                min[3] = 35;
                max[3] = 45;
                x = -20;
                y = 14;
                break;
            case HBT:
                away_from_ball = 15;
                //flags[0] = "l r";
                min[0] = 65;
                max[0] = 75;
                //flags[1] = "l t";
                min[1] = 5;
                max[1] = 15;
                //flags[2] = "l b";
                min[2] = 50;
                max[2] = 60;
                //flags[3] = "l l";
                min[3] = 35;
                max[3] = 45;
                x = -20;
                y = -14;
                break;
            case HBC:
                away_from_ball = 15;
                //flags[0] = "l r";
                min[0] = 65;
                max[0] = 75;
                //flags[1] = "l t";
                min[1] = 30;
                max[1] = 40;
                //flags[2] = "l b";
                min[2] = 30;
                max[2] = 40;
                //flags[3] = "l l";
                min[3] = 35;
                max[3] = 45;
                x = -20;
                y = 0;
                break;
            case FT:
                away_from_ball = 5;
                //flags[0] = "l r";
                min[0] = 45;
                max[0] = 65;
                //flags[1] = "l t";
                min[1] = 5;
                max[1] = 15;
                //flags[2] = "l b";
                min[2] = 50;
                max[2] = 60;
                //flags[3] = "l l";
                min[3] = 45;
                max[3] = 65;
                x = -6;
                y = -17;
                break;
            case FC:
                away_from_ball = 5;
                //flags[0] = "l r";
                min[0] = 45;
                max[0] = 65;
                //flags[1] = "l t";
                min[1] = 30;
                max[1] = 40;
                //flags[2] = "l b";
                min[2] = 30;
                max[2] = 40;
                //flags[3] = "l l";
                min[3] = 45;
                max[3] = 65;
                x = -10;
                y = 0;
                break;
            case FB:
                away_from_ball = 5;
                //flags[0] = "l r";
                min[0] = 45;
                max[0] = 65;
                //flags[1] = "l t";
                min[1] = 50;
                max[1] = 60;
                //flags[2] = "l b";
                min[2] = 5;
                max[2] = 15;
                //flags[3] = "l l";
                min[3] = 45;
                max[3] = 65;
                x = -6;
                y = 17;
                break;
        }
        for (int i = 0; i < flags.length; i++) {
            thresholds.add(new Threshold(flags[i], min[i], max[i]));
        }
    }

}
