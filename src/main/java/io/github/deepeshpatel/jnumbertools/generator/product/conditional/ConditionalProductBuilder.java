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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

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
 *         .when(network).is("4G").then(os).isNoneOf("Linux")
 *
 * configs.count();                       // exact, without listing the tuples
 * configs.lexOrder().stream().forEach(System.out::println); // all valid tuples, in lexicographic order
 * configs.lexOrderMth(1000, 0);          // every 1000th valid tuple
 * configs.choice(5, new Random(42));     // uniform over the valid tuples
 * }</pre>
 *
 * <h2>Meaning of a rule</h2>
 * <p>{@code when(A).is(x).then(B).isOneOf(y, z)} means: whenever the value chosen from {@code A}
 * is {@code x}, the value chosen from {@code B} must be {@code y} or {@code z}. Every completed
 * pairwise rule is combined with the other rules using AND. Dimensions are identified by the same
 * list instance passed to {@code conditionalProductOf} or {@code and}. More complex joint predicates
 * can be expressed through the Lambda Conditional Product API.</p>
 *
 * <h2>What is supported</h2>
 * <p>This builder supports pairwise rules expressed with {@code when(...).then(...)}.
 * It provides lexicographic and any-order traversal, exact counting, rank-based traversal,
 * and random choice and sampling. Joint predicates can also be compiled through the Lambda Conditional
 * Product API while retaining the same counting and traversal capabilities.</p>
 *
 * <h2>Practical performance guidance: dimension order</h2>
 * <p>Dimension declaration order can affect the amount of work required by lexicographic traversal.
 * The order is the order in which the dimensions are supplied to {@code conditionalProductOf(...)}
 * and {@link #and(List)}. It also defines the tuple positions and lexicographic encounter order, so
 * changing it changes the order (and positional meaning) of the generated tuples.</p>
 *
 * <p>During prefix-pruned traversal, a pairwise rule can be checked when the latter of its two dimensions
 * is assigned. A multi-trigger rule can be checked as soon as every dimension in its trigger and target
 * scope has been assigned. A restrictive rule can therefore prune the remaining search below that point.
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
 * <p>Internally, this builder models rule-constrained Cartesian products as a constraint satisfaction
 * problem (CSP). Pairwise rules use the binary constraint graph; multi-trigger rules form bounded
 * joint-assignment blocks. Both are compiled into an exact counting and generation plan:</p>
 * <ul>
 *   <li><strong>Cycle Contraction &amp; Forest Decomposition</strong>
 *       <em>(related to Freuder 1982; Dechter &amp; Pearl 1987)</em>:
 *       Dimensions linked by rules form a constraint graph. Bridges are identified, and dimensions
 *       connected through non-bridge edges are contracted into disjoint blocks. The valid joint
 *       assignments of each block are enumerated once, subject to the
 *       {@code MAX_LOOP_COMBINATIONS} limit (1,000,000) on the product of the original dimension sizes in that
 *       block. The remaining bridge edges form a forest (of treewidth at most 1), enabling dynamic
 *       programming across the resulting trees. The dimensions of a multi-trigger rule's scope are
 *       always one block. A chain of pairwise rules that links two of them is a second block, joined to
 *       the first by the rules between them, rather than part of the scope's block.</li>
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

    /**
     * Creates a builder with a single dimension.
     * @param elements first dimension; copied. An empty list is allowed and makes the product empty.
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder(List<?> elements) {
        this(new ConstraintModel(List.of(snapshot(elements)), PersistentRelationMap.empty()),
                List.of(elements));
    }

    /**
     * Creates a builder with two or more dimensions, equivalent to calling
     * {@link #ConditionalProductBuilder(List)} with {@code first} and then adding each remaining
     * dimension through {@link #and(List)}, but in a single pass: the dimension list, the identity
     * indexes and the rule compiler are built once, so the cost is O(dimensions) rather than the
     * O(dimensions²) of repeated {@code .and(...)} calls.
     *
     * @param first the first dimension; copied. An empty list is allowed and makes the product empty.
     * @param second the second dimension; copied
     * @param additional any further dimensions, in product order; each copied
     * @throws NullPointerException if any dimension is null or contains a null element
     */
    public ConditionalProductBuilder(List<?> first, List<?> second, List<?>... additional) {
        this(snapshotAll(first, second, additional), callerListsOf(first, second, additional));
    }

    private ConditionalProductBuilder(ConditionalProductBuilder source) {
        this(source.model, source.originals);
    }

    private ConditionalProductBuilder(ConstraintModel model, List<List<?>> originals) {
        this.model = model;
        this.originals = originals;
        this.ruleCompiler = new RuleCompiler(model.dimensions, originals, model.indexes);
    }

    /**
     * Builds a builder directly from already-snapshotted dimensions and the caller's parallel list of
     * original instances. Used by the multi-argument constructor, which does not go through repeated
     * {@link #and(List)} calls: those would copy the growing dimension list, indexes and rule compiler
     * once per dimension, making the whole construction O(d²). With this constructor it is O(d).
     *
     * @param copies    private snapshots, one per dimension (element i is the snapshot of originals.get(i))
     * @param originals the caller's list instances, kept only for identity comparison in the rule API
     */
    private ConditionalProductBuilder(List<List<?>> copies, List<List<?>> originals) {
        this.model = new ConstraintModel(copies, PersistentRelationMap.empty());
        this.originals = Collections.unmodifiableList(originals);
        this.ruleCompiler = new RuleCompiler(model.dimensions, this.originals, model.indexes);
    }

    /** Snapshots every dimension of the multi-argument constructor exactly once. */
    private static List<List<?>> snapshotAll(List<?> first, List<?> second, List<?>... additional) {
        int count = 2 + additional.length;
        List<List<?>> copies = new ArrayList<>(count);
        copies.add(snapshot(first));
        copies.add(snapshot(second));
        for (List<?> dimension : additional) copies.add(snapshot(dimension));
        return copies;
    }

    /** Keeps the caller's list instances (for identity-based rule resolution), in the same order as the copies. */
    private static List<List<?>> callerListsOf(List<?> first, List<?> second, List<?>... additional) {
        int count = 2 + additional.length;
        List<List<?>> lists = new ArrayList<>(count);
        lists.add(first);
        lists.add(second);
        for (List<?> dimension : additional) lists.add(dimension);
        return lists;
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
        return new WhenDimension(ruleCompiler.indexOf(dimension), List.of(), false);
    }

    /** Resolves a dimension handle for the package-private Lambda builder. */
    int indexOfDimension(List<?> dimension) {
        return ruleCompiler.indexOf(dimension);
    }

    /** First step of a pairwise trigger condition. */
    public final class WhenDimension {
        private final int dim;
        private final List<List<MultiTriggerRule.Condition>> groups;
        private final boolean startsAlternative;

        private WhenDimension(int dim, List<List<MultiTriggerRule.Condition>> groups, boolean startsAlternative) {
            this.dim = dim;
            this.groups = groups;
            this.startsAlternative = startsAlternative;
        }

        /** The chosen value equals {@code value}. */
        public Trigger is(Object value) { return trigger(List.of(nonNull(value)), false); }
        /** The chosen value is greater than {@code value}, using its natural ordering. */
        public Trigger isGreaterThan(Object value) { return trigger(ComparisonPredicates.greaterThan(value)); }
        /** The chosen value is greater than or equal to {@code value}, using its natural ordering. */
        public Trigger isGreaterThanOrEqual(Object value) { return trigger(ComparisonPredicates.greaterThanOrEqual(value)); }
        /** The chosen value is less than {@code value}, using its natural ordering. */
        public Trigger isLessThan(Object value) { return trigger(ComparisonPredicates.lessThan(value)); }
        /** The chosen value is less than or equal to {@code value}, using its natural ordering. */
        public Trigger isLessThanOrEqual(Object value) { return trigger(ComparisonPredicates.lessThanOrEqual(value)); }
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
            List<List<MultiTriggerRule.Condition>> updated = new ArrayList<>();
            for (List<MultiTriggerRule.Condition> group : groups) updated.add(new ArrayList<>(group));
            if (updated.isEmpty() || startsAlternative) updated.add(new ArrayList<>());
            updated.get(updated.size() - 1).add(
                    new MultiTriggerRule.Condition(dim, ruleCompiler.matching(dim, values, negate)));
            return new Trigger(MultiTriggerRule.copyGroups(updated));
        }
        private Trigger trigger(Predicate<Object> predicate) {
            List<List<MultiTriggerRule.Condition>> updated = new ArrayList<>();
            for (List<MultiTriggerRule.Condition> group : groups) updated.add(new ArrayList<>(group));
            if (updated.isEmpty() || startsAlternative) updated.add(new ArrayList<>());
            updated.get(updated.size() - 1).add(
                    new MultiTriggerRule.Condition(dim, ruleCompiler.matching(dim, predicate)));
            return new Trigger(MultiTriggerRule.copyGroups(updated));
        }
    }

    /**
     * A completed pairwise trigger expression.
     */
    public final class Trigger {
        private final List<List<MultiTriggerRule.Condition>> groups;

        private Trigger(List<List<MultiTriggerRule.Condition>> groups) {
            this.groups = groups;
        }

        /**
         * Names the dimension restricted by this trigger expression.
         * @param dimension target dimension; must not occur in any trigger condition
         * @return the final step of the rule
         * @throws NullPointerException if {@code dimension} is null
         * @throws IllegalArgumentException if the dimension is not part of this product or is used by the trigger
         */
        public ThenDimension then(List<?> dimension) {
            int target = ruleCompiler.indexOf(dimension);
            for (List<MultiTriggerRule.Condition> group : groups) {
                for (MultiTriggerRule.Condition condition : group) {
                    if (condition.dimension == target) {
                        throw new IllegalArgumentException("The target dimension must differ from every trigger dimension");
                    }
                }
            }
            return new ThenDimension(groups, target);
        }
    }

    /** Final step of a rule: what the restricted dimension may be. Returns the new builder. */
    public final class ThenDimension {
        private final List<List<MultiTriggerRule.Condition>> groups;
        private final int targetDim;

        private ThenDimension(List<List<MultiTriggerRule.Condition>> groups, int targetDim) {
            this.groups = groups;
            this.targetDim = targetDim;
        }

        /** The value must equal {@code value}. */
        public ConditionalProductBuilder is(Object value) { return apply(List.of(nonNull(value)), false); }
        /** The value must be greater than {@code value}, using its natural ordering. */
        public ConditionalProductBuilder isGreaterThan(Object value) { return apply(ComparisonPredicates.greaterThan(value)); }
        /** The value must be greater than or equal to {@code value}, using its natural ordering. */
        public ConditionalProductBuilder isGreaterThanOrEqual(Object value) { return apply(ComparisonPredicates.greaterThanOrEqual(value)); }
        /** The value must be less than {@code value}, using its natural ordering. */
        public ConditionalProductBuilder isLessThan(Object value) { return apply(ComparisonPredicates.lessThan(value)); }
        /** The value must be less than or equal to {@code value}, using its natural ordering. */
        public ConditionalProductBuilder isLessThanOrEqual(Object value) { return apply(ComparisonPredicates.lessThanOrEqual(value)); }
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
            return applyMatching(ruleCompiler.matching(targetDim, values, negate));
        }

        private ConditionalProductBuilder apply(Predicate<Object> predicate) {
            return applyMatching(ruleCompiler.matching(targetDim, predicate));
        }

        private ConditionalProductBuilder applyMatching(BitSet allowed) {
            int singleTriggerDimension = -1;
            BitSet combinedTriggers = new BitSet();
            boolean pairwise = true;
            for (List<MultiTriggerRule.Condition> group : groups) {
                BitSet groupValues = null;
                for (MultiTriggerRule.Condition condition : group) {
                    if (singleTriggerDimension < 0) singleTriggerDimension = condition.dimension;
                    if (singleTriggerDimension != condition.dimension) {
                        pairwise = false;
                        break;
                    }
                    if (groupValues == null) groupValues = (BitSet) condition.values.clone();
                    else groupValues.and(condition.values);
                }
                if (!pairwise) break;
                if (groupValues != null) combinedTriggers.or(groupValues);
            }
            if (pairwise) return withRule(singleTriggerDimension, combinedTriggers, targetDim, allowed);
            return new ConditionalProductBuilder(
                    model.withMultiRule(new MultiTriggerRule(groups, targetDim, allowed)), originals);
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
        return ConditionalProductGenerators.anyOrder(model);
    }

    /**
     * Switches to the array-based Lambda rule-definition style while retaining the dimensions and
     * pairwise rules already configured on this builder. The predicate receives all current values
     * as an {@code Object[]} and supports any number of dimensions.
     *
     * @return an array-based Lambda builder over the current conditional product
     */
    public LambdaConditionalProductBuilder.ArrayBased withLambda() {
        return new LambdaConditionalProductBuilder.ArrayBased(this);
    }

    /**
     * Switches to a typed Lambda builder for one dimension.
     *
     * @param dimension the existing dimension, used to preserve its element type
     * @param <A> element type
     * @return a typed one-dimensional Lambda builder over this product
     */
    public <A> LambdaConditionalProductBuilder.Typed1<A> withLambda(List<A> dimension) {
        requireLambdaDimensions(dimension);
        return new LambdaConditionalProductBuilder.Typed1<>(this);
    }

    /**
     * Switches to a typed Lambda builder for two dimensions.
     *
     * @param first the existing first dimension
     * @param second the existing second dimension
     * @param <A> first element type
     * @param <B> second element type
     * @return a typed two-dimensional Lambda builder over this product
     */
    public <A, B> LambdaConditionalProductBuilder.Typed2<A, B> withLambda(
            List<A> first, List<B> second) {
        requireLambdaDimensions(first, second);
        return new LambdaConditionalProductBuilder.Typed2<>(this);
    }

    /**
     * Switches to a typed Lambda builder for three dimensions.
     *
     * @param first the existing first dimension
     * @param second the existing second dimension
     * @param third the existing third dimension
     * @param <A> first element type
     * @param <B> second element type
     * @param <C> third element type
     * @return a typed three-dimensional Lambda builder over this product
     */
    public <A, B, C> LambdaConditionalProductBuilder.Typed3<A, B, C> withLambda(
            List<A> first, List<B> second, List<C> third) {
        requireLambdaDimensions(first, second, third);
        return new LambdaConditionalProductBuilder.Typed3<>(this);
    }

    /**
     * Switches to a typed Lambda builder for four dimensions.
     *
     * @param first the existing first dimension
     * @param second the existing second dimension
     * @param third the existing third dimension
     * @param fourth the existing fourth dimension
     * @param <A> first element type
     * @param <B> second element type
     * @param <C> third element type
     * @param <D> fourth element type
     * @return a typed four-dimensional Lambda builder over this product
     */
    public <A, B, C, D> LambdaConditionalProductBuilder.Typed4<A, B, C, D> withLambda(
            List<A> first, List<B> second, List<C> third, List<D> fourth) {
        requireLambdaDimensions(first, second, third, fourth);
        return new LambdaConditionalProductBuilder.Typed4<>(this);
    }

    /**
     * Switches to a typed Lambda builder for five dimensions.
     *
     * @param first the existing first dimension
     * @param second the existing second dimension
     * @param third the existing third dimension
     * @param fourth the existing fourth dimension
     * @param fifth the existing fifth dimension
     * @param <A> first element type
     * @param <B> second element type
     * @param <C> third element type
     * @param <D> fourth element type
     * @param <E> fifth element type
     * @return a typed five-dimensional Lambda builder over this product
     */
    public <A, B, C, D, E> LambdaConditionalProductBuilder.Typed5<A, B, C, D, E> withLambda(
            List<A> first, List<B> second, List<C> third, List<D> fourth, List<E> fifth) {
        requireLambdaDimensions(first, second, third, fourth, fifth);
        return new LambdaConditionalProductBuilder.Typed5<>(this);
    }

    /**
     * Ensures that the dimensions supplied for typed Lambda inference are exactly the dimensions
     * already registered on this builder, in declaration order.
     */
    private void requireLambdaDimensions(List<?>... dimensions) {
        Objects.requireNonNull(dimensions, "dimensions must not be null");
        if (dimensions.length != dimensionCount()) {
            throw new IllegalArgumentException("Typed Lambda entry point requires exactly "
                    + dimensions.length + " dimensions, but this product has " + dimensionCount());
        }
        for (int i = 0; i < dimensions.length; i++) {
            Objects.requireNonNull(dimensions[i], "dimension must not be null");
            if (originals.get(i) != dimensions[i]) {
                throw new IllegalArgumentException("Typed Lambda dimensions must be the same list instances "
                        + "registered with this product, in declaration order");
            }
        }
    }

    // Builder interface Methods
    /**
     * Returns valid tuples in lexicographic order. Pairwise-only products use {@link LexOrderStrategy#PRUNED_BITSET};
     * products containing multi-trigger Lambda rules use {@link LexOrderStrategy#DP}.
     *
     * <p>For pairwise-only rules, the default strategy checks constraints against the selected prefix and
     * lazily enables feasibility lookahead after substantial dead-end work. It does not build exact completion
     * counts. When multi-trigger Lambda rules are present, the builder selects DP because the pruned strategies
     * operate on pairwise rules only.</p>
     *
     * <p>Use {@link #lexOrder(LexOrderStrategy)} to select {@link LexOrderStrategy#DP} explicitly, or
     * {@link #lexOrder(LexOrderStrategy, long)} to use the {@link LexOrderStrategy#ADAPTIVE} policy.
     * The lookahead threshold is an implementation choice and may change between versions.</p>
     *
     * @return a re-iterable generator of valid tuples in lexicographic order
     */
    @Override
    public StreamableIterable<Object> lexOrder() {
        return lexOrder(model.multiRules.isEmpty() ? LexOrderStrategy.PRUNED_BITSET : LexOrderStrategy.DP);
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
     * <p>This setting affects only this lexicographic traversal. Exact-count and rank-based operations
     * use the DP plan.</p>
     *
     * @param strategy traversal strategy; must not be {@code null} or {@link LexOrderStrategy#ADAPTIVE}
     * @return a re-iterable generator of valid tuples in lexicographic order
     * @throws NullPointerException if {@code strategy} is null
     * @throws IllegalArgumentException if {@code strategy} is {@code ADAPTIVE}
     */
    public StreamableIterable<Object> lexOrder(LexOrderStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy");
        if (!model.multiRules.isEmpty() && strategy != LexOrderStrategy.DP) {
            throw new UnsupportedOperationException(
                    "Compound conditions require lexOrder(LexOrderStrategy.DP); " +
                            "the pruned lexicographic strategies support pairwise rules only.");
        }
        if (strategy == LexOrderStrategy.ADAPTIVE) {
            throw new IllegalArgumentException("ADAPTIVE requires an estimated traversal limit; use lexOrder(strategy, estimatedLimit)");
        }
        return ConditionalProductGenerators.lexOrder(model, strategy);
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
        return ConditionalProductGenerators.lexOrder(
                model, model.multiRules.isEmpty() ? LexOrderStrategy.PRUNED_BITSET : LexOrderStrategy.DP);
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
        return ConditionalProductGenerators.rankBased(model, ranks);
    }

    /**
     * {@inheritDoc}
     * <p>Uniform over the valid tuples, with replacement.</p>
     */
    @Override
    public StreamableIterable<Object> choice(int sampleSize, Random random) {
        BigIntegerChoice ranks = new BigIntegerChoice(count(), sampleSize, random); // validates the arguments
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
     * <p>The count is computed from the rules without enumerating the Cartesian product.</p>
     *
     * @return the exact count
     */
    @Override
    public BigInteger count() {
        return model.plan().counts().total;
    }

    @Override
    public boolean isEmpty() {
        return model.plan().isEmpty();
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

    /**
     * Filters a sole dimension while preserving the caller's original list as its identity handle.
     */
    ConditionalProductBuilder withUnaryLambdaRule(Predicate<Object> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (model.dimensions.size() != 1) {
            throw new IllegalStateException("A unary lambda predicate requires exactly one dimension");
        }
        List<Object> filtered = new ArrayList<>();
        for (Object value : model.dimensions.get(0)) {
            if (predicate.test(value)) filtered.add(value);
        }
        List<List<?>> copies = new ArrayList<>(model.dimensions);
        copies.set(0, snapshot(filtered));
        return new ConditionalProductBuilder(copies, originals);
    }

    int dimensionCount() {
        return model.dimensions.size();
    }

    /**
     * Compiles an arbitrary joint predicate over {@code scopeDims} into one or more multi-trigger rules.
     * The predicate is evaluated
     * once per combination of the scope's dimensions, here, during compilation; it is never called again
     * during generation, counting, or emptiness checks.
     *
     * <h2>How this is compiled</h2>
     * <p>The last dimension in {@code scopeDims} is treated as the rule's target; the remaining dimensions
     * are its antecedent. Every combination of the antecedent dimensions' values is enumerated (bounded by
     * {@link ConstraintGraph#MAX_LOOP_COMBINATIONS}) and, for each one, the predicate is evaluated once
     * per value of the target dimension to
     * determine that combination's allowed target values. Antecedent combinations that produce the same
     * allowed-target set are grouped into a single rule with one OR-group per combination, mirroring how
     * {@code .when(conditionA).then(dim).isX(); .when(conditionB).then(dim).isY();} compiles into separate
     * rules only when their consequences actually differ. An antecedent combination under which every
     * target value is accepted contributes no rule at all, since it imposes no constraint.</p>
     *
     * <p>When the scope is exactly two dimensions (one antecedent, one target), each compiled group is
     * routed to the lightweight binary {@code Relation} table instead of a multi-trigger rule. This keeps
     * several independent two-dimension
     * lambda rules that happen to share one dimension (e.g. four rules all triggered by the same {@code os})
     * from being merged into one oversized block purely because of how they were declared.</p>
     *
     * <p>This gives the compiled rule the same downstream treatment as a multi-trigger Lambda rule: the
     * constraint graph folds its scope into one bounded, enumerated block, and the tree DP that backs
     * {@link #count()}/{@link #isEmpty()}/{@link #anyOrder()} treats that block as a single node with
     * precomputed, table-driven transitions — no interpreter re-evaluates the predicate while generating.</p>
     *
     * <p>A scope of a single dimension is rejected: {@code ConstraintPlan} does not run a rule through its
     * node enumeration for a single-dimension node, so such a rule would be enforced by traversal but
     * ignored by counting and unranking, giving inconsistent results between {@code lexOrder()},
     * {@code count()} and {@code byRanks(...)}. To filter a single dimension, pre-filter its input list
     * before building the product; unary predicates are also supported by the one-dimension Lambda API.</p>
     *
     * @param scopeDims positions of the dimensions the predicate jointly depends on, in declaration order;
     *                  must contain at least two positions (one antecedent plus the target) and no duplicates
     * @param predicate evaluated with one argument per {@code scopeDims} position, in the same order;
     *                  must be deterministic and side-effect-free
     * @return a new builder with the compiled rule(s) added
     * @throws IllegalArgumentException if the scope has fewer than two dimensions or contains a duplicate
     *                                  dimension, or the antecedent-combinations-times-target-values count
     *                                  that would need to be probed exceeds
     *                                  {@link ConstraintGraph#MAX_LOOP_COMBINATIONS}
     */
    ConditionalProductBuilder withLambdaRule(int[] scopeDims, Predicate<Object[]> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        Objects.requireNonNull(scopeDims, "scopeDims must not be null");
        if (scopeDims.length < 2) {
            throw new IllegalArgumentException("A lambda rule must depend on at least two dimensions "
                    + "(one antecedent and one target); to filter values from a single dimension, "
                    + "pre-filter its input list before building the product");
        }
        for (int i = 0; i < scopeDims.length; i++) {
            for (int j = i + 1; j < scopeDims.length; j++) {
                if (scopeDims[i] == scopeDims[j]) {
                    throw new IllegalArgumentException("A lambda rule scope cannot contain the same dimension twice");
                }
            }
        }

        int targetDim = scopeDims[scopeDims.length - 1];
        int[] antecedentDims = Arrays.copyOf(scopeDims, scopeDims.length - 1);
        int[] antecedentSizes = new int[antecedentDims.length];
        for (int i = 0; i < antecedentDims.length; i++) {
            antecedentSizes[i] = model.dimensions.get(antecedentDims[i]).size();
        }
        int targetSize = model.dimensions.get(targetDim).size();

        // An empty target or antecedent makes the entire Cartesian product empty, so no
        // predicate probes are required. Check every antecedent before bounded multiplication:
        // an empty dimension may occur after a prefix whose product already exceeds the probe limit.
        if (targetSize == 0) {
            return this;
        }
        for (int size : antecedentSizes) {
            if (size == 0) {
                return this;
            }
        }

        long antecedentCombos = 1L;
        for (int size : antecedentSizes) {
            if (antecedentCombos > ConstraintGraph.MAX_LOOP_COMBINATIONS / size) {
                antecedentCombos = ConstraintGraph.MAX_LOOP_COMBINATIONS + 1;
                break;
            }
            antecedentCombos *= size;
        }
        long totalProbes;
        if (antecedentCombos > ConstraintGraph.MAX_LOOP_COMBINATIONS
                || antecedentCombos > ConstraintGraph.MAX_LOOP_COMBINATIONS / targetSize) {
            totalProbes = ConstraintGraph.MAX_LOOP_COMBINATIONS + 1;
        } else {
            totalProbes = antecedentCombos * targetSize;
        }
        if (totalProbes > ConstraintGraph.MAX_LOOP_COMBINATIONS) {
            BigInteger probes = BigInteger.ONE;
            for (int dim : scopeDims) probes = probes.multiply(BigInteger.valueOf(model.dimensions.get(dim).size()));
            throw new IllegalArgumentException("Compiling this lambda rule would need to probe the predicate "
                    + probes + " times (antecedent combinations times target values), but the limit is "
                    + ConstraintGraph.MAX_LOOP_COMBINATIONS + ". A predicate over all dimensions is compiled "
                    + "over the whole product; use when(dim1, dim2, ...).matches(predicate) to cover only the "
                    + "dimensions it depends on, or reduce the sizes of the dimensions it covers.");
        }

        // Nothing to compile when either side is empty. An empty antecedent dimension means there is no
        // antecedent combination to pick a value from (the enumeration loop below would call get(0) on an
        // empty list); an empty target dimension means no tuple can satisfy any rule that names it. In
        // either case the product itself has count 0, and returning without adding a rule is both correct
        // and avoids both the exception and the pointless probe loop.
        if (antecedentCombos == 0 || targetSize == 0) {
            return this;
        }

        Map<BitSet, List<int[]>> combosByAllowedTargets = new LinkedHashMap<>();
        Object[] tuple = new Object[scopeDims.length];
        int[] counters = new int[antecedentDims.length];
        List<?> targetValues = model.dimensions.get(targetDim);
        while (true) {
            for (int i = 0; i < antecedentDims.length; i++) {
                tuple[i] = model.dimensions.get(antecedentDims[i]).get(counters[i]);
            }
            BitSet allowed = new BitSet(targetSize);
            for (int t = 0; t < targetSize; t++) {
                tuple[scopeDims.length - 1] = targetValues.get(t);
                if (predicate.test(tuple)) {
                    allowed.set(t);
                }
            }
            if (allowed.cardinality() < targetSize) {
                combosByAllowedTargets.computeIfAbsent(allowed, key -> new ArrayList<>()).add(counters.clone());
            }
            int i = antecedentDims.length - 1;
            while (i >= 0) {
                counters[i]++;
                if (counters[i] < antecedentSizes[i]) break;
                counters[i] = 0;
                i--;
            }
            if (i < 0) break;
        }

        ConditionalProductBuilder result = this;
        if (antecedentDims.length == 1) {
            // Exactly one antecedent dimension and one target: route to the lightweight binary relation
            // table rather than a multi-trigger block.
            int antecedentDim = antecedentDims[0];
            for (Map.Entry<BitSet, List<int[]>> entry : combosByAllowedTargets.entrySet()) {
                BitSet triggerValues = new BitSet(antecedentSizes[0]);
                for (int[] combo : entry.getValue()) triggerValues.set(combo[0]);
                result = new ConditionalProductBuilder(
                        result.model.withRule(antecedentDim, triggerValues, targetDim, entry.getKey()), result.originals);
            }
        } else {
            for (Map.Entry<BitSet, List<int[]>> entry : combosByAllowedTargets.entrySet()) {
                List<List<MultiTriggerRule.Condition>> groups = new ArrayList<>(entry.getValue().size());
                for (int[] combo : entry.getValue()) {
                    List<MultiTriggerRule.Condition> group = new ArrayList<>(antecedentDims.length);
                    for (int i = 0; i < antecedentDims.length; i++) {
                        BitSet single = new BitSet(antecedentSizes[i]);
                        single.set(combo[i]);
                        group.add(new MultiTriggerRule.Condition(antecedentDims[i], single));
                    }
                    groups.add(group);
                }
                MultiTriggerRule rule = new MultiTriggerRule(groups, targetDim, entry.getKey());
                result = new ConditionalProductBuilder(result.model.withMultiRule(rule), result.originals);
            }
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
}