/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A composable trigger condition for {@link CompoundConditionalProductBuilder}, built from atomic
 * per-dimension predicates combined with {@link #and(Condition)} and {@link #or(Condition)}.
 *
 * <p>Unlike the fluent {@code .when(dim).is(v).and(dim2).is(v2).or(dim3)...} chain (which can only
 * express an OR of AND-groups, with every group spanning the whole trigger), a {@code Condition} can
 * be nested arbitrarily, because grouping is expressed by nested method calls rather than by reading
 * left to right:
 * <pre>{@code
 * import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;
 *
 * var trigger = condition(browser).is("Safari")
 *         .and(condition(os).is("Windows").or(condition(os).is("Linux")));
 * // means: browser = Safari AND (os = Windows OR os = Linux)
 *
 * product.when(trigger).then(database).isNot("PostgreSQL");
 * }</pre>
 *
 * <p>Internally, a {@code Condition} is kept as a disjunction of conjunctions (an OR of AND-groups) of
 * atomic predicates. {@link #and(Condition)} and {@link #or(Condition)} rewrite this representation
 * directly (distributing AND over OR, or concatenating OR-groups) rather than building a tree that is
 * converted later, so the result is always already in the same OR-of-AND-groups shape that
 * {@code .when(dim)...then(dim2)...} rules compile to; nesting changes only how many AND-groups the
 * final condition has, not how it is evaluated or counted. Nesting {@code and(...)} around a condition
 * that already has several OR-groups multiplies the group count (distributing AND over OR), so deep
 * nesting of wide OR expressions can produce a large number of groups; a condition is rejected before
 * it reaches that size (see the limit below) rather than being silently accepted and then costly.
 *
 * <p>A dimension used inside a {@code Condition} is identified the same way as in the fluent API: by
 * the same {@code List} instance that was passed to {@code compoundProductOf(...)} or {@code and(...)}.
 * Resolving a dimension to its index, and a value to its matching bit set, happens when the condition
 * is attached to a product with {@code .when(Condition)}, not when the condition itself is built; the
 * same {@code Condition} object can therefore be built once (e.g., as a constant) before any product
 * exists, as long as it is only ever attached to a product that has matching dimensions and values.
 *
 * <p>Instances are immutable; {@code and} and {@code or} return new instances and do not modify the
 * operands.
 *
 * @author Deepesh Patel
 */
public final class Condition {

    /**
     * Upper bound on the number of AND-groups a condition's OR-of-AND-groups form may have once fully
     * combined. {@code and(...)} can multiply the group count of its operands (distributing AND over
     * OR); this bound catches runaway nesting at the point a condition is built, rather than letting it
     * silently compile into an excessive number of rule groups.
     */
    static final int MAX_CLAUSES = 4096;

    private final List<List<Leaf>> groups;

    private Condition(List<List<Leaf>> groups) {
        this.groups = groups;
    }

    /**
     * Starts an atomic condition on a single dimension: {@code condition(dim).is(value)},
     * {@code condition(dim).isOneOf(...)}, etc.
     *
     * @param dimension the dimension this condition refers to, identified by the same list instance
     *                   later passed to {@code compoundProductOf(...)} or {@code and(...)}
     * @return the next step, choosing how the dimension's value must relate to the given values
     * @throws NullPointerException if {@code dimension} is null
     */
    public static LeafBuilder condition(List<?> dimension) {
        Objects.requireNonNull(dimension, "dimension must not be null");
        return new LeafBuilder(dimension);
    }

    /**
     * Combines this condition with {@code other} using AND, distributing over any OR-groups already
     * present in either operand: {@code (A or B).and(C or D)} is {@code (A and C) or (A and D) or
     * (B and C) or (B and D)}.
     *
     * @param other the condition to AND with this one
     * @return a new condition; this condition and {@code other} are unchanged
     * @throws NullPointerException if {@code other} is null
     * @throws IllegalArgumentException if the combined condition would exceed {@value #MAX_CLAUSES}
     *                                   AND-groups
     */
    public Condition and(Condition other) {
        Objects.requireNonNull(other, "other must not be null");
        long clauseCount = (long) this.groups.size() * other.groups.size();
        checkClauseCount(clauseCount);
        List<List<Leaf>> combined = new ArrayList<>((int) clauseCount);
        for (List<Leaf> left : this.groups) {
            for (List<Leaf> right : other.groups) {
                List<Leaf> merged = new ArrayList<>(left.size() + right.size());
                merged.addAll(left);
                merged.addAll(right);
                combined.add(Collections.unmodifiableList(merged));
            }
        }
        return new Condition(Collections.unmodifiableList(combined));
    }

    /**
     * Combines this condition with {@code other} using OR: the result's AND-groups are the union of
     * both operands' AND-groups.
     *
     * @param other the condition to OR with this one
     * @return a new condition; this condition and {@code other} are unchanged
     * @throws NullPointerException if {@code other} is null
     * @throws IllegalArgumentException if the combined condition would exceed {@value #MAX_CLAUSES}
     *                                   AND-groups
     */
    public Condition or(Condition other) {
        Objects.requireNonNull(other, "other must not be null");
        long clauseCount = (long) this.groups.size() + other.groups.size();
        checkClauseCount(clauseCount);
        List<List<Leaf>> combined = new ArrayList<>((int) clauseCount);
        combined.addAll(this.groups);
        combined.addAll(other.groups);
        return new Condition(Collections.unmodifiableList(combined));
    }

    private static void checkClauseCount(long clauseCount) {
        if (clauseCount > MAX_CLAUSES) {
            throw new IllegalArgumentException("Combining these conditions produces " + clauseCount
                    + " AND-groups; the limit is " + MAX_CLAUSES
                    + ". This usually means a condition with wide OR expressions is nested inside "
                    + "several and(...) calls; restructure it into fewer, narrower conditions.");
        }
    }

    /** OR-of-AND-groups form, for resolution by the builder that owns the dimensions this refers to. */
    List<List<Leaf>> groups() {
        return groups;
    }

    /** An unresolved atomic predicate: a dimension (by caller list identity), candidate values, and whether negated. */
    static final class Leaf {
        final List<?> dimension;
        final Collection<?> values;
        final boolean negate;

        Leaf(List<?> dimension, Collection<?> values, boolean negate) {
            this.dimension = dimension;
            this.values = values;
            this.negate = negate;
        }
    }

    /** Next step after {@link Condition#condition(List)}: how the dimension's value must relate to given values. */
    public static final class LeafBuilder {
        private final List<?> dimension;

        private LeafBuilder(List<?> dimension) {
            this.dimension = dimension;
        }

        /** The chosen value equals {@code value}. */
        public Condition is(Object value) { return leaf(List.of(nonNull(value)), false); }
        /** The chosen value is anything except {@code value}. */
        public Condition isNot(Object value) { return leaf(List.of(nonNull(value)), true); }
        /** The chosen value is one of {@code values} (at least one is required). */
        public Condition isOneOf(Object... values) { return leaf(asList(values), false); }
        /** The chosen value is one of {@code values} (at least one is required). */
        public Condition isOneOf(Collection<?> values) { return leaf(values, false); }
        /** The chosen value is none of {@code values} (at least one is required). */
        public Condition isNoneOf(Object... values) { return leaf(asList(values), true); }
        /** The chosen value is none of {@code values} (at least one is required). */
        public Condition isNoneOf(Collection<?> values) { return leaf(values, true); }
        /** Alias of {@link #isNoneOf(Object...)}. */
        public Condition isNotIn(Object... values) { return isNoneOf(values); }
        /** Alias of {@link #isNoneOf(Collection)}. */
        public Condition isNotIn(Collection<?> values) { return isNoneOf(values); }

        private Condition leaf(Collection<?> values, boolean negate) {
            Objects.requireNonNull(values, "values must not be null");
            if (values.isEmpty()) {
                throw new IllegalArgumentException("At least one value is required");
            }
            List<Leaf> group = List.of(new Leaf(dimension, List.copyOf(values), negate));
            return new Condition(List.of(group));
        }

        private static Object nonNull(Object value) {
            return Objects.requireNonNull(value, "a condition value must not be null");
        }

        private static List<Object> asList(Object[] values) {
            Objects.requireNonNull(values, "values must not be null");
            return Arrays.asList(values);
        }
    }
}
