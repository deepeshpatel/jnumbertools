/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

class LambdaConditionalProductBuilderTest {

    @Test
    void twoDimensionLambdaRuleGeneratesAndCountsMatchingTuples() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox", "Internet Explorer");

        var product = cartesianProduct.withLambda(os, browser)
                .when(os, browser).matches((o, b) -> {
                    if (o.equals("Linux")) return b.equals("Firefox") || b.equals("Safari");
                    if (o.equals("Windows")) return !b.equals("Internet Explorer");
                    return true;
                });

        Set<List<Object>> expected = new HashSet<>();
        for (String o : os) {
            for (String b : browser) {
                boolean ok = o.equals("Linux") ? (b.equals("Firefox") || b.equals("Safari"))
                        : o.equals("Windows") ? !b.equals("Internet Explorer") : true;
                if (ok) expected.add(List.of(o, b));
            }
        }

        assertEquals(BigInteger.valueOf(expected.size()), product.count());
        assertFalse(product.isEmpty());
        assertEquals(expected, new HashSet<>(product.generate().stream().toList()));
    }

    @Test
    void severalTwoDimensionRulesSharingOneDimensionDoNotFormABlock() {
        // All four rules are triggered by the same "trigger" dimension; this must stay pairwise
        // (the same optimization CompoundConditionalProductBuilder applies for a single-dimension
        // Condition) rather than merging trigger+a+b+c+d into one joint-assignment block.
        List<String> trigger = List.of("X", "Y");

        // Four distinct dimensions (distinct list instances; dimension identity is by instance, not
        // content), each wide enough that merging all four plus the trigger into one joint-assignment
        // block would provably exceed MAX_LOOP_COMBINATIONS (1,000,000): 2 * 60^4 = 25,920,000.
        List<Integer> wideA = wideDimension(60);
        List<Integer> wideB = wideDimension(60);
        List<Integer> wideC = wideDimension(60);
        List<Integer> wideD = wideDimension(60);

        var product = cartesianProduct.withLambda(trigger, wideA, wideB, wideC, wideD)
                .when(trigger, wideA).matches((t, v) -> !(t.equals("X") && v % 2 == 0))
                .andWhen(trigger, wideB).matches((t, v) -> !(t.equals("Y") && v % 3 == 0))
                .andWhen(trigger, wideC).matches((t, v) -> !(t.equals("X") && v % 5 == 0))
                .andWhen(trigger, wideD).matches((t, v) -> !(t.equals("Y") && v % 7 == 0));

        // If this had been forced into one block, it would have thrown IllegalArgumentException
        // instead of returning a result.
        assertFalse(product.isEmpty());
        assertTrue(product.count().compareTo(BigInteger.ZERO) > 0);
    }

    private static List<Integer> wideDimension(int size) {
        List<Integer> values = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) values.add(i);
        return values;
    }

    @Test
    void uncappedObjectArrayFormSupportsThreeOrMoreDimensions() {
        List<Integer> cores = List.of(1, 2, 4, 8);
        List<Integer> memory = List.of(2, 4, 8, 16);
        List<Integer> budget = List.of(8, 16, 32);

        var product = cartesianProduct.withLambda(cores, memory, budget)
                .when(cores, memory, budget).matches(values -> {
                    int c = (Integer) values[0];
                    int m = (Integer) values[1];
                    int bud = (Integer) values[2];
                    return c * m <= bud;
                });

        long expected = 0;
        for (int c : cores) for (int m : memory) for (int bud : budget) if (c * m <= bud) expected++;

        assertEquals(BigInteger.valueOf(expected), product.count());
        assertEquals(expected, product.generate().stream().count());
    }

    @Test
    void predicateIsEvaluatedOnlyAtCompileTimeNeverDuringGenerationCountingOrEmptinessChecks() {
        List<String> os = List.of("Windows", "Linux");
        List<String> browser = List.of("Chrome", "Firefox", "Internet Explorer");

        AtomicInteger calls = new AtomicInteger();
        var product = cartesianProduct.withLambda(os, browser)
                .when(os, browser).matches((o, b) -> {
                    calls.incrementAndGet();
                    return !(o.equals("Linux") && b.equals("Internet Explorer"));
                });

        int callsAfterCompile = calls.get();
        assertTrue(callsAfterCompile > 0);

        product.count();
        product.isEmpty();
        product.generate().stream().count();
        product.generate().stream().count(); // re-iterate the same generator

        assertEquals(callsAfterCompile, calls.get(),
                "the predicate must not be invoked again after the rule is compiled");
    }

    @Test
    void anAlwaysTruePredicateAddsNoConstraint() {
        List<String> a = List.of("a0", "a1", "a2");
        List<String> b = List.of("b0", "b1");

        var product = cartesianProduct.withLambda(a, b)
                .when(a, b).matches((x, y) -> true);

        assertEquals(BigInteger.valueOf(a.size() * (long) b.size()), product.count());
    }

    @Test
    void anAlwaysFalsePredicateEmptiesTheProduct() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");

        var product = cartesianProduct.withLambda(a, b)
                .when(a, b).matches((x, y) -> false);

        assertTrue(product.isEmpty());
        assertEquals(BigInteger.ZERO, product.count());
        assertTrue(product.generate().stream().toList().isEmpty());
    }

    @Test
    void whenRejectsAnEmptyScope() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        var builder = cartesianProduct.withLambda(a, b);
        assertThrows(IllegalArgumentException.class, () -> builder.when().matches(v -> true));
    }

    @Test
    void whenRejectsADuplicatedDimensionInScope() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        var builder = cartesianProduct.withLambda(a, b);
        assertThrows(IllegalArgumentException.class, () -> builder.when(a, a).matches((x, y) -> true));
    }

    @Test
    void whenRejectsADimensionThatIsNotPartOfThisProduct() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        List<String> notADimension = List.of("z0", "z1");
        var builder = cartesianProduct.withLambda(a, b);
        assertThrows(IllegalArgumentException.class, () -> builder.when(a, notADimension).matches((x, y) -> true));
    }

    @Test
    void andCanAppendDimensionsBeforeAnyRuleIsAdded() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        List<String> c = List.of("c0", "c1", "c2");

        var product = cartesianProduct.withLambda(a)
                .and(b)
                .and(c)
                .when(a, c).matches((x, z) -> !(x.equals("a0") && z.equals("c2")));

        long expected = 0;
        for (String x : a) for (String ignored : b) for (String z : c) {
            if (!(x.equals("a0") && z.equals("c2"))) expected++;
        }
        assertEquals(BigInteger.valueOf(expected), product.count());
    }
}
