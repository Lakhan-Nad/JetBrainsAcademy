package converter;

class Constants {
    private Constants() {
    }

    /**
     * The base word for use
     */
    public static final long BASE = 0x100000000L;

    /**
     * Used for masking and converting integers to long
     * so that full 32 bits can be used as magnitude
     */
    public static final long MASK = 0xffffffffL;
}
