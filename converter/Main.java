package converter;

import java.math.BigInteger;
import java.util.Scanner;

class RadixConvertor {
    protected static final String DIGITS;
    protected static final int MAX_RADIX;
    protected static final int MIN_RADIX;
    protected static final int DECIMAL;
    protected static final int BINARY;
    protected static final int OCTAL;
    protected static final int HEXADECIMAL;
    protected static final char MINUS;
    protected static final char PLUS;
    protected static final char DOT;
    protected static final String EMPTY_STRING;
    protected final int radix;

    static {
        DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        MINUS = '-';
        PLUS = '+';
        DOT = '.';
        EMPTY_STRING = "";
        MAX_RADIX = 36;
        MIN_RADIX = 1;
        DECIMAL = 10;
        BINARY = 2;
        OCTAL = 8;
        HEXADECIMAL = 16;
    }

    RadixConvertor(int radix) {
        this.radix = radix;
    }

    private static String fromARadixToOther(String number, int fromRadix, int toRadix) throws IllegalArgumentException {
        if (fromRadix < MIN_RADIX || fromRadix > MAX_RADIX) {
            throw new NumberFormatException(String.format("Invalid from radix provided expected [%d - %d] found %d", MIN_RADIX, MAX_RADIX, fromRadix));
        }
        if (toRadix < MIN_RADIX || toRadix > MAX_RADIX) {
            throw new NumberFormatException(String.format("Invalid to radix provided expected [%d - %d] found %d", MIN_RADIX, MAX_RADIX, toRadix));
        }
        boolean negate = false;
        int cursor = 0;
        int len = number.length();
        int index1 = number.lastIndexOf(MINUS);
        int index2 = number.lastIndexOf(PLUS);
        if (index1 != -1) {
            if (index1 != 0 || index2 >= 0) {
                throw new NumberFormatException("Sign of the number must be at start");
            }
            negate = true;
            cursor = 1;
        } else if (index2 >= 0) {
            if (index2 != 0) {
                throw new NumberFormatException("Sign of the number must be at start");
            }
            cursor = 1;
        }
        if (cursor == len) {
            throw new NumberFormatException("Zero length number provided");
        }
        index1 = number.indexOf(DOT);
        index2 = number.lastIndexOf(DOT);
        if (index1 != index2) {
            throw new NumberFormatException("Wrong number format multiple '.' occurrences");
        }
        // If both radixes are same just return the same ans
        if (fromRadix == toRadix) {
            return number;
        }
        String integer;
        String fraction = EMPTY_STRING;
        if (index1 != -1) {
            integer = number.substring(cursor, index1);
            fraction = number.substring(index1 + 1);
        } else {
            integer = number.substring(cursor);
        }
        StringBuilder sb = new StringBuilder();
        if (negate) {
            sb.append(MINUS);
        }
        if (!EMPTY_STRING.equals(integer)) {
            sb.append(integerConversion(integer, fromRadix, toRadix));
        }
        if (!EMPTY_STRING.equals(fraction)) {
            sb.append(DOT);
            sb.append(fractionConversion(fraction, fromRadix, toRadix));
        }
        return sb.toString();
    }

    private static String integerConversion(String integer, int fromRadix, int toRadix) {
        if (fromRadix == 1) {
            int number = integer.length();
            return new BigInteger(String.valueOf(number), DECIMAL).toString(toRadix);
        }
        if (toRadix == 1) {
            int number = new BigInteger(integer, fromRadix).intValue();
            char one = '1';
            return String.valueOf(one).repeat(Math.max(0, number));
        }
        return new BigInteger(integer, fromRadix).toString(toRadix);
    }

    private static String fractionConversion(String fractional, int fromRadix, int toRadix) {
        double value = 0;
        double div = 1.0;
        double x;
        for (int i = 0; i < fractional.length(); i++) {
            x = Character.digit(fractional.charAt(i), fromRadix);
            div *= fromRadix;
            value += x / div;
        }
        StringBuilder sb = new StringBuilder();
        int max_char = 5;
        int chars = 0;
        double delta = 0.0000000000000001;
        while (value >= delta && chars < max_char) {
            value *= toRadix;
            x = (int) value;
            value -= x;
            sb.append(Character.forDigit((int) x, toRadix));
            chars++;
        }
        return sb.toString();
    }

    public String toDecimal(String number) {
        return fromARadixToOther(number, this.radix, DECIMAL);
    }

    public String toARadix(String number, int otherRadix) {
        return fromARadixToOther(number, this.radix, otherRadix);
    }

    public String fromDecimal(String decimalNumber) {
        return fromARadixToOther(decimalNumber, DECIMAL, this.radix);
    }

    public String fromRadix(String number, int otherRadix) {
        return fromARadixToOther(number, otherRadix, this.radix);
    }

    int checkIfNotValidForThisRadix(String number) {
        int j;
        for (int i = 0; i < number.length(); i++) {
            j = DIGITS.indexOf(number.charAt(i));
            if (j == -1 || j >= radix) {
                return i;
            }
        }
        return -1;
    }

    protected String getErrorCharMessage(char badChar, int index) {
        return String.format("Unexpected character %c found in the index %d", badChar, index);
    }
}

class BinaryConvertor extends RadixConvertor {
    public BinaryConvertor() {
        super(BINARY);
    }
}

class HexConvertor extends RadixConvertor {
    public HexConvertor() {
        super(HEXADECIMAL);
    }
}

class OctalConvertor extends RadixConvertor {
    public OctalConvertor() {
        super(OCTAL);
    }
}

class DecimalConvertor extends RadixConvertor {
    public DecimalConvertor() {
        super(DECIMAL);
    }

    @Override
    public String toDecimal(String decimalNumber) {
        int badIndex = checkIfNotValidForThisRadix(decimalNumber);
        if (badIndex != -1) {
            throw new IllegalArgumentException(getErrorCharMessage(decimalNumber.charAt(badIndex), badIndex));
        }
        return decimalNumber;
    }

    @Override
    public String fromDecimal(String decimalNumber) {
        return toDecimal(decimalNumber);
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int fromRadix = 2;
        String number = "";
        int toRadix = 2;
        try {
            fromRadix = sc.nextInt();
            number = sc.next();
            toRadix = sc.nextInt();
            try {
                RadixConvertor cv = new RadixConvertor(fromRadix);
                System.out.println(cv.toARadix(number, toRadix));
            }catch (NumberFormatException e) {
                System.out.println("error " + e.getMessage());
            }
        }catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
        sc.close();
    }
}
