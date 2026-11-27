package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.base.Permutation;
import io.github.deepeshpatel.jnumbertools.base.Combination;
import io.github.deepeshpatel.jnumbertools.base.Derangement;
import io.github.deepeshpatel.jnumbertools.base.Subset;

import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CartesianProductByRanks keeps one by-rank iterator per dimension and switches to BigInteger arithmetic when the
 * product does not fit in 62 bits. Every result must equal the straightforward reference: split the rank in mixed
 * radix and unrank each dimension separately.
 */
class CartesianProductByRanksTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Builder<Object>> dims(Builder<?>... builders) {
        return (List<Builder<Object>>) (List) List.of(builders);
    }

    @Test
    void smallProductsEveryRank() {
        check(dims(Combination.unique(5, 2), Combination.unique(4, 3)), true);
        check(dims(Permutation.unique(3), Derangement.of(4),
                Subset.of(4).inRange(1, 3)), true);
        check(dims(Permutation.unique(5)), true);
        check(dims(Permutation.repetitive(2, 3), Permutation.nPk(4, 2),
                Combination.repetitive(4, 2)), true);
    }

    @Test
    void productsAroundTheSixtyTwoBitLimit() {
        check(dims(Permutation.unique(20), Combination.unique(1, 1)), false); // 20! x 1
        check(dims(Permutation.unique(20), Combination.unique(2, 1)), false); // 20! x 2
        check(dims(Permutation.unique(25), Permutation.unique(25),
                Permutation.unique(25)), false);                                              // far beyond a long
    }

    @Test
    void secondIteratorStartsClean() {
        List<Builder<Object>> builders = dims(Permutation.unique(4), Combination.unique(5, 2));
        List<BigInteger> ranks = List.of(BigInteger.valueOf(7), BigInteger.valueOf(200), BigInteger.ZERO, BigInteger.valueOf(239));
        CartesianProductByRanks<Object> product = new CartesianProductByRanks<>(builders, ranks);
        List<List<Object>> first = new ArrayList<>();
        product.iterator().forEachRemaining(first::add);
        List<List<Object>> second = new ArrayList<>();
        product.iterator().forEachRemaining(second::add);
        assertEquals(first, second);
        assertEquals(4, first.size());
    }

    @Test
    void outOfRangeRankIsRejected() {
        List<Builder<Object>> builders = dims(Permutation.unique(3), Combination.unique(4, 2));
        BigInteger total = BigInteger.valueOf(6 * 6);
        assertThrows(IllegalArgumentException.class, () -> new CartesianProductByRanks<>(builders, List.of(total)).iterator().next());
        assertThrows(IllegalArgumentException.class,
                () -> new CartesianProductByRanks<>(builders, List.of(BigInteger.ONE.negate())).iterator().next());
    }

    private static void check(List<Builder<Object>> builders, boolean everyRank) {
        BigInteger total = BigInteger.ONE;
        for (Builder<Object> b : builders) total = total.multiply(b.count());
        List<BigInteger> ranks = new ArrayList<>();
        if (everyRank) {
            for (BigInteger i = BigInteger.ZERO; i.compareTo(total) < 0; i = i.add(BigInteger.ONE)) ranks.add(i);
        } else {
            Random random = new Random(9);
            ranks.add(BigInteger.ZERO);
            ranks.add(total.subtract(BigInteger.ONE));
            for (int i = 0; i < 300; i++) ranks.add(new BigInteger(total.bitLength() + 8, random).mod(total));
        }
        Iterator<List<Object>> actual = new CartesianProductByRanks<>(builders, ranks).iterator();
        for (BigInteger rank : ranks) {
            assertEquals(reference(builders, rank), actual.next(), "rank " + rank);
        }
    }

    private static List<Object> reference(List<Builder<Object>> builders, BigInteger rank) {
        Deque<Object> output = new ArrayDeque<>();
        BigInteger remaining = rank;
        for (int i = builders.size() - 1; i >= 0; i--) {
            Builder<Object> b = builders.get(i);
            BigInteger[] qr = remaining.divideAndRemainder(b.count());
            remaining = qr[0];
            List<Object> values = b.lexOrderMth(BigInteger.ONE, qr[1]).iterator().next();
            for (int j = values.size() - 1; j >= 0; j--) output.addFirst(values.get(j));
        }
        return List.copyOf(output);
    }
}
