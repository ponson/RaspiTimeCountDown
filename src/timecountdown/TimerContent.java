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
public class TimerContent {
    
    private final int COUNT_DOWN_MIN = 30;
    private int min = COUNT_DOWN_MIN, sec = 0;
    
    public int getMin() {
        
        //Warning: Now only has "minutes display", we should show the min + 1 to inform the remain minutes.
        //Afiter we got the "seconds disply", we should remove the "min + 1" code
        if (sec > 0)
            return min+1;
        else
           return min;
    };
    
    public int getSec() {
        return sec;
    };
    
    public int sub() {
/*        
        if (min > 0)
            min--;
        return 0;
*/        
        if (sec == 0 && min > 0) {
            min--;            
            sec = 59;
        }
        else if (sec > 0)
            sec--;
        else
            return -1;
        return 0;
    };
    
    public void reset() {
        sec = 0;
        min = COUNT_DOWN_MIN;
    };
    
    public boolean checkTimeOut() {
        if (min == 0 && sec == 0)
            return true;
        else
            return false;
    };
    
    public boolean checkTimeInit() {
        if (min == COUNT_DOWN_MIN && sec == 0)
            return true;
        else
            return false;
    };
    
    public void printTime() {
        System.out.println("remain time = " + min +":"+ sec);
    }
    
}
