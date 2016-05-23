//Author: Daniel Mor
//Date Started: 11/10/2012
//Updated: 2/23/2013 
//Program Details: Class that stores a motors information as well as controlling and actuating it (forward, backward, break). Using Pololu Motor Controller

class Motor
{

    //The arduino object that holds the motor
    private Arduino arduino;
    private char motorControllerChannel; // Can be 'A' or 'B'
    private int maxMotorPower;
    private int maxBreakPower;
    private float lastMotorPower;
    
    //control variables for the motor
    private boolean analogControlled; //if analog controlled, convert [-1,1] range to [-400,400], if not, then expects [-400,400] power
    private boolean breaksEngaged;
  
  
    //Constructor to initialize values and register pins with arduino
    public Motor(Arduino arduino, char motorControllerChannel, int maxMotorPower, int maxBreakPower, boolean analogControlled) {
        //Locally store variables
        this.arduino = arduino;
        this.motorControllerChannel = motorControllerChannel;
        this.maxMotorPower = maxMotorPower;
        this.maxBreakPower = maxBreakPower;
        this.analogControlled = analogControlled;
        this.lastMotorPower = 0;
        breaksEngaged = false; //By default set the breaks to not be engaged
       
        //Apply the breaks to the motor
        applyBreaks();        
    }
    
    //Apply the breaks to a motor if they aren't already engaged - this stops the motor
    public void applyBreaks() {     
        if(breaksEngaged == false) {
            arduino.breakMotor(motorControllerChannel, maxBreakPower);
            breaksEngaged = true;
            lastMotorPower = 0;
        }
    }
    
    //Move the motor in the forwards direction the specific amount of power passed in
    //If the motor is controlled by analog, pass in a value [-1, 1], or else a value [0, 400]
    public void moveForward(float power, float multiplier) {     
        //Reset breaks boolean  
        breaksEngaged = false;
        
        //Declare Var to store Motor Power
        float motorPower = abs(power);
        
        //Set motor power Based on control method
        if(analogControlled == true) {
            motorPower = map(motorPower, 0, 1, 0, maxMotorPower);                  
            motorPower = constrain(motorPower*multiplier, 0, 400);
        }
        else 
            motorPower = constrain(motorPower, 0, maxMotorPower);
        
        //Send command to move the motor
        if(motorPower != lastMotorPower) {
            arduino.actuateMotor(motorControllerChannel, -round(motorPower));
            lastMotorPower = motorPower;
        }
    }
    
    //Move the motor in the backwards direction the specific amount of power passed in
    //If the motor is controlled by analog, pass in a value [-1, 1], or else a value [0, 400]
    public void moveBackward(float power, float multiplier) {                
        //Reset breaks boolean  
        breaksEngaged = false;
        
        //Declare Var to store Motor Power
        float motorPower = abs(power);
        
        //Set motor power Based on control method
        if(analogControlled == true) {
            motorPower = map(motorPower, 0, 1, 0, maxMotorPower);
            motorPower = constrain(motorPower * multiplier, 0, 400);
        }
        else 
            motorPower = constrain(motorPower, 0, maxMotorPower);
        
        //Send command to move the motor
        if(motorPower != lastMotorPower) {
            arduino.actuateMotor(motorControllerChannel, round(motorPower));
            lastMotorPower = motorPower;
        }
    }
    
    public void moveForward(int motorPower) {
        breaksEngaged = false;
        
        //Send command to move the motor
        if(motorPower != lastMotorPower) {
            arduino.actuateMotor(motorControllerChannel, -constrain(motorPower, 0, 400));
            lastMotorPower = motorPower;
        }
    }
    
    public void moveBackward(int motorPower) {
        breaksEngaged = false;
        
        //Send command to move the motor
        if(motorPower != lastMotorPower) {
            arduino.actuateMotor(motorControllerChannel, constrain(motorPower, 0, 400));
            lastMotorPower = motorPower;
        }
    }
    
    //Set max motor power
    public void setMaxPower(int amount) {
        maxMotorPower = amount;
    }
    
    public int getMaxPower() {
        return maxMotorPower;
    }
}

