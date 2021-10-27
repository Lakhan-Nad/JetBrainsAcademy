package converter;

import java.util.Arrays;

import static converter.Constants.BASE;
import static converter.Constants.MASK;
import static converter.GeneralMagMethods.*;

public class Base implements Comparable<Base> {
    private int[] mag;
    private int sign;
    /**
     * Minimum allocated size of the magnitude array
     */
    private static final int MIN_SIZE = 16;
    /**
     * While default reallocation the size is increased by
     * current size / 2<sup>{this value}</sup> factor
     */
    private static final int MAG_LEN_COMPARE_FACTOR = 2;

    private static final int SHRINK_FACTOR = 8192;

    /**
     * The param is treated as unsigned magnitude
     * of 32 bits
     * @param a the number add
     */
    private void uAdd(long a) {
        if (a == 0) {
            return;
        } else if (sign == 0) {
            mag = new int[MIN_SIZE];
            sign = 1;
            if (a < 0) {
                sign *= -1;
                a *= -1;
            }
            mag[0] = (int) a;
            return;
        } else if (a < 0) {
            a *= -1;
            if (sign > 0) {
                uSubtract(a);
                return;
            }
        }
        long carry = a & MASK;
        int len = mag.length;
        long sum;
        for (int i = 0; i < len; i++) {
            sum = (mag[i] & MASK) + carry;
            mag[i] = (int) sum;
            carry = sum >>> Integer.SIZE;
        }
        if (carry > 0) {
            reallocate(len);
            mag[len] = (int) carry;
        }
    }

    /**
     * The param is treated as unsigned magnitude
     * of 32 bits
     * @param s the number to subtract
     */
    public void uSubtract(long s) {
        if (s == 0) {
            return;
        } else if (sign == 0) {
            sign = -1;
            if (s < 0) {
                sign *= -1;
                s *= -1;
            }
            mag = new int[MIN_SIZE];
            mag[0] = (int) s;
        } else if (s < 0) {
            s *= -1;
            if (sign > 0) {
                uAdd(s);
                return;
            }
        }
        long borrow = s & MASK;
        long sub;
        if ((mag[0] & MASK) > borrow || (mag[1] & MASK) > 0) {
            sub = (mag[0] & MASK) - borrow;
            if (sub < 0) {
                sub += BASE;
                mag[0] = (int) sub;
                mag[1] -= 1;
            }
        } else {
            mag[0] = (int) ((borrow & MASK) - (mag[0] & MASK));
            sign = -1;
        }
    }

    /**
     * The param is treated as unsigned magnitude
     * of 32 bits
     * @param m the number to multiply
     */
    private void uMultiply(long m) {
        m = (m & MASK);
        if (m == 0) {
            assignZero();
            return;
        }
        long mul;
        long carry = 0;
        int len = mag.length;
        if (mag[len - 1] > 0) {
            reallocate();
        }
        for (int i = 0; i < len; i++) {
            /* the value of mul will never overflow
                max(mul) = (2^32 -1)*(2^32) < (2 ^ 64)
             */
            mul = (mag[i] & MASK) * m + carry;
            mag[i] = (int) mul;
            carry = mul >>> Integer.SIZE;
        }
    }

    public int compareTo(Base b) {
        if (sign != b.sign) {
            return sign < b.sign ? -1 : 1;
        } else if (sign == 0) {
            return 0;
        } else {
            return compareMagnitude(mag, b.mag);
        }
    }

    /**
     * Reallocates the required mag size + 2
     * THe choice of +2 is due to implementation of
     * add and subtract functions
     * @param len size to reallocate to
     */
    private void reallocate(int len) {
        if (len < mag.length) {
            /* the method never decrease the size
               use shrink instead.
            */
            // Switch to default reallocation
            len = mag.length + (mag.length >>> MAG_LEN_COMPARE_FACTOR) - 2;
        }
        /* the extra 2 is added for addition and subtraction code
            while adding or subtracting the len argument send doesn't
            take into account the overflow factor
         */
        int newLen = len + 2;
        int[] newMag = new int[newLen];
        System.arraycopy(mag, 0, newMag, 0, mag.length);
        mag = newMag;
    }

    /**
     * Default relocation increases
     * size by 1/4 th of current size
     * can be tuned using EXPAND_FACTOR
     */
    private void reallocate() {
        int newLen = mag.length + (mag.length >>> MAG_LEN_COMPARE_FACTOR);
        int[] newMag = new int[newLen];
        System.arraycopy(mag, 0, newMag, 0, mag.length);
        mag = newMag;
    }

    /**
     * Default shrinking is removing of leading zeros
     * and the shrinking is only done if the size hold is
     * greater than a factor to prevent the overhead of
     * new array creation and copy element
     * and yes the shrinking is upto min length
     */
    private void shrink() {
        /* default shrink always removes the zeros
           in the magnitude array but never goes below
           minimum size
         */
        int len = leadingZeros(mag) + 1;
        int shrinkSize = mag.length - len;
        if (shrinkSize < (mag.length >>> MAG_LEN_COMPARE_FACTOR) || shrinkSize < SHRINK_FACTOR) {
            return;
        }
        if (len < MIN_SIZE) {
            len = MIN_SIZE;
        }
        int[] newMag = new int[len];
        System.arraycopy(mag, 0, newMag, 0, len);
        mag = newMag;
    }

    /**
     * Assigns zero to this number
     * changes sign as well as magnitude
     */
    private void assignZero() {
        sign = 0;
        Arrays.fill(mag, 0);
    }


}
