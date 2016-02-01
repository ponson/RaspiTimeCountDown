/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timecountdown;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 *
 * @author user
 */
public class TimeCountDown {
    
    private static final int TC_STAT_START = 0;    
    private static final int TC_STAT_PAUSE = 1;
    private static final int TC_STAT_RESUME = 2;
    private static final int TC_STAT_STOP = 3;
    
    private static final GpioPinDigitalOutput[][] pins = new GpioPinDigitalOutput[2][7];
    
    
    private static int cur_status = TC_STAT_STOP;
    private static int new_status = TC_STAT_STOP;
    
    private static TimerContent cnt_time = new TimerContent();
    
    private static GpioController gpio = GpioFactory.getInstance();
 
    // Speaker 
    private static GpioPinDigitalOutput spk = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25);
        
    // dot indicator for indicating the count down is processing...
    private static GpioPinDigitalOutput pinDot = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23);
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        System.out.println("TimeCountDown start...");
       
        //Left Digit Pins, for number 10 digit.
        pins[0][0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16, "LA");
        pins[0][1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "LB");
        pins[0][2] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "LC");
        pins[0][3] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "LD");
        pins[0][4] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "LE");
        pins[0][5] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "LF");
        pins[0][6] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "LG");
        
        //Right Digit Pins, for number 1 digit.
        pins[1][0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, "RA");
        pins[1][1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10, "RB");
        pins[1][2] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08, "RC");
        pins[1][3] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "RD");
        pins[1][4] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "RE");
        pins[1][5] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "RF");
        pins[1][6] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "RG");
        
        System.out.println("LED GPIO init Empty");
        // switch 1: Start/Stop time 
        // Warning: using GPIO_20 will cause system crash. I haven't find what's wrong.
        final GpioPinDigitalInput pinSwitch0 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_13, PinPullResistance.PULL_DOWN);        
        System.out.println("Switches 1 done.");
        // switch 2: Pause/Resume time 
        final GpioPinDigitalInput pinSwitch1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_14, PinPullResistance.PULL_DOWN);
        System.out.println("Switches 2 done.");
        // switch 3: Exit Program
        final GpioPinDigitalInput pinSwitch2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
        System.out.println("Switches 3 done.");
        System.out.println("Switches init complete");
        
        
        //Pin Listener
        GpioPinListenerDigital pinListener = new GpioPinListenerDigital() {
            
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                System.out.println("Got switch event");
                GpioPin pin = event.getPin();
                PinState state = event.getState();
                
                if (state.isHigh()) {
                    // Every time the switch was touched, speaker play a sound.
                    spk.low();
                    spk.pulse(200, true);
                    if (pin == pinSwitch0) {
                        System.out.println("Switches 0 pressed.");
       
                        if (cur_status == TC_STAT_START
                                || cur_status == TC_STAT_PAUSE
                                || cur_status == TC_STAT_RESUME)
                            new_status = TC_STAT_STOP;
                        else
                            new_status = TC_STAT_START;                                               
                    }
                    else if (pin == pinSwitch1) {
                        System.out.println("Switches 1 pressed.");
                        switch (cur_status) {
                            case TC_STAT_START:
                                new_status = TC_STAT_PAUSE;
                                break;
                            case TC_STAT_PAUSE:
                                new_status = TC_STAT_RESUME;
                                break;
                            case TC_STAT_RESUME:
                                new_status = TC_STAT_PAUSE;
                                break;
                            default:
                                break;
                        }    
                    }
                    else if (pin == pinSwitch2) {
                        System.out.println("Switches 2 pressed.");
                        System.out.println("Bye...");
                        displayOff();
                        pinDot.high();//turn off
                        spk.low(); // turn off
                        gpio.shutdown();
                        System.exit(0);
                    }
                    System.out.println("new status = " + new_status);
                    executeAfterStatusChanged(new_status);
                }
            }
        };
        
        pinSwitch0.addListener(pinListener);
        pinSwitch1.addListener(pinListener);
        pinSwitch2.addListener(pinListener);
        
        displayTime(cnt_time.getMin());
        
        spk.setState(false);
        
        while (true) {
            //System.out.println("cur status = " + cur_status);
            switch(cur_status) {
                case TC_STAT_START:
                case TC_STAT_RESUME:
                    pinDot.blink(500, 1000);
                    delay(1000);
                    cnt_time.sub();
                    cnt_time.printTime();                    
                    displayTime(cnt_time.getMin());
                    if (cnt_time.checkTimeOut() == true) {
                        new_status = TC_STAT_STOP;
                        executeAfterStatusChanged(new_status);
                        for (int i=0; i< 5; i++) {
                            spk.pulse(500, true);
                            delay(500);
                        }
                        displayTimeFlash(cnt_time.getMin(), 5000);                        
                    }
                    break;
                case TC_STAT_STOP:
                    if (cnt_time.checkTimeInit() == false) {
                        cnt_time.reset();
                        displayTime(cnt_time.getMin());
                    }
                    delay(1000);
                    break;
                case TC_STAT_PAUSE:
                    displayTimeFlash(cnt_time.getMin(), 2000);
                    break;
                    
            } 
        }
        
    }
    
    private static void displayTimeFlash(int min, int duration) {
        int digit10 = min / 10;
        int digit1 = min % 10;
        
        System.out.println("Flash min = " + min);
        
        for (int i = 0; i < 7; i++) {
            if (DisplayMap.COMMON_ANODE[digit10][i] == 0)
                pins[0][i].blink(1000, duration);
            else
                pins[0][i].setState(true);
            
            if (DisplayMap.COMMON_ANODE[digit1][i] == 0)
                pins[1][i].blink(1000, duration);
            else
                pins[1][i].setState(true);
        }
        delay(duration);
        displayTime(min);
    }
    
    private static void displayTime(int min) {
        int digit10 = min / 10;
        int digit1 = min % 10;
        
        for (int i = 0; i < 7; i++) {
            pins[0][i].setState(DisplayMap.COMMON_ANODE[digit10][i] == 1);
            pins[1][i].setState(DisplayMap.COMMON_ANODE[digit1][i] == 1);
        }
    }
    
    private static void displayOff() {
        for (int i = 0; i < 7; i++) {
            pins[0][i].setState(true);
            pins[1][i].setState(true);
        }
    }
    
    private static void executeAfterStatusChanged(int newStatus) {
        
        cur_status = newStatus;
        
        switch (cur_status){
            case TC_STAT_START:
                break;
            case TC_STAT_STOP:
                delay(1000);
                pinDot.high();//turn off
                break;
            case TC_STAT_PAUSE:
                delay(1000);
                pinDot.high();//turn off
                break;
            case TC_STAT_RESUME:
                break;
            default:
                System.out.println("Inavlid Status Value!!!");
                break;
        }
    }
    
    private static void delay(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    };
    
}
