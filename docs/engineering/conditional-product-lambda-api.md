# Lambda conditional-product API

- `withLambda(...)` registers dimensions in tuple order.
- One dimension uses `Predicate<T>`; two use `BiPredicate<T,U>`; three through ten use nested `Predicate3` through `Predicate10` functional interfaces.
- More than ten dimensions use the `Object[]` predicate form.
- Each `.when(predicate)` compiles a condition over all registered dimensions. Repeated calls are combined with AND.
- Use Java's `&&`, `||`, and `!` inside a predicate for arbitrary Boolean expressions.
- Predicates are evaluated during rule compilation, not during later counting or generation. The existing one-million-probe compilation limit applies to each predicate.
