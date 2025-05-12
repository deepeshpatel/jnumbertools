/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.util.ArrayList;
import java.util.List;

public final class MixedRadix {
    public static int[] toMixedRadix(int num, int[] bases) {
        List<Integer> digits = new ArrayList<>();

        for (int i = bases.length - 1; i >= 0; i--) {
            int digit = num % bases[i];
            num /= bases[i];
            digits.add(0, digit);
        }

        return digits.stream().mapToInt(Integer::intValue).toArray();
    }

    public static int toDecimal(int[] mixedRadix, int[] radix) {
        int decimal = 0;
        int factor = 1;
        for (int i = 0; i < radix.length; i++) {
            decimal += mixedRadix[i] * factor;
            factor *= radix[i];
        }
        return decimal;
    }
}
