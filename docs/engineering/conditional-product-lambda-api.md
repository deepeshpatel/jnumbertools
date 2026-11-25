# Lambda conditional-product API

- `withLambda(...)` registers dimensions in tuple order.
- The unscoped `.when(predicate)` overloads remain strongly typed for the complete tuple: one dimension uses `Predicate<T>`; two use `BiPredicate<T,U>`; three through ten use `Predicate3` through `Predicate10`; more than ten use `Predicate<Object[]>`.
- Use `.when(dim1, dim2, ...).matches(predicate)` when a rule depends on only a subset of the registered dimensions. The scope handles are the exact list instances supplied to `withLambda(...)` or `.and(...)`.
- Scoped predicates receive only the selected values, in the order in which the dimensions are listed. Two through ten selected dimensions have typed predicate interfaces; any scope arity can use the `Object[]` form.
- A scoped rule is compiled over the Cartesian product of its selected dimensions only. Unrelated dimensions do not increase its compilation probe count.
- The one-million-probe limit applies per rule to the product of its selected antecedent dimensions and selected target dimension. A scope containing two million-element dimensions still exceeds the limit.
- Each completed rule is combined with other rules using AND. Use Java's `&&`, `||`, and `!` inside a predicate for arbitrary Boolean expressions.
- Predicates are evaluated during rule compilation, not during later counting or generation. They must be deterministic and side-effect-free.
