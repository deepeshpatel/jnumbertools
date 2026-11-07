---
name: jnumbertools-engineering
description: Project-specific guidance for AI assistants reviewing, modifying, testing, or documenting JNumbertools. Use whenever working in this repository.
---

# JNumbertools Engineering Skill

## 1. Source of truth

- `ValidationRules.md` is the authoritative specification for expected behaviour and validation.
- Read `ValidationRules.md` before reviewing or changing validation, builders, generators, or related tests.
- The **rules section** defines the behavioural contract. Do not reinterpret, weaken, strengthen, or silently amend those rules while implementing or reviewing code.
- The implementation-status section may become outdated. Treat it as a report that must be checked against the current source and tests, not as the specification.
- If the rules and implementation appear inconsistent, report the discrepancy. Do not resolve it by silently changing the rules.

## 2. Rules are language- and exception-type agnostic

- Validation rules describe intended behaviour, not a mandatory Java exception class, unless a rule explicitly names one.
- If a rule says that invalid input must cause an exception, both `IllegalArgumentException` and `NullPointerException` can satisfy that rule unless the rule specifies the exact exception type.
- Do not report an implementation as incorrect merely because it throws a different exception subtype from the one an AI reviewer would personally prefer.
- Tests should normally assert that an exception is thrown, rather than over-specifying its concrete type, unless the specification explicitly requires that type or the distinction is part of the public API contract.
- Do not change exception types just for stylistic consistency when the documented behavioural requirement is already satisfied.

## 3. Public usage is through builders

- Assume supported user-facing usage goes through the corresponding public builder API.
- Review validation and behaviour through the public builder entry point and the objects it returns.
- Do not require generator classes, concrete implementation classes, or implementation constructors to repeat validation that the builder already guarantees.
- A missing check inside a generator or implementation class is not, by itself, a defect if callers cannot reach an invalid state through the supported builder API.
- When investigating a suspected defect, write a reproducer that starts with the relevant public builder and follows the normal public usage path.
- Do not use direct construction of internal or implementation classes as evidence of a public API defect unless those constructors are explicitly part of the supported public API.

### Tests also go through builders

- Tests, including those in classes named after a generator (for example `*ByRanksTest`, `*GeneratorTest`), must obtain the object under test through the corresponding public builder entry point (for example `JNumberTools.combinations()`, `permutations()`, `subsets()`, `product()`). Do not construct generators or other internal implementation classes directly in tests.
- Exceptions: classes that are themselves documented public API (builders and their constructors, `Calculator`, `RankOf`/`UnrankOf`, and the number-system classes such as `Combinadic`, `Factoradic`, `Permutadic`, `Derangadic`, `Involutadic`), and data structures that are tested on their own (for example `FreqVector`, `FenwickTree`, `BinomialCoefficientMap`).
- Do not repeat in generator-level tests what is already verified at builder level. Checks that every builder shares (`lexOrder`, `lexOrderMth`, `byRanks`, `choice`, `sample` parameter rules, `isEmpty`, immutability, laziness) belong in the builder test and are run through `BuilderTestHelper`. Generator-level tests should cover only what the builder tests do not: produced output and order, rank values that are validated lazily (negative or out-of-bound ranks), element-level checks that can only happen when elements are consumed, and algorithm-specific behaviour.
- Before adding a rule or validation test, check `BuilderTestHelper` and the family's `*BuilderTest` for an existing equivalent.
- This section is a temporary convention. Once the library is split into modules with internal classes hidden from callers, visibility will enforce it and this section can be dropped.

## 4. Internal implementation classes

- Classes intended for internal use (for example, `EveryMthIterable`) do not need to independently enforce every public validation rule or user-facing contract.
- Internal classes may rely on invariants established by their callers/builders.
- Do not recommend adding defensive checks to internal classes merely to make them independently robust if all supported builder-based usage is correct and the extra checks provide no meaningful user-facing benefit.
- Verify the actual visibility, documentation, and usage before classifying a class as internal. Do not assume a class is internal solely because its name or package looks implementation-oriented.
- If an internal class is public for technical or package-structure reasons, judge it in the context of the documented supported API, not visibility alone.

## 5. Fail-fast validation and where checks belong

- Validate inputs as early as practical, preferably in the builder method that receives them. The goal is fail-fast behaviour: reject invalid configuration before constructing or iterating a generator.
- Put checks in the builder when the builder has enough information to validate the condition cheaply and reliably.
- Do not move checks into a generator merely because that is where an exception is easy to throw.
- Some validation cannot reasonably be completed in the builder. In those cases, validate at the earliest layer that has the necessary information.
- Example: a builder accepting a custom `Stream` or `List` can and should check immediately whether the stream/list reference itself is `null` or whether a list is empty, where the rules require those checks.
- However, traversing an arbitrary caller-supplied stream/list in the builder solely to search every element for `null` may be impractical, destructive, expensive, or incompatible with lazy/one-shot sources. Element-level validation may therefore belong in the generator at the point where elements are consumed.
- Preserve such generator-level checks and their tests when they are the appropriate earliest practical validation point.
- Do not apply a blanket rule that every check must live in the builder. Decide based on available information, cost, laziness, repeatability, and whether validation would consume or alter caller-owned input.

## 6. Tests are primary evidence

- Before reporting a suspected bug, inspect the relevant existing tests and understand what they establish.
- The user may state that the complete test suite passes. Treat that as important evidence; do not casually dismiss it or assume tests are inadequate.
- Do not claim that a test fails, that the suite passes, or that behaviour was reproduced unless you actually ran it and observed the result.
- Prefer a focused regression test that uses the public builder API and demonstrates the alleged failure.
- Check whether an equivalent test already exists before proposing a new one.
- If a proposed test fails to compile, uses the wrong builder method, or bypasses the supported API, correct the test before drawing conclusions.
- If a test passes, withdraw the alleged defect unless there is a clearly different, independently reproducible scenario.
- Distinguish among:
  - **Confirmed defect:** a public, supported usage path violates `ValidationRules.md` or another documented public contract, demonstrated by a reproducible test or equivalent concrete evidence.
  - **Potential concern:** a plausible risk not yet demonstrated.
  - **Design preference:** an optional improvement that is not a correctness defect.
  - **Documentation discrepancy:** documentation does not describe the current implementation accurately.
- Do not present potential concerns or design preferences as confirmed bugs.

## 7. How to investigate and propose changes

For each suspected issue:

1. Identify the relevant rule or documented public contract.
2. Locate the public builder entry point.
3. Trace the normal call path from builder to generator/implementation.
4. Inspect the relevant existing tests before drawing a conclusion.
5. Create or propose the smallest focused test using the builder API.
6. Explain the expected result from the rule and the observed result from the implementation.
7. Only then recommend a code change, if one is necessary.

Additional constraints:

- Do not change production code merely to satisfy a test that contradicts `ValidationRules.md`.
- Do not add redundant validation at multiple layers without a demonstrated reason.
- Preserve established API behaviour and mathematical conventions unless a rule or explicit user request requires a change.
- Avoid speculative refactoring, unnecessary abstraction, and changes unrelated to the requested task.
- When asked to modify a document, preserve sections the user explicitly says not to change.
- When asked to update implementation status, inspect current code and tests; do not rely on stale status text.

## 8. Reporting format

When reporting findings, include:

- **Classification:** confirmed defect, potential concern, design preference, or documentation discrepancy.
- **Relevant rule/contract:** identify the applicable part of `ValidationRules.md` or public documentation.
- **Public API path:** show the builder method and normal usage route.
- **Evidence:** cite the relevant implementation and existing/new test locations.
- **Reproduction:** provide a minimal builder-based test where practical.
- **Impact:** explain what a library user would observe.
- **Recommendation:** propose the smallest justified change, or explicitly say that no code change is needed.

Be precise about certainty. If the evidence does not establish a defect, say so.

## 9. Scope and collaboration

- Follow the user's requested scope. Do not turn a narrow documentation or test request into an unsolicited redesign.
- Ask before changing public API contracts, mathematical definitions, or the rules section of `ValidationRules.md`.
- Treat `ValidationRules.md` rules as general behavioural requirements, not Java-specific implementation instructions.
- Prefer verifying behaviour with the user over insisting on speculative findings.
- When the user reports that a proposed test passes, acknowledge the result and update or withdraw the corresponding finding.
