# JNumberTools vs PySAT

## 1. Where JNumberTools has an advantage

| Capability | JNumberTools | PySAT | Advantage |
|---|---|---|---|
| Work with non-Boolean dimensions | Native support for arbitrary lists of values | Primarily Boolean variables; other domains require encoding | **JNumberTools** |
| Express conditional product rules | Direct API: `.when().is().then().is()` | Requires conversion to Boolean clauses or another encoding | **JNumberTools** |
| Represent constrained Cartesian products | Native concept | Not its primary abstraction | **JNumberTools** |
| Exact count of valid combinations | Native `count()` | Possible through exhaustive model enumeration, but no equivalent native exact-count operation in the basic solver API | **JNumberTools** |
| Retrieve an assignment at a specified rank | Native `byRanks()` | No equivalent direct rank-based model retrieval API | **JNumberTools** |
| Retrieve selected ranks without enumerating all preceding solutions | Direct rank lookup | Generally requires enumeration or an additional counting/selection strategy | **JNumberTools** |
| Generate valid combinations as tuples | Native product stream | Produces Boolean models that must be decoded into application values | **JNumberTools** |
| Work with multiple values per dimension | Directly supported | Requires Boolean encoding, potentially with additional constraints | **JNumberTools** |
| Navigate a large solution space by rank | Designed for this use case | Not a built-in SAT solver operation | **JNumberTools** |

PySAT is primarily designed as a SAT-solving interface. It provides solving, model extraction, assumptions, model enumeration and unsatisfiable-core extraction.

## 2. Where PySAT is better

| Capability | JNumberTools | PySAT | Advantage |
|---|---|---|---|
| General SAT solving | Not designed as a general SAT solver | Integrates several established SAT solvers | **PySAT** |
| Arbitrary CNF formulas | Current conditional API naturally represents binary clauses / implications | Supports general CNF, including clauses with more than two literals | **PySAT** |
| SAT-specific optimizations | Uses its own constraint/product algorithms | Benefits from mature solver techniques such as conflict learning and preprocessing | **PySAT** |
| Unsatisfiable core extraction | Not a native feature of the current API | Supported | **PySAT** |
| Incremental solving with assumptions | Not an equivalent SAT-oracle interface | Supported | **PySAT** |
| Proof logging | Not provided by the current API | DRUP proof logging supported by applicable solvers | **PySAT** |
| Cardinality constraints | Can model some constraints through conditional rules | Dedicated cardinality encodings and supported native constraints | **PySAT** |
| Pseudo-Boolean constraints | Not a dedicated capability | Dedicated encodings available | **PySAT** |
| MaxSAT and MUS/MCS workflows | Not its purpose | Has dedicated supporting modules and algorithms | **PySAT** |

PySAT integrates established solver implementations such as CaDiCaL, Glucose and MiniSat, and provides interfaces for cardinality and pseudo-Boolean encodings.

## 3. The most important difference: rank-based access

This is where JNumberTools has a particularly interesting capability.

Imagine a constrained product with 100 Boolean dimensions and 33,554,432 valid assignments.

Suppose you want:

- The total number of valid assignments.
- The assignment at rank 16,777,216.
- The assignment at rank 100.
- The assignments at ranks 10, 1000 and 1,000,000.

With JNumberTools, these are natural operations:

```java
BigInteger count = builder.count();

var selected = builder.byRanks(List.of(
        BigInteger.TEN,
        BigInteger.valueOf(1_000),
        BigInteger.valueOf(1_000_000)
));
```

With PySAT, model enumeration is available, but it enumerates models and blocks previously found models. There is no equivalent native rank-indexed retrieval operation.

This is a meaningful difference in capability, not simply a programming-language performance difference.

## 4. Overall assessment

### Where JNumberTools should position itself

| Area | Preferred tool | Reason |
|---|---|---|
| Constrained product generation | **JNumberTools** | Native constrained Cartesian product abstraction |
| Exact counting and rank access | **JNumberTools** | Integrated counting and rank-based retrieval |
| General SAT solving | **PySAT** | Established SAT solvers and mature solving algorithms |
| Advanced SAT workflows | **PySAT** | Assumptions, unsatisfiable cores, proof logging and related capabilities |
| Non-Boolean constrained domains | **JNumberTools** | Direct support for arbitrary values in product dimensions |

PySAT can be extended with model counters, and exact counting can also be implemented through enumeration. Therefore, it would be inaccurate to say that PySAT cannot count models.

The distinction is that JNumberTools integrates exact counting and rank-based access into its constrained-product abstraction.

### Recommendation

Don't position JNumberTools as an alternative SAT solver. Position it as a library for:

**Constrained combinatorial generation, counting and rank-based access.**

That is a much clearer identity. The 100-switch experiment demonstrates that this abstraction can handle non-trivial Boolean constraint spaces without enumerating the entire solution space.

This is already a valuable capability, and arguably a more interesting differentiator than trying to compete with dedicated SAT solvers.