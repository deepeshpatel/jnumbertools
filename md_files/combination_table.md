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