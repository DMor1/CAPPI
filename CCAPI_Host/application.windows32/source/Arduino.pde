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


