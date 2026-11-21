/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.compound;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

class CompoundConditionalProductBuilderTest {

    @Test
    void compoundEntryPointGeneratesAndCountsMatchingTuples() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");

        var product = cartesianProduct.compoundConditionalProductOf(os, browser, database)
                .when(browser).is("Safari").and(os).is("Windows")
                .or(browser).is("Chrome").and(os).is("Linux")
                .then(database).isNot("PostgreSQL");

        assertEquals(BigInteger.valueOf(16), product.count());
        assertFalse(product.isEmpty());
        assertEquals(16, product.generate().stream().count());
    }

    @Test
    void compoundBuilderCanBeExtendedAfterACompletedRule() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        List<String> target = List.of("x", "y");

        var product = cartesianProduct.compoundConditionalProductOf(a)
                .and(b)
                .and(target)
                .when(a).is("a1").and(b).is("b1")
                .then(target).is("y")
                .when(a).is("a0")
                .then(target).is("x");

        assertEquals(BigInteger.valueOf(5), product.count());
        assertEquals(5, product.generate().stream().count());
    }

    @Test
    void compoundBuilderReportsEmptyProducts() {
        List<String> a = List.of("a");
        List<String> b = List.of("b");
        List<String> target = List.of("x");

        var product = cartesianProduct.compoundConditionalProductOf(a, b, target)
                .when(a).is("a").and(b).is("b")
                .then(target).isNoneOf("x");

        assertEquals(BigInteger.ZERO, product.count());
        assertTrue(product.isEmpty());
        assertTrue(product.generate().stream().toList().isEmpty());
    }

    @Test
    void multiTriggerAndOrGroupsUseAndPrecedenceWithAnyOrder() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");
        var product = cartesianProduct.compoundConditionalProductOf(os, browser, database)
                .when(browser).is("Safari").and(os).is("Windows")
                .or(browser).is("Chrome").and(os).is("Linux")
                .then(database).isNot("PostgreSQL");

        List<List<Object>> expected = List.of(
                List.of("Windows", "Chrome", "PostgreSQL"),
                List.of("Windows", "Chrome", "Oracle"),
                List.of("Windows", "Safari", "Oracle"),
                List.of("Windows", "Firefox", "PostgreSQL"),
                List.of("Windows", "Firefox", "Oracle"),
                List.of("macOS", "Chrome", "PostgreSQL"),
                List.of("macOS", "Chrome", "Oracle"),
                List.of("macOS", "Safari", "PostgreSQL"),
                List.of("macOS", "Safari", "Oracle"),
                List.of("macOS", "Firefox", "PostgreSQL"),
                List.of("macOS", "Firefox", "Oracle"),
                List.of("Linux", "Chrome", "Oracle"),
                List.of("Linux", "Safari", "PostgreSQL"),
                List.of("Linux", "Safari", "Oracle"),
                List.of("Linux", "Firefox", "PostgreSQL"),
                List.of("Linux", "Firefox", "Oracle")
        );
        assertEquals(BigInteger.valueOf(16), product.count());
        assertEquals(new HashSet<>(expected), new HashSet<>(product.generate().stream().toList()));
        assertFalse(product.isEmpty());
    }

    @Test
    void orAlternativesOnOneDimensionAreSupported() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        var product = cartesianProduct.compoundConditionalProductOf(os, browser)
                .when(browser).is("Safari").or(browser).is("Chrome")
                .then(os).is("Windows");

        assertEquals(BigInteger.valueOf(5), product.count());
        List<List<Object>> expected = List.of(
                List.of("Windows", "Chrome"),
                List.of("Windows", "Safari"),
                List.of("Windows", "Firefox"),
                List.of("macOS", "Firefox"),
                List.of("Linux", "Firefox")
        );
        assertEquals(new HashSet<>(expected), new HashSet<>(product.generate().stream().toList()));
    }

    @Test
    void multiTriggerSupportsAnArbitraryLengthConjunction() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        List<String> c = List.of("c0", "c1");
        List<String> d = List.of("d0", "d1");
        List<String> target = List.of("x", "y");

        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, d, target)
                .when(a).is("a1").and(b).is("b1").and(c).is("c1").and(d).is("d1")
                .then(target).is("y");

        assertEquals(BigInteger.valueOf(31), product.count());
        assertEquals(31, product.generate().stream().count());
        assertFalse(product.isEmpty());
    }

    @Test
    void multiTriggerCanMakeTheEntireProductEmpty() {
        List<String> a = List.of("a");
        List<String> b = List.of("b");
        List<String> target = List.of("x", "y");
        var product = cartesianProduct.compoundConditionalProductOf(a, b, target)
                .when(a).is("a").and(b).is("b")
                .then(target).isNoneOf("x", "y");

        assertEquals(BigInteger.ZERO, product.count());
        assertTrue(product.isEmpty());
        assertTrue(product.generate().stream().toList().isEmpty());
    }

    // ── a compound rule whose scope overlaps a chain of pairwise rules ──────────────────────────────
    //
    // A compound rule needs the dimensions of its scope enumerated together. When two of them are already linked
    // by a chain of pairwise rules, that must not force the whole chain to be enumerated jointly with the scope:
    // the chain has no loop of its own.

    /** Distinct list instances 0..n-1, because a dimension is identified by its list instance. */
    private static List<Integer> values(int n) {
        return IntStream.range(0, n).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    /** Every tuple of the given dimension sizes that the predicate accepts, as lists of Integer. */
    private static Set<List<Object>> bruteForce(int[] sizes, Predicate<int[]> valid) {
        Set<List<Object>> result = new HashSet<>();
        int[] t = new int[sizes.length];
        while (true) {
            if (valid.test(t)) result.add(IntStream.of(t).boxed().collect(Collectors.<Object>toList()));
            int p = sizes.length - 1;
            while (p >= 0 && ++t[p] == sizes[p]) t[p--] = 0;
            if (p < 0) return result;
        }
    }

    /** Chain a-b-c-d-e of pairwise rules; the compound rule below spans a, e and f. */
    private static boolean chainHolds(int[] t) {
        return !(t[0] == 0 && t[1] == 0) && !(t[1] == 1 && t[2] == 0)
                && !(t[2] == 2 && t[3] == 2) && !(t[3] == 3 && t[4] == 3);
    }

    @Test
    void compoundRuleOverlappingAPairwiseChainIsNotMergedIntoOneLoop() {
        int n = 12;   // 12^6 exceeds the 1,000,000 combination limit, but the rules contain no loop
        var a = values(n); var b = values(n); var c = values(n);
        var d = values(n); var e = values(n); var f = values(n);

        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, d, e, f)
                .when(a).is(0).then(b).isNot(0)
                .when(b).is(1).then(c).isNot(0)
                .when(c).is(2).then(d).isNot(2)
                .when(d).is(3).then(e).isNot(3)
                .when(a).is(1).and(e).is(1).then(f).isNot(0);

        long expected = 0;
        for (int ia = 0; ia < n; ia++)
            for (int ib = 0; ib < n; ib++)
                for (int ic = 0; ic < n; ic++)
                    for (int id = 0; id < n; id++)
                        for (int ie = 0; ie < n; ie++) {
                            if (!chainHolds(new int[]{ia, ib, ic, id, ie})) continue;
                            for (int jf = 0; jf < n; jf++) {
                                if (!(ia == 1 && ie == 1 && jf == 0)) expected++;
                            }
                        }
        assertEquals(BigInteger.valueOf(expected), product.count());
        assertFalse(product.isEmpty());
    }

    @Test
    void compoundRuleOverlappingAPairwiseChainGeneratesExactlyTheValidTuples() {
        int n = 4;
        var a = values(n); var b = values(n); var c = values(n);
        var d = values(n); var e = values(n); var f = values(n);

        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, d, e, f)
                .when(a).is(0).then(b).isNot(0)
                .when(b).is(1).then(c).isNot(0)
                .when(c).is(2).then(d).isNot(2)
                .when(d).is(3).then(e).isNot(3)
                .when(a).is(1).and(e).is(1).then(f).isNot(0);

        Set<List<Object>> expected = bruteForce(new int[]{n, n, n, n, n, n},
                t -> chainHolds(t) && !(t[0] == 1 && t[4] == 1 && t[5] == 0));
        List<List<Object>> generated = product.generate().stream().toList();

        assertEquals(expected.size(), generated.size());
        assertEquals(expected, new HashSet<>(generated));
        assertEquals(BigInteger.valueOf(expected.size()), product.count());
        assertEquals(expected, product.generate().stream().parallel().collect(Collectors.toSet()));
    }

    @Test
    void compoundRulesSharingADimensionOverAChainAreCounted() {
        int n = 3;
        var a = values(n); var b = values(n); var c = values(n);
        var d = values(n); var e = values(n); var f = values(n);

        // both scopes contain f, and c is linked to the joined scope by two pairwise rules
        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, d, e, f)
                .when(a).is(0).then(b).isNot(0)
                .when(b).is(1).then(c).isNot(0)
                .when(c).is(2).then(d).isNot(2)
                .when(d).is(1).then(e).isNot(1)
                .when(a).is(1).and(e).is(1).then(f).isNot(0)
                .when(b).is(0).and(d).is(0).then(f).isNot(1);

        Set<List<Object>> expected = bruteForce(new int[]{n, n, n, n, n, n},
                t -> !(t[0] == 0 && t[1] == 0) && !(t[1] == 1 && t[2] == 0)
                        && !(t[2] == 2 && t[3] == 2) && !(t[3] == 1 && t[4] == 1)
                        && !(t[0] == 1 && t[4] == 1 && t[5] == 0)
                        && !(t[1] == 0 && t[3] == 0 && t[5] == 1));
        assertEquals(BigInteger.valueOf(expected.size()), product.count());
        assertEquals(expected, new HashSet<>(product.generate().stream().toList()));
    }

    @Test
    void compoundRulesWhoseScopesBothLieOnOneCycleAreCounted() {
        int n = 3;
        var a = values(n); var b = values(n); var c = values(n); var d = values(n);
        var e = values(n); var x = values(n); var y = values(n);

        // the chain a-b-c-d-e joins the scopes {a,d,x} and {b,e,y}, so no single scope can be set apart
        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, d, e, x, y)
                .when(a).is(0).then(b).isNot(0)
                .when(b).is(1).then(c).isNot(0)
                .when(c).is(2).then(d).isNot(2)
                .when(d).is(1).then(e).isNot(1)
                .when(a).is(1).and(d).is(1).then(x).isNot(0)
                .when(b).is(2).and(e).is(2).then(y).isNot(2);

        Set<List<Object>> expected = bruteForce(new int[]{n, n, n, n, n, n, n},
                t -> !(t[0] == 0 && t[1] == 0) && !(t[1] == 1 && t[2] == 0)
                        && !(t[2] == 2 && t[3] == 2) && !(t[3] == 1 && t[4] == 1)
                        && !(t[0] == 1 && t[3] == 1 && t[5] == 0)
                        && !(t[1] == 2 && t[4] == 2 && t[6] == 2));
        assertEquals(BigInteger.valueOf(expected.size()), product.count());
        assertEquals(expected, new HashSet<>(product.generate().stream().toList()));
    }

    @Test
    void compoundRuleOverAChainThatLeavesNoValidTupleIsReportedEmpty() {
        int n = 3;
        var a = values(n); var b = values(n); var c = values(n); var f = values(n);

        // The pairwise rules force b=1 and c=2, and c=2 forces a=0. Then a=0 with c=2 triggers the compound
        // rule, which allows no value of f at all.
        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, f)
                .when(a).isNot(1).then(b).is(1)
                .when(a).is(1).then(b).is(1)
                .when(b).is(1).then(c).is(2)
                .when(b).isNot(1).then(c).is(2)
                .when(c).is(2).then(a).is(0)
                .when(a).is(0).and(c).is(2).then(f).isNoneOf(0, 1, 2);

        Set<List<Object>> expected = bruteForce(new int[]{n, n, n, n},
                t -> (t[0] == 1 || t[1] == 1) && (t[0] != 1 || t[1] == 1)
                        && (t[1] != 1 || t[2] == 2) && (t[1] == 1 || t[2] == 2)
                        && (t[2] != 2 || t[0] == 0)
                        && !(t[0] == 0 && t[2] == 2));
        assertTrue(expected.isEmpty());   // the premise of this test
        assertEquals(BigInteger.ZERO, product.count());
        assertTrue(product.isEmpty());
        assertTrue(product.generate().stream().toList().isEmpty());
    }

    @Test
    void compoundRuleOverAChainCountsTheTuplesThatSurviveEveryRule() {
        int n = 3;
        var a = values(n); var b = values(n); var c = values(n); var f = values(n);

        // The pairwise rules force b=1 and c=2; the compound rule then rules out a=0, leaving a in {1, 2}.
        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, f)
                .when(a).isNot(1).then(b).is(1)
                .when(a).is(1).then(b).is(1)
                .when(b).is(1).then(c).is(2)
                .when(b).isNot(1).then(c).is(2)
                .when(a).is(0).and(c).is(2).then(f).isNoneOf(0, 1, 2);

        Set<List<Object>> expected = bruteForce(new int[]{n, n, n, n},
                t -> (t[0] == 1 || t[1] == 1) && (t[0] != 1 || t[1] == 1)
                        && (t[1] != 1 || t[2] == 2) && (t[1] == 1 || t[2] == 2)
                        && !(t[0] == 0 && t[2] == 2));
        assertEquals(6, expected.size());   // a in {1, 2}, b = 1, c = 2, f free
        assertEquals(BigInteger.valueOf(6), product.count());
        assertEquals(expected, new HashSet<>(product.generate().stream().toList()));
    }

    @Test
    void aRealLoopOfPairwiseRulesStillHasTheCombinationLimit() {
        int n = 40;   // 40^4 = 2,560,000 combinations
        var a = values(n); var b = values(n); var c = values(n); var d = values(n);

        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, d)
                .when(a).is(0).then(b).isNot(0)
                .when(b).is(0).then(c).isNot(0)
                .when(c).is(0).then(d).isNot(0);
        assertThrows(IllegalArgumentException.class,
                () -> product.when(d).is(0).then(a).isNot(0));
    }

}
