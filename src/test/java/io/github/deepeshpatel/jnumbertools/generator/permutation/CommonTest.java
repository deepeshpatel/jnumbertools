/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.createMap;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cross-validation that the three permutation engines (unique, multiset with all
 * frequencies = 1, and nPk with k = n) produce identical output for the special
 * case where every element appears exactly once and all of them are used.
 *
 * <p>Each engine is exercised both via {@code lexOrder()} and {@code lexOrderMth()}
 * to ensure the mth-iterator and the next-iterator agree with each other and with
 * the other engines.
 */
@DisplayName("Cross-engine equivalence: unique vs multiset(freq=1) vs nPk(k=n)")
class CommonTest {

    /**
     * For sizes 1..5 and increments 1..4, verify that all three generators produce
     * identical {@code lexOrderMth(increment, 0)} sequences.
     */
    @Test
    @DisplayName("lexOrderMth output matches across unique, multiset(freq=1), nPk(k=n)")
    void mthOutputMatchesAcrossEngines() {
        for (int size = 1; size <= 5; size++) {
            for (int increment = 1; increment <= 4; increment++) {
                var unique    = uniqueMth(size, increment);
                var multiset  = multisetMth(size, increment);
                var kPerm     = kPermMth(size, increment);
                assertEquals(unique, multiset,
                        "unique vs multiset mismatch (size=" + size + ", increment=" + increment + ")");
                assertEquals(unique, kPerm,
                        "unique vs nPk mismatch (size=" + size + ", increment=" + increment + ")");
            }
        }
    }

    /**
     * For sizes 1..5, verify that all three generators produce identical
     * {@code lexOrder()} (full-sequence) output.
     */
    @Test
    @DisplayName("lexOrder output matches across unique, multiset(freq=1), nPk(k=n)")
    void lexOrderOutputMatchesAcrossEngines() {
        for (int size = 1; size <= 5; size++) {
            var unique    = uniqueLex(size);
            var multiset  = multisetLex(size);
            var kPerm     = kPermLex(size);
            assertEquals(unique, multiset,
                    "unique vs multiset mismatch (size=" + size + ")");
            assertEquals(unique, kPerm,
                    "unique vs nPk mismatch (size=" + size + ")");
        }
    }

    /**
     * For size=0 all three engines must yield exactly one empty permutation,
     * not zero results.
     */
    @Test
    @DisplayName("size=0: all three engines yield the single empty permutation")
    void zeroSizeYieldsSingleEmptyPermutation() {
        assertEquals(List.of(List.of()), uniqueLex(0));
        assertEquals(List.of(List.of()), multisetLex(0));
        assertEquals(List.of(List.of()), kPermLex(0));
    }

    /**
     * For lexOrder vs lexOrderMth(1, 0) on the same engine: the mth-iterator with
     * step 1 must be identical to next-iterator. This is the cheapest internal
     * consistency check we have.
     */
    @Test
    @DisplayName("lexOrderMth(1,0) == lexOrder() for every engine")
    void mthIncrementOneEqualsNext() {
        for (int size = 1; size <= 4; size++) {
            assertEquals(uniqueLex(size),   uniqueMth(size, 1));
            assertEquals(multisetLex(size), multisetMth(size, 1));
            assertEquals(kPermLex(size),    kPermMth(size, 1));
        }
    }

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] cross-engine equivalence holds for size up to 7")
    void stressTesting() {
        for (int size = 0; size <= 7; size++) {
            var unique = uniqueLex(size);
            assertEquals(unique, multisetLex(size), "size=" + size);
            assertEquals(unique, kPermLex(size),    "size=" + size);
            for (int increment = 1; increment <= 5; increment++) {
                var uniqueMth = uniqueMth(size, increment);
                assertEquals(uniqueMth, multisetMth(size, increment),
                        "size=" + size + " increment=" + increment);
                assertEquals(uniqueMth, kPermMth(size, increment),
                        "size=" + size + " increment=" + increment);
            }
        }
    }

    // ---------- helpers ----------

    private static List<Integer> rangeBoxed(int size) {
        return IntStream.range(0, size).boxed().collect(Collectors.toList());
    }

    private static int[] onesOfLength(int size) {
        int[] f = new int[size];
        Arrays.fill(f, 1);
        return f;
    }

    private static List<?> uniqueLex(int size) {
        return permutation.unique(size).lexOrder().stream().toList();
    }

    private static List<?> uniqueMth(int size, int increment) {
        return permutation.unique(size).lexOrderMth(increment, 0).stream().toList();
    }

    private static List<?> multisetLex(int size) {
        return permutation.multiset(createMap(rangeBoxed(size), onesOfLength(size)))
                .lexOrder().stream().toList();
    }

    private static List<?> multisetMth(int size, int increment) {
        return permutation.multiset(createMap(rangeBoxed(size), onesOfLength(size)))
                .lexOrderMth(increment, 0).stream().toList();
    }

    private static List<?> kPermLex(int size) {
        return permutation.nPk(size, size).lexOrder().stream().toList();
    }

    private static List<?> kPermMth(int size, int increment) {
        return permutation.nPk(size, size).lexOrderMth(increment, 0).stream().toList();
    }
}