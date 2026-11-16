/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Focused contract and differential tests for the conditional-product lexicographic generators.
 * The oracle deliberately enumerates the small Cartesian product independently of the library.
 */
class ConditionalProductGeneratorTest {

    private static final List<LexOrderStrategy> EXPLICIT_STRATEGIES =
            List.of(LexOrderStrategy.PRUNED_BITSET, LexOrderStrategy.PRUNED_DFS, LexOrderStrategy.DP);

    @Test
    void allStrategiesAgreeForUnconstrainedEmptyAndDeadEndProducts() {
        List<Integer> a = List.of(0, 1);
        List<Integer> b = List.of(10, 11, 12);
        List<Integer> c = List.of(20, 21);

        var unconstrained = cartesianProduct.conditionalProductOf(a).and(b).and(c);
        assertAllStrategies(unconstrained, List.of(
                List.of(0, 10, 20), List.of(0, 10, 21),
                List.of(0, 11, 20), List.of(0, 11, 21),
                List.of(0, 12, 20), List.of(0, 12, 21),
                List.of(1, 10, 20), List.of(1, 10, 21),
                List.of(1, 11, 20), List.of(1, 11, 21),
                List.of(1, 12, 20), List.of(1, 12, 21)));

        var empty = cartesianProduct.conditionalProductOf(a).and(List.<Integer>of()).and(c);
        assertAllStrategies(empty, List.of());

        // Every prefix selected from A is a dead end because it permits no value from B.
        var deadEnd = cartesianProduct.conditionalProductOf(a).and(b).and(c)
                .when(a).isOneOf(a).then(b).isNoneOf(b);
        assertAllStrategies(deadEnd, List.of());
    }

    @Test
    void anyOrderProducesEveryValidTupleOnceWithoutAnOrderGuarantee() {
        List<Integer> first = java.util.stream.IntStream.range(0, 18).boxed().toList();
        List<Integer> second = java.util.stream.IntStream.range(0, 7).boxed().toList();
        List<Integer> third = java.util.stream.IntStream.range(0, 5).boxed().toList();
        var builder = cartesianProduct.conditionalProductOf(first).and(second).and(third)
                .when(first).isOneOf(List.of(2, 5, 8, 13, 17))
                .then(second).isOneOf(List.of(0, 3, 6))
                .when(second).is(3).then(third).isNot(4);

        var generator = builder.anyOrder();
        List<List<Object>> expected = builder.lexOrder().stream().toList();
        List<List<Object>> actual = generator.stream().toList();

        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
        assertEquals(expected.size(), actual.size(), "anyOrder must not emit duplicate tuples");
        assertEquals(actual, generator.stream().toList(), "the generator must be re-iterable");
        assertEquals(BigInteger.valueOf(expected.size()), builder.count());

        var spliterator = generator.spliterator();
        assertTrue((spliterator.characteristics() & java.util.Spliterator.ORDERED) == 0,
                "anyOrder must not advertise encounter order");
        var split = spliterator.trySplit();
        assertTrue(split != null, "a sufficiently large first dimension should split");
        List<List<Object>> splitOutput = new ArrayList<>();
        split.forEachRemaining(splitOutput::add);
        spliterator.forEachRemaining(splitOutput::add);
        assertEquals(new HashSet<>(expected), new HashSet<>(splitOutput));
        assertEquals(expected.size(), splitOutput.size(), "split partitions must not overlap");

        // A singleton first dimension must not prevent splitting at a deeper dimension.
        List<Integer> singleton = List.of(99);
        List<Integer> wide = java.util.stream.IntStream.range(0, 31).boxed().toList();
        List<Integer> tail = java.util.stream.IntStream.range(0, 5).boxed().toList();
        var narrowRoot = cartesianProduct.conditionalProductOf(singleton).and(wide).and(tail)
                .when(wide).isOneOf(List.of(2, 7, 13, 19, 29))
                .then(tail).isNot(3);
        var narrowGenerator = narrowRoot.anyOrder();
        List<List<Object>> narrowExpected = narrowRoot.lexOrder().stream().toList();
        var narrowSpliterator = narrowGenerator.spliterator();
        var deeperSplit = narrowSpliterator.trySplit();
        assertTrue(deeperSplit != null, "splitting must continue below a singleton first dimension");
        List<List<Object>> deeperOutput = new ArrayList<>();
        deeperSplit.forEachRemaining(deeperOutput::add);
        narrowSpliterator.forEachRemaining(deeperOutput::add);
        assertEquals(new HashSet<>(narrowExpected), new HashSet<>(deeperOutput));
        assertEquals(narrowExpected.size(), deeperOutput.size());

        Set<List<Object>> parallelOutput = new HashSet<>(narrowGenerator.stream().parallel().toList());
        assertEquals(new HashSet<>(narrowExpected), parallelOutput,
                "parallel anyOrder traversal must preserve the complete tuple set");

        var empty = cartesianProduct.conditionalProductOf(first).and(List.<Integer>of());
        assertTrue(empty.anyOrder().stream().toList().isEmpty());
    }

    @Test
    void everyStrategyHonoursIteratorAndReiterationContracts() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browsers = List.of("Chrome", "Safari", "Firefox");
        List<String> networks = List.of("WiFi", "4G");
        var builder = cartesianProduct.conditionalProductOf(os).and(browsers).and(networks)
                .when(browsers).is("Safari").then(os).isOneOf("macOS")
                .when(networks).is("4G").then(browsers).isNot("Chrome");

        for (LexOrderStrategy strategy : EXPLICIT_STRATEGIES) {
            var iterable = builder.lexOrder(strategy);
            List<List<Object>> firstPass = iterable.stream().toList();
            assertFalse(firstPass.isEmpty(), strategy.toString());
            assertEquals(firstPass, iterable.stream().toList(), strategy + " must be re-iterable");

            var iterator = iterable.iterator();
            List<List<Object>> iterated = new ArrayList<>();
            while (iterator.hasNext()) {
                assertTrue(iterator.hasNext(), strategy + " hasNext must be idempotent");
                iterated.add(iterator.next());
            }
            assertEquals(firstPass, iterated, strategy.toString());
            assertFalse(iterator.hasNext());
            assertThrows(java.util.NoSuchElementException.class, iterator::next);
        }

        var adaptive = builder.lexOrder(LexOrderStrategy.ADAPTIVE, 100);
        assertEquals(builder.lexOrder().stream().toList(), adaptive.stream().toList());
        assertEquals(builder.lexOrder().stream().toList(), adaptive.stream().toList());
    }


    @Test
    void allExplicitStrategiesPreserveLexicographicOrderInParallelStreams() {
        List<Integer> first = java.util.stream.IntStream.range(0, 32).boxed().toList();
        List<Integer> second = java.util.stream.IntStream.range(0, 7).boxed().toList();
        List<Integer> third = java.util.stream.IntStream.range(0, 5).boxed().toList();
        var builder = cartesianProduct.conditionalProductOf(first).and(second).and(third)
                .when(first).isOneOf(List.of(3, 7, 11, 19, 23, 29))
                .then(second).isOneOf(List.of(1, 3, 5))
                .when(second).is(3).then(third).isNot(2);

        for (LexOrderStrategy strategy : EXPLICIT_STRATEGIES) {
            var iterable = builder.lexOrder(strategy);
            List<List<Object>> sequential = iterable.stream().toList();
            List<List<Object>> parallel = iterable.stream().parallel().toList();
            assertEquals(sequential, parallel, strategy + " parallel encounter order");
            assertEquals(new HashSet<>(sequential), new HashSet<>(parallel),
                    strategy + " parallel tuple set");
        }
    }

    @Test
    void unorderedParallelStreamsPreserveTheTupleSetForEveryStrategy() {
        List<Integer> first = java.util.stream.IntStream.range(0, 24).boxed().toList();
        List<Integer> second = java.util.stream.IntStream.range(0, 9).boxed().toList();
        List<Integer> third = java.util.stream.IntStream.range(0, 6).boxed().toList();
        var builder = cartesianProduct.conditionalProductOf(first).and(second).and(third)
                .when(first).isOneOf(List.of(2, 5, 8, 13, 17, 21))
                .then(second).isOneOf(List.of(0, 3, 6, 8))
                .when(second).is(3).then(third).isNot(4);

        Set<List<Object>> expected = new HashSet<>(builder.lexOrder().stream().toList());

        for (LexOrderStrategy strategy : EXPLICIT_STRATEGIES) {
            Set<List<Object>> actual = new HashSet<>(builder.lexOrder(strategy)
                    .stream().unordered().parallel().toList());
            assertEquals(expected, actual, strategy + " unordered parallel tuple set");
        }

        Set<List<Object>> adaptive = new HashSet<>(builder
                .lexOrder(LexOrderStrategy.ADAPTIVE, 100)
                .stream().unordered().parallel().toList());
        assertEquals(expected, adaptive, "ADAPTIVE unordered parallel tuple set");
    }

    @Test
    void seededRandomRuleSetsMatchBruteForceForEveryStrategy() {
        Random random = new Random(0x5EED_C0DEL);
        for (int round = 0; round < 300; round++) {
            int caseNumber = round;
            RandomCase testCase = randomCase(random);
            List<List<Object>> expected = bruteForce(testCase);
            for (LexOrderStrategy strategy : EXPLICIT_STRATEGIES) {
                assertEquals(expected, testCase.builder().lexOrder(strategy).stream().toList(),
                        () -> "round=" + caseNumber + ", strategy=" + strategy);
            }
            assertEquals(expected, testCase.builder()
                    .lexOrder(LexOrderStrategy.ADAPTIVE, 500).stream().toList(),
                    () -> "round=" + caseNumber + ", strategy=ADAPTIVE");
        }
    }

    private static void assertAllStrategies(ConditionalProductBuilder builder, List<List<Object>> expected) {
        for (LexOrderStrategy strategy : EXPLICIT_STRATEGIES) {
            assertEquals(expected, builder.lexOrder(strategy).stream().toList(), strategy.toString());
        }
        assertEquals(expected, builder.lexOrder(LexOrderStrategy.ADAPTIVE, 500).stream().toList());
    }

    private record Rule(int triggerDimension, Set<Integer> triggers,
                        int targetDimension, Set<Integer> allowed) { }

    private record RandomCase(ConditionalProductBuilder builder, List<List<Integer>> dimensions,
                             List<Rule> rules) { }

    private static RandomCase randomCase(Random random) {
        int dimensionCount = 1 + random.nextInt(5);
        List<List<Integer>> dimensions = new ArrayList<>();
        for (int d = 0; d < dimensionCount; d++) {
            int size = random.nextInt(12) == 0 ? 0 : 1 + random.nextInt(4);
            List<Integer> values = new ArrayList<>();
            for (int i = 0; i < size; i++) values.add(d * 100 + i);
            dimensions.add(List.copyOf(values));
        }

        ConditionalProductBuilder builder = cartesianProduct.conditionalProductOf(dimensions.get(0));
        for (int d = 1; d < dimensionCount; d++) builder = builder.and(dimensions.get(d));

        List<Rule> rules = new ArrayList<>();
        for (int a = 0; a < dimensionCount; a++) {
            for (int b = a + 1; b < dimensionCount; b++) {
                if (random.nextInt(100) >= 45 || dimensions.get(a).isEmpty()
                        || dimensions.get(b).isEmpty()) continue;
                int triggerDimension = random.nextBoolean() ? a : b;
                int targetDimension = triggerDimension == a ? b : a;
                Set<Integer> triggers = randomSubset(dimensions.get(triggerDimension).size(), random);
                Set<Integer> allowed = randomSubset(dimensions.get(targetDimension).size(), random);
                builder = addRule(builder, dimensions, triggerDimension, triggers, targetDimension, allowed);
                rules.add(new Rule(triggerDimension, triggers, targetDimension, allowed));
                if (random.nextInt(4) == 0) {
                    Set<Integer> secondTriggers = randomSubset(dimensions.get(triggerDimension).size(), random);
                    Set<Integer> secondAllowed = randomSubset(dimensions.get(targetDimension).size(), random);
                    builder = addRule(builder, dimensions, triggerDimension, secondTriggers,
                            targetDimension, secondAllowed);
                    rules.add(new Rule(triggerDimension, secondTriggers, targetDimension, secondAllowed));
                }
            }
        }
        return new RandomCase(builder, dimensions, rules);
    }

    private static ConditionalProductBuilder addRule(ConditionalProductBuilder builder,
            List<List<Integer>> dimensions, int triggerDimension, Set<Integer> triggers,
            int targetDimension, Set<Integer> allowed) {
        List<Integer> triggerValues = valuesAt(dimensions.get(triggerDimension), triggers);
        List<Integer> allowedValues = valuesAt(dimensions.get(targetDimension), allowed);

        ConditionalProductBuilder.Trigger trigger = triggers.isEmpty()
                ? builder.when(dimensions.get(triggerDimension)).isNoneOf(dimensions.get(triggerDimension))
                : builder.when(dimensions.get(triggerDimension)).isOneOf(triggerValues);
        ConditionalProductBuilder.ThenDimension then = trigger.then(dimensions.get(targetDimension));
        return allowed.isEmpty()
                ? then.isNoneOf(dimensions.get(targetDimension))
                : then.isOneOf(allowedValues);
    }

    private static List<Integer> valuesAt(List<Integer> values, Set<Integer> indexes) {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) if (indexes.contains(i)) selected.add(values.get(i));
        return selected;
    }

    private static Set<Integer> randomSubset(int size, Random random) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < size; i++) if (random.nextBoolean()) result.add(i);
        return result;
    }

    private static List<List<Object>> bruteForce(RandomCase testCase) {
        List<List<Integer>> dimensions = testCase.dimensions();
        if (dimensions.stream().anyMatch(List::isEmpty)) return List.of();

        List<List<Object>> result = new ArrayList<>();
        int[] indexes = new int[dimensions.size()];
        while (true) {
            if (valid(indexes, testCase.rules())) {
                List<Object> tuple = new ArrayList<>(indexes.length);
                for (int d = 0; d < indexes.length; d++) tuple.add(dimensions.get(d).get(indexes[d]));
                result.add(Collections.unmodifiableList(tuple));
            }

            int d = indexes.length - 1;
            while (d >= 0 && ++indexes[d] == dimensions.get(d).size()) indexes[d--] = 0;
            if (d < 0) return result;
        }
    }

    private static boolean valid(int[] indexes, List<Rule> rules) {
        for (Rule rule : rules) {
            if (rule.triggers().contains(indexes[rule.triggerDimension()])
                    && !rule.allowed().contains(indexes[rule.targetDimension()])) return false;
        }
        return true;
    }
}
