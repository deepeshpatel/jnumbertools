package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 1 (tests first): one test per ValidationRules.md rule that applies to the public API of the
 * SIMPLE CARTESIAN PRODUCT builder, reached only through {@code cartesianProduct.simpleProductOf(..).and(..)}.
 * Each test states what the CURRENT code is predicted to do, so the failures can be confirmed before any
 * production change.
 *
 * Conventions follow the already-migrated builders (see ConstrainedProductValidationRulesTest):
 *   null list / null element / null Random -> NullPointerException
 *   null ranks                              -> IllegalArgumentException
 *   everything else invalid                 -> IllegalArgumentException
 *
 * Deliberately NOT covered: a product whose ONLY dimension is empty, simpleProductOf([]). The table says
 * "any dimension is empty -> count 0" but the code returns count 1 / [[]]; which one is intended is an
 * open question in ValidationRules.md, so no test pins either answer. Expected counts are computed
 * independently (plain products of the dimension sizes), never through the builder's own isEmpty().
 */
class SimpleProductValidationRulesTest {

    private static final List<String> AB = List.of("A", "B");
    private static final List<Integer> ONE_TWO_THREE = List.of(1, 2, 3);

    private static BigInteger bi(long v) {
        return BigInteger.valueOf(v);
    }

    private static List<Integer> ints(int n) {
        return IntStream.range(0, n).boxed().toList();
    }

    private static List<Object> drain(Iterable<?> iterable) {
        List<Object> out = new ArrayList<>();
        for (Object o : iterable) out.add(o);
        return out;
    }

    private static SimpleProductBuilder product(int... dimensionSizes) {
        SimpleProductBuilder b = cartesianProduct.simpleProductOf(ints(dimensionSizes[0]));
        for (int i = 1; i < dimensionSizes.length; i++) b = b.and(ints(dimensionSizes[i]));
        return b;
    }

    private static BigInteger productOf(int... sizes) {
        BigInteger p = BigInteger.ONE;
        for (int s : sizes) p = p.multiply(bi(s));
        return p;
    }

    /** A product that is empty for sure: its second dimension is empty. */
    private static SimpleProductBuilder emptyProduct() {
        SimpleProductBuilder p = cartesianProduct.simpleProductOf(AB).and(Collections.emptyList());
        assertEquals(BigInteger.ZERO, p.count());
        return p;
    }

    // ------------------------------------------------------------------------------------
    // GENERAL RULE 1 / 7 - null input and null elements
    // ------------------------------------------------------------------------------------

    /** Regression guard: null lists are rejected. */
    @Test
    void nullListsAreRejected() {
        List<String> nothing = null;
        assertThrows(NullPointerException.class, () -> cartesianProduct.simpleProductOf(nothing));
        assertThrows(NullPointerException.class, () -> cartesianProduct.simpleProductOf(AB).and(nothing));
        // also when the product is already empty
        var empty = emptyProduct();
        assertThrows(NullPointerException.class, () -> empty.and(nothing));
    }

    /**
     * Rule 7: a null element is rejected when the builder is created, in the first dimension and in every
     * dimension added later, also when the product is already empty.
     */
    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        for (List<String> list : List.of(Arrays.asList(null, "B", "C"), Arrays.asList("A", null, "C"), Arrays.asList("A", "B", null))) {
            assertThrows(NullPointerException.class, () -> cartesianProduct.simpleProductOf(list), "first dimension " + list);
            assertThrows(NullPointerException.class, () -> cartesianProduct.simpleProductOf(AB).and(list), "added dimension " + list);
            assertThrows(NullPointerException.class, () -> emptyProduct().and(list), "added to an empty product " + list);
        }
    }

    // ------------------------------------------------------------------------------------
    // GENERAL RULE 8 - snapshot
    // ------------------------------------------------------------------------------------

    /**
     * Rule 8: changing the caller's lists after the builder was created has no effect on count() or on any
     * generator, whether the generator was created before or after the change.
     */
    @Test
    void builderSnapshotsItsInput() {
        List<String> letters = new ArrayList<>(List.of("A", "B"));
        List<Integer> numbers = new ArrayList<>(List.of(1, 2));
        var builder = cartesianProduct.simpleProductOf(letters).and(numbers);

        BigInteger countBefore = builder.count();
        var lexBefore = builder.lexOrder().stream().toList();
        var mthBefore = builder.lexOrderMth(2, 1).stream().toList();
        var ranksBefore = builder.byRanks(List.of(bi(3), bi(0))).stream().toList();
        var generatorCreatedEarlier = builder.lexOrder();

        letters.add("C");
        letters.set(0, "Z");
        numbers.add(3);
        numbers.set(0, 9);

        assertEquals(countBefore, builder.count());
        assertEquals(lexBefore, builder.lexOrder().stream().toList(), "lexOrder created after the change");
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList(), "lexOrder created before the change");
        assertEquals(mthBefore, builder.lexOrderMth(2, 1).stream().toList(), "lexOrderMth");
        assertEquals(ranksBefore, builder.byRanks(List.of(bi(3), bi(0))).stream().toList(), "byRanks");
    }

    /** Regression guard: and() returns a new builder and leaves the original untouched. */
    @Test
    void andDoesNotChangeTheBuilderItWasCalledOn() {
        var base = cartesianProduct.simpleProductOf(AB);
        var extended = base.and(ONE_TWO_THREE);

        assertEquals(bi(2), base.count());
        assertEquals(bi(6), extended.count());
        assertEquals(List.of(List.of("A"), List.of("B")), base.lexOrder().stream().toList());
    }

    // ------------------------------------------------------------------------------------
    // SIMPLE CARTESIAN PRODUCT table - count and iterator results
    // ------------------------------------------------------------------------------------

    /** Regression guard: count is the product of the dimension sizes. */
    @Test
    void countIsTheProductOfTheDimensionSizes() {
        int[][] shapes = {{1}, {3}, {2, 3}, {3, 1}, {1, 1}, {2, 3, 2}, {1, 2, 3, 2}, {3, 3, 3}};
        for (int[] sizes : shapes) {
            var builder = product(sizes);
            assertEquals(productOf(sizes), builder.count(), Arrays.toString(sizes));
            assertEquals(productOf(sizes).intValueExact(), drain(builder.lexOrder()).size(), Arrays.toString(sizes));
            assertFalse(builder.isEmpty(), Arrays.toString(sizes));
        }
    }

    /**
     * "Any dimension is empty -> count 0, nothing is iterated". Covered for an empty dimension added
     * in any position after the first.
     */
    @Test
    void anEmptyAddedDimensionMakesTheProductEmpty() {
        int[][] shapes = {{2, 0}, {3, 0}, {2, 3, 0}, {2, 0, 3}, {2, 0, 0}};
        for (int[] sizes : shapes) {
            var builder = product(sizes);
            String label = Arrays.toString(sizes);
            assertEquals(BigInteger.ZERO, builder.count(), label);
            assertTrue(builder.isEmpty(), label);
            assertTrue(drain(builder.lexOrder()).isEmpty(), label);
            assertTrue(drain(builder.lexOrderMth(1, 0)).isEmpty(), label);
        }
    }

    /**
     * Rule: isEmpty() is true if and only if count() == 0. Includes shapes whose FIRST dimension is empty
     * and that have further dimensions; the test compares isEmpty() with the builder's own count() and with
     * the independent product of the sizes.
     */
    @Test
    void isEmptyIsTrueExactlyWhenCountIsZero() {
        int[][] shapes = {{1}, {2}, {2, 3}, {2, 0}, {0, 2}, {0, 3}, {0, 0}, {0, 2, 3}, {2, 0, 3}, {2, 3, 0}, {2, 3, 2}};
        for (int[] sizes : shapes) {
            var builder = product(sizes);
            String label = Arrays.toString(sizes);
            assertEquals(productOf(sizes), builder.count(), label + ": count");
            assertEquals(builder.count().signum() == 0, builder.isEmpty(), label + ": isEmpty() vs count()==" + builder.count());
        }
    }

    // ------------------------------------------------------------------------------------
    // GENERAL RULE 9 - immutable results, re-iterable generators
    // ------------------------------------------------------------------------------------

    /**
     * Rule 9: every tuple handed to the client is immutable, whichever generator produced it.
     */
    @Test
    void resultsAreImmutable() {
        var builders = List.of(
                cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE),
                cartesianProduct.simpleProductOf(List.of("A", "B", "C")),            // a single dimension
                cartesianProduct.simpleProductOf(List.of("A")).and(List.of(1)));      // a single tuple
        for (var builder : builders) {
            assertImmutable("lexOrder", builder.lexOrder().stream().limit(3).toList());
            assertImmutable("lexOrderMth", builder.lexOrderMth(1, 0).stream().limit(3).toList());
            assertImmutable("byRanks", builder.byRanks(List.of(BigInteger.ZERO)).stream().toList());
            assertImmutable("choice", builder.choice(2, new Random(1)).stream().toList());
            assertImmutable("sample", builder.sample(1, new Random(1)).stream().toList());
        }
    }

    @SuppressWarnings("unchecked")
    private static void assertImmutable(String source, List<?> tuples) {
        assertFalse(tuples.isEmpty(), source + " produced nothing, so nothing could be checked");
        for (Object tuple : tuples) {
            List<Object> list = (List<Object>) tuple;
            assertThrows(UnsupportedOperationException.class, () -> list.add("X"), source + ": add() on " + list);
            assertThrows(UnsupportedOperationException.class, () -> list.set(0, "X"), source + ": set() on " + list);
        }
    }

    /**
     * Rule 9: every iterator() call on a generator starts a fresh, independent traversal.
     */
    @Test
    void generatorsAreReIterable() {
        var builder = cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE);
        var generators = new java.util.LinkedHashMap<String, Iterable<?>>();
        generators.put("lexOrder", builder.lexOrder());
        generators.put("lexOrderMth(2, 1)", builder.lexOrderMth(2, 1));
        generators.put("byRanks([5, 0, 5])", builder.byRanks(List.of(bi(5), bi(0), bi(5))));

        for (var entry : generators.entrySet()) {
            Iterable<?> generator = entry.getValue();
            var first = drain(generator);
            assertFalse(first.isEmpty(), entry.getKey() + ": first traversal produced nothing");
            assertEquals(first, drain(generator), entry.getKey() + ": a second traversal differs");

            List<Object> a = new ArrayList<>(), b = new ArrayList<>();
            Iterator<?> i1 = generator.iterator(), i2 = generator.iterator();
            while (i1.hasNext() || i2.hasNext()) {                        // interleaved
                if (i1.hasNext()) a.add(i1.next());
                if (i2.hasNext()) b.add(i2.next());
            }
            assertEquals(first, a, entry.getKey() + ": interleaved iterator 1");
            assertEquals(first, b, entry.getKey() + ": interleaved iterator 2");
        }
    }

    // ------------------------------------------------------------------------------------
    // TRAVERSAL / SAMPLING RULES
    // ------------------------------------------------------------------------------------

    /** Regression guard: lexOrderMth validation and mth-sampling behavior. */
    @Test
    void lexOrderMthRules() {
        var builder = cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE);     // count 6
        var lex = drain(builder.lexOrder());

        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(BigInteger.ZERO, BigInteger.ZERO));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(bi(-1), BigInteger.ZERO));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(BigInteger.ONE, bi(-1)));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(BigInteger.ONE, bi(6)));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(BigInteger.ONE, bi(7)));

        assertEquals(List.of(lex.get(1), lex.get(3), lex.get(5)), drain(builder.lexOrderMth(2, 1)));
        assertEquals(lex, drain(builder.lexOrderMth(1, 0)));

        // count = 0: any start >= 0 is accepted and nothing is yielded
        var empty = emptyProduct();
        assertTrue(drain(empty.lexOrderMth(1, 0)).isEmpty());
        assertTrue(drain(empty.lexOrderMth(3, 5)).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> empty.lexOrderMth(0, 0));
        assertThrows(IllegalArgumentException.class, () -> empty.lexOrderMth(1, -1));
    }

    /** Regression guard: byRanks lazy validation and rank consumption. */
    @Test
    void byRanksRules() {
        var builder = cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE);
        var lex = drain(builder.lexOrder());

        assertThrows(IllegalArgumentException.class, () -> builder.byRanks(null));

        // consumed lazily: nothing is read at the call, the caller may stop whenever it likes
        int[] pulled = {0};
        Iterable<BigInteger> infinite = () -> new Iterator<>() {
            public boolean hasNext() { return true; }
            public BigInteger next() { pulled[0]++; return BigInteger.ONE; }
        };
        var lazy = builder.byRanks(infinite);
        assertEquals(0, pulled[0], "creating the generator must not read any rank");
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            Iterator<?> it = lazy.iterator();
            for (int i = 0; i < 3; i++) assertEquals(lex.get(1), it.next());
        });
        assertTrue(pulled[0] <= 4, "only the requested ranks may be read, but " + pulled[0] + " were");

        // an invalid rank is reported when it is reached, after the valid ones
        for (BigInteger bad : List.of(bi(6), bi(7), bi(-1), BigInteger.TEN.pow(30))) {
            var generator = assertDoesNotThrow(() -> builder.byRanks(List.of(BigInteger.ZERO, bad)), "rank " + bad + " must be reported lazily");
            Iterator<?> it = generator.iterator();
            assertEquals(lex.get(0), it.next());
            assertThrows(IllegalArgumentException.class, () -> { it.hasNext(); it.next(); }, "rank " + bad);
        }

        // rank k is the k-th result of lexOrder(), and the order of the rank source is respected
        assertEquals(List.of(lex.get(5), lex.get(0), lex.get(5)), drain(builder.byRanks(List.of(bi(5), bi(0), bi(5)))));

        // empty product: every rank is out of range; no ranks, no results
        var empty = emptyProduct();
        assertThrows(IllegalArgumentException.class, () -> drain(empty.byRanks(List.of(BigInteger.ZERO))));
        assertTrue(drain(empty.byRanks(List.of())).isEmpty());
    }

    /** Regression guard: sample validation and sampling behavior. */
    @Test
    void sampleRules() {
        var builder = cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE);     // count 6
        var lex = drain(builder.lexOrder());

        assertThrows(IllegalArgumentException.class, () -> builder.sample(0, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> builder.sample(-1, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> builder.sample(7, new Random(1)));       // without replacement

        var all = drain(builder.sample(6, new Random(42)));
        assertEquals(6, all.size());
        assertEquals(new HashSet<>(lex), new HashSet<>(all), "sample(count) must return every tuple exactly once");

        // empty product: count is 0, so every sample size is invalid
        var empty = emptyProduct();
        assertThrows(IllegalArgumentException.class, () -> empty.sample(1, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> empty.sample(0, new Random(1)));
    }

    /** Regression guard: choice is with replacement, so s > count is accepted. */
    @Test
    void choiceRules() {
        var builder = cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE);     // count 6
        var members = new HashSet<>(drain(builder.lexOrder()));

        assertThrows(IllegalArgumentException.class, () -> builder.choice(0, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> builder.choice(-1, new Random(1)));

        var drawn = drain(builder.choice(40, new Random(7)));
        assertEquals(40, drawn.size());
        assertTrue(members.containsAll(drawn));
        assertTrue(new HashSet<>(drawn).size() < 40, "with more draws than tuples there must be duplicates");
    }

    /**
     * Rule: choice with count = 0 is an invalid-argument error (nothing can be drawn from an empty domain).
     */
    @Test
    void choiceFromAnEmptyProductIsRejected() {
        var empty = emptyProduct();
        assertThrows(IllegalArgumentException.class, () -> empty.choice(1, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> empty.choice(5, new Random(1)));
        assertThrows(IllegalArgumentException.class, () -> empty.choice(0, new Random(1)));
    }

    /**
     * Rule: a null random source is a null-input error (NullPointerException, as in every other builder).
     */
    @Test
    void nullRandomIsANullInputError() {
        var builder = cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE);
        assertThrows(NullPointerException.class, () -> builder.choice(1, null));
        assertThrows(NullPointerException.class, () -> builder.sample(1, null));
    }

    /** Regression guard: for a given seed, sample and choice are reproducible. */
    @Test
    void sampleAndChoiceAreReproducibleForAGivenSeed() {
        var builder = cartesianProduct.simpleProductOf(AB).and(ONE_TWO_THREE);
        assertEquals(drain(builder.sample(5, new Random(99))), drain(builder.sample(5, new Random(99))));
        assertEquals(drain(builder.choice(9, new Random(99))), drain(builder.choice(9, new Random(99))));
    }

    // ------------------------------------------------------------------------------------
    // Randomness beyond 64 bits
    // ------------------------------------------------------------------------------------

    /**
     * choice and sample draw from the whole range [0, count) however large count is: 3^41 > 2^64 here, so
     * a sampler that stopped at 2^63 would never use the first dimension's last value.
     */
    @Test
    void choiceAndSampleDrawFromTheWholeRangeWhenCountExceeds64Bits() {
        List<String> abc = List.of("A", "B", "C");
        SimpleProductBuilder builder = cartesianProduct.simpleProductOf(abc);
        for (int i = 1; i < 41; i++) builder = builder.and(abc);
        final SimpleProductBuilder product = builder;
        assertTrue(product.count().bitLength() > 64);

        for (var source : List.of(product.choice(300, new Random(1)), product.sample(300, new Random(1)))) {
            var firstValues = new HashSet<Object>();
            for (Object tuple : source) {
                List<?> t = (List<?>) tuple;
                assertEquals(41, t.size());
                firstValues.add(t.get(0));
            }
            assertEquals(new HashSet<>(abc), firstValues, "the first dimension must take every value");
        }

        BigInteger last = product.count().subtract(BigInteger.ONE);
        assertEquals(Collections.nCopies(41, "C"), product.byRanks(List.of(last)).iterator().next());
        assertEquals(Collections.nCopies(41, "A"), product.byRanks(List.of(BigInteger.ZERO)).iterator().next());
        assertThrows(IllegalArgumentException.class, () -> drain(product.byRanks(List.of(product.count()))));
    }
}