/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timecountdown;

/**
 *
 * @author user
 */
public interface DisplayMap {
    
    public static final int [][] COMMON_ANODE = {
      // a, b, c, d, e, f, g
        {0, 0, 0, 0, 0, 0, 1},  //0
        {1, 0, 0, 1, 1, 1, 1},  //1
        {0, 0, 1, 0, 0, 1, 0},  //2
        {0, 0, 0, 0, 1, 1, 0},  //3
        {1, 0, 0, 1, 1, 0, 0},  //4
        {0, 1, 0, 0, 1, 0, 0},  //5
        {1, 1, 0, 0, 0, 0, 0},  //6
        {0, 0, 0, 1, 1, 1, 1},  //7
        {0, 0, 0, 0, 0, 0, 0},  //8
        {0, 0, 0, 1, 1, 0, 0},  //9
    };
    
}
