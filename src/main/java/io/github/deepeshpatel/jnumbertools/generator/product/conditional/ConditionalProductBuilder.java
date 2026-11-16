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
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

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

    /** Largest number of value pairs one pair of dimensions may have (guards memory of the rule tables). */
    private static final long MAX_PAIR_CELLS = 50_000_000L;

    /**
     * Largest product of the sizes of the dimensions that rules link in a loop (they are solved together,
     * so this bounds the work and memory for such a group).
     */
    private static final long MAX_LOOP_COMBINATIONS = 1_000_000L;

    private final ConstraintModel model;         // immutable shared dimensions and pairwise relations
    private final List<List<?>> dims;            // private snapshots; unmodifiable (facade transition alias)
    private final List<List<?>> originals;       // the caller's list objects, matched by identity only
    private final PersistentRelationMap relations; // persistent AVL map; key(u, v) with u < v (facade transition alias)
    private volatile ConstraintPlan plan;                  // built lazily on first use


    /**
     * Creates a builder with a single dimension.
     * @param elements first dimension; copied. An empty list is allowed and makes the product empty.
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder(List<?> elements) {
        this(List.<List<?>>of(snapshot(elements)), List.<List<?>>of(elements), PersistentRelationMap.empty());
    }

    private ConditionalProductBuilder(List<List<?>> dims, List<List<?>> originals, PersistentRelationMap relations) {
        this.model = new ConstraintModel(dims, relations);
        this.dims = model.dimensions;
        this.originals = originals;
        this.relations = (PersistentRelationMap) model.relations;
    }

    /**
     * Returns a <em>new</em> builder with {@code elements} appended as the next dimension.
     * @param elements the new dimension; copied. An empty list is allowed and makes the product empty.
     * @return new immutable builder
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder and(List<?> elements) {
        List<?> copy = snapshot(elements);
        List<List<?>> newDims = new ArrayList<>(dims);
        newDims.add(copy);
        List<List<?>> newOriginals = new ArrayList<>(originals);
        newOriginals.add(elements);
        return new ConditionalProductBuilder(
                Collections.unmodifiableList(newDims), Collections.unmodifiableList(newOriginals), relations);
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
        return new WhenDimension(indexOf(dimension));
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
            return new Trigger(dim, matching(dim, values, negate));
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
            int target = indexOf(dimension);
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
            return withRule(triggerDim, triggerValues, targetDim, matching(targetDim, values, negate));
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
        if (p.mostTuplesValid()) {
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
        return plan().total;
    }

    @Override
    public boolean isEmpty() {
        return count().signum() == 0;
    }

    @Override
    public String toString() {
        return "ConditionalProductBuilder{dimensions=" + dims + ", linkedPairs=" + relations.size() + '}';
    }

    // ── Rule application ─────────────────────────────────────────────────────

    private ConditionalProductBuilder withRule(int triggerDim, BitSet triggerValues, int targetDim, BitSet allowedTargets) {
        int u = Math.min(triggerDim, targetDim);
        int v = Math.max(triggerDim, targetDim);
        long key = key(u, v);
        Relation relation = relations.get(key);
        boolean newLinkedPair = relation == null;
        if (newLinkedPair) {
            long cells = (long) dims.get(u).size() * dims.get(v).size();
            if (cells > MAX_PAIR_CELLS) {
                throw new IllegalArgumentException("The two dimensions have too many value pairs (" + cells
                        + ") for a rule; the limit is " + MAX_PAIR_CELLS);
            }
            relation = Relation.full(dims.get(u).size(), dims.get(v).size());
        }
        Relation updated = relation.restrict(triggerValues, allowedTargets, triggerDim == u);
        // Path-copy only the AVL search path. Adding another rule to an existing pair is O(log E)
        // rather than copying all E relation entries; older builders retain their previous root.
        PersistentRelationMap updatedRelations = relations.with(key, updated);
        // A new graph edge can change loop blocks; another restriction on an existing edge cannot.
        if (newLinkedPair) {
            checkLoopBlocks(dims, updatedRelations.keySet());
        }
        return new ConditionalProductBuilder(dims, originals, updatedRelations);
    }

    /** Rejects a rule set in which a group of dimensions linked in loops has too many combinations. */
    private static void checkLoopBlocks(List<List<?>> dims, Collection<Long> keys) {
        int[] block = blockOf(dims.size(), keys);
        long[] product = new long[dims.size()];
        int[] members = new int[dims.size()];
        Arrays.fill(product, 1L);
        for (int d = 0; d < dims.size(); d++) {
            int b = block[d];
            members[b]++;
            long size = dims.get(d).size();
            if (size == 0) {
                product[b] = 0;
            } else if (product[b] > MAX_LOOP_COMBINATIONS / size) {
                product[b] = MAX_LOOP_COMBINATIONS + 1;
            } else {
                product[b] *= size;
            }
        }
        for (int b = 0; b < members.length; b++) {
            if (members[b] > 1 && product[b] > MAX_LOOP_COMBINATIONS) {
                throw new IllegalArgumentException("These rules link dimensions in a loop whose dimensions have "
                        + "too many value combinations together; the limit is " + MAX_LOOP_COMBINATIONS);
            }
        }
    }

    /**
     * Groups the dimensions: two dimensions are in the same block when rules link them through a loop
     * (an edge that lies on a cycle). Dimensions linked only by single "bridge" rules stay apart, so the
     * blocks and the bridges between them form a forest. Returns, per dimension, the lowest dimension
     * index of its block.
     */
    static int[] blockOf(int n, Collection<Long> keys) {
        List<List<Integer>> adjacency = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adjacency.add(new ArrayList<>());
        }
        for (long edge : keys) {
            int u = (int) (edge >>> 32);
            int v = (int) edge;
            adjacency.get(u).add(v);
            adjacency.get(v).add(u);
        }

        int[] discovery = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        int[] nextEdge = new int[n];
        int[] time = {0};
        Arrays.fill(parent, -1);
        Set<Long> bridges = new HashSet<>();
        for (int vertex = 0; vertex < n; vertex++) {
            if (discovery[vertex] == 0) {
                findBridges(vertex, adjacency, discovery, low, parent, nextEdge, time, bridges);
            }
        }

        // A dimension belongs to a loop block exactly when it is connected through non-bridge edges.
        int[] block = new int[n];
        for (int i = 0; i < n; i++) {
            block[i] = i;
        }
        for (long edge : keys) {
            if (!bridges.contains(edge)) {
                union(block, (int) (edge >>> 32), (int) edge);
            }
        }
        for (int i = 0; i < n; i++) {
            block[i] = find(block, i);
        }
        return block;
    }

    /**
     * Tarjan's depth-first bridge search. The constraint graph has at most one edge per dimension pair,
     * because {@code relations} stores one combined relation for each pair.
     */
    private static void findBridges(int root, List<List<Integer>> adjacency,
                                    int[] discovery, int[] low, int[] parent, int[] nextEdge,
                                    int[] time, Set<Long> bridges) {
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        discovery[root] = low[root] = ++time[0];
        stack.push(root);

        // Iterative DFS avoids making graph depth a Java call-stack limit.
        while (!stack.isEmpty()) {
            int vertex = stack.peek();
            List<Integer> neighbors = adjacency.get(vertex);
            if (nextEdge[vertex] < neighbors.size()) {
                int next = neighbors.get(nextEdge[vertex]++);
                if (next == parent[vertex]) {
                    continue;
                }
                if (discovery[next] == 0) {
                    parent[next] = vertex;
                    discovery[next] = low[next] = ++time[0];
                    stack.push(next);
                } else {
                    low[vertex] = Math.min(low[vertex], discovery[next]);
                }
            } else {
                stack.pop();
                int predecessor = parent[vertex];
                if (predecessor >= 0) {
                    low[predecessor] = Math.min(low[predecessor], low[vertex]);
                    if (low[vertex] > discovery[predecessor]) {
                        bridges.add(key(Math.min(vertex, predecessor), Math.max(vertex, predecessor)));
                    }
                }
            }
        }
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a);
        int rb = find(parent, b);
        if (ra < rb) parent[rb] = ra; else if (rb < ra) parent[ra] = rb;
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    static long key(int u, int v) {
        return ((long) u << 32) | v;
    }

    private int indexOf(List<?> dimension) {
        Objects.requireNonNull(dimension, "dimension must not be null");
        int found = -1;
        for (int i = 0; i < originals.size(); i++) {
            if (originals.get(i) == dimension) {
                if (found >= 0) {
                    throw new IllegalArgumentException("The same list instance is used for more than one "
                            + "dimension (positions " + found + " and " + i + "), so the rule is ambiguous; "
                            + "pass distinct list instances");
                }
                found = i;
            }
        }
        if (found < 0) {
            throw new IllegalArgumentException("The list is not a dimension of this product; pass the same "
                    + "List instance that was given to conditionalProductOf(...) or and(...)");
        }
        return found;
    }

    /** Indexes of the values of dimension {@code dim} that equal one of {@code values} (or the others). */
    private BitSet matching(int dim, Collection<?> values, boolean negate) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required");
        }
        Set<Object> wanted = new HashSet<>();
        for (Object value : values) {
            wanted.add(nonNull(value));
        }
        List<?> list = dims.get(dim);
        BitSet result = new BitSet(list.size());
        Set<Object> seen = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            if (wanted.contains(element)) {
                result.set(i);
                seen.add(element);
            }
        }
        if (seen.size() < wanted.size()) {
            Set<Object> missing = new HashSet<>(wanted);
            missing.removeAll(seen);
            throw new IllegalArgumentException("Not a value of that dimension: " + missing);
        }
        if (negate) {
            result.flip(0, list.size());
        }
        return result;
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
    /**
     * Immutable AVL map used for dimension-pair relations. Updates copy only the search path, while
     * iteration remains in ascending key order so model construction is deterministic.
     */
    private static final class PersistentRelationMap extends AbstractMap<Long, Relation> {
        private static final PersistentRelationMap EMPTY = new PersistentRelationMap(null);
        private final RelationNode root;
        private transient Set<Entry<Long, Relation>> entries;

        private PersistentRelationMap(RelationNode root) {
            this.root = root;
        }

        static PersistentRelationMap empty() {
            return EMPTY;
        }

        PersistentRelationMap with(long key, Relation value) {
            return new PersistentRelationMap(put(root, key, value));
        }

        @Override
        public Relation get(Object key) {
            if (!(key instanceof Long)) return null;
            long target = (Long) key;
            RelationNode node = root;
            while (node != null) {
                if (target == node.key) return node.value;
                node = target < node.key ? node.left : node.right;
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof Long)) return false;
            long target = (Long) key;
            RelationNode node = root;
            while (node != null) {
                if (target == node.key) return true;
                node = target < node.key ? node.left : node.right;
            }
            return false;
        }

        @Override
        public int size() {
            return size(root);
        }

        @Override
        public Set<Entry<Long, Relation>> entrySet() {
            if (entries == null) {
                entries = new AbstractSet<>() {
                    @Override public int size() { return PersistentRelationMap.this.size(); }

                    @Override
                    public Iterator<Entry<Long, Relation>> iterator() {
                        return new Iterator<>() {
                            private final ArrayDeque<RelationNode> stack = new ArrayDeque<>();
                            { pushLeft(root); }

                            private void pushLeft(RelationNode node) {
                                while (node != null) {
                                    stack.push(node);
                                    node = node.left;
                                }
                            }

                            @Override public boolean hasNext() { return !stack.isEmpty(); }

                            @Override
                            public Entry<Long, Relation> next() {
                                if (stack.isEmpty()) throw new NoSuchElementException();
                                RelationNode node = stack.pop();
                                pushLeft(node.right);
                                return new SimpleImmutableEntry<>(node.key, node.value);
                            }
                        };
                    }
                };
            }
            return entries;
        }

        private static RelationNode put(RelationNode node, long key, Relation value) {
            if (node == null) return new RelationNode(key, value, null, null);
            if (key == node.key) return new RelationNode(key, value, node.left, node.right);
            RelationNode updated = key < node.key
                    ? new RelationNode(node.key, node.value, put(node.left, key, value), node.right)
                    : new RelationNode(node.key, node.value, node.left, put(node.right, key, value));
            return balance(updated);
        }

        private static RelationNode balance(RelationNode node) {
            int factor = height(node.left) - height(node.right);
            if (factor > 1) {
                if (height(node.left.left) < height(node.left.right)) {
                    return rotateRight(new RelationNode(node.key, node.value,
                            rotateLeft(node.left), node.right));
                }
                return rotateRight(node);
            }
            if (factor < -1) {
                if (height(node.right.right) < height(node.right.left)) {
                    return rotateLeft(new RelationNode(node.key, node.value,
                            node.left, rotateRight(node.right)));
                }
                return rotateLeft(node);
            }
            return node;
        }

        private static RelationNode rotateRight(RelationNode node) {
            RelationNode pivot = node.left;
            RelationNode right = new RelationNode(node.key, node.value, pivot.right, node.right);
            return new RelationNode(pivot.key, pivot.value, pivot.left, right);
        }

        private static RelationNode rotateLeft(RelationNode node) {
            RelationNode pivot = node.right;
            RelationNode left = new RelationNode(node.key, node.value, node.left, pivot.left);
            return new RelationNode(pivot.key, pivot.value, left, pivot.right);
        }

        private static int height(RelationNode node) { return node == null ? 0 : node.height; }
        private static int size(RelationNode node) { return node == null ? 0 : node.size; }
    }

    private static final class RelationNode {
        final long key;
        final Relation value;
        final RelationNode left;
        final RelationNode right;
        final int height;
        final int size;

        RelationNode(long key, Relation value, RelationNode left, RelationNode right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
            this.height = 1 + Math.max(left == null ? 0 : left.height, right == null ? 0 : right.height);
            this.size = 1 + (left == null ? 0 : left.size) + (right == null ? 0 : right.size);
        }
    }


    // ── Counting plan ────────────────────────────────────────────────────────

    private ConstraintPlan plan() {
        ConstraintPlan p = plan;
        if (p == null) {
            synchronized (this) {
                p = plan;
                if (p == null) {
                    p = new ConstraintPlan(dims, relations);
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
            result[i] = dims.get(i).get(val[i]);
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
        private final int dimensions = dims.size();
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
            for (List<?> dimension : dims) {
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
            int size = dims.get(dimension).size();
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
            this.exhausted = p.total.signum() == 0;
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
            Plan p = plan();
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
                    Plan p = plan();
                    if (rank.signum() < 0 || rank.compareTo(p.total) >= 0) {
                        throw new IllegalArgumentException(
                                "Rank " + rank + " is out of bounds [0, " + p.total + ")");
                    }
                    return tuple(p.unrank(rank));
                }
            };
        }
    }
}