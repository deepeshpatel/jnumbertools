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

JNumberTools provides three number system representations related to combinatorics: Factoradic, Permutadic, and Combinadic. Each API accepts a decimal value (including `BigInteger` values) and returns an object containing its representation and decimal value.

**Currently Available Algorithms**

1. [Factorial Number System (Factoradic)](#1-factorial-number-system-factoradic)  
2. [Permutation Number System (Permutadic)](#2-permutation-number-system-permutadic)  
3. [Combinatorial Number System (Combinadic)](#3-combinatorial-number-system-combinadic)  

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
[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)