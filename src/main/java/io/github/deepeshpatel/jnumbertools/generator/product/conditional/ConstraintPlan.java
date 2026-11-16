/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.math.BigInteger;
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
     *   <li>{@code free[v][a]}: ways to choose the values of v's subtree when v has value a.</li>
     *   <li>{@code msg[c][a]}: ways to choose c's subtree when c's parent has value a.</li>
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
                belowRowList.get(v).add(e.getValue().uToV);
            }
            belowDims = new int[n][];
            belowRows = new BitSet[n][][];
            for (int d = 0; d < n; d++) {
                belowDims[d] = belowList.get(d).stream().mapToInt(Integer::intValue).toArray();
                belowRows[d] = belowRowList.get(d).toArray(new BitSet[0][]);
            }

            // nodes, ordered by their lowest dimension
            int[] blockOf = ConditionalProductBuilder.blockOf(n, relations.keySet());
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
                    Relation rel = relations.get(ConditionalProductBuilder.key(dimsOfNode[q], dimsOfNode[pos]));
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

