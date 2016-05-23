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

