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

### Whole-tuple `.filter(...)`: experimental, not accepted

A six-argument whole-tuple filter API and related tests were added to the branch. The current implementation evaluates each predicate only after a complete tuple has been generated. It therefore does not prune partial assignments and may scan many tuples that will ultimately be rejected. Filtered counting and rank-based operations also scan filtered lexicographic results.

**This implementation is not good enough for the 4.1.1 API and must not be treated as release-ready. Do not push or merge a post-generation-only `.filter(...)` implementation as the final feature.** The current implementation has already been pushed to the PR branch in the commits listed below; this note records that state.

Before this feature can be accepted, redesign it so predicates can reject a partial assignment as soon as all dimensions on which they depend have been assigned. The design must preserve correct behaviour and ordering across the builder's supported operations, not just `lexOrder()`. In particular, assess `count()`, `isEmpty()`, `byRanks()`, `lexOrderMth()`, `choice()`, and `sample()`. The predicate API must make dependencies explicit or otherwise provide a sound partial-evaluation contract; do not attempt to infer argument dependencies from arbitrary Java lambda bytecode.

Until a pruning-capable design and corresponding tests are complete, the whole-tuple filter implementation remains **experimental and must be removed or replaced before PR #7 is considered complete**. Existing pairwise `.when(...).then(...)` rules remain the optimized pruning mechanism.

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

- [ ] Design a pruning-capable filter contract with explicit, sound predicate dependencies.
- [ ] Implement partial-assignment pruning; do not evaluate a predicate only after a complete tuple when its dependencies are already known.
- [ ] Verify consistent semantics for lexicographic and any-order traversal, counting, emptiness, rank selection, every-Mth traversal, choice, and sampling.
- [ ] Add tests proving early rejection/pruning as well as output equivalence with the corresponding explicit constraints.
- [ ] Benchmark the pruned implementation against equivalent pairwise rules and a stream-filter baseline. Report end-to-end costs, including builder construction and counting where applicable.
- [ ] Remove or replace the current post-generation-only filter implementation before treating the feature as complete.
- [ ] Record each verified bug fix here with the failing test, fix summary, and commit SHA.
- [ ] Run the relevant Maven tests and record the actual result before merge.

## Validation status

The PR description records that tests were added but were **not executed** as part of the remote changes. Do not describe the branch as fully tested until the relevant test suite has actually been run and its result recorded here.
