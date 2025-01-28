package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactoradicTest {

    @Test
    void shouldGenerateCorrectValuesForFirst7Factoradic() {
        String[] expected = {
                "[0]",
                "[1, 0]",
                "[1, 0, 0]",
                "[1, 1, 0]",
                "[2, 0, 0]",
                "[2, 1, 0]",
                "[1, 0, 0, 0]"
        };
        for (int i = 0; i <= 6; i++) {
            assertEquals(expected[i], Factoradic.of(i).toString());
        }
    }

    @Test
    void shouldReturnCorrectDecimalEquivalentOfFactoradic() {
        for (int i = 0; i <= 100; i++) {
            assertEquals(i, Factoradic.of(i).decimalValue.intValue());
        }
    }

    @Test
    void shouldReturnCorrectFactoradicForFewVeryLargeNumbers() {

        String[] expected = {
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 0, 0]",
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 1, 0]"
        };

        long decimalValue = 4611686018427387904L;
        Factoradic f1 = Factoradic.of(decimalValue);
        Factoradic f2 = Factoradic.of(decimalValue + 1);
        assertEquals(expected[0], f1.toString());
        assertEquals(expected[1], f2.toString());
    }

    @Test
    void testEqualsAndHashCodeForCoverage() {
        int decimal = 23456;
        Factoradic f1 = Factoradic.of(decimal);
        Factoradic f2 = Factoradic.of(decimal);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertEquals(f1.hashCode(), decimal);
    }
}
