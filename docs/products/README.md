[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Cartesian Product Generators

The `Product` static entry point in `io.github.deepeshpatel.jnumbertools.entrypoint` provides three product builder families:

1. **Simple Product** — generates the full Cartesian product.
2. **Constrained Product** — uses combinations, multi-selections or subset ranges as product dimensions.
3. **Pre-Compiled Conditional Product** — applies compiled conditions between dimensions, using either pairwise rules or the Lambda Engine.

## 1. Simple Product

Pass one list for each dimension. The lists are supplied in tuple order.

### 1.1 Product in lexicographical order

```java
var sizes = List.of("Small", "Medium", "Large");
var crusts = List.of("Flatbread", "Neapolitan");
var cheeses = List.of("Ricotta", "Mozzarella");

Product.simple(sizes, crusts, cheeses)
    .lexOrder()
    .forEach(System.out::println);
```

### 1.2 Every M-th product in lexicographical order

`lexOrderMth(m, start)` generates every `m`-th product, beginning at the zero-based rank `start`.

```java
Product.simple(sizes, crusts, cheeses)
    .lexOrderMth(5, 0)
    .forEach(System.out::println);
```

The simple-product builder also supports `byRanks(...)`, `choice(...)`, `sample(...)`, `count()` and `isEmpty()`.

## 2. Constrained Product

Use `Product.constrained()` to start a constrained product. Each dimension is defined by a combination or subset operation.

| First-dimension method | Meaning |
|---|---|
| `ofDistinct(quantity, elements)` | Fixed-size combinations without repetition |
| `ofMultiSelect(quantity, elements)` | Fixed-size combinations with repetition |
| `ofInRange(from, to, elements)` | Subsets whose sizes are in the inclusive range |

Add subsequent dimensions with the corresponding `andDistinct`, `andMultiSelect` or `andInRange` methods.

### 2.1 Product in lexicographical order

```java
var colors = List.of("Red", "Green", "Blue");
var sizes = List.of("S", "M", "L", "XL");

Product.constrained()
    .ofDistinct(2, colors)
    .andMultiSelect(2, sizes)
    .lexOrder()
    .forEach(System.out::println);
```

### 2.2 Every M-th product in lexicographical order

```java
Product.constrained()
    .ofDistinct(2, colors)
    .andMultiSelect(2, sizes)
    .lexOrderMth(5, 0)
    .forEach(System.out::println);
```

A shared `Calculator` can be supplied through `Product.constrained(calculator)` when combinatorial caches should be reused across constrained-product factories.

## 3. Pre-Compiled Conditional Product

Use `Product.conditional(dimensions...)` to create a product whose valid tuples are determined by conditions. Conditions are compiled before traversal, allowing invalid branches to be pruned without first generating complete tuples.

### 3.1 Pairwise rules

Pairwise rules express relationships between two dimensions using the values that satisfy the business requirement.

```java
var operatingSystems = List.of("Linux", "Windows", "macOS");
var browsers = List.of("Firefox", "Safari", "Chrome");
var networks = List.of("4G", "WiFi", "Ethernet");

Product.conditional(operatingSystems, browsers, networks)
    .when(operatingSystems).is("Linux")
    .then(browsers).isOneOf("Firefox", "Safari")
    .lexOrder()
    .forEach(System.out::println);
```

The pairwise builder supports lexicographical traversal, every-M-th traversal, rank-based access, counting and random selection.

### 3.2 Lambda Engine

Use `withLambda(...)` when a condition involves multiple dimensions and is more naturally expressed as a predicate. Typed whole-product predicate overloads are available for one through five dimensions; the no-argument `withLambda()` form provides an array-based predicate for arbitrary dimension counts.

```java
Product.conditional(operatingSystems, browsers, networks)
    .withLambda(operatingSystems, browsers, networks)
    .when((os, browser, network) ->
        !os.equals("Linux") || browser.equals("Firefox") || browser.equals("Safari"))
    .lexOrder()
    .forEach(System.out::println);
```

Scoped predicates can also be declared with `.when(dimensions...).matches(predicate)` when the predicate depends only on selected dimensions. Lambda predicates are compiled into constraints, subject to the documented probe limit; this is not an unbounded general-purpose constraint solver.

The Lambda Engine supports lexicographical traversal, every-M-th traversal, rank-based access, counting and random selection. Its `anyOrder()` operation does not guarantee lexicographical order.

## 4. Conditional Product vs. Pruned Stream Filtering

A conditional product expresses the validity rules directly and lets the builder perform pruning. A manually pruned stream must encode the filtering and pruning logic itself.

| Aspect | Pre-Compiled Conditional Product | Pruned Stream Filtering |
|---|---|---|
| **Readability** | Conditions express the business requirement directly. | Pruning logic is more complex to write and maintain. |
| **Filtering logic** | `matches()` expresses the condition that should be satisfied. | `filter()` requires the negated condition, making the filtering logic less readable. |
| **Execution** | In the current benchmarks, around **2× faster** when more than 50% of tuples are valid; **many times faster** when fewer than 5% are valid. | Performance degrades as more invalid tuples must be visited and filtered. |
| **Pruning** | Pruning is handled automatically by the builder. | The developer must implement pruning manually and may miss pruning opportunities. |
| **Total count** | `count()` returns the exact count without enumerating the tuples. | Requires enumerating the stream. |
| **Access by rank** | `byRanks(...)` retrieves requested ranks without generating all preceding tuples. | Requires enumeration to reach the requested position. |

The performance figures reflect the workloads measured in the current JNumberTools benchmarks; actual results depend on dimension sizes, validity distribution and predicate cost.

## 5. Choosing a Product Builder

- Use **Simple Product** when every tuple in the Cartesian product is valid.
- Use **Constrained Product** when dimensions are combinations, multi-selections or subsets within a size range.
- Use **Pre-Compiled Conditional Product — Pairwise Rules** when validity depends on pairwise relationships between dimensions.
- Use **Pre-Compiled Conditional Product — Lambda Engine** when validity is more naturally expressed as a predicate involving multiple dimensions.

The available terminal operations depend on the returned builder type. Consult the Javadoc for the exact builder contract rather than assuming all product builders expose identical methods.

[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)
