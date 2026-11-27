package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Permutation;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The k-permutation lexOrder iterator advances its state in place; it must still match a plain recursive
 * enumeration, give independent iterators, and hand out lists that later steps do not change.
 */
class KPermutationLexOrderOracleTest {

    @Test
    void matchesRecursiveEnumerationForEveryNAndKUpToEight() {
        for (int n = 0; n <= 8; n++) {
            for (int k = 0; k <= n; k++) {
                List<List<Integer>> expected = new ArrayList<>();
                enumerate(n, k, new ArrayList<>(), new boolean[n], expected, Integer.MAX_VALUE);
                List<List<Integer>> actual = new ArrayList<>();
                for (List<Integer> p : Permutation.nPk(n, k).lexOrder()) actual.add(p);
                assertEquals(expected, actual, "n=" + n + " k=" + k);
            }
        }
    }

    @Test
    void firstThousandResultsOfLargerSizes() {
        for (int[] nk : new int[][]{{12, 4}, {20, 5}, {30, 2}, {10, 9}}) {
            List<List<Integer>> expected = new ArrayList<>();
            enumerate(nk[0], nk[1], new ArrayList<>(), new boolean[nk[0]], expected, 1000);
            List<List<Integer>> actual = new ArrayList<>();
            Iterator<List<Integer>> it = Permutation.nPk(nk[0], nk[1]).lexOrder().iterator();
            while (actual.size() < expected.size()) actual.add(it.next());
            assertEquals(expected, actual, "n=" + nk[0] + " k=" + nk[1]);
        }
    }

    @Test
    void iteratorsAreIndependentAndExhaustionIsReported() {
        var generator = Permutation.nPk(5, 3).lexOrder();
        Iterator<List<Integer>> a = generator.iterator();
        Iterator<List<Integer>> b = generator.iterator();
        a.next();
        a.next();
        assertEquals(List.of(0, 1, 2), b.next());

        int count = 0;
        Iterator<List<Integer>> c = generator.iterator();
        while (c.hasNext()) { c.next(); count++; }
        assertEquals(60, count);
        assertFalse(c.hasNext());
        assertThrows(NoSuchElementException.class, c::next);
    }

    @Test
    void returnedListsAreNotChangedByLaterSteps() {
        Iterator<List<Integer>> it = Permutation.nPk(5, 3).lexOrder().iterator();
        List<List<Integer>> collected = new ArrayList<>();
        while (it.hasNext()) collected.add(it.next());
        assertEquals(List.of(0, 1, 2), collected.get(0));
        assertEquals(List.of(0, 1, 3), collected.get(1));
        assertEquals(List.of(4, 3, 2), collected.get(collected.size() - 1));
    }

    private static void enumerate(int n, int k, List<Integer> current, boolean[] used, List<List<Integer>> out, int limit) {
        if (out.size() >= limit) return;
        if (current.size() == k) { out.add(new ArrayList<>(current)); return; }
        for (int i = 0; i < n && out.size() < limit; i++) {
            if (used[i]) continue;
            used[i] = true;
            current.add(i);
            enumerate(n, k, current, used, out, limit);
            current.remove(current.size() - 1);
            used[i] = false;
        }
    }
}
