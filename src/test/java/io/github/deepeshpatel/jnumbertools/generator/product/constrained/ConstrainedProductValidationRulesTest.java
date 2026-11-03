package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation rules for the public API of the CONSTRAINED CARTESIAN PRODUCT builder.
 *
 * Conventions follow the already-migrated builders (see SubsetBuilderTest):
 *   null list / null element / null Random -> NullPointerException
 *   null ranks                              -> IllegalArgumentException
 *   everything else invalid                 -> IllegalArgumentException
 *
 * Expected counts below are computed independently (plain binomials), NOT through Calculator or
 * through the builders' own isEmpty(), so a bug that is present in both count() and isEmpty()
 * cannot hide.
 */
class ConstrainedProductValidationRulesTest {

    private static final List<String> ABC = List.of("A", "B", "C");
    private static final List<Integer> ONE_TO_FOUR = List.of(1, 2, 3, 4);

    private static BigInteger bi(long v) {
        return BigInteger.valueOf(v);
    }

    private static List<Integer> range(int n) {
        return IntStream.range(0, n).boxed().toList();
    }

    private static BigInteger binom(int n, int k) {
        if (k < 0 || k > n) return BigInteger.ZERO;
        BigInteger r = BigInteger.ONE;
        for (int i = 1; i <= k; i++) {
            r = r.multiply(bi(n - k + i)).divide(bi(i));
        }
        return r;
    }

    private static BigInteger distinctCount(int n, int q) {
        return binom(n, q);
    }

    private static BigInteger multiSelectCount(int n, int q) {
        if (q == 0) return BigInteger.ONE;
        return n == 0 ? BigInteger.ZERO : binom(n + q - 1, q);
    }

    private static BigInteger rangeCount(int n, int from, int to) {
        if (n == 0) return from == 0 ? BigInteger.ONE : BigInteger.ZERO;
        BigInteger sum = BigInteger.ZERO;
        for (int i = from; i <= to; i++) sum = sum.add(binom(n, i));
        return sum;
    }

    /** A product that is empty for sure: its only dimension asks for 1 element out of nothing. */
    private ConstrainedProductBuilder emptyProduct() {
        var p = cartesianProduct.constrainedProductOfDistinct(1, Collections.emptyList());
        assertEquals(BigInteger.ZERO, p.count());
        assertTrue(p.isEmpty());
        return p;
    }

    // ------------------------------------------------------------------------------------
    // GENERAL RULE 1 - null input
    // ------------------------------------------------------------------------------------

    /** Regression guard: null input is rejected everywhere. */
    @Test
    void nullInputIsRejectedEverywhere() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, ABC);
        List<String> nothing = null;

        assertThrows(NullPointerException.class, () -> cartesianProduct.constrainedProductOfDistinct(2, nothing));
        assertThrows(NullPointerException.class, () -> cartesianProduct.constrainedProductOfMultiSelect(2, nothing));
        assertThrows(NullPointerException.class, () -> cartesianProduct.constrainedProductOfInRange(0, 1, nothing));
        assertThrows(NullPointerException.class, () -> new ConstrainedProductBuilder(2, nothing, false, calculator));
        assertThrows(NullPointerException.class, () -> new ConstrainedProductBuilder(0, 1, nothing, calculator));

        assertThrows(NullPointerException.class, () -> builder.andDistinct(2, nothing));
        assertThrows(NullPointerException.class, () -> builder.andMultiSelect(2, nothing));
        assertThrows(NullPointerException.class, () -> builder.andInRange(0, 1, nothing));

        // ...also when the product is already empty and the builder would otherwise short-circuit
        var empty = emptyProduct();
        assertThrows(NullPointerException.class, () -> empty.andDistinct(2, nothing));
        assertThrows(NullPointerException.class, () -> empty.andMultiSelect(2, nothing));
        assertThrows(NullPointerException.class, () -> empty.andInRange(0, 1, nothing));
    }

    // ------------------------------------------------------------------------------------
    // GENERAL RULE 7 - null elements rejected while the builder copies its list
    // ------------------------------------------------------------------------------------

    /**
     * Null elements must be rejected by every entry point, including on already-empty products.
     */
    @Test
    void nullElementsAreRejectedWhenBuilderIsCreated() {
        List<String> withNull = Arrays.asList("A", null, "C");
        var builder = cartesianProduct.constrainedProductOfDistinct(1, ABC);
        var empty = emptyProduct();

        List<Executable> checks = List.of(
                () -> assertThrows(NullPointerException.class, () -> cartesianProduct.constrainedProductOfDistinct(2, withNull), "ofDistinct"),
                () -> assertThrows(NullPointerException.class, () -> cartesianProduct.constrainedProductOfMultiSelect(2, withNull), "ofMultiSelect"),
                () -> assertThrows(NullPointerException.class, () -> cartesianProduct.constrainedProductOfInRange(0, 2, withNull), "ofInRange"),
                () -> assertThrows(NullPointerException.class, () -> new ConstrainedProductBuilder(2, withNull, false, calculator), "ctor distinct"),
                () -> assertThrows(NullPointerException.class, () -> new ConstrainedProductBuilder(2, withNull, true, calculator), "ctor multi"),
                () -> assertThrows(NullPointerException.class, () -> new ConstrainedProductBuilder(0, 2, withNull, calculator), "ctor range"),
                () -> assertThrows(NullPointerException.class, () -> builder.andDistinct(2, withNull), "andDistinct"),
                () -> assertThrows(NullPointerException.class, () -> builder.andMultiSelect(2, withNull), "andMultiSelect"),
                () -> assertThrows(NullPointerException.class, () -> builder.andInRange(0, 2, withNull), "andInRange"),
                () -> assertThrows(NullPointerException.class, () -> empty.andDistinct(2, withNull), "empty.andDistinct"),
                () -> assertThrows(NullPointerException.class, () -> empty.andMultiSelect(2, withNull), "empty.andMultiSelect"),
                () -> assertThrows(NullPointerException.class, () -> empty.andInRange(0, 2, withNull), "empty.andInRange")
        );
        assertAll(checks);
    }

    // ------------------------------------------------------------------------------------
    // GENERAL RULE 8 - builders snapshot their input
    // ------------------------------------------------------------------------------------

    /**
     * Builders must snapshot every dimension; changes to caller-owned lists after construction
     * must not affect count(), lexOrder(), or any generated tuples.
     */
    @Test
    void builderTakesSnapshotOfEveryDimension() {
        List<String> first = new ArrayList<>(ABC);
        List<Integer> second = new ArrayList<>(ONE_TO_FOUR);
        List<String> third = new ArrayList<>(List.of("x", "y"));

        var builder = cartesianProduct.constrainedProductOfDistinct(2, first)
                .andMultiSelect(2, second)
                .andInRange(1, 2, third);

        BigInteger countBefore = builder.count();
        var lexBefore = builder.lexOrder().stream().toList();
        var mthBefore = builder.lexOrderMth(7, 3).stream().toList();
        var ranksBefore = builder.byRanks(List.of(BigInteger.ZERO, bi(20))).stream().toList();
        var generatorCreatedEarlier = builder.lexOrder();

        first.add("D");
        first.set(0, "Z");
        second.set(0, 99);
        second.remove(3);
        third.clear();

        assertEquals(countBefore, builder.count());
        assertEquals(lexBefore, builder.lexOrder().stream().toList());
        assertEquals(mthBefore, builder.lexOrderMth(7, 3).stream().toList());
        assertEquals(ranksBefore, builder.byRanks(List.of(BigInteger.ZERO, bi(20))).stream().toList());
        assertEquals(lexBefore, generatorCreatedEarlier.stream().toList());
    }

    // ------------------------------------------------------------------------------------
    // Error rules: quantity < 0 and invalid range -> invalid-argument error
    // (must be raised for EVERY input, also empty element lists and already-empty products)
    // ------------------------------------------------------------------------------------

    /**
     * Negative quantity is an invalid-argument error for every input, including empty element
     * lists and already-empty products.
     */
    @SuppressWarnings("removal")
    @Test
    void negativeQuantityIsAlwaysAnInvalidArgumentError() {
        var builder = cartesianProduct.constrainedProductOfDistinct(1, ABC);
        var empty = emptyProduct();
        List<String> noElements = Collections.emptyList();

        List<Executable> checks = List.of(
                // non-empty input (should already pass)
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfDistinct(-1, ABC), "ofDistinct"),
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfMultiSelect(-1, ABC), "ofMultiSelect"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andDistinct(-1, ABC), "andDistinct"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andMultiSelect(-1, ABC), "andMultiSelect"),
                // empty element list
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfDistinct(-1, noElements), "ofDistinct, empty list"),
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfMultiSelect(-1, noElements), "ofMultiSelect, empty list"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andDistinct(-1, noElements), "andDistinct, empty list"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andMultiSelect(-1, noElements), "andMultiSelect, empty list"),
                // public constructors
                () -> assertThrows(IllegalArgumentException.class, () -> new ConstrainedProductBuilder(-1, ABC, false, calculator), "ctor distinct"),
                () -> assertThrows(IllegalArgumentException.class, () -> new ConstrainedProductBuilder(-1, noElements, false, calculator), "ctor distinct, empty list"),
                () -> assertThrows(IllegalArgumentException.class, () -> new ConstrainedProductBuilder(-1, noElements, true, calculator), "ctor multi, empty list"),
                // deprecated factory is still public API
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOf(-1, ABC), "constrainedProductOf"),
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOf(-1, noElements), "constrainedProductOf, empty list"),
                // already-empty product must not skip validation
                () -> assertThrows(IllegalArgumentException.class, () -> empty.andDistinct(-1, ABC), "empty.andDistinct"),
                () -> assertThrows(IllegalArgumentException.class, () -> empty.andMultiSelect(-1, ABC), "empty.andMultiSelect")
        );
        assertAll(checks);
    }

    /**
     * Rule: invalid range (from < 0, to < from, from > n, to > n) -> invalid-argument error.
     * Must be raised for every input, including already-empty products.
     */
    @Test
    void invalidRangeIsAlwaysAnInvalidArgumentError() {
        var builder = cartesianProduct.constrainedProductOfDistinct(1, ABC);
        var empty = emptyProduct();

        List<Executable> checks = List.of(
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfInRange(-1, 2, ABC), "from < 0"),
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfInRange(3, 1, ABC), "to < from"),
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfInRange(4, 4, ABC), "from > n"),
                () -> assertThrows(IllegalArgumentException.class, () -> cartesianProduct.constrainedProductOfInRange(0, 4, ABC), "to > n"),
                () -> assertThrows(IllegalArgumentException.class, () -> new ConstrainedProductBuilder(3, 1, ABC, calculator), "ctor to < from"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andInRange(-1, 2, ABC), "and from < 0"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andInRange(3, 1, ABC), "and to < from"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andInRange(4, 4, ABC), "and from > n"),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.andInRange(0, 4, ABC), "and to > n"),
                () -> assertThrows(IllegalArgumentException.class, () -> empty.andInRange(-1, 2, ABC), "empty.and from < 0"),
                () -> assertThrows(IllegalArgumentException.class, () -> empty.andInRange(3, 1, ABC), "empty.and to < from"),
                () -> assertThrows(IllegalArgumentException.class, () -> empty.andInRange(4, 4, ABC), "empty.and from > n"),
                () -> assertThrows(IllegalArgumentException.class, () -> empty.andInRange(0, 4, ABC), "empty.and to > n")
        );
        assertAll(checks);
    }

    /** Rule: n = 0 accepts any range with 0 <= from <= to (table SUBSETS). */
    @Test
    void emptySetAcceptsAnyRangeEvenHugeOnes() {
        var withEmptySubset = cartesianProduct.constrainedProductOfInRange(0, Integer.MAX_VALUE, Collections.emptyList());
        assertEquals(BigInteger.ONE, withEmptySubset.count());
        assertFalse(withEmptySubset.isEmpty());
        assertEquals(List.of(List.of()), withEmptySubset.lexOrder().stream().toList());

        var noSubsets = cartesianProduct.constrainedProductOfInRange(1, Integer.MAX_VALUE, Collections.emptyList());
        assertEquals(BigInteger.ZERO, noSubsets.count());
        assertTrue(noSubsets.isEmpty());
    }

    // ------------------------------------------------------------------------------------
    // CONSTRAINED CARTESIAN PRODUCT table: counts, [[]] / [] results, isEmpty() <=> count() == 0
    // ------------------------------------------------------------------------------------

    /**
     * Rule: count / isEmpty / lexOrder() must follow the table for EVERY (n, quantity) and (n, from, to).
     * isEmpty() must be true exactly when count() is zero.
     */
    @Test
    void countIsEmptyAndLexOrderFollowTheTableForSingleDimensions() {
        List<Executable> checks = new ArrayList<>();
        for (int n = 0; n <= 3; n++) {
            for (int q = 0; q <= 4; q++) {
                final int fn = n, fq = q;
                var list = range(n);
                checks.add(() -> assertDimension("distinct n=" + fn + " q=" + fq,
                        cartesianProduct.constrainedProductOfDistinct(fq, list), distinctCount(fn, fq)));
                checks.add(() -> assertDimension("multiSelect n=" + fn + " q=" + fq,
                        cartesianProduct.constrainedProductOfMultiSelect(fq, list), multiSelectCount(fn, fq)));
            }
            for (int from = 0; from <= 3; from++) {
                for (int to = from; to <= 3; to++) {
                    if (n > 0 && to > n) continue;                  // rejected, see invalidRangeIs...
                    final int fn = n, ff = from, ft = to;
                    var list = range(n);
                    checks.add(() -> assertDimension("range n=" + fn + " [" + ff + "," + ft + "]",
                            cartesianProduct.constrainedProductOfInRange(ff, ft, list), rangeCount(fn, ff, ft)));
                }
            }
        }
        assertAll(checks);
    }

    /** Same table, product of several dimensions (count = product of the dimension counts). */
    @Test
    void countIsEmptyAndLexOrderFollowTheTableForChainedDimensions() {
        List<Executable> checks = new ArrayList<>();
        for (int q1 = 0; q1 <= 3; q1++) {
            for (int q2 = 0; q2 <= 3; q2++) {
                final int f1 = q1, f2 = q2;
                // dimension 1: distinct q1 of 3, dimension 2: multi-select q2 of 2, dimension 3: subsets of {0,1}
                BigInteger expected = distinctCount(3, q1).multiply(multiSelectCount(2, q2)).multiply(rangeCount(2, 0, 2));
                checks.add(() -> assertDimension("chain q1=" + f1 + " q2=" + f2,
                        cartesianProduct.constrainedProductOfDistinct(f1, ABC)
                                .andMultiSelect(f2, List.of(1, 2))
                                .andInRange(0, 2, List.of("x", "y")),
                        expected));
            }
        }
        assertAll(checks);
    }

    private static void assertDimension(String label, ConstrainedProductBuilder builder, BigInteger expectedCount) {
        assertAll(label,
                () -> assertEquals(expectedCount, builder.count(), "count"),
                () -> assertEquals(expectedCount.signum() == 0, builder.isEmpty(), "isEmpty"),
                () -> assertEquals(expectedCount.longValueExact(), builder.lexOrder().stream().count(), "lexOrder size"));
    }

    /**
     * Rule: multiplicity 0 in a dimension contributes exactly one empty tuple ("as if the dimension
     * were absent"), also when the dimension has elements.
     */
    @Test
    void zeroMultiplicityDimensionContributesOneEmptyTuple() {
        var base = cartesianProduct.constrainedProductOfDistinct(2, ABC);          // 3 tuples
        var baseTuples = base.lexOrder().stream().toList();
        assertEquals(3, baseTuples.size());

        List<Executable> checks = List.of(
                () -> assertSameAsBase(baseTuples, base.andDistinct(0, List.of("X", "Y")), "andDistinct(0, non-empty)"),
                () -> assertSameAsBase(baseTuples, base.andMultiSelect(0, List.of("X", "Y")), "andMultiSelect(0, non-empty)"),
                () -> assertSameAsBase(baseTuples, base.andInRange(0, 0, List.of("X", "Y")), "andInRange(0,0, non-empty)"),
                () -> assertSameAsBase(baseTuples, base.andDistinct(0, Collections.emptyList()), "andDistinct(0, empty)"),
                () -> assertSameAsBase(baseTuples, base.andMultiSelect(0, Collections.emptyList()), "andMultiSelect(0, empty)"),
                () -> assertSameAsBase(baseTuples, base.andInRange(0, 0, Collections.emptyList()), "andInRange(0,0, empty)"));
        assertAll(checks);

        // as the FIRST dimension: a single [[]]
        var onlyEmptyTuple = cartesianProduct.constrainedProductOfDistinct(0, ABC);
        assertEquals(BigInteger.ONE, onlyEmptyTuple.count());
        assertFalse(onlyEmptyTuple.isEmpty());
        assertEquals(List.of(List.of()), onlyEmptyTuple.lexOrder().stream().toList());
    }

    private static void assertSameAsBase(List<List<Object>> baseTuples, ConstrainedProductBuilder b, String label) {
        assertAll(label,
                () -> assertEquals(bi(baseTuples.size()), b.count(), "count"),
                () -> assertFalse(b.isEmpty(), "isEmpty"),
                () -> assertEquals(baseTuples, b.lexOrder().stream().toList(), "lexOrder"));
    }

    /** Rule: any dimension with count = 0 makes the product empty ([]). */
    @Test
    void anyEmptyDimensionMakesTheProductEmpty() {
        var p = cartesianProduct.constrainedProductOfDistinct(2, ABC)
                .andDistinct(2, Collections.emptyList())
                .andMultiSelect(2, ABC);                    // later dimension must not revive it
        assertEquals(BigInteger.ZERO, p.count());
        assertTrue(p.isEmpty());
        assertTrue(p.lexOrder().stream().toList().isEmpty());
    }

    // ------------------------------------------------------------------------------------
    // TRAVERSAL / SAMPLING RULES
    // ------------------------------------------------------------------------------------

    /**
     * choice() and sample() must reject sample sizes <= 0 and sizes exceeding the domain count.
     */
    @Test
    void choiceAndSampleSizeRules() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, ABC).andMultiSelect(2, ONE_TO_FOUR);   // 30

        assertThrows(IllegalArgumentException.class, () -> builder.choice(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> builder.choice(-1, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> builder.sample(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> builder.sample(-1, TestBase.random));

        assertThrows(IllegalArgumentException.class, () -> builder.sample(31, TestBase.random));
        var everything = builder.sample(30, new Random(1)).stream().toList();
        assertEquals(30, new HashSet<>(everything).size());

        assertEquals(100, builder.choice(100, new Random(1)).stream().count());
    }

    /**
     * Rule: choice with count = 0 -> invalid-argument error; sample with count = 0 -> invalid-argument error.
     */
    @Test
    void emptyDomainRejectsChoiceAndSample() {
        var emptyDomain = emptyProduct();

        assertThrows(IllegalArgumentException.class, () -> emptyDomain.choice(1, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.sample(1, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.choice(0, TestBase.random));
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.sample(0, TestBase.random));
    }

    /**
     * Rule: sample/choice with a null random source -> null-input error.
     */
    @Test
    void nullRandomSourceThrowsNullInputError() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, ABC);

        assertThrows(NullPointerException.class, () -> builder.choice(1, null));
        assertThrows(NullPointerException.class, () -> builder.sample(1, null));
    }

    /**
     * Rule: count = 1 and the only element is the empty list -> choice yields s empty results,
     * sample(1) one, byRanks one [] per rank (all ranks must be 0), lexOrderMth(1, 0) one [].
     * Quantity 0 on any list (empty or not) must yield count = 1 and isEmpty() = false.
     */
    @Test
    void domainWhoseOnlyElementIsTheEmptyList() {
        List<Executable> checks = List.of(
                () -> assertOnlyEmptyTuple("distinct(0, empty list)", cartesianProduct.constrainedProductOfDistinct(0, Collections.emptyList())),
                () -> assertOnlyEmptyTuple("distinct(0, A,B,C)", cartesianProduct.constrainedProductOfDistinct(0, ABC)),
                () -> assertOnlyEmptyTuple("multiSelect(0, A,B,C)", cartesianProduct.constrainedProductOfMultiSelect(0, ABC)),
                () -> assertOnlyEmptyTuple("inRange(0,0, A,B,C)", cartesianProduct.constrainedProductOfInRange(0, 0, ABC)),
                () -> assertOnlyEmptyTuple("inRange(0,5, empty list)", cartesianProduct.constrainedProductOfInRange(0, 5, Collections.emptyList())));
        assertAll(checks);
    }

    private static void assertOnlyEmptyTuple(String label, ConstrainedProductBuilder b) {
        assertAll(label,
                () -> assertEquals(BigInteger.ONE, b.count(), "count"),
                () -> assertFalse(b.isEmpty(), "isEmpty"),
                () -> assertEquals(List.of(List.of()), b.lexOrder().stream().toList(), "lexOrder"),
                () -> assertEquals(List.of(List.of()), b.lexOrderMth(1, 0).stream().toList(), "lexOrderMth(1,0)"),
                () -> assertThrows(IllegalArgumentException.class, () -> b.lexOrderMth(1, 1), "lexOrderMth(1,1)"),
                () -> assertEquals(List.of(List.of(), List.of(), List.of()),
                        b.byRanks(List.of(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)).stream().toList(), "byRanks(0,0,0)"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> b.byRanks(List.of(BigInteger.ZERO, BigInteger.ONE)).stream().toList(), "byRanks(0,1)"),
                () -> assertEquals(List.of(List.of(), List.of(), List.of()), b.choice(3, new Random(1)).stream().toList(), "choice(3)"),
                () -> assertEquals(List.of(List.of()), b.sample(1, new Random(1)).stream().toList(), "sample(1)"),
                () -> assertThrows(IllegalArgumentException.class, () -> b.sample(2, new Random(1)), "sample(2)"));
    }

    /** Rule: lexOrderMth - count = 0 accepts any start >= 0; m <= 0 and start < 0 stay invalid. */
    @Test
    void lexOrderMthRules() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, ABC).andMultiSelect(2, ONE_TO_FOUR);   // 30
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(0, 0));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(1, -1));
        assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(1, 30));

        var emptyDomain = emptyProduct();
        assertTrue(emptyDomain.lexOrderMth(1, 0).stream().toList().isEmpty());
        assertTrue(emptyDomain.lexOrderMth(3, 5).stream().toList().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.lexOrderMth(0, 0));
        assertThrows(IllegalArgumentException.class, () -> emptyDomain.lexOrderMth(1, -1));
    }

    /**
     * Rule: byRanks - only null is checked up front, ranks are consumed lazily (may be infinite), and a
     * rank < 0 or >= count is reported lazily when reached.
     */
    @Test
    void byRanksIsLazyAndOnlyChecksNullUpFront() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, ABC).andMultiSelect(2, ONE_TO_FOUR);   // 30

        assertThrows(IllegalArgumentException.class, () -> builder.byRanks(null));

        var tooBig = builder.byRanks(List.of(bi(30)));
        var negative = builder.byRanks(List.of(BigInteger.ZERO, bi(-1)));
        assertThrows(IllegalArgumentException.class, () -> tooBig.stream().toList());
        Iterator<List<Object>> it = negative.iterator();
        assertEquals(List.of("A", "B", 1, 1), it.next());
        assertThrows(IllegalArgumentException.class, it::next);

        Iterable<BigInteger> infinite = () ->
                Stream.iterate(BigInteger.ZERO, r -> r.add(BigInteger.ONE).mod(bi(30))).iterator();
        assertEquals(10, builder.byRanks(infinite).stream().limit(10).count());
    }

    // ------------------------------------------------------------------------------------
    // GENERAL RULE 9 - immutability and re-iterability
    // ------------------------------------------------------------------------------------

    /**
     * Results must be immutable tuples for every generation source.
     */
    @Test
    void resultsAreImmutable() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, ABC).andMultiSelect(2, ONE_TO_FOUR);   // 30

        Map<String, List<List<Object>>> produced = new LinkedHashMap<>();
        produced.put("lexOrder", builder.lexOrder().stream().toList());
        produced.put("lexOrderMth", builder.lexOrderMth(7, 3).stream().toList());
        produced.put("byRanks", builder.byRanks(List.of(BigInteger.ZERO, bi(29))).stream().toList());
        produced.put("choice", builder.choice(3, new Random(1)).stream().toList());
        produced.put("sample", builder.sample(3, new Random(1)).stream().toList());
        produced.put("single dimension lexOrder", cartesianProduct.constrainedProductOfDistinct(2, ABC).lexOrder().stream().toList());
        produced.put("subset dimension lexOrder", cartesianProduct.constrainedProductOfInRange(1, 2, ABC).lexOrder().stream().toList());

        List<Executable> checks = new ArrayList<>();
        produced.forEach((source, tuples) -> {
            assertFalse(tuples.isEmpty(), source + " produced nothing");
            for (List<Object> tuple : tuples) {
                checks.add(() -> assertThrows(UnsupportedOperationException.class,
                        () -> tuple.add("Z"), source + ": add() on " + tuple));
                checks.add(() -> assertThrows(UnsupportedOperationException.class,
                        () -> tuple.set(0, "Z"), source + ": set() on " + tuple));
            }
        });
        assertAll(checks);
    }

    /** Regression guard: generators are re-iterable. */
    @Test
    void generatorsAreReIterable() {
        var builder = cartesianProduct.constrainedProductOfDistinct(2, ABC).andMultiSelect(2, ONE_TO_FOUR);

        var lex = builder.lexOrder();
        assertEquals(lex.stream().toList(), lex.stream().toList(), "lexOrder");

        var mth = builder.lexOrderMth(7, 3);
        assertEquals(mth.stream().toList(), mth.stream().toList(), "lexOrderMth");

        var byRanks = builder.byRanks(List.of(BigInteger.ZERO, bi(29)));
        assertEquals(byRanks.stream().toList(), byRanks.stream().toList(), "byRanks");
    }
}