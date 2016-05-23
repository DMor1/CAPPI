//Author: Daniel Mor
//Date Started: 11/20/2012
//Updated: 2/23/2013
//Program Details: A class that controls a virtual PS3 controller using Procontroll and MotionInJoy


//////////////////////////////////////////
//IMPORT LIBRARIES
import procontroll.*; //Procontroll allows interfacing with game controllers


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
    void triangleButtonPressed() {println2("Triangle Pressed"); triangleButtonPressed = true; lowMotorPower = sensitiveMotorPowerTriangle;}
    void triangleButtonReleased() {println2("Triangle Released"); triangleButtonPressed = false; triangleButtonHeld = false;}
    void triangleButtonHeld() {println2("Triangle Held"); triangleButtonHeld = true;}
    
    //Group of methods called when the circle button is pressed, released or held
    void circleButtonPressed() {println2("Circle Pressed"); circleButtonPressed = true; lowMotorPower = sensitiveMotorPowerCircle;}
    void circleButtonReleased() {println2("Circle Released"); circleButtonPressed = false; circleButtonHeld = false;}
    void circleButtonHeld() {println2("Circle Held"); circleButtonHeld = true;}
    
    //Group of methods called when the x button is pressed, released or held
    void xButtonPressed() {println2("X Pressed"); xButtonPressed = true; lowMotorPower = sensitiveMotorPowerX;}
    void xButtonReleased() {println2("X Released"); xButtonPressed = false; xButtonHeld = false;}
    void xButtonHeld() {println2("X Held"); xButtonHeld = false;}
    
    //Group of methods called when the square button is pressed, released or held
    void squareButtonPressed() {println2("Square Pressed"); squareButtonPressed = true; lowMotorPower = sensitiveMotorPowerSquare;}
    void squareButtonReleased() {println2("Square Released"); squareButtonPressed = false; squareButtonHeld = false;}
    void squareButtonHeld() {println2("Square Held"); squareButtonHeld = true;}
    
    //Group of methods called when the L1 button is pressed, released or held
    void L1ButtonPressed() {println2("L1 Pressed"); L1ButtonPressed = true; controllerGUI.setClawPosition(0);}
    void L1ButtonReleased() {println2("L1 Released"); L1ButtonPressed = false; L1ButtonHeld = false;}
    void L1ButtonHeld() {println2("L1 Held"); L1ButtonHeld = true;}
    
    //Group of methods called when the R1 button is pressed, released or held
    void R1ButtonPressed() {println2("R1 Pressed"); R1ButtonPressed = true; controllerGUI.setClawPosition(2400);}
    void R1ButtonReleased() {println2("R1 Released"); R1ButtonPressed = false; R1ButtonHeld = false;}
    void R1ButtonHeld() {println2("R1 Held"); R1ButtonHeld = true;}
    
    //Group of methods called when the L2 button is pressed, released or held
    void L2ButtonPressed() {println2("L2 Pressed"); L2ButtonPressed = true;}
    void L2ButtonReleased() {println2("L2 Released"); L2ButtonPressed = false; L2ButtonHeld = false;}
    void L2ButtonHeld() {println2("L2 Held"); L2ButtonHeld = true; controllerGUI.incrementClawPosition(-clawServoIncrement);}
    
    //Group of methods called when the R2 button is pressed, released or held
    void R2ButtonPressed() {println2("R2 Pressed"); R2ButtonPressed = true;}
    void R2ButtonReleased() {println2("R2 Released"); R2ButtonPressed = false; R2ButtonHeld = false;}
    void R2ButtonHeld() {println2("R2 Held"); R2ButtonHeld = true; controllerGUI.incrementClawPosition(clawServoIncrement);}
    
    //Group of methods called when the select button is pressed, released or held
    void selectButtonPressed() {println2("Select Pressed"); selectButtonPressed = true; arduino.hardReset(3);}
    void selectButtonReleased() {println2("Select Released"); selectButtonPressed = false; selectButtonHeld = false;}
    void selectButtonHeld() {println2("Select Held"); selectButtonHeld = true;}
    
    //Group of methods called when the start button is pressed, released or held
    void startButtonPressed() {println2("Start Pressed"); startButtonPressed = true; lowMotorPower = defaultLowMotorPower; maxMotorPower = defaultMaxMotorPower;}
    void startButtonReleased() {println2("Start Released"); startButtonPressed = false; startButtonHeld = false;}
    void startButtonHeld() {println2("Start Held"); startButtonHeld = true;}
    
    //Group of methods called when the left stick's button is pressed, released or held
    void leftStickButtonPressed() {println2("Left Stick Pressed"); leftStickButtonPressed = true;}
    void leftStickButtonReleased() {println2("Left Stick Released"); leftStickButtonPressed = false; leftStickButtonHeld = false;}
    void leftStickButtonHeld() {println2("Left Stick Held"); leftStickButtonHeld = true;}
    
    //Group of methods called when the right stick's button is pressed, released or held
    void rightStickButtonPressed() {println2("Right Stick Pressed"); rightStickButtonPressed = true;}
    void rightStickButtonReleased() {println2("Right Stick Released"); rightStickButtonPressed = false; rightStickButtonHeld = false;}
    void rightStickButtonHeld() {println2("Right Stick Held"); rightStickButtonHeld = true;}
    
    //Group of methods called when the PS3 button is pressed, released or held
    void PS3ButtonPressed() {println2("PS3 Pressed"); PS3ButtonPressed = true; maxPower = !maxPower;}
    void PS3ButtonReleased() {println2("PS3 Released"); PS3ButtonPressed = false; PS3ButtonHeld = false;}
    void PS3ButtonHeld() {println2("PS3 Held"); PS3ButtonHeld = true;}
    
    //Group of methods called when the D-Pad's Up button is pressed, released or held
    void dPadUpButtonPressed() {println2("D-pad Up Pressed"); dPadUpButtonPressed = true;}
    void dPadUpButtonReleased() {println2("D-pad Up Released"); dPadUpButtonPressed = false; dPadUpButtonHeld = false;}
    void dPadUpButtonHeld() {println2("D-pad Up Held"); dPadUpButtonHeld = true;}
    
    //Group of methods called when the D-Pad's Right button is pressed, released or held
    void dPadRightButtonPressed() {println2("D-Pad Right Pressed"); dPadRightButtonPressed = true;}
    void dPadRightButtonReleased() {println2("D-Pad Right Released"); dPadRightButtonPressed = false; dPadRightButtonHeld = false;}
    void dPadRightButtonHeld() {println2("D-Pad Right Held"); dPadRightButtonHeld = true;}
    
    //Group of methods called when the D-Pad's down button is pressed, released or held
    void dPadDownButtonPressed() {println2("D-Pad Down Pressed"); dPadDownButtonPressed = true;}
    void dPadDownButtonReleased() {println2("D-Pad Down Released"); dPadDownButtonPressed = false; dPadDownButtonHeld = false;}
    void dPadDownButtonHeld() {println2("D-Pad Down Held"); dPadDownButtonHeld = true;}
    
    //Group of methods called when the D-Pad's Left button is pressed, released or held
    void dPadLeftButtonPressed() {println2("D-Pad Left Pressed"); dPadLeftButtonPressed = true;}
    void dPadLeftButtonReleased() {println2("D-Pad Left Released"); dPadLeftButtonPressed = false; dPadLeftButtonHeld = false;}  
    void dPadLeftButtonHeld() {println2("D-Pad Left Held"); dPadLeftButtonHeld = true;}  
    
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

