/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Contains Cartesian-product generator implementations.
 *
 * <p>Simple products combine the elements of supplied dimensions. Constrained products allow
 * dimensions to be defined by combinatorial builders, such as combinations or subsets. Configure
 * products through the public Cartesian-product builder API.</p>
 *
 * <p>Conditional products restrict a simple product with rules between two dimensions and count, rank
 * and sample the valid tuples without enumerating the full product.</p>
 *
 * @see io.github.deepeshpatel.jnumbertools.base.CartesianProduct
 */
package io.github.deepeshpatel.jnumbertools.generator.product;
