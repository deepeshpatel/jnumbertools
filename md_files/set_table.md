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