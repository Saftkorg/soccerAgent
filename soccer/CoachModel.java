/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package soccer;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Alexander
 */
public class CoachModel {
    String initMsg = "(init (version 13))";
    char teamSide;
    int time = -1;
    String teamName;
    int goalScore = 150;
    int oppGoalScore = -100;
    //int kickScore = 1;
    int timeInterval = 1000;
    
    int score;
    private final Random r = new Random();
    List<ParamElement> paramList = new LinkedList<>();
	public boolean halfTime = false;
	public boolean quit = false;
    int oppGoals = 0;
    int ourGoals = 0;
    
    
    CoachModel(){
        
        //paramList.add(new ParamElement(1.0,0.1)); //FAR_HOLDW 0
        //paramList.add(new ParamElement(1.0,0.1)); //FAR_COVERPW 1
        //paramList.add(new ParamElement(1.0,0.1)); //FAR_COVERGW 2
        //paramList.add(new ParamElement(1.0,0.1)); //FAR_GETFREEW 3 
        //paramList.add(new ParamElement(1.0,0.1)); //FAR_GOTOBALLW, 4
        
        for(int i = 0; i < 22;i++){
            paramList.add(new ParamElement(1.0,0.1));
        }
        
        //MED_HOLDW, 0
        //MED_COVERPW,1
        //MED_COVERGW, 2
        //MED_GETFREEW,3
        //MED_GOTOBALLW, 4
        
        //CLO_HOLDW, 0
        //CLO_COVERPW, 1
        //CLO_COVERGW, 2
        //CLO_GETFREEW, 3
        //CLO_GOTOBALLW, 4
        
        //HAS_DRIBBLEW,5
        //HAS_KICKW, 6
        //HAS_PASSW, 7
        
        //NOB_HOLDW, //noballinvision 0
        //NOB_COVERPW, 1
        //NOB_COVERGW, 2
        //NOB_GETFREEW, 3
        
        
        
        paramList.add(new ParamElement(35.0,1.0)); //FAR_BALL_DIST 
        paramList.add(new ParamElement(10.0,1.0));//MED_BALL_DIST, 

        //GTB_DISTMULTI_EVAL, //GoToBall distance closer than other team player 0.0 - 5.0
        paramList.add(new ParamElement(5.0,0.5));//DRB_DIST_EVAL, //distance to opponent for dribble 0.7-100.0
        paramList.add(new ParamElement(30.0,1.0));//KCK_GOALDIST_EVAL, //distance to goal before shooting 5 - 50
        paramList.add(new ParamElement(35.0,1.0));//PAS_PLYRDIST_EVAL, // distance to team player within shoot 3-50
        paramList.add(new ParamElement(20.0,0.2));//GTB_TURNBALLDIR; // turn when ball is this much off center  3-44   
        
    }

    String getValues() {
        StringBuilder sb = new StringBuilder();
        for(ParamElement pe : paramList){
            sb.append((double)Math.round(pe.nextValue()*100)/100);
            sb.append("+");     
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    
    void endPeriod(){
        for(ParamElement pe : paramList){
            pe.endPeriod();
            System.err.println(pe.value);
        }
    }
    void endTurn(){
        for(ParamElement pe : paramList){
            pe.endTurn(score);
        }
        score = 0;
    }
    
    class ParamElement{
        double value;
        double[] steps = new double[3];
        int prevIndex = 1;
        int stepIndex = 1;
        int[] score = {0,0,0};
        
        ParamElement(double value,double step){
            this.value = value;
            steps[0] = -step;
            steps[1] = 0;
            steps[2] = step;
        }    
        /**
         * add score to prev step
         * @param score 
         */
        public void endTurn(int score){
            this.score[prevIndex] += score;
        }
        /**
         * randomize new one
         * @return 
         */
        public double nextValue(){
            prevIndex = stepIndex;
            stepIndex = r.nextInt(3);
            return value+steps[stepIndex];
        }
        /**
         * after 10 turns change value
         */
        public void endPeriod(){
            if(score[0] > score[1] && score[0] > score[2]){
                this.value += steps[0];
            }else if(score[1] < score[2]){
                this.value += steps[2];
            }
            System.err.println(score[0]+score[1]+score[2]);
            score[0] = score[1] = score[2] = 0;
        }
        
    }
    
    void time(int time) {
        this.time = time;
    }
    
}
