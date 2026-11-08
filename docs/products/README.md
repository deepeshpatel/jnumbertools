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

JNumberTools provides the following 4 generators for Cartesian products. All m<sup>th</sup> generators are BigInteger compatible, enabling rapid generation of Cartesian products at very large indices, such as 10<sup>100</sup>.

**Currently Available Algorithms**

1. [Simple Cartesian Product](#1-simple-cartesian-product)
   1. [Simple Cartesian product in lex order](#11-simple-cartesian-product-in-lex-order)
   2. [Every m<sup>th</sup> simple Cartesian product in lex order](#12-m-th-simple-cartesian-product)

2. [Constrained Cartesian Product](#2-constrained-cartesian-product)
   1. [Constrained Cartesian product in lex order](#21-constrained-cartesian-product-in-lex-order)
   2. [Every m<sup>th</sup> constrained Cartesian product in lex order](#22-m-th-constrained-cartesian-product)

3. [Products of ranks, random choice and random sample](#3-products-of-ranks-random-choice-and-random-sample)

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

### 3. Products of ranks, random choice and random sample

Simple and constrained products support the same `byRanks`, `choice` and `sample` operations as the other generators. `byRanks` returns the products at the given ranks, `choice` draws random products with replacement (duplicates possible), and `sample` draws random distinct products.

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