/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validation and decomposition of the dimension constraint graph.
 *
 * <p>Non-bridge edges identify dimensions connected through a cycle. Such dimensions are grouped into
 * loop blocks and checked against the maximum permitted product of their domain sizes. Bridge edges
 * remain between blocks, leaving a forest for the exact counting plan.</p>
 *
 * <p>Adjacency is stored as a compact sparse-row structure ({@code int[] offset} plus {@code int[] neighbors})
 * rather than {@code List<List<Integer>>}, which removes both the per-vertex list object and its
 * {@code Integer} boxing on the bridge-search hot path.</p>
 */
final class ConstraintGraph {
    static final long MAX_LOOP_COMBINATIONS = 1_000_000L;
    static final long MAX_PAIR_CELLS = 50_000_000L;

    private ConstraintGraph() {
    }

    /**
     * Rejects a rule set in which a group of dimensions that must be enumerated together has too many
     * combinations. The groups are the nodes returned by {@link #nodeOf}.
     */
    static void checkLoopBlocks(List<List<?>> dims, Collection<Long> keys, List<MultiTriggerRule> multiRules) {
        int[] block = nodeOf(dims.size(), keys, multiRules);
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
        if (multiRules.isEmpty()) return;

        // Two nodes joined by several rules (which happens when a multi-trigger rule splits a loop into two nodes)
        // are related through one combined table with a cell for every pair of node values. Bound its size by
        // the product of the nodes' domain sizes, as a pairwise rule's table is bounded by MAX_PAIR_CELLS.
        Map<Long, Integer> linksBetweenNodes = new HashMap<>();
        for (long edge : keys) {
            int a = block[(int) (edge >>> 32)];
            int b = block[(int) edge];
            if (a != b) linksBetweenNodes.merge(key(Math.min(a, b), Math.max(a, b)), 1, Integer::sum);
        }
        for (Map.Entry<Long, Integer> pair : linksBetweenNodes.entrySet()) {
            if (pair.getValue() < 2) continue;
            long pa = product[(int) (pair.getKey() >>> 32)];
            long pb = product[(int) (long) pair.getKey()];
            if (pa != 0 && pb > MAX_PAIR_CELLS / pa) {
                throw new IllegalArgumentException("A multi-trigger rule joins two groups of dimensions that have too many "
                        + "value pairs together; the limit is " + MAX_PAIR_CELLS);
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
        int[] degree = new int[n];
        for (long edge : keys) {
            degree[(int) (edge >>> 32)]++;
            degree[(int) edge]++;
        }
        int[] offset = new int[n + 1];
        for (int i = 0; i < n; i++) offset[i + 1] = offset[i] + degree[i];
        int[] neighbors = new int[offset[n]];
        int[] fill = Arrays.copyOf(offset, n);
        for (long edge : keys) {
            int u = (int) (edge >>> 32);
            int v = (int) edge;
            neighbors[fill[u]++] = v;
            neighbors[fill[v]++] = u;
        }

        int[] discovery = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        int[] nextEdge = Arrays.copyOf(offset, n);   // absolute index into `neighbors`, starts at offset[vertex]
        int[] time = {0};
        Arrays.fill(parent, -1);
        Set<Long> bridges = new HashSet<>();
        for (int vertex = 0; vertex < n; vertex++) {
            if (discovery[vertex] == 0) {
                findBridges(vertex, offset, neighbors, discovery, low, parent, nextEdge, time, bridges);
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
     * Groups the dimensions into the nodes of the counting plan. Returns, per dimension, the lowest dimension
     * index of its node.
     *
     * <p>Without multi-trigger rules the nodes are the loop blocks of {@link #blockOf}. A multi-trigger rule needs every
     * dimension in its scope in one node, because the rule is checked on complete assignments of a node. Joining
     * the scope can close a cycle through rules that were a plain chain, as in
     * {@code A-B-C-D-E} plus a rule over {@code A, E, F}. Merging that whole cycle into one node would
     * enumerate the product of all its dimensions, although the chain {@code B, C, D} has no loop of its own.
     * Instead the dimensions of the cycle other than the rule's scope form a second node that is related to the
     * scope's node through the conjunction of the rules between them, so the nodes still form a forest and each
     * is enumerated separately. When a cycle passes through more than one multi-trigger rule scope, the whole cycle
     * is merged into one node.</p>
     */
    static int[] nodeOf(int n, Collection<Long> keys, List<MultiTriggerRule> multiRules) {
        int[] node = blockOf(n, keys);
        if (multiRules.isEmpty()) return node;

        // 1. every multi-trigger rule's scope lies in one node
        for (MultiTriggerRule rule : multiRules) {
            int target = node[rule.scope[0]];
            for (int dimension : rule.scope) {
                int other = node[dimension];
                if (other == target) continue;
                int lower = Math.min(target, other);
                int higher = Math.max(target, other);
                for (int d = 0; d < n; d++) if (node[d] == higher) node[d] = lower;
                target = lower;
            }
        }

        // 2. joining scopes may have closed cycles between nodes: split each one around its scope node
        while (true) {
            int[] indexOfNode = new int[n];
            Arrays.fill(indexOfNode, -1);
            int[] reps = new int[n];   // node representatives; q of them, in encounter order
            int q = 0;
            for (int d = 0; d < n; d++) {
                if (indexOfNode[node[d]] < 0) {
                    indexOfNode[node[d]] = q;
                    reps[q++] = node[d];
                }
            }
            boolean[] special = new boolean[q];
            for (MultiTriggerRule rule : multiRules) special[indexOfNode[node[rule.scope[0]]]] = true;

            Set<Long> quotientEdges = new HashSet<>();
            for (long edge : keys) {
                int a = indexOfNode[node[(int) (edge >>> 32)]];
                int b = indexOfNode[node[(int) edge]];
                if (a != b) quotientEdges.add(key(Math.min(a, b), Math.max(a, b)));
            }
            // nodes lying on a cycle of the quotient graph (parallel links between two nodes are not a cycle)
            int[] cycle = blockOf(q, quotientEdges);
            int[] size = new int[q];
            int[] specials = new int[q];
            for (int i = 0; i < q; i++) {
                size[cycle[i]]++;
                if (special[i]) specials[cycle[i]]++;
            }

            // Within a cycle component that holds exactly one scope node, the other nodes that are connected to each
            // other become one node per connected group. Any other cycle component is merged as a whole.
            int[] merge = new int[q];
            for (int i = 0; i < q; i++) merge[i] = i;
            for (long edge : quotientEdges) {
                int a = (int) (edge >>> 32);
                int b = (int) edge;
                int c = cycle[a];
                if (c == cycle[b] && size[c] > 1 && specials[c] == 1 && !special[a] && !special[b]) union(merge, a, b);
            }
            for (int i = 0; i < q; i++) {
                int c = cycle[i];
                if (size[c] > 1 && specials[c] != 1) union(merge, i, c);
            }
            boolean changed = false;
            int[] newNode = new int[q];
            for (int i = 0; i < q; i++) {
                int root = find(merge, i);
                if (root != i) changed = true;
                newNode[i] = reps[root];   // the root is the lowest index, which is the lowest dimension
            }
            if (!changed) return node;
            for (int d = 0; d < n; d++) node[d] = newNode[indexOfNode[node[d]]];
        }
    }

    /**
     * Tarjan's depth-first bridge search. The constraint graph has at most one edge per dimension pair,
     * because {@code relations} stores one combined relation for each pair.
     */
    private static void findBridges(int root, int[] offset, int[] neighbors,
                                    int[] discovery, int[] low, int[] parent, int[] nextEdge,
                                    int[] time, Set<Long> bridges) {
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        discovery[root] = low[root] = ++time[0];
        stack.push(root);

        // Iterative DFS avoids making graph depth a Java call-stack limit.
        while (!stack.isEmpty()) {
            int vertex = stack.peek();
            if (nextEdge[vertex] < offset[vertex + 1]) {
                int next = neighbors[nextEdge[vertex]++];
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
}
