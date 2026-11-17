/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validation and decomposition of the dimension constraint graph.
 *
 * <p>Non-bridge edges identify dimensions connected through a cycle. Such dimensions are grouped into
 * loop blocks and checked against the maximum permitted product of their domain sizes. Bridge edges
 * remain between blocks, leaving a forest for the exact counting plan.</p>
 */
final class ConstraintGraph {
    static final long MAX_LOOP_COMBINATIONS = 1_000_000L;
    static final long MAX_PAIR_CELLS = 50_000_000L;

    private ConstraintGraph() {
    }

    /** Rejects a rule set in which a group of dimensions linked in loops has too many combinations. */
    static void checkLoopBlocks(List<List<?>> dims, Collection<Long> keys) {
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
     * Whether the constraint graph contains a loop block. This uses only the graph topology and does
     * not enumerate any value combinations or construct a constraint plan.
     */
    static boolean hasLoopBlock(int n, Collection<Long> keys) {
        int[] block = blockOf(n, keys);
        boolean[] seen = new boolean[n];
        for (int b : block) {
            if (seen[b]) return true;
            seen[b] = true;
        }
        return false;
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
}
