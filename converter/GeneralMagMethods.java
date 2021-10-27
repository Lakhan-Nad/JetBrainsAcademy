package converter;

import java.util.Arrays;

import static converter.Constants.BASE;
import static converter.Constants.MASK;

class GeneralMagMethods {
    /*
        Hidden Constructor
     */
    private GeneralMagMethods() {
    }

    /**
     * Performs the actual multiplication algorithm
     * @param magnitude1 array1 of magnitude
     * @param s1 start of magnitude1
     * @param e1 end of magnitude1
     * @param magnitude2 array2 of magnitude
     * @param s2 start of magnitude2
     * @param e2 end of magnitude2
     * @param result the array of result
     * @param s the start of result array
     */
    public static void basicMultiplyMagLoop(int[] magnitude1, int s1, int e1, int[] magnitude2, int s2, int e2,int[] result, int s) {
        Arrays.fill(result, 0);
        long mul, sum;
        long carry;
        /*
            the shift factor for result
            it assumes that the result array is big enough to accommodate
         */
        int sf = s - s1 - s2;
        for (int i = s2; i <= e2; i++) {
            carry = 0;
            for (int j = s1; j <= e1; j++) {
                mul = (magnitude1[j] & MASK) * (magnitude2[i] & MASK);
                sum = (result[i+j] & MASK) + mul + carry;
                result[i+j+sf] = (int) sum;
                carry = sum >>> Integer.SIZE;
            }
            if (carry > 0) {
                result[sf+i+e1+1] = (int) carry;
            }
        }
    }

    public static void basicMultiplyMagLoop(int[] magnitude1, int[] magnitude2, int[] result, int s) {
        basicMultiplyMagLoop(magnitude1,0,magnitude1.length-1,magnitude2,0, magnitude2.length-1, result, s);
    }

    public static void basicMultiplyMagLoop(int[] magnitude1, int[] magnitude2, int[] result) {
        basicMultiplyMagLoop(magnitude1,0,magnitude1.length-1,magnitude2,0, magnitude2.length-1, result, 0);
    }

    public static void subtractMagLoop(int[] largeMag, int sl, int el, int[] smallMag, int ss, int es, int[] result, int sr) {
        long borrow = 0;
        long sub;
        for (; sl <= el && ss < es; sl += 1, ss += 1) {
            sub = (largeMag[sl] & MASK) - (smallMag[ss] & MASK) - borrow;
            if (sub < 0) {
                borrow = 1;
                sub += BASE;
            } else {
                borrow = 0;
            }
            result[sr++] = (int) sub;
        }
        if (borrow > 0) {
            /* No possible overflow because subtraction
               always reduces the magnitude
             */
            sub = (result[sr] & MASK) - borrow;
            result[sr] = (int) sub;
        }
    }

    public static void subtractMagLoop(int[] largeMag, int[] smallMag, int[] result, int s) {
        basicMultiplyMagLoop(largeMag,0,largeMag.length-1,smallMag,0, smallMag.length-1, result, s);
    }

    public static void subtractMagLoop(int[] largeMag, int[] smallMag, int[] result) {
        basicMultiplyMagLoop(largeMag,0,largeMag.length-1,smallMag,0, smallMag.length-1, result, 0);
    }

    public static void addMagLoop(int[] largeMag, int sl, int el, int[] smallMag, int ss, int es, int[] result, int sr) {
        long carry = 0;
        long sum;
        for (; sl <= el && ss < es; sl += 1, ss += 1) {
            sum = (largeMag[sl] & MASK) + (smallMag[ss] & MASK) + carry;
            result[sr++] = (int) sum;
            carry = sum >> Integer.SIZE;
        }
        if (carry > 0) {
            result[sr] = (int) carry;
        }
    }

    public static void addMagLoop(int[] largeMag, int[] smallMag, int[] result, int s) {
        basicMultiplyMagLoop(largeMag,0,largeMag.length-1,smallMag,0, smallMag.length-1, result, s);
    }

    public static void addMagLoop(int[] largeMag, int[] smallMag, int[] result) {
        basicMultiplyMagLoop(largeMag,0,largeMag.length-1,smallMag,0, smallMag.length-1, result, 0);
    }

    /**
     * 1 if first > second
     * -1 if first < second
     * 0 if first == second
     *
     * @param first  the magnitude of first number
     * @param sf the starting of magnitude1
     * @param ef the end of magnitude1
     * @param second the magnitude of second number
     * @param ss the starting of magnitude2
     * @param es the end of magnitude2
     * @return integer
     */
    public static int compareMagnitude(int[] first, int sf, int ef, int[] second, int ss, int es) {
        int pa = leadingZeros(first, sf,ef);
        int pb = leadingZeros(second,ss,es);
        if ((pa-sf) > (pb-es)) {
            return 1;
        } else if ((pa-sf) < (pb-ss)) {
            return -1;
        }
        /*
            pa - sf == pb - ss in this point so checking
            only one in while loop is enough and
            one can be used as index for both
         */
        while (pa >= sf) {
            long diff = (first[pa--] & MASK) - (second[pb--] & MASK);
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            }
        }
        return 0;
    }

    public static int compareMagnitude(int[] first, int[] second) {
        return compareMagnitude(first,0, first.length, second, 0, second.length-1);
    }

    /**
     * Finds the first index with non-zero value
     * and -1 is returned if array is zero
     * @param number the array of magnitudes
     * @return index of first non-zero value
     */
    public static int leadingZeros(int[] number, int start, int end) {
        int x = end;
        while (x >= start && number[x] == 0) {
            x -= 1;
        }
        return x;
    }
    public static int leadingZeros(int[] number) {
        return leadingZeros(number, 0, number.length-1);
    }
}
