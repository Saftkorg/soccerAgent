/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soccer;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author Alexander
 */
public class Coach extends Thread {

    CoachCom cc ;//= new CoachCom(coach[0], Integer.parseInt(coach[1]));
    CoachModel cm;

    //while(

    Coach(String[] teamAdrPrt) throws UnknownHostException, SocketException {
        cm = new CoachModel();
        cm.teamName = teamAdrPrt[0];
        cc = new CoachCom(teamAdrPrt[1], Integer.parseInt(teamAdrPrt[2]), cm);
    }
    @Override
    public void run(){
        String msg = cm.initMsg;
        try {
            
            Thread.sleep(1 * 2 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        cc.send(msg);
        msg = "(team_names)";
        cc.send(msg);
        
        msg = "(say "+cm.getValues()+")";
       
        cc.send(msg);
        msg = "(start)";
        cc.send(msg);
        msg = "(ear on)";
        cc.send(msg);
        msg = "(eye on)";
        int update = cm.timeInterval-1;
        int reward = cm.timeInterval;
        
        while(cc.send(msg)){
            msg=null;
            if(cm.time>=update){
                //TODO make scores, scores are made in cc
                msg = "(say "+cm.getValues()+")";
                
                update+=cm.timeInterval;
            }
            if(cm.time >= reward){
                
                cm.endTurn();
                
                reward+=cm.timeInterval;
            }
            if(cm.time>=6000){
                cm.endPeriod();
            }
        } 
    }
}
