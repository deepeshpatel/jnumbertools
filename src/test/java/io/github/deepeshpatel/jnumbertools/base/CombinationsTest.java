package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.api.Combinations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombinationsTest {

    /*
    -------------------------------------------------------------------------------
    UNIQUE COMBINATION (ⁿCᵣ)
    -------------------------------------------------------------------------------
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

    private final Combinations combinations = new Combinations(calculator);

    @Test
    void unique() {
        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                combinations.unique(2, (List<String>) null));
        assertEquals(errMsgNullInput, nullExp.getMessage());

        // Test negative n - should throw IAE (outside rules)
        var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                combinations.unique(-1, 2));
        assertEquals(errMsgNK(-1,2),negativeNExp.getMessage());
        // Test negative r - should throw IAE (outside rules)
        var negativeRExp = assertThrows(IllegalArgumentException.class, () ->
                combinations.unique(5, -2));
        assertEquals(errMsgNK(5,-2),negativeRExp.getMessage());

        // Note: r > n is mathematically valid (count=0, tested in UniqueCombinationTest)
        // n=0, r=0 is mathematically valid (count=1, tested in UniqueCombinationTest)
        // n=0, r>0 is mathematically valid (count=0, tested in UniqueCombinationTest)
    }


    /*
    -------------------------------------------------------------------------------
    REPETITIVE COMBINATION (ⁿ⁺ʳ⁻¹Cᵣ)
    -------------------------------------------------------------------------------
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

    @Test
    void repetitive() {
        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                combinations.repetitive(2, (List<String>) null));
        assertEquals(errMsgNullInput, nullExp.getMessage());

        // Test negative n - should throw IAE (outside rules)
        var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                combinations.repetitive(-1, 2));
        assertEquals(errMsgNK(-1,2),negativeNExp.getMessage());

        // Test negative r - should throw IAE (outside rules)
        var negativeRExp = assertThrows(IllegalArgumentException.class, () ->
                combinations.repetitive(3, -2));
        assertEquals(errMsgNK(3,-2),negativeRExp.getMessage());

        // Note: n=0, r=0 is valid (tested in RepetitiveCombinationTest)
        // n=0, r>0 is valid (tested in RepetitiveCombinationTest)
        // r>n is valid (always allowed for repetitive combinations)
    }

    @Test
    void testRepetitive() {
        // Test with varargs - only exception paths
        String[] nullArray = null;
        assertThrows(NullPointerException.class, () ->
                combinations.repetitive(2, nullArray));

        // Note: empty varargs with r=0 is valid (tested in RepetitiveCombinationTest)
        // empty varargs with r>0 is valid (count=0, tested in RepetitiveCombinationTest)
    }

    @Test
    void testRepetitive1() {
        // Test with integer range only - only exception paths
        // Test negative n
        assertThrows(IllegalArgumentException.class, () ->
                combinations.repetitive(-1, 2));

        // Test negative r
        assertThrows(IllegalArgumentException.class, () ->
                combinations.repetitive(3, -2));

        // Note: n=0, r=0 is valid (tested in RepetitiveCombinationTest)
        // n=0, r>0 is valid (tested in RepetitiveCombinationTest)
    }

    /*
    -------------------------------------------------------------------------------
    MULTISET COMBINATION
    -------------------------------------------------------------------------------
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

    @Test
    void multiset() {
        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                combinations.multiset(null, 2));
        assertEquals(errMsgOptions, nullExp.getMessage());

        // Test negative frequency - should throw IAE (outside rules)
        var negativeFreqOptions = new LinkedHashMap<String, Integer>();
        negativeFreqOptions.put("A", -1);
        var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                combinations.multiset(negativeFreqOptions, 2));
        assertEquals(errMsgOptions, negativeExp.getMessage());

        // Note: empty map is allowed (tested in MultisetCombinationTest)
        // zero frequency is allowed (element not available) - tested in MultisetCombinationTest
        // r > total available is valid (count=0) - tested in MultisetCombinationTest
    }
}
