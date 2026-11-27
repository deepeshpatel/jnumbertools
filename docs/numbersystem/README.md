[Home](../../README.md)

# Number System Algorithms

JNumberTools provides Factoradic, Permutadic, Combinadic and Derangadic representations. These are now created directly through their representation classes rather than through a central number-system facade.

## Factoradic

```java
Factoradic factoradic = Factoradic.of(BigInteger.valueOf(859));
System.out.println(factoradic);
System.out.println(factoradic.decimalValue); // 859
```

Factoradic represents a non-negative integer using factorial place values and is used for unique permutation ranking and unranking.

## Permutadic

```java
Permutadic permutadic = Permutadic.of(BigInteger.valueOf(100), 3);
System.out.println(permutadic);
System.out.println(permutadic.decimalValue); // 100
```

Permutadic generalizes the factorial mixed-radix representation to k-permutations. The degree is supplied explicitly.

## Combinadic

Combinadic conversion uses a `Calculator` for binomial-coefficient calculations.

```java
Calculator calculator = new Calculator();
Combinadic combinadic = Combinadic.of(BigInteger.valueOf(35), 3, calculator);
System.out.println(combinadic);
System.out.println(combinadic.decimalValue); // 35
```

## Derangadic

Derangadic maps ranks to derangements of a fixed order. Construct it with the order, zero-based rank and calculator.

```java
Calculator calculator = new Calculator();
Derangadic derangadic = new Derangadic(5, BigInteger.valueOf(5), calculator);
int[] derangement = derangadic.derangement();
int[] digits = derangadic.encoded();
```

The encoded digit array is least-significant-digit first. The returned arrays are live internal references; do not modify them.

For application-level ranking and unranking, prefer the `Rank` and `Unrank` entry points. The number-system classes expose the underlying representations.
