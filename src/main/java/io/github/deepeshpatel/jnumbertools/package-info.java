/**
 * <H1><Strong>JNumberTools Version 1.0.0: (A Combinatorics Library)</Strong></H1>
 *
 * <LI><a href="#up">Unique Permutations</a>: All unique (n!) permutations of input list</LI>
 * <LI><a href="#nup">N<sup>th</sup> unique permutation</a>: Every Nth unique permutation in lex order</LI>
 *
 * <LI><a href="#kp">K-Permutation</a>: Unique permutations of size K</LI>
 * <LI><a href="#nkp">N<sup>th</sup> K-Permutation</a>:Every Nth unique permutations of size K in lex order</LI>
 *
 * <LI><a href="#rp">Repetitive Permutation</a>: Repetitive permutations of given size</LI>
 * <LI><a href="#nrp">N<sup>th</sup> Repetitive Permutation</a>: Every Nth repetitive permutation of given size</LI>
 * <LI><a href="#rpls">Repetitive permutation of multiset</a>: Repetitive permutations of given size with constraint on repetition count</LI>
 *
 * <hr>
 * <Strong><p id="up">Generating unique permutations</p></Strong>
 * <pre>
 *     JNumberTools.permutationsOf("A","B","C")
 *                 .unique()
 *                 .forEach(System.out::println);
 *
 * will generate following (all possible unique permutations of A,B and C in lex order) -
 * [A, B, C]
 * [A, C, B]
 * [B, A, C]
 * [B, C, A]
 * [C, A, B]
 * [C, B, A]
 * </pre>
 * <hr>
 * <Strong><p id="nup">Generating Nth unique permutations</p></Strong>
 * <pre>
 *         JNumberTools.permutationsOf("A","B","C")
 *                 .uniqueNth(2)
 *                 .forEach(System.out::println);
 *
 *  will generate following (0<sup>th</sup>, 2<sup>nd</sup> and 4<sup>th</sup>) unique permutations of A,B and C in lex order)-
 *
 * [A, B, C]
 * [B, A, C]
 * [C, A, B]

 * </pre>
 * <hr>
 * <Strong><p id="kp">Generating K-permutations</p></Strong>
 *  <pre>
 *  JNumberTools.permutationsOf("A","B","C")
 *      .k(2)
 *      .forEach(System.out::println);
 *
 * will generate following (all possible unique permutations of size(K) 2)-
 * [A, B]
 * [B, A]
 * [A, C]
 * [C, A]
 * [B, C]
 * [C, B]
 *  </pre>
 * <hr>
 * <Strong><p id="nkp">Generating N<sup>th</sup> K permutation</p></Strong>
 * <pre>
 *  JNumberTools.permutationsOf("A","B","C")
 *      .kNth(2,2) //size =2 and increment to every 2<sup>nd</sup> permutation starting from 0<sup>th</sup>
 *      .forEach(System.out::println);
 *
 * will generate following (0<sup>th</sup>, 2<sup>nd</sup> and 4<sup>th</sup>) K-permutation of size(K)=2 -
 * [A, B]
 * [A, C]
 * [B, C]
 * </pre>
 *
 * <hr>
 * <Strong><p id="rp">Generating repetitive permutations</p></Strong>
 * <pre>
 *      JNumberTools.permutationsOf("A","B","C")
 *            .repetitive(2)
 *            .forEach(System.out::println);
 *
 * will generate following (all possible repetitive permutations of size 2) -
 * [A, A]
 * [A, B]
 * [A, C]
 * [B, A]
 * [B, B]
 * [B, C]
 * [C, A]
 * [C, B]
 * [C, C]
 * </pre>
 *
 * <hr>
 * <Strong><p id="nrp">Generating N<sup>th</sup> repetitive permutation</p></Strong>
 * <pre>
 *      JNumberTools.permutationsOf("A","B")
 *                 .repetitiveNth(3,2)
 *                 .forEach(System.out::println);
 *
 * will generate following (0<sup>th</sup>, 2<sup>nd</sup> 4<sup>th</sup> and 6<sup>th</sup>) repetitive permutation of "A" and "B" of size 3 -
 *
 * [A, A, A]
 * [A, B, A]
 * [B, A, A]
 * [B, B, A]
 * </pre>
 *
 * <hr>
 * <Strong><p id="rpls">Generating repetitive permutations of multiset</p></Strong>
 * <pre>
 *     //here A can be repeated max 2 times and B can not be repeated(1 time)
 *     JNumberTools.permutationsOf("A","B")
 *                 ..repetitiveMultiset(2,1)
 *                 .forEach(System.out::println);
 *
 * will generate following -
 * [A, A, B]
 * [A, B, A]
 * [B, A, A]
 * </pre>
 *
 */
package io.github.deepeshpatel.jnumbertools;