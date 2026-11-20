# Engineering Notes — JNumberTools 4.1.1

## Purpose and scope

PR #7 is the working PR for the **4.1.1 API enhancement and bug-fixing release**. This file tracks implementation decisions, completed changes, outstanding work, and commit history.

- Pull request: https://github.com/deepeshpatel/jnumbertools/pull/7
- Branch: `feature/conditional-product-multi-list-4.1.1`
- Base branch: `main`
- Target project version: `4.1.1`
- PR status: open; not merged.

The PR is intended for API enhancements and relevant bug fixes. The commits below establish the current API-enhancement work. No separate bug-fix commit is identified in the current PR history yet; add each verified bug fix here with its test and commit.

## Completed work

### Multi-list `conditionalProductOf` overload

Added an overload accepting the first two dimensions and a varargs tail of additional dimensions:

```java
conditionalProductOf(List<?> first, List<?> second, List<?>... additional)
```

The existing single-list entry point and the `.and(...)` chaining API remain available. A test compares the overload's generated structure with the equivalent builder assembled through `.and(...)`.

### Experimental whole-tuple `.filter(...)` API removed

Removed the public six-argument `.filter(...)` method, its `Predicate6` interface, the post-generation filtering wrappers, and the filter-specific test. The builder now consistently uses the existing constraint plan for counting, emptiness, rank selection, and traversal. This restores the original optimized path and removes the post-generation scan fallback.

The removal commit is listed below. The next implementation step is a dependency-aware multi-trigger rule expression using `.and()` and `.or()`. It must preserve exact semantics across traversal, counting, ranking, and sampling. Existing pairwise-only rules must retain their current execution path without multi-trigger evaluation overhead.

## Commit history on the PR branch

The following commits are present on the PR branch, in chronological order:

| Commit | Change | Status / notes |
|---|---|---|
| [`5913345`](https://github.com/deepeshpatel/jnumbertools/commit/591334529331e138f939da1292bef0a7a27d6656) | Add multi-dimension `conditionalProductOf` overload | Completed |
| [`545499f`](https://github.com/deepeshpatel/jnumbertools/commit/545499fb077bb6bb4dc86b64c27bff709f687eb4) | Test multi-dimension overload against chaining | Completed |
| [`47f8576`](https://github.com/deepeshpatel/jnumbertools/commit/47f8576a002821f33e484be6d08d3466dbfd230d) | Set project version to 4.1.1 | Completed |
| [`9fa0784`](https://github.com/deepeshpatel/jnumbertools/commit/9fa0784805a10334f9bd8714769d7f808b54c834) | Add whole-tuple filter support | Experimental; post-generation filtering is not acceptable as the final implementation |
| [`a128ca1`](https://github.com/deepeshpatel/jnumbertools/commit/a128ca1385dd899555d97b5a9e8406a91b2476d5) | Apply tuple filters to adaptive lexicographic traversal | Experimental; part of the current filter implementation |
| [`e28ec73`](https://github.com/deepeshpatel/jnumbertools/commit/e28ec73eb52a2e8f251ac3754d4c7b9ba5fcb472) | Test whole-tuple filters against equivalent conditional rules | Test coverage for the experimental implementation; does not establish pruning |
| [`8ed8d1e`](https://github.com/deepeshpatel/jnumbertools/commit/8ed8d1ea70442e04ae53cf2cad94decd553916c3) | Clarify filter evaluation and dimension ordering | Documentation for the experimental implementation |
| [`f028358`](https://github.com/deepeshpatel/jnumbertools/commit/f028358b94aca9edca1904ba6fe8dcba305380a6) | Document dimension ordering for tuple filters | Documentation for the experimental implementation |
| [`8a28574`](https://github.com/deepeshpatel/jnumbertools/commit/8a285746949640defa5f352f3a66dae81066ca17) | Preserve traversal characteristics when applying tuple filters | Does not resolve the lack of partial-assignment pruning |

This list describes the implementation commits preceding this engineering-notes commit. It does not imply that every change is approved for release.

## Remaining work and acceptance criteria

- [ ] Design and implement arbitrary-length trigger expressions: `.and()` adds a condition to the current conjunction; `.or()` starts another conjunction alternative.
- [ ] Compile multi-trigger rules into a sound constraint representation; do not approximate a multi-dimensional antecedent with independent pairwise rules.
- [ ] Preserve exact semantics for lexicographic and any-order traversal, `count()`, `isEmpty()`, `byRanks()`, `lexOrderMth()`, `choice()`, and `sample()`.
- [ ] Keep pairwise-only workloads on the existing optimized execution path and benchmark for regressions.
- [ ] Add tests for AND/OR grouping, arbitrary chain lengths, validation, and differential agreement with brute force.
- [ ] Correct and verify public Javadocs and usage examples.
- [ ] Record each verified bug fix here with the failing test, fix summary, and commit SHA.
- [ ] Run the relevant Maven tests and record the actual result before merge.

## Validation status

The filter-removal commit was created remotely. Maven tests were **not executed** in this environment; the user will run the tests locally after the commits are pushed. Record the local test command and actual result here when available.
