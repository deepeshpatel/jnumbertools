![JNumberTools](resources/JNumberTools_1280x640.png)

# JNumberTools
**JNumberTools** is an open-source Java library that tames massive combinatorial problems in big data pipelines, delivering scalable permutations, combinations, Cartesian products, and rankings. Integrated with Apache Spark, Flink, and Hadoop for batch and stream processing, it outperforms libraries like Apache Commons and Guava with constrained generation and lexicographical sampling. From synthetic data generation to cybersecurity, bioinformatics, and software testing, JNumberTools empowers data engineers, developers, and researchers to solve real-world challenges efficiently.

**Key Features:**

1. Versatile applications in machine learning, cryptography, testing, and optimization, with production-ready performance.
2. Stream-based APIs for constrained permutations, combinations, and Cartesian products, optimized for distributed pipelines.
3. Lexicographical sampling and ranking for efficient exploration of large combinatorial spaces.
4. Seamless integration with Spark, Flink, and Hadoop for big data workflows.

## Latest Version
The latest release of the library is [v3.0.0](https://github.com/deepeshpatel/jnumbertools/releases/tag/v3.0.0).
It is available through The Maven Central Repository [here](https://central.sonatype.com/search?q=jnumbertools&smo=true).
Add the following section to your `pom.xml` file.

```xml
<dependency>
    <groupId>io.github.deepeshpatel</groupId>
    <artifactId>jnumbertools</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Currently Available Algorithms**

1. [Permutations: 23 different types of permutations](docs/permutations/README.md)
2. [Combinations: 15 different types of combinations](docs/combinations/README.md)
3. [Set/subset generations: 7 different types available](docs/sets/sets.md)
4. [Cartesian Product: 4 different types of products](docs/products/README.md)
5. [Ranking of permutations & combinations: 7 different types of rankings](docs/ranking/README.md)
6. [Number system algorithms: 3 different types for combinatorics](docs/numbersystem/README.md)
