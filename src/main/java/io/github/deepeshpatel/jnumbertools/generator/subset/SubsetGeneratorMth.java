/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates subsets of a set within a specified size range, producing only every mᵗʰ subset
 * (starting from a specified initial position) in lexicographical order (by increasing size,
 * then lexicographically within each size).
 *
 * <p><strong>Optimized, behaviour-preserving rewrite (BigInteger).</strong> The public surface
 * (constructor, {@code build()}, {@code iterator()}) and the relative-rank semantics are
 * unchanged. The arithmetic is the same {@code BigInteger}/{@code Calculator.nCr} as before; only
 * the algorithm around it was tightened:</p>
 * <ul>
 *   <li>The cumulative subset counts (size-block boundaries) are precomputed <em>once</em> as a
 *       prefix-sum table instead of being re-summed from {@code r = 0} on every {@code mth} call.
 *       Locating the block is an O(log n) binary search rather than an O(n) walk, and the range
 *       offset and iteration limit fall out of the same table.</li>
 *   <li>The combination unrank writes directly into an {@code int[]} via {@code Calculator.nCr},
 *       removing the per-call builder/generator/iterator pipeline and the Integer boxing.</li>
 * </ul>
 *
 * <p>(A primitive-{@code long} fast path for {@code n &le; 62} is intentionally omitted here; it
 * can be reintroduced once a dedicated test suite for it exists.)</p>
 *
 * <p><strong>Note:</strong> Constructed via {@link SubsetBuilder}; all parameter validation
 * (range, non-negative, m &gt; 0, start &lt; total) is performed there.</p>
 *
 * @param <T> the type of elements in the subsets
 * @author Deepesh Patel
 */
public final class SubsetGeneratorMth<T> extends AbstractGenerator<T> {

    private final BigInteger increment;
    private final Calculator calculator;
    private final int from;
    private final int to;
    private final int n;

    private final BigInteger[] pre;     // pre[k] = number of subsets of size < k, for k = 0..min(to,n)+1
    private final BigInteger initialM;  // absolute rank of the starting subset
    private final BigInteger limit;     // absolute rank one past the range's last subset
    private final BigInteger total;     // number of subsets in the range, i.e. the builder's count()

    SubsetGeneratorMth(int from, int to, BigInteger m, BigInteger start, List<T> elements, Calculator calculator) {
        super(elements);
        this.from = from;
        this.to = to;
        this.increment = m;
        this.calculator = calculator;
        this.n = elements.size();

        // Subsets larger than n do not exist, so the table stops at min(to, n). Besides saving work, this
        // keeps the table small for the empty set, where the builder accepts any to ≥ from, e.g.
        // inRange(0, Integer.MAX_VALUE) (a table of size to + 2 would overflow or exhaust memory).
        int top = Math.min(to, n);

        if (from > top) {
            // Only possible for the empty set with from > 0: no subsets at all (count = 0).
            pre = new BigInteger[]{BigInteger.ZERO};
            initialM = BigInteger.ZERO;
            limit = BigInteger.ZERO;
            total = BigInteger.ZERO;
        } else {
            // Prefix sums of binomials give the block boundaries, the range offset, and the limit:
            //   pre[from]    = #subsets of size < from  -> converts a relative rank to an absolute one
            //   pre[top + 1] = #subsets of size <= top  -> the iteration limit
            pre = new BigInteger[top + 2];
            pre[0] = BigInteger.ZERO;
            for (int i = 0; i <= top; i++) pre[i + 1] = pre[i].add(calculator.nCr(n, i));
            initialM = start.add(pre[from]);
            limit = pre[top + 1];
            total = limit.subtract(pre[from]);
        }
    }

    /**
     * Returns the subset at the starting position as a list of elements.
     *
     * @return the subset at {@code start} (within the configured range) in lexicographical order
     * @throws NoSuchElementException if the range contains no subset at that position (empty range)
     */
    public List<T> build() {
        if (initialM.compareTo(limit) >= 0) {
            throw new NoSuchElementException("The configured range has no subset at the requested position");
        }
        return indicesToValues(mth(initialM));
    }

    /** Number of subsets in the configured range (the builder's {@code count()}). */
    BigInteger count() {
        return total;
    }

    /**
     * Returns the subset at the given rank, relative to the start of the configured range.
     * <p>
     * Validates the rank itself, so it can be fed with user-supplied ranks one at a time.
     * </p>
     *
     * @param rank 0-based rank within the range, must satisfy {@code 0 ≤ rank < count()}
     * @return the subset at that rank, as an immutable list
     * @throws IllegalArgumentException if the rank is out of range (always the case when {@code count() == 0})
     */
    List<T> unrank(BigInteger rank) {
        if (rank.signum() < 0 || rank.compareTo(total) >= 0) {
            throw new IllegalArgumentException("Rank " + rank + " is out of bounds [0, " + total + ")");
        }
        return indicesToValues(mth(rank.add(pre[from])));
    }

    /** Absolute rank -> index array of the corresponding subset. */
    private int[] mth(BigInteger m) {
        int block = blockOf(m);                          // size of the subset
        return unrankLex(block, m.subtract(pre[block])); // offset within that size
    }

    /** Largest index b with pre[b] &lt;= m (the subset's size). */
    private int blockOf(BigInteger m) {
        int lo = 0, hi = pre.length - 1, ans = 0;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (pre[mid].compareTo(m) <= 0) { ans = mid; lo = mid + 1; } else hi = mid - 1;
        }
        return ans;
    }

    /** Unranks the {@code rank}-th r-combination of {0..n-1} in lexicographic order into an int[]. */
    private int[] unrankLex(int r, BigInteger rank) {
        int[] res = new int[r];
        int x = 0;
        for (int i = 0; i < r; i++) {
            while (true) {
                BigInteger c = calculator.nCr(n - 1 - x, r - 1 - i);
                //BigInteger c = nCrSafe(n - 1 - x, r - 1 - i);
                if (rank.compareTo(c) < 0) { res[i] = x++; break; }
                rank = rank.subtract(c); x++;
            }
        }
        return res;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    @Override
    public String toString() {
        return "SubsetGeneratorMth{from=" + from + ", to=" + to + ", increment=" + increment + "}";
    }

    private class Itr implements Iterator<List<T>> {
        private BigInteger m = initialM;

        @Override
        public boolean hasNext() {
            return m.compareTo(limit) < 0;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] indices = mth(m);
            m = m.add(increment);
            return indicesToValues(indices);
        }
    }
}