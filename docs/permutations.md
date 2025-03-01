
#### Example: `docs/permutations.md`
```markdown
# Permutations in JNumberTools

This section details the 10 types of permutation APIs available in JNumberTools, including unique, repetitive, k-permutations, and multiset permutations.

## 1.1 Unique Permutation
Generates all `n!` unique permutations of `n` elements in lexicographical order.

```java
// All permutations of integers in range [0,3)
JNumberTools.permutations()
    .unique(3)
    .lexOrder().stream().toList();

// All permutations of "Red", "Green", "Blue"
JNumberTools.permutations()
    .unique("Red", "Green", "Blue")
    .lexOrder().stream().toList();