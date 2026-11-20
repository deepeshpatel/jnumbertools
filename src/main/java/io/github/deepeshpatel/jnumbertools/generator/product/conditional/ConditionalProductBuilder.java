/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 * For all accepted rule sets, {@link #count()}, {@link #lexOrder()},
 * {@link #lexOrderMth(BigInteger, BigInteger)}, {@link #byRanks(Iterable)}, {@link #choice(int, Random)}
 * and {@link #sample(int, Random)} operate on valid tuples without materializing the complete Cartesian
 * product. {@link #choice(int, Random)} may use rejection sampling, which draws candidates from the full
 * product and discards those that violate a rule.
 * Ordinary {@link #lexOrder()} traversal defaults to {@link LexOrderStrategy#PRUNED_BITSET}; callers can select
 * {@link LexOrderStrategy#PRUNED_DFS} or {@link LexOrderStrategy#DP} explicitly, or use
 * {@link LexOrderStrategy#ADAPTIVE} with a non-negative estimate.
 * ADAPTIVE is an implementation policy, not a promise about which algorithm is selected for a given workload.
 * Its internal selection and selection criteria may change between versions. In this version it resolves to
 * {@link LexOrderStrategy#PRUNED_BITSET}; the estimate is validated but does not affect that choice.
 * Exact counting and rank-based operations use the DP plan, regardless of the lexicographic traversal strategy. Ranks are positions among the valid tuples in lexicographic order (dimension order,
 * then the order of each list). They change when rules change.</p>
 *
 * <h2>Practical performance guidance: dimension order</h2>
 * <p>Dimension declaration order can affect the amount of work required by lexicographic traversal.
 * The order is the order in which the dimensions are supplied to {@code conditionalProductOf(...)}
 * and {@link #and(List)}. It also defines the tuple positions and lexicographic encounter order, so
 * changing it changes the order (and positional meaning) of the generated tuples.</p>
 *
 * <p>During prefix-pruned traversal, a rule between two dimensions can be checked when the later of
 * those dimensions is assigned: the candidate value is tested against dimensions already present in
 * the prefix. A restrictive relation can therefore prune the remaining search below that point.
 * As a practical starting point:</p>
 * <ul>
 *   <li>Do not automatically put the most restrictive dimension first. First consider which other
 *       dimensions must already be assigned before its rules can be evaluated.</li>
 *   <li>Once the dimensions needed by those rules have been assigned, place a highly restrictive
 *       dimension before dimensions whose combinations it can eliminate. Avoid leaving a dimension
 *       that rejects many values until the very end, where little downstream work remains to prune.</li>
 *   <li>Consider the constraint graph as a whole. A dimension connected to several earlier dimensions
 *       may have more opportunities to reject candidates than one with only a single relation.</li>
 *   <li>Preserve the order required by your application: reordering dimensions changes tuple positions
 *       and lexicographic order, even when the set of valid combinations is unchanged.</li>
 * </ul>
 *
 * <p>This is analogous to join ordering and predicate placement in a SQL query: a selective condition
 * is valuable when it can be applied before large downstream combinations are produced, but its
 * dependencies must be available first. There is no universally optimal order; dimension sizes,
 * value distributions, rule dependencies and graph topology all matter. Benchmark representative
 * inputs when traversal performance is important.</p>
 *
 * <p>These ordering hints describe prefix-pruned lexicographic traversal. Exact counting and
 * rank-based operations use the counting plan and have different cost characteristics. The separate
 * {@link #anyOrder()} traversal does not preserve lexicographic order and prioritizes efficient execution;
 * it may benefit from partitioned execution when used with a parallel stream, but does not require or
 * guarantee parallel execution.</p>
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
    private final List<Predicate6> filters;

    /**
     * Predicate over a six-dimensional tuple. Each argument corresponds to a dimension
     * in the order in which it was supplied to {@code conditionalProductOf(...)} and {@code and(...)}.
     */
    @FunctionalInterface
    public interface Predicate6 {
        boolean test(Object first, Object second, Object third, Object fourth, Object fifth, Object sixth);
    }

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
        this(model, originals, List.of());
    }

    private ConditionalProductBuilder(
            ConstraintModel model, List<List<?>> originals, List<Predicate6> filters) {
        this.model = model;
        this.originals = originals;
        this.ruleCompiler = new RuleCompiler(model.dimensions, originals, model.indexes);
        this.filters = filters;
    }

    /**
     * Returns a <em>new</em> builder with {@code elements} appended as the next dimension.
     * @param elements the new dimension; copied. An empty list is allowed and makes the product empty.
     * @return new immutable builder
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder and(List<?> elements) {
        if (!filters.isEmpty()) {
            throw new IllegalStateException("All dimensions must be added before whole-tuple filters");
        }
        List<?> copy = snapshot(elements);
        List<List<?>> newOriginals = new ArrayList<>(originals);
        newOriginals.add(elements);
        return new ConditionalProductBuilder(model.withDimension(copy),
                Collections.unmodifiableList(newOriginals), filters);
    }

    /**
     * Adds a predicate over the complete six-dimensional tuple.
     *
     * <p>The predicate receives values in dimension declaration order. Multiple calls to
     * {@code filter} are combined with logical AND. Unlike pairwise rules configured through
     * {@link #when(List)}, an arbitrary predicate is evaluated only after a complete tuple has
     * been generated; it does not participate in constraint-plan pruning or dynamic-programming
     * counts.</p>
     *
     * @param predicate predicate that accepts a tuple when it returns {@code true}
     * @return a new builder with the predicate appended
     * @throws NullPointerException if {@code predicate} is null
     * @throws IllegalStateException if this product does not have exactly six dimensions
     */
    public ConditionalProductBuilder filter(Predicate6 predicate) {
        Objects.requireNonNull(predicate, "predicate");
        if (model.dimensions.size() != 6) {
            throw new IllegalStateException("A six-argument filter requires exactly six dimensions");
        }
        List<Predicate6> newFilters = new ArrayList<>(filters);
        newFilters.add(predicate);
        return new ConditionalProductBuilder(model, originals, Collections.unmodifiableList(newFilters));
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

    /**
     * Returns all valid tuples using a traversal that makes no encounter-order guarantee and
     * prioritizes efficient execution over lexicographic ordering.
     *
     * <p>Unlike {@link #lexOrder()}, this API does not guarantee lexicographic order. Its traversal
     * strategy is free to choose an efficient order of exploration. The returned spliterator can
     * partition work when the caller uses a parallel stream, but parallel execution is neither
     * required nor guaranteed; ordinary sequential iteration is also supported. The method itself
     * does not select or start parallel execution.</p>
     *
     * <p>The traversal is not randomized: use {@link #choice(int, Random)} or
     * {@link #sample(int, Random)} when random selection is required. The returned generator is
     * lazy and re-iterable, and produces every valid tuple exactly once without materializing the
     * complete result set.</p>
     *
     * @return a re-iterable generator of all valid tuples, with no encounter-order guarantee
     */
    public StreamableIterable<Object> anyOrder() {
        return applyFilters(ConditionalProductGenerators.anyOrder(model));
    }

    // Builder interface Methods
    /**
     * Returns the valid tuples in lexicographic order using the {@link LexOrderStrategy#PRUNED_BITSET} strategy.
     *
     * <p>This is the default and recommended strategy for ordinary lexicographic traversal. It checks constraints against the selected prefix and,
     * lazily enables feasibility lookahead when traversal work indicates that pairwise prefix pruning has
     * explored substantial dead-end work (more than 1,024 plus four times the tuples emitted so far; the trigger does
     * not depend on the constraint graph's shape). Lookahead never changes the tuples returned or their order.
     * It does not build exact completion counts.</p>
     *
     * <p>Use {@link #lexOrder(LexOrderStrategy)} to select {@link LexOrderStrategy#DP} explicitly, or
     * {@link #lexOrder(LexOrderStrategy, long)} to use the {@link LexOrderStrategy#ADAPTIVE} policy.
     * The lookahead threshold is an implementation choice and may change between versions.</p>
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
     *   <li>{@link LexOrderStrategy#PRUNED_BITSET}: performs prefix-based backtracking using compact
     *       relation rows and lazily enables feasibility lookahead after substantial dead-end work. It is
     *       the default strategy.</li>
     *   <li>{@link LexOrderStrategy#PRUNED_DFS}: uses the same prefix-pruned traversal and lazy
     *       work-triggered feasibility lookahead, with dense boolean relation rows when that
     *       representation fits the memory limit; otherwise
     *       it falls back to {@code PRUNED_BITSET}.</li>
     *   <li>{@link LexOrderStrategy#DP}: uses the feasibility tier to find the next valid value during
     *       sequential traversal. Its parallel spliterator uses exact completion counts to partition
     *       the valid tuples into contiguous rank ranges. Exact counting and rank-based operations also
     *       use the DP plan.</li>
     * </ul>
     *
     * <p>{@link LexOrderStrategy#ADAPTIVE} must be selected with
     * {@link #lexOrder(LexOrderStrategy, long)}. ADAPTIVE is an implementation policy: the API does not
     * guarantee which internal algorithm is used for any workload or which conditions drive that choice.
     * Those details may change between versions.
     * In this version it resolves to {@code PRUNED_BITSET}; the estimate is validated but does not affect
     * strategy selection.</p>
     *
     * <p>This setting affects only this lexicographic traversal. Without whole-tuple filters, exact-count
     * and rank-based operations use the DP plan. When a {@link #filter(Predicate6)} is configured, those
     * operations evaluate the filter over generated tuples because arbitrary predicates cannot be compiled
     * into the pairwise constraint plan.</p>
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
        return applyFilters(ConditionalProductGenerators.lexOrder(model, strategy));
    }

    /**
     * Returns the valid tuples in lexicographic order using the implementation policy exposed by
     * {@link LexOrderStrategy#ADAPTIVE}.
     *
     * <p>ADAPTIVE does not guarantee a particular internal algorithm or a mapping from workload
     * conditions to algorithms. Its selection policy may change between versions. In this version,
     * it resolves to {@code PRUNED_BITSET}; select {@link LexOrderStrategy#DP} explicitly when the DP
     * traversal is required.</p>
     *
     * <p>{@code estimatedLimit} is validated as non-negative but currently does not affect strategy
     * selection or limit the number of tuples produced.</p>
     *
     * @param strategy must be {@link LexOrderStrategy#ADAPTIVE}
     * @param estimatedLimit non-negative estimate; currently validated but not used for strategy selection
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
        return applyFilters(ConditionalProductGenerators.lexOrder(model, LexOrderStrategy.PRUNED_BITSET));
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
        return byRanks(new EveryMthIterable(start, m, count));
    }

    /**
     * {@inheritDoc}
     * <p>Only a null {@code ranks} is rejected up front. The ranks are consumed lazily and a rank that is
     * negative or not below {@code count()} is reported when it is reached.</p>
     */
    @Override
    public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        if (filters.isEmpty()) {
            return ConditionalProductGenerators.rankBased(model, ranks);
        }
        return filteredByRanks(ranks);
    }

    /**
     * {@inheritDoc}
     * <p>Uniform over the valid tuples, with replacement.</p>
     */
    @Override
    public StreamableIterable<Object> choice(int sampleSize, Random random) {
        BigIntegerChoice ranks = new BigIntegerChoice(count(), sampleSize, random); // validates the arguments
        if (!filters.isEmpty()) {
            return byRanks(ranks);
        }
        ConstraintPlan p = model.plan();
        if (p.counts().mostTuplesValid()) {
            return ConditionalProductGenerators.rejectionChoice(model, sampleSize, random);
        }
        return ConditionalProductGenerators.rankBased(model, ranks);
    }

    /**
     * {@inheritDoc}
     * <p>Uniform over the valid tuples, without replacement.</p>
     */
    @Override
    public StreamableIterable<Object> sample(int sampleSize, Random random) {
        return byRanks(new BigIntegerSample(count(), sampleSize, random));
    }

    /**
     * Number of valid tuples.
     *
     * <p>When no whole-tuple filters are configured, the count is computed from the rules without
     * enumerating the product. With filters, tuples are generated and tested because an arbitrary
     * six-argument predicate cannot be incorporated into the pairwise counting plan.</p>
     *
     * @return the exact count
     */
    @Override
    public BigInteger count() {
        if (filters.isEmpty()) {
            return model.plan().counts().total;
        }
        BigInteger result = BigInteger.ZERO;
        for (List<Object> ignored : lexOrder()) {
            result = result.add(BigInteger.ONE);
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return filters.isEmpty() ? model.plan().isEmpty() : !lexOrder().iterator().hasNext();
    }

    @Override
    public String toString() {
        return "ConditionalProductBuilder{dimensions=" + model.dimensions + ", linkedPairs=" + model.relations.size() + '}';
    }

    // ── Rule application ─────────────────────────────────────────────────────

    private ConditionalProductBuilder withRule(int triggerDim, BitSet triggerValues, int targetDim, BitSet allowedTargets) {
        return new ConditionalProductBuilder(
                model.withRule(triggerDim, triggerValues, targetDim, allowedTargets), originals, filters);
    }

    private boolean accepts(List<Object> tuple) {
        for (Predicate6 filter : filters) {
            if (!filter.test(tuple.get(0), tuple.get(1), tuple.get(2),
                    tuple.get(3), tuple.get(4), tuple.get(5))) {
                return false;
            }
        }
        return true;
    }

    private StreamableIterable<Object> applyFilters(StreamableIterable<Object> source) {
        if (filters.isEmpty()) {
            return source;
        }
        return new StreamableIterable<>() {
            @Override
            public java.util.Iterator<List<Object>> iterator() {
                java.util.Iterator<List<Object>> sourceIterator = source.iterator();
                return new java.util.Iterator<>() {
                    private List<Object> next;
                    private boolean ready;

                    @Override
                    public boolean hasNext() {
                        while (!ready && sourceIterator.hasNext()) {
                            List<Object> candidate = sourceIterator.next();
                            if (accepts(candidate)) {
                                next = candidate;
                                ready = true;
                            }
                        }
                        return ready;
                    }

                    @Override
                    public List<Object> next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        List<Object> result = next;
                        next = null;
                        ready = false;
                        return result;
                    }
                };
            }

            @Override
            public Stream<List<Object>> stream() {
                return StreamSupport.stream(spliterator(), false);
            }

            @Override
            public Spliterator<List<Object>> spliterator() {
                return Spliterators.spliteratorUnknownSize(iterator(),
                        Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
            }
        };
    }

    private StreamableIterable<Object> filteredByRanks(Iterable<BigInteger> ranks) {
        return new StreamableIterable<>() {
            @Override
            public java.util.Iterator<List<Object>> iterator() {
                java.util.Iterator<BigInteger> rankIterator = ranks.iterator();
                return new java.util.Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return rankIterator.hasNext();
                    }

                    @Override
                    public List<Object> next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        BigInteger requestedRank = rankIterator.next();
                        if (requestedRank == null || requestedRank.signum() < 0) {
                            throw new IllegalArgumentException("Rank must be non-negative: " + requestedRank);
                        }
                        BigInteger currentRank = BigInteger.ZERO;
                        java.util.Iterator<List<Object>> tuples = lexOrder().iterator();
                        while (tuples.hasNext()) {
                            List<Object> tuple = tuples.next();
                            if (currentRank.equals(requestedRank)) {
                                return tuple;
                            }
                            currentRank = currentRank.add(BigInteger.ONE);
                        }
                        throw new IllegalArgumentException(
                                "Rank " + requestedRank + " is out of bounds [0, " + currentRank + ")");
                    }
                };
            }

            @Override
            public Stream<List<Object>> stream() {
                return StreamSupport.stream(spliterator(), false);
            }

            @Override
            public Spliterator<List<Object>> spliterator() {
                return Spliterators.spliteratorUnknownSize(iterator(),
                        Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
            }
        };
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



}