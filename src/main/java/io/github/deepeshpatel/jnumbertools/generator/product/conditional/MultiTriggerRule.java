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
    }

    static List<List<Condition>> copyGroups(List<List<Condition>> groups) {
        List<List<Condition>> copy = new ArrayList<>(groups.size());
        for (List<Condition> group : groups) {
            List<Condition> conditions = new ArrayList<>(group.size());
            for (Condition condition : group) conditions.add(new Condition(condition.dimension, condition.values));
            copy.add(Collections.unmodifiableList(conditions));
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
        for (List<Condition> group : groups) {
            boolean matches = true;
            for (Condition condition : group) {
                if (!condition.values.get(assignment[condition.dimension])) {
                    matches = false;
                    break;
                }
            }
            if (matches) return allowedTargets.get(assignment[targetDimension]);
        }
        return true;
    }

    boolean isSatisfied(int[] dimensions, int[] localValues) {
        for (List<Condition> group : groups) {
            boolean matches = true;
            for (Condition condition : group) {
                int position = positionOf(dimensions, condition.dimension);
                if (position < 0 || !condition.values.get(localValues[position])) {
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
