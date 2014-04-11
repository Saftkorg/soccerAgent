/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package soccer;

/**
 *
 * @author Alexander
 */
public class Body {
    double stamina, effort, staminaCapacity, amountOfSpeed;//(stamina 7850 1 129025) 
    int directionOfSpeed, headAngle;        //(speed 0.27 1) 
    //(head_angle 0) 

    void change(String s) {
        String msgA[] = s.split("\\s");
        switch(msgA[0]){
            case("stamina"):
                stamina = Double.parseDouble(msgA[1]);
                effort = Double.parseDouble(msgA[2]);
                staminaCapacity = Double.parseDouble(msgA[3]);
                break;
            case("speed"):
                amountOfSpeed = Double.parseDouble(msgA[1]);
                directionOfSpeed = Integer.parseInt(msgA[2]);
                break;
            case("head_angle"):
                headAngle = Integer.parseInt(msgA[1]);
                break;
        }
        
    }
    
}
