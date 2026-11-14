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

JNumberTools provides the following generators for Cartesian products. All m<sup>th</sup> generators are BigInteger compatible, enabling rapid generation of Cartesian products at very large indices, such as 10<sup>100</sup>.

**Currently Available Algorithms**

1. [Simple Cartesian Product](#1-simple-cartesian-product)
   1. [Simple Cartesian product in lex order](#11-simple-cartesian-product-in-lex-order)
   2. [Every m<sup>th</sup> simple Cartesian product in lex order](#12-m-th-simple-cartesian-product)

2. [Constrained Cartesian Product](#2-constrained-cartesian-product)
   1. [Constrained Cartesian product in lex order](#21-constrained-cartesian-product-in-lex-order)
   2. [Every m<sup>th</sup> constrained Cartesian product in lex order](#22-m-th-constrained-cartesian-product)

3. [Conditional Cartesian Product](#3-conditional-cartesian-product)

4. [Products of ranks, random choice and random sample](#4-products-of-ranks-random-choice-and-random-sample)

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

### 3. Conditional Cartesian Product

A simple Cartesian product restricted by rules between two dimensions, for example "if the network is 4G, the
OS cannot be Linux". It counts, orders, ranks and samples only the valid tuples, **without generating and
filtering the full product**, so it stays fast when the full product is huge and most of it is invalid.

```java
var os = List.of("Windows", "macOS", "Linux");
var browser = List.of("Chrome", "Safari", "Firefox");
var network = List.of("WiFi", "4G");

var configs = JNumberTools.cartesianProduct()
        .conditionalProductOf(os)
        .and(browser)
        .and(network)
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

**Reading a rule.** `when(A).is(x).then(B).isOneOf(y, z)` means: whenever the value chosen from `A` is `x`,
the value chosen from `B` must be `y` or `z`. It says nothing about tuples where `A` is not `x`. All rules must
hold together. The condition side offers `is`, `isNot`, `isOneOf`, `isNoneOf` and `isNotIn`, and so does the
`then` side; each takes values or a collection of values. Several rules may share the same trigger.

**Dimensions are identified by list instance.** `when(...)` and `then(...)` take the same `List` object that was
passed to `conditionalProductOf` or `and`. Using one list instance for two dimensions is ambiguous and is rejected.
A rule value that is not in the dimension, or an empty list of values, is rejected, and so is a rule between a
dimension and itself.

**What it cannot do.**

- Rules compare two dimensions at a time. A condition on three or more dimensions at once, a sum or any other
  aggregate, or an arbitrary predicate is not supported; counting those requires evaluating every tuple.
- Rules that link dimensions in a loop are solved together as one block, so such a block is limited to 1,000,000
  value combinations, and any two dimensions to 50,000,000 value pairs.
- The order is always lexicographic. A rank is a position among the valid tuples, so ranks change when rules change.
- When almost every tuple is valid, a plain filter over a simple product is about as fast. The advantage grows as
  the rules remove more of the space.

### 4. Products of ranks, random choice and random sample

Simple, constrained and conditional products support the same `byRanks`, `choice` and `sample` operations as the other generators. `byRanks` returns the products at the given ranks, `choice` draws random products with replacement (duplicates possible), and `sample` draws random distinct products.

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
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)