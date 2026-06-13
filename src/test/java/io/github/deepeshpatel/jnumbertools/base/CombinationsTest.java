package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Combinations Factory")
class CombinationsTest {

    private final Combinations combinations = new Combinations(calculator);

    /*
    ===============================================================================
    UNIQUE COMBINATION (ⁿCᵣ)
    ===============================================================================
    n (set size) | r (selection) | Mathematical | Count | Iterator Returns
    -------------|---------------|--------------|-------|------------------
    n = 0        | r = 0         | ⁰C₀ = 1      | 1     | [[]] (one empty combination)
    n = 0        | r > 0         | ⁰Cᵣ = 0      | 0     | [] (empty iterator)
    n > 0        | r = 0         | ⁿC₀ = 1      | 1     | [[]] (one empty combination)
    n > 0        | 0 < r ≤ n     | ⁿCᵣ          | ⁿCᵣ   | combinations
    n > 0        | r > n         | ⁿCᵣ = 0      | 0     | [] (empty iterator)
    n < 0        | any r         | invalid      | -     | THROW IllegalArgumentException
    r < 0        | any n         | invalid      | -     | THROW IllegalArgumentException
    null input   | any r         | invalid      | -     | THROW NullPointerException
    */

    @Nested
    @DisplayName("unique(r, List<T>) | unique(r, T... elements) | unique(n, r)")
    class UniqueTests {
        @Test
        @DisplayName("unique(List<T>): null input throws NPE")
        void uniqueListNullInput() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    combinations.unique(2, (List<String>) null));
            assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("unique(List<T>): negative n throws IAE")
        void uniqueListNegativeN() {
            var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                    combinations.unique(-1, 2));
            assertTrue(negativeNExp.getMessage().startsWith(errMsgNK));
        }

        @Test
        @DisplayName("unique(List<T>): negative r throws IAE")
        void uniqueListNegativeR() {
            var negativeRExp = assertThrows(IllegalArgumentException.class, () ->
                    combinations.unique(5, -2));
            assertTrue(negativeRExp.getMessage().startsWith(errMsgNK));
        }

        @Test
        @DisplayName("unique(T... elements): null varargs array throws NPE")
        void uniqueVarArgsNullInput() {
            String[] nullArray = null;
            assertThrows(NullPointerException.class, () ->
                    combinations.unique(2, nullArray));
        }

        @Test
        @DisplayName("unique(n, r): negative n throws IAE")
        void uniqueIntNegativeN() {
            assertThrows(IllegalArgumentException.class, () ->
                    combinations.unique(-1, 2));
        }

        @Test
        @DisplayName("unique(n, r): negative r throws IAE")
        void uniqueIntNegativeR() {
            assertThrows(IllegalArgumentException.class, () ->
                    combinations.unique(5, -2));
        }
    }

    /*
    ===============================================================================
    REPETITIVE COMBINATION (ⁿ⁺ʳ⁻¹Cᵣ)
    ===============================================================================
    n (set size) | r (selection) | Mathematical     | Count | Iterator Returns
    -------------|---------------|------------------|-------|------------------
    n = 0        | r = 0         | (by convention)  | 1     | [[]] (one empty combination)
    n = 0        | r > 0         | 0                | 0     | [] (empty iterator)
    n > 0        | r = 0         | 1                | 1     | [[]] (one empty combination)
    n > 0        | r > 0         | ⁿ⁺ʳ⁻¹Cᵣ          | ⁿ⁺ʳ⁻¹Cᵣ | combinations
    n < 0        | any r         | invalid          | -     | THROW IllegalArgumentException
    r < 0        | any n         | invalid          | -     | THROW IllegalArgumentException
    null input   | any r         | invalid          | -     | THROW NullPointerException
    */

    @Nested
    @DisplayName("repetitive(r, List<T>) | repetitive(r, T... elements) | repetitive(n, r)")
    class RepetitiveTests {
        @Test
        @DisplayName("repetitive(List<T>): null input throws NPE")
        void repetitiveListNullInput() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    combinations.repetitive(2, (List<String>) null));
            assertTrue(nullExp.getMessage().startsWith(errMsgNullInput));
        }

        @Test
        @DisplayName("repetitive(List<T>): negative n throws IAE")
        void repetitiveListNegativeN() {
            var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                    combinations.repetitive(-1, 2));
            assertTrue(negativeNExp.getMessage().startsWith(errMsgNK));
        }

        @Test
        @DisplayName("repetitive(List<T>): negative r throws IAE")
        void repetitiveListNegativeR() {
            var negativeRExp = assertThrows(IllegalArgumentException.class, () ->
                    combinations.repetitive(3, -2));
            assertTrue(negativeRExp.getMessage().startsWith(errMsgNK));
        }

        @Test
        @DisplayName("repetitive(T... elements): null varargs array throws NPE")
        void repetitiveVarArgsNullInput() {
            String[] nullArray = null;
            assertThrows(NullPointerException.class, () ->
                    combinations.repetitive(2, nullArray));
        }

        @Test
        @DisplayName("repetitive(n, r): negative n throws IAE")
        void repetitiveIntNegativeN() {
            assertThrows(IllegalArgumentException.class, () ->
                    combinations.repetitive(-1, 2));
        }

        @Test
        @DisplayName("repetitive(n, r): negative r throws IAE")
        void repetitiveIntNegativeR() {
            assertThrows(IllegalArgumentException.class, () ->
                    combinations.repetitive(3, -2));
        }
    }

    /*
    ===============================================================================
    MULTISET COMBINATION
    ===============================================================================
    Map State   | r (selection) | Mathematical           | Count | Iterator Returns
    ------------|---------------|------------------------|-------|------------------
    Empty map   | r = 0         | 1                      | 1     | [{}] (one empty map)
    Empty map   | r > 0         | 0                      | 0     | [] (empty iterator)
    Non-empty   | r = 0         | 1                      | 1     | [{}] (one empty map)
    Non-empty   | r > 0         | multisetCombinationsCount(r, freq) | calculated | combinations
    Non-empty   | r > ∑fᵢ       | 0                      | 0     | [] (empty iterator)
    null map    | any r         | invalid                | -     | THROW NullPointerException
    negative freq| any r        | invalid                | -     | THROW IllegalArgumentException
    */

    @Nested
    @DisplayName("multiset(options, r)")
    class MultisetTests {
        @Test
        @DisplayName("null map throws NPE")
        void nullMap() {
            var nullExp = assertThrows(NullPointerException.class, () ->
                    combinations.multiset(null, 2));
            assertEquals(errMsgOptions, nullExp.getMessage());
        }

        @Test
        @DisplayName("negative frequency throws IAE")
        void negativeFrequency() {
            var negativeFreqOptions = new LinkedHashMap<String, Integer>();
            negativeFreqOptions.put("A", -1);
            var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                    combinations.multiset(negativeFreqOptions, 2));
            assertEquals(errMsgOptions, negativeExp.getMessage());
        }
    }
}