[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Number System Algorithms

JNumberTools provides three number system algorithms related to combinatorics, enabling efficient encoding and decoding of permutations and combinations. These APIs support `BigInteger` for large indices, making them suitable for applications in combinatorial generation, ranking, and data compression.

**Currently Available Algorithms**

1. [Factorial Number System (Factoradic)](#1-factorial-number-system-factoradic)  
2. [Permutation Number System (Permutadic)](#2-permutation-number-system-permutadic)  
3. [Combinatorial Number System (Combinadic)](#3-combinatorial-number-system-combinadic)  

---

### 1. Factorial Number System (Factoradic)
The Factorial Number System, also known as Factoradic, represents numbers as sums of factorials, where each digit is constrained by its position. It is used to encode and decode unique permutations efficiently.

```java
// Convert a number to its factoradic representation
BigInteger number = BigInteger.valueOf(859);
int[] factoradic = JNumberTools.numberSystem().factoradic(number);
System.out.println(Arrays.toString(factoradic)); // Output: [7, 1, 2, 0, 1]

// Convert a factoradic representation back to a number
BigInteger result = JNumberTools.numberSystem().fromFactoradic(new int[]{7, 1, 2, 0, 1});
System.out.println(result); // Output: 859
```

### 2. Permutation Number System (Permutadic)
The Permutation Number System, or Permutadic, is a generalization of the factoradic system for k-permutations. It encodes k-permutations using a mixed-radix system, enabling efficient generation of permutations at specific indices.

```java
// Convert a number to its permutadic representation for n=5, k=3
BigInteger number = BigInteger.valueOf(100);
int[] permutadic = JNumberTools.numberSystem().permutadic(5, 3, number);
System.out.println(Arrays.toString(permutadic)); // Output: [4, 2, 0]

// Convert a permutadic representation back to a number
BigInteger result = JNumberTools.numberSystem().fromPermutadic(5, new int[]{4, 2, 0});
System.out.println(result); // Output: 100
```

### 3. Combinatorial Number System (Combinadic)
The Combinatorial Number System, or Combinadic, encodes combinations by representing their indices as sums of binomial coefficients. It is used to efficiently generate and rank combinations.

```java
// Convert a number to its combinadic representation for n=5, k=3
BigInteger number = BigInteger.valueOf(35);
int[] combinadic = JNumberTools.numberSystem().combinadic(5, 3, number);
System.out.println(Arrays.toString(combinadic)); // Output: [4, 3, 1]

// Convert a combinadic representation back to a number
BigInteger result = JNumberTools.numberSystem().fromCombinadic(new int[]{4, 3, 1});
System.out.println(result); // Output: 35
```
[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)