/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.datastructure.Tuple;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

/**
 * Builder for a Cartesian product whose dimensions are linked by <em>rules</em>, written as plain
 * English sentences:
 * <pre>{@code
 * var os      = List.of("Windows", "macOS", "Linux");
 * var browser = List.of("Chrome", "Safari", "Firefox");
 * var network = List.of("WiFi", "4G");
 *
 * var configs = JNumberTools.cartesianProduct()
 *         .conditionalProductOf(os)
 *         .and(browser)
 *         .and(network)
 *         .when(browser).is("Safari").then(os).isOneOf("macOS")
 *         .when(os).is("Linux").then(browser).isNoneOf("Chrome")
 *         .when(network).is("4G").then(os).isNoneOf("Linux");
 *
 * configs.count();                       // exact, without listing the tuples
 * configs.lexOrder().stream().forEach(System.out::println); // all valid tuples, in lexicographic order
 * configs.lexOrderMth(1000, 0);          // every 1000th valid tuple
 * configs.choice(5, new Random(42));     // uniform over the valid tuples
 * }</pre>
 *
 * <h2>Meaning of a rule</h2>
 * <p>{@code when(A).is(x).then(B).isOneOf(y, z)} means: whenever the value chosen from {@code A}
 * is {@code x}, the value chosen from {@code B} must be {@code y} or {@code z}. The rule does not say
 * anything about tuples where {@code A} is not {@code x}. All rules must hold together ("and").
 * {@code when(...)} and {@code then(...)} take the same {@link List} instance that was passed to
 * {@code conditionalProductOf} or {@code and} (matched by identity).</p>
 *
 * <h2>What is supported, and what is not</h2>
 * <p>Rules are evaluated on pairs of values, never on whole tuples. Dimensions linked by rules in a tree
 * shape are handled with a small table per dimension. Dimensions that rules link in a <em>loop</em> (for
 * example rules between A and B, B and C, and C and A) are solved together as one block, so the work for
 * such a block grows with the product of the sizes of its dimensions. A loop block is rejected if that
 * product exceeds 1,000,000 ({@code MAX_LOOP_COMBINATIONS}). Each pair of dimensions connected by a rule
 * also requires a relation table; a rule is rejected if that table would exceed 50,000,000 value pairs
 * ({@code MAX_PAIR_CELLS}).
 * For all accepted rule sets {@link #count()}, {@link #lexOrder()},
 * {@link #lexOrderMth(BigInteger, BigInteger)}, {@link #byRanks(Iterable)}, {@link #choice(int, Random)}
 * and {@link #sample(int, Random)} work on the valid tuples only, without enumerating the full product.
 * Ordinary {@link #lexOrder()} traversal defaults to {@link LexOrderStrategy#PRUNED_BITSET}; callers can select
 * {@link LexOrderStrategy#DP} or use {@link LexOrderStrategy#ADAPTIVE} with an expected traversal size.
 * The exact counting and rank-based operations always use the DP plan, regardless of the lexicographic
 * traversal strategy. Ranks are positions among the valid tuples in lexicographic order (dimension order,
 * then the order of each list). They change when rules change.</p>
 *
 * <p>Instances are immutable and thread-safe. Calls to {@code and(...)} and completed rule chains return
 * new builders; intermediate {@code when(...)} and {@code then(...)} steps do not modify the existing
 * builder.</p>
 *
 * <h2>Algorithm &amp; Complexity</h2>
 * <p>Internally, this builder models rule-constrained Cartesian products as a binary
 * <strong>Constraint Satisfaction Problem (CSP)</strong> and compiles the rules into an exact counting
 * and generation plan:</p>
 * <ul>
 *   <li><strong>Cycle Contraction &amp; Forest Decomposition</strong>
 *       <em>(related to Freuder 1982; Dechter &amp; Pearl 1987)</em>:
 *       Dimensions linked by rules form a constraint graph. Bridges are identified, and dimensions
 *       connected through non-bridge edges are contracted into disjoint blocks. The valid joint
 *       assignments of each block are enumerated once, subject to the
 *       {@code MAX_LOOP_COMBINATIONS} limit (1,000,000) on the product of the original dimension sizes in that
 *       block. The remaining bridge edges form a forest (of treewidth at most 1), enabling dynamic
 *       programming across the resulting trees.</li>
 *
 *   <li><strong>Bottom-Up Tree DP / Sum-Product Counting</strong>
 *       <em>(related to Pearl 1988; Dechter 1999)</em>:
 *       A post-order traversal computes exact subtree completion counts
 *       ({@code free} and {@code msg} tables). The forest DP takes
 *       <i>O</i>(<i>N</i> &middot; <i>D</i><sup>2</sup>) time, where <i>N</i> is the number of
 *       contracted nodes and <i>D</i> is the maximum number of states (valid joint assignments)
 *       in any contracted node. This bound excludes the separate enumeration of loop blocks.
 *       Consequently, {@link #count()} and {@link #isEmpty()} resolve without enumerating the full
 *       Cartesian product.</li>
 *
 *   <li><strong>Prefix-Conditioned Unranking &amp; Hybrid Sampling</strong>
 *       <em>(JNumberTools extensions)</em>:
 *       The implementation preserves the caller's exact dimension declaration order. Partially
 *       fixed loop blocks are queried using binary searches over sorted joint assignments and
 *       prefix-sum tables, accelerating exact prefix-conditioned completion counts and rank-based
 *       generation. When all intermediate counts fit within 62 bits, queries use primitive
 *       {@code long} tables before falling back to {@link BigInteger}.
 *       {@link #choice(int, Random)} dynamically selects between rejection sampling, when at least
 *       25% of the full product is valid, and rank-based DP unranking otherwise. In the rejection
 *       sampling case, the expected number of full-product draws per accepted tuple is at most four.</li>
 * </ul>
 *
 * <h3>References</h3>
 * <ul>
 *   <li>E. C. Freuder (1982), <em>A Sufficient Condition for Backtrack-Free Search</em>,
 *       Journal of the ACM, 29(1), 24&ndash;32.</li>
 *   <li>R. Dechter &amp; J. Pearl (1987), <em>Network-Based Heuristics for Constraint-Satisfaction
 *       Problems</em>, Artificial Intelligence, 34(1), 1&ndash;38.</li>
 *   <li>J. Pearl (1988), <em>Probabilistic Reasoning in Intelligent Systems: Networks of Plausible
 *       Inference</em>, Morgan Kaufmann.</li>
 *   <li>R. Dechter (1999), <em>Bucket Elimination: A Unifying Framework for Reasoning</em>,
 *       Artificial Intelligence, 113(1&ndash;2), 41&ndash;85.</li>
 * </ul>
 *
 * @author Deepesh Patel
 */
public final class ConditionalProductBuilder implements Builder<Object> {

    private final ConstraintModel model;         // immutable shared dimensions and pairwise relations
    private final List<List<?>> originals;       // the caller's list objects, matched by identity only
    private final RuleCompiler ruleCompiler;
    private volatile ConstraintPlan plan;                  // built lazily on first use


    /**
     * Creates a builder with a single dimension.
     * @param elements first dimension; copied. An empty list is allowed and makes the product empty.
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder(List<?> elements) {
        this(new ConstraintModel(List.<List<?>>of(snapshot(elements)), PersistentRelationMap.empty()),
                List.<List<?>>of(elements));
    }

    private ConditionalProductBuilder(ConstraintModel model, List<List<?>> originals) {
        this.model = model;
        this.originals = originals;
        this.ruleCompiler = new RuleCompiler(model.dimensions, originals);
    }

    /**
     * Returns a <em>new</em> builder with {@code elements} appended as the next dimension.
     * @param elements the new dimension; copied. An empty list is allowed and makes the product empty.
     * @return new immutable builder
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder and(List<?> elements) {
        List<?> copy = snapshot(elements);
        List<List<?>> newOriginals = new ArrayList<>(originals);
        newOriginals.add(elements);
        return new ConditionalProductBuilder(model.withDimension(copy),
                Collections.unmodifiableList(newOriginals));
    }

    //Rule Methods

    /**
     * Starts a rule: {@code when(A).is(x).then(B).isOneOf(y, z)}.
     *
     * @param dimension the same list instance that was passed to {@code conditionalProductOf} or
     *                  {@code and}
     * @return the next step of the rule
     * @throws NullPointerException     if {@code dimension} is null
     * @throws IllegalArgumentException if the list is not one of this product's dimensions, or the same
     *                                  list instance was used for more than one dimension
     */
    public WhenDimension when(List<?> dimension) {
        return new WhenDimension(ruleCompiler.indexOf(dimension));
    }

    /** First step of a rule: what the chosen value of the "when" dimension must be. */
    public final class WhenDimension {
        private final int dim;

        private WhenDimension(int dim) { this.dim = dim; }

        /** The chosen value equals {@code value}. */
        public Trigger is(Object value) { return trigger(List.of(nonNull(value)), false); }

        /** The chosen value is anything except {@code value}. */
        public Trigger isNot(Object value) { return trigger(List.of(nonNull(value)), true); }

        /** The chosen value is one of {@code values} (at least one is required). */
        public Trigger isOneOf(Object... values) { return trigger(asList(values), false); }

        /** The chosen value is one of {@code values} (at least one is required). */
        public Trigger isOneOf(Collection<?> values) { return trigger(values, false); }

        /** The chosen value is none of {@code values} (at least one is required). */
        public Trigger isNoneOf(Object... values) { return trigger(asList(values), true); }

        /** The chosen value is none of {@code values} (at least one is required). */
        public Trigger isNoneOf(Collection<?> values) { return trigger(values, true); }

        /** Alias of {@link #isNoneOf(Object...)}. */
        public Trigger isNotIn(Object... values) { return isNoneOf(values); }

        /** Alias of {@link #isNoneOf(Collection)}. */
        public Trigger isNotIn(Collection<?> values) { return isNoneOf(values); }

        private Trigger trigger(Collection<?> values, boolean negate) {
            return new Trigger(dim, ruleCompiler.matching(dim, values, negate));
        }
    }

    /** Second step of a rule: the condition is known, now name the dimension that is restricted. */
    public final class Trigger {
        private final int dim;
        private final BitSet values;

        private Trigger(int dim, BitSet values) {
            this.dim = dim;
            this.values = values;
        }

        /**
         * Names the restricted dimension.
         *
         * @param dimension the same list instance that was passed to {@code conditionalProductOf} or
         *                  {@code and}; must be a different dimension from the one in {@code when}
         * @return the final step of the rule
         * @throws NullPointerException     if {@code dimension} is null
         * @throws IllegalArgumentException if the list is not a dimension of this product, is ambiguous, or
         *                                  is the dimension used in {@code when}
         */
        public ThenDimension then(List<?> dimension) {
            int target = ruleCompiler.indexOf(dimension);
            if (target == dim) {
                throw new IllegalArgumentException("A rule needs two different dimensions");
            }
            return new ThenDimension(dim, values, target);
        }
    }

    /** Final step of a rule: what the restricted dimension may be. Returns the new builder. */
    public final class ThenDimension {
        private final int triggerDim;
        private final BitSet triggerValues;
        private final int targetDim;

        private ThenDimension(int triggerDim, BitSet triggerValues, int targetDim) {
            this.triggerDim = triggerDim;
            this.triggerValues = triggerValues;
            this.targetDim = targetDim;
        }

        /** The value must equal {@code value}. */
        public ConditionalProductBuilder is(Object value) { return apply(List.of(nonNull(value)), false); }

        /** The value must be anything except {@code value}. */
        public ConditionalProductBuilder isNot(Object value) { return apply(List.of(nonNull(value)), true); }

        /** The value must be one of {@code values} (at least one is required). */
        public ConditionalProductBuilder isOneOf(Object... values) { return apply(asList(values), false); }

        /** The value must be one of {@code values} (at least one is required). */
        public ConditionalProductBuilder isOneOf(Collection<?> values) { return apply(values, false); }

        /** The value must be none of {@code values} (at least one is required). */
        public ConditionalProductBuilder isNoneOf(Object... values) { return apply(asList(values), true); }

        /** The value must be none of {@code values} (at least one is required). */
        public ConditionalProductBuilder isNoneOf(Collection<?> values) { return apply(values, true); }

        /** Alias of {@link #isNoneOf(Object...)}. */
        public ConditionalProductBuilder isNotIn(Object... values) { return isNoneOf(values); }

        /** Alias of {@link #isNoneOf(Collection)}. */
        public ConditionalProductBuilder isNotIn(Collection<?> values) { return isNoneOf(values); }

        private ConditionalProductBuilder apply(Collection<?> values, boolean negate) {
            return withRule(triggerDim, triggerValues, targetDim, ruleCompiler.matching(targetDim, values, negate));
        }
    }

    // Builder interface Methods
    /**
     * Returns the valid tuples in lexicographic order using the {@link LexOrderStrategy#PRUNED_BITSET} strategy.
     *
     * <p>This is the default and recommended strategy for ordinary traversal, especially when the caller
     * expects to consume only part of the result. It checks constraints against the already selected prefix
     * and does not build the exact counting plan.</p>
     *
     * <p>Use {@link #lexOrder(LexOrderStrategy)} to select {@link LexOrderStrategy#DP} explicitly, or
     * {@link #lexOrder(LexOrderStrategy, long)} to let {@link LexOrderStrategy#ADAPTIVE} choose based on
     * an expected traversal size.</p>
     *
     * @return a re-iterable generator of valid tuples in lexicographic order
     */
    @Override
    public StreamableIterable<Object> lexOrder() {
        return lexOrder(LexOrderStrategy.PRUNED_BITSET);
    }

    /**
     * Returns the valid tuples in lexicographic order using the specified traversal strategy.
     *
     * <ul>
     *   <li>{@link LexOrderStrategy#PRUNED_BITSET}: performs prefix-based backtracking and checks only the
     *       constraints between the candidate dimension and dimensions already selected. It avoids
     *       constructing the counting/DP plan and is the default strategy.</li>
     *   <li>{@link LexOrderStrategy#DP}: uses the exact counting plan. Its completion counts allow it to
     *       skip prefixes that cannot lead to a valid tuple. This can be useful when a substantial
     *       fraction of a large result is expected to be traversed, but plan construction has an upfront
     *       cost.</li>
     * </ul>
     *
     * <p>{@link LexOrderStrategy#ADAPTIVE} requires an expected traversal size and must be selected with
     * {@link #lexOrder(LexOrderStrategy, long)}.</p>
     *
     * <p>This setting affects only this lexicographic traversal. Exact-count and rank-based operations,
     * including {@link #count()}, {@link #lexOrderMth(BigInteger, BigInteger)}, {@link #byRanks(Iterable)},
     * {@link #choice(int, Random)} and {@link #sample(int, Random)}, continue to use the DP plan.</p>
     *
     * @param strategy traversal strategy; must not be {@code null} or {@link LexOrderStrategy#ADAPTIVE}
     * @return a re-iterable generator of valid tuples in lexicographic order
     * @throws NullPointerException if {@code strategy} is null
     * @throws IllegalArgumentException if {@code strategy} is {@code ADAPTIVE}
     */
    public StreamableIterable<Object> lexOrder(LexOrderStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy");
        if (strategy == LexOrderStrategy.ADAPTIVE) {
            throw new IllegalArgumentException("ADAPTIVE requires an estimated traversal limit; use lexOrder(strategy, estimatedLimit)");
        }
        return new LexOrderGenerator(strategy);
    }

    /**
     * Returns the valid tuples in lexicographic order using the adaptive strategy.
     *
     * <p>{@code estimatedLimit} is the expected number of valid tuples the caller intends to consume
     * from one traversal. The current adaptive policy selects {@link LexOrderStrategy#PRUNED_BITSET} for
     * estimates below 10,000 tuples and {@link LexOrderStrategy#DP} for estimates of 10,000 tuples or
     * more. The cutoff is a conservative heuristic, not a performance guarantee; workload shape,
     * constraint density and dimension sizes can change the crossover. Benchmark representative data
     * before relying on the adaptive choice for latency-sensitive workloads.</p>
     *
     * <p>The estimate does not limit the number of tuples produced. It is used only to select the
     * traversal implementation. The returned iterable remains lazy and re-iterable.</p>
     *
     * @param strategy must be {@link LexOrderStrategy#ADAPTIVE}
     * @param estimatedLimit expected number of tuples to consume in one traversal; must be non-negative
     * @return a re-iterable generator of valid tuples in lexicographic order
     * @throws NullPointerException if {@code strategy} is null
     * @throws IllegalArgumentException if the strategy is not {@code ADAPTIVE} or the estimate is negative
     */
    public StreamableIterable<Object> lexOrder(LexOrderStrategy strategy, long estimatedLimit) {
        Objects.requireNonNull(strategy, "strategy");
        if (strategy != LexOrderStrategy.ADAPTIVE) {
            throw new IllegalArgumentException("An estimated traversal limit is supported only with ADAPTIVE");
        }
        if (estimatedLimit < 0) {
            throw new IllegalArgumentException("estimatedLimit must be non-negative");
        }
        LexOrderStrategy selected = estimatedLimit >= 10_000L
                ? LexOrderStrategy.DP
                : LexOrderStrategy.PRUNED_BITSET;
        return new LexOrderGenerator(selected);
    }

    /**
     * {@inheritDoc}
     * <p>Ranks are positions among the valid tuples. If the product is empty ({@code count() == 0}) any
     * {@code start ≥ 0} is accepted and the generator yields nothing.</p>
     */
    @Override
    public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger count = count();
        Util.validateLexOrderMthParams(m, start, count);
        return new RankBasedGenerator(new EveryMthIterable(start, m, count));
    }

    /**
     * {@inheritDoc}
     * <p>Only a null {@code ranks} is rejected up front. The ranks are consumed lazily and a rank that is
     * negative or not below {@code count()} is reported when it is reached.</p>
     */
    @Override
    public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new RankBasedGenerator(ranks);
    }

    /**
     * {@inheritDoc}
     * <p>Uniform over the valid tuples, with replacement.</p>
     */
    @Override
    public StreamableIterable<Object> choice(int sampleSize, Random random) {
        BigIntegerChoice ranks = new BigIntegerChoice(count(), sampleSize, random); // validates the arguments
        ConstraintPlan p = plan();
        if (p.counts.mostTuplesValid()) {
            return new RejectionChoiceGenerator(sampleSize, random);
        }
        return new RankBasedGenerator(ranks);
    }

    /**
     * {@inheritDoc}
     * <p>Uniform over the valid tuples, without replacement.</p>
     */
    @Override
    public StreamableIterable<Object> sample(int sampleSize, Random random) {
        return new RankBasedGenerator(new BigIntegerSample(count(), sampleSize, random));
    }

    /**
     * Number of valid tuples, computed from the rules without enumerating the product.
     * @return the exact count
     */
    @Override
    public BigInteger count() {
        return plan().counts.total;
    }

    @Override
    public boolean isEmpty() {
        return count().signum() == 0;
    }

    @Override
    public String toString() {
        return "ConditionalProductBuilder{dimensions=" + model.dimensions + ", linkedPairs=" + model.relations.size() + '}';
    }

    // ── Rule application ─────────────────────────────────────────────────────

    private ConditionalProductBuilder withRule(int triggerDim, BitSet triggerValues, int targetDim, BitSet allowedTargets) {
        return new ConditionalProductBuilder(
                model.withRule(triggerDim, triggerValues, targetDim, allowedTargets), originals);
    }

    private static Object nonNull(Object value) {
        return Objects.requireNonNull(value, "a rule value must not be null");
    }

    private static List<Object> asList(Object[] values) {
        Objects.requireNonNull(values, "values must not be null");
        return Arrays.asList(values);
    }

    private static List<?> snapshot(List<?> elements) {
        Util.validateInput(elements);
        return Util.copyOfNoNulls(elements);
    }

    // ── Rule tables ──────────────────────────────────────────────────────────

    /** Allowed value pairs between dimension u and dimension v (u &lt; v). Immutable once built. */
    // ── Counting plan ────────────────────────────────────────────────────────

    private ConstraintPlan plan() {
        ConstraintPlan p = plan;
        if (p == null) {
            synchronized (this) {
                p = plan;
                if (p == null) {
                    p = new ConstraintPlan(model.dimensions, model.relations);
                    plan = p;
                }
            }
        }
        return p;
    }


    // ── Generators

    private List<Object> tuple(int[] val) {
        Object[] result = new Object[val.length];
        for (int i = 0; i < val.length; i++) {
            result[i] = model.dimensions.get(i).get(val[i]);
        }
        return Tuple.wrap(result);
    }

    private final class LexOrderGenerator extends AbstractGenerator<Object> {
        private final LexOrderStrategy strategy;

        LexOrderGenerator(LexOrderStrategy strategy) {
            super(Collections.emptyList());
            this.strategy = strategy;
        }

        @Override
        public Iterator<List<Object>> iterator() {
            return strategy == LexOrderStrategy.PRUNED_BITSET
                    ? new PrunedLexItr()
                    : new LexItr(plan());
        }
    }

    /**
     * Lexicographic depth-first traversal without the counting plan. At each dimension, candidates are
     * filtered against all earlier dimensions connected to it by a rule. A prefix that has no completion
     * may still be explored until the conflicting later dimension is reached; unlike the DP iterator, this
     * iterator does not perform future-completion lookahead.
     */
    private final class PrunedLexItr implements Iterator<List<Object>> {
        private final int dimensions = model.dimensions.size();
        private final int[] val = new int[dimensions];
        private final int[] nextCandidate = new int[dimensions];
        private final int[][] belowDims;
        private final BitSet[][][] belowRows;
        private int depth;
        private boolean ready;
        private boolean exhausted;

        PrunedLexItr() {
            ConstraintModel.Rows rows = model.rows();
            belowDims = rows.belowDims;
            belowRows = rows.belowRows;
            for (List<?> dimension : model.dimensions) {
                if (dimension.isEmpty()) {
                    exhausted = true;
                    break;
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (!ready && !exhausted) {
                ready = advance();
                if (!ready) exhausted = true;
            }
            return ready;
        }

        @Override
        public List<Object> next() {
            if (!hasNext()) throw new NoSuchElementException();
            ready = false;
            return tuple(val);
        }

        private boolean advance() {
            while (depth >= 0) {
                int candidate = nextValidCandidate(depth, nextCandidate[depth]);
                if (candidate < 0) {
                    if (depth == 0) return false;
                    depth--;
                    nextCandidate[depth] = val[depth] + 1;
                    continue;
                }

                val[depth] = candidate;
                nextCandidate[depth] = candidate + 1;
                if (depth == dimensions - 1) return true;

                depth++;
                nextCandidate[depth] = 0;
            }
            return false;
        }

        private int nextValidCandidate(int dimension, int from) {
            int size = model.dimensions.get(dimension).size();
            if (from >= size) return -1;

            int[] earlier = belowDims[dimension];
            if (earlier.length == 0) return from;

            BitSet[][] rows = belowRows[dimension];
            BitSet firstAllowed = rows[0][val[earlier[0]]];
            candidateLoop:
            for (int candidate = firstAllowed.nextSetBit(from);
                 candidate >= 0 && candidate < size;
                 candidate = firstAllowed.nextSetBit(candidate + 1)) {
                for (int i = 1; i < earlier.length; i++) {
                    if (!rows[i][val[earlier[i]]].get(candidate)) continue candidateLoop;
                }
                return candidate;
            }
            return -1;
        }
    }

    /** Walks the valid tuples in lexicographic order; branches without any valid completion are skipped. */
    private final class LexItr implements Iterator<List<Object>> {
        private final ConstraintPlan p;
        private final int[] val;
        private boolean started;
        private boolean ready;
        private boolean exhausted;

        LexItr(ConstraintPlan p) {
            this.p = p;
            this.val = new int[p.dimensions];
            this.exhausted = p.counts.total.signum() == 0;
        }

        @Override
        public boolean hasNext() {
            if (!ready && !exhausted) {
                if (advance()) {
                    ready = true;
                } else {
                    exhausted = true;
                }
            }
            return ready;
        }

        @Override
        public List<Object> next() {
            if (!hasNext()) throw new NoSuchElementException();
            ready = false;
            return tuple(val);
        }

        private boolean advance() {
            int k;
            int from;
            if (!started) {
                started = true;
                k = 0;
                from = 0;
            } else {
                k = p.dimensions - 1;
                from = val[k] + 1;
            }
            while (true) {
                int idx = p.firstValid(k, from, val);
                if (idx < 0) {
                    k--;
                    if (k < 0) return false;
                    from = val[k] + 1;
                } else {
                    val[k] = idx;
                    k++;
                    if (k == p.dimensions) return true;
                    from = 0;
                }
            }
        }
    }

    /**
     * Uniform choice with replacement by rejection: draw a random point of the full product and keep it if it
     * satisfies every rule. Exact (each valid tuple is equally likely) and used only when at least a quarter
     * of the product is valid, so a draw is accepted after four attempts on average.
     */
    private final class RejectionChoiceGenerator extends AbstractGenerator<Object> {
        private final int sampleSize;
        private final Random random;

        RejectionChoiceGenerator(int sampleSize, Random random) {
            super(Collections.emptyList());
            this.sampleSize = sampleSize;
            this.random = random;
        }

        @Override
        public Iterator<List<Object>> iterator() {
            ConstraintPlan p = plan();
            return new Iterator<>() {
                private int produced;

                @Override public boolean hasNext() { return produced < sampleSize; }

                @Override
                public List<Object> next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    produced++;
                    int[] val = new int[p.dimensions];
                    do {
                        for (int d = 0; d < val.length; d++) val[d] = random.nextInt(p.dimSize[d]);
                    } while (!p.isValid(val));
                    return tuple(val);
                }
            };
        }
    }

    /** Resolves each rank to a tuple through the counting tables. Ranks are consumed lazily. */
    private final class RankBasedGenerator extends AbstractGenerator<Object> {
        private final Iterable<BigInteger> ranks;

        RankBasedGenerator(Iterable<BigInteger> ranks) {
            super(Collections.emptyList());
            this.ranks = ranks;
        }

        @Override
        public Iterator<List<Object>> iterator() {
            Iterator<BigInteger> rankIt = ranks.iterator();
            return new Iterator<>() {
                @Override public boolean hasNext() { return rankIt.hasNext(); }

                @Override
                public List<Object> next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    BigInteger rank = rankIt.next();
                    ConstraintPlan p = plan();
                    if (rank.signum() < 0 || rank.compareTo(p.counts.total) >= 0) {
                        throw new IllegalArgumentException(
                                "Rank " + rank + " is out of bounds [0, " + p.counts.total + ")");
                    }
                    return tuple(p.counts.unrank(rank));
                }
            };
        }
    }
}