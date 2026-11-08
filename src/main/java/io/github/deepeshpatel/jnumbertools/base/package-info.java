/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
/**
 * Provides the public entry points and facade classes for JNumberTools.
 *
 * The {@link io.github.deepeshpatel.jnumbertools.base.JNumberTools} facade creates the builders and
 * services used to generate permutations, derangements, combinations, subsets and Cartesian products.
 * This package also contains {@link io.github.deepeshpatel.jnumbertools.base.Calculator} for
 * combinatorial arithmetic, {@link io.github.deepeshpatel.jnumbertools.base.RankOf} and
 * {@link io.github.deepeshpatel.jnumbertools.base.UnrankOf} for rank conversion, and
 * {@link io.github.deepeshpatel.jnumbertools.base.NumberSystem} for combinatorial number systems.
 *
 * <p>Use the facade and its returned builders as the normal public API. For example:</p>
 * <pre>{@code
 * JNumberTools.permutations()
 *     .unique("A", "B", "C")
 *     .lexOrder()
 *     .forEach(System.out::println);
 * }</pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 */
package io.github.deepeshpatel.jnumbertools.base;
