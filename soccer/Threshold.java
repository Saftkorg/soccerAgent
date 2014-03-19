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
public class Threshold {
    double max;
    double min;
    String name;
    Threshold(String name, double min, double max){
        this.name = name;
        this.min = min;
        this.max = max;
    }
}
