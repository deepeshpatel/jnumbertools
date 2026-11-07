/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.datastructure;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Thread-safe cache for requested binomial coefficients, grouped by {@code n}.
 * <p>
 * For each {@code n}, only explicitly requested coefficients are retained. When a new
 * coefficient is needed, its value is calculated from the closest cached coefficient
 * with a smaller {@code r}, if one exists. Intermediate coefficients are not cached.
 * Requesting the coefficients of one row in ascending {@code r} therefore costs one
 * multiplication and one division per step instead of a full recomputation each time.
 * </p>
 * <p>
 * The canonical range {@code 0 <= r <= n / 2} is expected; callers should apply
 * {@code min(r, n - r)} before accessing this cache.
 * </p>
 */
public final class BinomialCoefficientMap {

    private final ConcurrentHashMap<Integer, BinomialCoefficientRow> rows = new ConcurrentHashMap<>();

    /**
     * Returns the requested coefficient, computing and caching it when absent.
     *
     * @param n the total number of items
     * @param r the canonical number of items to choose
     * @return the binomial coefficient {@code nCr(n, r)}
     */
    public BigInteger computeIfAbsent(int n, int r) {
        return rows.computeIfAbsent(n, ignored -> new BinomialCoefficientRow())
                .computeIfAbsent(n, r);
    }

    /** Clears all cached binomial coefficients. */
    public void clear() {
        rows.clear();
    }

    private static final class BinomialCoefficientRow {

        private final ConcurrentSkipListMap<Integer, BigInteger> coefficients = new ConcurrentSkipListMap<>();

        private BigInteger computeIfAbsent(int n, int requestedR) {
            // The mapping function is pure, so it is harmless if it runs more than once under contention.
            return coefficients.computeIfAbsent(requestedR, r -> {
                Map.Entry<Integer, BigInteger> lower = coefficients.lowerEntry(requestedR);

                int startR;
                BigInteger coefficient;
                if (lower == null) {
                    startR = 0;
                    coefficient = BigInteger.ONE; // C(n, 0)
                } else {
                    startR = lower.getKey();
                    coefficient = lower.getValue();
                }

                // C(n, i + 1) = C(n, i) * (n - i) / (i + 1). Every division is exact;
                // only the requested endpoint is retained.
                for (int i = startR; i < requestedR; i++) {
                    coefficient = coefficient.multiply(BigInteger.valueOf(n - i))
                            .divide(BigInteger.valueOf(i + 1L));
                }
                return coefficient;
            });
        }
    }
}
