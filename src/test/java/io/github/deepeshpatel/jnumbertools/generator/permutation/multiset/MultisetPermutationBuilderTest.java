package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.BuilderTestHelper;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.LinkedHashMap;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class MultisetPermutationBuilderTest {

    /*
    -------------------------------------------------------------------------------
    Multiset Permutation Builder Validation Rules
    -------------------------------------------------------------------------------
    Method        | Input Validation                                  | Throws
    --------------|---------------------------------------------------|------------------
    lexOrder()    | none                                              | -
    lexOrderMth() | m ≤ 0                                             | IllegalArgumentException
                  | start < 0                                         | IllegalArgumentException
                  | start ≥ count()                                   | IllegalArgumentException
    byRanks()     | null ranks                                        | IllegalArgumentException
    choice()      | sampleSize ≤ 0                                    | IllegalArgumentException
    sample()      | sampleSize ≤ 0                                    | IllegalArgumentException
                  | sampleSize > count()                              | IllegalArgumentException
    count()       | none (always valid)                               | -
    */

    private final Permutations permutations = new Permutations(calculator);
    private final LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
    private final MultisetPermutationBuilder<String> builder;

    {
        options.put("A", 2);
        options.put("B", 1);
        options.put("C", 1);
        builder = permutations.multiset(options);
    }

    @Test
    void lexOrder() {
        BuilderTestHelper.testLexOrder(builder);
    }

    @Test
    void lexOrderMth() {
        BuilderTestHelper.testLexOrderMth(builder);
    }

    @Test
    void byRanks() {
        BuilderTestHelper.testByRanks(builder);
    }

    @Test
    void choice() {
        BuilderTestHelper.testChoice(builder);
    }

    @Test
    void sample() {
        BuilderTestHelper.testSample(builder);
    }

    @Test
    void count() {
        // Normal case: {A=2, B=1, C=1} -> 4!/(2!·1!·1!) = 12
        assertEquals(BigInteger.valueOf(12), builder.count());

        // Single element with frequency 3 -> 3!/3! = 1
        var singleOptions = new LinkedHashMap<String, Integer>();
        singleOptions.put("X", 3);
        var singleBuilder = permutations.multiset(singleOptions);
        assertEquals(BigInteger.ONE, singleBuilder.count());

        // Empty map -> 0! = 1
        var emptyBuilder = permutations.multiset(new LinkedHashMap<>());
        assertEquals(BigInteger.ONE, emptyBuilder.count());

        // Map with zero frequencies (filtered out) -> treated as empty
        var zeroFreqOptions = new LinkedHashMap<String, Integer>();
        zeroFreqOptions.put("A", 0);
        zeroFreqOptions.put("B", 0);
        var zeroFreqBuilder = permutations.multiset(zeroFreqOptions);
        assertEquals(BigInteger.ONE, zeroFreqBuilder.count());
    }

    private static LinkedHashMap<String, Integer> map(Object... keyValues) {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put((String) keyValues[i], (Integer) keyValues[i + 1]);
        }
        return m;
    }

    @Test
    void isEmpty() {
        assertFalse(builder.isEmpty());
        assertFalse(permutations.multiset(map()).isEmpty());                // empty map: the only result is []
        assertFalse(permutations.multiset(map("A", 0)).isEmpty());          // all zero: treated as empty map
        for (LinkedHashMap<String, Integer> m : List.of(map(), map("A", 0), map("A", 1), map("A", 2), map("A", 0, "B", 2), map("A", 1, "B", 1))) {
            BuilderTestHelper.testIsEmpty(permutations.multiset(m));
        }
    }

    @Test
    void emptyAndAllZeroMapsAreASingleEmptyResult() {
        BuilderTestHelper.testSingleEmptyResultDomain(permutations.multiset(map()));
        BuilderTestHelper.testSingleEmptyResultDomain(permutations.multiset(map("A", 0)));
        BuilderTestHelper.testSingleEmptyResultDomain(permutations.multiset(map("A", 0, "B", 0)));
    }

    @Test
    void zeroFrequencyEntriesAreIgnored() {
        var withZeros = permutations.multiset(map("A", 2, "X", 0, "B", 1, "Y", 0));
        var without = permutations.multiset(map("A", 2, "B", 1));
        assertEquals(without.count(), withZeros.count());
        assertEquals(without.lexOrder().stream().toList(), withZeros.lexOrder().stream().toList());
        assertEquals(without.lexOrderMth(2, 1).stream().toList(), withZeros.lexOrderMth(2, 1).stream().toList());
        assertEquals(without.byRanks(List.of(BigInteger.ZERO, BigInteger.TWO)).stream().toList(),
                withZeros.byRanks(List.of(BigInteger.ZERO, BigInteger.TWO)).stream().toList());
    }

    @Test
    void negativeFrequencyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> permutations.multiset(map("A", -1)));
        assertThrows(IllegalArgumentException.class, () -> permutations.multiset(map("A", 2, "B", -1)));
    }

    @Test
    void nullInputIsRejected() {
        assertThrows(NullPointerException.class, () -> permutations.multiset((LinkedHashMap<String, Integer>) null));
    }

    @Test
    void nullElementsAndNullFrequenciesAreRejectedWhenBuilderIsCreated() {
        LinkedHashMap<String, Integer> nullKey = map("A", 1);
        nullKey.put(null, 1);
        assertThrows(NullPointerException.class, () -> permutations.multiset(nullKey));

        LinkedHashMap<String, Integer> nullKeyWithZeroFrequency = new LinkedHashMap<>();
        nullKeyWithZeroFrequency.put(null, 0);          // must not be silently dropped by the zero-frequency filter
        assertThrows(NullPointerException.class, () -> permutations.multiset(nullKeyWithZeroFrequency));

        LinkedHashMap<String, Integer> nullFrequency = new LinkedHashMap<>();
        nullFrequency.put("A", null);
        assertThrows(NullPointerException.class, () -> permutations.multiset(nullFrequency));
    }

    @Test
    void builderSnapshotsItsInput() {
        LinkedHashMap<String, Integer> input = map("A", 1, "B", 1);
        var snapshot = permutations.multiset(input);
        var lexBefore = snapshot.lexOrder().stream().toList();
        var generatorCreatedEarlier = snapshot.lexOrder();

        input.put("C", 3);
        input.put("A", 5);

        assertEquals(BigInteger.TWO, snapshot.count());
        assertEquals(lexBefore, snapshot.lexOrder().stream().toList());
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList());
    }

    @Test
    void resultsAreImmutableAndReIterable() {
        BuilderTestHelper.testResultsAreImmutable(builder);
        BuilderTestHelper.testReIterable(builder);
    }

    @Test
    void byRanksIsLazy() {
        BuilderTestHelper.testByRanksIsLazy(builder);
    }

    @Test
    void choiceAndSampleLimits() {
        BuilderTestHelper.testChoiceMayExceedCountAndNullRandomIsRejected(builder);
    }
}