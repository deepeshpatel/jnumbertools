/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

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
import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;
import static org.junit.jupiter.api.Assertions.*;

class CompoundConditionalProductBuilderTest {

    @Test
    void compoundEntryPointGeneratesAndCountsMatchingTuples() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");

        var product = cartesianProduct.compoundProductOf(os, browser, database)
                .when(condition(browser).is("Safari").and(condition(os).is("Windows"))
                        .or(condition(browser).is("Chrome").and(condition(os).is("Linux"))))
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

        var product = cartesianProduct.compoundProductOf(a)
                .and(b)
                .and(target)
                .when(condition(a).is("a1").and(condition(b).is("b1")))
                .then(target).is("y")
                .when(condition(a).is("a0"))
                .then(target).is("x");

        assertEquals(BigInteger.valueOf(5), product.count());
        assertEquals(5, product.generate().stream().count());
    }

    @Test
    void compoundBuilderReportsEmptyProducts() {
        List<String> a = List.of("a");
        List<String> b = List.of("b");
        List<String> target = List.of("x");

        var product = cartesianProduct.compoundProductOf(a, b, target)
                .when(condition(a).is("a").and(condition(b).is("b")))
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
        var product = cartesianProduct.compoundProductOf(os, browser, database)
                .when(condition(browser).is("Safari").and(condition(os).is("Windows"))
                        .or(condition(browser).is("Chrome").and(condition(os).is("Linux"))))
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
        var product = cartesianProduct.compoundProductOf(os, browser)
                .when(condition(browser).is("Safari").or(condition(browser).is("Chrome")))
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

        var product = cartesianProduct.compoundProductOf(a, b, c, d, target)
                .when(condition(a).is("a1").and(condition(b).is("b1")).and(condition(c).is("c1")).and(condition(d).is("d1")))
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
        var product = cartesianProduct.compoundProductOf(a, b, target)
                .when(condition(a).is("a").and(condition(b).is("b")))
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

        var product = cartesianProduct.compoundProductOf(a, b, c, d, e, f)
                .when(condition(a).is(0)).then(b).isNot(0)
                .when(condition(b).is(1)).then(c).isNot(0)
                .when(condition(c).is(2)).then(d).isNot(2)
                .when(condition(d).is(3)).then(e).isNot(3)
                .when(condition(a).is(1).and(condition(e).is(1))).then(f).isNot(0);

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

        var product = cartesianProduct.compoundProductOf(a, b, c, d, e, f)
                .when(condition(a).is(0)).then(b).isNot(0)
                .when(condition(b).is(1)).then(c).isNot(0)
                .when(condition(c).is(2)).then(d).isNot(2)
                .when(condition(d).is(3)).then(e).isNot(3)
                .when(condition(a).is(1).and(condition(e).is(1))).then(f).isNot(0);

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
        var product = cartesianProduct.compoundProductOf(a, b, c, d, e, f)
                .when(condition(a).is(0)).then(b).isNot(0)
                .when(condition(b).is(1)).then(c).isNot(0)
                .when(condition(c).is(2)).then(d).isNot(2)
                .when(condition(d).is(1)).then(e).isNot(1)
                .when(condition(a).is(1).and(condition(e).is(1))).then(f).isNot(0)
                .when(condition(b).is(0).and(condition(d).is(0))).then(f).isNot(1);

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
        var product = cartesianProduct.compoundProductOf(a, b, c, d, e, x, y)
                .when(condition(a).is(0)).then(b).isNot(0)
                .when(condition(b).is(1)).then(c).isNot(0)
                .when(condition(c).is(2)).then(d).isNot(2)
                .when(condition(d).is(1)).then(e).isNot(1)
                .when(condition(a).is(1).and(condition(d).is(1))).then(x).isNot(0)
                .when(condition(b).is(2).and(condition(e).is(2))).then(y).isNot(2);

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
        var product = cartesianProduct.compoundProductOf(a, b, c, f)
                .when(condition(a).isNot(1)).then(b).is(1)
                .when(condition(a).is(1)).then(b).is(1)
                .when(condition(b).is(1)).then(c).is(2)
                .when(condition(b).isNot(1)).then(c).is(2)
                .when(condition(c).is(2)).then(a).is(0)
                .when(condition(a).is(0).and(condition(c).is(2))).then(f).isNoneOf(0, 1, 2);

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
        var product = cartesianProduct.compoundProductOf(a, b, c, f)
                .when(condition(a).isNot(1)).then(b).is(1)
                .when(condition(a).is(1)).then(b).is(1)
                .when(condition(b).is(1)).then(c).is(2)
                .when(condition(b).isNot(1)).then(c).is(2)
                .when(condition(a).is(0).and(condition(c).is(2))).then(f).isNoneOf(0, 1, 2);

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

        var product = cartesianProduct.compoundProductOf(a, b, c, d)
                .when(condition(a).is(0)).then(b).isNot(0)
                .when(condition(b).is(0)).then(c).isNot(0)
                .when(condition(c).is(0)).then(d).isNot(0);
        assertThrows(IllegalArgumentException.class,
                () -> product.when(condition(d).is(0)).then(a).isNot(0));
    }

    // ── Condition: composed trigger expressions with real nesting ──────────────────────────────────

    @Test
    void conditionTreeRepresentsAnOrOfConjunctions() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");

        var tree = cartesianProduct.compoundProductOf(os, browser, database)
                .when(
                        condition(browser).is("Safari").and(condition(os).is("Windows"))
                                .or(condition(browser).is("Chrome").and(condition(os).is("Linux")))
                )
                .then(database).isNot("PostgreSQL");

        assertEquals(BigInteger.valueOf(16), tree.count());
        assertEquals(16, tree.generate().stream().count());
    }

    @Test
    void conditionTreeExpressesAndOfOr() {
        // browser = Safari AND (os = Windows OR (os = Linux AND network = 4G))
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");
        List<String> network = List.of("WiFi", "4G");

        var product = cartesianProduct.compoundProductOf(os, browser, database)
                .and(network)
                .when(
                        condition(browser).is("Safari")
                                .and(
                                        condition(os).is("Windows")
                                                .or(condition(os).is("Linux").and(condition(network).is("4G")))
                                )
                )
                .then(database).isNot("PostgreSQL");

        long expected = 0;
        for (String o : os) for (String b : browser) for (String d : database) for (String n : network) {
            boolean triggerMatches = b.equals("Safari") && (o.equals("Windows") || (o.equals("Linux") && n.equals("4G")));
            boolean ok = !triggerMatches || !d.equals("PostgreSQL");
            if (ok) expected++;
        }
        assertEquals(BigInteger.valueOf(expected), product.count());
        assertEquals(expected, product.generate().stream().count());
    }

    @Test
    void conditionAndDistributesOverOr() {
        // (browser = Safari OR browser = Chrome) AND (os = Windows OR os = Linux)
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");

        Condition left = condition(browser).is("Safari").or(condition(browser).is("Chrome"));
        Condition right = condition(os).is("Windows").or(condition(os).is("Linux"));

        var product = cartesianProduct.compoundProductOf(os, browser, database)
                .when(left.and(right))
                .then(database).isNot("PostgreSQL");

        long expected = 0;
        for (String o : os) for (String b : browser) for (String d : database) {
            boolean triggerMatches = (b.equals("Safari") || b.equals("Chrome")) && (o.equals("Windows") || o.equals("Linux"));
            boolean ok = !triggerMatches || !d.equals("PostgreSQL");
            if (ok) expected++;
        }
        assertEquals(BigInteger.valueOf(expected), product.count());
    }

    @Test
    void conditionRejectsADimensionNotPartOfTheProduct() {
        List<String> os = List.of("Windows", "macOS");
        List<String> browser = List.of("Chrome", "Safari");
        List<String> database = List.of("PostgreSQL", "Oracle");
        List<String> unrelated = List.of("x", "y");

        var product = cartesianProduct.compoundProductOf(os, browser, database);
        assertThrows(IllegalArgumentException.class,
                () -> product.when(condition(unrelated).is("x")).then(database).isNot("PostgreSQL"));
    }

    @Test
    void conditionRejectsAnUnknownValue() {
        List<String> os = List.of("Windows", "macOS");
        List<String> browser = List.of("Chrome", "Safari");
        List<String> database = List.of("PostgreSQL", "Oracle");

        var product = cartesianProduct.compoundProductOf(os, browser, database);
        assertThrows(IllegalArgumentException.class,
                () -> product.when(condition(os).is("DoesNotExist")).then(database).isNot("PostgreSQL"));
    }

    @Test
    void conditionChecksClauseLimitBeforeBuildingCombinedGroups() {
        List<String> browser = List.of("Chrome", "Safari");

        // Doubling creates the requested number of clauses in logarithmic time.
        Condition wide = condition(browser).is("Safari");
        for (int clauses = 1; clauses < Condition.MAX_CLAUSES; clauses *= 2) {
            wide = wide.or(wide);
        }

        // 4096 x 4096 would otherwise attempt to materialize over 16 million groups
        // before discovering that the configured limit has been exceeded.
        Condition finalWide = wide;
        assertThrows(IllegalArgumentException.class, () -> finalWide.and(finalWide));

        Condition halfWide = condition(browser).is("Chrome");
        for (int clauses = 1; clauses < Condition.MAX_CLAUSES / 2; clauses *= 2) {
            halfWide = halfWide.or(halfWide);
        }
        Condition atLimit = halfWide.or(halfWide);
        assertEquals(Condition.MAX_CLAUSES, atLimit.groups().size());

        assertThrows(IllegalArgumentException.class,
                () -> atLimit.or(condition(browser).is("Safari")));
    }

    @Test
    void compoundBuilderExposesLexicographicRanksChoiceAndSampling() {
        List<Integer> a = List.of(0, 1);
        List<Integer> b = List.of(0, 1);
        List<Integer> target = List.of(0, 1);

        var product = cartesianProduct.compoundProductOf(a, b, target)
                .when(condition(a).is(0).and(condition(b).is(0)))
                .then(target).is(0);

        List<List<Object>> expected = List.of(
                List.of(0, 0, 0),
                List.of(0, 1, 0), List.of(0, 1, 1),
                List.of(1, 0, 0), List.of(1, 0, 1),
                List.of(1, 1, 0), List.of(1, 1, 1));

        assertEquals(expected, product.lexOrder().stream().toList());
        assertEquals(List.of(expected.get(0), expected.get(3), expected.get(6)),
                product.byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(3), BigInteger.valueOf(6))).stream().toList());
        assertEquals(product.lexOrder().stream().toList(),
                product.lexOrder(LexOrderStrategy.DP).stream().toList());
        assertEquals(3, product.lexOrderMth(BigInteger.valueOf(3), BigInteger.ZERO).stream().count());
        assertEquals(4, product.choice(4, new java.util.Random(42)).stream().count());
        List<List<Object>> sample = product.sample(4, new java.util.Random(42)).stream().toList();
        assertEquals(4, sample.size());
        assertEquals(4, new HashSet<>(sample).size());
    }

}
