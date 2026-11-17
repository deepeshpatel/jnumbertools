/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Exact completion-count plan. The dimensions are grouped into <em>nodes</em>: a dimension that is on no loop of
 * rules is a node by itself; the dimensions of a loop (a block) form one node whose values are the
 * valid joint assignments of its dimensions. Nodes and the single rules between them form a forest;
 * each tree is rooted at its node with the lowest dimension index.
 * <ul>
 *   <li>{@code counts.free[v][a]}: ways to choose the values of v's subtree when v has value a.</li>
 *   <li>{@code counts.msg[c][a]}: ways to choose c's subtree when c's parent has value a.</li>
 * </ul>
 * The number of completions of a prefix (the first k dimensions fixed) is a product over the trees; a
 * tree without a fixed dimension uses the tables directly, a tree with fixed dimensions is evaluated
 * along the paths to them only, counting only the node values that agree with the fixed dimensions.
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
    final ConstraintCounts counts;

    ConstraintPlan(List<List<?>> dims, Map<Long, Relation> relations) {
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
            belowRowList.get(v).add(e.getValue().uToV());
        }
        belowDims = new int[n][];
        belowRows = new BitSet[n][][];
        for (int d = 0; d < n; d++) {
            belowDims[d] = belowList.get(d).stream().mapToInt(Integer::intValue).toArray();
            belowRows[d] = belowRowList.get(d).toArray(new BitSet[0][]);
        }

        // nodes, ordered by their lowest dimension
        int[] blockOf = ConstraintGraph.blockOf(n, relations.keySet());
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

        minSub = new int[m];
        for (int idx = order.size() - 1; idx >= 0; idx--) {
            int v = order.get(idx);
            minSub[v] = members[v][0];
            for (int c : children[v]) minSub[v] = Math.min(minSub[v], minSub[c]);
        }

        minChildSub = new int[m];
        for (int x = 0; x < m; x++) {
            int low = Integer.MAX_VALUE;
            for (int c : children[x]) low = Math.min(low, minSub[c]);
            minChildSub[x] = low;
        }

        roots = rootList.stream().mapToInt(Integer::intValue).toArray();
        firstRoot = new int[n + 1];
        int ri = 0;
        for (int k = 0; k <= n; k++) {
            while (ri < roots.length && members[roots[ri]][0] < k) ri++;
            firstRoot[k] = ri;
        }
        counts = new ConstraintCounts(this, order.stream().mapToInt(Integer::intValue).toArray(), dimSize);
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
                Relation rel = relations.get(ConstraintGraph.key(dimsOfNode[q], dimsOfNode[pos]));
                if (rel != null && !rel.uToV()[current[q]].get(idx)) ok = false;
            }
            if (ok) fill(pos + 1, current, dimsOfNode, dims, relations, out);
        }
    }

    /** Rows[a] = values of {@code child} allowed when {@code parent} has value a, through the one rule between them. */
    private BitSet[] rowsBetween(int parent, int parentDim, int child, int childDim, Relation rel,
                                 boolean parentIsLow) {
        BitSet[] rowsOfDim = parentIsLow ? rel.uToV() : rel.vToU();   // by the parent's dimension value
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

    /** Whether a complete tuple satisfies every rule. */
    boolean isValid(int[] val) {
        for (int d = 1; d < dimensions; d++) {
            if (!agreesWithBelow(d, val[d], val)) return false;
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
                if (last || counts.hasCompletion(k + 1, val)) return idx;
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
            if (last || counts.hasCompletion(k + 1, val)) return idx;
        }
        return -1;
    }

    /**
     * For the last dimension only: value idx is valid iff every rule between it and the (already
     * consistent) earlier dimensions holds. Equivalent to {@code completions(n, val) > 0} but needs no
     * walk over the whole tree.
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
