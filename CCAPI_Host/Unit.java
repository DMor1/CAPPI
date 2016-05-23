//An enumeration to holds units of distance
public enum Unit
{
    //ENUMERATION VALUES
    METER(0),
    CM(1),
    FT(2),
    IN(3);
    
    //The value the enum holds
    private int value;
    
    //Basic constructor
    Unit(int value) {
        this.value = value;
    }
    
    //REturn the int that identifies this enum
    public int getUnitID() {
        return value;
    }
}

