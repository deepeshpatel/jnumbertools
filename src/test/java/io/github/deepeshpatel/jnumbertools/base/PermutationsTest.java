package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Permutations Factory")
class PermutationsTest {

    private final Permutations permutations = new Permutations(calculator);
    private final List<String> elements = List.of("A", "B", "C");

    /*
    ===============================================================================
    UNIQUE PERMUTATION (n!)
    ===============================================================================
    n (set size) | Mathematical | Count | Iterator Returns
    -------------|--------------|-------|------------------
    n = 0        | 0! = 1       | 1     | [[]] (one empty permutation)
    n > 0        | n!           | n!    | permutations
    n < 0        | invalid      | -     | THROW IllegalArgumentException
    null input   | invalid      | -     | THROW NullPointerException
    */

    @Nested
    @DisplayName("unique(List<T>) | unique(T... elements)")
    class UniqueTests {
        @Test
        @DisplayName("unique(List<T>): null input throws NPE")
        void uniqueListNullInput() {
            var nullInputExp = assertThrows(NullPointerException.class, () ->
                    permutations.unique((List<String>) null));
            assertTrue(nullInputExp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("unique(List<T>): negative n throws IAE")
        void uniqueListNegativeN() {
            var negativeNExp = assertThrows(IllegalArgumentException.class, ()-> permutations.unique(-2));
            assertEquals("n should be ≥ 0 for unique permutation generation", negativeNExp.getMessage());
        }

        @Test
        @DisplayName("unique(T... elements): valid varargs returns builder")
        void uniqueVarArgsValid() {
            var builder = permutations.unique("A", "B", "C");
            assertNotNull(builder);
            assertEquals(calculator.factorial(3), builder.count());
        }

        @Test
        @DisplayName("unique(T... elements): single element returns builder with count 1")
        void uniqueVarArgsSingleElement() {
            var singleBuilder = permutations.unique("X");
            assertEquals(calculator.factorial(1), singleBuilder.count());
        }

        @Test
        @DisplayName("unique(T... elements): empty varargs allowed with count 1")
        void uniqueVarArgsEmpty() {
            var emptyBuilder = permutations.unique(new String[0]);
            assertEquals(calculator.factorial(0), emptyBuilder.count());
        }
    }

    /*
    ===============================================================================
    K-PERMUTATION (ⁿPₖ)
    ===============================================================================
    n (set size) | k (selection) | Mathematical | Count | Iterator Returns
    -------------|---------------|--------------|-------|------------------
    n = 0        | k = 0         | ⁰P₀ = 1      | 1     | [[]] (one empty permutation)
    n = 0        | k > 0         | ⁰Pₖ = 0      | 0     | [] (empty iterator)
    n > 0        | k = 0         | ⁿP₀ = 1      | 1     | [[]] (one empty permutation)
    n > 0        | 0 < k ≤ n     | ⁿPₖ          | ⁿPₖ   | permutations
    n > 0        | k > n         | ⁿPₖ = 0      | 0     | [] (empty iterator)
    n < 0        | any k         | invalid      | -     | THROW IllegalArgumentException
    k < 0        | any n         | invalid      | -     | THROW IllegalArgumentException
    null input   | any k         | invalid      | -     | THROW NullPointerException
    */

    @Nested
    @DisplayName("nPk(k, List<T>) | nPk(k, T... elements)")
    class NpkTests {
        @Test
        @DisplayName("nPk(List<T>): null input throws NPE")
        void npkListNullInput() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    permutations.nPk(2, (List<String>) null));
            assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("nPk(List<T>): negative k throws IAE")
        void npkListNegativeK() {
            for(int n = 0; n <= 1; n++) {
                final int currentN = n;
                var negativeKExp = assertThrows(IllegalArgumentException.class, ()->
                        permutations.nPk(currentN, -3));
                assertTrue(negativeKExp.getMessage().startsWith(errMsgNK));
            }
        }

        @Test
        @DisplayName("nPk(List<T>): negative n throws IAE")
        void npkListNegativeN() {
            var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                    permutations.nPk(-1, 2));
            assertTrue(negativeNExp.getMessage().startsWith(errMsgNK));
        }

        @Test
        @DisplayName("nPk(T... elements): valid varargs with k > 0")
        void npkVarArgsValid() {
            var builder = permutations.nPk(2, "A", "B", "C", "D");
            assertNotNull(builder);
            assertEquals(calculator.nPr(4, 2), builder.count());
        }

        @Test
        @DisplayName("nPk(T... elements): k=0 with varargs")
        void npkVarArgsKZero() {
            var zeroBuilder = permutations.nPk(0, "A", "B", "C");
            assertEquals(calculator.nPr(3, 0), zeroBuilder.count());
        }

        @Test
        @DisplayName("nPk(T... elements): empty varargs with k=0 allowed")
        void npkVarArgsEmptyKZero() {
            var emptyBuilder = permutations.nPk(0, new String[0]);
            assertEquals(calculator.nPr(0, 0), emptyBuilder.count());
        }

        @Test
        @DisplayName("nPk(T... elements): empty varargs with k>0 allowed (mathematically valid)")
        void npkVarArgsEmptyKPositive() {
            assertDoesNotThrow(() -> permutations.nPk(2, new String[0]));
        }
    }

    /*
    ===============================================================================
    MULTISET PERMUTATION
    ===============================================================================
    Map State              | Mathematical      | Count | Iterator Returns
    -----------------------|-------------------|-------|------------------
    Empty map              | 0! = 1            | 1     | [[]] (one empty permutation)
    Non-empty map          | n!/(Πfᵢ!)         | multinomial | permutations
    All frequencies = 0    | treated as empty  | 1     | [[]] (one empty permutation)
    null map               | invalid           | -     | THROW NullPointerException
    negative frequency     | invalid           | -     | THROW IllegalArgumentException
    */

    @Nested
    @DisplayName("multiset(Map)")
    class MultisetTests {
        @Test
        @DisplayName("null input throws NPE")
        void nullInput() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    permutations.multiset(null));
            assertEquals(errMsgOptions, nullExp.getMessage());
        }

        @Test
        @DisplayName("negative frequency throws IAE")
        void negativeFrequency() {
            var negativeFreqOptions = new LinkedHashMap<String, Integer>();
            negativeFreqOptions.put("A", -1);
            var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                    permutations.multiset(negativeFreqOptions));
            assertEquals(errMsgOptions, negativeExp.getMessage());
        }
    }

    /*
    ===============================================================================
    REPETITIVE PERMUTATION (nʳ)
    ===============================================================================
    n (set size) | r (length) | Mathematical | Count | Iterator Returns
    -------------|------------|--------------|-------|------------------
    n = 0        | r = 0      | 0⁰ = 1       | 1     | [[]] (one empty permutation)
    n = 0        | r > 0      | 0ʳ = 0       | 0     | [] (empty iterator)
    n > 0        | r = 0      | n⁰ = 1       | 1     | [[]] (one empty permutation)
    n > 0        | r > 0      | nʳ           | nʳ    | permutations
    n < 0        | any r      | invalid      | -     | THROW IllegalArgumentException
    r < 0        | any n      | invalid      | -     | THROW IllegalArgumentException
    null input   | any r      | invalid      | -     | THROW NullPointerException
    */

    @Nested
    @DisplayName("repetitive(r, List<T>) | repetitive(r, T... elements)")
    class RepetitiveTests {
        @Test
        @DisplayName("repetitive(List<T>): null input throws NPE")
        void repetitiveListNullInput() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    permutations.repetitive(2, (List<String>) null));
            assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("repetitive(List<T>): negative width throws IAE")
        void repetitiveListNegativeWidth() {
            var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                    permutations.repetitive(-1, elements));
            assertEquals("Width (r) cannot be negative for repetitive permutation generation", negativeExp.getMessage());
        }

        @Test
        @DisplayName("repetitive(List<T>): negative n throws IAE")
        void repetitiveListNegativeN() {
            assertThrows(IllegalArgumentException.class, () ->
                    permutations.repetitive(2, -1));
        }

        @Test
        @DisplayName("repetitive(T... elements): negative width throws IAE")
        void repetitiveVarArgsNegativeWidth() {
            var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                    permutations.repetitive(-1, "A", "B"));
            assertEquals("Width (r) cannot be negative for repetitive permutation generation", negativeExp.getMessage());
        }

        @Test
        @DisplayName("repetitive(T... elements): empty varargs with r>0 allowed (mathematically valid)")
        void repetitiveVarArgsEmptyRPositive() {
            assertDoesNotThrow(() -> permutations.repetitive(2, new String[0]));
        }

        @Test
        @DisplayName("repetitive(T... elements): empty varargs with r=0 allowed (mathematically valid)")
        void repetitiveVarArgsEmptyRZero() {
            assertDoesNotThrow(() -> permutations.repetitive(0, new String[0]));
        }
    }
}