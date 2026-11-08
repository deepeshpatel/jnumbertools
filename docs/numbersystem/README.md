[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Number System Algorithms

JNumberTools provides four number system representations related to combinatorics: Factoradic, Permutadic, Combinadic, and Derangadic. Each API accepts a decimal value (including `BigInteger` values) and returns an object containing its representation and decimal value.

**Currently Available Algorithms**

1. [Factorial Number System (Factoradic)](#1-factorial-number-system-factoradic)
2. [Permutation Number System (Permutadic)](#2-permutation-number-system-permutadic)
3. [Combinatorial Number System (Combinadic)](#3-combinatorial-number-system-combinadic)
4. [Derangement Number System (Derangadic)](#4-derangement-number-system-derangadic)

---

### 1. Factorial Number System (Factoradic)
The Factorial Number System, also known as Factoradic, represents numbers as sums of factorials, where each digit is constrained by its position. It is used to encode and decode unique permutations efficiently.

```java
// Convert a decimal number to its factoradic representation
BigInteger number = BigInteger.valueOf(859);
Factoradic factoradic = JNumberTools.numberSystem().factoradic(number);
System.out.println(factoradic);              // Factoradic representation
System.out.println(factoradic.decimalValue); // 859
```

### 2. Permutation Number System (Permutadic)
The Permutation Number System, or Permutadic, is a generalization of the factoradic system for k-permutations. It encodes k-permutations using a mixed-radix system, enabling efficient generation of permutations at specific indices.

```java
// Convert a decimal number to its permutadic representation of degree 3
BigInteger number = BigInteger.valueOf(100);
Permutadic permutadic = JNumberTools.numberSystem().permutadic(number, 3);
System.out.println(permutadic);              // Permutadic representation
System.out.println(permutadic.decimalValue); // 100
```

### 3. Combinatorial Number System (Combinadic)
The Combinatorial Number System, or Combinadic, encodes combinations by representing their indices as sums of binomial coefficients. It is used to efficiently generate and rank combinations.

```java
// Convert a decimal number to its combinadic representation of degree 3
BigInteger number = BigInteger.valueOf(35);
Combinadic combinadic = JNumberTools.numberSystem().combinadic(number, 3);
System.out.println(combinadic);              // Combinadic representation
System.out.println(combinadic.decimalValue); // 35
```

### 4. Derangement Number System (Derangadic)
The Derangement Number System, or Derangadic, gives a bijection between the integers `0 .. !n - 1` and the derangements of `n` elements (permutations with no fixed point) in lexicographical order, where `!n` is the subfactorial. Unlike the other three systems, the accessor takes the rank and the order `n` (`n >= 2`) and returns an object that exposes the derangement itself.

```java
// The derangement of order 5 at rank 5
Derangadic derangadic = JNumberTools.numberSystem().derangadic(BigInteger.valueOf(5), 5);
System.out.println(Arrays.toString(derangadic.derangement())); // [1, 3, 0, 4, 2]
System.out.println(derangadic.rank());                         // 5
System.out.println(derangadic.count());                        // 44 derangements of order 5
System.out.println(Arrays.toString(derangadic.encoded()));     // [0, 1, 0, 2, 0] (least significant digit first)

// A new instance two ranks further on
int[] later = derangadic.add(BigInteger.TWO).derangement();    // [1, 3, 4, 2, 0]
```

`next()` advances an instance to the following rank in place, using an incremental algorithm that does not recompute from rank 0, so it is the efficient way to walk consecutive derangements. To enumerate derangements without handling digits yourself, use the [derangement generators](../derangements/README.md).
[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)