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
            distance = ((pingTime * .00034) / 2);
        else if(distanceUnit == Unit.CM)
            distance = ((pingTime * .034029) / 2);
        else if(distanceUnit == Unit.FT)
            distance = ((pingTime * .001116) / 2);
        else if(distanceUnit == Unit.IN)
            distance = ((pingTime * .013397) / 2);   
           
        if(distance > 2) 
            return distance;
        else 
           return 0; 
    }
}

