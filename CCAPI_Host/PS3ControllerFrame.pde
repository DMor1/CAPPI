//Author: Daniel Mor
//Date Started: 2/24/2013
//Program Details: Create a Processing GUI Window


//////////////////////////////////////////
//IMPORT LIBRARIES
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.ImageIcon;


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

