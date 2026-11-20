/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.compiled;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Rule;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Scenario;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Scenario.ScenarioData;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompiledConditionalProductDefinition;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shapes and datasets for comparing the ordinary builder with the compiled prototype. The rules name VALUES, so a
 * compiled definition is fixed once; the "different data" per call is each list shuffled with a per-dataset seed
 * (same values, different positions, so every call resolves the rules to different bit positions).
 */
public final class CompiledShapes {
    private CompiledShapes() { }

    public static final List<String> SHAPES = List.of("CHAIN-5x25", "DEAD_END-5x25", "SMALL_DOMAIN-10x4",
            "LARGE-20x50", "RULE_HEAVY-7x25");

    public record Shape(List<List<Integer>> base, List<Rule> rules) { }

    public static Shape shape(String name) {
        if (name.startsWith("RULE_HEAVY")) return ruleHeavy(7, 25);
        String[] p = name.split("-");
        ScenarioData data = Scenario.create("phase-e/" + p[0] + "/" + p[1]);
        return new Shape(data.dimensions(), data.rules());
    }

    /** A chain where every link carries four overlapping rules, so rule application dominates the setup. */
    private static Shape ruleHeavy(int n, int size) {
        List<List<Integer>> dims = new ArrayList<>();
        for (int d = 0; d < n; d++) {
            List<Integer> list = new ArrayList<>();
            for (int v = 0; v < size; v++) list.add(v);
            dims.add(list);
        }
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d < n - 1; d++) {
            rules.add(new Rule(d, range(0, size), d + 1, range(0, size - 2)));
            rules.add(new Rule(d, range(0, size / 2), d + 1, range(0, size - 4)));
            rules.add(new Rule(d, range(size / 2, size), d + 1, range(1, size - 2)));
            rules.add(new Rule(d, range(0, size - 3), d + 1, range(0, size - 6)));
        }
        return new Shape(dims, rules);
    }

    private static BitSet range(int from, int to) {
        BitSet b = new BitSet();
        b.set(from, to);
        return b;
    }

    /** {@code count} datasets: each dimension shuffled with a seed derived from the dataset number. */
    public static List<List<List<Integer>>> datasets(Shape shape, int count) {
        List<List<List<Integer>>> out = new ArrayList<>(count);
        for (int k = 0; k < count; k++) {
            Random random = new Random(k * 7919L + 1);
            List<List<Integer>> dims = new ArrayList<>();
            for (List<Integer> base : shape.base()) {
                List<Integer> copy = new ArrayList<>(base);
                Collections.shuffle(copy, random);
                dims.add(List.copyOf(copy));
            }
            out.add(dims);
        }
        return out;
    }

    private static List<Integer> values(BitSet bits) {
        List<Integer> list = new ArrayList<>();
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) list.add(i);
        return list;
    }

    // ------------------------------------------------------------------ the two ways of building

    public static ConditionalProductBuilder uncompiled(Shape shape, List<List<Integer>> dims) {
        ConditionalProductBuilder b = JNumberTools.cartesianProduct().conditional().ofList(dims.get(0));
        for (int d = 1; d < dims.size(); d++) b = b.and(dims.get(d));
        for (Rule r : shape.rules()) {
            b = b.when(dims.get(r.triggerDimension())).isOneOf(values(r.triggerValues()))
                    .then(dims.get(r.targetDimension())).isOneOf(values(r.allowedValues()));
        }
        return b;
    }

    public static CompiledConditionalProductDefinition.CompiledConditionalProduct compile(Shape shape) {
        String[] names = new String[shape.base().size()];
        for (int d = 0; d < names.length; d++) names[d] = "d" + d;
        CompiledConditionalProductDefinition def = CompiledConditionalProductDefinition.of(names);
        for (Rule r : shape.rules()) {
            def = def.when("d" + r.triggerDimension()).isOneOf(values(r.triggerValues()))
                    .then("d" + r.targetDimension()).isOneOf(values(r.allowedValues()));
        }
        return def.compile();
    }

    public static ConditionalProductBuilder bind(CompiledConditionalProductDefinition.CompiledConditionalProduct compiled,
                                                                 List<List<Integer>> dims) {
        return compiled.bind(dims.toArray(new List<?>[0]));
    }
}
