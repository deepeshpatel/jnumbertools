package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

/**
 * lexOrder() of the constrained Cartesian product must be lazy: producing the first tuples may not
 * require enumerating (materializing) any dimension. Only the public builder API is used.
 *
 * C(34,17) = 2,333,606,220 combinations can never be held in memory, so a lexOrder() that materializes
 * a dimension cannot finish: it fails with OutOfMemoryError. firstTuples() converts that into an
 * AssertionError inside the test, because
 *   - JUnit 5 treats OutOfMemoryError as unrecoverable and aborts the whole run if it escapes a test, and
 *   - assertTimeoutPreemptively is no alternative: it abandons the runaway thread, which keeps allocating.
 * Once the stack unwinds the half-built lists are garbage and the JVM carries on with the other tests.
 *
 * PREDICTED on the current code (to be confirmed by running):
 *   FAIL  hugeDimensionLast, hugeDimensionFirst, hugeDimensionInTheMiddle
 *   PASS  the rest (guards that pin the exact tuple order and the iterator behaviour)
 */
class ConstrainedProductLexOrderLazinessTest {

    private static final List<String> A_B_C = List.of("A", "B", "C");
    private static final List<Integer> THIRTY_FOUR = IntStream.range(0, 34).boxed().toList();   // C(34,17) ~ 2.3e9

    /** The first {@code n} tuples of lexOrder(); a dimension being materialized shows up as a failed assertion. */
    private static List<List<Object>> firstTuples(ConstrainedProductBuilder product, int n) {
        try {
            return product.lexOrder().stream().limit(n).toList();
        } catch (OutOfMemoryError e) {
            throw new AssertionError("lexOrder() ran out of memory producing " + n
                    + " tuples: a dimension is being materialized instead of iterated lazily", e);
        }
    }

    private static List<Object> tuple(Object... parts) {
        return List.of(parts);
    }

    private static List<Object> firstSeventeen() {
        return new ArrayList<>(IntStream.range(0, 17).boxed().toList());
    }

    // ---- laziness: these fail while a dimension is materialized -----------------------------------

    @Test
    void hugeDimensionLast() {
        var product = cartesianProduct.constrainedProductOfDistinct(2, A_B_C)      // 3 combinations
                .andDistinct(17, THIRTY_FOUR);                                      // C(34,17)

        var first = firstTuples(product, 3);

        List<Object> expected0 = new ArrayList<>(List.of("A", "B"));
        expected0.addAll(firstSeventeen());
        assertEquals(expected0, first.get(0));
        // the right-most dimension moves first: [..., 0..15, 17]
        List<Object> expected1 = new ArrayList<>(List.of("A", "B"));
        expected1.addAll(IntStream.range(0, 16).boxed().toList());
        expected1.add(17);
        assertEquals(expected1, first.get(1));
    }

    @Test
    void hugeDimensionFirst() {
        var product = cartesianProduct.constrainedProductOfDistinct(17, THIRTY_FOUR)   // C(34,17)
                .andDistinct(2, A_B_C);                                                  // 3 combinations

        var first = firstTuples(product, 4);

        // the small right-most dimension cycles three times, then the huge one advances once
        for (int i = 0; i < 3; i++) {
            assertEquals(firstSeventeen(), first.get(i).subList(0, 17), "row " + i);
        }
        assertEquals(List.of("A", "B"), first.get(0).subList(17, 19));
        assertEquals(List.of("A", "C"), first.get(1).subList(17, 19));
        assertEquals(List.of("B", "C"), first.get(2).subList(17, 19));
        List<Object> secondOfHuge = new ArrayList<>(IntStream.range(0, 16).boxed().toList());
        secondOfHuge.add(17);
        assertEquals(secondOfHuge, first.get(3).subList(0, 17));
        assertEquals(List.of("A", "B"), first.get(3).subList(17, 19));
    }

    @Test
    void hugeDimensionInTheMiddle() {
        var product = cartesianProduct.constrainedProductOfDistinct(1, List.of("X", "Y"))
                .andDistinct(17, THIRTY_FOUR)
                .andMultiSelect(1, List.of("p", "q"));

        var first = firstTuples(product, 3);

        assertEquals("X", first.get(0).get(0));
        assertEquals("p", first.get(0).get(18));
        assertEquals("q", first.get(1).get(18));
        assertEquals(firstSeventeen(), first.get(0).subList(1, 18));
        assertEquals(first.get(0).subList(1, 18), first.get(1).subList(1, 18));
        assertNotEquals(first.get(0).subList(1, 18), first.get(2).subList(1, 18));   // the middle dimension moved
    }

    // ---- guards: exact order and iterator behaviour (pass now, must keep passing) -----------------

    @Test
    void lexOrderIsTheNestedLoopOrder() {
        var product = cartesianProduct.constrainedProductOfDistinct(1, List.of("X", "Y"))
                .andDistinct(2, A_B_C)
                .andInRange(0, 1, List.of(1, 2));

        List<List<Object>> expected = new ArrayList<>();
        for (String x : List.of("X", "Y"))
            for (List<String> pair : List.of(List.of("A", "B"), List.of("A", "C"), List.of("B", "C")))
                for (List<Integer> subset : List.of(List.<Integer>of(), List.of(1), List.of(2))) {
                    List<Object> t = new ArrayList<>(List.of(x));
                    t.addAll(pair);
                    t.addAll(subset);
                    expected.add(t);
                }

        assertEquals(expected, product.lexOrder().stream().toList());
        assertEquals(BigInteger.valueOf(expected.size()), product.count());
    }

    @Test
    void lexOrderAgreesWithByRanksForEveryRank() {
        var products = List.of(
                cartesianProduct.constrainedProductOfDistinct(2, A_B_C),
                cartesianProduct.constrainedProductOfDistinct(1, List.of("X", "Y")).andMultiSelect(2, A_B_C),
                cartesianProduct.constrainedProductOfInRange(0, 2, A_B_C).andDistinct(1, List.of("X", "Y")),
                cartesianProduct.constrainedProductOfDistinct(0, A_B_C).andDistinct(2, A_B_C).andMultiSelect(0, Collections.emptyList()),
                cartesianProduct.constrainedProductOfMultiSelect(2, List.of("X", "Y")).andDistinct(3, A_B_C).andInRange(0, 0, A_B_C));

        for (var product : products) {
            var lex = product.lexOrder().stream().toList();
            var ranks = IntStream.range(0, lex.size()).mapToObj(BigInteger::valueOf).toList();
            assertEquals(lex, product.byRanks(ranks).stream().toList(), product.toString());
            assertEquals(product.count().intValue(), lex.size(), product.toString());
        }
    }

    @Test
    void singleDimensionAndSingleTuplePaths() {
        assertEquals(List.of(tuple("A", "B"), tuple("A", "C"), tuple("B", "C")),
                cartesianProduct.constrainedProductOfDistinct(2, A_B_C).lexOrder().stream().toList());
        // every dimension selects nothing: exactly one, empty, tuple
        assertEquals(List.of(List.of()),
                cartesianProduct.constrainedProductOfDistinct(0, A_B_C).andMultiSelect(0, A_B_C).lexOrder().stream().toList());
        // an impossible dimension anywhere makes the product empty
        assertEquals(List.of(),
                cartesianProduct.constrainedProductOfDistinct(1, A_B_C).andDistinct(4, A_B_C).lexOrder().stream().toList());
    }

    @Test
    void iteratorsAreIndependentAndFailPastTheEnd() {
        var generator = cartesianProduct.constrainedProductOfDistinct(1, List.of("X", "Y"))
                .andDistinct(1, List.of(1, 2)).lexOrder();

        Iterator<List<Object>> it1 = generator.iterator();
        Iterator<List<Object>> it2 = generator.iterator();
        assertEquals(tuple("X", 1), it1.next());
        assertEquals(tuple("X", 2), it1.next());
        assertEquals(tuple("X", 1), it2.next());            // not advanced by it1

        assertEquals(tuple("Y", 1), it1.next());
        assertEquals(tuple("Y", 2), it1.next());
        assertFalse(it1.hasNext());
        assertThrows(NoSuchElementException.class, it1::next);
        assertEquals(4, generator.stream().count());         // a fresh traversal is complete
    }
}