//Author: Daniel Mor
//Date Started: 2/27/2013
//Program Details: Store information about a cont. servo


//Encapsulates the functions of a Continuous Servo
public class ContinuousServo
{
    //Holds an arduino object
    private Arduino arduino;
    
    //Holds the programattic bounds to the servos rotation and speed
    private int lowerBound;
    private int upperBound;
    
    //Holds the logic controll pin of the servo
    private int pinNumber;
    
    
    //Basic constructor for the servo
    public ContinuousServo(Arduino arduino, int pinNumber) {
        //Store pin number locally
        this.arduino = arduino;
        this.pinNumber = pinNumber;
        this.lowerBound = 0;
        this.upperBound = 180;
    }
    
    //Spin the servo in a specific direction with a specific speed [0, 180] - Doesn't stop servo
    public void spin(int speed) {
        //Physically move the servo
        arduino.moveServo(pinNumber, constrain(speed, lowerBound, upperBound));
    }   
    
    //Spin the servo in a direction with a speed for a specified amount of time
    public void spin(int speed, int time) {
        //Physically move the servo
        arduino.moveServo(pinNumber, constrain(speed, lowerBound, upperBound));
      
        //Stop the servo
        delay(time);
        
        //Stop the servo
        stopServo();
    }
    
    //Stop the servo's movement
    public void stopServo() {
      //Sends a control signal of 1.5ms to stop the servo
      arduino.moveServo(pinNumber, 90);
    }
    
    
}

