import processing.core.*; 
import processing.xml.*; 

import processing.serial.*; 
import javax.swing.JOptionPane; 
import java.awt.BorderLayout; 
import java.util.ArrayList; 
import javax.swing.ImageIcon; 
import procontroll.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class CCAPI_Host extends PApplet {


//Author: Daniel Mor
//Date Started: 11/15/2012
//Updated: 2/23/2013
//Program Details: A program to directly interface with a PS3 Controller and send commands to an Arduino microcontroller via Serial Connection



//////////////////////////////////////////
//IMPORT LIBRARIES
 //Enable serial communication
 //Enables Popup dialog for initialization errors


//////////////////////////////////////////
//GLOBAL VARIABLES

//General
String deviceName = "MotioninJoy Virtual Game Controller";
int bgColor = color(0, 0, 0);
char terminatingCharacter = '/';
int baudRate = 115200;


//Serial com-port object
Serial port;

//Arduino-Microcontroller object
Arduino arduino;

//Motor Objects
Motor leftMotor;
Motor rightMotor;

//Servo Object
StandardServo clawServo;

//Playstation controller object
PS3Controller controller;

//Other
private int speedControlIncrement;

//////////////////////////////////////////
//DEFAULT SETUP METHOD - EXECUTES ONCE
//////////////////////////////////////////
public void setup() {       
    //Initialize the window and general application settings
    initWindow();

    ////////////////////////////////////////
    //SERIAL CONNECTION

    //Check if Serial device is plugged in
    //Open a Serial connection and clear the serial buffer if it is
    if(Serial.list().length > 0) {
        //Create Serial Object with specified settings
        port = new Serial(this, Serial.list()[0], baudRate); 
        
        //Clear the serial port
        port.clear();
    }
    else {
        //Unable to connect to serial device. Display error message
        JOptionPane.showMessageDialog(null, "Please Connect The Wireless Transmitter/Receiver and Try Again.", "Transmitter Not Found", JOptionPane.ERROR_MESSAGE);
        
        //Exit Program
        System.exit(1);
    } 
    
    
    ////////////////////////////////////////
    //Register Microcontroller
    arduino = new Arduino(port, terminatingCharacter);
    
    
    ////////////////////////////////////////
    //PS3 CONTROLLER
    
    //Initialize PS3 Controller. If not connected, then Display warning and exit.
    try {
        //Create and init the ps3 controller
        controller = new PS3Controller(this, arduino, deviceName);
    }
    catch(Exception ex) {
        //Controller Couldn't be initialized - Display error dialog
        JOptionPane.showMessageDialog(null, "Please Connect the PS3 Controller and Try Again.", "PS3 Controller Not Found", JOptionPane.ERROR_MESSAGE);
        
        //Exit program if controller isn't connected
        System.exit(1);
    }
       
    
    ////////////////////////////////////////
    //INITIALIZE PROGRAM OBJECTS
    
    //Initialize Motors
    leftMotor = new Motor(arduino, 'B', 400, 400, true);
    rightMotor = new Motor(arduino, 'A', 400, 400, true);
    
    //Initialize Servos
    clawServo = new StandardServo(arduino, 5);
    
    speedControlIncrement = 5;
}



//////////////////////////////////////////
//UPDATE DATA METHOD - ENDLESS LOOP
//////////////////////////////////////////
public void update() { 
    try {       
        //Check if DPad is pressed to change max/low power caps
        if(controller.maxPower == false) {
            //In low Power mode
            if(controller.isDPadUpButtonHeld() == true)
                controller.lowMotorPower = constrain(controller.lowMotorPower + speedControlIncrement, 0, 400);
            else if(controller.isDPadDownButtonHeld() == true)
                controller.lowMotorPower = constrain(controller.lowMotorPower - speedControlIncrement, 0, 400);
        }
        else {
            //In high power mode
            if(controller.isDPadUpButtonHeld() == true)
                controller.maxMotorPower = constrain(controller.maxMotorPower + speedControlIncrement, 0, 400);
            else if(controller.isDPadDownButtonHeld() == true)
                controller.maxMotorPower = constrain(controller.maxMotorPower - speedControlIncrement, 0, 400);            
        }
      
        //Set left Motor Speed
        if((controller.maxPower == false && controller.isLeftStickButtonHeld() == false) || (controller.maxPower == true && controller.isLeftStickButtonHeld() == true)) 
            leftMotor.setMaxPower(controller.lowMotorPower); // low power
        else
            leftMotor.setMaxPower(controller.maxMotorPower); // max power
            
        //Set Right motor speed
        if((controller.maxPower == false && controller.isRightStickButtonHeld() == false) || (controller.maxPower == true & controller.isRightStickButtonHeld() == true)) 
            rightMotor.setMaxPower(controller.lowMotorPower); // Low Power
        else 
            rightMotor.setMaxPower(controller.maxMotorPower); // max power
      
      
        //Retrieve analog stick Y axis values for both sticks
        float leftValue = controller.getLeftStick().getY();        
        float leftMultiplier = 1;
        
        float rightValue = controller.getRightStick().getY();
        float rightMultiplier = 1;
        
                                      
        //Move motor1 according to the left stick's position
        //BREAK MOTORS
        if(leftValue == 0) 
            leftMotor.applyBreaks();
        //MOTORS FORWARD
        if(leftValue < 0) {
            //Calculate the real motor power value
            float power = map(abs(leftValue), 0, 1, 0, leftMotor.getMaxPower());
            int maxPower = leftMotor.getMaxPower();
            
            //Adjust multiplier based on current speed mode
            if(maxPower == controller.defaultLowMotorPower) 
                leftMultiplier = .96f; 
            else if(maxPower == controller.defaultMaxMotorPower) 
                leftMultiplier = .94f;
            else if(maxPower == controller.sensitiveMotorPowerX) 
                leftMultiplier = .975f;
            else if(maxPower == controller.sensitiveMotorPowerCircle)
                leftMultiplier = .98f;
            else if(maxPower == controller.sensitiveMotorPowerTriangle) 
                leftMultiplier = .95f;
            else if(maxPower == controller.sensitiveMotorPowerSquare)
                leftMultiplier = .925f;
                
            //Use multiplier to adjust power
            power = power * leftMultiplier;
           println(leftMultiplier);
            
            //Send the command to the motor object - which will send the command to the arduino
            leftMotor.moveForward(round(power));
        }
        //MOTORS BACKWARD
        else if(leftValue > 0) {
            //Calculate the real motor power value
            float power = map(abs(leftValue), 0, 1, 0, leftMotor.getMaxPower());
            int maxPower = leftMotor.getMaxPower();

            //Adjust multiplier based on current speed mode
            if(maxPower == controller.defaultLowMotorPower)
                leftMultiplier = 1;
            else if(maxPower == controller.defaultMaxMotorPower)
                leftMultiplier = 1;
            else if(maxPower == controller.sensitiveMotorPowerX) 
                leftMultiplier = 1;
            else if(maxPower == controller.sensitiveMotorPowerCircle)
                leftMultiplier = 1;
            else if(maxPower == controller.sensitiveMotorPowerTriangle)
                leftMultiplier = 1;
            else if(maxPower == controller.sensitiveMotorPowerSquare)
                leftMultiplier = 1;
                
            //Use multiplier to adjust power
            power = power * leftMultiplier;
            
            //Send the command to the motor object - which will send the command to the arduino            
            leftMotor.moveBackward(round(power));
        }
            
            
            
        //Move motor2 according to the right stick's position
        //BREAK MOTORS
        if(rightValue == 0) 
            rightMotor.applyBreaks();
        //MOTORS FORWARD
        else if(rightValue < 0) {
            //Calculate the real motor power value
            float power = map(abs(rightValue), 0, 1, 0, rightMotor.getMaxPower());
            int maxPower = rightMotor.getMaxPower();

            //Adjust multiplier based on current speed mode
            if(maxPower == controller.defaultLowMotorPower)
                rightMultiplier = 1;
            else if(maxPower == controller.defaultMaxMotorPower)
                rightMultiplier = 1;
            else if(maxPower == controller.sensitiveMotorPowerX) 
                rightMultiplier = 1;
            else if(power == controller.sensitiveMotorPowerCircle)
                rightMultiplier = 1;
            else if(maxPower == controller.sensitiveMotorPowerTriangle)
                rightMultiplier = 1;
            else if(maxPower == controller.sensitiveMotorPowerSquare)
                rightMultiplier = 1;
                
            //Use multiplier to adjust power
            power = power * rightMultiplier;
            
            //Send the command to the motor object - which will send the command to the arduino  
            rightMotor.moveForward(round(power));
        }
        //MOTORS BACKWARD
        else if(rightValue > 0) {
            //Calculate the real motor power value
            float power = map(abs(rightValue), 0, 1, 0, rightMotor.getMaxPower());
            int maxPower = rightMotor.getMaxPower();

            //Adjust multiplier based on current speed mode
            if(maxPower == controller.defaultLowMotorPower)
                rightMultiplier = .96f;
            else if(maxPower == controller.defaultMaxMotorPower)
                rightMultiplier = .94f;
            else if(maxPower == controller.sensitiveMotorPowerX) 
                rightMultiplier = .92f;
            else if(maxPower == controller.sensitiveMotorPowerCircle)
                rightMultiplier = .93f;
            else if(maxPower == controller.sensitiveMotorPowerTriangle)
                rightMultiplier = .95f;
            else if(maxPower == controller.sensitiveMotorPowerSquare)
                rightMultiplier = .92f;
                
            //Use multiplier to adjust power
            power = power * rightMultiplier;
            
            //Send the command to the motor object - which will send the command to the arduino
            rightMotor.moveBackward(round(power));
        }
            
            
        //Check Servo Input
        if(controller.isL1ButtonPressed() == true) 
            clawServo.openClaw();
        else if(controller.isR1ButtonPressed() == true) 
            clawServo.closeClaw();
        else if(controller.isL2ButtonHeld() == true || controller.isL2ButtonPressed() == true) 
            clawServo.incrementClawOpen(controller.clawServoIncrement);
        else if(controller.isR2ButtonHeld() == true || controller.isR2ButtonPressed() == true) 
            clawServo.incrementClawClosed(controller.clawServoIncrement);
    }
    catch(Exception ex) {
        leftMotor.applyBreaks();
        rightMotor.applyBreaks();
    }
}



//////////////////////////////////////////
//DRAW - ENDLESS LOOP
//////////////////////////////////////////
public void draw() { 
    //Update data before drawing
    update();   
    
    //Pause the program temporarily
    delay(50);
}



//////////////////////////////////////////
//ASSISTIVE FUNCTIONS
//////////////////////////////////////////


public void sonarPing(StandardServo sonarServo, UltraSonicSensor frontSensor, UltraSonicSensor rearSensor, int amount) { 
    //Pan the servo a number of increments
    sonarServo.pan(amount);
  
    //Clear the background if the servo reached its bounds
    if(sonarServo.atBound()) {
        background(bgColor);
    }
    
    //Create integer to relatively Distance the sonar display - used in position calculations - scale the display 
    float displayMultiplier = 25;
    
    //Ping the ultrasonic sensor and retrieve a distance in the specified unit
    float frontDistance = frontSensor.requestDistance(Unit.IN);
    float rearDistance = rearSensor.requestDistance(Unit.IN);    
        
    //If No object is in sight, then draw line and plot data really far out
    int extendedDistance = 100;
    frontDistance = (frontDistance == 0 ? extendedDistance : frontDistance);
    rearDistance = (rearDistance == 0 ? extendedDistance : rearDistance);
    
    //Pull Servo Position/Angle to calculate Sonar Graph    
    float angle = sonarServo.getPosition() * PI / 180; //Convert servo position from angles to radians
    float offSet = 90 * PI / 180; //Offset the graph by 90*. Causes front sensor to use quadrants I & II and rear sensor to use quadrants III * IV. Converts 90* to radians

    
    ////////////////////////////////////////
    //Calculate Sonar Sensor Data
    
    //Calculate front x & y positions
    float frontSensorX = width/2 + (frontDistance * sin(angle + offSet) * displayMultiplier); //Works by calculating screen's center X and adding the distance at proper angle and multiplier
    float frontSensorY = height/2 + (frontDistance * cos(angle + offSet) * displayMultiplier);
    
    //Calculate rear x & y positions
    float rearSensorX = width/2 + (rearDistance * sin(angle - offSet) * displayMultiplier);
    float rearSensorY = height/2 + (rearDistance * cos(angle - offSet) * displayMultiplier);
    
    
    
    ////////////////////////////////////////
    //Plot Sonar Data & Draw Corresponding Lines
    
    //Change Color for front plot points
    fill(245,185,0);
    stroke(0,0,0);
   
    //Draw front plot point
    ellipse(frontSensorX, frontSensorY, 8, 8); //small circles   
    
    //Change color for front line
    stroke(0, 255, 0);
    line(width/2, height/2, frontSensorX, frontSensorY); //Line
    


    //Change color for rear plot points
    fill(255,255,255);
    stroke(0,0,0);
    
    //Draw rear plot point
    ellipse(rearSensorX, rearSensorY, 8, 8); //small circles   
    
    //Change color for rear line
    stroke(0, 255, 0);
    line(width/2, height/2, rearSensorX, rearSensorY); //Line



    //////////////////////////////////
    //draws the Sonar's rings, crosshairs and the vehicle
    
    //Draw Rings
    
    //Create variable with the number of display rings to be drawn
    int numOfRings = 25;
  
    //Change Draw Settings - rings
    stroke(0, 50, 0); //Set color
    noFill(); //Set Shapes won't fill
    
    //Draw the specified number of display rings
    for(int i = 0; i < numOfRings; i++) 
        ellipse(width/2, height/2, i*displayMultiplier, i*displayMultiplier); //Each ring is multiplied to remain consistent with distance
    
  
  
    //////////////////////////////////
    //Draw Cross Hairs
    
    //Set Crosshair color
    stroke(0, 100,0);
    
    //Draw vertical and horizontal crosshairs
    line(0, height/2, width, height/2); //horizontal line
    line(width/2, 0, width/2, height); //vertical line



    //////////////////////////////////
    //Draw Vehicle
    
    //Set Vehicle Draw settings
    fill(200, 0, 0);
    stroke(0,0,0);
    
    //Draw vehicle Shape
    rect(width/2, height/2, displayMultiplier, displayMultiplier); //center 
}


//////////////////////////////////////////
//INITIALIZE WINDOW AND PROGRAM SETTINGS
//////////////////////////////////////////
public void initWindow() {
    //Set a default size
    size(600, 650);
  
    //Allow the window to be resized and/or maximized
    frame.setResizable(true);
    
    //set the background color of the screen
    background(bgColor);
    
    //Set that rectangles and ellipses are drawn from a centerpoint
    rectMode(CENTER); 
    ellipseMode(RADIUS);

    //Set the size of the text used in the program
    textSize(18);
}

//Author: Daniel Mor
//Date Started: 11/25/2012
//Date Completed: 
//Program Details: A library that uses the CCAPI Protocol to send commands over a serial connection to an arduino microcontroller

public class Arduino
{
    //General
    private Serial port;
    private char terminatingCharacter;
  
    //Commands
    private final static int PINMODE_COMMAND = 0;
    private final static int READ_COMMAND = 1;
    private final static int WRITE_COMMAND = 2;
    private final static int SENSOR_COMMAND = 3;
    private final static int SERVO_COMMAND = 4;
    private final static int MOTOR_COMMAND = 5;
    
    //SENSOR SUBCOMMANDS
    private final static int PING_SUBCOMMAND = 0;
    
    //SERVO SUBCOMMANDS
    private final static int ATTACH_SERVO_SUBCOMMAND = 0;
    private final static int MOVE_SERVO_SUBCOMMAND = 1;
    private final static int DETACH_SERVO_SUBCOMMAND = 2;
    
    //MOTOR SUBCOMMANDS
    private final static int INIT_MOTOR_SUBCOMMAND = 0;
    private final static int MOVE_MOTOR_DIRECTIONA_SUBCOMMAND = 1;
    private final static int MOVE_MOTOR_DIRECTIONB_SUBCOMMAND = 2;
    private final static int BREAK_MOTOR_SUBCOMMAND = 3;
    
    //Motor Channels
    private final static int MOTOR_CHANNEL_A = 1;
    private final static int MOTOR_CHANNEL_B = 2;
    private final static int MOTOR_CHANNEL_NONE = 3;
  
    //Type
    private final static int ANALOG_TYPE = 0;
    private final static int DIGITAL_TYPE = 1;
    
    //Mode
    private final static int INPUT = 0;
    private final static int OUTPUT = 1;
    
    //Power Level
    private final static int LOW = 0;
    private final static int HIGH = 1;
  
    //Sensor Type
    private final static int ULTRASONIC_SENSOR_TYPE = 0;
  
  
    //Class constructor
    public Arduino(Serial port, char terminatingCharacter) {
        this.port = port;
        this.terminatingCharacter = terminatingCharacter;
    }
    
    
    
    ////////////////////////////////////////////
    //MAIN ARDUINO INTERFACING FUNCTIONS
    ////////////////////////////////////////////
    
    
    public void pinMode(int pinNumber, int mode) {
        serialWrite(PINMODE_COMMAND); //command
        serialWrite(pinNumber); //pin #
        serialWrite(mode); //INPUT OR OUTPUT
        //println("PINMODE:\t" + PINMODE_COMMAND+"/"+pinNumber+"/"+mode+"/");
    }
    
    public int analogRead(int pinNumber) {
        serialWrite(READ_COMMAND);
        serialWrite(ANALOG_TYPE);
        serialWrite(pinNumber);
        //println("ANALOG READ:\t" + READ_COMMAND+"/"+ANALOG_TYPE+"/"+pinNumber+"/");
        return getNextSerialToken();
    }
    
    public int digitalRead(int pinNumber) {
        serialWrite(READ_COMMAND);
        serialWrite(DIGITAL_TYPE);
        serialWrite(pinNumber);
        //println("Digital Read:\t" + READ_COMMAND+"/"+DIGITAL_TYPE+"/"+pinNumber+"/");
        return getNextSerialToken();
    }
    
    public void analogWrite(int pinNumber, int value) {
        serialWrite(WRITE_COMMAND);
        serialWrite(ANALOG_TYPE);
        serialWrite(pinNumber);
        serialWrite(value);
        //println("Analog Write:\t" + WRITE_COMMAND+"/"+ANALOG_TYPE+"/"+pinNumber+"/"+value+"/");
    }
    
    public void digitalWrite(int pinNumber, int value) {
        serialWrite(WRITE_COMMAND);
        serialWrite(DIGITAL_TYPE);
        serialWrite(pinNumber);
        serialWrite(value);
        //println("Digital Write:\t" + WRITE_COMMAND + "/"+DIGITAL_TYPE+"/"+pinNumber+"/"+value+"/");
    }
    
    
    //Request a Sensor ping, return time delay in microseconds and convert to appropriate unit of distance
    public long requestUltraSonicSensorPing(int echoPinNumber, int triggerPinNumber) {
        //Request ping from arduino
        serialWrite(SENSOR_COMMAND);
        serialWrite(PING_SUBCOMMAND);
        serialWrite(ULTRASONIC_SENSOR_TYPE);
        serialWrite(echoPinNumber);
        serialWrite(triggerPinNumber);
        
        //Return microseconds to calling function
        return getNextSerialToken();
    }
    
    //Attach Servo
    public void attachServo(int pinNumber) {
        serialWrite(SERVO_COMMAND);
        serialWrite(ATTACH_SERVO_SUBCOMMAND);
        serialWrite(pinNumber);
    }
    
    //Detach Servo
    public void detachServo(int pinNumber) {
        serialWrite(SERVO_COMMAND);
        serialWrite(DETACH_SERVO_SUBCOMMAND);
        serialWrite(pinNumber);
    }
    
    //Move the servo
    public void moveServo(int pinNumber, int degree) {
        serialWrite(SERVO_COMMAND);
        serialWrite(MOVE_SERVO_SUBCOMMAND);
        serialWrite(pinNumber);
        serialWrite(degree);
    }
    
    //Initialize Motor controller
    public void initMotorController() {
        serialWrite(MOTOR_COMMAND);
        serialWrite(INIT_MOTOR_SUBCOMMAND);
    }
    
    //Move a motor
    public void actuateMotor(char channel, int amount) {
        serialWrite(MOTOR_COMMAND);
        
        //Decide direction of motor
        if(amount >= 0)
          serialWrite(MOVE_MOTOR_DIRECTIONA_SUBCOMMAND);
        else
          serialWrite(MOVE_MOTOR_DIRECTIONB_SUBCOMMAND);
        
        //Decide which motor channel
        if(channel == 'A')
          serialWrite(MOTOR_CHANNEL_A);
        else if(channel == 'B')
          serialWrite(MOTOR_CHANNEL_B);
        else 
          serialWrite(MOTOR_CHANNEL_NONE);
          
        //Send power of motor
        serialWrite(abs(amount));
    }
    
    //Break a motor
    public void breakMotor(char channel, int amount) {
        serialWrite(MOTOR_COMMAND);
        serialWrite(BREAK_MOTOR_SUBCOMMAND);
        
        if(channel == 'A')
          serialWrite(MOTOR_CHANNEL_A);
        else if(channel == 'B')
          serialWrite(MOTOR_CHANNEL_B);
        else 
          serialWrite(MOTOR_CHANNEL_NONE);
          
        serialWrite(amount);
    }
    
    
    
    ////////////////////////////////////////////
    //SERIAL FUNCTIONS
    ////////////////////////////////////////////
    
    //append the terminating character to the end of the value and then write it to the serial port
    private void serialWrite(int value) {
        String out = str(value) + terminatingCharacter;
        
        //If the port is open, Send Data
        if(port != null) {
            port.write(out);
        }
    }
    
    //Send a single terminating character to the serial port
    private void sendTerminator() {
        port.write(str(terminatingCharacter));
    }
    
    //Retrieve next token from serial port (command that ends with terminating character), return -1 if no serial data is available
    public int getNextSerialToken() {   
        //Initialize token collection variables to 0
        int serialToken = 0;
        int incomingByte = 0; 
      
        //Collect serial data for this token until terminating character is reached        
        do {
            //Read the next byte of incoming serial data
            incomingByte = port.read();
            
            //If the incoming byte isn't a terminating character or 0, concatenate it to a new number
            if(incomingByte > 0 && incomingByte != terminatingCharacter) 
                serialToken = serialToken * 10 + incomingByte - '0';
        }
        while(incomingByte != terminatingCharacter);
    
        //Return the serial token
        return serialToken;
    }
}


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

//Author: Daniel Mor
//Date Started: 2/24/2013
//Program Details: Create a Processing GUI Window


//////////////////////////////////////////
//IMPORT LIBRARIES





//////////////////////////////////////////
//A basic Frame to allow multiple windows
public class PS3ControllerFrame extends Frame
{
  //Store embedded applet
  EmbeddedApplet embedded;
  
  //Active PS3 Controller Interfacing with program
  private PS3Controller controller;
  
  
  //Constructor to allow size creation
  public PS3ControllerFrame(PS3Controller controller) {
      //Set Frame title
      super("PS3 Controller Graphical Display");
    
      //Store Current PS3 Controller Object in memory
      this.controller = controller;
      
      //Set frame properties
      setSize(600, 650);

      
      //////////////////////////
      //Create embedded Applet & configure it
      embedded = new EmbeddedApplet();
      
      //Add applet to frame
      setLayout(new BorderLayout());
      add(embedded, BorderLayout.CENTER);
      //////////////////////////
      
      
      //Display Frame
      show();     
     
      //Initialize embedded
      embedded.init(); 
  }
  
  //Update embedded applet to display text
  public void displayString(String input) {
      embedded.displayString(input);
  }
  
  //
  public void setClawPosition(int pos) {
      embedded.setClawPosition(pos);
  }
  
  public void incrementClawPosition(int inc) {
      embedded.incrementClawPosition(inc);
  }
}

//Applet that is embedded into the Frame
public class EmbeddedApplet extends PApplet
{
    //List that stores most recent buttons pressed
    private ArrayList<String> buttonList;
    
    private int clawPosition;
  
    //Initialize applet
    public void setup() {
        //Initialize class variables
        buttonList = new ArrayList<String>();
      
        //Set properties
        size(600, 650);
        frameRate(30);
        
        //Prevent thread being overloaded 
        //noLoop();
        
        clawPosition = -1;
    }
    
    //Add string to display on GUI
    public void displayString(String input) {
        buttonList.add(0, input); //Adds item to top of the list
        
        //Delete last thing on list
        if(buttonList.size() == 20)
          buttonList.remove(19); //Remove 
    }
    
    //
    public void setClawPosition(int pos) {
        clawPosition = constrain(pos, 0, 2000);
    }
    
    public void incrementClawPosition(int inc) {
        clawPosition = constrain(clawPosition+inc, 0, 2000);
    }
    
    //Draw Frame Data
    public void draw() {
        //Set text size
        textSize(16);
        fill(255, 255, 255);       
      
        //Clear Background
        if((controller.maxPower == false && controller.isLeftStickButtonHeld() == false && controller.isRightStickButtonHeld() == false) || 
          (controller.maxPower == true && (controller.isLeftStickButtonHeld() == true || controller.isRightStickButtonHeld() == true))) {
            background(125, 153, 255);
            text(controller.lowMotorPower, 10, 25);
          }
        else {
            background(255, 0, 0);
            text(controller.maxMotorPower, 10, 25);
        }
        
      
        //DRAW ANALOG STICKS
        /////////////////////////////////////////////////////////////////
        
        //The size of the circle representing each analog Stick   
        int circleSize = 30;
        
        //Draw Left Stick
        fill(255, 0, 0);
        rect(width/2 + controller.getLeftStick().getX()*width/2, height/2 + controller.getLeftStick().getY()*height/2, circleSize, circleSize, 12, 12);
        
        //Draw right stick
        fill(0, 255, 0);
        rect(width/2 + controller.getRightStick().getX()*width/2, height/2 + controller.getRightStick().getY()*height/2, circleSize, circleSize, 12, 12);     
        /////////////////////////////////////////////////////////////////
        

        
        //DRAW THROTTLE BAR
        /////////////////////////////////////////////////////////////////
        
        //Display variables
        int barWidth = 30;
        int barHeight = 300;
        
        int offSetX = 120;
        int offSetY = 55;
        
        int lineLength = 90;
        int lineThickness = 5;
        int lineLengthCenter = 60;
        
        //Set Bar color
        //stroke(255, 255, 255);
        fill(255, 255, 255);     
   
        //Draw Main Bars
        rect(0 + offSetX, 0 + offSetY, barWidth, barHeight); //Left
        rect(width - offSetX, 0 + offSetY, barWidth, barHeight); //Right
        
        //Draw Position Lines
        
        //TOP
        rect(0 + offSetX - (lineLength/2 - barWidth/2), 0 + offSetY, lineLength, lineThickness); //Left
        rect(width - offSetX - (lineLength/2 - barWidth/2), 0 + offSetY, lineLength, lineThickness); //Right
        
        //BOTTOM
        rect(0 + offSetX - (lineLength/2 - barWidth/2), 0 + offSetY + barHeight, lineLength, lineThickness); //Left
        rect(width - offSetX - (lineLength/2 - barWidth/2), 0 + offSetY + barHeight, lineLength, lineThickness); //Right
        
        //CENTER
        rect(0 + offSetX - (lineLengthCenter/2 - barWidth/2), 0 + offSetY + barHeight/2, lineLengthCenter, lineThickness); //Left
        rect(width - offSetX - (lineLengthCenter/2 - barWidth/2), 0 + offSetY + barHeight/2, lineLengthCenter, lineThickness); //Right        
        /////////////////////////////////////////////////////////////////
        
                
        //DRAW CLAW SLIDER
        /////////////////////////////////////////////////////////////////                
        rect(width/2-50, 0 + offSetY*2, 150, 15); //Main Bar
        rect(width/2-50, 0 + offSetY*2-20, 5, 55); // Left
        rect(width/2+100-5, 0 + offSetY*2-20, 5, 55); //right
        
        fill(255, 0, 0);
        if(clawPosition != -1)
            rect(width/2-50 + map(clawPosition, 0, 2000, 0, 150-10), 0 + offSetY*2, 10, 15);
            
        //Draw Claw text
        text("Claw Position", width/2-25, 0 + offSetY*2-15); 
        text("Open", width/2-95, 0 + offSetY*2+15); // left label
        text("Closed", width/2+105, 0 + offSetY*2+15); // right label
        /////////////////////////////////////////////////////////////////
        
        
        //Draw Throttle Text
        /////////////////////////////////////////////////////////////////
        text("Left Stick Throttle", 0 + offSetX - (lineLength/2 - barWidth/2) - 25, 0 + offSetY - 20); //Left
        text("Right Stick Throttle", width - offSetX - (lineLength/2 - barWidth/2) - 25, 0 + offSetY - 20); // Right
        /////////////////////////////////////////////////////////////////
        
        //Draw Throttle Text Percentage
        text(controller.getLeftStick().getY() * 100 + "%",  0 + offSetX - (lineLength/2 - barWidth/2) + 10, barHeight + 100); //Left
        text(controller.getRightStick().getY() * 100 + "%", width - offSetX - (lineLength/2 - barWidth/2) + 10, barHeight + 100); //right
        
        
        //Draw Animated Throttle Bar
        /////////////////////////////////////////////////////////////////
        fill(255, 0, 0);
        
        rect(0 + offSetX,  0 + offSetY + barHeight/2 + controller.getLeftStick().getY() * barHeight/2, barWidth, lineThickness); //Left
        rect(width - offSetX, 0 + offSetY + barHeight/2 + controller.getRightStick().getY() * barHeight/2, barWidth, lineThickness); //right
        /////////////////////////////////////////////////////////////////
        
        
        //Display Pressed Buttons
        /////////////////////////////////////////////////////////////////
        
        //Set color
        fill(255, 0, 0);
        textSize(25);
        
        //Draw Title
        text("Buttons Pressed", width/2-100, height/2 + 70);
        
        //Draw Most recently pressed buttons
        fill(255, 255, 255);
        textSize(20);
        
        for(int i = 0; i < buttonList.size(); i++) {
            //Initial Position
            int xPosition = width/2 - 80;
            int yPosition = 30 + height/2 + 70 + (i*20);
          
            //Draw Text
            text(buttonList.get(i), xPosition, yPosition); 
        }
    }
}

//Author: Daniel Mor
//Date Started: 11/20/2012
//Updated: 2/23/2013
//Program Details: A class that controls a virtual PS3 controller using Procontroll and MotionInJoy


//////////////////////////////////////////
//IMPORT LIBRARIES
 //Procontroll allows interfacing with game controllers


public class PS3Controller 
{   
    /////////////////////////////
    //PS3 CONTROLLER ID MAPPING
    
    //Button IDs
    final static private int triangleButtonID = 0;
    final static private int circleButtonID = 1;
    final static private int xButtonID = 2;
    final static private int squareButtonID = 3;
    
    final static private int L1ButtonID = 4;
    final static private int R1ButtonID = 5;
    final static private int L2ButtonID = 6;
    final static private int R2ButtonID = 7;
    
    final static private int selectButtonID = 8;
    final static private int startButtonID = 9;
    
    final static private int leftStickButtonID = 10;
    final static private int rightStickButtonID = 11;
    
    final static private int PS3ButtonID = 12;
    
    final static private int dPadUpID = 13;
    final static private int dPadRightID = 14;
    final static private int dPadDownID = 15;
    final static private int dPadLeftID = 16;
    
    //Slider IDs
    final static private int wAxisSliderID = 2; //Right Stick up/down
    final static private int yTiltSliderID = 3; //Tilt left/right
    final static private int xTiltSliderID = 4; //Tilt away/toward body
    final static private int zAxisSliderID = 5; //Right stick left/right
    final static private int yAxisSliderID = 6; //Left Stick up/down
    final static private int xAxisSliderID = 7; //Left Stick left/right
  
    /////////////////////////////
    //BOOLEANS - Used to store current state of 
    
    private boolean triangleButtonPressed = false;
    private boolean triangleButtonHeld = false;
    
    private boolean circleButtonPressed = false;
    private boolean circleButtonHeld = false;
    
    private boolean xButtonPressed = false;
    private boolean xButtonHeld = false;
    
    private boolean squareButtonPressed = false;
    private boolean squareButtonHeld = false;
    
    private boolean L1ButtonPressed = false;
    private boolean L1ButtonHeld = false;
    
    private boolean R1ButtonPressed = false;
    private boolean R1ButtonHeld = false;
    
    private boolean L2ButtonPressed = false;
    private boolean L2ButtonHeld = false;
    
    private boolean R2ButtonPressed = false;
    private boolean R2ButtonHeld = false;
    
    private boolean selectButtonPressed = false;
    private boolean selectButtonHeld = false;
    
    private boolean startButtonPressed = false;
    private boolean startButtonHeld = false;
    
    private boolean leftStickButtonPressed = false;
    private boolean leftStickButtonHeld = false;
    
    private boolean rightStickButtonPressed = false;
    private boolean rightStickButtonHeld = false;
    
    private boolean PS3ButtonPressed = false;
    private boolean PS3ButtonHeld = false;
    
    private boolean dPadUpButtonPressed = false;
    private boolean dPadUpButtonHeld = false;
    
    private boolean dPadRightButtonPressed = false;
    private boolean dPadRightButtonHeld = false;
    
    private boolean dPadDownButtonPressed = false;
    private boolean dPadDownButtonHeld = false;
    
    private boolean dPadLeftButtonPressed = false;
    private boolean dPadLeftButtonHeld = false;
    
    
    
    /////////////////////////////
    //General
    private Arduino arduino;
    private String deviceName; //The name of the device being interfaced    
    private ControllIO io;
    
    //GUI
    private PS3ControllerFrame controllerGUI;
    
    //Controller object
    private ControllDevice controller;
    
    //Stick Objects
    private ControllStick leftStick;
    private ControllStick rightStick;
    private ControllStick tiltStick;
    
    //other
    public boolean maxPower;
    public int defaultLowMotorPower;
    public int defaultMaxMotorPower;
    public int lowMotorPower;
    public int maxMotorPower;
    
    public int sensitiveMotorPowerX;
    public int sensitiveMotorPowerCircle;
    public int sensitiveMotorPowerTriangle;
    public int sensitiveMotorPowerSquare;
    
    public int clawServoIncrement;
    
    public int maxPowerSettings;
    
    
    /////////////////////////////////////////
    //Constructor
    public PS3Controller(PApplet instance, Arduino arduino, String deviceName) throws Exception {
        this.arduino = arduino;
        this.deviceName = deviceName;
        
        //Initialize all Controller objects
        initController(instance);
        
        //Create Window to Monitor PS3 Controller and display graphical data
        controllerGUI = new PS3ControllerFrame(this);      
      
        defaultLowMotorPower = 125;
        defaultMaxMotorPower = 300;
        lowMotorPower = defaultLowMotorPower;
        maxMotorPower = defaultMaxMotorPower;              
        maxPower = false;
        
        sensitiveMotorPowerX = 75;
        sensitiveMotorPowerCircle = 90;
        sensitiveMotorPowerTriangle = 200;
        sensitiveMotorPowerSquare = 400;
        
        clawServoIncrement = 50;
    }

    //////////////////////////////////////////
    //INITIALIZE CONTROLLER OBJECTS
    //////////////////////////////////////////
    private void initController(PApplet instance) throws Exception {
        //Retrieve the ControllIO instance
        io = ControllIO.getInstance(instance);
          
        //Store the controller's instance into a variable
        controller = io.getDevice(deviceName);
            
        //Create Plug Event Listeners for Controller Buttons
        //Each Button specifies to trigger: ON_PRESS, ON_RELEASE or WHILE_PRESS with specified functions
        controller.plug(this, "triangleButtonPressed", ControllIO.ON_PRESS, triangleButtonID); 
        controller.plug(this, "triangleButtonReleased", ControllIO.ON_RELEASE, triangleButtonID);
        controller.plug(this, "triangleButtonHeld", ControllIO.WHILE_PRESS, triangleButtonID);
        
        controller.plug(this, "circleButtonPressed", ControllIO.ON_PRESS, circleButtonID);
        controller.plug(this, "circleButtonReleased", ControllIO.ON_RELEASE, circleButtonID);
        controller.plug(this, "circleButtonHeld", ControllIO.WHILE_PRESS, circleButtonID);
        
        controller.plug(this, "xButtonPressed", ControllIO.ON_PRESS, xButtonID);
        controller.plug(this, "xButtonReleased", ControllIO.ON_RELEASE, xButtonID);
        controller.plug(this, "xButtonHeld", ControllIO.WHILE_PRESS, xButtonID);
        
        controller.plug(this, "squareButtonPressed", ControllIO.ON_PRESS, squareButtonID);
        controller.plug(this, "squareButtonReleased", ControllIO.ON_RELEASE, squareButtonID);
        controller.plug(this, "squareButtonHeld", ControllIO.WHILE_PRESS, squareButtonID);
        
        controller.plug(this, "L1ButtonPressed", ControllIO.ON_PRESS, L1ButtonID);
        controller.plug(this, "L1ButtonReleased", ControllIO.ON_RELEASE, L1ButtonID);
        controller.plug(this, "L1ButtonHeld", ControllIO.WHILE_PRESS, L1ButtonID);
        
        controller.plug(this, "R1ButtonPressed", ControllIO.ON_PRESS, R1ButtonID);
        controller.plug(this, "R1ButtonReleased", ControllIO.ON_RELEASE, R1ButtonID);
        controller.plug(this, "R1ButtonHeld", ControllIO.WHILE_PRESS, R1ButtonID);
        
        controller.plug(this, "L2ButtonPressed", ControllIO.ON_PRESS, L2ButtonID);
        controller.plug(this, "L2ButtonReleased", ControllIO.ON_RELEASE, L2ButtonID);
        controller.plug(this, "L2ButtonHeld", ControllIO.WHILE_PRESS, L2ButtonID);
        
        controller.plug(this, "R2ButtonPressed", ControllIO.ON_PRESS, R2ButtonID);
        controller.plug(this, "R2ButtonReleased", ControllIO.ON_RELEASE, R2ButtonID);
        controller.plug(this, "R2ButtonHeld", ControllIO.WHILE_PRESS, R2ButtonID);
        
        controller.plug(this, "selectButtonPressed", ControllIO.ON_PRESS, selectButtonID);
        controller.plug(this, "selectButtonReleased", ControllIO.ON_RELEASE, selectButtonID);
        controller.plug(this, "selectButtonHeld", ControllIO.WHILE_PRESS, selectButtonID);
        
        controller.plug(this, "startButtonPressed", ControllIO.ON_PRESS, startButtonID);
        controller.plug(this, "startButtonReleased", ControllIO.ON_RELEASE, startButtonID);
        controller.plug(this, "startButtonHeld", ControllIO.WHILE_PRESS, startButtonID);
        
        controller.plug(this, "leftStickButtonPressed", ControllIO.ON_PRESS, leftStickButtonID);
        controller.plug(this, "leftStickButtonReleased", ControllIO.ON_RELEASE, leftStickButtonID);
        controller.plug(this, "leftStickButtonHeld", ControllIO.WHILE_PRESS, leftStickButtonID);
        
        controller.plug(this, "rightStickButtonPressed", ControllIO.ON_PRESS, rightStickButtonID);
        controller.plug(this, "rightStickButtonReleased", ControllIO.ON_RELEASE, rightStickButtonID);
        controller.plug(this, "rightStickButtonHeld", ControllIO.WHILE_PRESS, rightStickButtonID);
        
        controller.plug(this, "PS3ButtonPressed", ControllIO.ON_PRESS, PS3ButtonID);
        controller.plug(this, "PS3ButtonReleased", ControllIO.ON_RELEASE, PS3ButtonID);
        controller.plug(this, "PS3ButtonHeld", ControllIO.WHILE_PRESS, PS3ButtonID);
        
        controller.plug(this, "dPadUpButtonPressed", ControllIO.ON_PRESS, dPadUpID);
        controller.plug(this, "dPadUpButtonReleased", ControllIO.ON_RELEASE, dPadUpID);
        controller.plug(this, "dPadUpButtonHeld", ControllIO.WHILE_PRESS, dPadUpID);
        
        controller.plug(this, "dPadRightButtonPressed", ControllIO.ON_PRESS, dPadRightID);
        controller.plug(this, "dPadRightButtonReleased", ControllIO.ON_RELEASE, dPadRightID);
        controller.plug(this, "dPadRightButtonHeld", ControllIO.WHILE_PRESS, dPadRightID);
        
        controller.plug(this, "dPadDownButtonPressed", ControllIO.ON_PRESS, dPadDownID);
        controller.plug(this, "dPadDownButtonReleased", ControllIO.ON_RELEASE, dPadDownID);
        controller.plug(this, "dPadDownButtonHeld", ControllIO.WHILE_PRESS, dPadDownID);
        
        controller.plug(this, "dPadLeftButtonPressed", ControllIO.ON_PRESS, dPadLeftID);
        controller.plug(this, "dPadLeftButtonReleased", ControllIO.ON_RELEASE, dPadLeftID);
        controller.plug(this, "dPadLeftButtonHeld", ControllIO.WHILE_PRESS, dPadLeftID);
            
        //Retrieve Controller's 6 Sliders
        ControllSlider wAxisSlider = controller.getSlider(wAxisSliderID);  //Right Stick up/down
        ControllSlider yTiltSlider = controller.getSlider(yTiltSliderID);  //Tilt left/right
        ControllSlider xTiltSlider = controller.getSlider(xTiltSliderID);  //Tilt away/toward body
        ControllSlider zAxisSlider = controller.getSlider(zAxisSliderID); //Right stick left/right
        ControllSlider yAxisSlider = controller.getSlider(yAxisSliderID);  //Left Stick up/down
        ControllSlider xAxisSlider = controller.getSlider(xAxisSliderID);  //Left Stick left/right
          
        //Create custom stick objects using individual sliders & set tolerances
        leftStick = new ControllStick(xAxisSlider, yAxisSlider);
        leftStick.setTolerance(.05f);
        
        rightStick = new ControllStick(zAxisSlider, wAxisSlider);
        rightStick.setTolerance(.05f);
        
        tiltStick = new ControllStick(xTiltSlider, yTiltSlider);
        tiltStick.setTolerance(.2f);
    }
    
    
    
    /////////////////////////////////////////////////////////////
    //BUTTON-PLUG METHODS
    /////////////////////////////////////////////////////////////
    
    //Group of methods called when the Triangle button is pressed, released or held
    public void triangleButtonPressed() {println2("Triangle Pressed"); triangleButtonPressed = true; lowMotorPower = sensitiveMotorPowerTriangle;}
    public void triangleButtonReleased() {println2("Triangle Released"); triangleButtonPressed = false; triangleButtonHeld = false;}
    public void triangleButtonHeld() {println2("Triangle Held"); triangleButtonHeld = true;}
    
    //Group of methods called when the circle button is pressed, released or held
    public void circleButtonPressed() {println2("Circle Pressed"); circleButtonPressed = true; lowMotorPower = sensitiveMotorPowerCircle;}
    public void circleButtonReleased() {println2("Circle Released"); circleButtonPressed = false; circleButtonHeld = false;}
    public void circleButtonHeld() {println2("Circle Held"); circleButtonHeld = true;}
    
    //Group of methods called when the x button is pressed, released or held
    public void xButtonPressed() {println2("X Pressed"); xButtonPressed = true; lowMotorPower = sensitiveMotorPowerX;}
    public void xButtonReleased() {println2("X Released"); xButtonPressed = false; xButtonHeld = false;}
    public void xButtonHeld() {println2("X Held"); xButtonHeld = false;}
    
    //Group of methods called when the square button is pressed, released or held
    public void squareButtonPressed() {println2("Square Pressed"); squareButtonPressed = true; lowMotorPower = sensitiveMotorPowerSquare;}
    public void squareButtonReleased() {println2("Square Released"); squareButtonPressed = false; squareButtonHeld = false;}
    public void squareButtonHeld() {println2("Square Held"); squareButtonHeld = true;}
    
    //Group of methods called when the L1 button is pressed, released or held
    public void L1ButtonPressed() {println2("L1 Pressed"); L1ButtonPressed = true; controllerGUI.setClawPosition(0);}
    public void L1ButtonReleased() {println2("L1 Released"); L1ButtonPressed = false; L1ButtonHeld = false;}
    public void L1ButtonHeld() {println2("L1 Held"); L1ButtonHeld = true;}
    
    //Group of methods called when the R1 button is pressed, released or held
    public void R1ButtonPressed() {println2("R1 Pressed"); R1ButtonPressed = true; controllerGUI.setClawPosition(2400);}
    public void R1ButtonReleased() {println2("R1 Released"); R1ButtonPressed = false; R1ButtonHeld = false;}
    public void R1ButtonHeld() {println2("R1 Held"); R1ButtonHeld = true;}
    
    //Group of methods called when the L2 button is pressed, released or held
    public void L2ButtonPressed() {println2("L2 Pressed"); L2ButtonPressed = true;}
    public void L2ButtonReleased() {println2("L2 Released"); L2ButtonPressed = false; L2ButtonHeld = false;}
    public void L2ButtonHeld() {println2("L2 Held"); L2ButtonHeld = true; controllerGUI.incrementClawPosition(-clawServoIncrement);}
    
    //Group of methods called when the R2 button is pressed, released or held
    public void R2ButtonPressed() {println2("R2 Pressed"); R2ButtonPressed = true;}
    public void R2ButtonReleased() {println2("R2 Released"); R2ButtonPressed = false; R2ButtonHeld = false;}
    public void R2ButtonHeld() {println2("R2 Held"); R2ButtonHeld = true; controllerGUI.incrementClawPosition(clawServoIncrement);}
    
    //Group of methods called when the select button is pressed, released or held
    public void selectButtonPressed() {println2("Select Pressed"); selectButtonPressed = true;}
    public void selectButtonReleased() {println2("Select Released"); selectButtonPressed = false; selectButtonHeld = false;}
    public void selectButtonHeld() {println2("Select Held"); selectButtonHeld = true;}
    
    //Group of methods called when the start button is pressed, released or held
    public void startButtonPressed() {println2("Start Pressed"); startButtonPressed = true; lowMotorPower = defaultLowMotorPower; maxMotorPower = defaultMaxMotorPower;}
    public void startButtonReleased() {println2("Start Released"); startButtonPressed = false; startButtonHeld = false;}
    public void startButtonHeld() {println2("Start Held"); startButtonHeld = true;}
    
    //Group of methods called when the left stick's button is pressed, released or held
    public void leftStickButtonPressed() {println2("Left Stick Pressed"); leftStickButtonPressed = true;}
    public void leftStickButtonReleased() {println2("Left Stick Released"); leftStickButtonPressed = false; leftStickButtonHeld = false;}
    public void leftStickButtonHeld() {println2("Left Stick Held"); leftStickButtonHeld = true;}
    
    //Group of methods called when the right stick's button is pressed, released or held
    public void rightStickButtonPressed() {println2("Right Stick Pressed"); rightStickButtonPressed = true;}
    public void rightStickButtonReleased() {println2("Right Stick Released"); rightStickButtonPressed = false; rightStickButtonHeld = false;}
    public void rightStickButtonHeld() {println2("Right Stick Held"); rightStickButtonHeld = true;}
    
    //Group of methods called when the PS3 button is pressed, released or held
    public void PS3ButtonPressed() {println2("PS3 Pressed"); PS3ButtonPressed = true; maxPower = !maxPower;}
    public void PS3ButtonReleased() {println2("PS3 Released"); PS3ButtonPressed = false; PS3ButtonHeld = false;}
    public void PS3ButtonHeld() {println2("PS3 Held"); PS3ButtonHeld = true;}
    
    //Group of methods called when the D-Pad's Up button is pressed, released or held
    public void dPadUpButtonPressed() {println2("D-pad Up Pressed"); dPadUpButtonPressed = true;}
    public void dPadUpButtonReleased() {println2("D-pad Up Released"); dPadUpButtonPressed = false; dPadUpButtonHeld = false;}
    public void dPadUpButtonHeld() {println2("D-pad Up Held"); dPadUpButtonHeld = true;}
    
    //Group of methods called when the D-Pad's Right button is pressed, released or held
    public void dPadRightButtonPressed() {println2("D-Pad Right Pressed"); dPadRightButtonPressed = true;}
    public void dPadRightButtonReleased() {println2("D-Pad Right Released"); dPadRightButtonPressed = false; dPadRightButtonHeld = false;}
    public void dPadRightButtonHeld() {println2("D-Pad Right Held"); dPadRightButtonHeld = true;}
    
    //Group of methods called when the D-Pad's down button is pressed, released or held
    public void dPadDownButtonPressed() {println2("D-Pad Down Pressed"); dPadDownButtonPressed = true;}
    public void dPadDownButtonReleased() {println2("D-Pad Down Released"); dPadDownButtonPressed = false; dPadDownButtonHeld = false;}
    public void dPadDownButtonHeld() {println2("D-Pad Down Held"); dPadDownButtonHeld = true;}
    
    //Group of methods called when the D-Pad's Left button is pressed, released or held
    public void dPadLeftButtonPressed() {println2("D-Pad Left Pressed"); dPadLeftButtonPressed = true;}
    public void dPadLeftButtonReleased() {println2("D-Pad Left Released"); dPadLeftButtonPressed = false; dPadLeftButtonHeld = false;}  
    public void dPadLeftButtonHeld() {println2("D-Pad Left Held"); dPadLeftButtonHeld = true;}  
    
    /////////////////////////////////////////////////////////////
    //GET METHODS
    /////////////////////////////////////////////////////////////
    
    //GET Methods for Controller Sticks
    public ControllStick getLeftStick() {return leftStick;}
    public ControllStick getRightStick() {return rightStick;}
    public ControllStick getTiltStick() {return tiltStick;}
    
    //GET Methods for Controller Buttons
    public boolean isTriangleButtonPressed() {return triangleButtonPressed;}
    public boolean isTriangleButtonHeld() {return triangleButtonHeld;}
    
    public boolean isCircleButtonPressed() {return circleButtonPressed;}
    public boolean isCircleButtonHeld() {return circleButtonHeld;}
    
    public boolean isXButtonPressed() {return xButtonPressed;}
    public boolean isXButtonHeld() {return xButtonHeld;} 
    
    public boolean isSquareButtonPressed() {return squareButtonPressed;}
    public boolean isSquareButtonHeld() {return squareButtonHeld;}
    
    public boolean isL1ButtonPressed() {return L1ButtonPressed;}
    public boolean isL1ButtonHeld() {return L1ButtonHeld;}
    
    public boolean isR1ButtonPressed() {return R1ButtonPressed;}
    public boolean isR1ButtonHeld() {return R1ButtonHeld;}
    
    public boolean isL2ButtonPressed() {return L2ButtonPressed;}
    public boolean isL2ButtonHeld() {return L2ButtonHeld;}
    
    public boolean isR2ButtonPressed() {return R2ButtonPressed;}
    public boolean isR2ButtonHeld() {return R2ButtonHeld;}
    
    public boolean isSelectButtonPressed() {return selectButtonPressed;}
    public boolean isSelectButtonHeld() {return selectButtonHeld;}
    
    public boolean isStartButtonPressed() {return startButtonPressed;}
    public boolean isStartButtonHeld() {return startButtonHeld;}
    
    public boolean isLeftStickButtonPressed() {return leftStickButtonPressed;}
    public boolean isLeftStickButtonHeld() {return leftStickButtonHeld;}
    
    public boolean isRightStickButtonPressed() {return rightStickButtonPressed;}
    public boolean isRightStickButtonHeld() {return rightStickButtonHeld;}
    
    public boolean isPS3ButtonPressed() {return PS3ButtonPressed;}
    public boolean isPS3ButtonHeld() {return PS3ButtonHeld;}
    
    public boolean isDPadUpButtonPressed() {return dPadUpButtonPressed;}
    public boolean isDPadUpButtonHeld() {return dPadUpButtonHeld;}
    
    public boolean isDPadRightButtonPressed() {return dPadRightButtonPressed;}
    public boolean isDPadRightButtonHeld() {return dPadRightButtonHeld;}
    
    public boolean isDPadDownButtonPressed() {return dPadDownButtonPressed;}
    public boolean isDPadDownButtonHeld() {return dPadDownButtonHeld;}
    
    public boolean isDPadLeftButtonPressed() {return dPadLeftButtonPressed;}
    public boolean isDPadLeftButtonHeld() {return dPadLeftButtonHeld;}
    
    //Wrapper Function to prevent rewriting all functions
    private void println2(String text) {
        controllerGUI.displayString(text);
    }
}

//Author: Daniel Mor
//Date Started: 11/15/2012
//Program Details: Store information about a servo

public class StandardServo
{
    //Holds an arduino object
    private Arduino arduino;
    
    //Holds the bounds to the servo's rotation
    private int lowerBound;
    private int upperBound;
    
    //Holds the logic controll pin of the servo
    private int pinNumber;
    
    //Holds the current position of the servo
    private int position;
    
    //Holds a logic variable for the panning feature of the servo, remembering which direction to move
    //true means the servo is moving up (increasing position), false means servo is moving down (decreasing position);
    private boolean panDirection;
    
    
    //Basic constructor
    public StandardServo(Arduino arduino, int pinNumber) {
        //Store pin number locally
        this.arduino = arduino;
        this.pinNumber = pinNumber;
        this.lowerBound = 0;
        this.upperBound = 2400;
        
        //Set default servo logic
        panDirection = false;
        position = 0; //set a default position
        
        //move the servo to the default position       
        rotate(position); 
    }
    
    //Move the servo to a specific angle
    public void rotate(int degree) {
        //Store position
        position = degree;
        
        //Constrain position to upper and lower bounds
        position = constrain(position, lowerBound, upperBound);
        
        //Physically move the servo
        arduino.moveServo(pinNumber, position);
    }   
    
    public void attach() {
        arduino.attachServo(pinNumber);
    }
    
    public void detach() {
        arduino.detachServo(pinNumber);
    }
    
    //Pan the servo in specified increments
    public void pan(int increment) {
        //Check which direction to move the position and whether it hit its bounds already
        if(panDirection == true) {
            if(position >= upperBound)
                panDirection = false;
            else 
                position += increment;
        }
        else {
            if(position <= lowerBound)
                panDirection = true;
            else
                position -= increment;
        }
        
        //Force position to remain within the min and max bounds if it exceeded it
        constrain(position, lowerBound, upperBound);
        
        //Physically move the servo to the specified position
        this.rotate(position);
    }
    
    //Return the current position of the servo
    public int getPosition() {
        return position;
    }
    
    //Return the lower bound of the servo
    public int getLowerBound() {
        return lowerBound;
    }
    
    //Return the upper bond of the servo
    public int getUpperBound() {
        return upperBound;
    }
    
    //Return a boolean regarding whether the servo passed or is at its own bounds
    public boolean atBound() {
        if(position >= upperBound || position <= lowerBound)
            return true;
        else
            return false;    
    }
    
    //Close the claw fully
    public void closeClaw() {
        rotate(0);
    }
    
    //open claw fully
    public void openClaw() {
        rotate(2000);
    }
    
    public void incrementClawOpen(int amount) {
        rotate(position+amount);
    }
    
    public void incrementClawClosed(int amount) {
        rotate(position-amount);
    }
}

//Author: Daniel Mor
//Date Started: 11/28/2012
//Program Details: A class that is able to time events

public class Timer
{
    //Variables that track how much time passed
    private long startTime;
  
    //Basic constructor
    public Timer() {
        startTime = millis();
    }
    
    //Reset the timer to 0
    public void reset() {
        startTime = millis();
    }
    
    //Retrieve the time ellapsed in MS
    public long ellapsedMillis() {
        return (millis() - startTime);
    }
    
    //Retrieve the time ellapsed in Seconds
    public long ellapsedSeconds() {
        return (millis() - startTime) / 1000;
    }
}

//Author: Daniel Mor
//Date Started: 11/27/2012
//Program Details: Class that allows you to store ultrasonic sensor pin data and request sensor distance values from the arduino

public class UltraSonicSensor
{
    //Arduino object that holds the sensor
    private Arduino arduino;
    
    //The physical pin numbers that the sensor is connected to
    private int echoPinNumber;
    private int triggerPinNumber;
    
    
    //General constructor to initialize the sensor
    public UltraSonicSensor(Arduino arduino, int echoPinNumber, int triggerPinNumber) {
       //Copy pin data to local memory
        this.arduino = arduino;
        this.echoPinNumber = echoPinNumber;
        this.triggerPinNumber = triggerPinNumber;
        
        //Send commands to arduino to initialize pins
        arduino.pinMode(triggerPinNumber, Arduino.OUTPUT);
        arduino.pinMode(echoPinNumber, Arduino.INPUT);    
    }
    
    //request the arduino to ping the sensor to return a distance value in the specified unit of distance
    //Return -1 if distanceUnit parameter isn't foun
    public float requestDistance(Unit distanceUnit) {
        //Retrieve the ping delay
        long pingTime = arduino.requestUltraSonicSensorPing(echoPinNumber, triggerPinNumber);

        //Holds the distance
        float distance = 0;
        
        //convert the ping time into a distance in the appropriate distance unit and return distance as a float
        //Divide distance by 2 to account for travel distance to wall and back
        if(distanceUnit == Unit.METER)
            distance = ((pingTime * .00034f) / 2);
        else if(distanceUnit == Unit.CM)
            distance = ((pingTime * .034029f) / 2);
        else if(distanceUnit == Unit.FT)
            distance = ((pingTime * .001116f) / 2);
        else if(distanceUnit == Unit.IN)
            distance = ((pingTime * .013397f) / 2);   
           
        if(distance > 2) 
            return distance;
        else 
           return 0; 
    }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--stop-color=#cccccc", "CCAPI_Host" });
  }
}
