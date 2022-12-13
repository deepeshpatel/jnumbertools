# JNumberTools V1.0.0
JNumberTools is the open source java-library of combinatorics and number-theory 

**Currently Available Algorithms**

1. Permutations (10 different types of permutation ranking and un-ranking)
1. Combinations (4 different types of combination ranking and un-ranking)
1. Set/subset generations
1. Factoradic sequence generation.
1. Combinadic sequence generation.
1. Permutadic (A new concept for nth k-Permutation)

***
**Permutation Generation Examples:**

[permutation examples](md_files/permutation_table.md)

<iframe src="md_files/permutation.html" title="permutation examples">
</iframe>

**Combination Generation Examples:**

| #   | Type of Combination                   | Description                                                                                                                                                                                                                                                                                                                                                      | API                                                                                                                                       | Output                                           | Count                         |
|-----|---------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|-------------------------------|
| 1.  | Unique Combination of size r          | Selection of r distinct items out of n items<br> In mathematics this is known as n-Choose-r                                                                                                                                                                                                                                                                      | JNumberTools.combinationsOf("A","B","C")<br>.unique(2)<br>.forEach(System.out::println);                                                  | [A, B]<br>[A, C]<br>[B, C]                       | <sup>n</sup>C<sub>r</sub>     ||
| 2.  | Nth Unique Combination of size r      | Same as n-Choose-r but generating evey nth combination in lex order.<br>This is important because count of combinations can grow astronomically<br>large and to generate say, next 1 billionth combination of 34Choose17,<br>we do not like to wait for few 100 hours to generate billion combinations<br>sequentially and then selecting billionth combination. | JNumberTools.combinationsOf("A","B","C")<br>.uniqueNth(2,2)<br>.forEach(System.out::println);                                             | [A, B]<br>[B, C]                                 | <sup>n</sup>C<sub>r</sub> / m ||
| 3.  | Repetitive Combination of size r.     | Generates combinations of repeated values od size r.                                                                                                                                                                                                                                                                                                             | JNumberTools.combinationsOf("A","B")<br>.repetitive(3)<br>.forEach(System.out::println);                                                  | [A, A, A]<br>[A, A, B]<br>[A, B, B]<br>[B, B, B] | <sup>n+r-1</sup>C<sub>r</sub> ||
| 4.  | Repetitive Combination<br>of multiset | Also known as multiset-combination.<br>This is special case of repetitive combination where every item say,<br> a<sub>1</sub>,a<sub>2</sub>,a<sub>3</sub> ... has an associated number s<sub>1</sub>,s<sub>2</sub>,s<sub>3</sub>... which denotes how many<br>times an item can be repeated in a combination                                                     | JNumberTools.combinationsOf("A","B","C")<br>..repetitiveMultiset(2,2,1,1) // size 2 and frequency 2,1,1<br>.forEach(System.out::println); | [A, A]<br>[A, B]<br>[A, C]<br>[B, C]             ||
**Subset Generation Examples:**

| Type of set                       | API                                                                                    | Output                                                             | Count                                     |
|-----------------------------------|----------------------------------------------------------------------------------------|--------------------------------------------------------------------|-------------------------------------------|
| All subsets                       | JNumberTools.subsetsOf("A","B","C")<br>.all()<br>.forEach(System.out::println);        | []<br>[A]<br>[B]<br>[C]<br>[A, B]<br>[A, C]<br>[B, C]<br>[A, B, C] | 2<sup>n</sup>                             |
| Subsets in size range from a to b | JNumberTools.subsetsOf("A","B","C")<br>.inRange(2,3)<br>.forEach(System.out::println); | [A, B]<br>[A, C]<br>[B, C]<br>[A, B, C]                            | ∑ <sup>n</sup>C<sub>i</sub> for i= a to b |
|||||


***
**Factoradic (Factorial Number System): Following code will generate factoradic numbers
starting from 3 to 6.

```java
class Example {
    public static void main(String[] args) {
        Iterator<Factoradic> itr  = NumberTools
                .factoradic()
                .from(3)
                .build().iterator();

        //will print factoradic for 3,4 5 and 6
        for(int i=1; i<=4; i++) {
            System.out.print(itr.next() + " ");
        }    
    }
}
```

**Combinadic (Combinatorial Number System): Following code will generate combinadics from 10 to 15 of degree 5.

```java
class Example {
    public static void main(String[] args) {
        Iterable<Combinadic> iterable = JNumberTools.combinadic(5, 10, 15);

        //print combinadics of degree 5, from 10 to 15 
        for(Combinadic c: iterable) {
            System.out.println(c);
        }
    }
}
```



