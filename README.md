![JNumberTools](resources/JNumberTools_logo_banner_transparent.png)

# JNumberTools
**JNumberTools** is an open-source Java library for combinatorics and number theory. It provides permutations, combinations, Cartesian products, derangements, and ranking/unranking algorithms, including constrained generation and lexicographical sampling. Its iterator- and stream-based APIs can be consumed by Java applications and adapted for use in data-processing workflows, including applications built with Apache Spark, Flink, and Hadoop. From synthetic data generation to cybersecurity, bioinformatics, and software testing, JNumberTools helps developers and researchers work with large combinatorial spaces.

**Key Features:**

1. Applications in machine learning, cryptography, testing, and optimization.
2. Iterator- and stream-based APIs for permutations, combinations, and Cartesian products, including constrained generation.
3. Lexicographical sampling and ranking for efficient exploration of large combinatorial spaces.
4. Java APIs that can be adapted by application code for workflows using Spark, Flink, Hadoop, and other data-processing frameworks; JNumberTools does not provide framework-specific connectors.

## Latest Version
The latest release of the library is available on [GitHub](https://github.com/deepeshpatel/jnumbertools/releases).
It is available through The Maven Central Repository [here](https://mvnrepository.com/artifact/io.github.deepeshpatel/jnumbertools/versions).
Add the following section to your configuration(pom/gradle etc) file.

```xml
<dependency>
    <groupId>io.github.deepeshpatel</groupId>
    <artifactId>jnumbertools</artifactId>
    <version>4.0.1</version>
</dependency>
```

**Gradle**
```gradle
implementation 'io.github.deepeshpatel:jnumbertools:4.0.1'
```
**SBT**
```sbt
libraryDependencies += "io.github.deepeshpatel" % "jnumbertools" % "4.0.1"
```

## Performance

- **BigInteger Support**: Handle indices up to 10^100 and beyond
- **Memory Efficient**: Stream-based generation for massive datasets
- **Data-processing workflows**: Java iterators and streams can be adapted for use in applications built with Apache Spark, Flink, and Hadoop; framework-specific integration is handled by the application.
- **Optimized Algorithms**: Efficient lexicographical generation and ranking

## Operations available on every generator

All generators (permutations, derangements, combinations, subsets and Cartesian products) are obtained from a builder, and every builder offers the same operations:

| Operation | Result |
|---|---|
| `lexOrder()` | all results in lexicographical order |
| `lexOrderMth(m, start)` | every m<sup>th</sup> result from rank `start`, generated directly without the preceding ones |
| `byRanks(ranks)` | the results at the given ranks, consumed lazily |
| `choice(n, random)` | `n` random results, with replacement (duplicates possible) |
| `sample(n, random)` | `n` distinct random results, without replacement |
| `count()` | the number of results as a `BigInteger` (`0` is valid) |
| `isEmpty()` | `true` exactly when `count()` is `0`; a builder whose only result is the empty list is not empty |

```java
var combinations = JNumberTools.combinations().unique(5, 3);
combinations.count();   // 10
combinations.isEmpty(); // false
JNumberTools.combinations().unique(2, 3).isEmpty(); // true: cannot choose 3 of 2
```

The argument checks and the exceptions each operation raises are specified in [ValidationRules.md](ValidationRules.md).

![License](https://img.shields.io/github/license/deepeshpatel/jnumbertools)
![Stars](https://img.shields.io/github/stars/deepeshpatel/jnumbertools?style=social)

**Currently Available Algorithms**

1. [Permutations: 23 different types of permutations](docs/permutations/README.md)
2. [Derangements: fixed-point-free permutations](docs/derangements/README.md)
3. [Combinations: 15 different types of combinations](docs/combinations/README.md)
4. [Set/subset generations: 7 different types available](docs/sets/README.md)
5. [Cartesian Product: simple and constrained products](docs/products/README.md)
6. [Ranking of permutations & combinations: 8 different types of rankings](docs/ranking/README.md)
7. [Number system algorithms: 4 different types for combinatorics](docs/numbersystem/README.md)
8. [Math Functions](docs/calculator/README.md)