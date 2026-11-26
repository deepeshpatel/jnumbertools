# Lambda conditional-product API

- `withLambda(...)` registers dimensions in tuple order.
- The unscoped `.when(predicate)` overloads remain strongly typed for the complete tuple: one dimension uses `Predicate<T>`; two use `BiPredicate<T,U>`; three through five use `Predicate3` through `Predicate5`; more than five use `Predicate<Object[]>`.
- Use `.when(dim1, dim2, ...).matches(predicate)` when a rule depends on only a subset of the registered dimensions. The scope handles are the exact list instances supplied to `withLambda(...)` or `.and(...)`.
- Scoped predicates receive only the selected values, in the order in which the dimensions are listed. Two through five selected dimensions have typed predicate interfaces; any scope arity can use the `Object[]` form by passing an explicit `List<?>[]` scope, e.g. `.when(new List<?>[]{a, b}).matches(values -> ...)`.
- A scoped rule is compiled over the Cartesian product of its selected dimensions only. Unrelated dimensions do not increase its compilation probe count.
- The one-million-probe limit applies per rule to the product of its selected antecedent dimensions and selected target dimension. A scope containing two million-element dimensions still exceeds the limit.
- Each completed rule is combined with other rules using AND. Use Java's `&&`, `||`, and `!` inside a predicate for arbitrary Boolean expressions.
- Predicates are evaluated during rule compilation, not during later counting or generation. They must be deterministic and side-effect-free.
- A scoped rule returns the builder type it was started from, so typed full-tuple rules, scoped rules and `and(list)` can be mixed in any order (`Typed3.when(a, b).matches(...)` is still a `Typed3`).
- A typed builder checks that it holds exactly as many dimensions as its arity; constructing one over a product with a different number of dimensions throws `IllegalArgumentException`.
- A full-tuple `.when(predicate)` covers every dimension registered at that point. On a product with many or large dimensions it can exceed the probe limit; the error reports the probe count and suggests the scoped form.
- Predicates that receive an `Object[]` must not keep a reference to it: the array is reused between calls.
