# JNumberTools V1.0.0
JNumberTools is the open source java-library of combinatorics and number-theory 

**Currently Available Algorithms**

1. Permutations (9 different types of permutation generation)
1. Combinations (4 different types of combination generation)
1. Set/subset generations
1. Factoradic sequence generation.
1. Combinadic sequence generation.
1. Permutadic (A new concept for Nth K-Permutation)

***
**Permutation Generation Examples:**

| #   | Type of Permutation                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | API Example                                                                                                          | Example Output                                                                         | Count                                                        |
|-----|------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|--------------------------------------------------------------|
| 1.  | Unique Permutations                            | All permutations of size equal to input in lex order of indices of input                                                                                                                                                                                                                                                                                                                                                                                                                              | NumberTools<br>.permutationsOf("A","B","C")<br>.unique()<br>.forEach(System.out::println);                           | [A, B, C]<br>[A, C, B]<br>[B, A, C]<br>[B, C, A]<br>[C, A, B]<br>[C, B, A]             | **n!**                                                       |
| 2.  | Nth Unique Permutation                         | Generate permutations with repeated values starting from 0<sup>th</sup> permutation(input) and<br> then generate every nth permutation in lexicographical order of indices of input values.<br> This is important because say, if we need to generate next 100 trillionth permutation<br>of 100 items then it will take months to compute if we go sequentially and then skip<br>the unwanted permutations because the total # of permutations is astronomical<br> (100!= 9.3326E X 10<sup>157</sup>) | NumberTools<br>.permutationsOf("A","B","C")<br>.uniqueNth(2)<br>.forEach(System.out::println);                       | [A, B, C]<br>[B, A, C]<br>[C, A, B]                                                    | **n! / m**                                                   |
| 3.  | Unique Permutation of Size k                   | Generates all unique permutations of size k.<br>In number theory this is also know as k-permutations                                                                                                                                                                                                                                                                                                                                                                                                  | NumberTools<br>.permutationsOf("A","B","C")<br>.k(2)<br>.forEach(System.out::println);                               | [A, B]<br>[B, A]<br>[A, C]<br>[C, A]<br>[B, C]<br>[C, B]                               | **<sup>n</sup>P<sub>k</sub>**                                |
| 4.  | Nth Unique Permutation of Size k               | Generates permutations of unique values of size k,<br> skipping to every nth permutation in lex order.                                                                                                                                                                                                                                                                                                                                                                                                | NumberTools<br>.permutationsOf("A","B","C")<br>.kNth(2,3)<br>.forEach(System.out::println);                          | [A, B]<br>[A, C]<br>[B, C]                                                             | **<sup>n</sup>P<sub>k</sub> / m**                            |
| 5.  | Repetitive Permutations of size r              | This is same as generating base-n numbers of max size r-digits<br>with given n-symbols, starting from zero in lex order                                                                                                                                                                                                                                                                                                                                                                               | NumberTools<br>.permutationsOf("A","B","C")<br>.repetitive(2)<br>.forEach(System.out::println);                      | [A, A]<br>[A, B]<br>[A, C]<br>[B, A]<br>[B, B]<br>[B, C]<br>[C, A]<br>[C, B]<br>[C, C] | **n<sup>r</sup>**                                            |
| 6.  | Nth Repetitive Permutation<br>of size r        | This is same as generating AP series of base-n numbers with given n-symbols<br>and a=0, d=m and with max-digits =r                                                                                                                                                                                                                                                                                                                                                                                    | NumberTools<br>.permutationsOf("A","B","C")<br>.repetitiveNth(2,3)//size 2 and m=3<br>.forEach(System.out::println); | [A, A]<br>[B, A]<br>[C, A]                                                             | **n<sup>r</sup>/m**                                          |
| 7.  | Repetitive Permutations<br>with limited supply | Also known as multiset-permutation.<br>This is special case of repetitive permutation where every item say,<br> a<sub>1</sub>,a<sub>2</sub>,a<sub>3</sub> ... has an associated number s<sub>1</sub>,s<sub>2</sub>,s<sub>3</sub>... which denotes how many<br>times an item can be repeated in a permutation                                                                                                                                                                                          | JJNumberTools.permutationsOf("A", "B")<br>.repetitiveWithSupply(3,1)//a 3 times<br>.forEach(System.out::println);    | [A, A, A, B]<br>[A, A, B, A]<br>[A, B, A, A]<br>[B, A, A, A]                           | **( ∑ a<sub>i</sub> . s<sub>i</sub> )! / Π(s<sub>i</sub>!)** |



**Combination Generation Examples:**

| #   | Type of Combination                           | Description                                                                                                                                                                                                                                                                                                                                                      | API                                                                                                                                     | Output                                           | Count                         |
|-----|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|-------------------------------|
| 1.  | Unique Combination of size r                  | Selection of r distinct items out of n items<br> In mathematics this is known as n-Choose-r                                                                                                                                                                                                                                                                      | JNumberTools.combinationsOf("A","B","C")<br>.unique(2)<br>.forEach(System.out::println);                                                | [A, B]<br>[A, C]<br>[B, C]                       | <sup>n</sup>C<sub>r</sub>     ||
| 2.  | Nth Unique Combination of size r              | Same as n-Choose-r but generating evey nth combination in lex order.<br>This is important because count of combinations can grow astronomically<br>large and to generate say, next 1 billionth combination of 34Choose17,<br>we do not like to wait for few 100 hours to generate billion combinations<br>sequentially and then selecting billionth combination. | JNumberTools.combinationsOf("A","B","C")<br>.uniqueNth(2,2)<br>.forEach(System.out::println);                                           | [A, B]<br>[B, C]                                 | <sup>n</sup>C<sub>r</sub> / m ||
| 3.  | Repetitive Combination of size r.             | Generates combinations of repeated values od size r.                                                                                                                                                                                                                                                                                                             | JNumberTools.combinationsOf("A","B")<br>.repetitive(3)<br>.forEach(System.out::println);                                                | [A, A, A]<br>[A, A, B]<br>[A, B, B]<br>[B, B, B] | <sup>n+r-1</sup>C<sub>r</sub> ||
| 4.  | Repetitive Combination<br>with limited supply | Also known as multiset-combination.<br>This is special case of repetitive combination where every item say,<br> a<sub>1</sub>,a<sub>2</sub>,a<sub>3</sub> ... has an associated number s<sub>1</sub>,s<sub>2</sub>,s<sub>3</sub>... which denotes how many<br>times an item can be repeated in a combination                                                     | JNumberTools.combinationsOf("A","B","C")<br>.repetitiveWithSupply(2,2,1,1) // size 2 and supply 2,1,1<br>.forEach(System.out::println); | [A, A]<br>[A, B]<br>[A, C]<br>[B, C]             ||
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

**Coming Soon**

1. Nth repetitive permutation with limited supply
1. Nth repetitive combination
1. Nth repetitive combination with limited supply
1. Mixed Radix Number System
1. Number Partition
1. Cartesian Product

