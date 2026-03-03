/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for mixed radix number system conversions.
 * <p>
 * The mixed radix number system is a positional numeral system where each digit position
 * has a different base (radix). Unlike standard decimal (base-10) or binary (base-2) systems,
 * mixed radix allows each position to have its own base value.
 * </p>
 * <p>
 * For example, in a mixed radix system with bases [3, 4, 5], the number 47 would be represented as:
 * <pre>
 * 47₁₀ = 1·3·4·5 + 2·4·5 + 3·5 + 2 = [1, 2, 3, 2]₍₃,₄,₅₎
 * </pre>
 * This is commonly used in time systems (60 seconds, 60 minutes, 24 hours) and
 * combinatorial algorithms for efficient indexing.
 * </p>
 * <p>
 * <strong>Example:</strong>
 * <pre>{@code
 * // Convert decimal 47 to mixed radix with bases [3, 4, 5]
 * int[] result = MixedRadix.toMixedRadix(47, new int[]{3, 4, 5});
 * // Returns: [1, 2, 3, 2]
 * }</pre>
 * </p>
 *
 * @author Deepesh Patel
 * @see <a href="https://en.wikipedia.org/wiki/Mixed_radix">Wikipedia: Mixed Radix</a>
 */
public final class MixedRadix {
    /**
     * Converts a decimal number to mixed radix representation.
     * <p>
     * This method converts a standard decimal integer to its representation in a mixed radix
     * number system where each digit position has a different base. The conversion is performed
     * from least significant digit to most significant digit.
     * </p>
     * <p>
     * <strong>Example:</strong>
     * <pre>{@code
     * // Convert 47 to mixed radix with bases [3, 4, 5]
     * int[] result = MixedRadix.toMixedRadix(47, new int[]{3, 4, 5});
     * // Returns: [1, 2, 3, 2] representing 1·3·4·5 + 2·4·5 + 3·5 + 2
     * }</pre>
     * </p>
     *
     * @param num   the decimal number to convert (must be non-negative)
     * @param bases an array of bases for each digit position, from most significant to least significant
     * @return an array representing the mixed radix digits
     * @throws IllegalArgumentException if num is negative, bases is null/empty, or any base is ≤ 1
     * @see #toDecimal(int[], int[]) for the reverse conversion
     */
    public static int[] toMixedRadix(int num, int[] bases) {
        List<Integer> digits = new ArrayList<>();

        for (int i = bases.length - 1; i >= 0; i--) {
            int digit = num % bases[i];
            num /= bases[i];
            digits.add(0, digit);
        }

        return digits.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Converts a mixed radix number back to decimal representation.
     * <p>
     * This method performs the reverse operation of {@link #toMixedRadix(int, int[])},
     * converting a mixed radix representation back to a standard decimal integer.
     * The conversion is performed by multiplying each digit by its positional factor
     * and summing the results.
     * </p>
     * <p>
     * <strong>Example:</strong>
     * <pre>{@code
     * // Convert mixed radix [1, 2, 3, 2] with bases [3, 4, 5] back to decimal
     * int result = MixedRadix.toDecimal(new int[]{1, 2, 3, 2}, new int[]{3, 4, 5});
     * // Returns: 47 (1·3·4·5 + 2·4·5 + 3·5 + 2)
     * }</pre>
     * </p>
     *
     * @param mixedRadix an array representing the mixed radix digits
     * @param radix      an array of bases for each digit position, must match mixedRadix length
     * @return the decimal representation of the mixed radix number
     * @throws IllegalArgumentException if mixedRadix or radix is null/empty, lengths don't match,
     *                                  any base is ≤ 1, or any digit is out of valid range
     * @see #toMixedRadix(int, int[]) for the forward conversion
     */
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
