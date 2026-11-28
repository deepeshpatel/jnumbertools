/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Constraint topology, bounded multi-trigger rule blocks and the two lookahead tiers built on them. The dimensions are grouped into <em>nodes</em>: a dimension that is on no loop of
 * rules is a node by itself; the dimensions of a loop (a block) form one node whose values are the
 * valid joint assignments of its dimensions. Nodes and the rules between them form a forest (when a multi-trigger rule has
 * split a loop, several rules join the same two nodes and restrict them together);
 * each tree is rooted at its node with the lowest dimension index.
 * <ul>
 *   <li>{@code counts.free[v][a]}: ways to choose the values of v's subtree when v has value a.</li>
 *   <li>{@code counts.msg[c][a]}: ways to choose c's subtree when c's parent has value a.</li>
 * </ul>
 * The number of completions of a prefix (the first k dimensions fixed) is a product over the trees; a
 * tree without a fixed dimension uses the tables directly, a tree with fixed dimensions is evaluated
 * along the paths to them only, counting only the node values that agree with the fixed dimensions.
 *
 * <p>There are two tiers. The <em>feasibility</em> tier is built with the plan: {@code canComplete[v]} holds the
 * values of node v whose subtree can be filled in so that every rule holds. It answers yes/no questions
 * ({@link #isEmpty()}, {@link #hasCompletion}, {@link #firstValid}) with bit-set operations and keeps no counts.
 * The <em>counting</em> tier, {@link #counts()}, holds the exact tables; it is built on first use, so iteration
 * and {@code isEmpty()} never pay for it.</p>
 */
final class ConstraintPlan {
    final int[][] members;         // members[node]: dimensions of the node, ascending
    final int[] size;              // number of values of the node
    final int[][][] values;        // values[node][a][pos]: value of members[node][pos]; null for a single dimension
    final int[][] children;
    final BitSet[][] fromParent;   // fromParent[c][a]: values of c allowed when c's parent has value a
    final int[] minSub;            // lowest dimension index in the subtree
    final int[] minChildSub;        // lowest dimension index among the children's subtrees (MAX_VALUE if none)
    final int[] roots;             // ascending
    final int[] firstRoot;         // firstRoot[k] = index in roots of the first root whose lowest dimension is >= k
    final int dimensions;
    final int[] dimSize;           // number of values of each dimension
    final int[][] belowDims;       // belowDims[d]: lower-numbered dimensions linked to d by a rule
    final BitSet[][][] belowRows;  // belowRows[d][i][a]: values of d allowed when belowDims[d][i] has value a
    final BitSet[] canComplete;    // canComplete[node]: values of the node whose subtree can be completed
    private final boolean[] suffixFeasible;  // suffixFeasible[i]: every tree from roots[i] on can be completed
    private final int[] nodeOrder;     // nodes, parents before children
    private volatile ConstraintCounts countsTier;
    private final List<MultiTriggerRule> multiRules;

    ConstraintPlan(List<List<?>> dims, Map<Long, Relation> relations, List<MultiTriggerRule> multiRules) {
        this.multiRules = multiRules;
        dimensions = dims.size();
        int n = dimensions;
        dimSize = new int[n];
        for (int d = 0; d < n; d++) dimSize[d] = dims.get(d).size();

        // belowDims / belowRows: count then fill. relations.entrySet() iterates in ascending key order,
        // so per-dimension entries are appended in the same order they would have been with a List<List<…>>.
        int[] belowCount = new int[n];
        for (Long key : relations.keySet()) {
            belowCount[(int) (long) key]++;
        }
        belowDims = new int[n][];
        belowRows = new BitSet[n][][];
        for (int d = 0; d < n; d++) {
            belowDims[d] = new int[belowCount[d]];
            belowRows[d] = new BitSet[belowCount[d]][];
        }
        int[] belowFill = new int[n];
        for (Map.Entry<Long, Relation> e : relations.entrySet()) {
            int u = (int) (e.getKey() >>> 32);
            int v = (int) (long) e.getKey();
            belowDims[v][belowFill[v]] = u;
            belowRows[v][belowFill[v]] = e.getValue().uToV();
            belowFill[v]++;
        }

        // Nodes, ordered by their lowest dimension. Two passes over d: first assign node indices to every
        // block, then count members per node, then fill. Members in each node stay in ascending dimension
        // order, which is what the previous List<List<Integer>> build guaranteed.
        int[] blockOf = ConstraintGraph.nodeOf(n, relations.keySet(), multiRules);
        int[] nodeOfDim = new int[n];
        int[] nodeOfBlock = new int[n];
        Arrays.fill(nodeOfBlock, -1);
        int m = 0;
        for (int d = 0; d < n; d++) {
            int b = blockOf[d];
            if (nodeOfBlock[b] < 0) {
                nodeOfBlock[b] = m++;
            }
            nodeOfDim[d] = nodeOfBlock[b];
        }
        int[] memberCount = new int[m];
        for (int d = 0; d < n; d++) memberCount[nodeOfDim[d]]++;
        members = new int[m][];
        for (int x = 0; x < m; x++) members[x] = new int[memberCount[x]];
        Arrays.fill(memberCount, 0);
        for (int d = 0; d < n; d++) {
            int x = nodeOfDim[d];
            members[x][memberCount[x]++] = d;
        }

        size = new int[m];
        values = new int[m][][];
        for (int x = 0; x < m; x++) {
            if (members[x].length == 1) {
                size[x] = dims.get(members[x][0]).size();
            } else {
                values[x] = enumerate(members[x], dims, relations, multiRules);
                size[x] = values[x].length;
            }
        }

        // Rules between two different nodes. Several rules may join the same two nodes when a multi-trigger rule
        // rule has split a loop into two nodes; all of them then restrict the pair together.
        List<Map<Integer, List<Link>>> adjacency = new ArrayList<>(m);
        for (int x = 0; x < m; x++) adjacency.add(new LinkedHashMap<>());
        for (Map.Entry<Long, Relation> e : relations.entrySet()) {
            int u = (int) (e.getKey() >>> 32);
            int v = (int) (long) e.getKey();
            if (nodeOfDim[u] == nodeOfDim[v]) continue;
            adjacency.get(nodeOfDim[u]).computeIfAbsent(nodeOfDim[v], k -> new ArrayList<>())
                    .add(new Link(u, v, e.getValue().uToV()));
            adjacency.get(nodeOfDim[v]).computeIfAbsent(nodeOfDim[u], k -> new ArrayList<>())
                    .add(new Link(v, u, e.getValue().vToU()));
        }

        fromParent = new BitSet[m][];
        children = new int[m][];
        int[] childCount = new int[m];
        // Upper bound: distinct neighbours of each node. Trim afterwards.
        for (int x = 0; x < m; x++) children[x] = new int[adjacency.get(x).size()];
        boolean[] visited = new boolean[m];
        int[] rootList = new int[m];
        int rootCount = 0;
        int[] order = new int[m];
        int orderCount = 0;
        for (int r = 0; r < m; r++) {
            if (visited[r]) continue;
            rootList[rootCount++] = r;
            visited[r] = true;
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(r);
            while (!queue.isEmpty()) {
                int x = queue.poll();
                order[orderCount++] = x;
                for (Map.Entry<Integer, List<Link>> edge : adjacency.get(x).entrySet()) {
                    int other = edge.getKey();
                    if (visited[other]) continue;
                    visited[other] = true;
                    fromParent[other] = rowsBetween(x, other, edge.getValue());
                    children[x][childCount[x]++] = other;
                    queue.add(other);
                }
            }
        }
        // Trim over-allocated children. Only nodes with fewer children than neighbours reallocate.
        for (int x = 0; x < m; x++) {
            if (childCount[x] < children[x].length) {
                children[x] = Arrays.copyOf(children[x], childCount[x]);
            }
        }

        minSub = new int[m];
        for (int idx = m - 1; idx >= 0; idx--) {
            int v = order[idx];
            minSub[v] = members[v][0];
            for (int c : children[v]) minSub[v] = Math.min(minSub[v], minSub[c]);
        }

        minChildSub = new int[m];
        for (int x = 0; x < m; x++) {
            int low = Integer.MAX_VALUE;
            for (int c : children[x]) low = Math.min(low, minSub[c]);
            minChildSub[x] = low;
        }

        roots = Arrays.copyOf(rootList, rootCount);
        firstRoot = new int[n + 1];
        int ri = 0;
        for (int k = 0; k <= n; k++) {
            while (ri < roots.length && members[roots[ri]][0] < k) ri++;
            firstRoot[k] = ri;
        }
        // BFS visits every node exactly once, so orderCount == m and order is fully populated.
        nodeOrder = order;

        canComplete = new BitSet[m];
        for (int idx = nodeOrder.length - 1; idx >= 0; idx--) {
            int v = nodeOrder[idx];
            BitSet ok = new BitSet(size[v]);
            ok.set(0, size[v]);
            for (int c : children[v]) {
                for (int a = ok.nextSetBit(0); a >= 0; a = ok.nextSetBit(a + 1)) {
                    if (!fromParent[c][a].intersects(canComplete[c])) ok.clear(a);
                }
            }
            canComplete[v] = ok;
        }
        suffixFeasible = new boolean[roots.length + 1];
        suffixFeasible[roots.length] = true;
        for (int i = roots.length - 1; i >= 0; i--) {
            suffixFeasible[i] = suffixFeasible[i + 1] && !canComplete[roots[i]].isEmpty();
        }
    }

    /** The exact counting tables; built on first use and then shared. */
    ConstraintCounts counts() {
        ConstraintCounts result = countsTier;
        if (result == null) {
            synchronized (this) {
                result = countsTier;
                if (result == null) {
                    result = new ConstraintCounts(this, nodeOrder, dimSize);
                    countsTier = result;
                }
            }
        }
        return result;
    }

    /** Whether the counting tier has been built (for tests of the lazy tiering). */
    boolean countsBuilt() {
        return countsTier != null;
    }

    /** True when no tuple satisfies every rule. Uses the feasibility tier only. */
    boolean isEmpty() {
        return !suffixFeasible[0];
    }

    /** Whether dimensions k..n-1 can be chosen so that every rule holds, given dimensions 0..k-1 in {@code val}. */
    boolean hasCompletion(int k, int[] val) {
        int fixedRoots = firstRoot[k];
        if (!suffixFeasible[fixedRoots]) return false;
        for (int i = 0; i < fixedRoots; i++) {
            if (!anyConsistent(roots[i], null, k, val)) return false;
        }
        return true;
    }

    /**
     * Whether some value of node c, among those in {@code allowed} (all if null) that agree with the dimensions
     * fixed below k, has a completable subtree. The yes/no twin of the counting tier's sum.
     */
    private boolean anyConsistent(int c, BitSet allowed, int k, int[] val) {
        int lo;
        int hi;
        if (members[c][0] >= k) {
            lo = 0;
            hi = size[c];
        } else {
            long r = range(c, k, val);
            lo = (int) (r >>> 32);
            hi = (int) r;
        }
        if (lo >= hi) return false;
        if (minChildSub[c] >= k) {
            // nothing is fixed below this node's own dimensions: a value is completable exactly when the table says so
            BitSet ok = canComplete[c];
            if (allowed == null) {
                int b = ok.nextSetBit(lo);
                return b >= 0 && b < hi;
            }
            for (int b = allowed.nextSetBit(lo); b >= 0 && b < hi; b = allowed.nextSetBit(b + 1)) {
                if (ok.get(b)) return true;
            }
            return false;
        }
        if (allowed == null) {
            for (int b = lo; b < hi; b++) if (feasibleValue(c, b, k, val)) return true;
            return false;
        }
        for (int b = allowed.nextSetBit(lo); b >= 0 && b < hi; b = allowed.nextSetBit(b + 1)) {
            if (feasibleValue(c, b, k, val)) return true;
        }
        return false;
    }

    /** Whether v's subtree can be completed when v has value a, with the dimensions below k fixed. */
    private boolean feasibleValue(int v, int a, int k, int[] val) {
        if (minSub[v] >= k) return canComplete[v].get(a);
        for (int c : children[v]) {
            BitSet allowed = fromParent[c][a];
            boolean ok = minSub[c] >= k ? allowed.intersects(canComplete[c]) : anyConsistent(c, allowed, k, val);
            if (!ok) return false;
        }
        return true;
    }

    /**
     * Range [lo, hi) of the values of node c that agree with the dimensions already fixed (below k),
     * packed as lo in the high 32 bits and hi in the low 32 bits. The fixed dimensions of a node are the
     * first ones in its member list and its values are in lexicographic order, so the agreeing values are
     * contiguous and are found by binary search.
     */
    long range(int c, int k, int[] val) {
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

    /** All assignments of the dimensions in {@code dimsOfNode} that satisfy every rule among them. */
    private static int[][] enumerate(int[] dimsOfNode, List<List<?>> dims, Map<Long, Relation> relations,
                                     List<MultiTriggerRule> multiRules) {
        int len = dimsOfNode.length;
        List<int[]> out = new ArrayList<>();
        int[] current = new int[len];
        List<MultiTriggerRule> blockRules;
        if (multiRules.isEmpty()) {
            blockRules = List.of();
        } else {
            blockRules = multiRules.stream().filter(rule -> rule.isInBlock(dimsOfNode)).toList();
        }
        fill(0, current, dimsOfNode, dims, relations, blockRules, out);
        return out.toArray(new int[0][]);
    }

    private static void fill(int pos, int[] current, int[] dimsOfNode, List<List<?>> dims,
                             Map<Long, Relation> relations, List<MultiTriggerRule> blockRules, List<int[]> out) {
        if (pos == dimsOfNode.length) {
            for (MultiTriggerRule rule : blockRules) {
                if (!rule.isSatisfied(dimsOfNode, current)) return;
            }
            out.add(current.clone());
            return;
        }
        int size = dims.get(dimsOfNode[pos]).size();
        for (int idx = 0; idx < size; idx++) {
            current[pos] = idx;
            boolean ok = true;
            for (int q = 0; q < pos && ok; q++) {
                Relation rel = relations.get(ConstraintGraph.key(dimsOfNode[q], dimsOfNode[pos]));
                if (rel != null && !rel.uToV()[current[q]].get(idx)) ok = false;
            }
            if (ok) fill(pos + 1, current, dimsOfNode, dims, relations, blockRules, out);
        }
    }

    /**
     * A rule between a dimension of one node and a dimension of another, as seen from the first node.
     *
     * @param rowsByParentValue values of childDim allowed when parentDim has value a
     */
        private record Link(int parentDim, int childDim, BitSet[] rowsByParentValue) {
    }

    /** Hash key for the values that a node value takes on the dimensions that link it to another node. */
    private static final class ValuesKey {
        final int[] values;
        private final int hash;

        ValuesKey(int[] values) {
            this.values = values;
            this.hash = Arrays.hashCode(values);
        }

        @Override public int hashCode() {
            return hash;
        }

        @Override public boolean equals(Object other) {
            return other instanceof ValuesKey k && Arrays.equals(values, k.values);
        }
    }

    /**
     * Rows[a] = values of {@code child} allowed when {@code parent} has value a: the values for which every rule
     * between the two nodes allows the pair. Parent values that agree on all the dimensions the rules touch share
     * one row, which is computed once and only read afterwards. Distinct rows are cheap to produce when they are
     * few (bounded by how many distinct combinations of values the rules' own dimensions actually take), even
     * when the nodes themselves are large; {@link ConstraintGraph#checkLoopBlocks} cannot predict that count
     * ahead of time without duplicating the choice of which node is {@code parent}, so it is measured here instead,
     * as rows are actually built, and bounded by {@link ConstraintGraph#MAX_PAIR_CELLS}.
     */
    private BitSet[] rowsBetween(int parent, int child, List<Link> links) {
        if (links.size() == 1 && values[parent] == null && values[child] == null) {
            return links.get(0).rowsByParentValue;                 // single dimensions: use the rows as they are
        }
        int count = links.size();
        int[] parentPos = new int[count];
        int[] childPos = new int[count];
        for (int i = 0; i < count; i++) {
            parentPos[i] = position(members[parent], links.get(i).parentDim);
            childPos[i] = position(members[child], links.get(i).childDim);
        }
        java.util.Map<ValuesKey, BitSet> known = new java.util.HashMap<>();
        BitSet[] rows = new BitSet[size[parent]];
        long cellsBuilt = 0;
        for (int a = 0; a < size[parent]; a++) {
            int[] parentValues = new int[count];
            for (int i = 0; i < count; i++) {
                parentValues[i] = values[parent] == null ? a : values[parent][a][parentPos[i]];
            }
            ValuesKey key = new ValuesKey(parentValues);
            BitSet row = known.get(key);
            if (row == null) {
                cellsBuilt += size[child];
                if (cellsBuilt > ConstraintGraph.MAX_PAIR_CELLS) {
                    throw new IllegalArgumentException("Two groups of dimensions linked by rules have too many "
                            + "value pairs together; the limit is " + ConstraintGraph.MAX_PAIR_CELLS);
                }
                row = new BitSet(size[child]);
                for (int b = 0; b < size[child]; b++) {
                    boolean allowed = true;
                    for (int i = 0; i < count && allowed; i++) {
                        int childValue = values[child] == null ? b : values[child][b][childPos[i]];
                        allowed = links.get(i).rowsByParentValue[parentValues[i]].get(childValue);
                    }
                    if (allowed) row.set(b);
                }
                known.put(key, row);
            }
            rows[a] = row;
        }
        return rows;
    }

    private static int position(int[] dimsOfNode, int dim) {
        for (int i = 0; i < dimsOfNode.length; i++) if (dimsOfNode[i] == dim) return i;
        throw new IllegalStateException("dimension not in node");
    }

    /** Whether a complete tuple satisfies every pairwise and multi-trigger rule. */
    boolean isValid(int[] val) {
        for (int d = 1; d < dimensions; d++) {
            if (!agreesWithBelow(d, val[d], val)) return false;
        }
        for (MultiTriggerRule rule : multiRules) {
            if (!rule.isSatisfied(val)) return false;
        }
        return true;
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
     * Checks a dimension against its earlier pairwise neighbours. multi-trigger rule validation is handled by the
     * multi-trigger traversal, not by this pairwise plan operation.
     */
    boolean agreesWithBelow(int d, int idx, int[] val) {
        int[] dimsBelow = belowDims[d];
        BitSet[][] rows = belowRows[d];
        for (int i = 0; i < dimsBelow.length; i++) {
            if (!rows[i][val[dimsBelow[i]]].get(idx)) return false;
        }
        return true;
    }

    int dimensionSize(int dim) {
        return dimSize[dim];
    }
}
