/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompiledConditionalProductDefinition;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;

import java.util.List;

/**
 * Selects the input style for a conditional Cartesian product.
 *
 * <p>Use {@link #ofList(List)} when the dimension values are already available, or
 * {@link #compiledForDimensions(String...)} when the rules should be declared once and
 * bound to dimension lists later.</p>
 */
public final class ConditionalProduct {
    ConditionalProduct() { }

    /**
     * Starts a conditional product with its first dimension supplied as a list.
     *
     * @param elements first dimension; copied by the builder
     * @return builder for adding dimensions and conditional rules
     */
    public ConditionalProductBuilder ofList(List<?> elements) {
        return new ConditionalProductBuilder(elements);
    }

    /**
     * Starts a reusable conditional-product definition using named dimensions.
     *
     * @param dimensionNames unique, non-null dimension names in binding order
     * @return definition to which named rules can be added before compilation
     */
    public CompiledConditionalProductDefinition compiledForDimensions(String... dimensionNames) {
        return CompiledConditionalProductDefinition.of(dimensionNames);
    }
}
