/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/** A non-binary implication with an OR of AND-groups as its antecedent. */
final class MultiTriggerRule {
    static final class Condition {
        final int dimension;
        final BitSet values;

        Condition(int dimension, BitSet values) {
            this.dimension = dimension;
            this.values = (BitSet) values.clone();
        }
    }

    final List<List<Condition>> groups;
    final int targetDimension;
    final BitSet allowedTargets;
    final int[] scope;

    // Flattened view of `groups`, filled once in the constructor. Iterating arrays by index avoids the
    // per-call Iterator allocation of the enhanced-for loops; isSatisfied is on the hot path for multi-trigger
    // traversal (once per candidate prefix per tuple) and for ConstraintPlan.fill (once per candidate).
    private final int[][] condDims;          // condDims[g][i] = dimension of group g, condition i
    private final BitSet[][] condValues;     // condValues[g][i] = allowed values of that condition

    MultiTriggerRule(List<List<Condition>> groups, int targetDimension, BitSet allowedTargets) {
        this.groups = copyGroups(groups);
        this.targetDimension = targetDimension;
        this.allowedTargets = (BitSet) allowedTargets.clone();
        int max = targetDimension;
        for (List<Condition> group : this.groups) {
            for (Condition condition : group) max = Math.max(max, condition.dimension);
        }
        boolean[] seen = new boolean[max + 1];
        seen[targetDimension] = true;
        for (List<Condition> group : this.groups) {
            for (Condition condition : group) seen[condition.dimension] = true;
        }
        int count = 0;
        for (boolean present : seen) if (present) count++;
        scope = new int[count];
        int at = 0;
        for (int d = 0; d < seen.length; d++) if (seen[d]) scope[at++] = d;

        // Build the flattened view once. Every Condition is immutable (its values are cloned in the
        // constructor), so the arrays only reference them; no further copying is needed.
        int groupCount = this.groups.size();
        condDims = new int[groupCount][];
        condValues = new BitSet[groupCount][];
        for (int g = 0; g < groupCount; g++) {
            List<Condition> group = this.groups.get(g);
            int condCount = group.size();
            int[] dims = new int[condCount];
            BitSet[] values = new BitSet[condCount];
            for (int i = 0; i < condCount; i++) {
                Condition c = group.get(i);
                dims[i] = c.dimension;
                values[i] = c.values;
            }
            condDims[g] = dims;
            condValues[g] = values;
        }
    }

    static List<List<Condition>> copyGroups(List<List<Condition>> groups) {
        List<List<Condition>> copy = new ArrayList<>(groups.size());
        for (List<Condition> group : groups) {
            // Condition is immutable — its BitSet is cloned in its own constructor and never exposed
            // mutably. Only the list containers need to be copied here; sharing Condition references
            // avoids re-cloning every BitSet on every call to this method.
            copy.add(Collections.unmodifiableList(new ArrayList<>(group)));
        }
        return Collections.unmodifiableList(copy);
    }

    boolean isInBlock(int[] dimensions) {
        for (int dimension : scope) if (positionOf(dimensions, dimension) < 0) return false;
        return true;
    }

    boolean acceptsPrefix(int assignedThrough, int[] assignment) {
        return scope[scope.length - 1] > assignedThrough || isSatisfied(assignment);
    }

    boolean isSatisfied(int[] assignment) {
        for (int g = 0; g < condDims.length; g++) {
            int[] dims = condDims[g];
            BitSet[] values = condValues[g];
            boolean matches = true;
            for (int i = 0; i < dims.length; i++) {
                if (!values[i].get(assignment[dims[i]])) {
                    matches = false;
                    break;
                }
            }
            if (matches) return allowedTargets.get(assignment[targetDimension]);
        }
        return true;
    }

    boolean isSatisfied(int[] dimensions, int[] localValues) {
        for (int g = 0; g < condDims.length; g++) {
            int[] dims = condDims[g];
            BitSet[] values = condValues[g];
            boolean matches = true;
            for (int i = 0; i < dims.length; i++) {
                int position = positionOf(dimensions, dims[i]);
                if (position < 0 || !values[i].get(localValues[position])) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                int targetPosition = positionOf(dimensions, targetDimension);
                if (targetPosition < 0) {
                    throw new IllegalStateException("A multi-trigger rule scope was split across constraint blocks");
                }
                return allowedTargets.get(localValues[targetPosition]);
            }
        }
        return true;
    }

    private static int positionOf(int[] dimensions, int dimension) {
        for (int i = 0; i < dimensions.length; i++) if (dimensions[i] == dimension) return i;
        return -1;
    }
}