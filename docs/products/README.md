
[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Cartesian Product Generators

JNumberTools provides the following generators for Cartesian products. All m<sup>th</sup> generators are BigInteger compatible, enabling rapid generation of Cartesian products at very large indices, such as 10<sup>100</sup>.

**Currently Available Algorithms**

1. [Simple Cartesian Product](#1-simple-cartesian-product)
    1. [Simple Cartesian product in lex order](#11-simple-cartesian-product-in-lex-order)
    2. [Every m<sup>th</sup> simple Cartesian product in lex order](#12-m-th-simple-cartesian-product)

2. [Constrained Cartesian Product](#2-constrained-cartesian-product)
    1. [Constrained Cartesian product in lex order](#21-constrained-cartesian-product-in-lex-order)
    2. [Every m<sup>th</sup> constrained Cartesian product in lex order](#22-m-th-constrained-cartesian-product)

3. [Compiled Product: pairwise conditional rules](#3-compiled-product-pairwise-conditional-rules)

4. [Compound Product: nested Boolean rules](#4-compound-product-nested-boolean-rules)

5. [Lambda Engine: predicate-to-constraint compiler](#5-lambda-engine-predicate-to-constraint-compiler)

6. [Products of ranks, random choice and random sample](#6-products-of-ranks-random-choice-and-random-sample)

---

### 1. Simple Cartesian Product

#### 1.1 Simple Cartesian product in lex order

A Cartesian Product of two sets A and B, denoted by A × B, is the set of all ordered pairs (a, b) where a ∈ A and b ∈ B. This API supports the Cartesian product of n different sets, allowing direct computation of A × B × C × D × ... in a single API call.

```java
// Every combination of 1 pizza base, 1 pizza crust, and 1 cheese
var pizzaSize = List.of("Small", "Medium", "Large");
var pizzaCrust = List.of("Flatbread", "Neapolitan", "Thin Crust");
var cheese = List.of("Ricotta", "Mozzarella", "Cheddar");

JNumberTools.cartesianProduct()
   .simpleProductOf(pizzaSize)
   .and(pizzaCrust)
   .and(cheese)
   .lexOrder()
   .stream().forEach(System.out::println);
```

#### 1.2 m-th simple Cartesian product

Generates every m<sup>th</sup> Cartesian product starting from any chosen index in lexicographical order. This API directly generates the desired product without iterating through preceding values, making it highly efficient.

```java
// Every 10th combination of 1 pizza base, 1 pizza crust, and 1 cheese
// starting from index 5 (zero-based) in lex order (i.e., the 6th, 16th, 26th, etc.)
var pizzaSize = List.of("Small", "Medium", "Large");
var pizzaCrust = List.of("Flatbread", "Neapolitan", "Thin Crust");
var cheese = List.of("Ricotta", "Mozzarella", "Cheddar");

JNumberTools.cartesianProduct()
   .simpleProductOf(pizzaSize)
   .and(pizzaCrust)
   .and(cheese)
   .lexOrderMth(10, 5)
   .stream().forEach(System.out::println);
```

### 2. Constrained Cartesian Product

Generates a Cartesian product with custom constraints, allowing selection of multiple items, distinct or repeated elements, and subsets within a specified range from each input set, in lexicographical order.

The first dimension is started with one of four methods, and further dimensions are added with the matching `and...` methods:

| Start with | Adds a dimension with | Meaning |
|---|---|---|
| `constrainedProductOfDistinct(quantity, elements)` | `andDistinct(quantity, elements)` | `quantity` distinct elements (a combination) |
| `constrainedProductOfMultiSelect(quantity, elements)` | `andMultiSelect(quantity, elements)` | `quantity` elements, repetition allowed |
| `constrainedProductOfInRange(from, to, elements)` | `andInRange(from, to, elements)` | every subset whose size is in `[from, to]` |

`constrainedProductOf(quantity, elements)` is deprecated for removal; use `constrainedProductOfDistinct`, which behaves identically.

#### 2.1 Constrained Cartesian product in lex order

Similar to the simple Cartesian product but allows selecting multiple items, repetitive elements, or items within a specified range from each set.

```java
// All combinations of:
// - any 1 pizza base
// - any 2 distinct cheeses
// - any 2 sauces (repetition allowed)
// - any toppings in range 1 to 5
var pizzaBase = List.of("Small", "Medium", "Large");
var cheese = List.of("Ricotta", "Mozzarella", "Cheddar");
var sauce = List.of("Tomato Ketchup", "White Sauce", "Green Chutney");
var toppings = List.of("Tomato", "Capsicum", "Onion", "Corn", "Mushroom");

JNumberTools.cartesianProduct()
   .constrainedProductOfDistinct(1, pizzaBase)
   .andDistinct(2, cheese)
   .andMultiSelect(2, sauce)
   .andInRange(1, 5, toppings)
   .lexOrder()
   .stream().forEach(System.out::println);
```

#### 2.2 m-th constrained Cartesian product

Generates every m<sup>th</sup> constrained Cartesian product directly without calculating preceding values. This API supports BigInteger, enabling efficient generation of products at extremely large indices, such as every 10<sup>18</sup>th or 10<sup>100</sup>th combination.

```java
// Every 10^18th lexicographical combination of:
// - any 10 distinct small alphabets
// - any 12 distinct capital alphabets
// - any 3 symbols (repetition allowed)
// - all subsets in size range [10, 20] of numbers in [0, 20)
var smallAlphabets = IntStream.rangeClosed('a', 'z').mapToObj(c -> (char) c).toList();
var capitalAlphabets = IntStream.rangeClosed('A', 'Z').mapToObj(c -> (char) c).toList();
var symbols = List.of('~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')');
var numbers = IntStream.rangeClosed(0, 20).boxed().toList();

JNumberTools.cartesianProduct()
   .constrainedProductOfDistinct(10, smallAlphabets) // distinct
   .andDistinct(12, capitalAlphabets) // distinct
   .andMultiSelect(3, symbols) // repetition allowed
   .andInRange(10, 20, numbers) // all subsets in size range
   .lexOrderMth(1_000_000_000_000_000_000L, 0)
   .stream().forEach(System.out::println);
```

### 3. Compiled Product: Pairwise Conditional Rules

A compiled Cartesian product restricted by pairwise rules between two dimensions, for example "if the network is 4G, the
OS cannot be Linux". It counts, orders, ranks and samples only the valid tuples, **without generating and
filtering the full product**, so it stays fast when the full product is huge and most of it is invalid.

```java
var os = List.of("Windows", "macOS", "Linux");
var browser = List.of("Chrome", "Safari", "Firefox");
var network = List.of("WiFi", "4G");

var configs = JNumberTools.cartesianProduct()
        .conditionalProductOf(os, browser, network)
        .when(browser).is("Safari").then(os).isOneOf("macOS")
        .when(network).is("4G").then(os).isNoneOf("Linux")
        .when(network).is("4G").then(browser).isNoneOf("Chrome");

configs.count();                    // 10 (of 18 in the full product)
configs.lexOrder().stream().forEach(System.out::println);
// [Windows, Chrome, WiFi], [Windows, Firefox, WiFi], [Windows, Firefox, 4G], [macOS, Chrome, WiFi], ...

configs.lexOrderMth(3, 1);          // every 3rd valid tuple, from rank 1
configs.byRanks(List.of(BigInteger.TWO));
configs.choice(2, new Random(42));  // uniform over the valid tuples
```

**Traversal options.**

- `lexOrder()` is the default and returns every valid tuple in lexicographic order.
- `anyOrder()` returns every valid tuple exactly once, lazily and without an encounter-order guarantee.
  It is useful when tuple order is not important. Its spliterator can partition work when consumed with
  `stream().parallel()`; parallel execution is opt-in and is not guaranteed to use multiple workers.
- `lexOrder(LexOrderStrategy.PRUNED_DFS)` selects dense relation rows when they fit the implementation's
  memory cap, otherwise it falls back to `PRUNED_BITSET`.
- `lexOrder(LexOrderStrategy.DP)` selects feasibility-guided traversal. The DP plan is also used for
  exact counting and rank-based operations.
- `lexOrder(LexOrderStrategy.ADAPTIVE, estimatedLimit)` selects the implementation-policy entry point.
  In this version it resolves to `PRUNED_BITSET`; `estimatedLimit` is validated but does not select a
  different strategy. This internal policy may change in later versions.

The lexicographic strategy affects `lexOrder()` only. It does not change the semantics of
`count()`, `byRanks(...)`, `lexOrderMth(...)`, `choice(...)` or `sample(...)`.


**Reading a rule.** `when(A).is(x).then(B).isOneOf(y, z)` means: whenever the value chosen from `A` is `x`,
the value chosen from `B` must be `y` or `z`. It says nothing about tuples where `A` is not `x`. All rules must
hold together. The condition side offers `is`, `isNot`, `isOneOf`, `isNoneOf` and `isNotIn`, and so does the
`then` side; the multi-value methods accept either varargs or a collection. `isNotIn(...)` is an alias
of `isNoneOf(...)` and is retained as a familiar alternative name. Several rules may share the same trigger.

**Dimensions are identified by list instance.** `when(...)` and `then(...)` take the same `List` object that was
passed to `conditionalProductOf` or to `compoundProductOf(...)`. Using one list instance for two dimensions is ambiguous and is rejected.
A rule value that is not in the dimension, or an empty list of values, is rejected, and so is a rule between a
dimension and itself.

**What it cannot do.**

- Each pairwise rule relates two dimensions. A trigger involving three or more dimensions, or custom
  predicates on individual dimensions, belongs in the compound conditional API below. Aggregate conditions
  over a complete tuple (such as a sum across dimensions) are not supported by this API.
- Rules that link dimensions in a loop are solved together as one block, so such a block is limited to 1,000,000
  value combinations, and any two dimensions to 50,000,000 value pairs.
- `lexOrder()` returns valid tuples in lexicographic order. `anyOrder()` returns the same valid tuple set exactly once but makes no encounter-order guarantee. Ranks used by `byRanks(...)` and `lexOrderMth(...)` are always positions in lexicographic order, so ranks change when rules change.
- When almost every tuple is valid, a plain filter over a simple product is about as fast. The advantage grows as
  the rules remove more of the space.

### 4. Compound Product: Nested Boolean Rules

For rules whose trigger depends on multiple dimensions or requires nested Boolean
grouping, use the separate `compoundProductOf(...)` entry point with a composed
`Condition`. Its concrete `CompoundConditionalProductBuilder` exposes lazy
generation, exact counting and emptiness checks. Use this declarative form when the rule can be expressed clearly as nested `AND`/`OR` conditions.

```java
var os = List.of("Windows", "macOS", "Linux");
var browser = List.of("Chrome", "Safari", "Firefox");
var database = List.of("PostgreSQL", "Oracle");

import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;

var trigger = condition(browser).is("Safari").and(condition(os).is("Windows"))
        .or(condition(browser).is("Chrome").and(condition(os).is("Linux")));

var products = JNumberTools.cartesianProduct()
        .compoundProductOf(os, browser, database)
        .when(trigger)
        .then(database).isNot("PostgreSQL");

products.count();                     // exact count, without generating tuples
products.isEmpty();                   // whether any valid tuple exists
products.generate().stream().forEach(System.out::println);
```

The trigger above means
`(browser = Safari AND os = Windows) OR (browser = Chrome AND os = Linux)`.
Conditions can be nested to express either grouping order, for example
`A AND (B OR C)` or `(A OR B) AND C`. The consequence applies when the
composed trigger matches. Multiple completed rules are combined with AND.

Atomic conditions can also use `matches(Predicate<? super T>)` for custom
logic on one dimension. This predicate receives one value from that dimension;
it is not a joint predicate over multiple dimensions. The dimension is explicit,
so the compiler evaluates the predicate against that dimension's values and
compiles the matching positions before tuple generation:

```java
var trigger = condition(browser).matches(value -> value.startsWith("Ch"))
        .and(condition(memory).matches(value -> value >= 16));
```

The predicate is evaluated when the condition is attached through `.when(trigger)`,
once for each position in the builder's dimension snapshot, not during tuple
generation. Predicates should be deterministic and side-effect-free. The generic
factory infers the predicate's value type from the dimension list.

`generate()` returns a lazy, re-iterable generator. It produces each valid tuple once,
but makes no encounter-order guarantee. Lexicographic traversal, rank-based operations,
random choice and sampling are deliberately not exposed by this builder. Use
`conditionalProductOf(...)` for pairwise rules and its lexicographic/rank-based APIs.

Compound trigger scopes are compiled as bounded joint-assignment nodes. The dimensions in
a rule's scope are enumerated together, so the product of their sizes is subject to the
1,000,000 combination limit (`MAX_LOOP_COMBINATIONS`). If two of those dimensions are also
linked by a chain of pairwise rules, the dimensions of that chain are enumerated as a
separate node, joined to the scope's node by the rules between them: the chain is not merged
into the scope. Each node is subject to the same combination limit, and the pair of nodes to
the 50,000,000 value-pair limit (`MAX_PAIR_CELLS`). When a chain passes through the scopes
of several compound rules, the dimensions on that cycle are enumerated together.

### 5. Lambda Engine: Predicate-to-Constraint Compiler

Use the Lambda Engine when the rules are too complex to express clearly with the pairwise
Compiled Product or the declarative nested conditions of the Compound Product. It provides
the flexibility of writing rules in a lambda style while retaining readable code and the
existing constraint engine's pruning and optimized execution path.

The key design point is that this is **not a predicate filter applied to every generated tuple**.
Each lambda rule is compiled when the rule is added. The builder enumerates the combinations
of values in the rule's declared scope, evaluates the predicate, and converts the result into
the same internal constraint representation used by the Compound Product. Subsequent
generation, counting and emptiness checks use those compiled constraints; they do not invoke
the predicate again.

#### Typed rule over two dimensions

For exactly two dimensions, the predicate is a typed `BiPredicate`:

```java
var cores = List.of(1, 2, 4, 8);
var memoryGb = List.of(2, 4, 8, 16);

var products = JNumberTools.cartesianProduct()
        .withLambda(cores, memoryGb)
        .when(cores, memoryGb).matches((c, m) -> c * m <= 256);

products.count();                    // exact count
products.isEmpty();                  // whether any valid tuple exists
products.generate().stream().forEach(System.out::println);
```

#### Joint predicate over three or more dimensions

For an arbitrary number of dimensions, the predicate receives an `Object[]` in the same
order as the dimensions passed to `when(...)`:

```java
var cores = List.of(1, 2, 4, 8);
var memoryGb = List.of(2, 4, 8, 16);
var budget = List.of(8, 16, 32);

var products = JNumberTools.cartesianProduct()
        .withLambda(cores, memoryGb, budget)
        .when(cores, memoryGb, budget).matches(values -> {
            int c = (Integer) values[0];
            int m = (Integer) values[1];
            int b = (Integer) values[2];
            return c * m <= b;
        })
        .andWhen(memoryGb, budget).matches((m, b) -> m * 2 <= b);

products.count();
products.generate().stream().forEach(System.out::println);
```

Additional dimensions can be appended with `and(list)`. Add further rules with
`andWhen(...).matches(...)`. Every completed lambda rule must hold; rules are combined
with AND.

**Compilation and limits.**

- A two-dimension rule is compiled into pairwise constraints when possible.
- A rule whose scope has more than two dimensions is compiled into a bounded joint constraint.
- Compiling a rule is limited to 1,000,000 predicate probes (the product of the antecedent
  combinations and target values). Narrow the scope or dimension sizes if this limit is exceeded.
- Predicates must be deterministic and side-effect-free. For the `Object[]` form, read the
  supplied values in scope order; do not mutate or retain the compilation array.
- `generate()` is lazy and re-iterable and produces each valid tuple once, without an
  encounter-order guarantee. The builder also exposes exact `count()` and `isEmpty()`.
  Lexicographical traversal, rank-based access, choice and sampling are not exposed.

**When to choose it.** Prefer the pairwise Compiled Product for straightforward two-dimension
rules. Prefer the Compound Product when a rule is naturally expressed as nested Boolean
conditions over dimensions. Use the Lambda Engine when the rule is genuinely joint logic
(arithmetic, relational or application-specific computation) and the lambda makes that logic
clearer than a condition tree.


### 6. Products of ranks, random choice and random sample

Simple, constrained and compiled products support `byRanks`, `choice` and `sample`. The Compound Product and Lambda Engine intentionally expose a smaller terminal API. `byRanks` returns the products at the given ranks, `choice` draws random products with replacement (duplicates possible), and `sample` draws random distinct products.

```java
var colors = List.of("Red", "Green", "Blue");
var sizes = List.of("S", "M");
var product = JNumberTools.cartesianProduct().simpleProductOf(colors).and(sizes);

// The products at ranks 5 and 0: [Blue, M], [Red, S]
product.byRanks(List.of(BigInteger.valueOf(5), BigInteger.ZERO))
       .stream().forEach(System.out::println);

// 3 random products, duplicates possible
product.choice(3, new Random(1)).stream().forEach(System.out::println);

// 3 distinct random products
product.sample(3, new Random(1)).stream().forEach(System.out::println);

// The same operations on a constrained product (any 1 size, any 2 distinct cheeses)
var cheese = List.of("Ricotta", "Mozzarella", "Cheddar");
var constrained = JNumberTools.cartesianProduct()
        .constrainedProductOfDistinct(1, sizes)
        .andDistinct(2, cheese);
constrained.byRanks(List.of(BigInteger.TWO)).stream().forEach(System.out::println); // [S, Mozzarella, Cheddar]
```

The number of products is available from `product.count()` (6 here).
[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)
