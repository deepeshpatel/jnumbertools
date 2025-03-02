[Home](../../README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Permutation Generators](../permutations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Combination Generators](../combinations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Set/subset Generators](../sets/sets.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Cartesian Product Generators](../products/README.md)



# Cartesian Product Generators

JNumberTools provides following 4 generators for cartesian product

1. Simple Cartesian Product
   1. Simple Cartesian product in lex order
   2. Every mth cartesian product in lex order


2. Constrained cartesian product
   1. Constrained cartesian product in lex order
   2. Every mth constrained cartesian product in lex order


### 1. Simple Cartesian Product

#### 1.1 Simple Cartesian product in lex order

A Cartesian Product of two sets A and B, denoted by A × B is the set of all
ordered pairs (a, b) where a ∈ A and b ∈ B.

This API supports the cartesian product of n different sets. That is for sets A, B, C, D...
you can find A × B × C × D X ... directly in single API call

```java
//every combination of 1 pizza-base, 1 pizza-crust and 1 cheese
var pizzaSize   =  List.of("Small", "Medium", "Large");
var pizzaCrust  =  List.of("Flatbread", "Neapolitan", "Thin Crust");
var cheese      =  List.of( "Ricotta ","Mozzarella","Cheddar");

JNumberTools.cartesianProduct()
   .simpleProductOf(pizzaSize)
   .and(pizzaCrust)
   .and(cheese)
   .lexOrder().stream().toList();
```

#### 1.2 m-th simple cartesian product
Generates every m<sup>th</sup> cartesian product starting from any starting index of choice.
This API does not search for the m<sup>th</sup> product in a sorted list but
directly generates the desired product and hence it is very efficient.

```java
//every 10th combination of 1 pizza-base, 1-pizza crust and 1 cheese
//starting form 5th index in lex order. That is 5th, 10th,15th and 20th
var pizzaSize   =  List.of("Small", "Medium", "Large");
var pizzaCrust  =  List.of("Flatbread", "Neapolitan", "Thin Crust");
var cheese      =  List.of( "Ricotta ","Mozzarella","Cheddar");

JNumberTools.cartesianProduct()
   .simpleProductOf(pizzaSize)
   .and(pizzaCrust)
   .and(cheese)
   .lexOrderMth(5,10).stream().toList();
```

### 2. Constrained Cartesian Product
Generates a cartesian product with custom constraints, allowing selection of multiple items, distinct or repeated elements, and subsets within a specified range from each input set, in lexicographical order.

#### 2.1 Constrained cartesian product in lex order
Same as simple cartesian product but instead of having only one item from each list
we can select multiple, select repetitive elements and select in a given range.
```java
//Print all combination of -
// any 1 pizza base and
// any 2 distinct cheese
// and any 2 sauce (repeated allowed)
// and any toppings in range 1 to 5";

var pizzaBase = List.of("Small ","Medium", "Large");
var cheese = List.of( "Ricotta ","Mozzarella","Cheddar");
var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
var toppings = List.of("Tomato","Capsicum", "Onion", "Corn", "Mushroom");

JNumberTools
   .cartesianProduct().constrainedProductOf(1, pizzaBase)
   .andDistinct(2, cheese)
   .andMultiSelect(2, sauce)
   .andInRange(1,5,toppings)
   .lexOrder().stream().toList();
```

#### 2.2 m-th constrained cartesian product
Generates m<sup>th</sup> constrained cartesian product directly without calculating the values
preceding it.
For example below code prints every 10<sup>18</sup> th lexicographical combination
which is not feasible to calculate via one by one iteration. API supports BigInteger,
so it is even possible to execute it for very large value of m.
Say every 10<sup>100</sup> th combination.
```java
//Every 10^18 th lexicographical combinations of -
// any 10 distinct small alphabets and
// any 12 distinct capital alphabets and
// any 3 symbols with repetition allowed and 
// all subsets in size range [10,20] of a set of numbers in [0,20)

var smallAlphabets = IntStream.rangeClosed('a', 'z').mapToObj(c -> (char) c).toList();
var capitalAlphabets = IntStream.rangeClosed('A', 'Z').mapToObj(c -> (char) c).toList();
var symbols = List.of('~','!','@','#','$','%','^','&','*','(',')');
var numbers = IntStream.rangeClosed(0, 20).boxed().toList();

JNumberTools.cartesianProduct()
   .constrainedProductOf(10, smallAlphabets) //distinct
   .andDistinct(12, capitalAlphabets) //distinct
   .andMultiSelect(3, symbols) // repetition allowed
   .andInRange(10, 20, numbers) // all subsets in size range
   .lexOrderMth(1000_000_000_000_000_000L, 0).stream().toList();
```

[Home](../../README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Permutation Generators](../permutations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Combination Generators](../combinations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Set/subset Generators](../sets/sets.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Cartesian Product Generators](../products/README.md)


