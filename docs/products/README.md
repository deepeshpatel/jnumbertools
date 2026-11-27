[Home](../../README.md)

# Cartesian Product Generators

Use the static `Product` entry point in `io.github.deepeshpatel.jnumbertools.entrypoint`. It exposes simple, constrained and pairwise-conditional products. Lambda predicates are available from the conditional builder.

## 1. Simple Cartesian product

Pass each dimension as a `List`. The lists are supplied in tuple order.

```java
var sizes = List.of("Small", "Medium", "Large");
var crusts = List.of("Flatbread", "Neapolitan");
var cheeses = List.of("Ricotta", "Mozzarella");

Product.simple(sizes, crusts, cheeses)
    .lexOrder()
    .forEach(System.out::println);
```

The simple-product builder also supports `lexOrderMth(m, start)`, `byRanks(...)`, `choice(...)`, `sample(...)`, `count()` and `isEmpty()`.

## 2. Constrained Cartesian product

Use `Product.constrained()` to start a constrained product. The first dimension is selected with one of these methods:

| Method | Meaning |
|---|---|
| `ofDistinct(quantity, elements)` | Fixed-size combinations without repetition |
| `ofMultiSelect(quantity, elements)` | Fixed-size combinations with repetition |
| `ofInRange(from, to, elements)` | Subsets whose sizes are in the inclusive range |

Add later dimensions with the corresponding `andDistinct`, `andMultiSelect` or `andInRange` builder methods.

```java
var colors = List.of("Red", "Green", "Blue");
var sizes = List.of("S", "M", "L", "XL");

Product.constrained()
    .ofDistinct(2, colors)
    .andMultiSelect(2, sizes)
    .lexOrder()
    .forEach(System.out::println);
```

A shared `Calculator` can be supplied when creating the constrained factory. This allows combinatorial caches to be reused across factories.

```java
Calculator calculator = new Calculator();
var constrained = Product.constrained(calculator)
    .ofDistinct(2, colors);
```

## 3. Compiled pairwise conditional product

Use `Product.conditional(dimensions...)` to create a product with pairwise rules. Each dimension is a list.

```java
var operatingSystems = List.of("Linux", "Windows", "macOS");
var browsers = List.of("Firefox", "Safari", "Chrome");
var networks = List.of("4G", "WiFi", "Ethernet");

Product.conditional(operatingSystems, browsers, networks)
    .when(operatingSystems).is("Linux")
    .then(browsers).isOneOf("Firefox", "Safari")
    .lexOrder()
    .forEach(System.out::println);
```

The compiled pairwise builder supports lexicographical traversal, every-mth traversal, rank-based access, counting and sampling. Its rules are compiled into constraints rather than filtering completed tuples.

## 4. Lambda predicates

The conditional builder also exposes `withLambda(...)` for joint predicates. Typed overloads are available for one through five dimensions; the no-argument form provides an array-based predicate for arbitrary dimension counts.

```java
Product.conditional(operatingSystems, browsers, networks)
    .withLambda(operatingSystems, browsers, networks)
    .when((os, browser, network) ->
        !os.equals("Linux") || browser.equals("Firefox") || browser.equals("Safari"))
    .lexOrder()
    .forEach(System.out::println);
```

Use scoped predicates with `.when(dimensions...).matches(predicate)` when the rule only depends on a subset of dimensions. Lambda predicates are compiled into constraints, subject to the documented probe limit; they are not intended as an unbounded general-purpose constraint solver.

## 5. Choosing a product style

- Use **simple** when every tuple is valid.
- Use **constrained** when dimensions are combinations, multi-selects or subset ranges.
- Use **conditional** for pairwise relationships between dimensions.
- Use **lambda predicates** for joint rules involving multiple dimensions.

The available terminal operations depend on the returned builder type. Consult the Javadoc for the exact builder contract rather than assuming all product styles expose identical methods.
