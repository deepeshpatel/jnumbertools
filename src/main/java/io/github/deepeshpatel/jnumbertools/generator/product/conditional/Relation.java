/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable allowed-value relation between two dimensions {@code u < v}.
 *
 * <p>The relation records the rules written for the pair, and builds the rows only when something reads them.
 * {@link #uToV()} gives, for each value {@code a} of {@code u}, the values of {@code v} allowed with it;
 * {@link #vToU()} gives the reverse. Adding a rule with {@link #restrict} links one more rule onto a shared chain and never
 * touches the rows, so writing rules costs time and space in proportion to the number of values, not to the number
 * of value pairs.</p>
 *
 * <p>A row depends only on which rules constrain its value, so values constrained by the same rules <em>share one
 * row object</em>, and values no rule constrains share a single full row. The table therefore takes space in
 * proportion to the number of values plus the number of distinct rows. Rows are shared between values and between
 * callers and must never be modified.</p>
 *
 * <p>Instances are thread-safe. Each direction is built at most once, on first use, and the reverse direction is
 * built only if it is asked for.</p>
 */
final class Relation {

    /**
     * A rule for this pair, as written: values in {@code trigger} allow only the values in {@code allowed}.
     */
        private record Rule(boolean triggerIsU, BitSet trigger, BitSet allowed) {
    }

    /** Up to this many rules, the set of rules that constrain a value fits in a long and rows can be shared. */
    private static final int MAX_SHARED_RULES = 62;

    private final int du;
    private final int dv;
    private final Link last;   // the rules form a chain shared with the relations they were derived from
    private final int ruleCount;
    private volatile BitSet[] uToV;
    private volatile BitSet[] vToU;

    /**
     * One rule in the chain; a relation and the relation it was derived from share every earlier link.
     */
        private record Link(Rule rule, Link previous) {
    }

    private Relation(int du, int dv, Link last, int ruleCount) {
        this.du = du;
        this.dv = dv;
        this.last = last;
        this.ruleCount = ruleCount;
    }

    /** The relation that allows every pair of values. */
    static Relation full(int du, int dv) {
        return new Relation(du, dv, null, 0);
    }

    /**
     * Returns a copy in which each trigger value allows only the supplied target values. The given sets are
     * copied, so later changes to them do not affect the relation.
     */
    Relation restrict(BitSet triggers, BitSet allowedTargets, boolean triggerIsU) {
        Rule rule = new Rule(triggerIsU, (BitSet) triggers.clone(), (BitSet) allowedTargets.clone());
        return new Relation(du, dv, new Link(rule, last), ruleCount + 1);
    }

    /** {@code uToV()[a]}: the values of {@code v} allowed together with value {@code a} of {@code u}. Rows are shared. */
    BitSet[] uToV() {
        BitSet[] rows = uToV;
        if (rows == null) {
            synchronized (this) {
                rows = uToV;
                if (rows == null) {
                    rows = buildRows(true);
                    uToV = rows;
                }
            }
        }
        return rows;
    }

    /** {@code vToU()[b]}: the values of {@code u} allowed together with value {@code b} of {@code v}. Rows are shared. */
    BitSet[] vToU() {
        BitSet[] rows = vToU;
        if (rows == null) {
            synchronized (this) {
                rows = vToU;
                if (rows == null) {
                    rows = buildRows(false);
                    vToU = rows;
                }
            }
        }
        return rows;
    }

    /**
     * True when value {@code x} lies in the dimension the rule says "when" about (the trigger side), as opposed to
     * the dimension it restricts. {@code uSide} says which dimension {@code x} belongs to.
     */
    private static boolean onTriggerSide(Rule rule, boolean uSide) {
        return rule.triggerIsU == uSide;
    }

    /** Whether the rule restricts the partners of value {@code x}. */
    private static boolean constrains(Rule rule, boolean uSide, int x) {
        return onTriggerSide(rule, uSide) ? rule.trigger.get(x) : !rule.allowed.get(x);
    }

    /** Removes from {@code row} the partner values the rule forbids for a value it constrains. */
    private static void narrow(BitSet row, Rule rule, boolean uSide) {
        if (onTriggerSide(rule, uSide)) {
            row.and(rule.allowed);          // a trigger value may only be paired with the allowed values
        } else {
            row.andNot(rule.trigger);       // a value that is not allowed may not be paired with any trigger value
        }
    }

    private BitSet[] buildRows(boolean uSide) {
        int count = uSide ? du : dv;        // number of rows
        int width = uSide ? dv : du;        // number of values in a row
        BitSet full = new BitSet(width);
        full.set(0, width);
        BitSet[] rows = new BitSet[count];
        Rule[] rules = new Rule[ruleCount];
        int at = ruleCount;
        for (Link link = last; link != null; link = link.previous) rules[--at] = link.rule;
        boolean share = rules.length <= MAX_SHARED_RULES;
        Map<Long, BitSet> bySignature = share ? new HashMap<>() : null;
        for (int x = 0; x < count; x++) {
            long signature = 0;
            if (share) {
                for (int i = 0; i < rules.length; i++) {
                    if (constrains(rules[i], uSide, x)) signature |= 1L << i;
                }
                if (signature == 0) {
                    rows[x] = full;
                    continue;
                }
                BitSet known = bySignature.get(signature);
                if (known != null) {
                    rows[x] = known;
                    continue;
                }
            }
            BitSet row = (BitSet) full.clone();
            for (Rule rule : rules) {
                if (constrains(rule, uSide, x)) narrow(row, rule, uSide);
            }
            if (share) bySignature.put(signature, row);
            rows[x] = row;
        }
        return rows;
    }
}
