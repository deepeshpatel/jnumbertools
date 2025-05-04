![JNumberTools](resources/JNumberTools_1280x640.png)

# JNumberTools
**JNumberTools**  is an open-source Java library that tames massive combinatorial problems in big data pipelines, delivering scalable permutations, combinations, Cartesian products, and rankings. Integrated with Apache Spark, Flink, and Hadoop for batch and stream processing, it outperforms libraries like Apache Commons and Guava with constrained generation and lexicographical sampling. From synthetic data generation to cybersecurity, bioinformatics, and software testing, JNumberTools empowers data engineers, developers, and researchers to solve real-world challenges efficiently.

**Key Features:**

1. Versatile applications in machine learning, cryptography, testing, and optimization, with production-ready performance.
2. Stream-based APIs for constrained permutations, combinations, and Cartesian products, optimized for distributed pipelines.
3. Lexicographical sampling and ranking for efficient exploration of large combinatorial spaces.
4. Seamless integration with Spark, Flink, and Hadoop for big data workflows.

## latest version
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

3. [Set/subset generations: 4 different types available](docs/sets/sets.md)

4. [Cartesian Product: 4 different types of product](docs/products/README.md)

5. [Ranking of permutations & combinations](#5-ranking-of-permutations--combinations)
    1. [Ranking of unique permutation](#51-ranking-of-unique-permutation)
    2. [Ranking of k-permutation](#52-ranking-of-k-permutation)
    3. [Ranking of repetitive permutation](#53-ranking-of-repetitive-permutation)
    4. [Ranking of unique combination](#54-ranking-of-combination)
    5. Rank of repetitive combination : Coming soon
    6. Rank of multiset permutation : Coming soon
    7. Rank of multiset combination : Coming soon


6. Number system algorithms related to combinatorics
    1. Factorial Number System aka Factoradic
    2. Permutation Number System aka Permutadic
    3. Combinatorial Number System aka Combinadic



### 5. Ranking of permutations & combinations

#### 5.1 Ranking of unique permutation
Calculates the rank of a unique permutation starting from 0th
For example, there are total 4!=24 permutations of [0,1,2,3]
where the first permutation [0,1,2,3] has rank 0 and the last permutation
[3,2,1,0] has the rank of 23

```
BigInteger rank = JNumberTools.rankOf().uniquePermutation(3,2,1,0);
```

#### 5.2 Ranking of k-permutation
Calculates the rank of a k=permutation. For example if we
select 5 elements out of 8 [0,1,2,3,4,5,6,7] then there are total <sup>8</sup>C<sub>5</sub>
permutations out of which 1000<sup>th</sup> permutation is [8,4,6,2,0]
```java
//will return 1000
BigInteger rank = JNumberTools.rankOf().kPermutation(8,4,6,2,0);
```

#### 5.3 Ranking of repetitive permutation
Calculates the rank of repetitive permutation of

```java
//1,0,0,1 is the 9th repeated permutation of two digits 0 and 1  
int elementCount = 2;
BigInteger result = JNumberTools.rankOf()
    .repeatedPermutation(elementCount, new Integer[]{1,0,0,1});
```

#### 5.4 Ranking of combination
Calculates the rank of given combination. For example if we generate
combination of 4 elements out of 8  in lex order, then 35th combination
will be [1,2,3,4]. So given the input 8 and [1,2,3,4], the API returns 35,
the rank of combination

```java
BigInteger rank = JNumberTools.rankOf().uniqueCombination(8, 1,2,3,4);
```