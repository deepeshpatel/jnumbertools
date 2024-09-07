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
                Selection of r distinct items out of n elements. In mathematics, this is also known as n-Choose-r.
                Generates all combinations in lex order.
            </td>
            <td>
<pre>
<code class="language-java">int n = 3;
int r = 2;<br>
JNumberTools.combinations().unique(n, r)
    .lexOrder()
    .forEach(System.out::println);<br>
JNumberTools.combinations().unique(r,"A","B","C")
    .lexOrder()
    .forEach(System.out::println);
</code>
</pre>
            </td>
            <td> [A, B]
                 [A, C]
                 [B, C]
            </td>
            <td> <sup>n</sup>C<sub>r</sub> </td>
        </tr>
        <tr>
            <td> 2 </td>
            <td> M<sup>th</sup> unique combination of size r </td>
            <td>
                    Same as n-Choose-r but generates every m<sup>th</sup> combination in lex order starting from given index.&nbsp;
                    This concept is important because the count of combinations can grow astronomically
                    large. For example, to generate, say, the next 1 billionth combination of
                    34-Choose-17, &nbsp;we need to wait for days to generate the desired billionth
                    combination if we generate all combinations sequentially and then select
                    the billionth combination.
            </td>
            <td>
                <pre><code class="language-java">int n=3;
int r = 2;
int m = 2;
int start = 0;<br>
JNumberTools.combinations().unique(n, r)
        .lexOrderMth(m, start)
        .forEach(System.out::println);<br>
JNumberTools.combinations().unique(r,"A","B","C")
        .lexOrderMth(m, start)
        .forEach(System.out::println);
</code></pre>
            </td>
            <td> 
            [A, B]<br>
            [B, C]
            </td>
            <td> <sup>n</sup>C<sub>r</sub>/m </td>
        </tr>
        <tr>
            <td> 3 </td>
            <td> Repetitive Combination of size r </td>
            <td>
                Generates combinations with repeated elements allowed in lexicographical order.
            </td>
            <td>
                <pre><code class="language-java">int n=3;
int r = 2;<br>
JNumberTools.combinations()
.repetitive(n, r)
.lexOrder()
.forEach(System.out::println);<br>
JNumberTools.combinations()
.repetitive(r,"A","B","C")
.lexOrder()
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
            <td> <sup>(n+r-1)</sup>C<sub>r</sub> </td>
        </tr>
        <tr>
            <td>4</td>
            <td>M<sup>th</sup> repetitive combination</td>
            <td>Generates every m<sup>th</sup> combination with repeated elements in lex order</td>
            <td><pre><code class="language-java">int n=3;
int r = 2;<br>
JNumberTools.combinations()
    .repetitive(n, r)
    .lexOrderMth(m,start).build();<br>
JNumberTools.combinations()
.repetitive(r,"A","B","C")
.lexOrderMth(m, start).build();
</code></pre></td>
            <td>[A, C]</td>
            <td><sup>(n+r-1)</sup>C<sub>r</sub>/m</td>
        </tr>
        <tr>
            <td> 5 </td>
            <td> Multiset Combination  </td>
            <td>
                Special case of repetitive 
                combination where every element has an associated frequency that denotes how
                many times an element can be repeated in a combination. For example, combinations of
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