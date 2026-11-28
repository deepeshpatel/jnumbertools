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

### 1.1 Simple product in lexicographical order

```java
// Every combination of 1 pizza size, 1 pizza crust, and 1 cheese
var pizzaSize = List.of("Small", "Medium", "Large");
var pizzaCrust = List.of("Flatbread", "Neapolitan", "Thin Crust");
var cheese = List.of("Ricotta", "Mozzarella", "Cheddar");

Product.simple(pizzaSize, pizzaCrust, cheese)
    .lexOrder()
    .forEach(System.out::println);
```

### 1.2 Every M-th simple product in lexicographical order

`lexOrderMth(m, start)` generates every `m`-th product, beginning at the zero-based rank `start`.

```java
// Every 10th product starting from zero-based rank 5 (5th, 15th, 25th, ...)
Product.simple(pizzaSize, pizzaCrust, cheese)
    .lexOrderMth(BigInteger.TEN, BigInteger.valueOf(5))
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

### 2.1 Constrained product in lexicographical order

```java
// All combinations of:
// - any 1 pizza base
// - any 2 distinct cheeses
// - any 2 sauces (repetition allowed)
// - any toppings in the inclusive range 1 to 5
var pizzaBase = List.of("Small", "Medium", "Large");
var cheeses = List.of("Ricotta", "Mozzarella", "Cheddar");
var sauces = List.of("Tomato Ketchup", "White Sauce", "Green Chutney");
var toppings = List.of("Tomato", "Capsicum", "Onion", "Corn", "Mushroom");

Product.constrained()
    .ofDistinct(1, pizzaBase)
    .andDistinct(2, cheeses)
    .andMultiSelect(2, sauces)
    .andInRange(1, 5, toppings)
    .lexOrder()
    .forEach(System.out::println);
```

### 2.2 Every M-th constrained product in lexicographical order

```java
// Every 10^18th lexicographical product, starting at rank zero.
var smallAlphabets = IntStream.rangeClosed('a', 'z')
    .mapToObj(c -> (char) c).toList();
var capitalAlphabets = IntStream.rangeClosed('A', 'Z')
    .mapToObj(c -> (char) c).toList();
var symbols = List.of('~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')');
var numbers = IntStream.range(0, 20).boxed().toList();

Product.constrained()
    .ofDistinct(10, smallAlphabets)
    .andDistinct(12, capitalAlphabets)
    .andMultiSelect(3, symbols)
    .andInRange(10, 20, numbers)
    .lexOrderMth(
        BigInteger.valueOf(1_000_000_000_000_000_000L),
        BigInteger.ZERO)
    .forEach(System.out::println);
```

A shared `Calculator` can be supplied through `Product.constrained(calculator)` when combinatorial caches should be reused across constrained-product factories.

## 3. Pre-Compiled Conditional Product

Use `Product.conditional(dimensions...)` to create a product whose valid tuples are determined by conditions. Conditions are compiled before traversal, allowing invalid branches to be pruned without first generating complete tuples.

### 3.1 Pairwise precompiled conditional product

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

### 3.2 Lambda Engine - for precompiled complex conditions

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

For example, each rule below receives only the values from its named dimensions:

```java
var os = List.of("Linux", "Windows", "macOS");
var browser = List.of("Firefox", "Chrome", "Safari");
var network = List.of("WiFi", "Ethernet", "4G");
var database = List.of("PostgreSQL", "MySQL", "MongoDB");
var region = List.of("US", "EU", "APAC");

var browserAccepted = Set.of("Firefox", "Chrome");
var networkAccepted = Set.of("WiFi", "Ethernet");
var databaseAccepted = Set.of("PostgreSQL", "MySQL");
var regionAccepted = Set.of("US", "EU");
var linux = "Linux";

Product.conditional(os, browser, network, database, region)
    .withLambda(os, browser, network, database, region)
    .when(os, browser).matches((system, selectedBrowser) ->
        browserAccepted.contains(selectedBrowser))
    .when(os, network).matches((system, selectedNetwork) ->
        !linux.equals(system) || networkAccepted.contains(selectedNetwork))
    .when(os, database).matches((system, selectedDatabase) ->
        databaseAccepted.contains(selectedDatabase))
    .when(os, region).matches((system, selectedRegion) ->
        regionAccepted.contains(selectedRegion))
    .lexOrder()
    .forEach(System.out::println);
```

The Lambda Engine supports lexicographical traversal, every-M-th traversal, rank-based access, counting and random selection. Its `anyOrder()` operation does not guarantee lexicographical order.

### 3.3 Compilation limits and a hybrid pattern for conditions that do not decompose

The conditional-product implementation uses different safeguards for different stages of compilation. These limits address different sources of computational and memory cost; they should not be treated as one interchangeable limit.

**Lambda predicate probe limit — 1,000,000**

A Lambda rule is compiled by treating the last dimension in its scope as the target and the preceding dimensions as its antecedent. The predicate is evaluated once for every combination of antecedent values and every value of the target dimension:

`antecedent combinations × target values`

The total number of predicate probes is capped at 1,000,000. A rule that would exceed this limit is rejected during builder construction, with the required probe count reported in the exception message.

For example:

| Lambda scope | Dimension sizes | Predicate probes |
|---|---:|---:|
| 2 dimensions | 100 × 100 | 10,000 |
| 3 dimensions | 100 × 100 × 100 | 1,000,000 |
| 4 dimensions | 50 × 50 × 20 × 20 | 1,000,000 |
| 4 dimensions | 100 × 100 × 100 × 100 | 100,000,000 |

The limit applies to scoped Lambda predicates too. Restricting a predicate to fewer dimensions reduces the number of probes, but does not remove the limit. A two-dimension Lambda predicate, for example, is still subject to the same probe limit.

**Contracted loop-block enumeration limit — 1,000,000**

After rules are compiled, the constraint graph groups dimensions that must be enumerated together into loop blocks. The product of the original dimension sizes in a multi-dimension loop block is also bounded by 1,000,000 combinations.

This is a separate limit from Lambda predicate probing. A set of individually acceptable rules can still create a loop block whose combined dimension product exceeds this limit.

**Combined pair-table limit — 50,000,000 cells**

When multiple rules connect the same pair of contracted blocks, the implementation may need a combined table representing pairs of their joint assignments. The size of this table is bounded by 50,000,000 cells.

This is another separate safeguard. Whether it applies depends on the structure of the constraint graph.

#### Decomposing conditions into independent pairwise rules

Many multi-dimension conditions are actually several independent two-dimension rules sharing a common trigger. These can be decomposed when the original condition is a conjunction (AND) of independently-checkable conditions.

```java
// One four-dimension-looking rule...
.when((os, browser, network, database) ->
    accepted(os, browser) && accepted(os, network) && accepted(os, database))

// ...is equivalent to three independent two-dimension rules,
// because registered rules combine by intersection (AND).
.when(os, browser).matches((o, b) -> accepted(o, b))
.when(os, network).matches((o, n) -> accepted(o, n))
.when(os, database).matches((o, d) -> accepted(o, d))
```

Each two-dimension Lambda rule is compiled into a lightweight binary relation table rather than a multi-trigger rule. This avoids combining those independent rules into one larger multi-trigger scope merely because they share a dimension.

However, each scoped Lambda predicate still has to evaluate its own antecedent combinations against its target values and is subject to the 1,000,000 probe limit. The resulting constraint graph is also subject to the loop-block and combined pair-table limits described above.

This decomposition only works for conditions that are conjunctive (AND) across independently-checkable dimension pairs. It does **not** work for a condition that is a disjunction (OR) spanning three or more dimensions, such as "valid if the browser is accepted **or** the network is accepted **or** the database is accepted."

Splitting an OR into separate rules is incorrect, not just inefficient: the builder combines separately registered rules by intersection, so two rules meant to express an OR would silently produce the AND of both conditions instead. A condition like this must be evaluated jointly, and if it spans enough dimensions at large enough sizes, it can exceed the probe limit with no equivalent pairwise rewrite.

#### Hybrid approach for non-decomposable conditions

When the non-decomposable part exceeds the Lambda probe limit, and an exact `count()`, `byRanks(...)` or `lexOrder()` traversal over the fully filtered result is not required, apply that part as a plain stream filter on top of `anyOrder()`. The builder can still prune whatever part of the condition is compilable.

```java
Product.conditional(os, browser, network, database, region)
    // Compile whatever decomposes cleanly, so the builder still prunes these branches.
    .when(os).is("Linux")
    .then(database).isOneOf(databaseAccepted)
    .anyOrder()
    .stream()
    // Apply the non-decomposable OR condition directly, tuple by tuple.
    .filter(tuple -> browserAccepted.contains(tuple.get(1))
            || networkAccepted.contains(tuple.get(2)))
    .forEach(System.out::println);
```

This never triggers Lambda compilation for the OR condition, so the Lambda probe limit does not apply to it. The cost is visiting every tuple that survives the compiled constraints, just as a manually pruned stream would have to do.

The trade-off is that `count()`, `byRanks(...)` and exact-position `lexOrder()` access no longer reflect the tuples filtered out by the plain `.filter()` operation. Use this pattern only when `anyOrder()` traversal is sufficient and an exact count or rank over the fully filtered result is not required.

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