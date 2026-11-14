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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
 * such a block grows with the product of the sizes of its dimensions; a rule that would make that product
 * larger than a limit is rejected. For all accepted rule sets {@link #count()}, {@link #lexOrder()},
 * {@link #lexOrderMth(BigInteger, BigInteger)}, {@link #byRanks(Iterable)}, {@link #choice(int, Random)}
 * and {@link #sample(int, Random)} work on the valid tuples only, without enumerating the full product.
 * Ranks are positions among the valid tuples in lexicographic order (dimension order, then the order of
 * each list). They change when rules change.</p>
 *
 * <p>Instances are immutable and thread-safe; {@code and(...)}, {@code when(...)} chains return new
 * builders.</p>
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

    private final List<List<?>> dims;            // private snapshots; unmodifiable
    private final List<List<?>> originals;       // the caller's list objects, matched by identity only
    private final Map<Long, Relation> relations; // key(u, v) with u < v; immutable values
    private volatile Plan plan;                  // built lazily on first use


    /**
     * Creates a builder with a single dimension.
     * @param elements first dimension; copied. An empty list is allowed and makes the product empty.
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder(List<?> elements) {
        this(List.<List<?>>of(snapshot(elements)), List.<List<?>>of(elements), Map.of());
    }

    private ConditionalProductBuilder(List<List<?>> dims, List<List<?>> originals, Map<Long, Relation> relations) {
        this.dims = dims;
        this.originals = originals;
        this.relations = relations;
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
    @Override
    public StreamableIterable<Object> lexOrder() {
        return new LexOrderGenerator();
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
        Plan p = plan();
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
        if (relation == null) {
            long cells = (long) dims.get(u).size() * dims.get(v).size();
            if (cells > MAX_PAIR_CELLS) {
                throw new IllegalArgumentException("The two dimensions have too many value pairs (" + cells
                        + ") for a rule; the limit is " + MAX_PAIR_CELLS);
            }
            relation = Relation.full(dims.get(u).size(), dims.get(v).size());
        }
        Relation updated = relation.restrict(triggerValues, allowedTargets, triggerDim == u);
        Map<Long, Relation> newRelations = new HashMap<>(relations);
        newRelations.put(key, updated);
        checkLoopBlocks(dims, newRelations.keySet());
        return new ConditionalProductBuilder(dims, originals, Map.copyOf(newRelations));
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
    private static int[] blockOf(int n, Collection<Long> keys) {
        List<int[]> edges = new ArrayList<>(keys.size());
        for (long key : keys) edges.add(new int[]{(int) (key >>> 32), (int) key});
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        for (int skip = 0; skip < edges.size(); skip++) {
            int[] e = edges.get(skip);
            if (reachable(n, edges, skip, e[0], e[1])) {   // still connected without this edge: not a bridge
                union(parent, e[0], e[1]);
            }
        }
        int[] block = new int[n];
        for (int i = 0; i < n; i++) block[i] = find(parent, i);
        return block;
    }

    private static boolean reachable(int n, List<int[]> edges, int skip, int from, int to) {
        boolean[] seen = new boolean[n];
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(from);
        seen[from] = true;
        while (!stack.isEmpty()) {
            int x = stack.pop();
            if (x == to) return true;
            for (int i = 0; i < edges.size(); i++) {
                if (i == skip) continue;
                int[] e = edges.get(i);
                int other = e[0] == x ? e[1] : e[1] == x ? e[0] : -1;
                if (other >= 0 && !seen[other]) {
                    seen[other] = true;
                    stack.push(other);
                }
            }
        }
        return false;
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

    private static long key(int u, int v) {
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
    private static final class Relation {
        final BitSet[] uToV; // uToV[a] = values of v allowed together with value a of u
        final BitSet[] vToU; // vToU[b] = values of u allowed together with value b of v

        private Relation(BitSet[] uToV, BitSet[] vToU) {
            this.uToV = uToV;
            this.vToU = vToU;
        }

        static Relation full(int du, int dv) {
            BitSet[] uv = new BitSet[du];
            for (int a = 0; a < du; a++) {
                uv[a] = new BitSet(dv);
                uv[a].set(0, dv);
            }
            BitSet[] vu = new BitSet[dv];
            for (int b = 0; b < dv; b++) {
                vu[b] = new BitSet(du);
                vu[b].set(0, du);
            }
            return new Relation(uv, vu);
        }

        /** Copy in which every value of the trigger dimension in {@code triggers} allows only {@code allowedTargets}. */
        Relation restrict(BitSet triggers, BitSet allowedTargets, boolean triggerIsU) {
            BitSet[] uv = deepCopy(uToV);
            BitSet[] vu = deepCopy(vToU);
            int targetSize = triggerIsU ? vu.length : uv.length;
            for (int a = triggers.nextSetBit(0); a >= 0; a = triggers.nextSetBit(a + 1)) {
                for (int b = allowedTargets.nextClearBit(0); b < targetSize; b = allowedTargets.nextClearBit(b + 1)) {
                    if (triggerIsU) {
                        uv[a].clear(b);
                        vu[b].clear(a);
                    } else {
                        vu[a].clear(b);
                        uv[b].clear(a);
                    }
                }
            }
            return new Relation(uv, vu);
        }

        private static BitSet[] deepCopy(BitSet[] rows) {
            BitSet[] copy = new BitSet[rows.length];
            for (int i = 0; i < rows.length; i++) copy[i] = (BitSet) rows[i].clone();
            return copy;
        }
    }

    // ── Counting plan ────────────────────────────────────────────────────────

    private Plan plan() {
        Plan p = plan;
        if (p == null) {
            synchronized (this) {
                p = plan;
                if (p == null) {
                    p = new Plan(dims, relations);
                    plan = p;
                }
            }
        }
        return p;
    }

    /**
     * Counting tables. The dimensions are grouped into <em>nodes</em>: a dimension that is on no loop of
     * rules is a node by itself; the dimensions of a loop (a block) form one node whose values are the
     * valid joint assignments of its dimensions. Nodes and the single rules between them form a forest;
     * each tree is rooted at its node with the lowest dimension index.
     * <ul>
     *   <li>{@code free[v][a]}: ways to choose the values of v's subtree when v has value a.</li>
     *   <li>{@code msg[c][a]}: ways to choose c's subtree when c's parent has value a.</li>
     * </ul>
     * The number of completions of a prefix (the first k dimensions fixed) is a product over the trees; a
     * tree without a fixed dimension uses the tables directly, a tree with fixed dimensions is evaluated
     * along the paths to them only, counting only the node values that agree with the fixed dimensions.
     */
    private static final class Plan {
        final int[][] members;         // members[node]: dimensions of the node, ascending
        final int[] size;              // number of values of the node
        final int[][][] values;        // values[node][a][pos]: value of members[node][pos]; null for a single dimension
        final int[][] children;
        final BitSet[][] fromParent;   // fromParent[c][a]: values of c allowed when c's parent has value a
        final int[] minSub;            // lowest dimension index in the subtree
        final BigInteger[][] free;
        final BigInteger[][] msg;
        final BigInteger[][] freePrefix; // freePrefix[v][i] = sum of free[v][0..i-1]; only for multi-dimension nodes
        final int[] minChildSub;        // lowest dimension index among the children's subtrees (MAX_VALUE if none)
        final int[] roots;             // ascending
        final BigInteger[] suffix;     // suffix[i] = product of the free totals of roots[i..]
        final int[] firstRoot;         // firstRoot[k] = index in roots of the first root whose lowest dimension is >= k
        final int dimensions;
        final int[] dimSize;           // number of values of each dimension
        final int[][] belowDims;       // belowDims[d]: lower-numbered dimensions linked to d by a rule
        final BitSet[][][] belowRows;  // belowRows[d][i][a]: values of d allowed when belowDims[d][i] has value a
        final BigInteger total;
        final BigInteger fullProduct;   // product of all dimension sizes, ignoring the rules
        // Same tables as above in primitive form; null unless every table entry fits in a long. When they do,
        // every intermediate count computed at query time is bounded by a table entry, so no overflow is possible.
        final long[][] lfree;
        final long[][] lmsg;
        final long[][] lfreePrefix;
        final long[] lsuffix;

        Plan(List<List<?>> dims, Map<Long, Relation> relations) {
            dimensions = dims.size();
            int n = dimensions;
            dimSize = new int[n];
            for (int d = 0; d < n; d++) dimSize[d] = dims.get(d).size();
            List<List<Integer>> belowList = new ArrayList<>(n);
            List<List<BitSet[]>> belowRowList = new ArrayList<>(n);
            for (int d = 0; d < n; d++) {
                belowList.add(new ArrayList<>());
                belowRowList.add(new ArrayList<>());
            }
            for (Map.Entry<Long, Relation> e : relations.entrySet()) {
                int u = (int) (e.getKey() >>> 32);
                int v = (int) (long) e.getKey();
                belowList.get(v).add(u);
                belowRowList.get(v).add(e.getValue().uToV);
            }
            belowDims = new int[n][];
            belowRows = new BitSet[n][][];
            for (int d = 0; d < n; d++) {
                belowDims[d] = belowList.get(d).stream().mapToInt(Integer::intValue).toArray();
                belowRows[d] = belowRowList.get(d).toArray(new BitSet[0][]);
            }

            // nodes, ordered by their lowest dimension
            int[] blockOf = blockOf(n, relations.keySet());
            int[] nodeOfDim = new int[n];
            List<List<Integer>> memberLists = new ArrayList<>();
            int[] nodeOfBlock = new int[n];
            Arrays.fill(nodeOfBlock, -1);
            for (int d = 0; d < n; d++) {
                int b = blockOf[d];
                if (nodeOfBlock[b] < 0) {
                    nodeOfBlock[b] = memberLists.size();
                    memberLists.add(new ArrayList<>());
                }
                nodeOfDim[d] = nodeOfBlock[b];
                memberLists.get(nodeOfBlock[b]).add(d);
            }
            int m = memberLists.size();
            members = new int[m][];
            size = new int[m];
            values = new int[m][][];
            for (int x = 0; x < m; x++) {
                members[x] = memberLists.get(x).stream().mapToInt(Integer::intValue).toArray();
                if (members[x].length == 1) {
                    size[x] = dims.get(members[x][0]).size();
                } else {
                    values[x] = enumerate(members[x], dims, relations);
                    size[x] = values[x].length;
                }
            }

            // single rules between different nodes
            List<List<Object[]>> adjacency = new ArrayList<>(m);
            for (int x = 0; x < m; x++) adjacency.add(new ArrayList<>());
            for (Map.Entry<Long, Relation> e : relations.entrySet()) {
                int u = (int) (e.getKey() >>> 32);
                int v = (int) (long) e.getKey();
                if (nodeOfDim[u] == nodeOfDim[v]) continue;
                adjacency.get(nodeOfDim[u]).add(new Object[]{nodeOfDim[v], u, v, e.getValue(), true});
                adjacency.get(nodeOfDim[v]).add(new Object[]{nodeOfDim[u], v, u, e.getValue(), false});
            }

            fromParent = new BitSet[m][];
            List<List<Integer>> childLists = new ArrayList<>(m);
            for (int x = 0; x < m; x++) childLists.add(new ArrayList<>());
            boolean[] visited = new boolean[m];
            List<Integer> rootList = new ArrayList<>();
            List<Integer> order = new ArrayList<>(m);
            for (int r = 0; r < m; r++) {
                if (visited[r]) continue;
                rootList.add(r);
                visited[r] = true;
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                queue.add(r);
                while (!queue.isEmpty()) {
                    int x = queue.poll();
                    order.add(x);
                    for (Object[] edge : adjacency.get(x)) {
                        int other = (Integer) edge[0];
                        if (visited[other]) continue;
                        visited[other] = true;
                        fromParent[other] = rowsBetween(x, (Integer) edge[1], other, (Integer) edge[2],
                                (Relation) edge[3], (Boolean) edge[4]);
                        childLists.get(x).add(other);
                        queue.add(other);
                    }
                }
            }
            children = new int[m][];
            for (int x = 0; x < m; x++) {
                children[x] = childLists.get(x).stream().mapToInt(Integer::intValue).toArray();
            }

            free = new BigInteger[m][];
            msg = new BigInteger[m][];
            minSub = new int[m];
            for (int idx = order.size() - 1; idx >= 0; idx--) {
                int v = order.get(idx);
                BigInteger[] f = new BigInteger[size[v]];
                Arrays.fill(f, BigInteger.ONE);
                minSub[v] = members[v][0];
                for (int c : children[v]) {
                    BigInteger[] msgToParent = new BigInteger[size[v]];
                    for (int a = 0; a < size[v]; a++) {
                        BitSet allowed = fromParent[c][a];
                        BigInteger sum = BigInteger.ZERO;
                        for (int b = allowed.nextSetBit(0); b >= 0; b = allowed.nextSetBit(b + 1)) {
                            sum = sum.add(free[c][b]);
                        }
                        msgToParent[a] = sum;
                        f[a] = f[a].multiply(sum);
                    }
                    msg[c] = msgToParent;
                    minSub[v] = Math.min(minSub[v], minSub[c]);
                }
                free[v] = f;
            }

            freePrefix = new BigInteger[m][];
            minChildSub = new int[m];
            for (int x = 0; x < m; x++) {
                int low = Integer.MAX_VALUE;
                for (int c : children[x]) low = Math.min(low, minSub[c]);
                minChildSub[x] = low;
                if (values[x] != null) {
                    BigInteger[] prefix = new BigInteger[size[x] + 1];
                    prefix[0] = BigInteger.ZERO;
                    for (int i = 0; i < size[x]; i++) prefix[i + 1] = prefix[i].add(free[x][i]);
                    freePrefix[x] = prefix;
                }
            }

            roots = rootList.stream().mapToInt(Integer::intValue).toArray();
            BigInteger[] rootTotal = new BigInteger[roots.length];
            for (int i = 0; i < roots.length; i++) {
                BigInteger sum = BigInteger.ZERO;
                for (BigInteger w : free[roots[i]]) sum = sum.add(w);
                rootTotal[i] = sum;
            }
            suffix = new BigInteger[roots.length + 1];
            suffix[roots.length] = BigInteger.ONE;
            for (int i = roots.length - 1; i >= 0; i--) suffix[i] = suffix[i + 1].multiply(rootTotal[i]);
            total = suffix[0];
            BigInteger product = BigInteger.ONE;
            for (int d = 0; d < n; d++) product = product.multiply(BigInteger.valueOf(dimSize[d]));
            fullProduct = product;

            boolean fits = fitsLong(free) && fitsLong(msg) && fitsLong(freePrefix) && fitsLong(suffix);
            lfree = fits ? toLong(free) : null;
            lmsg = fits ? toLong(msg) : null;
            lfreePrefix = fits ? toLong(freePrefix) : null;
            lsuffix = fits ? toLong(suffix) : null;

            firstRoot = new int[n + 1];
            int ri = 0;
            for (int k = 0; k <= n; k++) {
                while (ri < roots.length && members[roots[ri]][0] < k) ri++;
                firstRoot[k] = ri;
            }
        }

        /** All assignments of the dimensions in {@code dimsOfNode} that satisfy every rule among them. */
        private static int[][] enumerate(int[] dimsOfNode, List<List<?>> dims, Map<Long, Relation> relations) {
            int len = dimsOfNode.length;
            List<int[]> out = new ArrayList<>();
            int[] current = new int[len];
            fill(0, current, dimsOfNode, dims, relations, out);
            return out.toArray(new int[0][]);
        }

        private static void fill(int pos, int[] current, int[] dimsOfNode, List<List<?>> dims,
                                 Map<Long, Relation> relations, List<int[]> out) {
            if (pos == dimsOfNode.length) {
                out.add(current.clone());
                return;
            }
            int size = dims.get(dimsOfNode[pos]).size();
            for (int idx = 0; idx < size; idx++) {
                current[pos] = idx;
                boolean ok = true;
                for (int q = 0; q < pos && ok; q++) {
                    Relation rel = relations.get(key(dimsOfNode[q], dimsOfNode[pos]));
                    if (rel != null && !rel.uToV[current[q]].get(idx)) ok = false;
                }
                if (ok) fill(pos + 1, current, dimsOfNode, dims, relations, out);
            }
        }

        /** Rows[a] = values of {@code child} allowed when {@code parent} has value a, through the one rule between them. */
        private BitSet[] rowsBetween(int parent, int parentDim, int child, int childDim, Relation rel,
                                     boolean parentIsLow) {
            BitSet[] rowsOfDim = parentIsLow ? rel.uToV : rel.vToU;   // by the parent's dimension value
            if (values[parent] == null && values[child] == null) {
                return rowsOfDim;                                      // single dimensions: use the rows as they are
            }
            int parentPos = position(members[parent], parentDim);
            int childPos = position(members[child], childDim);
            BitSet[] rows = new BitSet[size[parent]];
            for (int a = 0; a < size[parent]; a++) {
                int parentValue = values[parent] == null ? a : values[parent][a][parentPos];
                BitSet row = new BitSet(size[child]);
                for (int b = 0; b < size[child]; b++) {
                    int childValue = values[child] == null ? b : values[child][b][childPos];
                    if (rowsOfDim[parentValue].get(childValue)) row.set(b);
                }
                rows[a] = row;
            }
            return rows;
        }

        private static int position(int[] dimsOfNode, int dim) {
            for (int i = 0; i < dimsOfNode.length; i++) if (dimsOfNode[i] == dim) return i;
            throw new IllegalStateException("dimension not in node");
        }

        /**
         * Range [lo, hi) of the values of node c that agree with the dimensions already fixed (below k),
         * packed as lo in the high 32 bits and hi in the low 32 bits. The fixed dimensions of a node are the
         * first ones in its member list and its values are in lexicographic order, so the agreeing values are
         * contiguous and are found by binary search.
         */
        private long range(int c, int k, int[] val) {
            int[] mem = members[c];
            if (mem[0] >= k) return (long) size[c];                 // nothing fixed: [0, size)
            if (values[c] == null) {
                int b = val[mem[0]];
                return ((long) b << 32) | (b + 1);
            }
            int fixed = 0;
            while (fixed < mem.length && mem[fixed] < k) fixed++;
            int[][] list = values[c];
            int lo = lowerBound(list, mem, fixed, val, false);
            int hi = lowerBound(list, mem, fixed, val, true);
            return ((long) lo << 32) | hi;
        }

        /** First index whose fixed-prefix is >= the target prefix (or > it when {@code strict}). */
        private static int lowerBound(int[][] list, int[] mem, int fixed, int[] val, boolean strict) {
            int lo = 0;
            int hi = list.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                int cmp = 0;
                for (int pos = 0; pos < fixed && cmp == 0; pos++) {
                    cmp = Integer.compare(list[mid][pos], val[mem[pos]]);
                }
                if (cmp < 0 || (strict && cmp == 0)) lo = mid + 1; else hi = mid;
            }
            return lo;
        }

        /** Sum of count(c, b) over the values b in {@code allowed} (all values if null) that agree with the fixed dimensions. */
        private BigInteger sumConsistent(int c, BitSet allowed, int k, int[] val) {
            long r = range(c, k, val);
            int lo;
            int hi;
            if (members[c][0] >= k) {
                lo = 0;
                hi = size[c];
            } else {
                lo = (int) (r >>> 32);
                hi = (int) r;
            }
            if (lo >= hi) return BigInteger.ZERO;
            if (allowed == null && minChildSub[c] >= k && freePrefix[c] != null) {
                // no fixed dimension below c's children: every value counts exactly free[c][b]
                return freePrefix[c][hi].subtract(freePrefix[c][lo]);
            }
            BigInteger sum = BigInteger.ZERO;
            if (allowed == null) {
                for (int b = lo; b < hi; b++) sum = sum.add(count(c, b, k, val));
            } else {
                for (int b = allowed.nextSetBit(lo); b >= 0 && b < hi; b = allowed.nextSetBit(b + 1)) {
                    sum = sum.add(count(c, b, k, val));
                }
            }
            return sum;
        }

        private static boolean fitsLong(BigInteger[] row) {
            if (row == null) return true;
            for (BigInteger b : row) if (b.bitLength() > 62) return false;
            return true;
        }

        private static boolean fitsLong(BigInteger[][] rows) {
            for (BigInteger[] row : rows) if (!fitsLong(row)) return false;
            return true;
        }

        private static long[] toLong(BigInteger[] row) {
            if (row == null) return null;
            long[] r = new long[row.length];
            for (int i = 0; i < r.length; i++) r[i] = row[i].longValue();
            return r;
        }

        private static long[][] toLong(BigInteger[][] rows) {
            long[][] r = new long[rows.length][];
            for (int i = 0; i < rows.length; i++) r[i] = toLong(rows[i]);
            return r;
        }

        /** True when a uniformly random point of the full product is valid with probability at least 1/4. */
        boolean mostTuplesValid() {
            return total.shiftLeft(2).compareTo(fullProduct) >= 0;
        }

        /** Whether a complete tuple satisfies every rule. */
        boolean isValid(int[] val) {
            for (int d = 1; d < dimensions; d++) {
                if (!agreesWithBelow(d, val[d], val)) return false;
            }
            return true;
        }

        // ── primitive-long versions of the counting queries (used when lsuffix != null) ──

        private long sumConsistentL(int c, BitSet allowed, int k, int[] val) {
            long r = range(c, k, val);
            int lo;
            int hi;
            if (members[c][0] >= k) {
                lo = 0;
                hi = size[c];
            } else {
                lo = (int) (r >>> 32);
                hi = (int) r;
            }
            if (lo >= hi) return 0;
            if (allowed == null && minChildSub[c] >= k && lfreePrefix[c] != null) {
                return lfreePrefix[c][hi] - lfreePrefix[c][lo];
            }
            long sum = 0;
            if (allowed == null) {
                for (int b = lo; b < hi; b++) sum += countL(c, b, k, val);
            } else {
                for (int b = allowed.nextSetBit(lo); b >= 0 && b < hi; b = allowed.nextSetBit(b + 1)) {
                    sum += countL(c, b, k, val);
                }
            }
            return sum;
        }

        private long completionsL(int k, int[] val) {
            int fixedRoots = firstRoot[k];
            long result = lsuffix[fixedRoots];
            if (result == 0) return 0;
            for (int i = 0; i < fixedRoots; i++) {
                long c = sumConsistentL(roots[i], null, k, val);
                if (c == 0) return 0;
                result *= c;
            }
            return result;
        }

        private long countL(int v, int a, int k, int[] val) {
            if (minSub[v] >= k) return lfree[v][a];
            long result = 1;
            for (int c : children[v]) {
                BitSet allowed = fromParent[c][a];
                long s = minSub[c] >= k ? lmsg[c][a] : sumConsistentL(c, allowed, k, val);
                if (s == 0) return 0;
                result *= s;
            }
            return result;
        }

        /** Number of valid ways to choose dimensions k..n-1 when dimensions 0..k-1 have values val[0..k-1]. */
        BigInteger completions(int k, int[] val) {
            int fixedRoots = firstRoot[k];
            BigInteger result = suffix[fixedRoots];
            if (result.signum() == 0) return BigInteger.ZERO;
            for (int i = 0; i < fixedRoots; i++) {
                BigInteger c = sumConsistent(roots[i], null, k, val);
                if (c.signum() == 0) return BigInteger.ZERO;
                result = result.multiply(c);
            }
            return result;
        }

        /** Ways to choose v's subtree when v has value a, with dimensions below k fixed. */
        private BigInteger count(int v, int a, int k, int[] val) {
            if (minSub[v] >= k) return free[v][a];
            BigInteger result = BigInteger.ONE;
            for (int c : children[v]) {
                BitSet allowed = fromParent[c][a];
                BigInteger s = minSub[c] >= k ? msg[c][a] : sumConsistent(c, allowed, k, val);
                if (s.signum() == 0) return BigInteger.ZERO;
                result = result.multiply(s);
            }
            return result;
        }

        /** Smallest value index of dimension k, at least {@code from}, that can still be completed; -1 if none. */
        int firstValid(int k, int from, int[] val) {
            int dimSizeK = dimensionSize(k);
            if (from >= dimSizeK) return -1;
            boolean last = k == dimensions - 1;
            int[] dimsBelow = belowDims[k];
            if (dimsBelow.length == 0) {
                for (int idx = from; idx < dimSizeK; idx++) {
                    val[k] = idx;
                    if (last || hasCompletion(k + 1, val)) return idx;
                }
                return -1;
            }
            // Candidates must satisfy every rule with an earlier dimension: walk the set bits of the first
            // row and test the others, so invalid values are skipped a machine word at a time.
            BitSet[][] rows = belowRows[k];
            BitSet first = rows[0][val[dimsBelow[0]]];
            outer:
            for (int idx = first.nextSetBit(from); idx >= 0 && idx < dimSizeK; idx = first.nextSetBit(idx + 1)) {
                for (int i = 1; i < dimsBelow.length; i++) {
                    if (!rows[i][val[dimsBelow[i]]].get(idx)) continue outer;
                }
                val[k] = idx;
                if (last || hasCompletion(k + 1, val)) return idx;
            }
            return -1;
        }

        /**
         * For the last dimension only: value idx is valid iff every rule between it and the (already
         * consistent) earlier dimensions holds. Equivalent to {@code completions(n, val) > 0} but needs no
         * walk over the whole tree.
         */
        private boolean agreesWithBelow(int d, int idx, int[] val) {
            int[] dimsBelow = belowDims[d];
            BitSet[][] rows = belowRows[d];
            for (int i = 0; i < dimsBelow.length; i++) {
                if (!rows[i][val[dimsBelow[i]]].get(idx)) return false;
            }
            return true;
        }

        private boolean hasCompletion(int k, int[] val) {
            return lsuffix != null ? completionsL(k, val) > 0 : completions(k, val).signum() > 0;
        }

        int dimensionSize(int dim) {
            return dimSize[dim];
        }

        /** Value indexes of the tuple with the given rank among the valid tuples; rank must be in range. */
        int[] unrank(BigInteger rank) {
            if (lsuffix != null) return unrankL(rank.longValueExact());
            int[] val = new int[dimensions];
            BigInteger remaining = rank;
            for (int k = 0; k < dimensions; k++) {
                boolean found = false;
                int dimSize = dimensionSize(k);
                for (int idx = 0; idx < dimSize; idx++) {
                    val[k] = idx;
                    BigInteger c = k == dimensions - 1
                            ? (agreesWithBelow(k, idx, val) ? BigInteger.ONE : BigInteger.ZERO)
                            : completions(k + 1, val);
                    if (c.signum() == 0) continue;
                    if (remaining.compareTo(c) < 0) {
                        found = true;
                        break;
                    }
                    remaining = remaining.subtract(c);
                }
                if (!found) {
                    throw new IllegalStateException("Rank " + rank + " could not be resolved");
                }
            }
            return val;
        }

        /** {@link #unrank(BigInteger)} in primitive arithmetic; only valid when the long tables exist. */
        private int[] unrankL(long rank) {
            int[] val = new int[dimensions];
            long remaining = rank;
            for (int k = 0; k < dimensions; k++) {
                boolean found = false;
                int size = dimensionSize(k);
                for (int idx = 0; idx < size; idx++) {
                    val[k] = idx;
                    long c = k == dimensions - 1
                            ? (agreesWithBelow(k, idx, val) ? 1L : 0L)
                            : completionsL(k + 1, val);
                    if (c == 0) continue;
                    if (remaining < c) {
                        found = true;
                        break;
                    }
                    remaining -= c;
                }
                if (!found) {
                    throw new IllegalStateException("Rank " + rank + " could not be resolved");
                }
            }
            return val;
        }
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
        LexOrderGenerator() { super(Collections.emptyList()); }

        @Override
        public Iterator<List<Object>> iterator() {
            return new LexItr(plan());
        }
    }

    /** Walks the valid tuples in lexicographic order; branches without any valid completion are skipped. */
    private final class LexItr implements Iterator<List<Object>> {
        private final Plan p;
        private final int[] val;
        private boolean started;
        private boolean ready;
        private boolean exhausted;

        LexItr(Plan p) {
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