package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.api.Permutations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

class PermutationsTest {

    /*
    -------------------------------------------------------------------------------
    UNIQUE PERMUTATION (n!)
    -------------------------------------------------------------------------------
    n (set size) | Mathematical | Count | Iterator Returns
    -------------|--------------|-------|------------------
    n = 0        | 0! = 1       | 1     | [[]] (one empty permutation)
    n > 0        | n!           | n!    | permutations
    n < 0        | invalid      | -     | THROW IllegalArgumentException
    null input   | invalid      | -     | THROW NullPointerException
    */

    private final Permutations permutations = new Permutations(calculator);
    private final List<String> elements = List.of("A", "B", "C");

    @Test
    void unique() {
        // Test null input - should throw NPE (outside rules)
        var nullInputExp = assertThrows(NullPointerException.class, () ->
                permutations.unique((List<String>) null));
        assertEquals(errMsgNullInput, nullInputExp.getMessage());


        // Test negative n - should throw IAE (outside rules)
        var negativeNExp = assertThrows(IllegalArgumentException.class, ()-> permutations.unique(-2));
        assertEquals("n should be ≥ 0 for unique permutation generation", negativeNExp.getMessage());

        // Note: n=0 is allowed (covered in UniquePermutationBuilder tests)
    }

    @Test
    void uniqueVarArgs() {
        // Test with varargs - all valid cases
        var builder = permutations.unique("A", "B", "C");
        assertNotNull(builder);
        assertEquals(calculator.factorial(3), builder.count());

        // Test single element
        var singleBuilder = permutations.unique("X");
        assertEquals(calculator.factorial(1), singleBuilder.count());

        // Test empty varargs (n=0) - allowed by rules
        var emptyBuilder = permutations.unique(new String[0]);
        assertEquals(calculator.factorial(0), emptyBuilder.count());
    }

    /*
    -------------------------------------------------------------------------------
    K-PERMUTATION (ⁿPₖ)
    -------------------------------------------------------------------------------
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

    @Test
    void nPk() {
        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                permutations.nPk(2, (List<String>) null));
        assertEquals(errMsgNullInput, nullExp.getMessage());

        // Test negative k - should throw IAE (outside rules)
        for(int n = 0; n <= 1; n++) {
            final int currentN = n;
            var negativeKExp = assertThrows(IllegalArgumentException.class, ()->
                    permutations.nPk(currentN, -3));
            assertEquals(errMsgNK(currentN, -3),negativeKExp.getMessage());
        }

        // Test negative n - should throw IAE (outside rules)
        var negativeNExp = assertThrows(IllegalArgumentException.class, () ->
                permutations.nPk(-1, 2));
        assertEquals(errMsgNK(-1, 2),negativeNExp.getMessage());

        // Note: k > n is NOT tested here because it's mathematically valid
        // (returns count=0, empty iterator). This will be tested in KPermutationBuilderTest
    }

    @Test
    void nPkVarArgs() {
        // Test with varargs - valid cases only
        var builder = permutations.nPk(2, "A", "B", "C", "D");
        assertNotNull(builder);
        assertEquals(calculator.nPr(4, 2), builder.count());

        // Test k=0 with varargs (allowed by rules)
        var zeroBuilder = permutations.nPk(0, "A", "B", "C");
        assertEquals(calculator.nPr(3, 0), zeroBuilder.count());

        // Test empty varargs with k=0 (n=0, k=0) - allowed by rules
        var emptyBuilder = permutations.nPk(0, new String[0]);
        assertEquals(calculator.nPr(0, 0), emptyBuilder.count());

        // Test empty varargs with k>0 (n=0, k>0) - mathematically valid
        // returns count=0, empty iterator (tested in KPermutationBuilderTest)
        assertDoesNotThrow(() -> permutations.nPk(2, new String[0]));
    }

    /*
    -------------------------------------------------------------------------------
    MULTISET PERMUTATION
    -------------------------------------------------------------------------------
    Map State              | Mathematical      | Count | Iterator Returns
    -----------------------|-------------------|-------|------------------
    Empty map              | 0! = 1            | 1     | [[]] (one empty permutation)
    Non-empty map          | n!/(Πfᵢ!)         | multinomial | permutations
    All frequencies = 0    | treated as empty  | 1     | [[]] (one empty permutation)
    null map               | invalid           | -     | THROW NullPointerException
    negative frequency     | invalid           | -     | THROW IllegalArgumentException
    */

    @Test
    void multiset() {
        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                permutations.multiset(null));
        assertEquals(errMsgOptions, nullExp.getMessage());

        // Test negative frequency - should throw IAE (outside rules)
        var negativeFreqOptions = new LinkedHashMap<String, Integer>();
        negativeFreqOptions.put("A", -1);
        var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                permutations.multiset(negativeFreqOptions));
        assertEquals(errMsgOptions, negativeExp.getMessage());

        // Note: Empty map is allowed by rules (tested in MultisetPermutationBuilderTest)
        // Zero frequencies are allowed by rules (treated as element not available)
    }

    /*
    -------------------------------------------------------------------------------
    REPETITIVE PERMUTATION (nʳ)
    -------------------------------------------------------------------------------
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

    @Test
    void repetitive() {
        // Test null input - should throw NPE (outside rules)
        var nullExp = assertThrows(NullPointerException.class, () ->
                permutations.repetitive(2, (List<String>) null));
        assertEquals(errMsgNullInput, nullExp.getMessage());

        // Test negative width - should throw IAE (outside rules)
        var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                permutations.repetitive(-1, elements));
        assertEquals("Width (r) cannot be negative for repetitive permutation generation", negativeExp.getMessage());

        // Test negative n - should throw IAE (outside rules)
        assertThrows(IllegalArgumentException.class, () ->
                permutations.repetitive(2, -1));

        // Note: n=0 is allowed by rules (tested in RepetitivePermutationBuilderTest)
        // r=0 is allowed by rules (tested in RepetitivePermutationBuilderTest)
    }

    @Test
    void repetitiveVarArgs() {
        // Test width negative - should throw IAE (outside rules)
        var negativeExp = assertThrows(IllegalArgumentException.class, () ->
                permutations.repetitive(-1, "A", "B"));
        assertEquals("Width (r) cannot be negative for repetitive permutation generation", negativeExp.getMessage());

        // Test empty varargs with r>0 (n=0, r>0) - mathematically valid
        // returns count=0, empty iterator (tested in RepetitivePermutationBuilderTest)
        assertDoesNotThrow(() -> permutations.repetitive(2, new String[0]));

        // Test empty varargs with r=0 (n=0, r=0) - mathematically valid
        // returns count=1, [[]] (tested in RepetitivePermutationBuilderTest)
        assertDoesNotThrow(() -> permutations.repetitive(0, new String[0]));
    }
}
