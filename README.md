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

## Operations by product family

The available operations depend on the product builder. Do not assume that every product style exposes the same traversal or sampling methods.

| Product family | Entry point | Main operations |
|---|---|---|
| Simple Product | `simpleProductOf(...)` | `lexOrder()`, `lexOrderMth(m, start)`, rank-based access, choice and sample |
| Constrained Product | `constrainedProductOfDistinct(...)` and related builders | `lexOrder()`, `lexOrderMth(m, start)`, rank-based access, choice and sample |
| Compiled Product (pairwise rules) | `conditionalProductOf(...)` | `lexOrder()`, `lexOrderMth(m, start)`, `byRanks(...)`, `choice(...)`, `sample(...)`, `count()`, `isEmpty()` |
| Lambda Engine (joint predicates compiled to constraints) | `withLambda(...)` | `generate()`, `count()`, `isEmpty()` |

`generate()` on the Lambda Engine returns a lazy, re-iterable generator; it does not promise lexicographical encounter order or expose rank-based traversal and sampling.

\`\`\`java
import io.github.deepeshpatel.jnumbertools.base.Combination;

var combinations = Combination.unique(5, 3);
combinations.count();   // 10
combinations.isEmpty(); // false
Combination.unique(2, 3).isEmpty(); // true: cannot choose 3 of 2
```

The argument checks and the exceptions each operation raises are specified in [ValidationRules.md](ValidationRules.md).

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