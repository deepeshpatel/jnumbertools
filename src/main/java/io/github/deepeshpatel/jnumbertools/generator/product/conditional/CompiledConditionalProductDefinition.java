/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A reusable, named definition of a conditional Cartesian product.
 *
 * <p>Dimension names and rule structure are resolved when {@link #compile()} is called.
 * Concrete dimension lists are supplied later to {@link CompiledConditionalProduct#bind(List[])}.
 * The compiled definition can be bound repeatedly to different lists.</p>
 *
 * <p>Rule values are symbolic values, not positions. At bind time they are resolved against the
 * supplied dimensions. A rule value absent from its dimension is rejected.</p>
 */
public final class CompiledConditionalProductDefinition {
    private final List<String> names = new ArrayList<>();
    private final List<RuleSpec> rules = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();

    private CompiledConditionalProductDefinition(String... dimensionNames) {
        Objects.requireNonNull(dimensionNames, "dimensionNames");
        if (dimensionNames.length == 0) {
            throw new IllegalArgumentException("At least one dimension name is required");
        }
        for (String name : dimensionNames) {
            Objects.requireNonNull(name, "dimension name");
            if (name.isBlank()) throw new IllegalArgumentException("Dimension names must not be blank");
            if (index.putIfAbsent(name, names.size()) != null) {
                throw new IllegalArgumentException("Duplicate dimension name: " + name);
            }
            names.add(name);
        }
    }

    public static CompiledConditionalProductDefinition of(String... dimensionNames) {
        return new CompiledConditionalProductDefinition(dimensionNames);
    }

    private int dimensionIndex(String name) {
        Objects.requireNonNull(name, "dimension name");
        Integer found = index.get(name);
        if (found == null) throw new IllegalArgumentException("Unknown dimension: " + name);
        return found;
    }

    /** Starts a rule by naming the dimension whose value triggers the rule. */
    public When when(String dimension) {
        return new When(dimensionIndex(dimension));
    }

    /**
     * Compiles this definition into an immutable reusable rule plan.
     * Subsequent changes to this definition do not affect the compiled result.
     */
    public CompiledConditionalProduct compile() {
        return new CompiledConditionalProduct(List.copyOf(names), List.copyOf(rules));
    }

    private record RuleSpec(int trigger, List<?> triggerValues, boolean triggerNegated,
                            int target, List<?> allowedValues, boolean allowedNegated) { }

    public final class When {
        private final int dimension;
        private When(int dimension) { this.dimension = dimension; }

        /** The trigger value equals {@code value}. */
        public Then is(Object value) { return isOneOf(List.of(Objects.requireNonNull(value, "value"))); }

        /** The trigger value is anything except {@code value}. */
        public Then isNot(Object value) { return isNoneOf(List.of(Objects.requireNonNull(value, "value"))); }

        /** The trigger value is one of {@code values}. */
        public Then isOneOf(Object... values) { return isOneOf(asValues(values)); }

        /** The trigger value is one of {@code values}. */
        public Then isOneOf(Collection<?> values) { return new Then(dimension, snapshotValues(values), false); }

        /** The trigger value is none of {@code values}. */
        public Then isNoneOf(Object... values) { return isNoneOf(asValues(values)); }

        /** The trigger value is none of {@code values}. */
        public Then isNoneOf(Collection<?> values) { return new Then(dimension, snapshotValues(values), true); }

        /** Alias of {@link #isNoneOf(Object...)}. */
        public Then isNotIn(Object... values) { return isNoneOf(values); }

        /** Alias of {@link #isNoneOf(Collection)}. */
        public Then isNotIn(Collection<?> values) { return isNoneOf(values); }
    }

    public final class Then {
        private final int trigger;
        private final List<?> triggerValues;
        private final boolean triggerNegated;

        private Then(int trigger, List<?> triggerValues, boolean triggerNegated) {
            this.trigger = trigger;
            this.triggerValues = triggerValues;
            this.triggerNegated = triggerNegated;
        }

        /** Names the dimension restricted when the trigger condition holds. */
        public Target then(String dimension) {
            int target = dimensionIndex(dimension);
            if (target == trigger) throw new IllegalArgumentException("A rule needs two different dimensions");
            return new Target(trigger, triggerValues, triggerNegated, target);
        }
    }

    public final class Target {
        private final int trigger;
        private final List<?> triggerValues;
        private final boolean triggerNegated;
        private final int target;

        private Target(int trigger, List<?> triggerValues, boolean triggerNegated, int target) {
            this.trigger = trigger;
            this.triggerValues = triggerValues;
            this.triggerNegated = triggerNegated;
            this.target = target;
        }

        /** The target value must equal {@code value}. */
        public CompiledConditionalProductDefinition is(Object value) {
            return isOneOf(List.of(Objects.requireNonNull(value, "value")));
        }

        /** The target value must be anything except {@code value}. */
        public CompiledConditionalProductDefinition isNot(Object value) {
            return isNoneOf(List.of(Objects.requireNonNull(value, "value")));
        }

        /** The target value must be one of {@code allowedValues}. */
        public CompiledConditionalProductDefinition isOneOf(Object... allowedValues) {
            return isOneOf(asValues(allowedValues));
        }

        /** The target value must be one of {@code allowedValues}. */
        public CompiledConditionalProductDefinition isOneOf(Collection<?> allowedValues) {
            return add(allowedValues, false);
        }

        /** The target value must be anything except {@code forbiddenValues}. */
        public CompiledConditionalProductDefinition isNoneOf(Object... forbiddenValues) {
            return isNoneOf(asValues(forbiddenValues));
        }

        /** The target value must be anything except {@code forbiddenValues}. */
        public CompiledConditionalProductDefinition isNoneOf(Collection<?> forbiddenValues) {
            return add(forbiddenValues, true);
        }

        /** Alias of {@link #isNoneOf(Object...)}. */
        public CompiledConditionalProductDefinition isNotIn(Object... values) { return isNoneOf(values); }

        /** Alias of {@link #isNoneOf(Collection)}. */
        public CompiledConditionalProductDefinition isNotIn(Collection<?> values) { return isNoneOf(values); }

        private CompiledConditionalProductDefinition add(Collection<?> values, boolean negated) {
            rules.add(new RuleSpec(trigger, triggerValues, triggerNegated, target, snapshotValues(values), negated));
            return CompiledConditionalProductDefinition.this;
        }
    }

    private static List<?> asValues(Object[] values) {
        Objects.requireNonNull(values, "values");
        return snapshotValues(java.util.Arrays.asList(values));
    }

    private static List<?> snapshotValues(Collection<?> values) {
        Objects.requireNonNull(values, "values");
        if (values.isEmpty()) throw new IllegalArgumentException("At least one value is required");
        List<?> copy = List.copyOf(values);
        return Collections.unmodifiableList(copy);
    }

    public static final class CompiledConditionalProduct {
        private final List<String> names;
        private final List<RuleSpec> rules;
        private final int[] triggerDims;
        private final int[] targetDims;

        private CompiledConditionalProduct(List<String> names, List<RuleSpec> rules) {
            this.names = names;
            this.rules = rules;
            triggerDims = new int[rules.size()];
            targetDims = new int[rules.size()];
            for (int i = 0; i < rules.size(); i++) {
                triggerDims[i] = rules.get(i).trigger();
                targetDims[i] = rules.get(i).target();
            }
        }

        /** Binds one list per declared dimension, in declaration order. */
        public ConditionalProductBuilder bind(List<?>... lists) {
            Objects.requireNonNull(lists, "lists");
            if (lists.length != names.size()) {
                throw new IllegalArgumentException("Expected " + names.size() + " lists for dimensions "
                        + names + " but got " + lists.length);
            }

            List<List<?>> snapshots = new ArrayList<>(lists.length);
            List<List<?>> originals = new ArrayList<>(lists.length);
            for (List<?> list : lists) {
                Objects.requireNonNull(list, "dimension list");
                Util.validateInput(list);
                snapshots.add(Util.copyOfNoNulls(list));
                originals.add(list);
            }
            List<List<?>> dimensions = Collections.unmodifiableList(snapshots);
            DimensionIndexes indexes = new DimensionIndexes(dimensions);
            RuleCompiler compiler = new RuleCompiler(dimensions, dimensions, indexes);

            Map<Long, Relation> pending = new LinkedHashMap<>();
            for (RuleSpec rule : rules) {
                BitSet triggerValues = compiler.matching(rule.trigger(), rule.triggerValues(), rule.triggerNegated());
                BitSet allowedValues = compiler.matching(rule.target(), rule.allowedValues(), rule.allowedNegated());
                int u = Math.min(rule.trigger(), rule.target());
                int v = Math.max(rule.trigger(), rule.target());
                long key = ConstraintGraph.key(u, v);
                Relation relation = pending.get(key);
                if (relation == null) {
                    long cells = (long) dimensions.get(u).size() * dimensions.get(v).size();
                    if (cells > ConstraintGraph.MAX_PAIR_CELLS) {
                        throw new IllegalArgumentException("The two dimensions have too many value pairs (" + cells
                                + "); the limit is " + ConstraintGraph.MAX_PAIR_CELLS);
                    }
                    relation = Relation.full(dimensions.get(u).size(), dimensions.get(v).size());
                }
                pending.put(key, relation.restrict(triggerValues, allowedValues, rule.trigger() == u));
            }

            PersistentRelationMap relations = PersistentRelationMap.empty();
            for (Map.Entry<Long, Relation> entry : pending.entrySet()) {
                relations = relations.with(entry.getKey(), entry.getValue());
            }
            if (!pending.isEmpty()) ConstraintGraph.checkLoopBlocks(dimensions, relations.keySet());
            ConstraintModel model = new ConstraintModel(dimensions, relations);
            return ConditionalProductBuilder.fromCompiledModel(model, originals);
        }

        public List<String> dimensionNames() { return names; }
    }
}
