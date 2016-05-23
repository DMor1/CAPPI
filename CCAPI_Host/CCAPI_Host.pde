   
//Author: Daniel Mor
//Date Started: 11/15/2012
//Updated: 2/23/2013
//Program Details: A program to directly interface with a PS3 Controller and send commands to an Arduino microcontroller via Serial Connection



//////////////////////////////////////////
//IMPORT LIBRARIES
import processing.serial.*; //Enable serial communication
import javax.swing.JOptionPane; //Enables Popup dialog for initialization errors


//////////////////////////////////////////
//GLOBAL VARIABLES

//General
String deviceName = "MotioninJoy Virtual Game Controller";
color bgColor = color(0, 0, 0);
char terminatingCharacter = '/';
int baudRate = 57600;


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
void setup() {       
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
void update() { 
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
                leftMultiplier = .96; 
            else if(maxPower == controller.defaultMaxMotorPower) 
                leftMultiplier = .94;
            else if(maxPower == controller.sensitiveMotorPowerX) 
                leftMultiplier = .975;
            else if(maxPower == controller.sensitiveMotorPowerCircle)
                leftMultiplier = .98;
            else if(maxPower == controller.sensitiveMotorPowerTriangle) 
                leftMultiplier = .95;
            else if(maxPower == controller.sensitiveMotorPowerSquare)
                leftMultiplier = .925;
                
            //Use multiplier to adjust power
            power = power * leftMultiplier;
            
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
                rightMultiplier = .96;
            else if(maxPower == controller.defaultMaxMotorPower)
                rightMultiplier = .94;
            else if(maxPower == controller.sensitiveMotorPowerX) 
                rightMultiplier = .92;
            else if(maxPower == controller.sensitiveMotorPowerCircle)
                rightMultiplier = .93;
            else if(maxPower == controller.sensitiveMotorPowerTriangle)
                rightMultiplier = .95;
            else if(maxPower == controller.sensitiveMotorPowerSquare)
                rightMultiplier = .92;
                
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
void draw() { 
    //Update data before drawing
    update();   
    
    //Pause the program temporarily
    delay(50);
}



//////////////////////////////////////////
//ASSISTIVE FUNCTIONS
//////////////////////////////////////////


void sonarPing(StandardServo sonarServo, UltraSonicSensor frontSensor, UltraSonicSensor rearSensor, int amount) { 
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
void initWindow() {
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

