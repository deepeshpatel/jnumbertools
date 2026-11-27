![JNumberTools](resources/JNumberTools_logo_banner_transparent.png)

# JNumberTools
**JNumberTools** is an open-source Java library for combinatorics and number theory. It provides permutations, combinations, Cartesian products, derangements, and ranking/unranking algorithms, including constrained generation and lexicographical sampling. Its iterator- and stream-based APIs can be consumed by Java applications and adapted for use in data-processing workflows, including applications built with Apache Spark, Flink, and Hadoop. From synthetic data generation to cybersecurity, bioinformatics, and software testing, JNumberTools helps developers and researchers work with large combinatorial spaces.

**Key Features:**

1. Applications in machine learning, cryptography, testing, and optimization.
2. Iterator- and stream-based APIs for permutations, combinations, and four Cartesian product styles: simple, constrained, compiled pairwise-rule, and lambda-predicate products.
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
    <version>4.2.0</version>
</dependency>
```

**Gradle**
```gradle
implementation 'io.github.deepeshpatel:jnumbertools:4.2.0'
```
**SBT**
```sbt
libraryDependencies += "io.github.deepeshpatel" % "jnumbertools" % "4.2.0"
```

## Performance

- **BigInteger Support**: Handle indices up to 10^100 and beyond
- **Memory Efficient**: Stream-based generation for massive datasets
- **Data-processing workflows**: Java iterators and streams can be adapted for use in applications built with Apache Spark, Flink, and Hadoop; framework-specific integration is handled by the application.
- **Optimized Algorithms**: Efficient lexicographical generation and ranking

## Public API entry points

The library uses dedicated static entry points for generator creation:

| Entry point | Purpose |
|---|---|
| `Permutation` | Unique, k-, repetitive and multiset permutations |
| `Combination` | Unique, repetitive and multiset combinations |
| `Subset` | All subsets or subsets within a size range |
| `Derangement` | Fixed-point-free permutations |
| `Product` | Simple, constrained and conditional Cartesian products |
| `Rank` / `Unrank` | Ranking and unranking permutations, derangements and combinations |

The public entry points are in `io.github.deepeshpatel.jnumbertools.entrypoint`. Builders are in `io.github.deepeshpatel.jnumbertools.builders`, and the reusable mathematical `Calculator` is in `io.github.deepeshpatel.jnumbertools.datastructure`.

```java
import io.github.deepeshpatel.jnumbertools.entrypoint.*;
import java.util.List;
import java.math.BigInteger;

var permutations = Permutation.unique("A", "B", "C").lexOrder();
var combinations = Combination.unique(3, "A", "B", "C", "D").lexOrder();
var subsets = Subset.of("A", "B", "C").inRange(1, 2).lexOrder();
var products = Product.simple(List.of("Linux", "Windows"), List.of("Firefox", "Chrome"));

BigInteger rank = Rank.permutation().unique(2, 0, 1);
int[] permutation = Unrank.permutation().unique(rank, 3);
```

The four Cartesian product styles are simple, constrained, compiled pairwise-conditional, and lambda-predicate products. The available operations depend on the builder type; consult the [Products guide](docs/products/README.md).

The argument checks and exceptions for public operations are specified in [ValidationRules.md](ValidationRules.md).

![License](https://img.shields.io/github/license/deepeshpatel/jnumbertools)
![Stars](https://img.shields.io/github/stars/deepeshpatel/jnumbertools?style=social)

**Currently Available Algorithms**

1. [Permutations: 23 different types of permutations](docs/permutations/README.md)
2. [Derangements: fixed-point-free permutations](docs/derangements/README.md)
3. [Combinations: 15 different types of combinations](docs/combinations/README.md)
4. [Set/subset generations: 7 different types available](docs/sets/README.md)
5. [Cartesian Products: simple, constrained, compiled and lambda-engine products](docs/products/README.md)
6. [Ranking of permutations & combinations: 8 different types of rankings](docs/ranking/README.md)
7. [Number system algorithms: 4 different types for combinatorics](docs/numbersystem/README.md)
8. [Math Functions](docs/calculator/README.md)