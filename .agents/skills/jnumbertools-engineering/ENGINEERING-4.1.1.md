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

The removal commit is listed below. The compound `.and()` / `.or()` expression is now implemented in the same PR branch. Existing pairwise-only rules remain on the original relation path; performance still needs confirmation with JMH.

### Compound trigger expressions with AND / OR

Added arbitrary-length trigger expressions:

- `.and(dimension)` adds a condition to the current conjunction.
- `.or(dimension)` starts a new alternative conjunction.
- The trigger is interpreted as an OR of AND-groups; AND binds within each group.
- The consequence applies if any group matches.

Example:

```java
.when(browser).is("Safari").and(os).is("Windows")
.or(browser).is("Chrome").and(os).is("Linux")
.then(database).isNot("PostgreSQL");
```

The engine stores this as a non-binary constraint. Every dimension in its trigger scope and target is joined into a bounded joint-assignment block, and the complete implication is checked against those assignments. The block is subject to the existing `MAX_LOOP_COMBINATIONS` limit; pair links introduced to form the block are subject to `MAX_PAIR_CELLS`. Expressions whose alternatives all refer to one trigger dimension are collapsed back into a pairwise rule, preserving the existing optimized relation path. The plan builder also bypasses compound-rule block filtering entirely when the rule list is empty. Prefix-pruned traversal checks compound rules only when their complete scope has been assigned. The DP lexicographic path also performs a final feasibility check on the last dimension when compound rules exist; without it, the normal pairwise fast path would have skipped that final check.

Tests added cover AND/OR grouping, arbitrary-length conjunctions, and the builder's traversal, counting, ranking and sampling operations. The Maven suite has not been run remotely; the user will run it locally.

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
| [`8a28574`](https://github.com/deepeshpatel/jnumbertools/commit/8a285746949640defa5f352f3a66dae81066ca17) | Preserve traversal characteristics when applying tuple filters | Experimental implementation, subsequently removed |
| [`1a40b1a`](https://github.com/deepeshpatel/jnumbertools/commit/1a40b1ae9d208771509239749917a1e663300e5f) | Remove experimental whole-tuple filter API | Completed |
| [`ab08e86`](https://github.com/deepeshpatel/jnumbertools/commit/ab08e86c860784aeccd471341ec2c7d2676d8280) | Update engineering notes after filter removal | Completed |
| [`ac92786`](https://github.com/deepeshpatel/jnumbertools/commit/ac92786900a73fb9eba664c9475e3265111d11a1) | Add compound AND/OR conditional trigger rules | Implemented; local validation pending |
| [`38d7266`](https://github.com/deepeshpatel/jnumbertools/commit/38d72665c960751be82a8a43555590215ecb3e0e) | Update engineering notes for compound trigger rules | Completed |
| [`cc384fa`](https://github.com/deepeshpatel/jnumbertools/commit/cc384fa1983b09b236cf74fbea1f0ffd107cccd6) | Preserve the pairwise fast path for compound rule planning | Added empty-rule fast path and single-dimension OR test |
| [`50d08f8`](https://github.com/deepeshpatel/jnumbertools/commit/50d08f84ea5969be607efbf4894a8e93f858714f) | Record pairwise fast-path follow-up in engineering notes | Completed |
| [`c134885`](https://github.com/deepeshpatel/jnumbertools/commit/c1348857122c55fd414c8245661ba94468ab6943) | Apply compound-rule feasibility checks at final DP dimension | Correctness fix; local validation pending |

This list records the PR branch's implementation and engineering-note commits. It does not imply that local validation or performance acceptance is complete.

## Remaining work and acceptance criteria

- [x] Design and implement arbitrary-length trigger expressions: `.and()` adds a condition to the current conjunction; `.or()` starts another conjunction alternative.
- [x] Compile multi-trigger rules as bounded joint-scope constraints, not as independent pairwise approximations.
- [x] Integrate multi-trigger semantics with lexicographic traversal, any-order traversal, `count()`, `isEmpty()`, `byRanks()`, `lexOrderMth()`, `choice()`, and `sample()`.
- [ ] Run differential/property tests and the Maven suite locally; fix any failures.
- [ ] Benchmark pairwise-only workloads against the pre-change baseline to verify no measurable regression; source-level fast-path preservation is not a substitute for measurement.
- [x] Add tests for AND/OR grouping and arbitrary chain lengths.
- [ ] Review Javadocs after local compilation and Javadoc generation.
- [ ] Record each verified bug fix here with the failing test, fix summary, and commit SHA.
- [ ] Run the relevant Maven tests and record the actual result before merge.

## Validation status

The filter-removal and compound-trigger implementation commits were created remotely. Maven tests and Javadoc generation were **not executed** in this environment; the user will run the tests locally after the commits are pushed. Record the exact commands and actual results here when available. No performance benchmark has yet been run for pairwise-only workloads.
