[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Math Functions in Calculator

The `Calculator` class in **JNumberTools** provides efficient, thread-safe mathematical functions for combinatorics and number theory computations. It uses memoization for performance, supporting operations like factorials, binomial coefficients, multinomial coefficients, and more, with `BigInteger` compatibility for large-scale calculations.

**Key Features:**
- Thread-safe with synchronized caches for `nCr`, `nPr`, factorial, and subfactorial values.
- Optimized for both small and large inputs using memoization and `BigInteger`.
- Includes utility methods for combinatorial counts, number bounds, and basic arithmetic operations.

**Currently Available Functions**

1. [Factorial and Subfactorial](#1-factorial-and-subfactorial)
   1. [Factorial](#11-factorial)
   2. [Subfactorial](#12-subfactorial)
   3. [Factorial Upper Bound](#13-factorial-upper-bound)
2. [Combinations and Permutations](#2-combinations-and-permutations)
   1. [nCr (Binomial Coefficient)](#21-ncr-binomial-coefficient)
   2. [nCr with Repetition](#22-ncr-with-repetition)
   3. [nCr Upper Bound](#23-ncr-upper-bound)
   4. [nPr (Permutation Coefficient)](#24-npr-permutation-coefficient)
3. [Multiset Calculations](#3-multiset-calculations)
   1. [Multinomial Coefficient](#31-multinomial-coefficient)
   2. [Total m-th Multinomial](#32-total-m-th-multinomial)
   3. [Multiset Combinations Count All](#33-multiset-combinations-count-all)
   4. [Multiset Combinations Count](#34-multiset-combinations-count)
   5. [Multiset Combination Count starting from index](#35-multiset-combination-count-starting-from-given-index)
4. [Subset Calculations](#4-subset-calculations)
   1. [Total Subsets in Range](#41-total-subsets-in-range)
5. [Arithmetic Operations](#5-arithmetic-operations)
   1. [Power](#51-power)
   2. [Greatest Common Divisor (GCD)](#52-greatest-common-divisor-gcd)
   3. [Least Common Multiple (LCM)](#53-least-common-multiple-lcm)
6. [Special Combinatorial Functions](#6-special-combinatorial-functions)
   1. [Rencontres Number](#61-rencontres-number)

---

### 1. Factorial and Subfactorial

#### 1.1 Factorial
Computes the factorial of a non-negative integer `n` (n!), using a memoized cache for efficiency.

```java
Calculator calc = new Calculator();
// Computes 5! = 120
BigInteger result = calc.factorial(5);
System.out.println(result); // Output: 120
```

#### 1.2 Subfactorial
Calculates the subfactorial (!n), the number of derangements of n items, using memoization.

```java
Calculator calc = new Calculator();
BigInteger result = calc.subFactorial(4);
System.out.println(result); // Output: 9
```

#### 1.3 Factorial Upper Bound
Finds the smallest n such that n! exceeds a given value.

```java
Calculator calc = new Calculator();
int result = calc.factorialUpperBound(BigInteger.valueOf(1000));
System.out.println(result); // Output: 7
```

### 2. Combinations and Permutations

#### 2.1 nCr (Binomial Coefficient)
Calculates nCr, the number of ways to choose r items from n without repetition, using memoization.

```java
Calculator calc = new Calculator();
BigInteger result = calc.nCr(5, 2);
System.out.println(result); // Output: 10
```

#### 2.2 nCr with Repetition
Computes the binomial coefficient with repetition allowed, equivalent to C(n + r - 1, r).

```java
Calculator calc = new Calculator();
BigInteger result = calc.nCrRepetitive(4, 3);
System.out.println(result); // Output: 20
```

#### 2.3 nCr Upper Bound
Finds the smallest n such that nCr(n, r) exceeds a given value.

```java
Calculator calc = new Calculator();
int result = calc.nCrUpperBound(3, BigInteger.valueOf(100));
System.out.println(result); // Output: 9
```

#### 2.4 nPr (Permutation Coefficient)
Calculates nPr, the number of ways to arrange r items from n, using memoization.

```java
Calculator calc = new Calculator();
BigInteger result = calc.nPr(5, 3);
System.out.println(result); // Output: 60
```

### 3. Multiset Calculations

#### 3.1 Multinomial Coefficient
Computes the multinomial coefficient for given counts, representing distinct permutations of a multiset.

```java
Calculator calc = new Calculator();
BigInteger result = calc.multinomial(2, 1, 3);
System.out.println(result); // Output: 60
```

#### 3.2 Total m-th Multinomial
Calculates the total number of m-th permutations for a multiset, given a start index and step m.

```java
Calculator calc = new Calculator();
BigInteger result = calc.totalMthMultinomial(0, 2, 2, 1, 3);
System.out.println(result); // Output: 30
```

#### 3.3 Multiset Combinations Count All
Computes the number of ways to select exactly s items from a multiset for all s in the range [0, ⌊total⌋/2], where total is the sum of the frequencies. Returns an array of counts for selecting 0 to ⌊total/2⌋ items, using dynamic programming.

```java
int[] result = Calculator.multisetCombinationsCountAll(2, 1);
System.out.println(Arrays.toString(result)); // Output: [1, 2, 1]
```

#### 3.4 Multiset Combinations Count
Calculates the number of ways to select exactly k items from a multiset.

```java
BigInteger result = Calculator.multisetCombinationsCount(2, 2, 1);
System.out.println(result); // Output: 2
```

#### 3.5 Multiset Combination Count starting from given index
Counts the number of ways to select exactly k items from a multiset defined by frequencies, considering only the item types from the specified index onward.

```java
int k = 5;
int index = 2;
int[] frequencies = {10, 12, 5, 8};
BigInteger result = Calculator.multisetCombinationsCountStartingFromIndex(k, index, frequencies);
```

### 4. Subset Calculations

#### 4.1 Total Subsets in Range
Computes the total number of subsets of sizes within a given range from n elements.

```java
Calculator calc = new Calculator();
BigInteger result = calc.totalSubsetsInRange(1, 2, 4);
System.out.println(result); // Output: 10
```

### 5. Arithmetic Operations

#### 5.1 Power
Calculates base raised to exponent, supporting both long and BigInteger inputs.

```java
Calculator calc = new Calculator();
BigInteger result = calc.power(2, 10);
System.out.println(result); // Output: 1024
```

#### 5.2 Greatest Common Divisor (GCD)
Computes the GCD of multiple BigInteger numbers using the binary GCD algorithm.

```java
Calculator calc = new Calculator();
BigInteger result = calc.gcd(BigInteger.valueOf(48), BigInteger.valueOf(18), BigInteger.valueOf(27));
System.out.println(result); // Output: 3
```

#### 5.3 Least Common Multiple (LCM)
Computes the LCM of multiple BigInteger numbers using a hybrid iterative/recursive approach.

```java
Calculator calc = new Calculator();
BigInteger result = calc.lcm(BigInteger.valueOf(4), BigInteger.valueOf(6), BigInteger.valueOf(8));
System.out.println(result); // Output: 24
```

### 6. Special Combinatorial Functions

#### 6.1 Rencontres Number
Calculates the number of permutations of n items with exactly k fixed points.

```java
Calculator calc = new Calculator();
BigInteger result = calc.rencontresNumber(4, 1);
System.out.println(result); // Output: 8
```
[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)