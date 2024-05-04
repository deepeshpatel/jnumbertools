# JNumberTools V1.0.0
JNumberTools is the open source java-library of combinatorics and number-theory 

**Currently Available Algorithms**

1. Permutations (12 different types of permutation ranking and un-ranking)
2. Combinations (4 different types of combination ranking and un-ranking)
3. Set/subset generations
4. Factoradic number system aka Factoradic.
5. Combinadic number system aka Combinadic
6. Permutational Number System aka Permutadic (A new concept for nth k-Permutation)

***
**Permutation Generation Examples:**
[permutation examples](md_files/permutation_table.md)

<div>
<iframe src="/md_files/permutation_table.html" title="permutation examples">
</iframe>
</div>
<table>
    <tbody>
        <tr>
            <td>#</td>
            <td>Permutation Type</td>
            <td>Description</td>
            <td>API</td>
            <td>Output</td>
            <td>Count</td>
        </tr>
        <tr>
            <td>1</td>
            <td>
                Unique permutation in lex order
            </td>
            <td>
                All unique permutations of input elements in lex order.
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .unique()
    .forEach(System.out::println);
&nbsp;
JNumberTools.permutationsOf("A","B","C")
    .unique()
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A,B,C]&nbsp;<br>
                [A,C,B]&nbsp;<br>
                [B,A,C]&nbsp;<br>
                [B,C,A]&nbsp;<br>
                [C,A,B]&nbsp;<br>
                [C,B,A]
            </td>
            <td>m!</td>
        </tr>
        <tr>
            <td>2</td>
            <td>N<sup>th</sup> unique permutation in lex order</td>
            <td>
                Generate permutations with repeated values starting from 0<sup>th</sup> 
                permutation(input) and then generate every n<sup>th</sup> permutation in 
                lexicographical order of indices of input values.<br><br>
                This is important because say, if we need to generate next 100 trillionth permutation
                of 100 items then it will take months to compute if we go sequentially and then 
                increment to the desired permutation because the total # of permutations is 
                astronomical &nbsp;(100!= 9.3326 x 10<sup>157</sup>)
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .uniqueNth(increment)
    .forEach(System.out::println);
&nbsp;
JNumberTools
    .permutationsOf("A","B","C")
    .uniqueNth(increment)
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                    [A,B,C]<br>
                    [B,A,C]<br>
                    [C,A,B]
            </td>
            <td>m!/n</td>
        </tr>
        <tr>
            <td>3</td>
            <td>Repetitive Permutation in lex order</td>
            <td>
                This is same as generating base-n numbers of max size r-digits with given n symbols, 
                starting from zero in lex order.
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .repetitive(repetitionSize)
    .forEach(System.out::println);
    &nbsp;
JNumberTools
    .permutationsOf("A","B","C")
    .repetitive(repetitionSize)
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A, A]&nbsp;<br>
                [A, B]&nbsp;<br>
                [A, C]&nbsp;<br>
                [B, A]&nbsp;<br>
                [B, B]&nbsp;<br>
                [B, C]&nbsp;<br>
                [C, A]&nbsp;<br>
                [C, B]&nbsp;<br>
                [C, C]
            </td>
            <td>m<sup>r</sup></td>
        </tr>
        <tr>
            <td>4</td>
            <td>N<sup>th</sup> Repetitive Permutation in lex order</td>
            <td>
                This is same as generating AP series of base-n numbers with given 
                <i>n</i>-symbols and <i>a</i>=0, <i>d=m</i> and with max-digits = <i>r</i>
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .repetitiveNth(repetitionSize, increment)
    .forEach(System.out::println);
    &nbsp;
JNumberTools
    .permutationsOf("A","B","C")
    .repetitiveNth(repetitionSize, increment)
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A, A]<br>
                [B, A]<br>
                [C, A]
            </td>
            <td>m<sup>r</sup>/n</td>
        </tr>
        <tr>
            <td>5</td>
            <td><i>k</i>-permutation</td>
            <td>
                For a given <i>m </i>elements, it generates all unique permutations of size
                <i>k</i> where 0 ≤ <i>k</i> ≤ <i>m</i>
                <br>
                In number theory, this is also known as <i>k</i>-permutation.
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .k(k)
    .lexOrder()
    .forEach(System.out::println);
    &nbsp;
JNumberTools
    .permutationsOf("A","B","C")
    .k(k)
    .lexOrder()
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A, B]<br>
                [A, C]<br>
                [B, A]<br>
                [B, C]<br>
                [C, A]<br>
                [C, B]
            </td>
            <td> <sup>m</sup>P<sub>k</sub> </td>
        </tr>
        <tr>
            <td>6</td>
            <td>N<sup>th</sup> <i>k</i>-permutation</td>
            <td>
                Generates every n<sup>th</sup> k-permutation in lex order without explicitly 
                computing all the permutations preceding it.<br>
                This concept is important because the total number of permutations can grow astronomically
                large. For instance, the number of permutations of 100 elements selected 50 at a time is 
                <sup>100</sup>P<sub>50</sub> = 3.068518756 x 10<sup>93</sup>, which is way beyond the practical limit to be generated 
                sequentially to reach the desired permutation.<br>
                To achieve this, a new concept called Permutadic and Deep-code(an extension of Lehmer code)
                is used. Details can be found in the research paper - 
                <a href="https://papers.ssrn.com/sol3/papers.cfm?abstract_id=4174035">
                <strong>Generating the n<sup>th</sup> Lexicographical Element of a Mathematical k-Permutation using 
                Permutational Number System</strong></a>
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .k(k)
    .lexOrderNth(increment)
    .forEach(System.out::println);
&nbsp;
JNumberTools
    .permutationsOf("A","B","C")
    .k(k)
    .lexOrderNth(increment)
    .forEach(System.out::println);</code></pre>
            </td>
            <td> [A, B]<br> [B, A]<br> [C, A] </td>
            <td> <sup>m</sup>P<sub>k</sub>/n </td>
        </tr>
        <tr>
            <td>7</td>
            <td> <i>k</i>-permutation combination order </td>
            <td>
                Generates all k-permutations in the lexicographical order of combination. 
                For example, [C,A] comes before [B,C] because combination-wise [C,A] = [A,C] 
                which comes before [B,C]<BR>Note that the API does not sort the output to achieve 
                this, but it generates the permutation in said order, so it is very efficient.
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .k(k)
    .combinationOrder()
    .forEach(System.out::println);
    &nbsp;
JNumberTools
    .permutationsOf("A","B","C")
    .k(k).combinationOrder()
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A, B]<br>
                [B, A]<br>
                [A, C]<br>
                [C, A]<br>
                [B, C]<br>
                [C, B]
            </td>
            <td> <sup>m</sup>P<sub>k</sub> </td>
        </tr>
        <tr>
            <td>8</td>
            <td> N<sup>th</sup> <i>k</i>-permutation combination order N<sup>th</sup></td>
            <td>
                Generates every n<sup>th</sup> k-permutation in the lexicographical order of combination.<br>
                This API does not sort or search to achieve this but generates the desired permutation
                directly, so it is very efficient.
            </td>
            <td>
                <pre><code class="language-java">JNumberTools
    .permutationsOf(size)
    .k(k)
    .combinationOrderNth(increment)
    .forEach(System.out::println);
    &nbsp;
JNumberTools
    .permutationsOf("A","B","C")
    .k(k).combinationOrderNth(increment)
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A, B]<br>
                [A, C]<br>
                [B, C]
            </td>
            <td> <sup>m</sup>P<sub>k</sub>/n </td>
        </tr>
        <tr>
            <td>9</td>
            <td> Multi-set permutation in lex order </td>
            <td>
                Permutation, where every item has an associated frequency that denotes how many
                times an item can be repeated in a permutation.<br><br>For example, permutations of 
                3 apples and 2 oranges.
            </td>
            <td>
                <pre><code class="language-java">int[] multisetFreqArray = new int[]{1,2};
JNumberTools
    .permutationsOf(size)
    .multiset(multisetFreqArray)
    .forEach(System.out::println);
&nbsp;
JNumberTools
.permutationsOf("A","B")
.multiset(multisetFreqArray)
.forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A,B,B]<br>
                [B,A,B]<br>
                [B,B,A]
            </td>
            <td> ( ∑ ai . si )! / Π(si!) </td>
        </tr>
        <tr>
            <td> 10 </td>
            <td> N<sup>th</sup> multi-set permutation in lex order </td>
            <td>
                Generates every n<sup>th</sup> multiset-permutation.<br>This API does not search for the
                n<sup>th</sup> permutation in a sorted list but directly generates the next n<sup>th</sup> 
                multiset-permutation and hence it is very efficient.
            </td>
            <td>
                <pre><code class="language-java">int increment = 2;
int[] multisetFreqArray = new int[]{1,2};	
&nbsp;
JNumberTools
    .permutationsOf(size)
    .multisetNth(increment,multisetFreqArray)
    .forEach(System.out::println);
&nbsp;
JNumberTools
    .permutationsOf("A","B")
    .multisetNth(increment,multisetFreqArray)
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A,B,B]<br> [B,B,A]
            </td>
            <td> ( ∑ ai . si )! / (n*Π(si!)) </td>
        </tr>
        <tr>
            <td> 11 </td>
            <td> Rank of unique permutation </td>
            <td>
                Finds the rank of a given unique permutation. For example, [2,1,0] is the 5th permutation 
                of 3 elements starting from 0, hence its rank is 5.
            </td>
            <td>
                <pre><code class="language-java">int[] permutation = new int[]{1,3,0,2};
int rank = JNumberTools
    .rankOf()
    .uniquePermutation(permutation)
    .intValue();</code></pre>
            </td>
            <td> 10 </td>
            <td> NA </td>
        </tr>
        <tr>
            <td> 12 </td>
            <td> Rank of <i>k</i>-permutation </td>
            <td> Finds the rank of a given <i>k</i>-permutation </td>
            <td>
                <pre><code class="language-java">int[] permutation = new int[]{1,3,0};
int size=3;
int rank = JNumberTools.rankOf()
    .kPermutation(size, permutation).intValue();
                </code></pre>
            </td>
            <td>4</td>
            <td>
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>13</td>
            <td>
                Rank of repetitive permutation
            </td>
            <td>
                TODO: This can be easily achieved. It’s just a base conversion from a given number system to a decimal (base 10) number system.
            </td>
            <td>
                &nbsp;
            </td>
            <td>
                &nbsp;
            </td>
            <td>
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>14</td>
            <td>
                Rank of multiset permutation
            </td>
            <td>
                TODO: Coming soon
            </td>
            <td>
                &nbsp;
            </td>
            <td>
                &nbsp;
            </td>
            <td>
                &nbsp;
            </td>
        </tr>
    </tbody>
</table>


**Combination Generation Examples:**
<table>
    <tbody>
        <tr>
            <td> # </td>
            <td> Combination Type </td>
            <td> Description </td>
            <td> API </td>
            <td> Output </td>
            <td> Count </td>
        </tr>
        <tr>
            <td> 1 </td>
            <td> Unique Combination of size r </td>
            <td>
                Selection of r distinct items out of m items.In mathematics, this is also known as m-Choose-r.
                Generates all combinations in lex order.
            </td>
            <td>
<pre>
<code class="language-java">int m=3;
int size = 2;
&nbsp;
JNumberTools.combinationsOfnCr(n,size)
    .unique()
    .forEach(System.out::println);
&nbsp;
JNumberTools.combinationsOf(size,"A","B","C")
    .unique()
    .forEach(System.out::println);
</code>
</pre>
            </td>
            <td> [A, B]
                 [A, C]
                 [B, C]
            </td>
            <td> <sup>m</sup>C<sub>r</sub> </td>
        </tr>
        <tr>
            <td> 2 </td>
            <td> N<sup>th</sup> Unique Combination of size r </td>
            <td>
                    Same as m-Choose-r but generates every n<sup>th</sup> combination in lex order.&nbsp;
                    This concept is important because the count of combinations can grow astronomically
                    large. For example, to generate, say, the next 1 billionth combination of
                    34-Choose-17, &nbsp;we need to wait for days to generate the desired billionth
                    combination if we generate all combinations sequentially and then select
                    the billionth combination.
            </td>
            <td>
                <pre><code class="language-java">int m=3;
int size = 2;
int increment = 2;
&nbsp;
JNumberTools
    .combinationsOfnCr(m,size)
    .uniqueNth(increment)
.forEach(System.out::println);
&nbsp;
JNumberTools
    .combinationsOf(size,"A","B","C")
    .uniqueNth(increment)
    .forEach(System.out::println);
</code></pre>
            </td>
            <td> 
            [A, B]<br>
            [B, C]
            </td>
            <td> <sup>m</sup>C<sub>r</sub>/n </td>
        </tr>
        <tr>
            <td> 3 </td>
            <td> Repetitive Combination of size r </td>
            <td>
                Generates combinations of repeated values of size r in lexicographical order.
            </td>
            <td>
                <pre><code class="language-java">
int m=3;
int size = 2;
&nbsp;
JNumberTools.combinationsOfnCr(m,size)
    .repetitive()
    .forEach(System.out::println);
&nbsp;
JNumberTools
    .combinationsOf(size,"A","B","C")
    .repetitive()
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A, A]<br>
                [A, B]<br>
                [A, C]<br>
                [B, B]<br>
                [B, C]<br>
                [C, C]
            </td>
            <td> m+r-1 </td>
        </tr>
        <tr>
            <td> 4 </td>
            <td> Repetitive Combination of multiset </td>
            <td>
                Also known as multiset-combination.This is a special case of repetitive 
                combination where every item has an associated frequency that denotes how
                many times an item can be repeated in a combination. For example, combinations of
                3 apples and 2 oranges.
            </td>
            <td>
                <pre><code class="language-java">
int m=3;
int size = 2;
int[] multisetFreqArray = new int[]{1,2,1};
&nbsp;
JNumberTools.combinationsOfnCr(m,size)
    .repetitiveMultiset(multisetFreqArray)
    .forEach(System.out::println);
&nbsp;
JNumberTools.combinationsOf(size,"A","B","C")
    .repetitiveMultiset(multisetFreqArray)
    .forEach(System.out::println);</code></pre>
            </td>
            <td>
                [A, B]<br>
                [A, C]<br>
                [B, B]<br>
                [B, C]
            </td>
            <td> &nbsp; </td>
        </tr>
    </tbody>
</table>

**Subset Generation Examples:**
<table>
<tr>
<td>Type of set</td>
<td>API</td>
<td>Output</td>
<td>Count</td>
</tr>

<tr>
<td>All subsets</td>
<td>JNumberTools.subsetsOf("A","B","C")<br>.all()<br>.forEach(System.out::println); </td>
<td>
[]//represents φ set <br>
[A]<br>
[B]<br>
[C]<br>
[A, B]<br>
[A, C]<br>
[B, C]<br>
[A, B, C]</td>
<td>2<sup>n</sup> </td>
</tr>

<tr>
<td>Subsets in size-range from a to b</td>
<td>JNumberTools.subsetsOf("A","B","C")<br>.inRange(2,3)<br>.forEach(System.out::println);</td>
<td>[A, B]<br>[A, C]<br>[B, C]<br>[A, B, C]</td>
<td>∑ <sup>n</sup>C<sub>i</sub> for i= a to b</td>
</tr>
</table>









