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

