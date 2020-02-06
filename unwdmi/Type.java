package unwdmi;

/**
 * Possible types of data.
 */
public enum Type {
    TEMP (1, true, 1, true, 3),
    DEWP (1, true, 1, true, 3),
    SLP (1, true, 1, false, 3),
    VISIB (1, true, 1, false, 2),
    WDSP (1, true, 1, false, 2),
    PRCP (1, true, 2, false, 3),
    SNDP (1, true, 1, true, 3),
    CLDC (1, true, 1, false, 2),
    STP (1, false, 1, false, 3),
    STN (1, false),
    DATE (0, false),
    TIME (0, false),
    FRSHTT (2, false, 1),
    WNDDIR (1, false, 2);

    public final static short STRING = 0;
    public final static short NUMBER = 1;
    public final static short BIT = 2;

    private int type = 0;
    private int exponent = 0;
    private boolean correction;
    private boolean negative = false;
    private int byteLength = 0;

    Type(int t, boolean c) {
        type = t;
        correction = c;
    }

    Type(int t, boolean c, int b) {
        type = t;
        correction = c;
        byteLength = b;
    }

    Type(int t, boolean c, int e, boolean n, int b) {
        type = t;
        correction = c;
        exponent = e;
        negative = n;
        byteLength = b;
    }

    /**
     * @return The type.
     */
    public int getType(){
        return type;
    }

    /**
     * @return The exponent.
     */
    public int getExponent(){
        return exponent;
    }

    /**
     * @return Whether correction should be done or not.
     */
    public boolean shouldCorrect(){
        return correction;
    }

    /**
     * @return Whether the type allows for negative values or not.
     */
    public boolean allowsNegative(){
        return negative;
    }

    /**
     * @return The byte length of the type.
     */
    public int getByteLength() {return byteLength;}
}